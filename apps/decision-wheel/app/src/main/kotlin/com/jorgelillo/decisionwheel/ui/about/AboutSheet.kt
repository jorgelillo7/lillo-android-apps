package com.jorgelillo.decisionwheel.ui.about

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.jorgelillo.core.designsystem.AboutTexts
import com.jorgelillo.core.designsystem.LilloAboutSheet
import com.jorgelillo.core.platform.LilloLinks
import com.jorgelillo.decisionwheel.BuildConfig
import com.jorgelillo.decisionwheel.R
import com.jorgelillo.decisionwheel.ui.theme.WheelColors

val PRIVACY_POLICY_URL = LilloLinks.privacyPolicy("decision-wheel")
val PLAY_STORE_URL = LilloLinks.playStore(BuildConfig.APPLICATION_ID)

@Composable
internal fun AboutSheet(onDismiss: () -> Unit) = LilloAboutSheet(
    icon = painterResource(R.drawable.ic_launcher_foreground),
    iconBackground = WheelColors.Night,
    containerColor = WheelColors.NightRaised,
    texts = AboutTexts(
        title = stringResource(R.string.app_name),
        version = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
        body = stringResource(R.string.about_body),
        rate = stringResource(R.string.about_rate),
        privacy = stringResource(R.string.about_privacy)
    ),
    privacyUrl = PRIVACY_POLICY_URL,
    playStoreUrl = PLAY_STORE_URL,
    onDismiss = onDismiss
)
