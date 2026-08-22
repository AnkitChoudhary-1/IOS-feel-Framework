package dev.iosfeel.components.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.core.tokens.IOSMotionTokens
import dev.iosfeel.core.tokens.IOSShapes
import dev.iosfeel.core.tokens.IOSSpacing

@Composable
fun IOSSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    onSearch: ((String) -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    containerColor: Color = Color(0xFF1C1C1E).copy(alpha = 0.6f),
    textColor: Color = Color.White,
    placeholderColor: Color = Color.White.copy(alpha = 0.5f),
    cancelTextColor: Color = Color(0xFF007AFF)
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    val focusProgress = remember { Animatable(0f) }
    LaunchedEffect(isFocused) {
        focusProgress.animateTo(
            targetValue = if (isFocused) 1f else 0f,
            animationSpec = spring(
                stiffness = IOSMotionTokens.PressStiffness,
                dampingRatio = IOSMotionTokens.PressDampingRatio
            )
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = IOSSpacing.Large, vertical = IOSSpacing.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search Input Container
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(IOSShapes.Control)
                .background(containerColor)
                .padding(horizontal = IOSSpacing.Medium),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔍",
                    fontSize = 13.sp,
                    color = placeholderColor
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 15.sp,
                            color = placeholderColor
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused
                            },
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = textColor
                        ),
                        cursorBrush = SolidColor(Color(0xFF007AFF)),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                onSearch?.invoke(value)
                                focusManager.clearFocus()
                            }
                        )
                    )
                }

                // Clear Button
                if (value.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onValueChange("")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            fontSize = 9.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Animated "Cancel" Button
        AnimatedVisibility(
            visible = isFocused || value.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row {
                Spacer(modifier = Modifier.width(IOSSpacing.Medium))
                Text(
                    text = "Cancel",
                    fontSize = 15.sp,
                    color = cancelTextColor,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onValueChange("")
                            focusManager.clearFocus()
                            onCancel?.invoke()
                        }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}
