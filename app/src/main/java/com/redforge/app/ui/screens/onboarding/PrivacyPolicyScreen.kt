package com.redforge.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.redforge.app.R
import com.redforge.app.ui.components.ForgeButton
import com.redforge.app.ui.theme.ForgeGold

/**
 * Mandatory first-run gate: the user must actively acknowledge that
 * RedForge stores everything locally and that the developer carries no
 * responsibility for leaks caused by the device itself being compromised,
 * since the app never transmits data anywhere. Cannot be skipped.
 */
@Composable
fun PrivacyPolicyScreen(onAccept: () -> Unit) {
    val scrollState = rememberScrollState()
    var hasScrolledToEnd by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = ForgeGold, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.privacy_policy_title), style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                Text(
                    stringResource(R.string.privacy_policy_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        androidx.compose.runtime.LaunchedEffect(scrollState.value, scrollState.maxValue) {
            if (scrollState.maxValue == 0 || scrollState.value >= scrollState.maxValue - 24) {
                hasScrolledToEnd = true
            }
        }

        Spacer(Modifier.height(20.dp))
        ForgeButton(
            text = stringResource(R.string.privacy_accept),
            onClick = onAccept,
            enabled = hasScrolledToEnd,
            modifier = Modifier.fillMaxWidth()
        )
        if (!hasScrolledToEnd) {
            Text(
                "Scroll to read the full policy to continue",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
