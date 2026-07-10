package com.turnzero.games.yugioh.tools.rulings

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

private const val TAG = "TZ/RulingsOCR"

private object ScanZone {
    const val WIDTH_FRACTION = 0.56f
    const val CARD_RATIO = 3.5f / 2.5f
    const val TOP_FRACTION = 0.20f
    val LEFT_FRACTION get() = (1f - WIDTH_FRACTION) / 2f
}

private object CardNameRegion {
    const val LEFT_FRACTION = 0.22f
    const val TOP_FRACTION = 0.20f
    const val RIGHT_FRACTION = 0.64f
    const val BOTTOM_FRACTION = 0.32f
}

@Composable
fun RulingsOcrScreen(
    selectedFormat: String,
    onTextRecognized: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    DisposableEffect(lifecycleOwner) {
        onDispose { cameraExecutor.shutdown() }
    }

    if (!hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Camera permission is required for card scanning")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = androidx.camera.core.Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()
                        imageCapture = capture
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture,
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Camera bind failed", e)
                            errorMessage = "Failed to start camera"
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
        )

        if (!isProcessing && errorMessage == null) {
            ScanZoneOverlay(modifier = Modifier.fillMaxSize())
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).statusBarsPadding(),
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onPrimary)
        }

        if (isProcessing) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Identifying card...", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }

        if (!isProcessing && errorMessage == null) {
            FilledIconButton(
                onClick = {
                    val capture = imageCapture ?: return@FilledIconButton
                    isProcessing = true
                    capture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            processCapture(image, isProcessing = { isProcessing = false }) { text ->
                                isProcessing = false
                                onTextRecognized(text)
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            isProcessing = false
                            errorMessage = "Failed to capture image"
                        }
                    })
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp).size(72.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(Icons.Default.Camera, contentDescription = "Scan card", modifier = Modifier.size(36.dp))
            }
        }

        if (errorMessage != null) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text(errorMessage ?: "", color = MaterialTheme.colorScheme.onPrimary, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(onClick = { errorMessage = null }) { Text("Retry") }
                            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                        }
                    }
                }
            }
        }
    }
}

private fun processCapture(
    image: ImageProxy,
    isProcessing: () -> Unit,
    onTextRecognized: (String) -> Unit,
) {
    val bitmap = image.toJpegBitmap()
    image.close()

    val preprocessed = bitmap.toGrayscaleEnhanced()
    bitmap.recycle()

    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun tryRegion(cropped: Bitmap?): String? {
        if (cropped == null) return null
        val inputImage = InputImage.fromBitmap(cropped, 0)
        return try {
            val visionText = com.google.android.gms.tasks.Tasks.await(recognizer.process(inputImage))
            val blocks = visionText.textBlocks.filter { it.text.isNotBlank() }
            val scoringBlock = blocks.maxByOrNull { block ->
                val heights = block.lines.flatMap { it.elements }
                    .mapNotNull { it.boundingBox?.height() }
                if (heights.isEmpty()) -1f
                else heights.sorted()[heights.size / 2].toFloat()
            }
            scoringBlock?.text?.takeIf { it.isNotBlank() } ?: visionText.text.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Log.w(TAG, "ML Kit processing failed", e)
            null
        }
    }

    val nameBitmap = preprocessed.cropToCardNameRegion()
    val nameText = tryRegion(nameBitmap)
    nameBitmap?.recycle()

    if (!nameText.isNullOrBlank()) {
        onTextRecognized(nameText)
        preprocessed.recycle()
        return
    }

    val fullText = tryRegion(preprocessed)
    preprocessed.recycle()

    if (!fullText.isNullOrBlank()) {
        onTextRecognized(fullText)
    } else {
        isProcessing()
    }
}

private fun ImageProxy.toJpegBitmap(): Bitmap {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

private fun Bitmap.toGrayscaleEnhanced(): Bitmap {
    val contrast = 1.8f
    val offset = 255f * (1f - contrast) / 2f
    val cm = ColorMatrix(floatArrayOf(
        contrast, 0f, 0f, 0f, offset,
        0f, contrast, 0f, 0f, offset,
        0f, 0f, contrast, 0f, offset,
        0f, 0f, 0f, 1f, 0f,
    ))
    val grayMatrix = ColorMatrix().apply { setSaturation(0f) }
    cm.postConcat(grayMatrix)

    val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(cm) }
    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    Canvas(result).drawBitmap(this, 0f, 0f, paint)
    return result
}

