package br.com.autombot.ecogestor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import br.com.autombot.ecogestor.data.CompanyProfile
import br.com.autombot.ecogestor.data.ConsumptionCategory
import br.com.autombot.ecogestor.data.ConsumptionEntry
import br.com.autombot.ecogestor.data.SustainabilityGoal

@Composable
internal fun ConsumptionFormDialog(
    onDismiss: () -> Unit,
    onSave: (ConsumptionEntry) -> Unit
) {
    var category by remember { mutableStateOf(ConsumptionCategory.ENERGY) }
    var quantity by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var period by remember { mutableStateOf(periodForOffset(0)) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar consumo") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Categoria", fontWeight = FontWeight.SemiBold)
                ConsumptionCategory.entries.forEach { option ->
                    FilterChip(
                        selected = category == option,
                        onClick = { category = option },
                        label = { Text("${option.label} (${option.unit})") }
                    )
                }
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantidade (${category.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Valor pago (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = period,
                    onValueChange = { period = it },
                    label = { Text("Período") },
                    supportingText = { Text("Use MM/AAAA") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val quantityValue = parseDecimal(quantity)
                val moneyValue = parseDecimal(value)
                when {
                    quantityValue == null || quantityValue <= 0 -> error = "Informe uma quantidade válida."
                    moneyValue == null || moneyValue < 0 -> error = "Informe um valor válido."
                    !period.matches(Regex("(0[1-9]|1[0-2])/\\d{4}")) -> error = "Período inválido. Use MM/AAAA."
                    else -> onSave(
                        ConsumptionEntry(
                            id = System.currentTimeMillis(),
                            category = category,
                            quantity = quantityValue,
                            value = moneyValue,
                            period = period
                        )
                    )
                }
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
internal fun CompanyFormDialog(
    company: CompanyProfile?,
    onDismiss: () -> Unit,
    onSave: (CompanyProfile) -> Unit
) {
    var name by remember(company) { mutableStateOf(company?.name.orEmpty()) }
    var document by remember(company) { mutableStateOf(company?.document.orEmpty()) }
    var businessType by remember(company) { mutableStateOf(company?.businessType ?: "MEI") }
    var segment by remember(company) { mutableStateOf(company?.segment.orEmpty()) }
    var collaborators by remember(company) { mutableStateOf(company?.collaborators?.toString() ?: "0") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (company == null) "Cadastrar empresa" else "Editar empresa") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(name, { name = it }, label = { Text("Nome do negócio") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(document, { document = it }, label = { Text("CNPJ/CPF (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    businessType,
                    { businessType = it },
                    label = { Text("Tipo") },
                    supportingText = { Text("MEI, ME, PJ, autônomo...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(segment, { segment = it }, label = { Text("Segmento") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    collaborators,
                    { collaborators = it },
                    label = { Text("Colaboradores") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val number = collaborators.toIntOrNull()
                when {
                    name.isBlank() -> error = "Informe o nome do negócio."
                    number == null || number < 0 -> error = "Informe uma quantidade válida de colaboradores."
                    else -> onSave(
                        CompanyProfile(
                            name = name.trim(),
                            document = document.trim(),
                            businessType = businessType.trim().ifBlank { "MEI" },
                            segment = segment.trim(),
                            collaborators = number
                        )
                    )
                }
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
internal fun GoalFormDialog(
    goal: SustainabilityGoal?,
    onDismiss: () -> Unit,
    onSave: (SustainabilityGoal) -> Unit
) {
    var title by remember(goal) { mutableStateOf(goal?.title.orEmpty()) }
    var target by remember(goal) { mutableStateOf(goal?.targetPercent?.toString() ?: "") }
    var progress by remember(goal) { mutableStateOf(goal?.progressPercent?.toString() ?: "0") }
    var deadline by remember(goal) { mutableStateOf(goal?.deadline.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (goal == null) "Criar meta" else "Editar meta") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(title, { title = it }, label = { Text("Objetivo") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    target,
                    { target = it },
                    label = { Text("Meta (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    progress,
                    { progress = it },
                    label = { Text("Progresso atual (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(deadline, { deadline = it }, label = { Text("Prazo (opcional)") }, modifier = Modifier.fillMaxWidth())
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val targetValue = target.toIntOrNull()
                val progressValue = progress.toIntOrNull()
                when {
                    title.isBlank() -> error = "Informe o objetivo."
                    targetValue == null || targetValue !in 1..100 -> error = "A meta deve ficar entre 1% e 100%."
                    progressValue == null || progressValue !in 0..100 -> error = "O progresso deve ficar entre 0% e 100%."
                    else -> onSave(
                        SustainabilityGoal(
                            id = goal?.id ?: System.currentTimeMillis(),
                            title = title.trim(),
                            targetPercent = targetValue,
                            progressPercent = progressValue,
                            deadline = deadline.trim(),
                            createdAt = goal?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                }
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
