package ua.kpi.its.lab.security.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import ua.kpi.its.lab.security.dto.SoftwareModuleRequest
import ua.kpi.its.lab.security.dto.SoftwareProductRequest
import ua.kpi.its.lab.security.dto.SoftwareProductResponse

@Composable
fun SoftwareProductScreen(
    token: String,
    scope: CoroutineScope,
    client: HttpClient,
    snackbarHostState: SnackbarHostState
) {
    var softwareProducts by remember { mutableStateOf<List<SoftwareProductResponse>>(listOf()) }
    var loading by remember { mutableStateOf(false) }
    var openDialog by remember { mutableStateOf(false) }
    var selectedSoftwareProduct by remember { mutableStateOf<SoftwareProductResponse?>(null) }

    LaunchedEffect(token) {
        loading = true
        delay(1000)
        softwareProducts = withContext(Dispatchers.IO) {
            try {
                val response = client.get("http://localhost:8080/software-products") {
                    bearerAuth(token)
                }
                loading = false
                response.body()
            } catch (e: Exception) {
                val msg = e.toString()
                snackbarHostState.showSnackbar(msg, withDismissAction = true, duration = SnackbarDuration.Indefinite)
                softwareProducts
            }
        }
    }

    if (loading) {
        LinearProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedSoftwareProduct = null
                    openDialog = true
                },
                content = {
                    Icon(Icons.Filled.Add, "Add SW Product")
                }
            )
        }
    ) {
        if (softwareProducts.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text("No SW products to show", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant).fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(softwareProducts) { softwareProduct ->
                    SoftwareProductItem(
                        softwareProduct = softwareProduct,
                        onEdit = {
                            selectedSoftwareProduct = softwareProduct
                            openDialog = true
                        },
                        onRemove = {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    try {
                                        val response = client.delete("http://localhost:8080/software-products/${softwareProduct.id}") {
                                            bearerAuth(token)
                                        }
                                        require(response.status.isSuccess())
                                    } catch (e: Exception) {
                                        val msg = e.toString()
                                        snackbarHostState.showSnackbar(msg, withDismissAction = true, duration = SnackbarDuration.Indefinite)
                                    }
                                }

                                loading = true

                                softwareProducts = withContext(Dispatchers.IO) {
                                    try {
                                        val response = client.get("http://localhost:8080/software-products") {
                                            bearerAuth(token)
                                        }
                                        loading = false
                                        response.body()
                                    } catch (e: Exception) {
                                        val msg = e.toString()
                                        snackbarHostState.showSnackbar(msg, withDismissAction = true, duration = SnackbarDuration.Indefinite)
                                        softwareProducts
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        if (openDialog) {
            SoftwareProductDialog(
                softwareProduct = selectedSoftwareProduct,
                token = token,
                scope = scope,
                client = client,
                onDismiss = {
                    openDialog = false
                },
                onError = {
                    scope.launch {
                        snackbarHostState.showSnackbar(it, withDismissAction = true, duration = SnackbarDuration.Indefinite)
                    }
                },
                onConfirm = {
                    openDialog = false
                    loading = true
                    scope.launch {
                        softwareProducts = withContext(Dispatchers.IO) {
                            try {
                                val response = client.get("http://localhost:8080/software-products") {
                                    bearerAuth(token)
                                }
                                loading = false
                                response.body()
                            } catch (e: Exception) {
                                loading = false
                                softwareProducts
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun SoftwareProductDialog(
    softwareProduct: SoftwareProductResponse?,
    token: String,
    scope: CoroutineScope,
    client: HttpClient,
    onDismiss: () -> Unit,
    onError: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    val module = softwareProduct?.module

    var name by remember { mutableStateOf(softwareProduct?.name ?: "") }
    var developer by remember { mutableStateOf(softwareProduct?.developer ?: "") }
    var version by remember { mutableStateOf(softwareProduct?.version ?: "") }
    var releaseDate by remember { mutableStateOf(softwareProduct?.releaseDate ?: "") }
    var size by remember { mutableStateOf(softwareProduct?.size?.toString() ?: "") }
    var is64bit by remember { mutableStateOf(softwareProduct?.is64bit ?: false) }
    var isCrossPlatform by remember { mutableStateOf(softwareProduct?.isCrossPlatform ?: false) }
    var moduleDescription by remember { mutableStateOf(module?.description ?: "") }
    var moduleAuthor by remember { mutableStateOf(module?.author ?: "") }
    var moduleLanguage by remember { mutableStateOf(module?.language ?: "") }
    var moduleLastUpdated by remember { mutableStateOf(module?.lastUpdated ?: "") }
    var moduleSize by remember { mutableStateOf(module?.size?.toString() ?: "") }
    var moduleLinesOfCode by remember { mutableStateOf(module?.linesOfCode?.toString() ?: "") }
    var moduleIsCrossPlatform by remember { mutableStateOf(module?.isCrossPlatform ?: false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(16.dp).wrapContentSize()) {
            Column(
                modifier = Modifier.padding(16.dp, 8.dp).width(IntrinsicSize.Max).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (softwareProduct == null) {
                    Text("Create SW product")
                } else {
                    Text("Update SW product")
                }

                HorizontalDivider()
                Text("SW Product info")
                TextField(name, { name = it }, label = { Text("Name") })
                TextField(developer, { developer = it }, label = { Text("Developer") })
                TextField(version, { version = it }, label = { Text("Version") })
                TextField(releaseDate, { releaseDate = it }, label = { Text("Release date") })
                TextField(size, { size = it }, label = { Text("Size") })
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(is64bit, { is64bit = it })
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("64-bit")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(isCrossPlatform, { isCrossPlatform = it })
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Cross-platform")
                }

                HorizontalDivider()
                Text("Module info")
                TextField(moduleDescription, { moduleDescription = it }, label = { Text("Description") })
                TextField(moduleAuthor, { moduleAuthor = it }, label = { Text("Author") })
                TextField(moduleLanguage, { moduleLanguage = it }, label = { Text("Language") })
                TextField(moduleLastUpdated, { moduleLastUpdated = it }, label = { Text("Last updated") })
                TextField(moduleSize, { moduleSize = it }, label = { Text("Size") })
                TextField(moduleLinesOfCode, { moduleLinesOfCode = it }, label = { Text("Lines of code") })
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(moduleIsCrossPlatform, { moduleIsCrossPlatform = it })
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Cross-platform")
                }

                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.fillMaxWidth(0.1f))
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            scope.launch {
                                try {
                                    val request = SoftwareProductRequest(
                                        name, developer, version, releaseDate, size.toDouble(), is64bit, isCrossPlatform,
                                        SoftwareModuleRequest(
                                            moduleDescription, moduleAuthor, moduleLanguage, moduleLastUpdated, moduleSize.toDouble(),
                                            moduleLinesOfCode.toInt(), moduleIsCrossPlatform
                                        )
                                    )
                                    val response = if (softwareProduct == null) {
                                        client.post("http://localhost:8080/software-products") {
                                            bearerAuth(token)
                                            setBody(request)
                                            contentType(ContentType.Application.Json)
                                        }
                                    } else {
                                        client.put("http://localhost:8080/software-products/${softwareProduct.id}") {
                                            bearerAuth(token)
                                            setBody(request)
                                            contentType(ContentType.Application.Json)
                                        }
                                    }
                                    require(response.status.isSuccess())
                                    onConfirm()
                                } catch (e: Exception) {
                                    val msg = e.toString()
                                    onError(msg)
                                }
                            }
                        }
                    ) {
                        if (softwareProduct == null) {
                            Text("Create")
                        } else {
                            Text("Update")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SoftwareProductItem(softwareProduct: SoftwareProductResponse, onEdit: () -> Unit, onRemove: () -> Unit) {
    Card(shape = CardDefaults.elevatedShape, elevation = CardDefaults.elevatedCardElevation()) {
        ListItem(
            overlineContent = {
                Text(softwareProduct.name)
            },
            headlineContent = {
                Text(softwareProduct.developer)
            },
            supportingContent = {
                Text("$${softwareProduct.size}")
            },
            trailingContent = {
                Row(modifier = Modifier.padding(0.dp, 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(CircleShape).clickable(onClick = onEdit)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(CircleShape).clickable(onClick = onRemove)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        )
    }
}
