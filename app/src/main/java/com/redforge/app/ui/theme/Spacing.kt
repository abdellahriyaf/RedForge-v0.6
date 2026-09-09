package com.redforge.app.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The app's spacing scale — every padding/gap value in RedForge should be
 * one of these rather than an arbitrary dp literal, so density stays
 * consistent across screens instead of drifting screen-to-screen.
 *
 * xs=4, sm=8, md=12, lg=16, xl=20, xxl=24, xxxl=32 — a strict 4dp rhythm.
 */
object ForgeSpacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val xxxl: Dp = 32.dp

    /** Standard screen-edge padding, used as the outer Column/LazyColumn padding on every top-level screen. */
    val screenPadding: Dp = xl

    /** Standard corner radius for cards and hero containers. */
    val cardRadius: Dp = 18.dp
    val heroRadius: Dp = 24.dp
}
