package com.cheeke.surfy.ui.setting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cheeke.surfy.R
import com.cheeke.surfy.analytics.api.TrackScreenViewEvent
import com.cheeke.surfy.common.getVersionName
import com.cheeke.surfy.firebase.api.LocalFirebaseLogHelper
import com.cheeke.surfy.ui.utils.dp1
import com.cheeke.surfy.ui.utils.dp10
import com.cheeke.surfy.ui.utils.dp12
import com.cheeke.surfy.ui.utils.dp14
import com.cheeke.surfy.ui.utils.dp16
import com.cheeke.surfy.ui.utils.dp18
import com.cheeke.surfy.ui.utils.dp2
import com.cheeke.surfy.ui.utils.dp28
import com.cheeke.surfy.ui.utils.dp420
import com.cheeke.surfy.ui.utils.dp48
import com.cheeke.surfy.ui.utils.dp5
import com.cheeke.surfy.ui.utils.dp520
import com.cheeke.surfy.ui.utils.dp560
import com.cheeke.surfy.ui.utils.dp8
import com.cheeke.surfy.ui.utils.dp84
import com.cheeke.surfy.ui.utils.dp999

@Composable
fun SettingScreen(
    viewModel: SettingVM = hiltViewModel()
) {
    LocalFirebaseLogHelper.current.sendLog("MyScreen", "my screen init")
    TrackScreenViewEvent(screenName = "SettingScreen")

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isCheatActive by viewModel.isCheatActive.collectAsStateWithLifecycle()

    SettingScreen(
        state = uiState,
        isCheatActive = isCheatActive,
        onAction = viewModel::onAction,
        onClickTitle = viewModel::onClickTitle
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    state: SettingsUiState,
    isCheatActive: Boolean,
    onAction: (SettingsAction) -> Unit,
    onClickTitle: () -> Unit
) {
    if (state.sheet == SettingsSheet.Hidden) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onAction(SettingsAction.CloseSheet) },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = dp28, topEnd = dp28),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        when (state.sheet) {
            SettingsSheet.Main -> {
                SettingMainSheet(
                    state = state,
                    isCheatActive = isCheatActive,
                    onAction = onAction,
                    onClickTitle = onClickTitle
                )
            }
            SettingsSheet.ThemeSetting -> {
                ThemeSettingSubSheet(
                    state = state,
                    onAction = onAction
                )
            }
            SettingsSheet.LanguageRegion -> {
                LanguageRegionSubSheet(
                    state = state,
                    onAction = onAction
                )
            }
            SettingsSheet.ImageQuality -> {
                ImageQualitySubSheet(
                    state = state,
                    onAction = onAction
                )
            }
            SettingsSheet.Hidden -> Unit
        }

        Spacer(modifier = Modifier.height(height = dp12))
    }
}

@Composable
fun SettingMainSheet(
    state: SettingsUiState,
    isCheatActive: Boolean,
    onAction: (SettingsAction) -> Unit,
    onClickTitle: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        SheetHeader(
            title = stringResource(id = R.string.feature_my_name),
            onClickTitle = onClickTitle
        )
        HorizontalDivider()
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dp10, vertical = dp5),
            shape = RoundedCornerShape(size = dp16),
            border = BorderStroke(width = dp1, color = MaterialTheme.colorScheme.inverseSurface)
        ) {
            SettingRowSwitch(
                title = stringResource(id = R.string.is_adult_setting),
                checked = state.isAdult,
                onCheckedChange = { onAction(SettingsAction.SetAdult(enabled = it)) }
            )
            SettingRowSwitch(
                title = stringResource(id = R.string.auto_playing_trailer_setting),
                checked = state.isTrailerAutoplay,
                onCheckedChange = { onAction(SettingsAction.SetTrailerAutoplay(enabled = it)) }
            )
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dp10, vertical = dp5),
            shape = RoundedCornerShape(size = dp16),
            border = BorderStroke(width = dp1, color = MaterialTheme.colorScheme.inverseSurface)
        ) {
            SettingRowChevron(
                title = stringResource(id = R.string.dark_mode_setting),
                value = state.theme.label,
                onClick = { onAction(SettingsAction.OpenThemeSetting) }
            )
            SettingRowChevron(
                title = stringResource(id = R.string.language_setting),
                value = "${state.selectedLanguage}-${state.selectedRegion}",
                onClick = { onAction(SettingsAction.OpenLanguageRegion) }
            )
            SettingRowChevron(
                title = stringResource(id = R.string.image_quality_setting),
                value = state.imageQuality,
                onClick = { onAction(SettingsAction.OpenImageQuality) }
            )
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dp10, vertical = dp5),
            shape = RoundedCornerShape(size = dp16),
            border = BorderStroke(width = dp1, color = MaterialTheme.colorScheme.inverseSurface)
        ) {
            SettingRowText(
                title = stringResource(id = R.string.main_update_data_setting),
                value = state.mainUpdateDate
            )
            SettingRowText(
                title = stringResource(id = R.string.version_info),
                value = getVersionName(context = context)
            )
            if (isCheatActive) {
                SettingRowSwitch(
                    title = "Developer Mode",
                    checked = state.isCheatActive ?: false,
                    onCheckedChange = { onAction(SettingsAction.SetCheatActive(enabled = it)) }
                )
            }
        }

        BottomCloseButton(
            onClick = { onAction(SettingsAction.CloseSheet) }
        )
    }
}

