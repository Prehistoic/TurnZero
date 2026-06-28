package com.turnzero.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.turnzero.R
import com.turnzero.core.ui.components.TZTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(onBack: () -> Unit) {
    val libs by produceLibraries(R.raw.aboutlibraries)
    val libraries = libs?.libraries ?: emptyList()
    val uriHandler = LocalUriHandler.current
    var selectedLibrary by remember { mutableStateOf<LibraryDetail?>(null) }

    Scaffold(
        topBar = {
            TZTopBar(
                title = "Licenses",
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(libraries, key = { it.uniqueId }) { library ->
                val primaryLicense = library.licenses?.firstOrNull()
                LicenseRow(
                    name = library.name ?: library.artifactId ?: library.uniqueId,
                    artifactId = library.artifactId,
                    version = library.artifactVersion ?: "",
                    licenses = library.licenses?.joinToString(", ") { it.name } ?: "",
                    onClick = {
                        selectedLibrary = LibraryDetail(
                            name = library.name ?: library.artifactId ?: library.uniqueId,
                            artifactId = library.artifactId,
                            version = library.artifactVersion ?: "",
                            licenses = library.licenses?.map { it.name } ?: emptyList(),
                            licenseUrl = primaryLicense?.url,
                            website = library.website
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }

    selectedLibrary?.let { detail ->
        AlertDialog(
            onDismissRequest = { selectedLibrary = null },
            title = { Text(detail.name, maxLines = 2, overflow = TextOverflow.Ellipsis) },
            text = {
                Column {
                    Text(
                        text = detail.artifactId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Version: ${detail.version}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "License: ${detail.licenses.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedLibrary = null }) {
                    Text("Close")
                }
            },
            dismissButton = {
                if (detail.licenseUrl != null) {
                    TextButton(onClick = {
                        uriHandler.openUri(detail.licenseUrl)
                        selectedLibrary = null
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("View license")
                    }
                }
                if (detail.website != null) {
                    TextButton(onClick = {
                        uriHandler.openUri(detail.website)
                        selectedLibrary = null
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Website")
                    }
                }
            }
        )
    }
}

private data class LibraryDetail(
    val name: String,
    val artifactId: String,
    val version: String,
    val licenses: List<String>,
    val licenseUrl: String?,
    val website: String?
)

@Composable
private fun LicenseRow(
    name: String,
    artifactId: String,
    version: String,
    licenses: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Outlined.Description,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .offset(y = 2.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = artifactId,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (licenses.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = licenses,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = version,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}
