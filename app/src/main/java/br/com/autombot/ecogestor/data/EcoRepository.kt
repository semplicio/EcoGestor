package br.com.autombot.ecogestor.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class EcoRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun isModeConfigured(): Boolean = preferences.getBoolean(KEY_MODE_CONFIGURED, false)

    fun loadSelectedMode(): AppMode =
        AppMode.fromName(preferences.getString(KEY_SELECTED_MODE, AppMode.HOUSEHOLD.name))

    fun completeModeSetup(mode: AppMode) {
        preferences.edit()
            .putBoolean(KEY_MODE_CONFIGURED, true)
            .putString(KEY_SELECTED_MODE, mode.name)
            .apply()
    }

    fun saveSelectedMode(mode: AppMode) {
        preferences.edit().putString(KEY_SELECTED_MODE, mode.name).apply()
    }

    fun loadCompany(): CompanyProfile? {
        val raw = preferences.getString(KEY_COMPANY, null) ?: return null
        return runCatching {
            val json = JSONObject(raw)
            CompanyProfile(
                name = json.optString("name"),
                document = json.optString("document"),
                businessType = json.optString("businessType", "MEI"),
                segment = json.optString("segment"),
                collaborators = json.optInt("collaborators", 0)
            )
        }.getOrNull()
    }

    fun saveCompany(profile: CompanyProfile) {
        val json = JSONObject()
            .put("name", profile.name)
            .put("document", profile.document)
            .put("businessType", profile.businessType)
            .put("segment", profile.segment)
            .put("collaborators", profile.collaborators)

        preferences.edit().putString(KEY_COMPANY, json.toString()).apply()
    }

    fun loadConsumptions(): List<ConsumptionEntry> {
        val raw = preferences.getString(KEY_CONSUMPTIONS, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val json = array.getJSONObject(index)
                    add(
                        ConsumptionEntry(
                            id = json.optLong("id"),
                            category = ConsumptionCategory.fromName(json.optString("category")),
                            quantity = json.optDouble("quantity", 0.0),
                            value = json.optDouble("value", 0.0),
                            period = json.optString("period"),
                            createdAt = json.optLong("createdAt", 0L)
                        )
                    )
                }
            }.sortedByDescending { it.createdAt }
        }.getOrDefault(emptyList())
    }

    fun saveConsumptions(entries: List<ConsumptionEntry>) {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(
                JSONObject()
                    .put("id", entry.id)
                    .put("category", entry.category.name)
                    .put("quantity", entry.quantity)
                    .put("value", entry.value)
                    .put("period", entry.period)
                    .put("createdAt", entry.createdAt)
            )
        }
        preferences.edit().putString(KEY_CONSUMPTIONS, array.toString()).apply()
    }

    fun loadGoals(): List<SustainabilityGoal> {
        val raw = preferences.getString(KEY_GOALS, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val json = array.getJSONObject(index)
                    add(
                        SustainabilityGoal(
                            id = json.optLong("id"),
                            title = json.optString("title"),
                            targetPercent = json.optInt("targetPercent", 0),
                            progressPercent = json.optInt("progressPercent", 0),
                            deadline = json.optString("deadline"),
                            createdAt = json.optLong("createdAt", 0L)
                        )
                    )
                }
            }.sortedByDescending { it.createdAt }
        }.getOrDefault(emptyList())
    }

    fun saveGoals(goals: List<SustainabilityGoal>) {
        val array = JSONArray()
        goals.forEach { goal ->
            array.put(
                JSONObject()
                    .put("id", goal.id)
                    .put("title", goal.title)
                    .put("targetPercent", goal.targetPercent)
                    .put("progressPercent", goal.progressPercent)
                    .put("deadline", goal.deadline)
                    .put("createdAt", goal.createdAt)
            )
        }
        preferences.edit().putString(KEY_GOALS, array.toString()).apply()
    }

    fun loadHouseholdProfile(): HouseholdProfile? {
        val raw = preferences.getString(KEY_HOUSEHOLD_PROFILE, null) ?: return null
        return runCatching {
            val json = JSONObject(raw)
            HouseholdProfile(
                name = json.optString("name", "Minha casa"),
                monthlyIncome = json.optDouble("monthlyIncome", 0.0),
                members = json.optInt("members", 1).coerceAtLeast(1)
            )
        }.getOrNull()
    }

    fun saveHouseholdProfile(profile: HouseholdProfile) {
        val json = JSONObject()
            .put("name", profile.name)
            .put("monthlyIncome", profile.monthlyIncome)
            .put("members", profile.members)
        preferences.edit().putString(KEY_HOUSEHOLD_PROFILE, json.toString()).apply()
    }

    fun loadHouseholdEntries(): List<HouseholdEntry> {
        val raw = preferences.getString(KEY_HOUSEHOLD_ENTRIES, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val json = array.getJSONObject(index)
                    add(
                        HouseholdEntry(
                            id = json.optLong("id"),
                            kind = FinanceKind.fromName(json.optString("kind")),
                            category = HouseholdCategory.fromName(json.optString("category")),
                            title = json.optString("title"),
                            value = json.optDouble("value", 0.0),
                            period = json.optString("period"),
                            date = json.optString("date"),
                            recurring = json.optBoolean("recurring", false),
                            notes = json.optString("notes"),
                            createdAt = json.optLong("createdAt", 0L)
                        )
                    )
                }
            }.sortedByDescending { it.createdAt }
        }.getOrDefault(emptyList())
    }

    fun saveHouseholdEntries(entries: List<HouseholdEntry>) {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(
                JSONObject()
                    .put("id", entry.id)
                    .put("kind", entry.kind.name)
                    .put("category", entry.category.name)
                    .put("title", entry.title)
                    .put("value", entry.value)
                    .put("period", entry.period)
                    .put("date", entry.date)
                    .put("recurring", entry.recurring)
                    .put("notes", entry.notes)
                    .put("createdAt", entry.createdAt)
            )
        }
        preferences.edit().putString(KEY_HOUSEHOLD_ENTRIES, array.toString()).apply()
    }

    fun loadBudgets(): List<BudgetLimit> {
        val raw = preferences.getString(KEY_BUDGETS, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val json = array.getJSONObject(index)
                    add(
                        BudgetLimit(
                            id = json.optLong("id"),
                            category = HouseholdCategory.fromName(json.optString("category")),
                            monthlyLimit = json.optDouble("monthlyLimit", 0.0),
                            createdAt = json.optLong("createdAt", 0L)
                        )
                    )
                }
            }.sortedBy { it.category.label }
        }.getOrDefault(emptyList())
    }

    fun saveBudgets(items: List<BudgetLimit>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("category", item.category.name)
                    .put("monthlyLimit", item.monthlyLimit)
                    .put("createdAt", item.createdAt)
            )
        }
        preferences.edit().putString(KEY_BUDGETS, array.toString()).apply()
    }

    fun loadRecurringBills(): List<RecurringBill> {
        val raw = preferences.getString(KEY_RECURRING_BILLS, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val json = array.getJSONObject(index)
                    val paidArray = json.optJSONArray("paidPeriods") ?: JSONArray()
                    val paid = buildSet {
                        for (paidIndex in 0 until paidArray.length()) {
                            add(paidArray.optString(paidIndex))
                        }
                    }
                    add(
                        RecurringBill(
                            id = json.optLong("id"),
                            title = json.optString("title"),
                            category = HouseholdCategory.fromName(json.optString("category")),
                            value = json.optDouble("value", 0.0),
                            dueDay = json.optInt("dueDay", 1).coerceIn(1, 31),
                            active = json.optBoolean("active", true),
                            paidPeriods = paid,
                            createdAt = json.optLong("createdAt", 0L)
                        )
                    )
                }
            }.sortedBy { it.dueDay }
        }.getOrDefault(emptyList())
    }

    fun saveRecurringBills(items: List<RecurringBill>) {
        val array = JSONArray()
        items.forEach { item ->
            val paid = JSONArray()
            item.paidPeriods.forEach { paid.put(it) }
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("title", item.title)
                    .put("category", item.category.name)
                    .put("value", item.value)
                    .put("dueDay", item.dueDay)
                    .put("active", item.active)
                    .put("paidPeriods", paid)
                    .put("createdAt", item.createdAt)
            )
        }
        preferences.edit().putString(KEY_RECURRING_BILLS, array.toString()).apply()
    }

    fun loadSubscriptions(): List<SubscriptionEntry> {
        val raw = preferences.getString(KEY_SUBSCRIPTIONS, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val json = array.getJSONObject(index)
                    add(
                        SubscriptionEntry(
                            id = json.optLong("id"),
                            name = json.optString("name"),
                            value = json.optDouble("value", 0.0),
                            dueDay = json.optInt("dueDay", 1).coerceIn(1, 31),
                            active = json.optBoolean("active", true),
                            createdAt = json.optLong("createdAt", 0L)
                        )
                    )
                }
            }.sortedBy { it.dueDay }
        }.getOrDefault(emptyList())
    }

    fun saveSubscriptions(items: List<SubscriptionEntry>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("name", item.name)
                    .put("value", item.value)
                    .put("dueDay", item.dueDay)
                    .put("active", item.active)
                    .put("createdAt", item.createdAt)
            )
        }
        preferences.edit().putString(KEY_SUBSCRIPTIONS, array.toString()).apply()
    }

    fun loadFinancialGoals(): List<FinancialGoal> {
        val raw = preferences.getString(KEY_FINANCIAL_GOALS, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val json = array.getJSONObject(index)
                    add(
                        FinancialGoal(
                            id = json.optLong("id"),
                            title = json.optString("title"),
                            targetAmount = json.optDouble("targetAmount", 0.0),
                            savedAmount = json.optDouble("savedAmount", 0.0),
                            deadline = json.optString("deadline"),
                            createdAt = json.optLong("createdAt", 0L)
                        )
                    )
                }
            }.sortedByDescending { it.createdAt }
        }.getOrDefault(emptyList())
    }

    fun saveFinancialGoals(items: List<FinancialGoal>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("title", item.title)
                    .put("targetAmount", item.targetAmount)
                    .put("savedAmount", item.savedAmount)
                    .put("deadline", item.deadline)
                    .put("createdAt", item.createdAt)
            )
        }
        preferences.edit().putString(KEY_FINANCIAL_GOALS, array.toString()).apply()
    }

    fun loadGasPurchases(): List<GasPurchase> {
        val raw = preferences.getString(KEY_GAS_PURCHASES, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val json = array.getJSONObject(index)
                    add(
                        GasPurchase(
                            id = json.optLong("id"),
                            value = json.optDouble("value", 0.0),
                            purchaseDate = json.optString("purchaseDate"),
                            durationDays = json.optInt("durationDays", 0),
                            createdAt = json.optLong("createdAt", 0L)
                        )
                    )
                }
            }.sortedByDescending { it.createdAt }
        }.getOrDefault(emptyList())
    }

    fun saveGasPurchases(items: List<GasPurchase>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("value", item.value)
                    .put("purchaseDate", item.purchaseDate)
                    .put("durationDays", item.durationDays)
                    .put("createdAt", item.createdAt)
            )
        }
        preferences.edit().putString(KEY_GAS_PURCHASES, array.toString()).apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "ecogestor_data"
        private const val KEY_MODE_CONFIGURED = "mode_configured"
        private const val KEY_SELECTED_MODE = "selected_mode"
        private const val KEY_COMPANY = "company"
        private const val KEY_CONSUMPTIONS = "consumptions"
        private const val KEY_GOALS = "goals"
        private const val KEY_HOUSEHOLD_PROFILE = "household_profile"
        private const val KEY_HOUSEHOLD_ENTRIES = "household_entries"
        private const val KEY_BUDGETS = "household_budgets"
        private const val KEY_RECURRING_BILLS = "household_recurring_bills"
        private const val KEY_SUBSCRIPTIONS = "household_subscriptions"
        private const val KEY_FINANCIAL_GOALS = "household_financial_goals"
        private const val KEY_GAS_PURCHASES = "household_gas_purchases"
    }
}
