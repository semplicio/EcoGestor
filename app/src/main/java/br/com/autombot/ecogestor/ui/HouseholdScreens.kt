package br.com.autombot.ecogestor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.autombot.ecogestor.data.BudgetLimit
import br.com.autombot.ecogestor.data.FinanceKind
import br.com.autombot.ecogestor.data.FinancialGoal
import br.com.autombot.ecogestor.data.GasPurchase
import br.com.autombot.ecogestor.data.HouseholdCategory
import br.com.autombot.ecogestor.data.HouseholdEntry
import br.com.autombot.ecogestor.data.HouseholdProfile
import br.com.autombot.ecogestor.data.RecurringBill
import br.com.autombot.ecogestor.data.SubscriptionEntry
import br.com.autombot.ecogestor.ui.theme.EcoDark
import br.com.autombot.ecogestor.ui.theme.EcoGreen
import br.com.autombot.ecogestor.ui.theme.EcoGreenLight
import br.com.autombot.ecogestor.ui.theme.EcoTeal
import kotlin.math.roundToInt

@Composable
internal fun HouseholdHomeScreen(
    profile: HouseholdProfile?,
    entries: List<HouseholdEntry>,
    budgets: List<BudgetLimit>,
    bills: List<RecurringBill>,
    subscriptions: List<SubscriptionEntry>,
    gasPurchases: List<GasPurchase>,
    onAddEntry: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenBills: () -> Unit,
    onOpenMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val period = periodForOffset(0)
    val income = householdIncomeForPeriod(profile, entries, period)
    val expenses = householdExpensesForPeriod(entries, period)
    val balance = income - expenses
    val recurringCommitment = bills.filter { it.active }.sumOf { it.value } +
        subscriptions.filter { it.active }.sumOf { it.value }
    val expenseEntries = entries.filter { it.period == period && it.kind == FinanceKind.EXPENSE }
    val categoryTotals = HouseholdCategory.entries.map { category ->
        category to expenseEntries.filter { it.category == category }.sumOf { it.value }
    }.filter { it.second > 0.0 }.sortedByDescending { it.second }
    val insights = householdInsights(profile, entries, subscriptions, budgets)
    val latestGas = gasPurchases.maxByOrNull { it.createdAt }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Minha casa", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                profile?.name ?: "Organize sua renda, seus gastos e suas metas em um só lugar.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (profile == null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Configure seu orçamento doméstico", fontWeight = FontWeight.Bold)
                        Text(
                            "Informe o nome do perfil e sua renda mensal para calcular saldo e percentual comprometido.",
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        FilledTonalButton(onClick = onEditProfile, modifier = Modifier.padding(top = 12.dp)) {
                            Icon(Icons.Default.Home, contentDescription = null)
                            Text("Cadastrar minha casa", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = EcoDark),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Saldo disponível em $period", color = MaterialTheme.colorScheme.surface)
                    Text(
                        formatCurrency(balance),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (balance >= 0) EcoGreenLight else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        "Renda considerada ${formatCurrency(income)} • Gastos ${formatCurrency(expenses)}",
                        color = MaterialTheme.colorScheme.surface,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard("Renda", formatCurrency(income), Modifier.weight(1f))
                SummaryCard("Gastos", formatCurrency(expenses), Modifier.weight(1f))
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Compromissos fixos", fontWeight = FontWeight.Bold)
                            Text("Contas + assinaturas ativas", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(formatCurrency(recurringCommitment), fontWeight = FontWeight.Bold, color = EcoTeal)
                    }
                    if (income > 0.0) {
                        val ratio = (recurringCommitment / income).toFloat().coerceIn(0f, 1f)
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier.fillMaxWidth().height(8.dp)
                        )
                        Text(
                            "${(recurringCommitment / income * 100).roundToInt()}% da renda planejada",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    TextButton(onClick = onOpenBills) { Text("Ver contas e assinaturas") }
                }
            }
        }

        item {
            Text("Raio-X dos meus gastos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (categoryTotals.isEmpty()) {
            item {
                EmptyHouseholdCard("Ainda não há despesas em $period. Registre seu primeiro gasto para montar o Raio-X.")
            }
        } else {
            items(categoryTotals.take(6).size) { index ->
                val (category, value) = categoryTotals[index]
                val share = if (expenses > 0) (value / expenses).toFloat() else 0f
                CategoryShareCard(category, value, share)
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null)
                        Text("Oportunidades encontradas", fontWeight = FontWeight.Bold)
                    }
                    insights.forEach { Text("• $it") }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Text("Gás de cozinha", fontWeight = FontWeight.Bold)
                    Text(gasForecastText(latestGas), modifier = Modifier.padding(top = 6.dp))
                    TextButton(onClick = onOpenMore) { Text("Ver controle de gás") }
                }
            }
        }

        item {
            Button(
                onClick = onAddEntry,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Registrar entrada ou gasto", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
internal fun HouseholdTransactionsScreen(
    entries: List<HouseholdEntry>,
    onAdd: () -> Unit,
    onEdit: (HouseholdEntry) -> Unit,
    onDelete: (HouseholdEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    var search by remember { mutableStateOf("") }
    var kindFilter by remember { mutableStateOf<FinanceKind?>(null) }

    val filtered = entries.filter { entry ->
        val matchesText = search.isBlank() ||
            entry.title.contains(search, ignoreCase = true) ||
            entry.category.label.contains(search, ignoreCase = true) ||
            entry.period.contains(search, ignoreCase = true)
        val matchesKind = kindFilter == null || entry.kind == kindFilter
        matchesText && matchesKind
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Movimentos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Entradas e saídas ficam registradas no histórico e alimentam o dashboard.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text("Buscar descrição, categoria ou mês") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = kindFilter == null, onClick = { kindFilter = null }, label = { Text("Todos") })
                FilterChip(selected = kindFilter == FinanceKind.INCOME, onClick = { kindFilter = FinanceKind.INCOME }, label = { Text("Entradas") })
                FilterChip(selected = kindFilter == FinanceKind.EXPENSE, onClick = { kindFilter = FinanceKind.EXPENSE }, label = { Text("Saídas") })
            }
        }
        item {
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Adicionar movimento", modifier = Modifier.padding(start = 8.dp))
            }
        }

        if (filtered.isEmpty()) {
            item { EmptyHouseholdCard("Nenhum lançamento encontrado.") }
        } else {
            items(filtered.size) { index ->
                HouseholdEntryCard(filtered[index], onEdit, onDelete)
            }
        }
    }
}

