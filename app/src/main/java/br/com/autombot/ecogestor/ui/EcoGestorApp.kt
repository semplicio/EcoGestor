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
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.autombot.ecogestor.data.CompanyProfile
import br.com.autombot.ecogestor.data.ConsumptionCategory
import br.com.autombot.ecogestor.data.ConsumptionEntry
import br.com.autombot.ecogestor.data.EcoRepository
import br.com.autombot.ecogestor.data.SustainabilityGoal
import br.com.autombot.ecogestor.ui.theme.EcoDark
import br.com.autombot.ecogestor.ui.theme.EcoGreen
import br.com.autombot.ecogestor.ui.theme.EcoGreenLight
import br.com.autombot.ecogestor.ui.theme.EcoTeal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

private data class AppSection(
    val title: String,
    val icon: ImageVector
)

private data class Metric(
    val title: String,
    val value: String,
    val detail: String,
    val positive: Boolean
)

private val sections = listOf(
    AppSection("Início", Icons.Default.Home),
    AppSection("Consumos", Icons.Default.BarChart),
    AppSection("Metas", Icons.Default.TrackChanges),
    AppSection("Empresa", Icons.Default.Business)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoGestorApp() {
    val context = LocalContext.current
    val repository = remember(context) { EcoRepository(context.applicationContext) }

    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    var company by remember { mutableStateOf(repository.loadCompany()) }
    var consumptions by remember { mutableStateOf(repository.loadConsumptions()) }
    var goals by remember { mutableStateOf(repository.loadGoals()) }

    var showConsumptionDialog by remember { mutableStateOf(false) }
    var showCompanyDialog by remember { mutableStateOf(false) }
    var goalBeingEdited by remember { mutableStateOf<SustainabilityGoal?>(null) }
    var showNewGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = EcoDark,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = EcoGreenLight,
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "EcoGestor",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Economia sustentável para o seu negócio",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                sections.forEachIndexed { index, section ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(
                                imageVector = section.icon,
                                contentDescription = section.title
                            )
                        },
                        label = { Text(section.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedIndex) {
            0 -> HomeScreen(
                company = company,
                consumptions = consumptions,
                goals = goals,
                onAddConsumption = { showConsumptionDialog = true },
                onOpenCompany = {
                    selectedIndex = 3
                    showCompanyDialog = true
                },
                modifier = Modifier.padding(innerPadding)
            )

            1 -> ConsumptionScreen(
                entries = consumptions,
                onAdd = { showConsumptionDialog = true },
                onDelete = { entry ->
                    val updated = consumptions.filterNot { it.id == entry.id }
                    repository.saveConsumptions(updated)
                    consumptions = updated
                },
                modifier = Modifier.padding(innerPadding)
            )

            2 -> GoalsScreen(
                goals = goals,
                onAdd = { showNewGoalDialog = true },
                onEdit = { goalBeingEdited = it },
                onDelete = { goal ->
                    val updated = goals.filterNot { it.id == goal.id }
                    repository.saveGoals(updated)
                    goals = updated
                },
                modifier = Modifier.padding(innerPadding)
            )

            else -> BusinessScreen(
                company = company,
                onEdit = { showCompanyDialog = true },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    if (showConsumptionDialog) {
        ConsumptionDialog(
            onDismiss = { showConsumptionDialog = false },
            onSave = { entry ->
                val updated = (listOf(entry) + consumptions).sortedByDescending { it.createdAt }
                repository.saveConsumptions(updated)
                consumptions = updated
                showConsumptionDialog = false
            }
        )
    }

    if (showCompanyDialog) {
        CompanyDialog(
            company = company,
            onDismiss = { showCompanyDialog = false },
            onSave = { profile ->
                repository.saveCompany(profile)
                company = profile
                showCompanyDialog = false
            }
        )
    }

    if (showNewGoalDialog || goalBeingEdited != null) {
        GoalDialog(
            goal = goalBeingEdited,
            onDismiss = {
                showNewGoalDialog = false
                goalBeingEdited = null
            },
            onSave = { goal ->
                val updated = if (goals.any { it.id == goal.id }) {
                    goals.map { if (it.id == goal.id) goal else it }
                } else {
                    listOf(goal) + goals
                }
                repository.saveGoals(updated)
                goals = updated
                showNewGoalDialog = false
                goalBeingEdited = null
            }
        )
    }
}

@Composable
private fun HomeScreen(
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
        Metric(
            title = categoryShortName(category),
            value = formatCurrency(current),
            detail = comparison.first,
            positive = comparison.second
        )
    }

    val monthlySavings = ConsumptionCategory.entries.sumOf { category ->
        val current = currentEntries.filter { it.category == category }.sumOf { it.value }
        val previous = previousEntries.filter { it.category == category }.sumOf { it.value }
        if (previous > 0) max(previous - current, 0.0) else 0.0
    }

    val ecoScore = calculateEcoScore(company, currentEntries, goals)
    val recommendation = buildRecommendation(company, currentEntries, previousEntries)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Visão geral",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Acompanhe quanto o seu negócio economiza enquanto reduz desperdícios.",
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
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Comece cadastrando seu negócio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Os dados da empresa são usados para personalizar os indicadores e recomendações.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        FilledTonalButton(
                            onClick = onOpenCompany,
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
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
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Economia estimada no mês",
                                color = MaterialTheme.colorScheme.surface,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = formatCurrency(monthlySavings),
                                color = MaterialTheme.colorScheme.surface,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                            Text(
                                text = if (previousEntries.isEmpty()) {
                                    "Cadastre também o mês anterior para comparar"
                                } else {
                                    "${formatCurrency(monthlySavings * 12)}/ano mantendo este ritmo"
                                },
                                color = EcoGreenLight,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = EcoTeal,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = EcoGreenLight,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
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
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Índice EcoGestor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = ecoScoreLabel(ecoScore),
                                style = MaterialTheme.typography.bodySmall,
                                color = EcoGreen
                            )
                        }
                        Text(
                            text = "$ecoScore/100",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = EcoTeal
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { ecoScore / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = EcoGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "Consumo de $currentPeriod",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                metrics.chunked(2).forEach { rowMetrics ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowMetrics.forEach { metric ->
                            MetricCard(
                                metric = metric,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = recommendation.first,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = recommendation.second,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 6.dp)
                        )
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
                Text(
                    text = "Registrar novo consumo",
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    metric: Metric,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = metric.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = metric.value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = metric.detail,
                style = MaterialTheme.typography.bodySmall,
                color = if (metric.positive) EcoGreen else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ConsumptionScreen(
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
            Text(
                text = "Consumos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Registre contas e despesas para acompanhar a evolução do negócio.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                ConsumptionCategory.entries.forEachIndexed { index, category ->
                    val categoryEntries = currentEntries.filter { it.category == category }
                    val quantity = categoryEntries.sumOf { it.quantity }
                    val value = categoryEntries.sumOf { it.value }
                    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(category.label, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "${formatQuantity(quantity)} ${category.unit}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(formatCurrency(value), fontWeight = FontWeight.Bold)
                        }
                    }
                    if (index < ConsumptionCategory.entries.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }

        item {
            FilledTonalButton(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Adicionar lançamento", modifier = Modifier.padding(start = 8.dp))
            }
        }

        item {
            Text(
                text = "Histórico de lançamentos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (entries.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Nenhum consumo registrado",
                    description = "Cadastre sua primeira conta de energia, água, combustível ou materiais."
                )
            }
        } else {
            items(entries.size) { index ->
                val entry = entries[index]
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.category.label, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${formatQuantity(entry.quantity)} ${entry.category.unit} • ${entry.period}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 3.dp)
                            )
                            Text(
                                text = formatCurrency(entry.value),
                                color = EcoTeal,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 5.dp)
                            )
                        }
                        IconButton(onClick = { onDelete(entry) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir lançamento",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalsScreen(
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
            Text(
                text = "Metas sustentáveis",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Transforme pequenas mudanças em economia mensurável.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (goals.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Nenhuma meta criada",
                    description = "Crie uma meta, defina o objetivo e atualize o progresso conforme o negócio evoluir."
                )
            }
        } else {
            items(goals.size) { index ->
                val goal = goals[index]
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = goal.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = buildString {
                                        append("Meta: ${goal.targetPercent}%")
                                        if (goal.deadline.isNotBlank()) append(" • Prazo: ${goal.deadline}")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            }
                            IconButton(onClick = { onEdit(goal) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar meta")
                            }
                            IconButton(onClick = { onDelete(goal) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Excluir meta",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Progresso",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${goal.progressPercent}%",
                                color = EcoTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { goal.progressPercent.coerceIn(0, 100) / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = EcoGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Criar nova meta", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun BusinessScreen(
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
            Text(
                text = "Minha empresa",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Dados usados para personalizar indicadores e recomendações.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (company == null) {
            item {
                EmptyStateCard(
                    title = "Empresa ainda não cadastrada",
                    description = "Cadastre seu MEI, empresa ou negócio para começar a personalizar o EcoGestor."
                )
            }
        } else {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        Text(
                            text = company.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 14.dp)
                        )
                        Text(
                            text = listOfNotNull(
                                company.businessType.takeIf { it.isNotBlank() },
                                company.segment.takeIf { it.isNotBlank() },
                                "${company.collaborators} colaboradores"
                            ).joinToString(" • "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        if (company.document.isNotBlank()) {
                            Text(
                                text = "Documento: ${company.document}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = if (company == null) Icons.Default.Add else Icons.Default.Edit,
                    contentDescription = null
                )
                Text(
                    text = if (company == null) "Cadastrar empresa" else "Editar dados da empresa",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = EcoGreen
                    )
                    Column {
                        Text(
                            text = "Dados salvos neste aparelho",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Nesta primeira versão funcional, empresa, consumos e metas ficam armazenados localmente. A sincronização em nuvem será adicionada em uma etapa posterior.",
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
private fun EmptyStateCard(
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
    }
}

@Composable
private fun ConsumptionDialog(
    onDismiss: () -> Unit,
    onSave: (ConsumptionEntry) -> Unit
) {
    var category by remember { mutableStateOf(ConsumptionCategory.ENERGY) }
    var quantityText by remember { mutableStateOf("") }
    var valueText by remember { mutableStateOf("") }
    var periodText by remember { mutableStateOf(periodForOffset(0)) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo consumo") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Categoria", fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(
                        category = ConsumptionCategory.ENERGY,
                        selected = category == ConsumptionCategory.ENERGY,
                        onClick = { category = ConsumptionCategory.ENERGY },
                        modifier = Modifier.weight(1f)
                    )
                    CategoryChip(
                        category = ConsumptionCategory.WATER,
                        selected = category == ConsumptionCategory.WATER,
                        onClick = { category = ConsumptionCategory.WATER },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(
                        category = ConsumptionCategory.FUEL,
                        selected = category == ConsumptionCategory.FUEL,
                        onClick = { category = ConsumptionCategory.FUEL },
                        modifier = Modifier.weight(1f)
                    )
                    CategoryChip(
                        category = ConsumptionCategory.MATERIALS,
                        selected = category == ConsumptionCategory.MATERIALS,
                        onClick = { category = ConsumptionCategory.MATERIALS },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantidade (${category.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it },
                    label = { Text("Valor pago (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = periodText,
                    onValueChange = { periodText = it },
                    label = { Text("Mês/Ano") },
                    supportingText = { Text("Formato: MM/AAAA") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(
                        text = error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val quantity = parseDecimal(quantityText)
                    val value = parseDecimal(valueText)
                    when {
                        quantity == null || quantity <= 0 -> error = "Informe uma quantidade válida."
                        value == null || value < 0 -> error = "Informe um valor válido."
                        !periodText.matches(Regex("(0[1-9]|1[0-2])/\\d{4}")) -> error = "Informe o período no formato MM/AAAA."
                        else -> onSave(
                            ConsumptionEntry(
                                id = System.currentTimeMillis(),
                                category = category,
                                quantity = quantity,
                                value = value,
                                period = periodText
                            )
                        )
                    }
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun CategoryChip(
    category: ConsumptionCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(categoryShortName(category)) },
        modifier = modifier
    )
}

@Composable
private fun CompanyDialog(
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do negócio") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = document,
                    onValueChange = { document = it },
                    label = { Text("CNPJ/CPF (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = businessType,
                    onValueChange = { businessType = it },
                    label = { Text("Tipo do negócio") },
                    supportingText = { Text("Ex.: MEI, ME, PJ, autônomo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = segment,
                    onValueChange = { segment = it },
                    label = { Text("Segmento") },
                    supportingText = { Text("Ex.: comércio, alimentação, serviços") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = collaborators,
                    onValueChange = { collaborators = it },
                    label = { Text("Colaboradores") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(
                        text = error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val collaboratorsValue = collaborators.toIntOrNull()
                    when {
                        name.isBlank() -> error = "Informe o nome do negócio."
                        collaboratorsValue == null || collaboratorsValue < 0 -> error = "Informe uma quantidade válida de colaboradores."
                        else -> onSave(
                            CompanyProfile(
                                name = name.trim(),
                                document = document.trim(),
                                businessType = businessType.trim().ifBlank { "MEI" },
                                segment = segment.trim(),
                                collaborators = collaboratorsValue
                            )
                        )
                    }
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun GoalDialog(
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
        title = { Text(if (goal == null) "Nova meta" else "Editar meta") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Objetivo") },
                    supportingText = { Text("Ex.: Reduzir consumo de energia") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Meta de redução/resultado (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = progress,
                    onValueChange = { progress = it },
                    label = { Text("Progresso atual (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Prazo (opcional)") },
                    supportingText = { Text("Ex.: 3 meses ou Dez/2026") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(
                        text = error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val targetValue = target.toIntOrNull()
                    val progressValue = progress.toIntOrNull()
                    when {
                        title.isBlank() -> error = "Informe o objetivo da meta."
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
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private fun periodForOffset(monthOffset: Int): String {
    val calendar = Calendar.getInstance().apply { add(Calendar.MONTH, monthOffset) }
    return SimpleDateFormat("MM/yyyy", Locale("pt", "BR")).format(calendar.time)
}

private fun formatCurrency(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)

private fun formatQuantity(value: Double): String =
    if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else {
        String.format(Locale("pt", "BR"), "%.2f", value)
    }

private fun parseDecimal(raw: String): Double? {
    val value = raw.trim()
    if (value.isEmpty()) return null
    val normalized = if (value.contains(',')) {
        value.replace(".", "").replace(',', '.')
    } else {
        value
    }
    return normalized.toDoubleOrNull()
}

private fun categoryShortName(category: ConsumptionCategory): String = when (category) {
    ConsumptionCategory.ENERGY -> "Energia"
    ConsumptionCategory.WATER -> "Água"
    ConsumptionCategory.FUEL -> "Combustível"
    ConsumptionCategory.MATERIALS -> "Materiais"
}

private fun comparisonText(current: Double, previous: Double): Pair<String, Boolean> {
    if (previous <= 0) return "Sem comparação anterior" to true
    val difference = ((current - previous) / previous) * 100
    if (abs(difference) < 0.5) return "Estável" to true
    val rounded = abs(difference).roundToInt()
    return if (difference < 0) {
        "$rounded% menor" to true
    } else {
        "$rounded% maior" to false
    }
}

private fun calculateEcoScore(
    company: CompanyProfile?,
    currentEntries: List<ConsumptionEntry>,
    goals: List<SustainabilityGoal>
): Int {
    val companyPoints = if (company != null) 30 else 0
    val coveragePoints = currentEntries.map { it.category }.distinct().size * 10
    val goalPoints = if (goals.isEmpty()) {
        0
    } else {
        ((goals.map { it.progressPercent }.average() / 100.0) * 30.0).roundToInt()
    }
    return (companyPoints + coveragePoints + goalPoints).coerceIn(0, 100)
}

private fun ecoScoreLabel(score: Int): String = when {
    score >= 90 -> "Eco Empresa Destaque"
    score >= 80 -> "Eco Ouro"
    score >= 65 -> "Eco Prata"
    score >= 50 -> "Eco Bronze"
    score > 0 -> "Eco Iniciante"
    else -> "Comece seus registros"
}

private fun buildRecommendation(
    company: CompanyProfile?,
    current: List<ConsumptionEntry>,
    previous: List<ConsumptionEntry>
): Pair<String, String> {
    if (company == null) {
        return "Primeiro passo" to "Cadastre seu negócio para que o EcoGestor possa organizar indicadores e recomendações."
    }
    if (current.isEmpty()) {
        return "Registre o consumo do mês" to "Adicione contas de energia, água, combustível e materiais para começar a medir sua eficiência."
    }
    if (previous.isEmpty()) {
        return "Crie uma base de comparação" to "Cadastre também os valores do mês anterior. Assim o EcoGestor poderá mostrar aumentos, reduções e economia real."
    }

    val increases = ConsumptionCategory.entries.map { category ->
        val currentValue = current.filter { it.category == category }.sumOf { it.value }
        val previousValue = previous.filter { it.category == category }.sumOf { it.value }
        val delta = if (previousValue > 0) currentValue - previousValue else 0.0
        Triple(category, delta, previousValue)
    }.filter { it.second > 0 && it.third > 0 }

    val largestIncrease = increases.maxByOrNull { it.second }
    if (largestIncrease != null) {
        val percentage = ((largestIncrease.second / largestIncrease.third) * 100).roundToInt()
        return "Atenção ao consumo" to "${largestIncrease.first.label} aumentou aproximadamente $percentage% em relação ao mês anterior. Vale revisar a causa desse aumento."
    }

    val totalCurrent = current.sumOf { it.value }
    val totalPrevious = previous.sumOf { it.value }
    if (totalCurrent < totalPrevious) {
        return "Boa evolução" to "Seus gastos monitorados caíram ${formatCurrency(totalPrevious - totalCurrent)} em relação ao mês anterior. Continue acompanhando para manter a redução."
    }

    return "Continue monitorando" to "Você já possui dados suficientes para acompanhar o mês. Mantenha os lançamentos atualizados para identificar novas oportunidades de economia."
}
