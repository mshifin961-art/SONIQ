package com.soniq.music.ui.feature.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.soniq.music.core.config.AppConfig
import com.soniq.music.core.state.UiState
import com.soniq.music.domain.model.AtmosphereAccent
import com.soniq.music.domain.model.AuthSessionState
import com.soniq.music.domain.model.UserProfile
import com.soniq.music.network.SupabaseEnvironmentStatus
import com.soniq.music.ui.components.SoniqBrandMark
import com.soniq.music.ui.components.SoniqErrorState
import com.soniq.music.ui.components.SoniqGlassCard
import com.soniq.music.ui.components.SoniqLoadingView
import com.soniq.music.ui.theme.LocalDynamicAtmosphere
import com.soniq.music.ui.theme.LocalSoniqSpacing
import com.soniq.music.ui.theme.SoniqColors

@Composable
fun ProfileScreen(
    activeAccent: AtmosphereAccent,
    onSelectAccent: (AtmosphereAccent) -> Unit,
    onOpenAuth: () -> Unit,
    appConfig: AppConfig,
    supabaseStatus: SupabaseEnvironmentStatus,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val spacing = LocalSoniqSpacing.current
    val atmosphere = LocalDynamicAtmosphere.current
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val editState by viewModel.editState.collectAsStateWithLifecycle()
    val emailChangeState by viewModel.emailChangeState.collectAsStateWithLifecycle()
    val isSigningOut by viewModel.isSigningOut.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = spacing.lg,
            bottom = spacing.bottomBarClearance
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.lg)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SoniqColors.TextPrimary
                )
                Text(
                    text = "Account, profile identity, appearance, and environment status.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoniqColors.TextSecondary
                )
            }
        }

        // Account & User Profile Section (Phase 2)
        item {
            when (val session = sessionState) {
                is AuthSessionState.Initializing -> {
                    SoniqGlassCard(elevated = true) {
                        SoniqLoadingView(label = "Checking your SONIQ session...")
                    }
                }

                is AuthSessionState.Authenticated -> {
                    when (val state = profileState) {
                        is UiState.Loading, UiState.Idle -> {
                            SoniqGlassCard(elevated = true) {
                                SoniqLoadingView(label = "Loading your profile...")
                            }
                        }

                        is UiState.Error -> {
                            SoniqErrorState(
                                title = "Couldn't load profile",
                                message = state.error.message,
                                onRetry = viewModel::refreshProfile
                            )
                        }

                        is UiState.Success -> {
                            AuthenticatedProfileCard(
                                profile = state.data,
                                editState = editState,
                                emailChangeState = emailChangeState,
                                isSigningOut = isSigningOut,
                                onStartEditing = viewModel::startEditing,
                                onCancelEditing = viewModel::cancelEditing,
                                onDisplayNameChanged = viewModel::onDisplayNameChanged,
                                onUsernameChanged = viewModel::onUsernameChanged,
                                onBioChanged = viewModel::onBioChanged,
                                onAvatarUrlChanged = viewModel::onAvatarUrlChanged,
                                onSaveProfile = viewModel::saveProfile,
                                onToggleEmailChange = viewModel::toggleEmailChange,
                                onNewEmailChanged = viewModel::onNewEmailChanged,
                                onSubmitEmailChange = viewModel::submitEmailChange,
                                onSignOut = viewModel::signOut
                            )
                        }

                        is UiState.Empty -> {
                            SoniqGlassCard(elevated = true) {
                                Text(
                                    text = "Signed in as ${session.user.email}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = SoniqColors.TextPrimary
                                )
                            }
                        }
                    }
                }

                is AuthSessionState.Guest,
                is AuthSessionState.Unauthenticated,
                is AuthSessionState.Unconfigured -> {
                    SignedOutAccountCard(
                        sessionState = session,
                        onOpenAuth = onOpenAuth
                    )
                }
            }
        }

        // Dynamic Atmosphere Accent Selector (Preserved from Phase 1)
        item {
            SoniqGlassCard(elevated = true) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = null,
                            tint = atmosphere.primaryAccent
                        )
                        Text(
                            text = "Dynamic Atmosphere Accent",
                            style = MaterialTheme.typography.titleMedium,
                            color = SoniqColors.TextPrimary
                        )
                    }
                    Text(
                        text = "Preview SONIQ's live dark glassmorphism accent transitions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoniqColors.TextSecondary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AtmosphereAccent.entries.forEach { preset ->
                            val isSelected = preset == activeAccent
                            SoniqGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                borderColor = if (isSelected) {
                                    Color(preset.primaryHex).copy(alpha = 0.85f)
                                } else {
                                    SoniqColors.GlassBorderSubtle
                                },
                                contentPadding = PaddingValues(14.dp),
                                onClick = { onSelectAccent(preset) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(Color(preset.primaryHex))
                                        )
                                        Column {
                                            Text(
                                                text = preset.displayName,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = SoniqColors.TextPrimary
                                            )
                                            Text(
                                                text = preset.subtitle,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = SoniqColors.TextSecondary
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = Color(preset.primaryHex)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Backend Environment Status (Preserved from Phase 1)
        item {
            SoniqGlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = atmosphere.secondaryAccent
                        )
                        Text(
                            text = "Backend Environment",
                            style = MaterialTheme.typography.titleMedium,
                            color = SoniqColors.TextPrimary
                        )
                    }
                    Text(
                        text = supabaseStatus.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (supabaseStatus == SupabaseEnvironmentStatus.Configured) {
                            SoniqColors.Success
                        } else {
                            SoniqColors.Warning
                        }
                    )
                    Text(
                        text = supabaseStatus.detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoniqColors.TextSecondary
                    )
                }
            }
        }

        // SONIQ Build Info (Preserved from Phase 1)
        item {
            SoniqGlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SoniqBrandMark(showTagline = true)
                    Text(
                        text = "${appConfig.phaseLabel} · v${appConfig.versionName} (${appConfig.versionCode})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoniqColors.TextSecondary
                    )
                    Text(
                        text = "Package: ${appConfig.applicationId}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SoniqColors.TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun SignedOutAccountCard(
    sessionState: AuthSessionState,
    onOpenAuth: () -> Unit
) {
    val atmosphere = LocalDynamicAtmosphere.current
    SoniqGlassCard(
        elevated = true,
        borderColor = atmosphere.primaryAccent.copy(alpha = 0.45f),
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(atmosphere.primaryAccent.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = atmosphere.primaryAccent
                    )
                }
                Column {
                    Text(
                        text = when (sessionState) {
                            AuthSessionState.Guest -> "Listening as Guest"
                            AuthSessionState.Unconfigured -> "Local Preview Mode"
                            else -> "Sign in to SONIQ"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = SoniqColors.TextPrimary
                    )
                    Text(
                        text = "Sync your profile, playlists, favorites, and listening history across devices.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoniqColors.TextSecondary
                    )
                }
            }

            Button(
                onClick = onOpenAuth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = atmosphere.primaryAccent,
                    contentColor = SoniqColors.TextPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Login,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "  Sign In or Create Account",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun AuthenticatedProfileCard(
    profile: UserProfile,
    editState: ProfileEditState,
    emailChangeState: EmailChangeState,
    isSigningOut: Boolean,
    onStartEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onBioChanged: (String) -> Unit,
    onAvatarUrlChanged: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onToggleEmailChange: (Boolean) -> Unit,
    onNewEmailChanged: (String) -> Unit,
    onSubmitEmailChange: () -> Unit,
    onSignOut: () -> Unit
) {
    val atmosphere = LocalDynamicAtmosphere.current
    SoniqGlassCard(
        elevated = true,
        borderColor = atmosphere.primaryAccent.copy(alpha = 0.45f),
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(atmosphere.primaryAccent.copy(alpha = 0.28f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.initials,
                        style = MaterialTheme.typography.titleLarge,
                        color = SoniqColors.TextPrimary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.effectiveDisplayName,
                        style = MaterialTheme.typography.titleLarge,
                        color = SoniqColors.TextPrimary
                    )
                    if (profile.username.isNotBlank()) {
                        Text(
                            text = "@${profile.username}",
                            style = MaterialTheme.typography.labelLarge,
                            color = atmosphere.secondaryAccent
                        )
                    }
                    Text(
                        text = profile.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoniqColors.TextSecondary
                    )
                }
            }

            if (profile.bio.isNotBlank() && !editState.isEditing) {
                Text(
                    text = profile.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoniqColors.TextPrimary
                )
            }

            if (editState.statusMessage != null && !editState.isEditing) {
                Text(
                    text = editState.statusMessage,
                    style = MaterialTheme.typography.labelLarge,
                    color = SoniqColors.Success
                )
            }

            if (emailChangeState.statusMessage != null) {
                Text(
                    text = emailChangeState.statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoniqColors.Success
                )
            }

            AnimatedVisibility(visible = editState.isEditing) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProfileInputField(
                        value = editState.displayName,
                        onValueChange = onDisplayNameChanged,
                        label = "Display name",
                        error = editState.displayNameError,
                        enabled = !editState.isSaving
                    )
                    ProfileInputField(
                        value = editState.username,
                        onValueChange = onUsernameChanged,
                        label = "Username (optional, case-insensitive)",
                        error = editState.usernameError,
                        enabled = !editState.isSaving
                    )
                    ProfileInputField(
                        value = editState.bio,
                        onValueChange = onBioChanged,
                        label = "Bio (optional)",
                        error = editState.bioError,
                        enabled = !editState.isSaving,
                        singleLine = false
                    )
                    ProfileInputField(
                        value = editState.avatarUrl,
                        onValueChange = onAvatarUrlChanged,
                        label = "Avatar HTTPS URL (optional)",
                        error = editState.avatarUrlError,
                        enabled = !editState.isSaving
                    )

                    if (editState.errorMessage != null) {
                        Text(
                            text = editState.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoniqColors.Error
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancelEditing,
                            enabled = !editState.isSaving,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Cancel", color = SoniqColors.TextSecondary)
                        }
                        Button(
                            onClick = onSaveProfile,
                            enabled = !editState.isSaving,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = atmosphere.primaryAccent,
                                contentColor = SoniqColors.TextPrimary
                            )
                        ) {
                            if (editState.isSaving) {
                                CircularProgressIndicator(
                                    color = SoniqColors.TextPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text("Save")
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = emailChangeState.isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Change Account Email (via Supabase Auth)",
                        style = MaterialTheme.typography.titleMedium,
                        color = SoniqColors.TextPrimary
                    )
                    ProfileInputField(
                        value = emailChangeState.newEmail,
                        onValueChange = onNewEmailChanged,
                        label = "New email address",
                        error = emailChangeState.emailError,
                        enabled = !emailChangeState.isSubmitting
                    )
                    if (emailChangeState.errorMessage != null) {
                        Text(
                            text = emailChangeState.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoniqColors.Error
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onToggleEmailChange(false) },
                            enabled = !emailChangeState.isSubmitting,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Cancel", color = SoniqColors.TextSecondary)
                        }
                        Button(
                            onClick = onSubmitEmailChange,
                            enabled = !emailChangeState.isSubmitting,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = atmosphere.secondaryAccent,
                                contentColor = SoniqColors.TextPrimary
                            )
                        ) {
                            if (emailChangeState.isSubmitting) {
                                CircularProgressIndicator(
                                    color = SoniqColors.TextPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text("Update Email")
                            }
                        }
                    }
                }
            }

            if (!editState.isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onStartEditing,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = atmosphere.primaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("  Edit Profile", color = SoniqColors.TextPrimary)
                    }
                    OutlinedButton(
                        onClick = onSignOut,
                        enabled = !isSigningOut,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = null,
                            tint = SoniqColors.Error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isSigningOut) "  Signing Out..." else "  Sign Out",
                            color = SoniqColors.Error
                        )
                    }
                }

                if (!emailChangeState.isExpanded) {
                    TextButton(
                        onClick = { onToggleEmailChange(true) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Email,
                            contentDescription = null,
                            tint = atmosphere.secondaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "  Change account email",
                            style = MaterialTheme.typography.labelLarge,
                            color = atmosphere.secondaryAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    enabled: Boolean,
    singleLine: Boolean = true
) {
    val atmosphere = LocalDynamicAtmosphere.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        enabled = enabled,
        isError = error != null,
        label = { Text(label) },
        supportingText = error?.let { msg -> { Text(msg, color = SoniqColors.Error) } },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SoniqColors.DeepNavy.copy(alpha = 0.85f),
            unfocusedContainerColor = SoniqColors.MidnightBlack.copy(alpha = 0.65f),
            focusedBorderColor = atmosphere.primaryAccent,
            unfocusedBorderColor = SoniqColors.GlassBorderSubtle,
            errorBorderColor = SoniqColors.Error,
            focusedTextColor = SoniqColors.TextPrimary,
            unfocusedTextColor = SoniqColors.TextPrimary,
            focusedLabelColor = atmosphere.primaryAccent,
            unfocusedLabelColor = SoniqColors.TextSecondary,
            cursorColor = atmosphere.primaryAccent
        )
    )
}
