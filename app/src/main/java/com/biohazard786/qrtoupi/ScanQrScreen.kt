package com.biohazard786.qrtoupi

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun ScanQrScreen(
    onQrDetected: (upiId: String, payeeName: String, fullLink: String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(), onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                Toast.makeText(context, "Camera permission needed to scan QR", Toast.LENGTH_SHORT)
                    .show()
            }
        })

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(), onQrDetected = onQrDetected
            )
            // Overlay
            ScannerOverlay()
            // UI Controls
            ScannerUi(onBackClick = onBackClick)

        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Please grant camera permission to scan QR code.")
            }
        }
    }
}

@Composable
fun ScannerUi(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Scan any QR",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "PhonePe • Google Pay • BHIM • Paytm",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Logos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_bhim),
                contentDescription = "BHIM",
                tint = Color.Unspecified,
                modifier = Modifier
                    .height(20.dp)
                    .alpha(0.3f)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_line),
                contentDescription = "BHIM",
                tint = Color.Unspecified,
                modifier = Modifier
                    .width(20.dp)
                    .alpha(0.3f)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_upi),
                contentDescription = "UPI",
                tint = Color.Unspecified,
                modifier = Modifier
                    .height(20.dp)
                    .alpha(0.3f)
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ScannerOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {

        // Styling knobs
        val scrimColor = Color.Black.copy(alpha = 0.4f)
        val cornerColor = Color.White

        val cornerRadius = 18.dp.toPx()
        val cornerStrokeWidth = 3.dp.toPx()
        val cornerLength = 18.dp.toPx()
        val inset = cornerStrokeWidth / 2f + 20.dp.toPx()

        // Scanner box
        val boxSize = size.width * 0.75f
        val boxLeft = (size.width - boxSize) / 2
        val boxTop = (size.height - boxSize) / 2
        val boxCornerRadius = 50.dp.toPx()

        val boxRect = Rect(
            left = boxLeft,
            top = boxTop,
            right = boxLeft + boxSize,
            bottom = boxTop + boxSize
        )

        // Scrim with cutout
        val cutoutPath = Path().apply {
            addRect(Rect(0f, 0f, size.width, size.height))
            addRoundRect(
                RoundRect(
                    boxRect,
                    CornerRadius(boxCornerRadius, boxCornerRadius)
                )
            )
            fillType = PathFillType.EvenOdd
        }
        drawPath(cutoutPath, scrimColor)

        // Helper to draw curved corner using arc
        fun drawCornerArc(
            rect: Rect,
            startAngle: Float
        ) {
            val path = Path().apply {
                arcTo(
                    rect = rect,
                    startAngleDegrees = startAngle,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
            }
            drawPath(
                path = path,
                color = cornerColor,
                style = Stroke(
                    width = cornerStrokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }

        // Top-Left
        drawCornerArc(
            rect = Rect(
                boxRect.left + inset,
                boxRect.top + inset,
                boxRect.left + inset + (cornerRadius + cornerLength) * 2,
                boxRect.top + inset + (cornerRadius + cornerLength) * 2
            ),
            startAngle = 180f
        )

        // Top-Right
        drawCornerArc(
            rect = Rect(
                boxRect.right - inset - (cornerRadius + cornerLength) * 2,
                boxRect.top + inset,
                boxRect.right - inset,
                boxRect.top + inset + (cornerRadius + cornerLength) * 2
            ),
            startAngle = 270f
        )

        // Bottom-Right
        drawCornerArc(
            rect = Rect(
                boxRect.right - inset - (cornerRadius + cornerLength) * 2,
                boxRect.bottom - inset - (cornerRadius + cornerLength) * 2,
                boxRect.right - inset,
                boxRect.bottom - inset
            ),
            startAngle = 0f
        )

        // Bottom-Left
        drawCornerArc(
            rect = Rect(
                boxRect.left + inset,
                boxRect.bottom - inset - (cornerRadius + cornerLength) * 2,
                boxRect.left + inset + (cornerRadius + cornerLength) * 2,
                boxRect.bottom - inset
            ),
            startAngle = 90f
        )


    }
}



@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onQrDetected: (upiId: String, payeeName: String, fullLink: String) -> Unit
) {
    // ... (Keep existing implementation logic)

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(modifier = modifier, factory = { ctx ->
        PreviewView(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }, update = { previewView ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also {
                    it.setAnalyzer(cameraExecutor, QrCodeAnalyzer { url ->
                        if (url.startsWith("upi://pay")) {
                            try {
                                val uri = url.toUri()
                                val pa = uri.getQueryParameter("pa") ?: ""
                                val pn = uri.getQueryParameter("pn") ?: ""
                                // Use Main thread to update UI
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    onQrDetected(pa, pn, url)
                                }
                            } catch (e: Exception) {
                                Log.e("ScanQrScreen", "Error parsing UPI URL", e)
                            }
                        } else {
                            // Optional: Show invalid QR message (debounced)
                        }
                    })
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("ScanQrScreen", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    })
}

class QrCodeAnalyzer(private val onQrDetected: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient()

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image).addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let {
                        onQrDetected(it)
                    }
                }
            }.addOnFailureListener {
                // Task failed with an exception
            }.addOnCompleteListener {
                imageProxy.close()
            }
        } else {
            imageProxy.close()
        }
    }
}