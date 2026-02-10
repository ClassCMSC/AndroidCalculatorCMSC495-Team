package com.example.calculator_cmsc495

import kotlin.math.*                     // Math helpers (some reserved for future)
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn          // Scrollable history list
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material3.*
import androidx.compose.runtime.*                            // Compose state
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// App entry point — launches the Compose screen.
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CalculatorScreen() }
    }
}

// Tiny container for “expression = result” history rows.
class CalcHistory(
    val expression: String,
    val result: String
)

// Glitter-gold + plum + ink palette.
// Only tweak these values to re-skin everything.
private object CalcColors {

    // App surfaces
    val Frame = Color(0xFF0B0B0E)          // ink black (background)
    val Display = Color(0xFFF2E7D6)        // warm parchment
    val DisplayText = Color(0xFF0B0B0E)    // ink text

    // History surfaces
    val HistoryPanel = Color(0xFF141019)   // deep plum-ink
    val HistoryCard  = Color(0xFF1B1422)   // slightly lighter plum
    val HistoryTitleText = Color(0xFFF7EEDC)
    val HistoryItemText  = Color(0xFFEFE3CF)

    // Buttons
    val NumberBtn   = Color(0xFF161218)    // ink charcoal (numbers)
    val OperatorBtn = Color(0xFFD4AF37)    // classic gold (ops)
    val EqualsBtn   = Color(0xFFFFD66B)    // bright "glitter" gold pop (equals)

    val ClearBtn    = Color(0xFF5B0F1A)    // deep wine (AC)
    val BackspaceBtn= Color(0xFFB8872B)    // warm gold-brown (⌫)
    val HistoryBtn  = Color(0xFF4B1F6F)    // plum purple (⏱)

    // Text
    val BtnText     = Color(0xFFF8F2E6)    // warm off-white
}

