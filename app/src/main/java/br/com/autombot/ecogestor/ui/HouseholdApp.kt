package br.com.autombot.ecogestor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import br.com.autombot.ecogestor.data.BudgetLimit
import br.com.autombot.ecogestor.data.EcoRepository
import br.com.autombot.ecogestor.data.FinanceKind
import br.com.autombot.ecogestor.data.FinancialGoal
import br.com.autombot.ecogestor.data.GasPurchase
import br.com.autombot.ecogestor.data.HouseholdCategory
import br.com.autombot.ecogestor.data.HouseholdEntry
import br.com.autombot.ecogestor.data.RecurringBill
import br.com.autombot.ecogestor.data.SubscriptionEntry
import br.com.autombot.ecogestor.ui.theme.EcoDark
import br.com.autombot.ecogestor.ui.theme.EcoGreenLight

private data class HouseholdSection(val title: String, val icon: ImageVector)

private val householdSections = listOf(
    HouseholdSection("Início", Icons.Default.Home),
    HouseholdSection("Movimentos", Icons.Default.AccountBalanceWallet),
    HouseholdSection("Contas", Icons.Default.ReceiptLong),
    HouseholdSection("Metas", Icons.Default.Savings),
    HouseholdSection("Mais", Icons.Default.MoreHoriz)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HouseholdModeApp(onSwitchToBusiness: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { EcoRepository(context.applicationContext) }

    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    var calculatorsOpen by rememberSaveable { mutableStateOf(false) }

    var profile by remember { mutableStateOf(repository.loadHouseholdProfile()) }
    var entries by remember { mutableStateOf(repository.loadHouseholdEntries()) }
    var budgets by remember { mutableStateOf(repository.loadBudgets()) }
    var bills by remember { mutableStateOf(repository.loadRecurringBills()) }
    var subscriptions by remember { mutableStateOf(repository.loadSubscriptions()) }
    var goals by remember { mutableStateOf(repository.loadFinancialGoals()) }
    var gasPurchases by remember { mutableStateOf(repository.loadGasPurchases()) }

    var showProfileDialog by remember { mutableStateOf(profile == null) }
    var showEntryDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<HouseholdEntry?>(null) }
    var showBillDialog by remember { mutableStateOf(false) }
    var editingBill by remember { mutableStateOf<RecurringBill?>(null) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    var editingSubscription by remember { mutableStateOf<SubscriptionEntry?>(null) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<BudgetLimit?>(null) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<FinancialGoal?>(null) }
    var showGasDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (calculatorsOpen) {
                        IconButton(onClick = { calculatorsOpen = false }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(shape = CircleShape, color = EcoDark, modifier = Modifier.size(38.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (calculatorsOpen) Icons.Default.Calculate else Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = EcoGreenLight,
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                if (calculatorsOpen) "Calculadoras" else "EcoGestor Casa",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (calculatorsOpen) "Economize entendendo os números" else "Finanças e consumo consciente",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (!calculatorsOpen) {
                        TextButton(onClick = onSwitchToBusiness) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null)
                            Text("Negócio", modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (!calculatorsOpen) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    householdSections.forEachIndexed { index, section ->
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                            icon = { Icon(section.icon, contentDescription = section.title) },
                            label = { Text(section.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (calculatorsOpen) {
            CalculatorsScreen(Modifier.padding(innerPadding))
        } else {
            when (selectedIndex) {
                0 -> HouseholdHomeScreen(
                    profile = profile,
                    entries = entries,
                    budgets = budgets,
                    bills = bills,
                    subscriptions = subscriptions,
                    gasPurchases = gasPurchases,
                    onAddEntry = { showEntryDialog = true },
                    onEditProfile = { showProfileDialog = true },
                    onOpenBills = { selectedIndex = 2 },
                    onOpenMore = { selectedIndex = 4 },
                    modifier = Modifier.padding(innerPadding)
                )

                1 -> HouseholdTransactionsScreen(
                    entries = entries,
                    onAdd = { showEntryDialog = true },
                    onEdit = { editingEntry = it },
                    onDelete = { item ->
                        val updated = entries.filterNot { it.id == item.id }
                        repository.saveHouseholdEntries(updated)
                        entries = updated
                    },
                    modifier = Modifier.padding(innerPadding)
                )

                2 -> HouseholdBillsScreen(
                    bills = bills,
                    subscriptions = subscriptions,
                    onAddBill = { showBillDialog = true },
                    onEditBill = { editingBill = it },
                    onDeleteBill = { item ->
                        val updated = bills.filterNot { it.id == item.id }
                        repository.saveRecurringBills(updated)
                        bills = updated
                    },
                    onToggleBillActive = { item ->
                        val updated = bills.map { if (it.id == item.id) it.copy(active = !it.active) else it }
                        repository.saveRecurringBills(updated)
                        bills = updated
                    },
                    onToggleBillPaid = { item ->
                        val currentPeriod = periodForOffset(0)
                        val wasPaid = currentPeriod in item.paidPeriods
                        val updatedBill = if (wasPaid) {
                            item.copy(paidPeriods = item.paidPeriods - currentPeriod)
                        } else {
                            item.copy(paidPeriods = item.paidPeriods + currentPeriod)
                        }
                        val updatedBills = bills.map { if (it.id == item.id) updatedBill else it }
                        repository.saveRecurringBills(updatedBills)
                        bills = updatedBills

                        val token = "__bill:${item.id}:$currentPeriod"
                        val updatedEntries = if (wasPaid) {
                            entries.filterNot { it.notes == token }
                        } else if (entries.any { it.notes == token }) {
                            entries
                        } else {
                            listOf(
                                HouseholdEntry(
                                    id = System.currentTimeMillis(),
                                    kind = FinanceKind.EXPENSE,
                                    category = item.category,
                                    title = item.title,
                                    value = item.value,
                                    period = currentPeriod,
                                    recurring = true,
                                    notes = token
                                )
                            ) + entries
                        }
                        repository.saveHouseholdEntries(updatedEntries)
                        entries = updatedEntries
                    },
                    onAddSubscription = { showSubscriptionDialog = true },
                    onEditSubscription = { editingSubscription = it },
                    onDeleteSubscription = { item ->
                        val updated = subscriptions.filterNot { it.id == item.id }
                        repository.saveSubscriptions(updated)
                        subscriptions = updated
                    },
                    onToggleSubscription = { item ->
                        val updated = subscriptions.map { if (it.id == item.id) it.copy(active = !it.active) else it }
                        repository.saveSubscriptions(updated)
                        subscriptions = updated
                    },
                    onLaunchSubscription = { item ->
                        val currentPeriod = periodForOffset(0)
                        val token = "__subscription:${item.id}:$currentPeriod"
                        val existing = entries.firstOrNull { it.notes == token }
                        val newEntry = HouseholdEntry(
                            id = existing?.id ?: System.currentTimeMillis(),
                            kind = FinanceKind.EXPENSE,
                            category = HouseholdCategory.SUBSCRIPTIONS,
                            title = item.name,
                            value = item.value,
                            period = currentPeriod,
                            recurring = true,
                            notes = token,
                            createdAt = existing?.createdAt ?: System.currentTimeMillis()
                        )
                        val updated = if (existing == null) {
                            listOf(newEntry) + entries
                        } else {
                            entries.map { if (it.id == existing.id) newEntry else it }
                        }
                        repository.saveHouseholdEntries(updated)
                        entries = updated
                    },
                    modifier = Modifier.padding(innerPadding)
                )

                3 -> HouseholdFinancialGoalsScreen(
                    goals = goals,
                    onAdd = { showGoalDialog = true },
                    onEdit = { editingGoal = it },
                    onDelete = { item ->
                        val updated = goals.filterNot { it.id == item.id }
                        repository.saveFinancialGoals(updated)
                        goals = updated
                    },
                    modifier = Modifier.padding(innerPadding)
                )

                else -> HouseholdMoreScreen(
                    profile = profile,
                    entries = entries,
                    budgets = budgets,
                    gasPurchases = gasPurchases,
                    onEditProfile = { showProfileDialog = true },
                    onAddBudget = { showBudgetDialog = true },
                    onEditBudget = { editingBudget = it },
                    onDeleteBudget = { item ->
                        val updated = budgets.filterNot { it.id == item.id }
                        repository.saveBudgets(updated)
                        budgets = updated
                    },
                    onAddGas = { showGasDialog = true },
                    onDeleteGas = { item ->
                        val updated = gasPurchases.filterNot { it.id == item.id }
                        repository.saveGasPurchases(updated)
                        gasPurchases = updated
                    },
                    onOpenCalculators = { calculatorsOpen = true },
                    onExport = { shareHouseholdCsv(context, profile, entries) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    if (showProfileDialog) {
        HouseholdProfileFormDialog(
            profile = profile,
            onDismiss = { showProfileDialog = false },
            onSave = { item ->
                repository.saveHouseholdProfile(item)
                profile = item
                showProfileDialog = false
            }
        )
    }

    if (showEntryDialog || editingEntry != null) {
        HouseholdEntryFormDialog(
            entry = editingEntry,
            onDismiss = {
                showEntryDialog = false
                editingEntry = null
            },
            onSave = { item ->
                val updated = if (entries.any { it.id == item.id }) {
                    entries.map { if (it.id == item.id) item else it }
                } else {
                    listOf(item) + entries
                }.sortedByDescending { it.createdAt }
                repository.saveHouseholdEntries(updated)
                entries = updated
                showEntryDialog = false
                editingEntry = null
            }
        )
    }

    if (showBillDialog || editingBill != null) {
        RecurringBillFormDialog(
            item = editingBill,
            onDismiss = {
                showBillDialog = false
                editingBill = null
            },
            onSave = { item ->
                val updated = if (bills.any { it.id == item.id }) {
                    bills.map { if (it.id == item.id) item else it }
                } else listOf(item) + bills
                val sorted = updated.sortedBy { it.dueDay }
                repository.saveRecurringBills(sorted)
                bills = sorted
                showBillDialog = false
                editingBill = null
            }
        )
    }

    if (showSubscriptionDialog || editingSubscription != null) {
        SubscriptionFormDialog(
            item = editingSubscription,
            onDismiss = {
                showSubscriptionDialog = false
                editingSubscription = null
            },
            onSave = { item ->
                val updated = if (subscriptions.any { it.id == item.id }) {
                    subscriptions.map { if (it.id == item.id) item else it }
                } else listOf(item) + subscriptions
                val sorted = updated.sortedBy { it.dueDay }
                repository.saveSubscriptions(sorted)
                subscriptions = sorted
                showSubscriptionDialog = false
                editingSubscription = null
            }
        )
    }

    if (showBudgetDialog || editingBudget != null) {
        BudgetFormDialog(
            item = editingBudget,
            onDismiss = {
                showBudgetDialog = false
                editingBudget = null
            },
            onSave = { item ->
                val withoutSameCategory = budgets.filterNot { it.category == item.category && it.id != item.id }
                val updated = if (withoutSameCategory.any { it.id == item.id }) {
                    withoutSameCategory.map { if (it.id == item.id) item else it }
                } else listOf(item) + withoutSameCategory
                val sorted = updated.sortedBy { it.category.label }
                repository.saveBudgets(sorted)
                budgets = sorted
                showBudgetDialog = false
                editingBudget = null
            }
        )
    }

    if (showGoalDialog || editingGoal != null) {
        FinancialGoalFormDialog(
            item = editingGoal,
            onDismiss = {
                showGoalDialog = false
                editingGoal = null
            },
            onSave = { item ->
                val updated = if (goals.any { it.id == item.id }) {
                    goals.map { if (it.id == item.id) item else it }
                } else listOf(item) + goals
                repository.saveFinancialGoals(updated)
                goals = updated
                showGoalDialog = false
                editingGoal = null
            }
        )
    }

    if (showGasDialog) {
        GasPurchaseFormDialog(
            onDismiss = { showGasDialog = false },
            onSave = { item ->
                val updated = (listOf(item) + gasPurchases).sortedByDescending { it.createdAt }
                repository.saveGasPurchases(updated)
                gasPurchases = updated
                showGasDialog = false
            }
        )
    }
}
