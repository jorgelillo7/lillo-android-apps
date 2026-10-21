package com.jorgelillo.christmascountdown.ui.about

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.jorgelillo.christmascountdown.BuildConfig
import com.jorgelillo.christmascountdown.R
import com.jorgelillo.christmascountdown.ui.theme.ChristmasColors
import com.jorgelillo.core.designsystem.AboutTexts
import com.jorgelillo.core.designsystem.LilloAboutSheet
import com.jorgelillo.core.platform.LilloLinks

val PRIVACY_POLICY_URL = LilloLinks.privacyPolicy("christmas-countdown")
val PLAY_STORE_URL = LilloLinks.playStore(BuildConfig.APPLICATION_ID)

@Composable
internal fun AboutSheet(onDismiss: () -> Unit) = LilloAboutSheet(
    icon = painterResource(R.drawable.ic_launcher_foreground),
    iconBackground = ChristmasColors.Berry,
    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    texts = AboutTexts(
        title = stringResource(R.string.about_title),
        version = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
        body = stringResource(R.string.about_body),
        rate = stringResource(R.string.about_rate),
        privacy = stringResource(R.string.about_privacy)
    ),
    privacyUrl = PRIVACY_POLICY_URL,
    playStoreUrl = PLAY_STORE_URL,
    onDismiss = onDismiss
)
