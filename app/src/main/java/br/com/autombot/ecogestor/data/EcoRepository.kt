package br.com.autombot.ecogestor.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class EcoRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

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

    companion object {
        private const val PREFERENCES_NAME = "ecogestor_data"
        private const val KEY_COMPANY = "company"
        private const val KEY_CONSUMPTIONS = "consumptions"
        private const val KEY_GOALS = "goals"
    }
}
