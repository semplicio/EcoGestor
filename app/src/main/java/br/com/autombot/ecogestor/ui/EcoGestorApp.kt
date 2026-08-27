package br.com.autombot.ecogestor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.autombot.ecogestor.data.AppMode
import br.com.autombot.ecogestor.data.EcoRepository
import br.com.autombot.ecogestor.ui.theme.EcoDark
import br.com.autombot.ecogestor.ui.theme.EcoGreenLight

@Composable
fun EcoGestorApp() {
    val context = LocalContext.current
    val repository = remember(context) { EcoRepository(context.applicationContext) }
    var configured by remember { mutableStateOf(repository.isModeConfigured()) }
    var selectedMode by remember { mutableStateOf(repository.loadSelectedMode()) }

    if (!configured) {
        FirstUseModeScreen(
            onSelect = { mode ->
                repository.completeModeSetup(mode)
                selectedMode = mode
                configured = true
            },
            onUseBoth = {
                repository.completeModeSetup(AppMode.HOUSEHOLD)
                selectedMode = AppMode.HOUSEHOLD
                configured = true
            }
        )
        return
    }

    when (selectedMode) {
        AppMode.HOUSEHOLD -> HouseholdModeApp(
            onSwitchToBusiness = {
                repository.saveSelectedMode(AppMode.BUSINESS)
                selectedMode = AppMode.BUSINESS
            }
        )

        AppMode.BUSINESS -> BusinessModeApp(
            onSwitchToHousehold = {
                repository.saveSelectedMode(AppMode.HOUSEHOLD)
                selectedMode = AppMode.HOUSEHOLD
            }
        )
    }
}

@Composable
private fun FirstUseModeScreen(
    onSelect: (AppMode) -> Unit,
    onUseBoth: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 44.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(shape = CircleShape, color = EcoDark, modifier = Modifier.size(56.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Eco,
                                contentDescription = null,
                                tint = EcoGreenLight,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            "EcoGestor",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Economia para sua casa e seu negócio",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    "Como você pretende usar o EcoGestor?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 18.dp)
                )
                Text(
                    "Os dados de casa e da empresa ficam separados. Você pode alternar entre os dois modos quando quiser.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            item {
                ModeCard(
                    title = "Controlar minha casa",
                    description = "Renda, gastos, contas, assinaturas, metas e economia doméstica.",
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    onClick = { onSelect(AppMode.HOUSEHOLD) }
                )
            }

            item {
                ModeCard(
                    title = "Controlar meu negócio",
                    description = "Consumos, custos, metas sustentáveis e indicadores da empresa.",
                    icon = { Icon(Icons.Default.Business, contentDescription = null) },
                    onClick = { onSelect(AppMode.BUSINESS) }
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null)
                            Text("Usar casa e negócio", fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "Comece pelo modo Casa e alterne para Negócio pelo botão no topo do aplicativo.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        OutlinedButton(
                            onClick = onUseBoth,
                            modifier = Modifier.fillMaxWidth().padding(top = 14.dp)
                        ) {
                            Text("Usar os dois")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) { icon() }
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 14.dp)
            )
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Começar")
            }
        }
    }
}
