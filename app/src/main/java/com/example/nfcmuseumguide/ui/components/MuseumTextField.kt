package com.example.nfcmuseumguide.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.nfcmuseumguide.ui.theme.CardWarm
import com.example.nfcmuseumguide.ui.theme.MuseumGold
import com.example.nfcmuseumguide.ui.theme.MuseumTeal
import com.example.nfcmuseumguide.ui.theme.SoftText
import com.example.nfcmuseumguide.ui.theme.WarmText

@Composable
fun MuseumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        leadingIcon = leadingIcon,
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = WarmText,
            unfocusedTextColor = WarmText,
            focusedLabelColor = MuseumGold,
            unfocusedLabelColor = SoftText,
            focusedLeadingIconColor = MuseumGold,
            unfocusedLeadingIconColor = SoftText,
            cursorColor = MuseumGold,
            focusedBorderColor = MuseumGold,
            unfocusedBorderColor = SoftText.copy(alpha = .42f),
            focusedContainerColor = CardWarm.copy(alpha = .92f),
            unfocusedContainerColor = CardWarm.copy(alpha = .72f),
            focusedPlaceholderColor = SoftText,
            unfocusedPlaceholderColor = SoftText,
            errorTextColor = WarmText,
            errorLabelColor = MuseumTeal
        )
    )
}
