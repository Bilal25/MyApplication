//package com.example.myapplication
//
//
//import android.os.Bundle
//import android.widget.FrameLayout
//import androidx.activity.viewModels
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.lifecycleScope
//import androidx.lifecycle.viewModelScope
//import com.checkout.components.core.CheckoutComponentsFactory
//import com.checkout.components.interfaces.Environment
//import com.checkout.components.interfaces.component.CheckoutComponent
//import com.checkout.components.interfaces.component.CheckoutComponentConfiguration
//import com.checkout.components.interfaces.component.ComponentCallback
//import com.checkout.components.interfaces.error.CheckoutError
//import com.checkout.components.interfaces.model.ComponentName
//import com.checkout.components.interfaces.model.PaymentSessionResponse
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//
//// --- ViewModel ---
//class CheckoutViewModel : androidx.lifecycle.ViewModel() {
//
//    private val _component = MutableStateFlow<CheckoutComponent?>(null)
//    val component: StateFlow<CheckoutComponent?> = _component
//
//    fun createCheckoutComponent(activityContext: AppCompatActivity, paymentSession: PaymentSessionResponse, publicKey: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val configuration = CheckoutComponentConfiguration(
//                context = activityContext,
//                paymentSession = paymentSession,
//                publicKey = publicKey,
//                environment = Environment.SANDBOX,
//                componentCallback = ComponentCallback(
//                    onReady = { component -> println("onReady: ${component.name}") },
//                    onSubmit = { component -> println("onSubmit: ${component.name}") },
//                    onSuccess = { component, paymentId -> println("onSuccess: ${component.name} - $paymentId") },
//                    onError = { component, error -> println("onError: ${component.name} - $error") }
//                )
//            )
//
//            try {
//                val checkoutComponents = CheckoutComponentsFactory(config = configuration).create()
//                val flowComponent = checkoutComponents.create(ComponentName.Flow) // Use Flow for Card+GooglePay
//                _component.update { flowComponent }
//            } catch (checkoutError: CheckoutError) {
//                println("Checkout error: $checkoutError")
//            }
//        }
//    }
//}
//
//// --- Activity ---
//
