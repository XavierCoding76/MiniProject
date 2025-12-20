package com.example.goyumapp.payment

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await



// ============================================
// DATA CLASSES FOR PAYPAL RESPONSES
// ============================================
data class PayPalOrderResponse(
    val success: Boolean,
    val orderId: String,
    val approvalUrl: String,
    val status: String
)

data class PayPalCaptureResponse(
    val success: Boolean,
    val status: String,
    val orderId: String,
    val captureId: String,
    val amount: String,
    val currency: String,
    val captureTime: String
)

// ============================================
// PAYPAL REPOSITORY
// ============================================
class PayPalRepository {
    private val functions: FirebaseFunctions = Firebase.functions

    /**
     * Create a PayPal order
     * @param amount The payment amount (e.g., "37.00")
     * @param currency Currency code (default: "USD")
     * @param description Order description (default: "GoYum Order")
     * @return PayPalOrderResponse with orderId and approvalUrl
     */
    suspend fun createOrder(
        amount: String,
        currency: String = "USD",
        description: String = "GoYum Order"
    ): Result<PayPalOrderResponse> {
        return try {
            val data = hashMapOf(
                "amount" to amount,
                "currency" to currency,
                "description" to description
            )

            val result = functions
                .getHttpsCallable("createPayPalOrder")
                .call(data)
                .await()

            val response = result.getData() as? Map<*, *>

            if (response != null && response["success"] == true) {
                Result.success(
                    PayPalOrderResponse(
                        success = true,
                        orderId = response["orderId"] as String,
                        approvalUrl = response["approvalUrl"] as String,
                        status = response["status"] as String
                    )
                )
            } else {
                Result.failure(Exception("Failed to create PayPal order"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Capture a PayPal payment after user approval
     * @param orderId The PayPal order ID
     * @return PayPalCaptureResponse with capture details
     */
    suspend fun captureOrder(orderId: String): Result<PayPalCaptureResponse> {
        return try {
            val data = hashMapOf("orderId" to orderId)

            val result = functions
                .getHttpsCallable("capturePayPalOrder")
                .call(data)
                .await()

            val response = result.getData() as? Map<*, *>

            if (response != null && response["success"] == true) {
                Result.success(
                    PayPalCaptureResponse(
                        success = true,
                        status = response["status"] as String,
                        orderId = response["orderId"] as String,
                        captureId = response["captureId"] as String,
                        amount = response["amount"] as String,
                        currency = response["currency"] as String,
                        captureTime = response["captureTime"] as String
                    )
                )
            } else {
                Result.failure(Exception("Failed to capture payment"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get order details (optional - for verification)
     * @param orderId The PayPal order ID
     * @return Order details map
     */
    suspend fun getOrderDetails(orderId: String): Result<Map<*, *>> {
        return try {
            val data = hashMapOf("orderId" to orderId)

            val result = functions
                .getHttpsCallable("getPayPalOrderDetails")
                .call(data)
                .await()

            val response = result.getData() as? Map<*, *>

            if (response != null && response["success"] == true) {
                Result.success(response["orderDetails"] as Map<*, *>)
            } else {
                Result.failure(Exception("Failed to get order details"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ============================================
// USAGE EXAMPLE IN YOUR VIEWMODEL OR SCREEN
// ============================================
/*
class PaymentViewModel : ViewModel() {
    private val paypalRepository = PayPalRepository()

    fun processPayPalPayment(amount: String) {
        viewModelScope.launch {
            // Step 1: Create order
            val orderResult = paypalRepository.createOrder(amount)

            if (orderResult.isSuccess) {
                val order = orderResult.getOrNull()!!

                // Step 2: Open approval URL in WebView
                // User approves payment on PayPal
                openPayPalWebView(order.approvalUrl)

                // Step 3: After approval, capture the payment
                val captureResult = paypalRepository.captureOrder(order.orderId)

                if (captureResult.isSuccess) {
                    val capture = captureResult.getOrNull()!!
                    // Payment successful!
                    onPaymentSuccess(capture.captureId)
                } else {
                    // Payment failed
                    onPaymentFailure(captureResult.exceptionOrNull())
                }
            }
        }
    }
}
*/