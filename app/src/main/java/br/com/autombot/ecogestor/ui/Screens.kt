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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.autombot.ecogestor.data.CompanyProfile
import br.com.autombot.ecogestor.data.ConsumptionCategory
import br.com.autombot.ecogestor.data.ConsumptionEntry
import br.com.autombot.ecogestor.data.SustainabilityGoal
import br.com.autombot.ecogestor.ui.theme.EcoDark
import br.com.autombot.ecogestor.ui.theme.EcoGreen
import br.com.autombot.ecogestor.ui.theme.EcoGreenLight
import br.com.autombot.ecogestor.ui.theme.EcoTeal

@Composable
internal fun HomeScreen(
    company: CompanyProfile?,
    consumptions: List<ConsumptionEntry>,
    goals: List<SustainabilityGoal>,
    onAddConsumption: () -> Unit,
    onOpenCompany: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPeriod = periodForOffset(0)
    val previousPeriod = periodForOffset(-1)
    val currentEntries = consumptions.filter { it.period == currentPeriod }
    val previousEntries = consumptions.filter { it.period == previousPeriod }

    val metrics = ConsumptionCategory.entries.map { category ->
        val current = currentEntries.filter { it.category == category }.sumOf { it.value }
        val previous = previousEntries.filter { it.category == category }.sumOf { it.value }
        val comparison = comparisonText(current, previous)
        Metric(categoryShortName(category), formatCurrency(current), comparison.first, comparison.second)
    }

    val savings = monthlySavings(currentEntries, previousEntries)
    val score = calculateEcoScore(company, currentEntries, goals)
    val recommendation = buildRecommendation(company, currentEntries, previousEntries)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Visão geral", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Acompanhe quanto o seu negócio economiza enquanto reduz desperdícios.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (company == null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Comece cadastrando seu negócio", fontWeight = FontWeight.Bold)
                        Text(
                            "Os dados da empresa serão usados para personalizar indicadores e recomendações.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        FilledTonalButton(onClick = onOpenCompany, modifier = Modifier.padding(top = 12.dp)) {
                            Icon(Icons.Default.Business, contentDescription = null)
                            Text("Cadastrar empresa", modifier = Modifier.padding(start = 8.dp))
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
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Economia estimada no mês", color = MaterialTheme.colorScheme.surface)
                        Text(
                            formatCurrency(savings),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            if (previousEntries.isEmpty()) "Cadastre o mês anterior para comparar"
                            else "${formatCurrency(savings * 12)}/ano mantendo este ritmo",
                            color = EcoGreenLight,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Surface(shape = CircleShape, color = EcoTeal, modifier = Modifier.size(54.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Savings, contentDescription = null, tint = EcoGreenLight)
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Índice EcoGestor", fontWeight = FontWeight.Bold)
                            Text(ecoScoreLabel(score), color = EcoGreen, style = MaterialTheme.typography.bodySmall)
                        }
                        Text("$score/100", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = EcoTeal)
                    }
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { score / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = EcoGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        item { Text("Consumo de $currentPeriod", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                metrics.chunked(2).forEach { pair ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEach { metric -> MetricCard(metric, Modifier.weight(1f)) }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
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
                Row(Modifier.padding(18.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(recommendation.first, fontWeight = FontWeight.Bold)
                        Text(recommendation.second, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }

        item {
            Button(
                onClick = onAddConsumption,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Registrar novo consumo", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun MetricCard(metric: Metric, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Text(metric.title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(metric.value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Text(
                metric.detail,
                style = MaterialTheme.typography.bodySmall,
                color = if (metric.positive) EcoGreen else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
internal fun ConsumptionScreen(
    entries: List<ConsumptionEntry>,
    onAdd: () -> Unit,
    onDelete: (ConsumptionEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPeriod = periodForOffset(0)
    val currentEntries = entries.filter { it.period == currentPeriod }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Consumos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Registre e compare os recursos utilizados pelo negócio.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                ConsumptionCategory.entries.forEachIndexed { index, category ->
                    val categoryEntries = currentEntries.filter { it.category == category }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(category.label, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${formatQuantity(categoryEntries.sumOf { it.quantity })} ${category.unit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(formatCurrency(categoryEntries.sumOf { it.value }), fontWeight = FontWeight.Bold)
                    }
                    if (index < ConsumptionCategory.entries.lastIndex) HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                }
            }
        }

        item {
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Adicionar lançamento", modifier = Modifier.padding(start = 8.dp))
            }
        }

        item { Text("Histórico", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }

        if (entries.isEmpty()) {
            item { EmptyStateCard("Nenhum consumo registrado", "Adicione a primeira conta ou despesa para iniciar o histórico.") }
        } else {
            items(entries.size) { index ->
                val entry = entries[index]
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(entry.category.label, fontWeight = FontWeight.Bold)
                            Text(
                                "${formatQuantity(entry.quantity)} ${entry.category.unit} • ${entry.period}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(formatCurrency(entry.value), color = EcoTeal, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                        }
                        IconButton(onClick = { onDelete(entry) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun GoalsScreen(
    goals: List<SustainabilityGoal>,
    onAdd: () -> Unit,
    onEdit: (SustainabilityGoal) -> Unit,
    onDelete: (SustainabilityGoal) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Metas sustentáveis", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Transforme pequenas mudanças em economia mensurável.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        }

        if (goals.isEmpty()) {
            item { EmptyStateCard("Nenhuma meta criada", "Crie uma meta e atualize o progresso conforme seu negócio evoluir.") }
        } else {
            items(goals.size) { index ->
                val goal = goals[index]
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(18.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text(goal.title, fontWeight = FontWeight.Bold)
                                Text(
                                    buildString {
                                        append("Meta: ${goal.targetPercent}%")
                                        if (goal.deadline.isNotBlank()) append(" • ${goal.deadline}")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onEdit(goal) }) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                            IconButton(onClick = { onDelete(goal) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error) }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Progresso", style = MaterialTheme.typography.bodySmall)
                            Text("${goal.progressPercent}%", color = EcoTeal, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { goal.progressPercent.coerceIn(0, 100) / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = EcoGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Criar nova meta", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
internal fun BusinessScreen(
    company: CompanyProfile?,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Minha empresa", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Dados usados para personalizar indicadores e recomendações.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        }

        if (company == null) {
            item { EmptyStateCard("Empresa ainda não cadastrada", "Cadastre seu MEI, empresa ou negócio para começar a usar dados reais.") }
        } else {
            item {
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(20.dp)) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(56.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Business, contentDescription = null) }
                        }
                        Text(company.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 14.dp))
                        Text(
                            listOfNotNull(
                                company.businessType.takeIf { it.isNotBlank() },
                                company.segment.takeIf { it.isNotBlank() },
                                "${company.collaborators} colaboradores"
                            ).joinToString(" • "),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        if (company.document.isNotBlank()) Text("Documento: ${company.document}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }

        item {
            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Icon(if (company == null) Icons.Default.Add else Icons.Default.Edit, contentDescription = null)
                Text(if (company == null) "Cadastrar empresa" else "Editar dados da empresa", modifier = Modifier.padding(start = 8.dp))
            }
        }

        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.padding(18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Eco, contentDescription = null, tint = EcoGreen)
                    Column {
                        Text("Dados salvos neste aparelho", fontWeight = FontWeight.Bold)
                        Text(
                            "Empresa, consumos e metas continuam disponíveis depois que o aplicativo é fechado. A sincronização online será adicionada em uma próxima etapa.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(title: String, description: String) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(18.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 5.dp))
        }
    }
}
