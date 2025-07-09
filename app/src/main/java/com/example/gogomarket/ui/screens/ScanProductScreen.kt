// ScanProductScreen.kt
package com.example.gogomarket.ui.screens

import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.gogomarket.viewmodel.CourierViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ScanProductScreen(
    scanType: String,
    viewModel: CourierViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    // Используем Accompanist для управления разрешением
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    // Этот блок запускается один раз, чтобы запросить разрешение
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Сканирование") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black.copy(alpha = 0.7f), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (cameraPermissionState.status.isGranted) {
                // Разрешение есть - показываем камеру
                CameraView(
                    scanType = scanType,
                    viewModel = viewModel,
                    navController = navController
                )
            } else {
                // Разрешения нет - показываем объяснение
                PermissionDeniedView(
                    showRationale = cameraPermissionState.status.shouldShowRationale,
                    onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                )
            }
        }
    }
}

@Composable
private fun CameraView(
    scanType: String,
    viewModel: CourierViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    val instructionText = when (scanType) {
        "warehouse", "product" -> "Наведите камеру на штрихкод товара"
        "confirm_delivery" -> "Наведите камеру на QR-код клиента"
        else -> "Наведите камеру на код"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraExecutor = Executors.newSingleThreadExecutor()
                val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().apply {
                    setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy = imageProxy) { qrOrBarcode ->
                            this.clearAnalyzer()
                            cameraProviderFuture.get().unbindAll()
                            when (scanType) {
                                "warehouse" -> viewModel.takeOrderFromWarehouse(qrOrBarcode) { s, m -> handleScanResult(context, navController, m) }
                                "confirm_delivery" -> viewModel.confirmOrderDelivery(qrOrBarcode) { s, m -> handleScanResult(context, navController, m) }
                                "product" -> viewModel.scanProductBarcode(qrOrBarcode) { s, m -> handleScanResult(context, navController, m) }
                                else -> {
                                    Toast.makeText(context, "Неизвестный тип сканирования: $scanType", Toast.LENGTH_LONG).show()
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                    } catch (e: Exception) {
                        Log.e("ScanProductScreen", "Ошибка привязки камеры", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        ScannerOverlay(instructionText = instructionText)
    }
}

private fun handleScanResult(context: android.content.Context, navController: NavController, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    navController.popBackStack()
}

@Composable
private fun PermissionDeniedView(showRationale: Boolean, onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (showRationale) "Для сканирования кодов нужен доступ к камере. Пожалуйста, предоставьте разрешение." else "Доступ к камере необходим. Откройте настройки приложения, чтобы предоставить разрешение.",
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text("Запросить разрешение")
        }
    }
}

@Composable
private fun ScannerOverlay(instructionText: String) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width; val canvasHeight = size.height; val rectSize = size.width * 0.7f
        val rectTopLeft = Offset((canvasWidth - rectSize) / 2, canvasHeight * 0.25f)
        drawRect(Color.Black.copy(alpha = 0.7f))
        drawRoundRect(topLeft = rectTopLeft, size = Size(rectSize, rectSize), cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()), color = Color.Transparent, blendMode = BlendMode.Clear)
        drawRoundRect(topLeft = rectTopLeft, size = Size(rectSize, rectSize), cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()), color = Color.White, style = Stroke(width = 2.dp.toPx()))
    }
    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
        Text(text = instructionText, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 300.dp))
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImageProxy(imageProxy: ImageProxy, onBarcodeScanned: (String) -> Unit) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes -> barcodes.firstOrNull()?.rawValue?.let { barcodeValue -> onBarcodeScanned(barcodeValue) } }
            .addOnFailureListener { Log.e("ScanProductScreen", "Ошибка распознавания ML Kit", it) }
            .addOnCompleteListener { imageProxy.close() }
    }
}