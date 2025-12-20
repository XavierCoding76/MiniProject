package com.example.miniproject.payment

import android.content.Context
import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.goyumapp.payment.PayPalRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayPalWebViewScreen(
    orderId: String,
    approvalUrl: String,
    facilityIndId: String,
    userId: String,
    equipmentData: String,
    startTime: Long,
    bookedHours: Double,  // CHANGED: Accept bookedHours instead of endTime
    navController: NavController
) {
    var isLoading by remember { mutableStateOf(true) }
    var showCaptureProgress by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    val paypalRepository = remember { PayPalRepository() }
    val paymentViewModel: PaymentViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Initialize ViewModel with booking data
    LaunchedEffect(Unit) {
        // Convert Long timestamps to Firebase Timestamp
        val startTimestamp = com.google.firebase.Timestamp(startTime / 1000, ((startTime % 1000) * 1000000).toInt())

        // Parse equipment data into list format
        val equipmentList = if (equipmentData.isNotEmpty()) {
            equipmentData.split(",").filter { it.isNotEmpty() }
        } else {
            emptyList()
        }

        // Load data into ViewModel
        paymentViewModel.loadPaymentData(
            userId = userId,
            facilityIndId = facilityIndId,
            equipmentData = equipmentList,
            startTime = startTimestamp,
            bookedHours = bookedHours  // CHANGED
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PayPal Payment") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Show loading indicator
            if (isLoading || showCaptureProgress) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (showCaptureProgress)
                                "Processing payment and saving booking..."
                            else
                                "Loading PayPal..."
                        )
                    }
                }
            }

            // WebView
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        val webView = this

                        // Clear cookies before setting up
                        clearPayPalCookies(context)

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            javaScriptCanOpenWindowsAutomatically = true
                            setSupportZoom(true)
                            builtInZoomControls = false

                            // IMPORTANT: Disable caching for fresh sessions
                            cacheMode = WebSettings.LOAD_NO_CACHE
                            setCacheMode(WebSettings.LOAD_NO_CACHE)

                            // For newer Android versions, disable cache with these settings
                            setCacheMode(WebSettings.LOAD_NO_CACHE)
                            databasePath = "" // Clear database path

                            // Enable cookies but we'll manage them
                            CookieManager.getInstance().apply {
                                setAcceptCookie(true)
                                setAcceptThirdPartyCookies(webView, true)
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                android.util.Log.d("PayPal", "Navigating to: $url")

                                // Check for PayPal approval URLs
                                when {
                                    // PayPal returns to your app after approval
                                    url?.contains("checkoutnow?token") == true ||
                                            url?.contains("webapps/hermes?flow=") == true ||
                                            (url?.contains("token=") == true && url.contains("paypal.com")) -> {

                                        android.util.Log.d("PayPal", "✅ PayPal approval page loaded")
                                        return false
                                    }

                                    // PayPal return URL (when user completes payment)
                                    url?.contains("return") == true ||
                                            url?.contains("payment/success") == true ||
                                            (url?.contains("paypal.com") == true && url.contains("status=completed")) -> {

                                        if (!isCapturing) {
                                            android.util.Log.d("PayPal", "✅ Payment COMPLETED on PayPal - Capturing...")
                                            showCaptureProgress = true
                                            isCapturing = true

                                            // Capture the payment
                                            scope.launch {
                                                try {
                                                    val captureResult = paypalRepository.captureOrder(orderId)

                                                    if (captureResult.isSuccess) {
                                                        val capture = captureResult.getOrNull()!!
                                                        android.util.Log.d("PayPal", "✅ Payment captured: ${capture.captureId}")

                                                        // ✅ CREATE BOOKING IN FIREBASE
                                                        paymentViewModel.createBookingAfterPayment(
                                                            paypalCaptureId = capture.captureId,
                                                            onSuccess = { paymentId, reservationId ->
                                                                android.util.Log.d("PayPal", "✅ Booking created successfully!")
                                                                android.util.Log.d("PayPal", "   Payment ID: $paymentId")
                                                                android.util.Log.d("PayPal", "   Reservation ID: $reservationId")
                                                                android.util.Log.d("PayPal", "   PayPal Capture ID: ${capture.captureId}")

                                                                // Navigate to success screen
                                                                navController.navigate("booking_success") {
                                                                    popUpTo("home") { inclusive = true }
                                                                }
                                                            },
                                                            onError = { errorMessage ->
                                                                android.util.Log.e("PayPal", "❌ Failed to create booking: $errorMessage")
                                                                // Still navigate to success since PayPal payment succeeded
                                                                navController.navigate("booking_success") {
                                                                    popUpTo("home") { inclusive = true }
                                                                }
                                                            }, userId
                                                        )

                                                    } else {
                                                        android.util.Log.e("PayPal", "❌ Capture failed: ${captureResult.exceptionOrNull()?.message}")
                                                        clearPayPalCookies(context)
                                                        navController.navigate("booking_failed")
                                                    }
                                                } catch (e: Exception) {
                                                    android.util.Log.e("PayPal", "❌ Error: ${e.message}")
                                                    clearPayPalCookies(context)
                                                    navController.navigate("booking_failed")
                                                } finally {
                                                    isCapturing = false
                                                }
                                            }
                                        }
                                        return true
                                    }

                                    // Cancel - user cancelled payment
                                    url?.contains("payment/cancel") == true ||
                                            url?.contains("cancel") == true -> {
                                        android.util.Log.d("PayPal", "❌ Payment CANCELLED")
                                        navController.navigateUp()
                                        return true
                                    }

                                    // Generic error page from PayPal
                                    url?.contains("genericError") == true -> {
                                        android.util.Log.e("PayPal", "❌ PayPal error page detected")
                                        clearPayPalCookies(context)
                                        navController.navigate("booking_failed")
                                        return true
                                    }

                                    // Allow all other PayPal URLs (login, etc.)
                                    else -> {
                                        return false
                                    }
                                }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                android.util.Log.d("PayPal", "✅ Page loaded: $url")
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                android.util.Log.d("PayPal", "⏳ Loading: $url")
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                errorCode: Int,
                                description: String?,
                                failingUrl: String?
                            ) {
                                super.onReceivedError(view, errorCode, description, failingUrl)
                                android.util.Log.e("PayPal", "❌ Error: $description for URL: $failingUrl")
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(msg: android.webkit.ConsoleMessage?): Boolean {
                                android.util.Log.d("PayPal-Console", "${msg?.message()}")
                                return true
                            }

                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                android.util.Log.d("PayPal", "Loading: $newProgress%")
                            }
                        }

                        // Load the PayPal approval URL
                        val timestamp = System.currentTimeMillis()
                        val urlWithCacheBust = if (approvalUrl.contains("?")) {
                            "$approvalUrl&timestamp=$timestamp&useraction=PAY_NOW"
                        } else {
                            "$approvalUrl?timestamp=$timestamp&useraction=PAY_NOW"
                        }
                        android.util.Log.d("PayPal", "🌐 Loading URL: $urlWithCacheBust")
                        loadUrl(urlWithCacheBust)
                    }
                }
            )
        }
    }
}

/**
 * Clear all PayPal cookies and cache
 */
fun clearPayPalCookies(context: Context) {
    try {
        // Clear all cookies
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()

        // Clear WebView cache
        WebView(context).apply {
            clearCache(true)
            clearHistory()
        }

        // Clear application cache
        context.cacheDir?.deleteRecursively()

        android.util.Log.d("PayPalUtils", "✅ PayPal cookies and cache cleared")
    } catch (e: Exception) {
        android.util.Log.e("PayPalUtils", "Error clearing PayPal cookies: ${e.message}")
    }
}