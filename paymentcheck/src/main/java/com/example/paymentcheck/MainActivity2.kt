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
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.checkout.components.interfaces.model.PaymentMethodName
import com.checkout.components.interfaces.model.PaymentSessionResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity2 : ComponentActivity() {

    private lateinit var checkoutComponents: CheckoutComponents

    // 1. Define a CoroutineScope tied to the main thread for safe UI state updates
    private val uiScope = CoroutineScope(Dispatchers.Main)

    // State variable to hold the component's validity. Accessible to the Composable.
    private var isComponentValid by mutableStateOf(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MainScope().launch {
            val userData = intent.getSerializableExtra("userData") as? HashMap<String, String>
            var id = ""
            var paymentSessionToken = ""
            var paymentSessionSecret = ""
            var publicKey = ""
            var email = ""
            userData?.let {
                id = it["id"]!!
                paymentSessionToken = it["paymentSessionToken"]!!
                paymentSessionSecret = it["paymentSessionSecret"]!!
                publicKey = it["publicKey"]!!
                email = it["email"]!!


            }
        //    checkoutWithGoogleV2(id,paymentSessionToken,paymentSessionSecret,publicKey)
            renderContent(id,paymentSessionToken,paymentSessionSecret,publicKey,email)

            // CheckoutFuctionImplement(id,paymentSessionToken,paymentSessionSecret,publicKey)
            // testlayout(id,paymentSessionToken,paymentSessionSecret,publicKey)


        }
    }
    internal var itemClickedLocation: OnDataPass = object : OnDataPass {
        override fun onDataSend(data: HashMap<String, Any>) {

        }

        override fun onITemClick(data: String) {
            print(data)
            val resultIntent = Intent()
            resultIntent.putExtra("paymentId", data)
            setResult(RESULT_OK, resultIntent)
            finish()
        }

    }
    private val customComponentCallback = ComponentCallback(
        onReady = { component ->
            Log.d("flow component", "onReady ${component.name}")
            // Must launch a coroutine to call the suspend function isValid()
            uiScope.launch {
                try {
                    isComponentValid = component.isValid()
                    Log.d("flow component", "onReady - isValid: $isComponentValid")
                } catch (e: Exception) {
                    Log.e("flow component error", "Error checking initial validity: ${e.message}")
                    isComponentValid = false
                }
            }
        },
        // Use onChange to react to user input changes
        onChange = { component ->
            // Must launch a coroutine to call the suspend function isValid()
            uiScope.launch {
                try {
                    // Update the state based on the suspend function result
                    isComponentValid = component.isValid()
                    Log.d("flow component", "onChange - isValid: $isComponentValid")
                } catch (e: Exception) {
                    Log.e("flow component error", "Error checking validity: ${e.message}")
                    isComponentValid = false
                }
            }
        },
        onSubmit = { component ->
            Log.d("flow component", "onSubmit ${component.name}")
        },
        onSuccess = { component, paymentID ->
            Log.d("flow component success", "${component.name}: $paymentID")
            itemClickedLocation.onITemClick(paymentID)
        },
        onError = { component, checkoutError ->
            Toast.makeText(this@MainActivity2, "Initialization error: ${checkoutError.message}, Details: ${checkoutError.code}", Toast.LENGTH_LONG).show()

            Log.e("flow component error", "${checkoutError.message}, ${checkoutError.code}")
        },
    )


    private fun createConfigs(
        paymentSessionID: String,
        paymentSessionSecret: String,
        email: String,
    ): Pair<ComponentOption, CheckoutComponentConfiguration> {
        val rememberMeConfiguration = RememberMeConfiguration(
            data = RememberMeConfiguration.Data(
                email = email,
            ),
            // 2. Set showPayButton to false
            showPayButton = false
        )

        val componentOption = ComponentOption(
            // 2. Set showPayButton to false
            showPayButton = false,
            rememberMeConfiguration = rememberMeConfiguration
        )

        val config = CheckoutComponentConfiguration(
            context = this,
            paymentSession = PaymentSessionResponse(
                id = paymentSessionID,
                secret = paymentSessionSecret,
            ),
            componentCallback = customComponentCallback,
            publicKey = "pk_sbox_awubbtkehjl742o3t5v44vngcyu",
            environment = Environment.SANDBOX
        )

        return componentOption to config
    }

    private fun renderContent(
        id: String,
        paymentSessionToken: String,
        paymentSessionSecret1: String,
        publicKey: String,
        email: String
    ) {
        setContent {
            var flow by remember { mutableStateOf<PaymentMethodComponent?>(null) }
            var selectedMethod by remember { mutableStateOf("") }
            var paymentSessionID by remember { mutableStateOf(id) }
            var paymentSessionSecret by remember { mutableStateOf(paymentSessionSecret1) }
            var isCardAvailable by remember { mutableStateOf(false) }

            // Use the class-level state for button enabling
            val payButtonEnabled = isComponentValid


            LaunchedEffect(selectedMethod) {
                if (selectedMethod == "card") {
                    val (options, config) = createConfigs(paymentSessionID, paymentSessionSecret,email)
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            checkoutComponents = CheckoutComponentsFactory(config = config).create()

                            flow = checkoutComponents.create(
                                PaymentMethodName.Card,
                                options
                            )
                            isCardAvailable = flow?.isAvailable() ?: false

                            // 3. FIX: Call the suspend function isValid() inside the coroutine
                            val isValidStatus = flow?.isValid() ?: false
                            isComponentValid = isValidStatus // Update the shared state

                            Log.e(
                                "flow component isValid???",
                                "flow component isValid???: $isValidStatus"
                            )

                        } catch (checkoutError: CheckoutError) {
                            runOnUiThread {
                                Toast.makeText(this@MainActivity2, "Initialization error: ${checkoutError.message}, Details: ${checkoutError.details}", Toast.LENGTH_LONG).show()

                            }

                            Log.e(
                                "flow component error",
                                "Initialization error: ${checkoutError.message}, Details: ${checkoutError.details}"
                            )
                        }
                    }
                }
            }
            if (paymentSessionID.isNotBlank() && paymentSessionSecret.isNotBlank()) {
                selectedMethod = "card"
            } else {
                Log.e("flow component error", "Please enter Payment Session ID and Secret")
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(8.dp)
            ) {

//                item {
//                    OutlinedTextField(
//                        value = paymentSessionID, label = { Text("Payment Session ID") },
//                        onValueChange = { paymentSessionID = it },
//                        modifier = Modifier.fillParentMaxWidth()
//                    )
//                }
//
//                item {
//                    OutlinedTextField(
//                        value = paymentSessionSecret, label = { Text("Payment Session Secret") },
//                        onValueChange = { paymentSessionSecret = it },
//                        modifier = Modifier.fillParentMaxWidth()
//                    )
//                }

//                item {
//                    Row {
//                        Button(
//                            enabled = paymentSessionID.isNotBlank() && paymentSessionSecret.isNotBlank(),
//                            onClick = {
//                                if (paymentSessionID.isEmpty() || paymentSessionSecret.isEmpty()) {
//                                    Log.e("flow component error", "Please enter Payment Session ID and Secret")
//                                    return@Button
//                                } else {
//                                    selectedMethod = "card"
//                                }
//                            },
//                        ) {
//                            Text("Render Card Component")
//                        }
//                    }
//                }

                item {
                    when (selectedMethod) {
                        "card" -> {
                            if (isCardAvailable) {
                                // Render the component for user input
                                flow?.Render()

                                // 4. Custom "Pay Now" Button
//                                Button(
//                                    // Button is enabled only when the component is valid (isComponentValid is true)
//                                    enabled = payButtonEnabled,
//                                    onClick = {
//                                        // Trigger component submission (this will call onSubmit)
//                                        uiScope.launch {
//                                            flow?.submit()
//                                        }
//                                    },
//                                    modifier = Modifier.padding(top = 16.dp)
//                                ) {

                                Button(
                                    enabled = payButtonEnabled,
                                    onClick = {
                                        uiScope.launch {
                                            flow?.submit()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth() // full width
                                        .padding(top = 16.dp)
                                ) {
                                    Text("Pay Now")
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}