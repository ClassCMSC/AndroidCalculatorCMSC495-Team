package com.example.calculator_cmsc495

import android.os.Bundle
import kotlin.math.* // Math helpers (reserved for future)

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // Tap history rows
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn // Scrollable history list
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*

import androidx.compose.runtime.* // Compose state
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.hapticfeedback.HapticFeedbackType // Haptic types
import androidx.compose.ui.platform.LocalHapticFeedback // Haptic engine access

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// App entry point — launches the Compose screen.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Host the Compose UI.
        setContent { CalculatorScreen() }
    }
}

// Data model for “expression = result” history rows.
class CalcHistory(
    val expression: String,
    val result: String
)

// App color palette (single source of truth).
private object CalcColors {
    // App surfaces
    val Frame = Color(0xFF0B0B0E)       // background
    val Display = Color(0xFFF2E7D6)     // display surface
    val DisplayText = Color(0xFF0B0B0E) // display text

    // History surfaces
    val HistoryPanel = Color(0xFF141019)
    val HistoryCard = Color(0xFF1B1422)
    val HistoryTitleText = Color(0xFFF7EEDC)
    val HistoryItemText = Color(0xFFEFE3CF)

    // Buttons
    val NumberBtn = Color(0xFF161218)
    val OperatorBtn = Color(0xFFD4AF37)
    val EqualsBtn = Color(0xFFFFD66B)

    val ClearBtn = Color(0xFF5B0F1A)
    val BackspaceBtn = Color(0xFFB8872B)
    val HistoryBtn = Color(0xFF4B1F6F)

    // Text
    val BtnText = Color(0xFFF8F2E6)
}

@Composable
fun CalculatorScreen() {

    // Haptics handle (used across UI events).
    val haptic = LocalHapticFeedback.current

    // ---- Calculator state (single screen) ----
    var currentValue by remember { mutableStateOf(0.0) }           // number being edited
    var previousValue by remember { mutableStateOf(0.0) }          // stored before operator
    var displayText by remember { mutableStateOf("0") }            // what user sees
    var operation by remember { mutableStateOf("") }               // "+", "-", "×", "÷"
    var isNewNumber by remember { mutableStateOf(true) }           // next digit starts fresh
    var history by remember { mutableStateOf(listOf<CalcHistory>()) } // immutable list state
    var showHistory by remember { mutableStateOf(false) }          // panel toggle

    // Append digits/decimal to displayText.
    fun appendNumber(num: String) {
        if (isNewNumber) {
            // Start new entry (normalize ".").
            displayText = if (num == ".") "0." else num
            isNewNumber = false
        } else {
            // Block double decimals.
            if (num == "." && displayText.contains(".")) return

            // Replace leading zero unless typing a decimal.
            displayText = if (displayText == "0" && num != ".") num else displayText + num
        }

        // Keep numeric state synced.
        currentValue = displayText.toDoubleOrNull() ?: 0.0
    }

    // Remove last char; fallback to "0".
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

    // Evaluate current operation.
    fun calculate() {
        val result = when (operation) {
            "+" -> previousValue + currentValue
            "-" -> previousValue - currentValue
            "×" -> previousValue * currentValue
            "÷" -> if (currentValue != 0.0) previousValue / currentValue else Double.NaN
            else -> currentValue
        }

        // Format output for readability.
        val resultText =
            if (result.isNaN()) {
                "Error"
            } else if (result % 1.0 == 0.0) {
                result.toLong().toString()
            } else {
                String.format("%.8f", result).trimEnd('0').trimEnd('.')
            }

        // Only log history when an operator was used.
        if (operation.isNotEmpty()) {
            history = history + CalcHistory(
                expression = "$previousValue $operation $currentValue",
                result = resultText
            )
        }

        // Reset op state for next entry.
        displayText = resultText
        currentValue = result
        previousValue = 0.0
        operation = ""
        isNewNumber = true
    }

    // Store operator, optionally auto-calc for chaining.
    fun executeOperation(oper: String) {
        if (operation.isNotEmpty() && !isNewNumber) {
            // Example: 2 + 3 + 4 → calc before switching operator.
            calculate()
        }
        previousValue = currentValue
        operation = oper
        isNewNumber = true
    }

    // Reset calculator state (does not clear history).
    fun clear() {
        displayText = "0"
        currentValue = 0.0
        previousValue = 0.0
        operation = ""
        isNewNumber = true
    }

    // ---- UI layout ----
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CalcColors.Frame)
            .padding(16.dp)
    ) {

        // History panel (toggle with ⏱).
        if (showHistory) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = CalcColors.HistoryPanel)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {

                    // Header row: title + Clear action.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "History",
                            color = CalcColors.HistoryTitleText,
                            fontSize = 20.sp,
                            modifier = Modifier.weight(1f)
                        )

                        // Clear history (destructive action).
                        TextButton(
                            onClick = {
                                history = emptyList()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        ) {
                            Text("Clear", color = CalcColors.HistoryTitleText)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // History list
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(history) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                colors = CardDefaults.cardColors(containerColor = CalcColors.HistoryCard)
                            ) {
                                // Tap row → reuse result as next input.
                                Text(
                                    text = "${item.expression} = ${item.result}",
                                    color = CalcColors.HistoryItemText,
                                    fontSize = 16.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // Reuse result safely ("Error" becomes 0.0)
                                            displayText = item.result
                                            currentValue = item.result.toDoubleOrNull() ?: 0.0

                                            // Reset operation so we don't chain old ops.
                                            previousValue = 0.0
                                            operation = ""
                                            isNewNumber = true

                                            // Light feedback.
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                        .padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Display area (shrinks when history is open).
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
    val haptic = LocalHapticFeedback.current // haptics for button taps

    Button(
        onClick = {
            // Light feedback first, then run button logic.
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
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
