package com.siroha.win11launcher.core

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorScreen(onDismiss: () -> Unit) {
    var display by remember { mutableStateOf("0") }
    var previousValue by remember { mutableStateOf<Double?>(null) }
    var pendingOperation by remember { mutableStateOf<String?>(null) }
    var isNewInput by remember { mutableStateOf(true) }

    fun calculate(a: Double, b: Double, op: String): Double {
        return when (op) {
            "+" -> a + b
            "-" -> a - b
            "\u00d7" -> a * b
            "\u00f7" -> if (b != 0.0) a / b else 0.0
            else -> b
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Filled.Backspace,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Calculator",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = display,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = if (display.length > 12) 36.sp else 52.sp),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }

        val buttonSpacing = 8.dp
        val buttonColor = MaterialTheme.colorScheme.surface
        val operatorColor = MaterialTheme.colorScheme.primary
        val textColor = MaterialTheme.colorScheme.onSurface

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
                CalcButton("AC", buttonColor, textColor, Modifier.weight(1f)) {
                    display = "0"
                    previousValue = null
                    pendingOperation = null
                    isNewInput = true
                }
                CalcButton("\u00b1", buttonColor, textColor, Modifier.weight(1f)) {
                    val current = display.toDoubleOrNull() ?: return@CalcButton
                    display = if (current == 0.0) "0" else (-current).toDisplayString()
                }
                CalcButton("%", buttonColor, textColor, Modifier.weight(1f)) {
                    val current = display.toDoubleOrNull() ?: return@CalcButton
                    display = (current / 100).toDisplayString()
                }
                CalcButton("\u00f7", operatorColor, Color.White, Modifier.weight(1f)) {
                    val current = display.toDoubleOrNull() ?: return@CalcButton
                    if (previousValue != null && pendingOperation != null && !isNewInput) {
                        display = calculate(previousValue!!, current, pendingOperation!!).toDisplayString()
                        previousValue = display.toDoubleOrNull()
                    } else {
                        previousValue = current
                    }
                    pendingOperation = "\u00f7"
                    isNewInput = true
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
                CalcButton("7", buttonColor, textColor, Modifier.weight(1f)) {
                    display = if (isNewInput) "7" else display + "7"
                    isNewInput = false
                }
                CalcButton("8", buttonColor, textColor, Modifier.weight(1f)) {
                    display = if (isNewInput) "8" else display + "8"
                    isNewInput = false
                }
                CalcButton("9", buttonColor, textColor, Modifier.weight(1f)) {
                    display = if (isNewInput) "9" else display + "9"
                    isNewInput = false
                }
                CalcButton("\u00d7", operatorColor, Color.White, Modifier.weight(1f)) {
                    val current = display.toDoubleOrNull() ?: return@CalcButton
                    if (previousValue != null && pendingOperation != null && !isNewInput) {
                        display = calculate(previousValue!!, current, pendingOperation!!).toDisplayString()
                        previousValue = display.toDoubleOrNull()
                    } else {
                        previousValue = current
                    }
                    pendingOperation = "\u00d7"
                    isNewInput = true
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
                CalcButton("4", buttonColor, textColor, Modifier.weight(1f)) {
                    display = if (isNewInput) "4" else display + "4"
                    isNewInput = false
                }
                CalcButton("5", buttonColor, textColor, Modifier.weight(1f)) {
                    display = if (isNewInput) "5" else display + "5"
                    isNewInput = false
                }
                CalcButton("6", buttonColor, textColor, Modifier.weight(1f)) {
                    display = if (isNewInput) "6" else display + "6"
                    isNewInput = false
                }
                CalcButton("-", operatorColor, Color.White, Modifier.weight(1f)) {
                    val current = display.toDoubleOrNull() ?: return@CalcButton
                    if (previousValue != null && pendingOperation != null && !isNewInput) {
                        display = calculate(previousValue!!, current, pendingOperation!!).toDisplayString()
                        previousValue = display.toDoubleOrNull()
                    } else {
                        previousValue = current
                    }
                    pendingOperation = "-"
                    isNewInput = true
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
                CalcButton("1", buttonColor, textColor, Modifier.weight(1f)) {
                    display = if (isNewInput) "1" else display + "1"
                    isNewInput = false
                }
                CalcButton("2", buttonColor, textColor, Modifier.weight(1f)) {
                    display = if (isNewInput) "2" else display + "2"
                    isNewInput = false
                }
                CalcButton("3", buttonColor, textColor, Modifier.weight(1f)) {
                    display = if (isNewInput) "3" else display + "3"
                    isNewInput = false
                }
                CalcButton("+", operatorColor, Color.White, Modifier.weight(1f)) {
                    val current = display.toDoubleOrNull() ?: return@CalcButton
                    if (previousValue != null && pendingOperation != null && !isNewInput) {
                        display = calculate(previousValue!!, current, pendingOperation!!).toDisplayString()
                        previousValue = display.toDoubleOrNull()
                    } else {
                        previousValue = current
                    }
                    pendingOperation = "+"
                    isNewInput = true
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
                CalcButton("0", buttonColor, textColor, Modifier.weight(2f)) {
                    display = if (isNewInput) "0" else display + "0"
                    isNewInput = false
                }
                CalcButton(".", buttonColor, textColor, Modifier.weight(1f)) {
                    if (isNewInput) {
                        display = "0."
                    } else if (!display.contains(".")) {
                        display = display + "."
                    }
                    isNewInput = false
                }
                CalcButton("=", operatorColor, Color.White, Modifier.weight(1f)) {
                    val current = display.toDoubleOrNull() ?: return@CalcButton
                    if (previousValue != null && pendingOperation != null) {
                        display = calculate(previousValue!!, current, pendingOperation!!).toDisplayString()
                        previousValue = null
                        pendingOperation = null
                        isNewInput = true
                    }
                }
            }
        }

        Box(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CalcButton(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor
        )
    }
}

private fun Double.toDisplayString(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        this.toString()
    }
}
