package com.capstone.androidcalculatorcmsc

import kotlin.math.*                     // Math helpers (some reserved for future)
import net.objecthunter.exp4j.ExpressionBuilder //Used for expressions and order of operations
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn          // Scrollable history list
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
    val historyExpression: String,
    val historyResult: String
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
    var expression by remember { mutableStateOf("") }   //current expression
    var displayText by remember { mutableStateOf("0") }      // what user sees
    var expressionEnded by remember { mutableStateOf(false) }   //tracks when the expression ends
    var history by remember { mutableStateOf(listOf<CalcHistory>()) }
    var showHistory by remember { mutableStateOf(false) }

    // Adds digits (or decimal) to the current expression
    fun appendNumber(num: String) {
        if (expressionEnded) {
            expression = num //creates a new expression if "equals" has been hit
            expressionEnded = false
        } else {
            expression += num
        }
        displayText = expression.ifEmpty{"0"}
    }

    //Adds an operator to the expression when valid
    fun appendOperator(oper : String) {
        if (expression.isNotEmpty() && (expression.last().isDigit() || expression.last() == ')')) {
            expression += oper
            displayText = expression
        }
        expressionEnded = false //allows the expression to continue
    }

    //Keeps track of parentheses used in the expression and adds open/close
    //parentheses as needed
    fun addParentheses() {
        val countOpenParentheses = expression.count { char -> char == '(' }
        val countClosedParentheses = expression.count { char -> char == ')' }

        if (expression.isEmpty() || expression.last() in setOf('+', '-', '×', '÷', '(')) {
            expression += "("
            //closes any open parentheses
        } else if (countOpenParentheses > countClosedParentheses) {
            expression += ")"
            //adds multiplication operator to display if the last entry is not
            //multiply (a number adjacent to open parentheses implies multiplication)
        } else if (expression.last() != '×') {
            expression += "×("
            //if multiply is selected, omit the multiplication operator
            //on the display
        } else
            expression += "("
        displayText = expression
        expressionEnded = false
    }

    //formats a double into a usable String
    fun Double.formatNumber() : String {
        return if (this.isNaN() || this.isInfinite()) {
            "Error"
        } else
            String.format("%.8f", this).trimEnd('0').trimEnd('.')
    }

    //formats to make a String compatible with exp4j
    fun String.formatToExp4j() : String {
        return this.replace("×", "*")
            .replace("÷", "/")
    }

    fun calculate() {
        if (expression.isEmpty()) {
            return
        }
        try {
            //build -> parses the expression & checks for errors (exp4j)
            //evaluate -> calculates the expression and returns double
            val result = ExpressionBuilder(expression.formatToExp4j()).build().evaluate()

            val resultText = result.formatNumber()

            //add to history
            history += CalcHistory(historyExpression = expression,
                historyResult = resultText)

            displayText = resultText
            expression = resultText
            expressionEnded = true

            //display error properly in case of exception
        } catch(e: Exception) {
            displayText = "Error"
            history += CalcHistory(historyExpression = expression,
                historyResult = "Error")
        }
    }

    // Deletes the last character, or resets to 0 if the expression is empty
    fun backspace() {
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
            displayText = if (expression.isEmpty()) "0" else expression
        }
    }

    // Full reset.
    fun clear() {
        displayText = "0"
        expression = ""
        expressionEnded = true
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

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(history) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                colors = CardDefaults.cardColors(containerColor = CalcColors.HistoryCard)
                            ) {
                                Text(
                                    text = "${item.historyExpression} = ${item.historyResult}",
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
                CalcButton("÷", Modifier.weight(1f), CalcColors.OperatorBtn) { appendOperator("÷") }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                CalcButton("7", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber("7") }
                CalcButton("8", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber("8") }
                CalcButton("9", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber("9") }
                CalcButton("×", Modifier.weight(1f), CalcColors.OperatorBtn) { appendOperator("×") }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                CalcButton("4", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber("4") }
                CalcButton("5", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber("5") }
                CalcButton("6", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber("6") }
                CalcButton("-", Modifier.weight(1f), CalcColors.OperatorBtn) { appendOperator("-") }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                CalcButton("1", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber("1") }
                CalcButton("2", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber("2") }
                CalcButton("3", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber("3") }
                CalcButton("+", Modifier.weight(1f), CalcColors.OperatorBtn) { appendOperator("+") }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                CalcButton("( )", Modifier.weight(1f), CalcColors.OperatorBtn) { addParentheses() }
                CalcButton("0", Modifier.weight(1f), CalcColors.NumberBtn) { appendNumber("0") }
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