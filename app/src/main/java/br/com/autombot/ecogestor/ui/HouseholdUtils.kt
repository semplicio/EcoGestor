package br.com.autombot.ecogestor.ui

import android.content.Context
import android.content.Intent
import br.com.autombot.ecogestor.data.BudgetLimit
import br.com.autombot.ecogestor.data.FinanceKind
import br.com.autombot.ecogestor.data.GasPurchase
import br.com.autombot.ecogestor.data.HouseholdCategory
import br.com.autombot.ecogestor.data.HouseholdEntry
import br.com.autombot.ecogestor.data.HouseholdProfile
import br.com.autombot.ecogestor.data.SubscriptionEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

internal fun householdIncomeForPeriod(
    profile: HouseholdProfile?,
    entries: List<HouseholdEntry>,
    period: String
): Double {
    val registered = entries
        .filter { it.period == period && it.kind == FinanceKind.INCOME }
        .sumOf { it.value }
    return if (registered > 0.0) registered else profile?.monthlyIncome ?: 0.0
}

internal fun householdExpensesForPeriod(entries: List<HouseholdEntry>, period: String): Double =
    entries.filter { it.period == period && it.kind == FinanceKind.EXPENSE }.sumOf { it.value }

internal fun expenseByCategory(
    entries: List<HouseholdEntry>,
    period: String,
    category: HouseholdCategory
): Double = entries
    .filter { it.period == period && it.kind == FinanceKind.EXPENSE && it.category == category }
    .sumOf { it.value }

internal fun budgetProgress(
    budget: BudgetLimit,
    entries: List<HouseholdEntry>,
    period: String
): Float {
    if (budget.monthlyLimit <= 0.0) return 0f
    return (expenseByCategory(entries, period, budget.category) / budget.monthlyLimit)
        .toFloat()
        .coerceAtLeast(0f)
}

internal fun householdInsights(
    profile: HouseholdProfile?,
    entries: List<HouseholdEntry>,
    subscriptions: List<SubscriptionEntry>,
    budgets: List<BudgetLimit>
): List<String> {
    val current = periodForOffset(0)
    val previous = periodForOffset(-1)
    val currentExpenses = householdExpensesForPeriod(entries, current)
    val previousExpenses = householdExpensesForPeriod(entries, previous)
    val income = householdIncomeForPeriod(profile, entries, current)
    val insights = mutableListOf<String>()

    if (profile == null) {
        insights += "Cadastre sua casa e a renda mensal para o EcoGestor calcular quanto do orçamento já está comprometido."
    }

    if (currentExpenses <= 0.0) {
        insights += "Registre seus gastos do mês para descobrir onde o dinheiro está sendo usado e onde existe espaço para economizar."
    } else {
        if (income > 0.0) {
            val commitment = ((currentExpenses / income) * 100).roundToInt()
            insights += "Os gastos lançados já representam $commitment% da renda considerada neste mês."
        }

        val largest = HouseholdCategory.entries
            .filterNot { it == HouseholdCategory.SALARY || it == HouseholdCategory.EXTRA_INCOME }
            .map { it to expenseByCategory(entries, current, it) }
            .maxByOrNull { it.second }

        if (largest != null && largest.second > 0.0) {
            val share = ((largest.second / currentExpenses) * 100).roundToInt()
            insights += "${largest.first.label} é hoje a maior categoria de gasto: ${formatCurrency(largest.second)} ($share% das despesas do mês)."
        }

        if (previousExpenses > 0.0) {
            val difference = ((currentExpenses - previousExpenses) / previousExpenses) * 100.0
            if (abs(difference) >= 1.0) {
                val value = abs(difference).roundToInt()
                insights += if (difference > 0) {
                    "Seus gastos estão $value% maiores que no mês anterior. Revise as categorias que mais cresceram."
                } else {
                    "Seus gastos estão $value% menores que no mês anterior. Você economizou ${formatCurrency(previousExpenses - currentExpenses)} até agora."
                }
            }
        }

        val delivery = expenseByCategory(entries, current, HouseholdCategory.DELIVERY)
        if (delivery > 0.0 && delivery / currentExpenses >= 0.10) {
            insights += "Delivery representa ${((delivery / currentExpenses) * 100).roundToInt()}% dos gastos do mês. Reduzir uma parte pode gerar economia imediata."
        }
    }

    val monthlySubscriptions = subscriptions.filter { it.active }.sumOf { it.value }
    if (monthlySubscriptions > 0.0) {
        insights += "Assinaturas ativas custam ${formatCurrency(monthlySubscriptions)}/mês e ${formatCurrency(monthlySubscriptions * 12)}/ano."
    }

    budgets.forEach { budget ->
        val progress = budgetProgress(budget, entries, current)
        if (progress >= 1f) {
            insights += "O orçamento de ${budget.category.label} já ultrapassou o limite de ${formatCurrency(budget.monthlyLimit)}."
        } else if (progress >= 0.85f) {
            insights += "Você já utilizou ${(progress * 100).roundToInt()}% do orçamento de ${budget.category.label}."
        }
    }

    return insights.distinct().take(4).ifEmpty {
        listOf("Mantenha os lançamentos atualizados para o EcoGestor identificar oportunidades de economia.")
    }
}

