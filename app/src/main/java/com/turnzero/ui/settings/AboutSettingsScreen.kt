package com.turnzero.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.turnzero.BuildConfig
import com.turnzero.R
import com.turnzero.core.ui.components.TZTopBar
import com.turnzero.update.UpdateManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSettingsScreen(
    onBack: () -> Unit,
    onLicensesClick: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    var updateStatus by remember { mutableStateOf<UpdateManager.UpdateStatus>(UpdateManager.UpdateStatus.Idle) }
    val updateManager = remember { UpdateManager(context) }

    Scaffold(
        topBar = {
            TZTopBar(
                title = "About",
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(48.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_tz_logo),
                        contentDescription = "TurnZero logo",
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "TurnZero",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(48.dp))
                }
            }

            item {
                SettingsCategoryRow(
                    icon = Icons.Outlined.Info,
                    title = "Version",
                    description = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    onClick = {}
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            item {
                SettingsCategoryRow(
                    icon = Icons.Outlined.Description,
                    title = "Changelog",
                    description = "View release notes and changes",
                    onClick = {
                        uriHandler.openUri("https://github.com/Prehistoic/TurnZero/releases")
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            item {
                SettingsCategoryRow(
                    icon = Icons.Outlined.SystemUpdate,
                    title = "Check for updates",
                    description = when (updateStatus) {
                        is UpdateManager.UpdateStatus.Checking -> "Checking..."
                        is UpdateManager.UpdateStatus.UpToDate -> "Up to date"
                        is UpdateManager.UpdateStatus.UpdateAvailable -> "Update available"
                        is UpdateManager.UpdateStatus.Downloading -> "Downloading..."
                        is UpdateManager.UpdateStatus.Error -> "Tap to retry"
                        is UpdateManager.UpdateStatus.Idle -> "Check for new versions"
                        is UpdateManager.UpdateStatus.ReadyToInstall -> "Ready to install"
                    },
                    widget = {
                        AnimatedVisibility(visible = updateStatus is UpdateManager.UpdateStatus.Checking ||
                            updateStatus is UpdateManager.UpdateStatus.Downloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        if (updateStatus is UpdateManager.UpdateStatus.UpToDate) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    onClick = {
                        if (updateStatus !is UpdateManager.UpdateStatus.Checking &&
                            updateStatus !is UpdateManager.UpdateStatus.Downloading) {
                            scope.launch {
                                updateStatus = UpdateManager.UpdateStatus.Checking
                                updateStatus = updateManager.checkForUpdate()
                            }
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            item {
                SettingsCategoryRow(
                    icon = Icons.Outlined.Description,
                    title = "Licenses",
                    description = "Open source components",
                    onClick = onLicensesClick
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            item {
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SocialLink(
                        icon = painterResource(id = R.drawable.ic_web),
                        label = "Web",
                        url = "https://turnzero.example.com",
                        uriHandler = uriHandler
                    )
                    SocialLink(
                        icon = painterResource(id = R.drawable.ic_discord),
                        label = "Discord",
                        url = "https://discord.gg/turnzero",
                        uriHandler = uriHandler
                    )
                    SocialLink(
                        icon = painterResource(id = R.drawable.ic_github),
                        label = "GitHub",
                        url = "https://github.com/Prehistoic/TurnZero",
                        uriHandler = uriHandler
                    )
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    LaunchedEffect(updateStatus) {
        if (updateStatus is UpdateManager.UpdateStatus.UpdateAvailable) {
            val available = updateStatus as UpdateManager.UpdateStatus.UpdateAvailable
            updateStatus = updateManager.downloadUpdate(available.downloadUrl)
        }
        if (updateStatus is UpdateManager.UpdateStatus.ReadyToInstall) {
            val ready = updateStatus as UpdateManager.UpdateStatus.ReadyToInstall
            updateManager.installApk(ready.apkFile)
        }
    }
}

@Composable
private fun SocialLink(
    icon: androidx.compose.ui.graphics.painter.Painter,
    label: String,
    url: String,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = { uriHandler.openUri(url) }) {
            Icon(
                painter = icon,
                contentDescription = label,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
