/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.util

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import io.realm.kotlin.internal.platform.WeakReference

class HideViewOnFailedAdLoad(viewToHide: View) : AdListener() {
    private val viewToHideRef = WeakReference(viewToHide)

    override fun onAdFailedToLoad(error: LoadAdError) {
        super.onAdFailedToLoad(error)
        viewToHideRef.get()?.gone()
    }
}

fun AdView.loadAd(context: Context) {
    setAdSize(getAdaptiveAdSize(context))
    loadAd(AdRequest.Builder().build())
}

private fun getAdaptiveAdSize(context: Context): AdSize {
    val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    val widthPixels: Float
    val density: Float

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val metrics = windowManager.currentWindowMetrics
        widthPixels = metrics.bounds.width().toFloat()
        density = context.resources.displayMetrics.density
    } else {
        val outMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(outMetrics)
        widthPixels = outMetrics.widthPixels.toFloat()
        density = outMetrics.density
    }

    val adWidth = (widthPixels / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
}
