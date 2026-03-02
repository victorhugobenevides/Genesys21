package com.genesys.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.genesys.ui.theme.Dimensions
import kotlinx.coroutines.delay

sealed class ButtonState {
    object Normal : ButtonState()
    object Loading : ButtonState()
    data class Success(val message: String? = null) : ButtonState()
    data class Error(val message: String? = null) : ButtonState()
    object Disabled : ButtonState()
}

@Composable
fun StateButton(
    text: String,
    state: ButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    autoResetSuccessDelay: Long = 2000L
) {
    val enabled = state == ButtonState.Normal
    
    LaunchedEffect(state) {
        if (state is ButtonState.Success) {
            delay(autoResetSuccessDelay)
        }
    }
    
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = when (state) {
            is ButtonState.Success -> MaterialTheme.colorScheme.tertiary
            is ButtonState.Error -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.primary
        },
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
    )
    
    val contentDescriptionText = when (state) {
        is ButtonState.Normal -> "$text button"
        is ButtonState.Loading -> "Loading, please wait"
        is ButtonState.Success -> state.message ?: "Success"
        is ButtonState.Error -> state.message ?: "Error"
        is ButtonState.Disabled -> "$text button, disabled"
    }
    
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(Dimensions.button_height)
            .semantics { contentDescription = contentDescriptionText },
        colors = buttonColors
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) with
                    fadeOut(animationSpec = tween(300))
            }
        ) { targetState ->
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (targetState) {
                    is ButtonState.Normal, is ButtonState.Disabled -> {
                        Text(text = text)
                    }
                    is ButtonState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(Dimensions.spacing_sm))
                        Text(text = "Loading...")
                    }
                    is ButtonState.Success -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimensions.spacing_sm))
                        Text(text = targetState.message ?: "Success!")
                    }
                    is ButtonState.Error -> {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Error",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimensions.spacing_sm))
                        Text(text = targetState.message ?: "Error")
                    }
                }
            }
        }
    }
}