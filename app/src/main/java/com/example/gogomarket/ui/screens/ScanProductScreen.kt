package com.example.gogomarket.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanProductScreen(
    scanType: String,
    viewModel: CourierViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var hasCamPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCamPermission = granted
            if (!granted) {
                Toast.makeText(context, "Разрешение на камеру необходимо для сканирования", Toast.LENGTH_LONG).show()
                navController.popBackStack()
            }
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCamPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    // ✅ Определяем текст подсказки в зависимости от типа сканирования
    val instructionText = when (scanType) {
        "warehouse", "product" -> "Наведите камеру на штрихкод товара"
        "confirm_delivery" -> "Наведите камеру на QR-код клиента"
        else -> "Наведите камеру на код"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Сканирование") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                // ✅ Улучшаем цвета для контраста
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.7f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        // ✅ Убираем фон по умолчанию у Scaffold, чтобы камера была видна
        containerColor = Color.Black
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
        ) {
            if (hasCamPermission) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { context ->
                            val previewView = PreviewView(context)
                            val cameraExecutor = Executors.newSingleThreadExecutor()

                            val analysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .apply {
                                    setAnalyzer(cameraExecutor) { imageProxy ->
                                        processImageProxy(
                                            imageProxy = imageProxy,
                                            onBarcodeScanned = { qrOrBarcode ->
                                                this.clearAnalyzer()
                                                cameraProviderFuture.get().unbindAll()

                                                when (scanType) {
                                                    "warehouse" -> {
                                                        viewModel.takeOrderFromWarehouse(qrOrBarcode) { success, message ->
                                                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                            navController.popBackStack()
                                                        }
                                                    }
                                                    "confirm_delivery" -> {
                                                        viewModel.confirmOrderDelivery(qrOrBarcode) { success, message ->
                                                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                            navController.popBackStack()
                                                        }
                                                    }
                                                    "product" -> {
                                                        viewModel.scanProductBarcode(qrOrBarcode) { success, message ->
                                                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                            navController.popBackStack()
                                                        }
                                                    }
                                                    else -> {
                                                        Toast.makeText(context, "Неизвестный тип сканирования: $scanType", Toast.LENGTH_LONG).show()
                                                        navController.popBackStack()
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }

                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        analysis
                                    )
                                } catch (e: Exception) {
                                    Log.e("ScanProductScreen", "Ошибка привязки камеры", e)
                                }
                            }, ContextCompat.getMainExecutor(context))

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // ✅ Заменяем старый текст на новый интерфейс сканера
                    ScannerOverlay(instructionText = instructionText)
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Нет разрешения на использование камеры.")
                }
            }
        }
    }
}

// ✅ Новый Composable для рисования оверлея
@Composable
private fun ScannerOverlay(instructionText: String) {
    // Слой для рисования рамки и фона
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val rectSize = size.width * 0.7f
        val rectTopLeft = Offset((canvasWidth - rectSize) / 2, canvasHeight * 0.25f)

        // Рисуем полупрозрачный фон
        drawRect(Color.Black.copy(alpha = 0.7f))

        // "Вырезаем" прозрачное окно в центре
        drawRoundRect(
            topLeft = rectTopLeft,
            size = Size(rectSize, rectSize),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            color = Color.Transparent,
            blendMode = BlendMode.Clear
        )

        // Рисуем белую рамку вокруг окна
        drawRoundRect(
            topLeft = rectTopLeft,
            size = Size(rectSize, rectSize),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            color = Color.White,
            style = Stroke(width = 2.dp.toPx())
        )
    }
    // Слой для текста, чтобы он был поверх всего
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Располагаем текст ниже центра "окна" сканера
        Text(
            text = instructionText,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 300.dp) // Сдвигаем текст вниз
        )
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImageProxy(imageProxy: ImageProxy, onBarcodeScanned: (String) -> Unit) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let { barcodeValue ->
                    onBarcodeScanned(barcodeValue)
                }
            }
            .addOnFailureListener {
                Log.e("ScanProductScreen", "Ошибка распознавания ML Kit", it)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}