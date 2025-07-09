package com.example.gogomarket.ui.screens

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.gogomarket.R
import com.example.gogomarket.viewmodel.CourierViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: CourierViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userName.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val languages = listOf("ru" to "Русский", "uz" to "O'zbekcha")
    var selectedIndex by remember { mutableStateOf(0) }

    // Устанавливаем начальное состояние переключателя при первом запуске
    LaunchedEffect(viewModel.userPreferences) {
        viewModel.userPreferences.getLanguage().collect { langCode ->
            selectedIndex = languages.indexOfFirst { it.first == langCode }.coerceAtLeast(0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = stringResource(R.string.avatar),
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.welcome_user, userName),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            stringResource(R.string.settings),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Переключатель языка
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            languages.forEachIndexed { index, (langCode, langName) ->
                val isSelected = index == selectedIndex
                val shape = when (index) {
                    0 -> RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
                    languages.lastIndex -> RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
                    else -> RectangleShape
                }
                val buttonModifier = Modifier
                    .weight(1f)
                    .offset(x = (-1 * index).dp)
                    .zIndex(if (isSelected) 1f else 0f)


                OutlinedButton(
                    onClick = {
                        selectedIndex = index
                        scope.launch {
                            // Просто сохраняем новый язык в Preferences.
                            // MainActivity отреагирует на это изменение.
                            viewModel.userPreferences.saveLanguage(langCode)
                        }
                    },
                    shape = shape,
                    colors = if (isSelected) {
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = buttonModifier
                ) {
                    Text(langName)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопка выхода
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(50.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = stringResource(R.string.logout))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.logout))
        }
    }
}