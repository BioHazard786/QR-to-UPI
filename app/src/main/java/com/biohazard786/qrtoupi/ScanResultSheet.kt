package com.biohazard786.qrtoupi

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ScanResultSheet(
    upiId: String,
    payeeName: String,
    fullLink: String,
    onScanAgain: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Scanned UPI Details",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (payeeName.isNotEmpty()) {
            Text(
                text = "Payee Name",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = payeeName,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Text(
            text = "UPI ID",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = upiId,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Share Button
        Button(
            onClick = {
                val shareText = """
                    Pay: $payeeName
                    
                    UPI ID: $upiId
                    
                    Link: $fullLink
                """.trimIndent()

                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Share UPI Details")
                context.startActivity(shareIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_share),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Share UPI Link")
        }

        // Scan Again (Secondary)
        Button(
            onClick = onScanAgain,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors() // Text button for less emphasis
        ) {
            Text("Scan Another QR")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