@Composable
private fun SheetHeader(title: String, onClickTitle: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dp18, vertical = dp10),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(weight = 1f))
        Text(
            modifier = Modifier.clickable(interactionSource = null, indication = null) { onClickTitle() },
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(weight = 1f))
    }
}

@Composable
private fun SettingRowText(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dp18, vertical = dp18),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider()
}

@Composable
private fun SettingRowChevron(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = dp18, vertical = dp18),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Text(text = "$value  >", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider()
}

@Composable
private fun SettingRowSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dp18),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(weight = 1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    HorizontalDivider()
}

@Composable
private fun BottomCloseButton(onClick: () -> Unit) {
    Box(Modifier
        .fillMaxWidth()
        .padding(horizontal = dp18, vertical = dp10)) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(height = dp48),
            shape = RoundedCornerShape(size = dp999)
        ) { Text(stringResource(id = R.string.setting_close)) }
    }
}

@Composable
fun ThemeSettingSubSheet(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit
) {
    val current = state.selectedTheme ?: state.theme
    var pending = state.theme
    val options = state.themeList

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dp18, vertical = dp10),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { onAction(SettingsAction.BackToMainFromImageQuality) }) { Text(text = stringResource(id = R.string.setting_back)) }
            Spacer(modifier = Modifier.weight(weight = 1f))
            Text(text = stringResource(id = R.string.theme_setting), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(weight = 1f))
            TextButton(onClick = { onAction(SettingsAction.CloseSheet) }) { Text(text = stringResource(id = R.string.setting_close)) }
        }

        HorizontalDivider()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = dp420, max = dp520)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = dp84),
                contentPadding = PaddingValues(horizontal = dp16, vertical = dp12),
                verticalArrangement = Arrangement.spacedBy(space = dp10)
            ) {
                items(items = options, key = { it }) { opt ->
                    SelectRowSimple(
                        label = opt.label,
                        selected = opt == current,
                        onClick = {
                            onAction(SettingsAction.PickTheme(option = opt))
                            pending = opt
                        }
                    )
                }
            }

            BottomConfirmBar(
                enabled = pending != current,
                text = stringResource(id = R.string.setting_confirm),
                onClick = { onAction(SettingsAction.ConfirmTheme) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun ImageQualitySubSheet(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit
) {
    val current = state.selectedImageQuality ?: state.imageQuality
    var pending = state.imageQuality
    val options = state.imageQualityList

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dp18, vertical = dp10),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { onAction(SettingsAction.BackToMainFromImageQuality) }) { Text(text = stringResource(id = R.string.setting_back)) }
            Spacer(modifier = Modifier.weight(weight = 1f))
            Text(text = stringResource(id = R.string.image_quality_setting), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(weight = 1f))
            TextButton(onClick = { onAction(SettingsAction.CloseSheet) }) { Text(text = stringResource(id = R.string.setting_close)) }
        }

        HorizontalDivider()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = dp420, max = dp520)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = dp84),
                contentPadding = PaddingValues(horizontal = dp16, vertical = dp12),
                verticalArrangement = Arrangement.spacedBy(space = dp10)
            ) {
                items(items = options, key = { it }) { opt ->
                    SelectRowSimple(
                        label = opt,
                        selected = opt == current,
                        onClick = {
                            onAction(SettingsAction.PickImageQuality(option = opt))
                            pending = opt
                        }
                    )
                }
            }

            BottomConfirmBar(
                enabled = pending != current,
                text = stringResource(id = R.string.setting_confirm),
                onClick = { onAction(SettingsAction.ConfirmImageQuality) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

enum class LocaleTab { LANGUAGE, REGION }

@Composable
fun LanguageRegionSubSheet(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit
) {
    var tab by remember { mutableStateOf(value = LocaleTab.LANGUAGE) }
    var query by remember { mutableStateOf(value = TextFieldValue(text = "")) }
    val selectedLang = state.selectedLanguage ?: state.language
    val selectedRegion = state.selectedRegion ?: state.region
    val all = if (tab == LocaleTab.LANGUAGE) state.allLanguages else state.allRegions
    val selectedCode = if (tab == LocaleTab.LANGUAGE) selectedLang else selectedRegion
    val filtered = remember(key1 = all, key2 = query.text) {
        if (query.text.isBlank()) {
            all
        } else {
            all.filter { it.label.contains(other = query.text, ignoreCase = true) || it.code.contains(other = query.text, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dp18, vertical = dp10),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { onAction(SettingsAction.BackToMainFromLanguageRegion) }) { Text(text = "뒤로") }
            Spacer(modifier = Modifier.weight(weight = 1f))
            Text(text = "언어 / 지역", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(weight = 1f))
            TextButton(onClick = { onAction(SettingsAction.CloseSheet) }) { Text(text = "닫기") }
        }

        PrimaryTabRow(selectedTabIndex = if (tab == LocaleTab.LANGUAGE) 0 else 1, divider = {}) {
            Tab(selected = tab == LocaleTab.LANGUAGE, onClick = { tab = LocaleTab.LANGUAGE; query = TextFieldValue(text = "") }, text = { Text(text = stringResource(id = R.string.language_setting)) })
            Tab(selected = tab == LocaleTab.REGION, onClick = { tab = LocaleTab.REGION; query = TextFieldValue(text = "") }, text = { Text(text = stringResource(id = R.string.region_setting)) })
        }

        Spacer(modifier = Modifier.height(height = dp12))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dp16),
            placeholder = { Text(text = stringResource(R.string.language_and_region_search_hint)) },
            singleLine = true,
            shape = RoundedCornerShape(size = dp16),
            trailingIcon = {
                if (query.text.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.language_and_region_remove_keyword),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(end = dp12)
                            .clickable { query = TextFieldValue(text = "") }
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(height = dp12))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = dp420, max = dp560)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = dp84),
                contentPadding = PaddingValues(horizontal = dp16, vertical = dp8),
                verticalArrangement = Arrangement.spacedBy(space = dp10)
            ) {
                items(items = filtered, key = { it.code }) { opt ->
                    SelectRowSimple(
                        label = opt.label,
                        selected = opt.code == selectedCode
                    ) {
                        if (tab == LocaleTab.LANGUAGE) {
                            onAction(SettingsAction.PickLanguage(option = opt.code))
                        } else {
                            onAction(SettingsAction.PickRegion(option = opt.code))
                        }
                    }
                }
            }

            // Confirm bar
            BottomConfirmBar(
                enabled = (selectedLang != state.language) || (selectedRegion != state.region),
                text = "확인",
                onClick = { onAction(SettingsAction.ConfirmLanguageRegion) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun SelectRowSimple(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(size = dp16),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dp14, vertical = dp12)
        ) {
            Text(text = label, modifier = Modifier.weight(weight = 1f))
            if (selected) Text(text = "✓", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun BottomConfirmBar(
    enabled: Boolean,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier.fillMaxWidth(),
        shadowElevation = dp10, tonalElevation = dp2
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dp16, vertical = dp14)
        ) {
            Button(
                enabled = enabled,
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = dp48),
                shape = RoundedCornerShape(size = dp999)
            ) { Text(text) }
        }
    }
}