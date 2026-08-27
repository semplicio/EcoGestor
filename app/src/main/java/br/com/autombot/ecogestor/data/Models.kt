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

enum class AppMode {
    HOUSEHOLD,
    BUSINESS;

    companion object {
        fun fromName(value: String?): AppMode =
            entries.firstOrNull { it.name == value } ?: HOUSEHOLD
    }
}

enum class FinanceKind {
    INCOME,
    EXPENSE;

    companion object {
        fun fromName(value: String): FinanceKind =
            entries.firstOrNull { it.name == value } ?: EXPENSE
    }
}

enum class HouseholdCategory(val label: String) {
    SALARY("Salário / renda"),
    EXTRA_INCOME("Renda extra"),
    HOUSING("Moradia"),
    SUPERMARKET("Supermercado"),
    FAIR("Feira"),
    COOKING_GAS("Gás de cozinha"),
    ENERGY("Energia"),
    WATER("Água"),
    INTERNET("Internet"),
    PHONE("Celular"),
    TV("TV / TV a cabo"),
    SUBSCRIPTIONS("Assinaturas"),
    GYM("Academia"),
    HEALTH("Saúde / convênio"),
    TRANSPORT("Transporte"),
    EDUCATION("Educação"),
    DELIVERY("Delivery"),
    LEISURE("Lazer"),
    PHARMACY("Farmácia"),
    CLOTHING("Roupas"),
    MAINTENANCE("Manutenção"),
    GIFTS("Presentes"),
    OTHER("Outros");

    companion object {
        fun fromName(value: String): HouseholdCategory =
            entries.firstOrNull { it.name == value } ?: OTHER
    }
}

data class HouseholdProfile(
    val name: String = "Minha casa",
    val monthlyIncome: Double = 0.0,
    val members: Int = 1
)

data class HouseholdEntry(
    val id: Long,
    val kind: FinanceKind,
    val category: HouseholdCategory,
    val title: String,
    val value: Double,
    val period: String,
    val date: String = "",
    val recurring: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class BudgetLimit(
    val id: Long,
    val category: HouseholdCategory,
    val monthlyLimit: Double,
    val createdAt: Long = System.currentTimeMillis()
)

data class RecurringBill(
    val id: Long,
    val title: String,
    val category: HouseholdCategory,
    val value: Double,
    val dueDay: Int,
    val active: Boolean = true,
    val paidPeriods: Set<String> = emptySet(),
    val createdAt: Long = System.currentTimeMillis()
)

data class SubscriptionEntry(
    val id: Long,
    val name: String,
    val value: Double,
    val dueDay: Int,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class FinancialGoal(
    val id: Long,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val deadline: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class GasPurchase(
    val id: Long,
    val value: Double,
    val purchaseDate: String,
    val durationDays: Int,
    val createdAt: Long = System.currentTimeMillis()
)
