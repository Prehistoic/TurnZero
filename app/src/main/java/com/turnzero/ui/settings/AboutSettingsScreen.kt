package com.turnzero.ui.settings

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
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
import com.turnzero.core.ui.components.TZDivider
import com.turnzero.update.UpdateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

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
                    Spacer(Modifier.height(16.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_tz_logo),
                        contentDescription = "TurnZero logo",
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "TurnZero",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            item {
                SettingsCategoryRow(
                    icon = Icons.Outlined.Info,
                    title = "Version",
                    description = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    onClick = {}
                )
                TZDivider()
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
                TZDivider()
            }

            item {
                SettingsCategoryRow(
                    icon = Icons.Outlined.SystemUpdate,
                    title = "Check for updates",
                    description = when (val status = updateStatus) {
                        is UpdateManager.UpdateStatus.Checking -> "Checking..."
                        is UpdateManager.UpdateStatus.Downloading ->
                            "Downloading update… ${status.progress}%"
                        is UpdateManager.UpdateStatus.ReadyToInstall -> "Ready to install"
                        else -> "Check for new versions"
                    },
                    widget = {
                        when (val status = updateStatus) {
                            is UpdateManager.UpdateStatus.Checking -> CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )

                            is UpdateManager.UpdateStatus.Downloading -> CircularProgressIndicator(
                                progress = { status.progress / 100f },
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )

                            else -> Unit
                        }
                    },
                    onClick = {
                        if (updateStatus !is UpdateManager.UpdateStatus.Checking &&
                            updateStatus !is UpdateManager.UpdateStatus.Downloading) {
                            scope.launch {
                                updateStatus = UpdateManager.UpdateStatus.Checking
                                when (val result = updateManager.checkForUpdate()) {
                                    is UpdateManager.UpdateStatus.UpToDate -> {
                                        toast("No update available")
                                        updateStatus = UpdateManager.UpdateStatus.Idle
                                    }

                                    is UpdateManager.UpdateStatus.Error -> {
                                        toast(result.message)
                                        updateStatus = UpdateManager.UpdateStatus.Idle
                                    }

                                    is UpdateManager.UpdateStatus.UpdateAvailable -> {
                                        toast("Update ${result.version} found — downloading…")
                                        updateStatus = UpdateManager.UpdateStatus.Downloading(0)
                                        val downloaded = updateManager.downloadUpdate(result.downloadUrl) { percent ->
                                            withContext(Dispatchers.Main.immediate) {
                                                updateStatus = UpdateManager.UpdateStatus.Downloading(percent)
                                            }
                                        }
                                        updateStatus = downloaded
                                        when (downloaded) {
                                            is UpdateManager.UpdateStatus.ReadyToInstall ->
                                                updateManager.installApk(downloaded.apkFile)

                                            is UpdateManager.UpdateStatus.Error -> {
                                                toast(downloaded.message)
                                                updateStatus = UpdateManager.UpdateStatus.Idle
                                            }

                                            else -> Unit
                                        }
                                    }

                                    else -> updateStatus = result
                                }
                            }
                        }
                    }
                )
                TZDivider()
            }

            item {
                SettingsCategoryRow(
                    icon = Icons.Outlined.Description,
                    title = "Licenses",
                    description = "Open source components",
                    onClick = onLicensesClick
                )
                TZDivider()
            }

            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SocialLink(
                        icon = painterResource(id = R.drawable.ic_web),
                        url = "https://turnzero.example.com",
                        uriHandler = uriHandler
                    )
                    SocialLink(
                        icon = painterResource(id = R.drawable.ic_discord),
                        url = "https://discord.gg/turnzero",
                        uriHandler = uriHandler
                    )
                    SocialLink(
                        icon = painterResource(id = R.drawable.ic_github),
                        url = "https://github.com/Prehistoic/TurnZero",
                        uriHandler = uriHandler
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SocialLink(
    icon: androidx.compose.ui.graphics.painter.Painter,
    url: String,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    IconButton(onClick = { uriHandler.openUri(url) }) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
