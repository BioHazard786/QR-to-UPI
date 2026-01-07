package com.biohazard786.qrtoupi


import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QRToUPITheme {
                val navController = rememberNavController()
                var showBottomSheet by remember { mutableStateOf(false) }
                var scannedResult by remember { mutableStateOf<ScanResult?>(null) }
                val sheetState = rememberModalBottomSheetState()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
                            LargeTopAppBar(
                                title = {
                                    Text(
                                        text = "QR to UPI", maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                            )
                        }) { innerPadding ->
                            InfoScreen(
                                modifier = Modifier.padding(innerPadding),
                                onScanClick = { navController.navigate("scanner") })
                        }
                    }

                    composable("scanner") {
                        ScanQrScreen(onQrDetected = { upiId, payeeName, fullLink ->
                            if (!showBottomSheet) {
                                scannedResult = ScanResult(upiId, payeeName, fullLink)
                                navController.popBackStack()
                                showBottomSheet = true
                            }
                        }, onBackClick = { navController.popBackStack() })
                    }
                }

                if (showBottomSheet && scannedResult != null) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            showBottomSheet = false
                            scannedResult = null
                        }, sheetState = sheetState
                    ) {
                        val result = scannedResult!!
                        ScanResultSheet(
                            upiId = result.upiId,
                            payeeName = result.payeeName,
                            fullLink = result.fullLink,
                            onScanAgain = {
                                showBottomSheet = false
                                navController.navigate("scanner")
                            })
                    }
                }
            }
        }
    }
}

data class ScanResult(val upiId: String, val payeeName: String, val fullLink: String)

@Composable
fun InfoScreen(
    modifier: Modifier = Modifier, onScanClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Main Scan Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clickable(onClick = onScanClick),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_qr_code_scanner),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tap to Scan QR",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // 2. How it works (Simplified)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ), shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                FeatureItem(
                    icon = R.drawable.ic_qr_code_scanner, title = "Scan", desc = "Any UPI QR"
                )
                FeatureItem(icon = R.drawable.ic_upi, title = "Verify", desc = "See Payee")
                FeatureItem(icon = R.drawable.ic_share, title = "Share", desc = "Send Link")
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // 3. Footer Actions
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            OutlinedButton(
                onClick = {
                    val upiUriBuilder = Uri.Builder().scheme("upi").authority("pay")
                        .appendQueryParameter("pa", R.string.upi_id.toString())
                        .appendQueryParameter("pn", R.string.upi_name.toString())
                        .appendQueryParameter("tn", R.string.upi_description.toString())

                    val intent = Intent(Intent.ACTION_VIEW, upiUriBuilder.build())
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "No UPI app found", Toast.LENGTH_SHORT).show()
                    }
                }, modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_volunteer_activism),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Donate to Support Dev")
            }

            Button(
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW, "https://github.com/biohazard786/qr-to-upi".toUri()
                    )
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_github),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Source Code")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun FeatureItem(@DrawableRes icon: Int, title: String, desc: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(48.dp),
            tonalElevation = 2.dp
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.padding(12.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.1
        )
    }
}


@Preview(showBackground = true)
@Composable
fun InfoScreenPreview() {
    QRToUPITheme {
        InfoScreen(onScanClick = {})
    }
}

// Simple Theme Wrapper
@Composable
fun QRToUPITheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit
) {
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        supportsDynamicColor && isDarkTheme -> {
            dynamicDarkColorScheme(LocalContext.current)
        }

        supportsDynamicColor && !isDarkTheme -> {
            dynamicLightColorScheme(LocalContext.current)
        }

        isDarkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme, content = content
    )
}