package com.example

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class CalculatorOperation(val symbol: String) {
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("×"),
    DIVIDE("÷")
}

data class CalculatorState(
    val number1: String = "",
    val number2: String = "",
    val operation: CalculatorOperation? = null,
    val display: String = "0",
    val history: List<String> = emptyList()
)

class CalculatorViewModel : ViewModel() {
    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> enterNumber(action.number)
            is CalculatorAction.Decimal -> enterDecimal()
            is CalculatorAction.Clear -> _state.update { CalculatorState(history = it.history) }
            is CalculatorAction.Operation -> enterOperation(action.operation)
            is CalculatorAction.Calculate -> performCalculation()
            is CalculatorAction.Delete -> delete()
            is CalculatorAction.Percentage -> percentage()
            is CalculatorAction.SignChange -> signChange()
            is CalculatorAction.ClearHistory -> _state.update { it.copy(history = emptyList()) }
        }
    }

    private fun signChange() {
        _state.update { state ->
            if (state.operation == null) {
                if (state.number1.isEmpty()) return@update state
                val newNum = if (state.number1.startsWith("-")) state.number1.substring(1) else "-${state.number1}"
                state.copy(number1 = newNum, display = newNum)
            } else {
                if (state.number2.isEmpty()) return@update state
                val newNum = if (state.number2.startsWith("-")) state.number2.substring(1) else "-${state.number2}"
                state.copy(number2 = newNum, display = newNum)
            }
        }
    }

    private fun percentage() {
        _state.update { state ->
             if (state.operation == null) {
                if (state.number1.isEmpty()) return@update state
                val result = state.number1.toDoubleOrNull()?.div(100) ?: return@update state
                val resultStr = formatResult(result)
                state.copy(number1 = resultStr, display = resultStr)
            } else {
                if (state.number2.isEmpty()) return@update state
                 val result = state.number2.toDoubleOrNull()?.div(100) ?: return@update state
                 val resultStr = formatResult(result)
                state.copy(number2 = resultStr, display = resultStr)
            }
        }
    }

    private fun delete() {
        _state.update { state ->
            if (state.operation == null) {
                if (state.number1.isEmpty()) return@update state
                val newNum = state.number1.dropLast(1)
                state.copy(number1 = newNum, display = if (newNum.isEmpty()) "0" else newNum)
            } else {
                if (state.number2.isEmpty()) {
                    return@update state.copy(operation = null, display = state.number1)
                }
                val newNum = state.number2.dropLast(1)
                state.copy(number2 = newNum, display = if (newNum.isEmpty()) state.operation?.symbol ?: "" else newNum)
            }
        }
    }

    private fun performCalculation() {
        _state.update { state ->
            val number1 = state.number1.toDoubleOrNull()
            val number2 = state.number2.toDoubleOrNull()
            if (number1 != null && number2 != null) {
                val result = when (state.operation) {
                    CalculatorOperation.ADD -> number1 + number2
                    CalculatorOperation.SUBTRACT -> number1 - number2
                    CalculatorOperation.MULTIPLY -> number1 * number2
                    CalculatorOperation.DIVIDE -> if (number2 != 0.0) number1 / number2 else null
                    null -> return@update state
                }
                if (result == null) {
                    state.copy(display = "Error", number1 = "", number2 = "", operation = null)
                } else {
                    val resultStr = formatResult(result)
                    val expression = "${formatResult(number1)} ${state.operation.symbol} ${formatResult(number2)}"
                    val newHistory = (listOf("$expression = $resultStr") + state.history).take(5)
                    CalculatorState(number1 = resultStr, display = resultStr, history = newHistory)
                }
            } else state
        }
    }

    private fun formatResult(result: Double): String {
        return if (result % 1 == 0.0) {
            result.toLong().toString()
        } else {
            result.toString()
        }
    }

    private fun enterOperation(operation: CalculatorOperation) {
        _state.update { state ->
            if (state.number1.isNotEmpty()) {
                if (state.number2.isNotEmpty()) {
                    val number1 = state.number1.toDoubleOrNull() ?: 0.0
                    val number2 = state.number2.toDoubleOrNull() ?: 0.0
                    val result = when (state.operation) {
                        CalculatorOperation.ADD -> number1 + number2
                        CalculatorOperation.SUBTRACT -> number1 - number2
                        CalculatorOperation.MULTIPLY -> number1 * number2
                        CalculatorOperation.DIVIDE -> if (number2 != 0.0) number1 / number2 else null
                        null -> number1
                    }
                    if (result == null) {
                        state.copy(display = "Error", number1 = "", number2 = "", operation = null)
                    } else {
                        val resultStr = formatResult(result)
                        val expression = "${formatResult(number1)} ${state.operation?.symbol ?: ""} ${formatResult(number2)}"
                        val newHistory = (listOf("$expression = $resultStr") + state.history).take(5)
                        state.copy(number1 = resultStr, display = resultStr, operation = operation, number2 = "", history = newHistory)
                    }
                } else {
                    state.copy(operation = operation)
                }
            } else state
        }
    }

    private fun enterDecimal() {
        _state.update { state ->
            if (state.operation == null) {
                if (!state.number1.contains(".") && state.number1.isNotBlank()) {
                    val newNum = state.number1 + "."
                    state.copy(number1 = newNum, display = newNum)
                } else if (state.number1.isEmpty()) {
                    state.copy(number1 = "0.", display = "0.")
                } else state
            } else {
                if (!state.number2.contains(".") && state.number2.isNotBlank()) {
                    val newNum = state.number2 + "."
                    state.copy(number2 = newNum, display = newNum)
                } else if (state.number2.isEmpty()) {
                    state.copy(number2 = "0.", display = "0.")
                } else state
            }
        }
    }

    private fun enterNumber(number: Int) {
        _state.update { state ->
            if (state.operation == null) {
                if (state.number1.length >= 8) return@update state
                val newNum = if (state.number1 == "0") number.toString() else state.number1 + number
                state.copy(number1 = newNum, display = newNum)
            } else {
                if (state.number2.length >= 8) return@update state
                val newNum = if (state.number2 == "0") number.toString() else state.number2 + number
                state.copy(number2 = newNum, display = newNum)
            }
        }
    }
}

sealed class CalculatorAction {
    data class Number(val number: Int) : CalculatorAction()
    object Clear : CalculatorAction()
    object Delete : CalculatorAction()
    object Decimal : CalculatorAction()
    object Calculate : CalculatorAction()
    data class Operation(val operation: CalculatorOperation) : CalculatorAction()
    object Percentage : CalculatorAction()
    object SignChange : CalculatorAction()
    object ClearHistory : CalculatorAction()
}