private fun Bitmap.cropToCardNameRegion(): Bitmap? {
    val left = (width * CardNameRegion.LEFT_FRACTION).toInt().coerceAtLeast(0)
    val top = (height * CardNameRegion.TOP_FRACTION).toInt().coerceAtLeast(0)
    val right = (width * CardNameRegion.RIGHT_FRACTION).toInt().coerceAtMost(width)
    val bottom = (height * CardNameRegion.BOTTOM_FRACTION).toInt().coerceAtMost(height)
    val cropWidth = right - left
    val cropHeight = bottom - top
    if (cropWidth <= 0 || cropHeight <= 0) return null
    return Bitmap.createBitmap(this, left, top, cropWidth, cropHeight)
}

@Composable
private fun ScanZoneOverlay(modifier: Modifier = Modifier) {
    val scrimColor = Color.Black.copy(alpha = 0.55f)
    val accentColor = Color.White.copy(alpha = 0.9f)
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val helperText = "Position card here"
    val bracketLength = with(density) { 24.dp.toPx() }
    val bracketThickness = with(density) { 3.dp.toPx() }

    ComposeCanvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val zoneWidth = canvasWidth * ScanZone.WIDTH_FRACTION
        val zoneHeight = zoneWidth * ScanZone.CARD_RATIO
        val zoneLeft = (canvasWidth - zoneWidth) / 2f
        val zoneTop = canvasHeight * ScanZone.TOP_FRACTION

        val zoneRect = Rect(zoneLeft, zoneTop, zoneLeft + zoneWidth, zoneTop + zoneHeight)

        drawRect(color = scrimColor)

        val cutoutPath = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(Offset.Zero, Size(canvasWidth, canvasHeight)))
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    zoneRect,
                    CornerRadius(12f, 12f),
                ),
            )
        }
        drawPath(cutoutPath, Color.Black.copy(alpha = 0.55f))

        val stroke = Stroke(width = bracketThickness)
        val l = bracketLength

        drawLine(accentColor, Offset(zoneRect.left - 6f, zoneRect.top - 6f), Offset(zoneRect.left + l - 6f, zoneRect.top - 6f), stroke.width)
        drawLine(accentColor, Offset(zoneRect.left - 6f, zoneRect.top - 6f), Offset(zoneRect.left - 6f, zoneRect.top + l - 6f), stroke.width)

        drawLine(accentColor, Offset(zoneRect.right + 6f, zoneRect.top - 6f), Offset(zoneRect.right - l + 6f, zoneRect.top - 6f), stroke.width)
        drawLine(accentColor, Offset(zoneRect.right + 6f, zoneRect.top - 6f), Offset(zoneRect.right + 6f, zoneRect.top + l - 6f), stroke.width)

        drawLine(accentColor, Offset(zoneRect.left - 6f, zoneRect.bottom + 6f), Offset(zoneRect.left + l - 6f, zoneRect.bottom + 6f), stroke.width)
        drawLine(accentColor, Offset(zoneRect.left - 6f, zoneRect.bottom + 6f), Offset(zoneRect.left - 6f, zoneRect.bottom - l + 6f), stroke.width)

        drawLine(accentColor, Offset(zoneRect.right + 6f, zoneRect.bottom + 6f), Offset(zoneRect.right - l + 6f, zoneRect.bottom + 6f), stroke.width)
        drawLine(accentColor, Offset(zoneRect.right + 6f, zoneRect.bottom + 6f), Offset(zoneRect.right + 6f, zoneRect.bottom - l + 6f), stroke.width)

        val textLayout = textMeasurer.measure(
            text = helperText,
            style = TextStyle(
                color = accentColor.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        drawText(
            textLayoutResult = textLayout,
            topLeft = Offset(
                (canvasWidth - textLayout.size.width) / 2f,
                zoneRect.bottom + 16f,
            ),
        )
    }
}
