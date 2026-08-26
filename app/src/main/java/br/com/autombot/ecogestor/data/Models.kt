package br.com.autombot.ecogestor.data

enum class ConsumptionCategory(
    val label: String,
    val unit: String
) {
    ENERGY("Energia elétrica", "kWh"),
    WATER("Água", "m³"),
    FUEL("Combustível", "litros"),
    MATERIALS("Materiais", "itens");

    companion object {
        fun fromName(value: String): ConsumptionCategory =
            entries.firstOrNull { it.name == value } ?: MATERIALS
    }
}

data class CompanyProfile(
    val name: String,
    val document: String = "",
    val businessType: String = "MEI",
    val segment: String = "",
    val collaborators: Int = 0
)

data class ConsumptionEntry(
    val id: Long,
    val category: ConsumptionCategory,
    val quantity: Double,
    val value: Double,
    val period: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class SustainabilityGoal(
    val id: Long,
    val title: String,
    val targetPercent: Int,
    val progressPercent: Int = 0,
    val deadline: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
