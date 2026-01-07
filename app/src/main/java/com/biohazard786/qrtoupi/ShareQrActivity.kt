package com.biohazard786.qrtoupi

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.net.toUri
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.io.IOException

class ShareQrActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle the incoming intent
        if (Intent.ACTION_SEND == intent.action && intent.type?.startsWith("image/") == true) {

            val imageUri: Uri? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                }

            imageUri?.let {
                scanQrCode(it)
            } ?: finish()

        } else {
            finish()
        }

    }

    private fun scanQrCode(imageUri: Uri) {
        val image: InputImage
        try {
            image = InputImage.fromFilePath(applicationContext, imageUri)
        } catch (e: IOException) {
            e.printStackTrace()
            showToastAndFinish("Failed to load image")
            return
        }

        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val upiBarcode = barcodes.firstOrNull {
                    it.rawValue?.startsWith("upi://") == true
                }

                if (upiBarcode != null) {
                    launchUpiPayment(upiBarcode.rawValue!!)
                } else {
                    showToastAndFinish("No valid UPI QR found")
                }
            }
            .addOnFailureListener {
                showToastAndFinish("Failed to scan QR")
            }
            .addOnCompleteListener {
                // Ensure we finish even if callbacks behave unexpectedly, 
                // but success/failure usually cover it. 
                // If success acts, it finishes. If failure acts, it finishes.
            }
    }

    private fun launchUpiPayment(upiUriString: String) {
        val upiIntent = Intent(Intent.ACTION_VIEW, upiUriString.toUri())
        try {
            startActivity(upiIntent)
            finish()
        } catch (e: Exception) {
            showToastAndFinish("No UPI app found")
        }
    }

    private fun showToastAndFinish(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }
}
