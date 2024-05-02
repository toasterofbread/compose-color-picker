// ktlint-disable filename
package com.godaddy.app.theme

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import composecolorpicker.composeapp.generated.resources.Res
import composecolorpicker.composeapp.generated.resources.content_description_back_button
import org.jetbrains.compose.resources.stringResource

@Composable
fun BackButton(onBackPress: () -> Unit) {
    IconButton(
        onClick = {
            onBackPress()
        }
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            tint = MaterialTheme.colors.onPrimary,
            contentDescription = stringResource(Res.string.content_description_back_button)
        )
    }
}
