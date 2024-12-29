package com.example.firstlab.ui.calculator2


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstlab.ui.theme.Purple40
import com.example.firstlab.ui.theme.White

// компоненти мазуту
data class MazutComposition(
    val carbonCombustible: Double = 0.0,
    val hydrogenCombustible: Double = 0.0,
    val oxygenCombustible: Double = 0.0,
    val sulfurCombustible: Double = 0.0,
    val vanadiumCombustible: Double = 0.0,
    val moistureContent: Double = 0.0,
    val ashDry: Double = 0.0,
    val heatingValueCombustible: Double = 0.0
)

// результати розрахунків
data class MazutResults(
    val workingComposition: Map<String, Double> = emptyMap(),
    val workingHeatingValue: Double = 0.0
)

@Composable
fun Calculator2Screen(
    goBack: () -> Unit,
) {
    var composition by remember { mutableStateOf(MazutComposition()) }
    var results by remember { mutableStateOf<MazutResults?>(null) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Калькулятор складу мазуту",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Склад горючої маси", style = MaterialTheme.typography.titleMedium)
                InputField("Вуглець (C^Г), %", composition.carbonCombustible) {
                    composition = composition.copy(carbonCombustible = it)
                }
                InputField("Водень (H^Г), %", composition.hydrogenCombustible) {
                    composition = composition.copy(hydrogenCombustible = it)
                }
                InputField("Кисень (O^Г), %", composition.oxygenCombustible) {
                    composition = composition.copy(oxygenCombustible = it)
                }
                InputField("Сірка (S^Г), %", composition.sulfurCombustible) {
                    composition = composition.copy(sulfurCombustible = it)
                }
                InputField("Ванадій (V^Г), мг/кг", composition.vanadiumCombustible) {
                    composition = composition.copy(vanadiumCombustible = it)
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Додаткові параметри", style = MaterialTheme.typography.titleMedium)
                InputField("Вологість, %", composition.moistureContent) {
                    composition = composition.copy(moistureContent = it)
                }
                InputField("Зольність, %", composition.ashDry) {
                    composition = composition.copy(ashDry = it)
                }
                InputField(
                    "Нижча теплота згоряння, МДж/кг",
                    composition.heatingValueCombustible
                ) {
                    composition = composition.copy(heatingValueCombustible = it)
                }
            }
        }

        Button(
            onClick = { results = calculateMazutResults(composition) },
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

        results?.let { DisplayMazutResults(it) }
    }
}

@Composable
fun DisplayMazutResults(results: MazutResults) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Результати розрахунків",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text("Склад робочої маси:", style = MaterialTheme.typography.titleSmall)
            results.workingComposition.forEach { (component, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(component)
                    if (component.contains("V")) {
                        Text("${String.format("%.2f", value)} мг/кг")
                    } else {
                        Text("${String.format("%.2f", value)}%")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Нижча теплота згоряння робочої маси:", style = MaterialTheme.typography.titleSmall)
            Text("${String.format("%.2f", results.workingHeatingValue)} МДж/кг")
        }
    }
}

private fun calculateMazutResults(composition: MazutComposition): MazutResults {
    // формула перерахунку складу палива
    val conversionFactor = (100.0 - composition.moistureContent - composition.ashDry) / 100.0

    // перерахунок для кожного компонента
    val workingComposition = mapOf(
        "C^P" to composition.carbonCombustible * conversionFactor,
        "H^P" to composition.hydrogenCombustible * conversionFactor,
        "O^P" to composition.oxygenCombustible * conversionFactor,
        "S^P" to composition.sulfurCombustible * conversionFactor,
        // віднімання - отримання маси без вологи (суха маса)
        // оскільки ванадій вказується відносно маси без вологи
        // ділення - отримання відсотку
        "V^P" to composition.vanadiumCombustible * (100.0 - composition.moistureContent) / 100.0,
        // віднімання - отримання відсотка маси без вологи
        // оскільки зола вказується відносно маси без вологи
        // ділення - отримання відсотку
        "A^P" to composition.ashDry * (100.0 - composition.moistureContent) / 100.0
    )

    // розрахунок нижчої теплоти згорання
    val workingHeatingValue = composition.heatingValueCombustible *
            (100.0 - composition.moistureContent - composition.ashDry) / 100.0 -
            0.025 * composition.moistureContent

    return MazutResults(
        workingComposition = workingComposition,
        workingHeatingValue = workingHeatingValue
    )
}

@Composable
private fun InputField(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit
) {
    OutlinedTextField(
        value = if (value == 0.0) "" else value.toString(),
        onValueChange = { onValueChange(it.toDoubleOrNull() ?: 0.0) },
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}
