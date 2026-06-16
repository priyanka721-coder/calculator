package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalculatorLogicTest {

  @Test
  fun `addition works correctly`() {
    val viewModel = CalculatorViewModel()
    viewModel.onAction(CalculatorAction.Number(5))
    viewModel.onAction(CalculatorAction.Operation(CalculatorOperation.ADD))
    viewModel.onAction(CalculatorAction.Number(3))
    viewModel.onAction(CalculatorAction.Calculate)
    
    assertEquals("8", viewModel.state.value.display)
  }

  @Test
  fun `clear resets state`() {
    val viewModel = CalculatorViewModel()
    viewModel.onAction(CalculatorAction.Number(5))
    viewModel.onAction(CalculatorAction.Clear)
    
    assertEquals("0", viewModel.state.value.display)
    assertEquals("", viewModel.state.value.number1)
  }
}
