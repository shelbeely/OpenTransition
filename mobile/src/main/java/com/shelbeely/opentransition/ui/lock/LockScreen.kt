/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.lock

import android.widget.ImageView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.util.settings.LockType
import com.squareup.picasso.Picasso

@Composable
fun LockScreen(
    lockType: LockType,
    onUnlock: (code: String) -> Unit,
    onUseBiometric: () -> Unit,
) {
    val hasBackground = lockType != LockType.normal

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasBackground) {
            AndroidView(
                factory = { context ->
                    ImageView(context).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
                    }
                },
                update = { imageView ->
                    Picasso.get()
                        .load(R.drawable.train_track_background)
                        .placeholder(android.R.color.black)
                        .fit()
                        .centerCrop()
                        .into(imageView)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        LockContent(
            lockType = lockType,
            onUnlock = onUnlock,
            onUseBiometric = onUseBiometric,
        )
    }
}

@Composable
private fun LockContent(
    lockType: LockType,
    onUnlock: (code: String) -> Unit,
    onUseBiometric: () -> Unit,
) {
    var code by remember { mutableStateOf("") }

    val isTrainLock = lockType != LockType.normal && lockType != LockType.biometric
    val isPasswordType = !isTrainLock
    val keyboardType = if (isPasswordType) KeyboardType.Password else KeyboardType.Text
    val imeAction = ImeAction.Done
    val hint = if (isTrainLock) {
        stringResource(R.string.enter_train_reporting_number)
    } else {
        stringResource(R.string.enter_password)
    }
    val buttonLabel = if (isTrainLock) {
        stringResource(R.string.search)
    } else {
        stringResource(R.string.unlock)
    }
    val titleText = when (lockType) {
        LockType.biometric -> stringResource(R.string.unlock_with_biometric)
        LockType.trains -> stringResource(R.string.train_tracks_title)
        else -> stringResource(R.string.unlock_with_password)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = titleText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        )

        if (lockType == LockType.biometric) {
            FilledTonalButton(
                onClick = onUseBiometric,
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                Text(text = stringResource(R.string.use_biometric))
            }
        }

        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text(hint) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onUnlock(code) },
            ),
            visualTransformation = if (isPasswordType) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        FilledTonalButton(
            onClick = { onUnlock(code) },
        ) {
            Text(text = buttonLabel)
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
