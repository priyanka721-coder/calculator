package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.platform.LocalContext
import android.app.WallpaperManager
import android.graphics.BitmapFactory
import android.widget.Toast
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Pets
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel = viewModel<CalculatorViewModel>()
                val state by viewModel.state.collectAsState()
                val focusRequester = remember { FocusRequester() }
                
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester)
                        .focusTarget()
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false
                            when (keyEvent.key) {
                                Key.Zero, Key.NumPad0 -> viewModel.onAction(CalculatorAction.Number(0))
                                Key.One, Key.NumPad1 -> viewModel.onAction(CalculatorAction.Number(1))
                                Key.Two, Key.NumPad2 -> viewModel.onAction(CalculatorAction.Number(2))
                                Key.Three, Key.NumPad3 -> viewModel.onAction(CalculatorAction.Number(3))
                                Key.Four, Key.NumPad4 -> viewModel.onAction(CalculatorAction.Number(4))
                                Key.Five, Key.NumPad5 -> viewModel.onAction(CalculatorAction.Number(5))
                                Key.Six, Key.NumPad6 -> viewModel.onAction(CalculatorAction.Number(6))
                                Key.Seven, Key.NumPad7 -> viewModel.onAction(CalculatorAction.Number(7))
                                Key.Eight, Key.NumPad8 -> viewModel.onAction(CalculatorAction.Number(8))
                                Key.Nine, Key.NumPad9 -> viewModel.onAction(CalculatorAction.Number(9))
                                Key.Plus, Key.NumPadAdd -> viewModel.onAction(CalculatorAction.Operation(CalculatorOperation.ADD))
                                Key.Minus, Key.NumPadSubtract -> viewModel.onAction(CalculatorAction.Operation(CalculatorOperation.SUBTRACT))
                                Key.NumPadMultiply -> viewModel.onAction(CalculatorAction.Operation(CalculatorOperation.MULTIPLY))
                                Key.Slash, Key.NumPadDivide -> viewModel.onAction(CalculatorAction.Operation(CalculatorOperation.DIVIDE))
                                Key.Equals, Key.Enter, Key.NumPadEnter -> viewModel.onAction(CalculatorAction.Calculate)
                                Key.Backspace -> viewModel.onAction(CalculatorAction.Delete)
                                Key.Escape -> viewModel.onAction(CalculatorAction.Clear)
                                Key.Period, Key.NumPadDot -> viewModel.onAction(CalculatorAction.Decimal)
                                else -> return@onKeyEvent false
                            }
                            true
                        },
                    containerColor = MaterialTheme.colorScheme.background
                ) { padding ->
                    CalculatorScreen(
                        state = state,
                        onAction = viewModel::onAction,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen(
    state: CalculatorState,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val time = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF1A1A1A),
                modifier = Modifier.width(300.dp).fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    if (state.history.isEmpty()) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No calculations yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            state.history.forEach { entry ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = entry,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(
                        onClick = { onAction(CalculatorAction.ClearHistory) },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF2B8B5))
                    ) {
                        Text("Clear History")
                    }
                }
            }
        }
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            // Dark Solid Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0D0D0D))
            )

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Display Area (Top)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    // Operation Preview
                    if (state.operation != null || state.number2.isNotEmpty()) {
                        Text(
                            text = "${state.number1} ${state.operation?.symbol ?: ""} ${state.number2}",
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            fontWeight = FontWeight.Normal,
                            fontSize = 24.sp,
                            color = Color(0xFFD0BCFF).copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }

                    // Main Display (Result/Current Input)
                    Text(
                        text = state.display,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calculator_display"),
                        fontWeight = FontWeight.Light,
                        fontSize = 80.sp,
                        color = Color.White,
                        maxLines = 1
                    )
                }

                // Keypad Container (Dark Frosted Glass)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                        ),
                    color = Color.White.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Indicator
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = CircleShape
                                )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        CalculatorButtons(onAction = onAction, spacing = 12.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButtons(
    onAction: (CalculatorAction) -> Unit,
    spacing: Dp
) {
    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            CalculatorButton(
                symbol = "AC",
                color = Color.White.copy(alpha = 0.08f),
                textColor = Color(0xFFF2B8B5),
                shape = RoundedCornerShape(24.dp),
                textTag = "clear_button",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CalculatorAction.Clear) }
            )
            CalculatorButton(
                symbol = "%",
                color = Color.White.copy(alpha = 0.08f),
                textColor = Color(0xFFD0BCFF),
                shape = RoundedCornerShape(24.dp),
                textTag = "percentage_button",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CalculatorAction.Percentage) }
            )
            CalculatorButton(
                symbol = "⌫",
                color = Color.White.copy(alpha = 0.08f),
                textColor = Color(0xFFD0BCFF),
                shape = RoundedCornerShape(24.dp),
                textTag = "delete_button",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CalculatorAction.Delete) }
            )
            CalculatorButton(
                symbol = "÷",
                color = Color.White.copy(alpha = 0.15f),
                textColor = Color(0xFFEADDFF),
                shape = RoundedCornerShape(24.dp),
                textTag = "divide_button",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CalculatorAction.Operation(CalculatorOperation.DIVIDE)) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            CalculatorButton(
                symbol = "7",
                color = Color.White.copy(alpha = 0.04f),
                shape = CircleShape,
                textTag = "number_7",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CalculatorAction.Number(7)) }
            )
            CalculatorButton(
                symbol = "8",
                color = Color.White.copy(alpha = 0.04f),
                shape = CircleShape,
                textTag = "number_8",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CalculatorAction.Number(8)) }
            )
            CalculatorButton(
                symbol = "9",
                color = Color.White.copy(alpha = 0.04f),
                shape = CircleShape,
                textTag = "number_9",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CalculatorAction.Number(9)) }
            )
            CalculatorButton(
                symbol = "×",
                color = Color.White.copy(alpha = 0.15f),
                textColor = Color(0xFFEADDFF),
                shape = RoundedCornerShape(24.dp),
                textTag = "multiply_button",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CalculatorAction.Operation(CalculatorOperation.MULTIPLY)) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            CalculatorButton(
                symbol = "4",
                color = Color.White.copy(alpha = 0.04f),
                shape = CircleShape,
                textTag = "number_4",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CalculatorAction.Number(4)) }
            )
            CalculatorButton(
                symbol = "5",
                color = Color.White.copy(alpha = 0.04f),
                shape = CircleShape,
                textTag = "number_5",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CalculatorAction.Number(5)) }
            )
            CalculatorButton(
                symbol = "6",
                color = Color.White.copy(alpha = 0.04f),
                shape = CircleShape,
                textTag = "number_6",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CalculatorAction.Number(6)) }
            )
            CalculatorButton(
                symbol = "−",
                color = Color.White.copy(alpha = 0.15f),
                textColor = Color(0xFFEADDFF),
                shape = RoundedCornerShape(24.dp),
                textTag = "subtract_button",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CalculatorAction.Operation(CalculatorOperation.SUBTRACT)) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            CalculatorButton(
                symbol = "1",
                color = Color.White.copy(alpha = 0.04f),
                shape = CircleShape,
                textTag = "number_1",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CalculatorAction.Number(1)) }
            )
            CalculatorButton(
                symbol = "2",
                color = Color.White.copy(alpha = 0.04f),
                shape = CircleShape,
                textTag = "number_2",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CalculatorAction.Number(2)) }
            )
            CalculatorButton(
                symbol = "3",
                color = Color.White.copy(alpha = 0.04f),
                shape = CircleShape,
                textTag = "number_3",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CalculatorAction.Number(3)) }
            )
            CalculatorButton(
                symbol = "+",
                color = Color.White.copy(alpha = 0.15f),
                textColor = Color(0xFFEADDFF),
                shape = RoundedCornerShape(24.dp),
                textTag = "add_button",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CalculatorAction.Operation(CalculatorOperation.ADD)) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            CalculatorButton(
                symbol = "0",
                color = Color.White.copy(alpha = 0.04f),
                shape = CircleShape,
                textTag = "number_0",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CalculatorAction.Number(0)) }
            )
            CalculatorButton(
                symbol = ".",
                color = Color.White.copy(alpha = 0.04f),
                shape = CircleShape,
                textTag = "decimal_button",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CalculatorAction.Decimal) }
            )
            CalculatorButton(
                symbol = "=",
                color = Color(0xFFD0BCFF).copy(alpha = 0.6f),
                textColor = Color(0xFFEADDFF),
                shape = RoundedCornerShape(24.dp),
                textTag = "calculate_button",
                modifier = Modifier.weight(2f),
                onClick = { onAction(CalculatorAction.Calculate) }
            )
        }
    }
}

@Composable
fun CalculatorButton(
    symbol: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = Color.Unspecified,
    fontSize: androidx.compose.ui.unit.TextUnit = 26.sp,
    shape: androidx.compose.ui.graphics.Shape = CircleShape,
    textTag: String = "",
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        label = "button_scale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .height(72.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .testTag(textTag),
        shape = shape,
        color = color
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                color = if (textColor == Color.Unspecified) contentColorFor(color) else textColor
            )
        }
    }
}