internal fun gasForecastText(purchase: GasPurchase?): String {
    if (purchase == null) return "Cadastre uma compra de gás para acompanhar custo e duração."
    if (purchase.durationDays <= 0) return "Informe quantos dias o botijão durou para calcular o custo médio."

    val monthlyCost = purchase.value / purchase.durationDays * 30.0
    val format = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).apply { isLenient = false }
    val parsed = runCatching { format.parse(purchase.purchaseDate) }.getOrNull()
    val forecast = parsed?.let { date ->
        Calendar.getInstance().apply {
            time = date
            add(Calendar.DAY_OF_MONTH, purchase.durationDays)
        }.time
    }

    return if (forecast != null) {
        "Custo médio aproximado: ${formatCurrency(monthlyCost)}/mês. Próxima troca estimada: ${format.format(forecast)}."
    } else {
        "Custo médio aproximado: ${formatCurrency(monthlyCost)}/mês para uma duração de ${purchase.durationDays} dias."
    }
}

internal fun shareHouseholdCsv(
    context: Context,
    profile: HouseholdProfile?,
    entries: List<HouseholdEntry>
) {
    val current = periodForOffset(0)
    val income = householdIncomeForPeriod(profile, entries, current)
    val expenses = householdExpensesForPeriod(entries, current)
    val lines = mutableListOf<String>()
    lines += "EcoGestor - resumo doméstico"
    lines += "Perfil;${csvValue(profile?.name ?: "Minha casa")}"
    lines += "Período;$current"
    lines += "Renda considerada;${String.format(Locale.US, "%.2f", income)}"
    lines += "Despesas;${String.format(Locale.US, "%.2f", expenses)}"
    lines += "Saldo;${String.format(Locale.US, "%.2f", income - expenses)}"
    lines += ""
    lines += "Tipo;Categoria;Descrição;Valor;Período;Data"
    entries.sortedByDescending { it.createdAt }.forEach { entry ->
        lines += listOf(
            if (entry.kind == FinanceKind.INCOME) "Entrada" else "Saída",
            csvValue(entry.category.label),
            csvValue(entry.title),
            String.format(Locale.US, "%.2f", entry.value),
            csvValue(entry.period),
            csvValue(entry.date)
        ).joinToString(";")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_SUBJECT, "EcoGestor - relatório doméstico $current")
        putExtra(Intent.EXTRA_TEXT, lines.joinToString("\n"))
    }
    context.startActivity(Intent.createChooser(intent, "Exportar relatório EcoGestor"))
}

private fun csvValue(value: String): String =
    value.replace(";", ",").replace("\n", " ").trim()