@Composable
internal fun HouseholdBillsScreen(
    bills: List<RecurringBill>,
    subscriptions: List<SubscriptionEntry>,
    onAddBill: () -> Unit,
    onEditBill: (RecurringBill) -> Unit,
    onDeleteBill: (RecurringBill) -> Unit,
    onToggleBillActive: (RecurringBill) -> Unit,
    onToggleBillPaid: (RecurringBill) -> Unit,
    onAddSubscription: () -> Unit,
    onEditSubscription: (SubscriptionEntry) -> Unit,
    onDeleteSubscription: (SubscriptionEntry) -> Unit,
    onToggleSubscription: (SubscriptionEntry) -> Unit,
    onLaunchSubscription: (SubscriptionEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val period = periodForOffset(0)
    val monthlySubscriptions = subscriptions.filter { it.active }.sumOf { it.value }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Contas recorrentes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Controle vencimentos e marque o que já foi pago em $period.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Button(onClick = onAddBill, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null)
                Text("Adicionar conta recorrente", modifier = Modifier.padding(start = 8.dp))
            }
        }

        if (bills.isEmpty()) {
            item { EmptyHouseholdCard("Cadastre internet, aluguel, academia, convênio, escola e outras contas fixas.") }
        } else {
            items(bills.size) { index ->
                val bill = bills[index]
                val paid = period in bill.paidPeriods
                Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(bill.title, fontWeight = FontWeight.Bold)
                                Text("${bill.category.label} • vence dia ${bill.dueDay}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(formatCurrency(bill.value), fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(checked = bill.active, onCheckedChange = { onToggleBillActive(bill) })
                                Text(if (bill.active) "Ativa" else "Pausada")
                            }
                            Row {
                                IconButton(onClick = { onEditBill(bill) }) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                                IconButton(onClick = { onDeleteBill(bill) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir") }
                            }
                        }
                        FilledTonalButton(onClick = { onToggleBillPaid(bill) }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Paid, contentDescription = null)
                            Text(if (paid) "Desfazer pagamento do mês" else "Marcar paga e lançar gasto", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }

        item {
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            Text("Assinaturas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "${formatCurrency(monthlySubscriptions)}/mês • ${formatCurrency(monthlySubscriptions * 12)}/ano",
                color = EcoTeal,
                fontWeight = FontWeight.SemiBold
            )
        }
        item {
            Button(onClick = onAddSubscription, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Subscriptions, contentDescription = null)
                Text("Adicionar assinatura", modifier = Modifier.padding(start = 8.dp))
            }
        }

        if (subscriptions.isEmpty()) {
            item { EmptyHouseholdCard("Cadastre Netflix, Spotify, streaming, nuvem e outros serviços mensais.") }
        } else {
            items(subscriptions.size) { index ->
                val item = subscriptions[index]
                Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold)
                                Text("Cobrança dia ${item.dueDay} • ${formatCurrency(item.value * 12)}/ano", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(formatCurrency(item.value), fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(checked = item.active, onCheckedChange = { onToggleSubscription(item) })
                                Text(if (item.active) "Ativa" else "Pausada")
                            }
                            Row {
                                IconButton(onClick = { onEditSubscription(item) }) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                                IconButton(onClick = { onDeleteSubscription(item) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir") }
                            }
                        }
                        FilledTonalButton(onClick = { onLaunchSubscription(item) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Lançar cobrança em $period")
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun HouseholdFinancialGoalsScreen(
    goals: List<FinancialGoal>,
    onAdd: () -> Unit,
    onEdit: (FinancialGoal) -> Unit,
    onDelete: (FinancialGoal) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Metas financeiras", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Defina objetivos e acompanhe quanto já conseguiu guardar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Savings, contentDescription = null)
                Text("Criar nova meta", modifier = Modifier.padding(start = 8.dp))
            }
        }

        if (goals.isEmpty()) {
            item { EmptyHouseholdCard("Exemplo: reserva de emergência, viagem, quitar dívida ou guardar R$ 5.000.") }
        } else {
            items(goals.size) { index ->
                val goal = goals[index]
                val progress = if (goal.targetAmount > 0) (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(goal.title, fontWeight = FontWeight.Bold)
                                if (goal.deadline.isNotBlank()) Text("Prazo: ${goal.deadline}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row {
                                IconButton(onClick = { onEdit(goal) }) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                                IconButton(onClick = { onDelete(goal) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir") }
                            }
                        }
                        Text("${formatCurrency(goal.savedAmount)} de ${formatCurrency(goal.targetAmount)}", modifier = Modifier.padding(top = 8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 4.dp),
                            color = EcoGreen
                        )
                        Text("${(progress * 100).roundToInt()}% concluído", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun CategoryShareCard(category: HouseholdCategory, value: Double, share: Float) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(category.label, fontWeight = FontWeight.SemiBold)
                Text(formatCurrency(value), fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { share.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(7.dp).padding(top = 4.dp),
                color = EcoGreen
            )
            Text("${(share * 100).roundToInt()}% dos gastos", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun HouseholdEntryCard(
    entry: HouseholdEntry,
    onEdit: (HouseholdEntry) -> Unit,
    onDelete: (HouseholdEntry) -> Unit
) {
    Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = if (entry.kind == FinanceKind.INCOME) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(if (entry.kind == FinanceKind.INCOME) Icons.Default.Savings else Icons.Default.Paid, contentDescription = null)
                }
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(entry.title, fontWeight = FontWeight.Bold)
                Text("${entry.category.label} • ${entry.period}${if (entry.date.isNotBlank()) " • ${entry.date}" else ""}", style = MaterialTheme.typography.bodySmall)
                if (entry.recurring) Text("Recorrente", style = MaterialTheme.typography.labelSmall, color = EcoTeal)
                if (entry.notes.isNotBlank() && !entry.notes.startsWith("__")) Text(entry.notes, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    (if (entry.kind == FinanceKind.INCOME) "+ " else "- ") + formatCurrency(entry.value),
                    fontWeight = FontWeight.Bold,
                    color = if (entry.kind == FinanceKind.INCOME) EcoGreen else MaterialTheme.colorScheme.onSurface
                )
                Row {
                    IconButton(onClick = { onEdit(entry) }) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                    IconButton(onClick = { onDelete(entry) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir") }
                }
            }
        }
    }
}

@Composable
internal fun EmptyHouseholdCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
