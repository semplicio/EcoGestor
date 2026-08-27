package br.com.autombot.ecogestor.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

private val expenseCategories = HouseholdCategory.entries.filterNot {
    it == HouseholdCategory.SALARY || it == HouseholdCategory.EXTRA_INCOME
}

private val incomeCategories = listOf(
    HouseholdCategory.SALARY,
    HouseholdCategory.EXTRA_INCOME,
    HouseholdCategory.OTHER
)

@Composable
internal fun HouseholdProfileFormDialog(
    profile: HouseholdProfile?,
    onDismiss: () -> Unit,
    onSave: (HouseholdProfile) -> Unit
) {
    var name by remember(profile) { mutableStateOf(profile?.name ?: "Minha casa") }
    var income by remember(profile) { mutableStateOf(profile?.monthlyIncome?.takeIf { it > 0 }?.toString() ?: "") }
    var members by remember(profile) { mutableStateOf((profile?.members ?: 1).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dados da casa") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do perfil") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = income,
                    onValueChange = { income = it },
                    label = { Text("Renda mensal planejada") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = members,
                    onValueChange = { members = it.filter(Char::isDigit) },
                    label = { Text("Pessoas na casa") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Se você cadastrar entradas reais no mês, elas terão prioridade sobre esta renda planejada no dashboard.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        HouseholdProfile(
                            name = name.trim(),
                            monthlyIncome = parseDecimal(income) ?: 0.0,
                            members = members.toIntOrNull()?.coerceAtLeast(1) ?: 1
                        )
                    )
                }
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
internal fun HouseholdEntryFormDialog(
    entry: HouseholdEntry? = null,
    onDismiss: () -> Unit,
    onSave: (HouseholdEntry) -> Unit
) {
    var kind by remember(entry?.id) { mutableStateOf(entry?.kind ?: FinanceKind.EXPENSE) }
    var category by remember(entry?.id) {
        mutableStateOf(entry?.category ?: HouseholdCategory.SUPERMARKET)
    }
    var title by remember(entry?.id) { mutableStateOf(entry?.title ?: "") }
    var value by remember(entry?.id) { mutableStateOf(entry?.value?.takeIf { it > 0 }?.toString() ?: "") }
    var period by remember(entry?.id) { mutableStateOf(entry?.period ?: periodForOffset(0)) }
    var date by remember(entry?.id) { mutableStateOf(entry?.date ?: "") }
    var notes by remember(entry?.id) { mutableStateOf(entry?.notes?.takeUnless { it.startsWith("__") } ?: "") }
    var recurring by remember(entry?.id) { mutableStateOf(entry?.recurring ?: false) }

    val allowedCategories = if (kind == FinanceKind.INCOME) incomeCategories else expenseCategories
    if (category !in allowedCategories) category = allowedCategories.first()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (entry == null) "Novo movimento" else "Editar movimento") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Tipo", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = kind == FinanceKind.EXPENSE,
                        onClick = { kind = FinanceKind.EXPENSE },
                        label = { Text("Saída") }
                    )
                    FilterChip(
                        selected = kind == FinanceKind.INCOME,
                        onClick = { kind = FinanceKind.INCOME },
                        label = { Text("Entrada") }
                    )
                }

                Text("Categoria", fontWeight = FontWeight.SemiBold)
                CategoryChipRow(category, allowedCategories) { category = it }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Descrição") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Valor (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = period,
                    onValueChange = { period = it },
                    label = { Text("Mês/ano (MM/AAAA)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Data (opcional, DD/MM/AAAA)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observação") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Gasto recorrente", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Marque para identificar despesas que se repetem.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = recurring, onCheckedChange = { recurring = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && (parseDecimal(value) ?: 0.0) > 0.0 && period.isNotBlank(),
                onClick = {
                    onSave(
                        HouseholdEntry(
                            id = entry?.id ?: System.currentTimeMillis(),
                            kind = kind,
                            category = category,
                            title = title.trim(),
                            value = parseDecimal(value) ?: 0.0,
                            period = period.trim(),
                            date = date.trim(),
                            recurring = recurring,
                            notes = notes.trim(),
                            createdAt = entry?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                }
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
internal fun RecurringBillFormDialog(
    item: RecurringBill?,
    onDismiss: () -> Unit,
    onSave: (RecurringBill) -> Unit
) {
    var title by remember(item?.id) { mutableStateOf(item?.title ?: "") }
    var category by remember(item?.id) { mutableStateOf(item?.category ?: HouseholdCategory.INTERNET) }
    var value by remember(item?.id) { mutableStateOf(item?.value?.takeIf { it > 0 }?.toString() ?: "") }
    var dueDay by remember(item?.id) { mutableStateOf(item?.dueDay?.toString() ?: "10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Nova conta recorrente" else "Editar conta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Conta") }, modifier = Modifier.fillMaxWidth())
                Text("Categoria", fontWeight = FontWeight.SemiBold)
                CategoryChipRow(category, expenseCategories) { category = it }
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Valor previsto (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDay,
                    onValueChange = { dueDay = it.filter(Char::isDigit).take(2) },
                    label = { Text("Dia do vencimento") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && (parseDecimal(value) ?: 0.0) > 0.0 && (dueDay.toIntOrNull() ?: 0) in 1..31,
                onClick = {
                    onSave(
                        RecurringBill(
                            id = item?.id ?: System.currentTimeMillis(),
                            title = title.trim(),
                            category = category,
                            value = parseDecimal(value) ?: 0.0,
                            dueDay = dueDay.toIntOrNull()?.coerceIn(1, 31) ?: 1,
                            active = item?.active ?: true,
                            paidPeriods = item?.paidPeriods ?: emptySet(),
                            createdAt = item?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                }
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
internal fun SubscriptionFormDialog(
    item: SubscriptionEntry?,
    onDismiss: () -> Unit,
    onSave: (SubscriptionEntry) -> Unit
) {
    var name by remember(item?.id) { mutableStateOf(item?.name ?: "") }
    var value by remember(item?.id) { mutableStateOf(item?.value?.takeIf { it > 0 }?.toString() ?: "") }
    var dueDay by remember(item?.id) { mutableStateOf(item?.dueDay?.toString() ?: "15") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Nova assinatura" else "Editar assinatura") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Serviço") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Valor mensal (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDay,
                    onValueChange = { dueDay = it.filter(Char::isDigit).take(2) },
                    label = { Text("Dia da cobrança") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && (parseDecimal(value) ?: 0.0) > 0.0 && (dueDay.toIntOrNull() ?: 0) in 1..31,
                onClick = {
                    onSave(
                        SubscriptionEntry(
                            id = item?.id ?: System.currentTimeMillis(),
                            name = name.trim(),
                            value = parseDecimal(value) ?: 0.0,
                            dueDay = dueDay.toIntOrNull()?.coerceIn(1, 31) ?: 1,
                            active = item?.active ?: true,
                            createdAt = item?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                }
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
internal fun BudgetFormDialog(
    item: BudgetLimit?,
    onDismiss: () -> Unit,
    onSave: (BudgetLimit) -> Unit
) {
    var category by remember(item?.id) { mutableStateOf(item?.category ?: HouseholdCategory.SUPERMARKET) }
    var limit by remember(item?.id) { mutableStateOf(item?.monthlyLimit?.takeIf { it > 0 }?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Novo orçamento" else "Editar orçamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Categoria", fontWeight = FontWeight.SemiBold)
                CategoryChipRow(category, expenseCategories) { category = it }
                OutlinedTextField(
                    value = limit,
                    onValueChange = { limit = it },
                    label = { Text("Limite mensal (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = (parseDecimal(limit) ?: 0.0) > 0.0,
                onClick = {
                    onSave(
                        BudgetLimit(
                            id = item?.id ?: System.currentTimeMillis(),
                            category = category,
                            monthlyLimit = parseDecimal(limit) ?: 0.0,
                            createdAt = item?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                }
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
internal fun FinancialGoalFormDialog(
    item: FinancialGoal?,
    onDismiss: () -> Unit,
    onSave: (FinancialGoal) -> Unit
) {
    var title by remember(item?.id) { mutableStateOf(item?.title ?: "") }
    var target by remember(item?.id) { mutableStateOf(item?.targetAmount?.takeIf { it > 0 }?.toString() ?: "") }
    var saved by remember(item?.id) { mutableStateOf(item?.savedAmount?.takeIf { it > 0 }?.toString() ?: "") }
    var deadline by remember(item?.id) { mutableStateOf(item?.deadline ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Nova meta financeira" else "Editar meta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Objetivo") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Valor da meta (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = saved,
                    onValueChange = { saved = it },
                    label = { Text("Valor já guardado (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Prazo (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && (parseDecimal(target) ?: 0.0) > 0.0,
                onClick = {
                    onSave(
                        FinancialGoal(
                            id = item?.id ?: System.currentTimeMillis(),
                            title = title.trim(),
                            targetAmount = parseDecimal(target) ?: 0.0,
                            savedAmount = (parseDecimal(saved) ?: 0.0).coerceAtLeast(0.0),
                            deadline = deadline.trim(),
                            createdAt = item?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                }
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
internal fun GasPurchaseFormDialog(
    onDismiss: () -> Unit,
    onSave: (GasPurchase) -> Unit
) {
    var value by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableStateOf("") }
    var durationDays by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar botijão de gás") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Valor pago (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = purchaseDate,
                    onValueChange = { purchaseDate = it },
                    label = { Text("Data da compra (DD/MM/AAAA)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = durationDays,
                    onValueChange = { durationDays = it.filter(Char::isDigit) },
                    label = { Text("Duração do botijão em dias") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Ao informar a duração do botijão anterior, o EcoGestor calcula custo médio e previsão aproximada da próxima troca.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = (parseDecimal(value) ?: 0.0) > 0.0 && purchaseDate.isNotBlank(),
                onClick = {
                    onSave(
                        GasPurchase(
                            id = System.currentTimeMillis(),
                            value = parseDecimal(value) ?: 0.0,
                            purchaseDate = purchaseDate.trim(),
                            durationDays = durationDays.toIntOrNull()?.coerceAtLeast(0) ?: 0
                        )
                    )
                }
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun CategoryChipRow(
    selected: HouseholdCategory,
    categories: List<HouseholdCategory>,
    onSelect: (HouseholdCategory) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { item ->
            FilterChip(
                selected = selected == item,
                onClick = { onSelect(item) },
                label = { Text(item.label) }
            )
        }
    }
}
