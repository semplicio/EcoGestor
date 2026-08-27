package br.com.autombot.ecogestor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.unit.dp
import br.com.autombot.ecogestor.data.EcoRepository
import br.com.autombot.ecogestor.data.SustainabilityGoal
import br.com.autombot.ecogestor.ui.theme.EcoDark
import br.com.autombot.ecogestor.ui.theme.EcoGreenLight

private data class BusinessSection(val title: String, val icon: ImageVector)

private val businessSections = listOf(
    BusinessSection("Início", Icons.Default.Home),
    BusinessSection("Consumos", Icons.Default.BarChart),
    BusinessSection("Metas", Icons.Default.TrackChanges),
    BusinessSection("Empresa", Icons.Default.Business)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BusinessModeApp(onSwitchToHousehold: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { EcoRepository(context.applicationContext) }

    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    var company by remember { mutableStateOf(repository.loadCompany()) }
    var consumptions by remember { mutableStateOf(repository.loadConsumptions()) }
    var goals by remember { mutableStateOf(repository.loadGoals()) }

    var showConsumptionDialog by remember { mutableStateOf(false) }
    var showCompanyDialog by remember { mutableStateOf(false) }
    var showNewGoalDialog by remember { mutableStateOf(false) }
    var goalBeingEdited by remember { mutableStateOf<SustainabilityGoal?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(shape = CircleShape, color = EcoDark, modifier = Modifier.size(38.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = EcoGreenLight,
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                        }
                        Column {
                            Text("EcoGestor Negócio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                "Economia sustentável para sua empresa",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    TextButton(onClick = onSwitchToHousehold) {
                        Text("Casa")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                businessSections.forEachIndexed { index, section ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(section.icon, contentDescription = section.title) },
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
        ConsumptionFormDialog(
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
        CompanyFormDialog(
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
        GoalFormDialog(
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
