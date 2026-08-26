package br.com.autombot.ecogestor.ui

import br.com.autombot.ecogestor.data.CompanyProfile
import br.com.autombot.ecogestor.data.ConsumptionCategory
import br.com.autombot.ecogestor.data.ConsumptionEntry
import br.com.autombot.ecogestor.data.SustainabilityGoal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

internal data class Metric(
    val title: String,
    val value: String,
    val detail: String,
    val positive: Boolean
)

internal fun periodForOffset(monthOffset: Int): String {
    val calendar = Calendar.getInstance().apply { add(Calendar.MONTH, monthOffset) }
    return SimpleDateFormat("MM/yyyy", Locale("pt", "BR")).format(calendar.time)
}

internal fun formatCurrency(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)

internal fun formatQuantity(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString()
    else String.format(Locale("pt", "BR"), "%.2f", value)

internal fun parseDecimal(raw: String): Double? {
    val value = raw.trim()
    if (value.isEmpty()) return null
    val normalized = if (value.contains(',')) {
        value.replace(".", "").replace(',', '.')
    } else value
    return normalized.toDoubleOrNull()
}

internal fun categoryShortName(category: ConsumptionCategory): String = when (category) {
    ConsumptionCategory.ENERGY -> "Energia"
    ConsumptionCategory.WATER -> "Água"
    ConsumptionCategory.FUEL -> "Combustível"
    ConsumptionCategory.MATERIALS -> "Materiais"
}

internal fun comparisonText(current: Double, previous: Double): Pair<String, Boolean> {
    if (previous <= 0) return "Sem comparação anterior" to true
    val difference = ((current - previous) / previous) * 100
    if (abs(difference) < 0.5) return "Estável" to true
    val rounded = abs(difference).roundToInt()
    return if (difference < 0) "$rounded% menor" to true else "$rounded% maior" to false
}

internal fun monthlySavings(
    current: List<ConsumptionEntry>,
    previous: List<ConsumptionEntry>
): Double = ConsumptionCategory.entries.sumOf { category ->
    val currentValue = current.filter { it.category == category }.sumOf { it.value }
    val previousValue = previous.filter { it.category == category }.sumOf { it.value }
    if (previousValue > 0) max(previousValue - currentValue, 0.0) else 0.0
}

internal fun calculateEcoScore(
    company: CompanyProfile?,
    currentEntries: List<ConsumptionEntry>,
    goals: List<SustainabilityGoal>
): Int {
    val companyPoints = if (company != null) 30 else 0
    val coveragePoints = currentEntries.map { it.category }.distinct().size * 10
    val goalPoints = if (goals.isEmpty()) 0 else {
        ((goals.map { it.progressPercent }.average() / 100.0) * 30.0).roundToInt()
    }
    return (companyPoints + coveragePoints + goalPoints).coerceIn(0, 100)
}

internal fun ecoScoreLabel(score: Int): String = when {
    score >= 90 -> "Eco Empresa Destaque"
    score >= 80 -> "Eco Ouro"
    score >= 65 -> "Eco Prata"
    score >= 50 -> "Eco Bronze"
    score > 0 -> "Eco Iniciante"
    else -> "Comece seus registros"
}

internal fun buildRecommendation(
    company: CompanyProfile?,
    current: List<ConsumptionEntry>,
    previous: List<ConsumptionEntry>
): Pair<String, String> {
    if (company == null) {
        return "Primeiro passo" to "Cadastre seu negócio para organizar indicadores e recomendações."
    }
    if (current.isEmpty()) {
        return "Registre o consumo do mês" to "Adicione energia, água, combustível e materiais para começar a medir sua eficiência."
    }
    if (previous.isEmpty()) {
        return "Crie uma base de comparação" to "Cadastre também o mês anterior para visualizar aumentos, reduções e economia real."
    }

    val largestIncrease = ConsumptionCategory.entries.mapNotNull { category ->
        val currentValue = current.filter { it.category == category }.sumOf { it.value }
        val previousValue = previous.filter { it.category == category }.sumOf { it.value }
        if (previousValue > 0 && currentValue > previousValue) {
            Triple(category, currentValue - previousValue, previousValue)
        } else null
    }.maxByOrNull { it.second }

    if (largestIncrease != null) {
        val percentage = ((largestIncrease.second / largestIncrease.third) * 100).roundToInt()
        return "Atenção ao consumo" to
            "${largestIncrease.first.label} aumentou aproximadamente $percentage% em relação ao mês anterior."
    }

    val totalCurrent = current.sumOf { it.value }
    val totalPrevious = previous.sumOf { it.value }
    if (totalCurrent < totalPrevious) {
        return "Boa evolução" to
            "Seus gastos monitorados caíram ${formatCurrency(totalPrevious - totalCurrent)} em relação ao mês anterior."
    }

    return "Continue monitorando" to
        "Mantenha os lançamentos atualizados para identificar novas oportunidades de economia."
}
