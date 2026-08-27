package br.com.autombot.ecogestor.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale

private enum class CalculatorType(val label: String) {
    BASIC("Comum"),
    BUDGET("Orçamento"),
    DISCOUNT("Desconto"),
    INSTALLMENT("Parcelamento"),
    ANNUAL("Custo anual"),
    ENERGY("Energia"),
    FUEL("Combustível"),
    GAS("Gás")
}

@Composable
internal fun CalculatorsScreen(modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf(CalculatorType.BASIC) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Calculadoras EcoGestor",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Ferramentas rápidas para enxergar o custo real das suas escolhas.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorType.entries.forEach { type ->
                    FilterChip(
                        selected = selected == type,
                        onClick = { selected = type },
                        label = { Text(type.label) }
                    )
                }
            }
        }

        item {
            when (selected) {
                CalculatorType.BASIC -> BasicCalculator()
                CalculatorType.BUDGET -> BudgetCalculator()
                CalculatorType.DISCOUNT -> DiscountCalculator()
                CalculatorType.INSTALLMENT -> InstallmentCalculator()
                CalculatorType.ANNUAL -> AnnualCostCalculator()
                CalculatorType.ENERGY -> EnergyCalculator()
                CalculatorType.FUEL -> FuelCalculator()
                CalculatorType.GAS -> GasCalculator()
            }
        }
    }
}

@Composable
private fun BasicCalculator() {
    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }
    var operation by remember { mutableStateOf("+") }
    val a = parseDecimal(first)
    val b = parseDecimal(second)
    val result = if (a != null && b != null) {
        when (operation) {
            "+" -> a + b
            "−" -> a - b
            "×" -> a * b
            "÷" -> if (b != 0.0) a / b else null
            "%" -> a * (b / 100.0)
            else -> null
        }
    } else null

    CalculatorCard("Calculadora comum") {
        DecimalField(first, { first = it }, "Primeiro valor")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("+", "−", "×", "÷", "%").forEach { op ->
                FilterChip(selected = operation == op, onClick = { operation = op }, label = { Text(op) })
            }
        }
        DecimalField(second, { second = it }, if (operation == "%") "Percentual" else "Segundo valor")
        ResultCard(
            if (result == null) "Informe valores válidos"
            else "Resultado: ${formatNumber(result)}"
        )
    }
}

@Composable
private fun BudgetCalculator() {
    var income by remember { mutableStateOf("") }
    var expenses by remember { mutableStateOf("") }
    val incomeValue = parseDecimal(income) ?: 0.0
    val expenseValue = parseDecimal(expenses) ?: 0.0
    val remaining = incomeValue - expenseValue
    val percent = if (incomeValue > 0) expenseValue / incomeValue * 100.0 else 0.0

    CalculatorCard("Orçamento mensal") {
        DecimalField(income, { income = it }, "Renda")
        DecimalField(expenses, { expenses = it }, "Despesas")
        ResultCard(
            "Disponível: ${formatCurrency(remaining)}\nRenda comprometida: ${String.format(Locale("pt", "BR"), "%.1f", percent)}%"
        )
    }
}

@Composable
private fun DiscountCalculator() {
    var price by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    val priceValue = parseDecimal(price) ?: 0.0
    val discountValue = (parseDecimal(discount) ?: 0.0).coerceIn(0.0, 100.0)
    val saving = priceValue * discountValue / 100.0

    CalculatorCard("Desconto") {
        DecimalField(price, { price = it }, "Preço original")
        DecimalField(discount, { discount = it }, "Desconto (%)")
        ResultCard("Preço final: ${formatCurrency(priceValue - saving)}\nEconomia: ${formatCurrency(saving)}")
    }
}

@Composable
private fun InstallmentCalculator() {
    var price by remember { mutableStateOf("") }
    var installments by remember { mutableStateOf("") }
    var monthlyInterest by remember { mutableStateOf("") }
    val priceValue = parseDecimal(price) ?: 0.0
    val count = installments.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val rate = (parseDecimal(monthlyInterest) ?: 0.0) / 100.0
    val total = if (rate > 0.0) priceValue * Math.pow(1.0 + rate, count.toDouble()) else priceValue
    val installment = if (count > 0) total / count else total

    CalculatorCard("Parcelamento") {
        DecimalField(price, { price = it }, "Valor da compra")
        NumberField(installments, { installments = it }, "Número de parcelas")
        DecimalField(monthlyInterest, { monthlyInterest = it }, "Juros ao mês (%) - opcional")
        ResultCard("$count × ${formatCurrency(installment)}\nTotal estimado: ${formatCurrency(total)}")
    }
}

@Composable
private fun AnnualCostCalculator() {
    var monthly by remember { mutableStateOf("") }
    val monthlyValue = parseDecimal(monthly) ?: 0.0
    CalculatorCard("Custo anual") {
        DecimalField(monthly, { monthly = it }, "Custo mensal")
        ResultCard("Em 12 meses: ${formatCurrency(monthlyValue * 12)}\nEm 5 anos: ${formatCurrency(monthlyValue * 60)}")
    }
}

@Composable
private fun EnergyCalculator() {
    var watts by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("") }
    var tariff by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("30") }
    val power = parseDecimal(watts) ?: 0.0
    val dailyHours = parseDecimal(hours) ?: 0.0
    val tariffValue = parseDecimal(tariff) ?: 0.0
    val dayCount = days.toIntOrNull()?.coerceAtLeast(1) ?: 30
    val kwh = power / 1000.0 * dailyHours * dayCount

    CalculatorCard("Custo de energia") {
        DecimalField(watts, { watts = it }, "Potência do aparelho (W)")
        DecimalField(hours, { hours = it }, "Horas de uso por dia")
        DecimalField(tariff, { tariff = it }, "Tarifa (R$/kWh)")
        NumberField(days, { days = it }, "Dias no mês")
        ResultCard("Consumo: ${formatNumber(kwh)} kWh\nCusto estimado: ${formatCurrency(kwh * tariffValue)}")
    }
}

@Composable
private fun FuelCalculator() {
    var distance by remember { mutableStateOf("") }
    var efficiency by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    val distanceValue = parseDecimal(distance) ?: 0.0
    val efficiencyValue = parseDecimal(efficiency) ?: 0.0
    val fuelPrice = parseDecimal(price) ?: 0.0
    val liters = if (efficiencyValue > 0) distanceValue / efficiencyValue else 0.0

    CalculatorCard("Custo da viagem") {
        DecimalField(distance, { distance = it }, "Distância (km)")
        DecimalField(efficiency, { efficiency = it }, "Consumo do veículo (km/l)")
        DecimalField(price, { price = it }, "Preço do combustível (R$/l)")
        ResultCard("Combustível: ${formatNumber(liters)} litros\nCusto estimado: ${formatCurrency(liters * fuelPrice)}")
    }
}

@Composable
private fun GasCalculator() {
    var price by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("") }
    val priceValue = parseDecimal(price) ?: 0.0
    val duration = days.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val daily = priceValue / duration

    CalculatorCard("Custo do gás") {
        DecimalField(price, { price = it }, "Valor do botijão")
        NumberField(days, { days = it }, "Quantos dias durou")
        ResultCard("Custo diário: ${formatCurrency(daily)}\nCusto médio em 30 dias: ${formatCurrency(daily * 30)}")
    }
}

@Composable
private fun CalculatorCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun DecimalField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun NumberField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit)) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ResultCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatNumber(value: Double): String =
    String.format(Locale("pt", "BR"), "%.2f", value)
