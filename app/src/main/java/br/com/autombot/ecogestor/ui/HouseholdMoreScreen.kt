package br.com.autombot.ecogestor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.autombot.ecogestor.data.BudgetLimit
import br.com.autombot.ecogestor.data.FinanceKind
import br.com.autombot.ecogestor.data.GasPurchase
import br.com.autombot.ecogestor.data.HouseholdEntry
import br.com.autombot.ecogestor.data.HouseholdProfile
import br.com.autombot.ecogestor.ui.theme.EcoGreen
import br.com.autombot.ecogestor.ui.theme.EcoTeal
import kotlin.math.roundToInt

@Composable
internal fun HouseholdMoreScreen(
    profile: HouseholdProfile?,
    entries: List<HouseholdEntry>,
    budgets: List<BudgetLimit>,
    gasPurchases: List<GasPurchase>,
    onEditProfile: () -> Unit,
    onAddBudget: () -> Unit,
    onEditBudget: (BudgetLimit) -> Unit,
    onDeleteBudget: (BudgetLimit) -> Unit,
    onAddGas: () -> Unit,
    onDeleteGas: (GasPurchase) -> Unit,
    onOpenCalculators: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val period = periodForOffset(0)
    val periods = entries
        .groupBy { it.period }
        .entries
        .sortedByDescending { (_, items) -> items.maxOfOrNull { it.createdAt } ?: 0L }
        .take(12)
    val latestGas = gasPurchases.maxByOrNull { it.createdAt }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Mais ferramentas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Planejamento, histórico, gás, exportação e calculadoras.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Home, contentDescription = null)
                        Column {
                            Text(profile?.name ?: "Minha casa", fontWeight = FontWeight.Bold)
                            Text(
                                if (profile != null) "Renda planejada: ${formatCurrency(profile.monthlyIncome)} • ${profile.members} pessoa(s)"
                                else "Perfil doméstico ainda não configurado",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    FilledTonalButton(onClick = onEditProfile, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Text(if (profile == null) "Configurar perfil" else "Editar perfil", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Central de calculadoras", fontWeight = FontWeight.Bold)
                    Text(
                        "Comum, orçamento, desconto, parcelamento, custo anual, energia, combustível e gás.",
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Button(onClick = onOpenCalculators, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                        Text("Abrir calculadoras", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }

        item {
            HorizontalDivider()
            Text("Orçamento por categoria", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Text("Defina quanto pretende gastar por mês em cada categoria.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Button(onClick = onAddBudget, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Adicionar limite", modifier = Modifier.padding(start = 8.dp))
            }
        }

        if (budgets.isEmpty()) {
            item { EmptyHouseholdCard("Crie limites para supermercado, delivery, lazer, roupas e outras categorias.") }
        } else {
            items(budgets.size) { index ->
                val budget = budgets[index]
                val spent = expenseByCategory(entries, period, budget.category)
                val rawProgress = budgetProgress(budget, entries, period)
                Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(budget.category.label, fontWeight = FontWeight.Bold)
                                Text("${formatCurrency(spent)} de ${formatCurrency(budget.monthlyLimit)}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row {
                                IconButton(onClick = { onEditBudget(budget) }) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                                IconButton(onClick = { onDeleteBudget(budget) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir") }
                            }
                        }
                        LinearProgressIndicator(
                            progress = { rawProgress.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = if (rawProgress >= 1f) MaterialTheme.colorScheme.error else EcoGreen
                        )
                        Text(
                            "${(rawProgress * 100).roundToInt()}% utilizado${if (rawProgress >= 1f) " • limite ultrapassado" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }

        item {
            HorizontalDivider()
            Text("Gás de cozinha", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Text(gasForecastText(latestGas), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Button(onClick = onAddGas, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Registrar botijão", modifier = Modifier.padding(start = 8.dp))
            }
        }

        if (gasPurchases.isNotEmpty()) {
            items(gasPurchases.take(6).size) { index ->
                val gas = gasPurchases[index]
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("Compra em ${gas.purchaseDate}", fontWeight = FontWeight.Bold)
                            Text(
                                if (gas.durationDays > 0) "Durou ${gas.durationDays} dias" else "Duração ainda não informada",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Column {
                            Text(formatCurrency(gas.value), fontWeight = FontWeight.Bold)
                            IconButton(onClick = { onDeleteGas(gas) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir") }
                        }
                    }
                }
            }
        }

        item {
            HorizontalDivider()
            Text("Histórico mensal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        }

        if (periods.isEmpty()) {
            item { EmptyHouseholdCard("O histórico aparecerá aqui conforme você registrar entradas e saídas.") }
        } else {
            items(periods.size) { index ->
                val (historyPeriod, items) = periods[index]
                val registeredIncome = items.filter { it.kind == FinanceKind.INCOME }.sumOf { it.value }
                val income = if (registeredIncome > 0) registeredIncome else if (historyPeriod == period) profile?.monthlyIncome ?: 0.0 else 0.0
                val expense = items.filter { it.kind == FinanceKind.EXPENSE }.sumOf { it.value }
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(historyPeriod, fontWeight = FontWeight.Bold)
                            Text(formatCurrency(income - expense), fontWeight = FontWeight.Bold, color = if (income - expense >= 0) EcoTeal else MaterialTheme.colorScheme.error)
                        }
                        Text("Entradas ${formatCurrency(income)} • Saídas ${formatCurrency(expense)}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item {
            HorizontalDivider()
            Text("Exportação", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Text(
                "Gere um resumo em formato CSV/texto para compartilhar ou salvar em outro aplicativo.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Button(onClick = onExport, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Icon(Icons.Default.IosShare, contentDescription = null)
                Text("Exportar relatório", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
