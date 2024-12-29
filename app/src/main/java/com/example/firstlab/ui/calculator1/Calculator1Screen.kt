package com.example.firstlab.ui.calculator1

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstlab.ui.theme.Purple40
import com.example.firstlab.ui.theme.White

// компоненти палива
data class FuelComposition(
    val hp: Double = 0.0,
    val cp: Double = 0.0,
    val sp: Double = 0.0,
    val np: Double = 0.0,
    val op: Double = 0.0,
    val wp: Double = 0.0,
    val ap: Double = 0.0
)

// результати розрахунків
data class CalculationResults(
    val dryMassCoefficient: Double = 0.0,
    val combustibleMassCoefficient: Double = 0.0,
    val dryComposition: Map<String, Double> = emptyMap(),
    val combustibleComposition: Map<String, Double> = emptyMap(),
    val lowerHeatingValue: Double = 0.0,
    val lowerDryHeatingValue:  Double = 0.0,
    val lowerCombustibleHeatingValue: Double = 0.0
)

@Composable
fun Calculator1Screen (
        goBack: () -> Unit,
) {
    var composition by remember { mutableStateOf(FuelComposition()) }
    var results by remember { mutableStateOf<CalculationResults?>(null) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Калькулятор складу палива",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        InputField("H^P, %", composition.hp) { composition = composition.copy(hp = it) }
        InputField("C^P, %", composition.cp) { composition = composition.copy(cp = it) }
        InputField("S^P, %", composition.sp) { composition = composition.copy(sp = it) }
        InputField("N^P, %", composition.np) { composition = composition.copy(np = it) }
        InputField("O^P, %", composition.op) { composition = composition.copy(op = it) }
        InputField("W^P, %", composition.wp) { composition = composition.copy(wp = it) }
        InputField("A^P, %", composition.ap) { composition = composition.copy(ap = it) }

        Button(
            onClick = { results = calculateResults(composition) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .size(width = 300.dp, height = 50.dp),

        ) {
            Text("Розрахувати")
        }

        Button(
            onClick = goBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .size(width = 300.dp, height = 50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = White),
            border = BorderStroke(2.dp, Purple40)
        ) {
            Text(
                text = "Повернутися",
                fontSize = 16.sp,
                color = Purple40
            )
        }
        results?.let { DisplayResults(it) }
    }
}

@Composable
fun InputField(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit
) {
    OutlinedTextField(
        value = if (value == 0.0) "" else value.toString(),
        onValueChange = {
            onValueChange(it.toDoubleOrNull() ?: 0.0)
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
fun DisplayResults(results: CalculationResults) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            "Результати розрахунків:",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ResultSection("Коефіцієнти переходу:") {
            ResultItem("К^PС", results.dryMassCoefficient)
            ResultItem("К^PГ", results.combustibleMassCoefficient)
        }

        ResultSection("Суха маса:") {
            results.dryComposition.forEach { (key, value) ->
                ResultItem(key, value)
            }
        }

        ResultSection("Горюча маса:") {
            results.combustibleComposition.forEach { (key, value) ->
                ResultItem(key, value)
            }
        }

        ResultSection("Нижча теплота згоряння:") {
            ResultItem("Для робочої маси", results.lowerHeatingValue)
            ResultItem("Для сухої маси", results.lowerDryHeatingValue)
            ResultItem("Для горючої маси", results.lowerCombustibleHeatingValue)
        }
    }
}

@Composable
fun ResultSection(title: String, content: @Composable () -> Unit) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
    content()
}

@Composable
fun ResultItem(label: String, value: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(String.format("%.2f", value))
    }
}

private fun calculateResults(composition: FuelComposition): CalculationResults {
    // кофіцієнт переходу від робочої до сухої маси
    val kpc = 100.0 / (100.0 - composition.wp)
    // кофіцієнт переходу від робочої до горючої маси
    val kpg = 100.0 / (100.0 - composition.wp - composition.ap)

    // cклад сухої маси палива
    val dryComposition = mapOf(
        "H^C" to composition.hp * kpc,
        "C^C" to composition.cp * kpc,
        "S^C" to composition.sp * kpc,
        "N^C" to composition.np * kpc,
        "O^C" to composition.op * kpc,
        "A^C" to composition.ap * kpc
    )

    // cклад горючої маси палива
    val combustibleComposition = mapOf(
        "H^Г" to composition.hp * kpg,
        "C^Г" to composition.cp * kpg,
        "S^Г" to composition.sp * kpg,
        "N^Г" to composition.np * kpg,
        "O^Г" to composition.op * kpg
    )
    // нижча теплота згоряння для робочої маси
    val qph = (339 * composition.cp +
            1030 * composition.hp -
            108.8 * (composition.op - composition.sp) -
            25 * composition.wp) / 1000
    // нижча теплота згоряння для сухої маси
    val qch = (qph + 0.025 * composition.wp) * (100.0 / (100.0 - composition.wp))
    // нижча теплота згоряння для горючої маси
    val qgh = (qph + 0.025 * composition.wp) * (100.0 / (100.0 - composition.wp - composition.ap))

    return CalculationResults(
        dryMassCoefficient = kpc,
        combustibleMassCoefficient = kpg,
        dryComposition = dryComposition,
        combustibleComposition = combustibleComposition,
        lowerHeatingValue = qph,
        lowerDryHeatingValue = qch,
        lowerCombustibleHeatingValue = qgh
    )
}