@Composable
fun CalculatorScreen() {

    // --- Calculator state ---
    var currentValue by remember { mutableStateOf(0.0) }     // number being typed/used
    var previousValue by remember { mutableStateOf(0.0) }    // stored value before operator
    val haptic = LocalHapticFeedback.current // haptics for taps
    var displayText by remember { mutableStateOf("0") }      // what user sees
    var operation by remember { mutableStateOf("") }         // +, -, ×, ÷
    var isNewNumber by remember { mutableStateOf(true) }     // are we starting fresh input?
    var history by remember { mutableStateOf(listOf<CalcHistory>()) }
    var showHistory by remember { mutableStateOf(false) }

    // Adds digits (or decimal) to the display.
    fun appendNumber(num: String) {

        if (isNewNumber) {
            // Starting a fresh number: "." becomes "0." like a normal calculator.
            displayText = if (num == ".") "0." else num
            isNewNumber = false
        } else {
            // Don't allow multiple decimals.
            if (num == "." && displayText.contains(".")) return

            // Replace leading zero unless we're typing a decimal.
            displayText = if (displayText == "0" && num != ".") {
                num
            } else {
                // IMPORTANT: must assign back, or multi-digit input “does nothing”.
                displayText + num
            }
        }

        // Keep numeric state synced to the display.
        currentValue = displayText.toDoubleOrNull() ?: 0.0
    }

    // Deletes the last character, or resets if we're basically empty.
    fun backspace() {
        if (!isNewNumber && displayText.length > 1) {
            displayText = displayText.dropLast(1)
            currentValue = displayText.toDoubleOrNull() ?: 0.0
        } else {
            displayText = "0"
            currentValue = 0.0
            isNewNumber = true
        }
    }

    // Runs the current operation and formats the result nicely.
    fun calculate() {

        val result = when (operation) {
            "+" -> previousValue + currentValue
            "-" -> previousValue - currentValue
            "×" -> previousValue * currentValue
            "÷" -> if (currentValue != 0.0) previousValue / currentValue else Double.NaN
            else -> currentValue
        }

        // "Error" for invalid math, otherwise clean formatting.
        val resultText =
            if (result.isNaN()) {
                "Error"
            } else if (result % 1.0 == 0.0) {
                result.toLong().toString()
            } else {
                String.format("%.8f", result).trimEnd('0').trimEnd('.')
            }

        // Only add history when a real operation happened.
        if (operation.isNotEmpty()) {
            history = history + CalcHistory(
                expression = "$previousValue $operation $currentValue",
                result = resultText
            )
        }

        // Reset for next input.
        displayText = resultText
        currentValue = result
        previousValue = 0.0
        operation = ""
        isNewNumber = true
    }

    // Stores the operator and prepares for the next number.
    fun executeOperation(oper: String) {
        // Allows chaining like 2 + 3 + 4 (auto-calc before switching ops).
        if (operation.isNotEmpty() && !isNewNumber) {
            calculate()
        }

        previousValue = currentValue
        operation = oper
        isNewNumber = true
    }

    // Full reset.
    fun clear() {
        displayText = "0"
        currentValue = 0.0
        previousValue = 0.0
        operation = ""
        isNewNumber = true
    }

    // --- UI ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CalcColors.Frame)
            .padding(16.dp)
    ) {

        // History panel (toggle with the ⏱ button)
        if (showHistory) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = CalcColors.HistoryPanel)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {

                    Text(
                        text = "History",
                        color = CalcColors.HistoryTitleText,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                     
                     Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                    TextButton(
                        onClick = { // Clear all saved calculations
                        history = emptyList()

            // Stronger haptic for destructive action
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                         ) {
                     Text("Clear", color = CalcColors.HistoryTitleText)
                     }
                  }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(history) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                colors = CardDefaults.cardColors(containerColor = CalcColors.HistoryCard)
                            ) {
                                Text(
                                    text = "${item.expression} = ${item.result}",
                                    color = CalcColors.HistoryItemText,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Display area (shrinks when history is open)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (showHistory) 0.3f else 1f),
            colors = CardDefaults.cardColors(containerColor = CalcColors.Display)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = displayText,
                    color = CalcColors.DisplayText,
                    fontSize = 54.sp,
                    textAlign = TextAlign.End,
                    maxLines = 2
                )
            }
        }

        // Button grid
        Column(modifier = Modifier.fillMaxWidth()) {

            Row(modifier = Modifier.fillMaxWidth()) {
                CalcButton("AC", Modifier.weight(1f), CalcColors.ClearBtn) { clear() }
                CalcButton("⌫", Modifier.weight(1f), CalcColors.BackspaceBtn) { backspace() }
                CalcButton("⏱", Modifier.weight(1f), CalcColors.HistoryBtn) { showHistory = !showHistory }
                CalcButton("÷", Modifier.weight(1f), CalcColors.OperatorBtn) { executeOperation("÷") }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                CalcButton("7", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber("7") }
                CalcButton("8", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber("8") }
                CalcButton("9", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber("9") }
                CalcButton("×", Modifier.weight(1f), CalcColors.OperatorBtn) { executeOperation("×") }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                CalcButton("4", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber("4") }
                CalcButton("5", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber("5") }
                CalcButton("6", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber("6") }
                CalcButton("-", Modifier.weight(1f), CalcColors.OperatorBtn) { executeOperation("-") }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                CalcButton("1", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber("1") }
                CalcButton("2", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber("2") }
                CalcButton("3", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber("3") }
                CalcButton("+", Modifier.weight(1f), CalcColors.OperatorBtn) { executeOperation("+") }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                CalcButton("0", Modifier.weight(2f), CalcColors.NumberBtn) { appendNumber("0") }
                CalcButton(".", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber(".") }
                CalcButton("=", Modifier.weight(1f), CalcColors.EqualsBtn) { calculate() }
            }
        }
    }
}

// Reusable button so layout/styling stays consistent.
@Composable
fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(80.dp).padding(5.dp),
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            color = CalcColors.BtnText
        )
    }
}
