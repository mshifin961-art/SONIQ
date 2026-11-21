package com.soniq.music.ui.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.soniq.music.domain.model.AuthSessionState
import com.soniq.music.ui.components.SoniqBrandMark
import com.soniq.music.ui.components.SoniqGlassCard
import com.soniq.music.ui.components.SoniqLoadingView
import com.soniq.music.ui.components.SoniqPillChip
import com.soniq.music.ui.theme.LocalDynamicAtmosphere
import com.soniq.music.ui.theme.LocalSoniqSpacing
import com.soniq.music.ui.theme.SoniqColors

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val spacing = LocalSoniqSpacing.current
    val atmosphere = LocalDynamicAtmosphere.current
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(sessionState) {
        if (sessionState is AuthSessionState.Authenticated) {
            onAuthenticated()
        }
    }

    if (sessionState is AuthSessionState.Initializing) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            SoniqLoadingView(label = "Restoring your SONIQ session...")
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentPadding = PaddingValues(
            horizontal = spacing.screenHorizontal,
            vertical = spacing.lg
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.lg)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = SoniqColors.TextPrimary
                    )
                }
                SoniqBrandMark(showTagline = false)
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Text(
                    text = if (uiState.mode == AuthMode.SignIn) {
                        "Welcome back"
                    } else {
                        "Create your SONIQ account"
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    color = SoniqColors.TextPrimary
                )
                Text(
                    text = if (uiState.mode == AuthMode.SignIn) {
                        "Sign in to sync your profile, playlists, and listening preferences."
                    } else {
                        "Set up your profile to synchronize your library across devices."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoniqColors.TextSecondary
                )
            }
        }

        if (!uiState.isSupabaseConfigured) {
            item {
                SoniqGlassCard(
                    borderColor = SoniqColors.Warning.copy(alpha = 0.55f),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = SoniqColors.Warning
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Supabase Environment Not Configured",
                                style = MaterialTheme.typography.titleMedium,
                                color = SoniqColors.TextPrimary
                            )
                            Text(
                                text = "Add SUPABASE_URL and SUPABASE_ANON_KEY to local.properties or CI environment variables to enable live authentication. You can continue as a guest in the meantime.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoniqColors.TextSecondary
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                SoniqPillChip(
                    label = "Sign In",
                    selected = uiState.mode == AuthMode.SignIn,
                    onClick = { viewModel.setMode(AuthMode.SignIn) }
                )
                SoniqPillChip(
                    label = "Create Account",
                    selected = uiState.mode == AuthMode.SignUp,
                    onClick = { viewModel.setMode(AuthMode.SignUp) }
                )
            }
        }

        item {
            SoniqGlassCard(
                elevated = true,
                contentPadding = PaddingValues(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                    AnimatedVisibility(visible = uiState.mode == AuthMode.SignUp) {
                        OutlinedTextField(
                            value = uiState.displayName,
                            onValueChange = viewModel::onDisplayNameChanged,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isSubmitting,
                            isError = uiState.displayNameError != null,
                            label = { Text("Display name") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = atmosphere.primaryAccent
                                )
                            },
                            supportingText = uiState.displayNameError?.let { error ->
                                { Text(error, color = SoniqColors.Error) }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(16.dp),
                            colors = soniqAuthTextFieldColors()
                        )
                    }

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChanged,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isSubmitting,
                        isError = uiState.emailError != null,
                        label = { Text("Email address") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Email,
                                contentDescription = null,
                                tint = atmosphere.primaryAccent
                            )
                        },
                        supportingText = uiState.emailError?.let { error ->
                            { Text(error, color = SoniqColors.Error) }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = soniqAuthTextFieldColors()
                    )

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChanged,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isSubmitting,
                        isError = uiState.passwordError != null,
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = atmosphere.primaryAccent
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) {
                                        Icons.Rounded.VisibilityOff
                                    } else {
                                        Icons.Rounded.Visibility
                                    },
                                    contentDescription = if (passwordVisible) {
                                        "Hide password"
                                    } else {
                                        "Show password"
                                    },
                                    tint = SoniqColors.TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        supportingText = {
                            val message = uiState.passwordError ?: if (uiState.mode == AuthMode.SignUp) {
                                "At least 8 characters with letters and numbers"
                            } else {
                                null
                            }
                            if (message != null) {
                                Text(
                                    text = message,
                                    color = if (uiState.passwordError != null) {
                                        SoniqColors.Error
                                    } else {
                                        SoniqColors.TextMuted
                                    }
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = soniqAuthTextFieldColors()
                    )

                    if (uiState.generalError != null) {
                        SoniqGlassCard(
                            borderColor = SoniqColors.Error.copy(alpha = 0.55f),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Text(
                                text = uiState.generalError.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoniqColors.Error
                            )
                        }
                    }

                    if (uiState.confirmationEmailSentTo != null) {
                        SoniqGlassCard(
                            borderColor = SoniqColors.Success.copy(alpha = 0.55f),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = SoniqColors.Success
                                )
                                Text(
                                    text = "Verification link sent to ${uiState.confirmationEmailSentTo}. Confirm your inbox, then sign in.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SoniqColors.TextPrimary
                                )
                            }
                        }
                    }

                    Button(
                        onClick = viewModel::submit,
                        enabled = !uiState.isSubmitting && uiState.isSupabaseConfigured,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = atmosphere.primaryAccent,
                            contentColor = SoniqColors.TextPrimary,
                            disabledContainerColor = SoniqColors.ElevatedNavy,
                            disabledContentColor = SoniqColors.TextMuted
                        )
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                color = SoniqColors.TextPrimary,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = if (uiState.mode == AuthMode.SignIn) "Sign In" else "Create Account",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    viewModel.continueAsGuest()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Continue without an account",
                    style = MaterialTheme.typography.labelLarge,
                    color = SoniqColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun soniqAuthTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = SoniqColors.DeepNavy.copy(alpha = 0.85f),
    unfocusedContainerColor = SoniqColors.MidnightBlack.copy(alpha = 0.65f),
    focusedBorderColor = LocalDynamicAtmosphere.current.primaryAccent,
    unfocusedBorderColor = SoniqColors.GlassBorderSubtle,
    errorBorderColor = SoniqColors.Error,
    focusedTextColor = SoniqColors.TextPrimary,
    unfocusedTextColor = SoniqColors.TextPrimary,
    focusedLabelColor = LocalDynamicAtmosphere.current.primaryAccent,
    unfocusedLabelColor = SoniqColors.TextSecondary,
    cursorColor = LocalDynamicAtmosphere.current.primaryAccent
)
