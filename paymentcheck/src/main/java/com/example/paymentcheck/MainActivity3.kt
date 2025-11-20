package com.example.paymentcheck

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.checkout.components.core.CheckoutComponentsFactory
import com.checkout.components.interfaces.Environment
import com.checkout.components.interfaces.api.CheckoutComponents
import com.checkout.components.interfaces.api.PaymentMethodComponent
import com.checkout.components.interfaces.component.CheckoutComponentConfiguration
import com.checkout.components.interfaces.component.ComponentCallback
import com.checkout.components.interfaces.component.ComponentOption
import com.checkout.components.interfaces.component.RememberMeConfiguration
import com.checkout.components.interfaces.error.CheckoutError
import com.checkout.components.interfaces.model.ComponentName
import com.checkout.components.interfaces.model.PaymentMethodName
import com.checkout.components.interfaces.model.PaymentSessionResponse
import com.checkout.components.wallet.wrapper.GooglePayFlowCoordinator
import com.example.paymentcheck.OnDataPass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity3 : ComponentActivity() {

    private lateinit var checkoutComponents: CheckoutComponents
    private lateinit var coordinator: GooglePayFlowCoordinator

    // CoroutineScope for UI
    private val uiScope = CoroutineScope(Dispatchers.Main)

    // Activity-level state for Compose
    private var isComponentValid by mutableStateOf(false)
    private var isLoading by mutableStateOf(false)
    private var payButtonEnabled by mutableStateOf(false)

    internal var itemClickedLocation: OnDataPass = object : OnDataPass {
        override fun onDataSend(data: HashMap<String, Any>) {}
        override fun onITemClick(data: String) {
            val resultIntent = Intent()
            resultIntent.putExtra("paymentId", data)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    // Custom Component Callback
    private val customComponentCallback = ComponentCallback(
        onReady = { component ->
            uiScope.launch {
                isComponentValid = component.isValid()
                payButtonEnabled = isComponentValid
            }
        },
        onChange = { component ->
            uiScope.launch {
                isComponentValid = component.isValid()
                payButtonEnabled = isComponentValid
            }
        },
        onSubmit = { component ->
            Log.d("flow component", "onSubmit ${component.name}")
        },
        onSuccess = { component, paymentID ->
            isLoading = false
            payButtonEnabled = true
            itemClickedLocation.onITemClick(paymentID)
        },
        onError = { component, checkoutError ->
            isLoading = false
            payButtonEnabled = true
            isComponentValid = false
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity3,
                    "Error: ${checkoutError.message}, ${checkoutError.code}",
                    Toast.LENGTH_LONG
                ).show()
            }

            Log.e("flow component error", "${checkoutError.message}, ${checkoutError.code}")
        }
    )

    private fun createConfigs(
        paymentSessionID: String,
        paymentSessionSecret: String,
        email: String,
        publicKey: String,
        envValue: Boolean
    ): Pair<ComponentOption, CheckoutComponentConfiguration> {

        val rememberMeConfiguration = RememberMeConfiguration(
            data = RememberMeConfiguration.Data(email = email),
            showPayButton = false
        )

        val componentOption = ComponentOption(
            showPayButton = false,
            rememberMeConfiguration = rememberMeConfiguration
        )

        coordinator = GooglePayFlowCoordinator(
            context = this@MainActivity3,
            handleActivityResult = { resultCode, data ->
                checkoutComponents?.handleActivityResult(resultCode, data)
            }
        )

        val flowCoordinators = mapOf(PaymentMethodName.GooglePay to coordinator!!)

        val config = CheckoutComponentConfiguration(
            context = this,
            paymentSession = PaymentSessionResponse(
                id = paymentSessionID,
                secret = paymentSessionSecret
            ),
            componentCallback = customComponentCallback,
            publicKey = publicKey,
            flowCoordinators = flowCoordinators,
            environment = if (envValue) Environment.PRODUCTION else Environment.SANDBOX

        )
//
        return componentOption to config
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userData = intent.getSerializableExtra("userData") as? HashMap<String, String>
        var id = ""
        var paymentSessionToken = ""
        var paymentSessionSecret = ""
        var publicKey = ""
        var email = ""
        var envValue = false

        userData?.let {
            id = it["id"]!!
            paymentSessionToken = it["paymentSessionToken"]!!
            paymentSessionSecret = it["paymentSessionSecret"]!!
            publicKey = it["publicKey"]!!
            email = it["email"]!!
            envValue = it["env"] as? Boolean ?: false
        }

        renderContent(id, paymentSessionToken, paymentSessionSecret, publicKey, email, envValue)
    }

    private fun renderContent(
        paymentSessionID: String,
        paymentSessionToken: String,
        paymentSessionSecret: String,
        publicKey: String,
        email: String,
        envValue: Boolean
    ) {
        setContent {
            var flow by remember { mutableStateOf<PaymentMethodComponent?>(null) }
            var selectedMethod by remember { mutableStateOf("") }
            var isCardAvailable by remember { mutableStateOf(false) }
            var showLogo by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                delay(3000)   // ⬅️ delay time (you can increase/decrease)
                showLogo = true
            }
            // Auto-select card if session available
            LaunchedEffect(Unit) {
                if (paymentSessionID.isNotBlank() && paymentSessionSecret.isNotBlank()) {
                    selectedMethod = "card"
                }
            }

            LaunchedEffect(selectedMethod) {
                if (selectedMethod == "card") {
                    val (options, config) = createConfigs(
                        paymentSessionID,
                        paymentSessionSecret,
                        email,
                        publicKey,
                        envValue
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            checkoutComponents = CheckoutComponentsFactory(config).create()
                            flow = checkoutComponents.create(ComponentName.Flow, options)
                            isCardAvailable = flow?.isAvailable() ?: false

                            val isValidStatus = flow?.isValid() ?: false
                            isComponentValid = isValidStatus
                            payButtonEnabled = isComponentValid

                        } catch (checkoutError: CheckoutError) {
                            isComponentValid = false
                            payButtonEnabled = false
                            Log.e(
                                "flow component error",
                                "Initialization error: ${checkoutError.message}, Details: ${checkoutError.details}"
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                item {

                    if (showLogo) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_ezhire_big_logo),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 26.dp)
                                .height(60.dp)
                        )
                    }
                    if (selectedMethod == "card" && isCardAvailable) {
                        flow?.Render()
                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            enabled = payButtonEnabled && !isLoading,
                            onClick = {
                                uiScope.launch {
                                    isLoading = true
                                    payButtonEnabled = false
                                    flow?.submit() // async
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text("Pay Now")
                            }
                        }
                    }
                }
            }
        }
    }
}
