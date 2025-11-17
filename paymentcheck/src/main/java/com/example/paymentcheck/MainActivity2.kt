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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
//import com.checkout.android_sdk.Components.Options.ComponentOptions


class MainActivity2 : ComponentActivity() {

    private lateinit var checkoutComponents: CheckoutComponents
    private lateinit var coordinator: GooglePayFlowCoordinator

    // 1. Define a CoroutineScope tied to the main thread for safe UI state updates
    private val uiScope = CoroutineScope(Dispatchers.Main)

    // State variable to hold the component's validity. Accessible to the Composable.
    private var isComponentValid by mutableStateOf(false)
//    pk_ifvf4pa3yvd574selka2ieh3nm6
    val config = null
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
            var envValue = false
            userData?.let {
                id = it["id"]!!
                paymentSessionToken = it["paymentSessionToken"]!!
                paymentSessionSecret = it["paymentSessionSecret"]!!
                publicKey = it["publicKey"]!!
                email = it["email"]!!
                envValue = it["env"] as? Boolean ?: false


            }
           renderContent(id,paymentSessionToken,paymentSessionSecret,publicKey,email,envValue)
           //renderContentV2(id,paymentSessionToken,paymentSessionSecret,publicKey,email)
           // testcons(id,paymentSessionToken,paymentSessionSecret,publicKey,email)

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
            isComponentValid = true
            Log.e("flow component error", "${checkoutError.message}, ${checkoutError.code}")
        },
    )


    private fun createConfigs(
        paymentSessionID: String,
        paymentSessionSecret: String,
        email: String,
        publicKey: String,
        envValue: Boolean,
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

        coordinator = GooglePayFlowCoordinator(
            context = this@MainActivity2,
            handleActivityResult = { resultCode, data ->
                checkoutComponents?.handleActivityResult(resultCode, data)
                Log.d("GooglePay", "Activity result received: $resultCode")
            }
        )

        val flowCoordinators = mapOf(
            PaymentMethodName.GooglePay to coordinator!!
        )


        val config: CheckoutComponentConfiguration

        if (envValue) {
            config = CheckoutComponentConfiguration(
                context = this,
                paymentSession = PaymentSessionResponse(
                    id = paymentSessionID,
                    secret = paymentSessionSecret,
                ),
                componentCallback = customComponentCallback,
                publicKey = publicKey,
                flowCoordinators = flowCoordinators,
                environment = Environment.PRODUCTION
            )
        } else {
            config = CheckoutComponentConfiguration(
                context = this,
                paymentSession = PaymentSessionResponse(
                    id = paymentSessionID,
                    secret = paymentSessionSecret,
                ),
                componentCallback = customComponentCallback,
                publicKey = publicKey,
                flowCoordinators = flowCoordinators,
                environment = Environment.SANDBOX
            )
        }

// Return non-nullable Pair
        return componentOption to config
    }


    private fun renderContent(
        id: String,
        paymentSessionToken: String,
        paymentSessionSecret1: String,
        publicKey: String,
        email: String,
        envValue: Boolean
    ) {
        setContent {
            var flow by remember { mutableStateOf<PaymentMethodComponent?>(null) }
            var selectedMethod by remember { mutableStateOf("") }
            var paymentSessionID by remember { mutableStateOf(id) }
            var paymentSessionSecret by remember { mutableStateOf(paymentSessionSecret1) }
            var isCardAvailable by remember { mutableStateOf(false) }
            var isLoading by remember { mutableStateOf(false) }
            var payButtonEnabledd by remember { mutableStateOf(true) }

            // Use the class-level state for button enabling
            var payButtonEnabled = isComponentValid


            LaunchedEffect(selectedMethod) {
                if (selectedMethod == "card") {

                    val (options, config) = createConfigs(paymentSessionID, paymentSessionSecret,email,publicKey,envValue)
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            checkoutComponents = CheckoutComponentsFactory(config = config).create()
                          //  ComponentName.Flow
//                            flow = checkoutComponents.create(
//                                PaymentMethodName.Card,
//                                options
//                            )
                            flow = checkoutComponents.create(
                                ComponentName.Flow,
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
                            payButtonEnabled = true
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


            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(16.dp),

                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center

//                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // ✅ EditText/TextField ہٹا دیے گئے، اب manually values set ہونگی
                paymentSessionID = paymentSessionID
                paymentSessionSecret = paymentSessionSecret

                // ✅ یہ والا بٹن خودکار طریقے سے کال ہوگا بغیر کلک کے
                item {
                    LaunchedEffect(Unit) {
                        if (paymentSessionID.isNotBlank() && paymentSessionSecret.isNotBlank()) {
                            selectedMethod = "card"
                        } else {
                            Log.e("flow component error", "Please enter Payment Session ID and Secret")
                        }
                    }
                }

                // ✅ Card Component خود render ہو جائے گا
                item {
                    when (selectedMethod) {
                        "card" -> {
                            if (isCardAvailable) {
                                flow?.Render()

                                Spacer(modifier = Modifier.height(20.dp))

                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .padding(top = 16.dp)
                                    )
                                } else {
                                    Button(
                                        enabled = payButtonEnabled,
                                        onClick = {
                                            uiScope.launch {
                                                isLoading = true          // show loader
                                                payButtonEnabled = false  // disable button

                                                try {
                                                    flow?.submit()        // proceed payment
                                                } finally {
                                                   // isLoading = false     // hide loader
                                                   // payButtonEnabled = true // enable button again
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp)
                                    ) {
                                        Text("Pay Now")
                                    }
                                }
                            }



//                            if (isCardAvailable) {
//
//                                flow?.Render()
//
//                                Spacer(modifier = Modifier.height(20.dp))
//
//                                if (isLoading) {
//                                    // Show Loader instead of button
//                                    CircularProgressIndicator(
//                                        modifier = Modifier
//                                            .size(40.dp)
//                                            .padding(top = 16.dp)
//                                    )
//                                } else {
//                                    Button(
//                                        enabled = payButtonEnabled,
//                                        onClick = {
//                                            uiScope.launch {
//                                                isLoading = true  // Start loader
//                                                flow?.submit()   // Proceed payment
//                                                isLoading = false // Stop loader (you can move this to callback for accuracy)
//                                            }
//                                        },
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(top = 16.dp)
//                                    ) {
//                                        Text("Pay Now")
//                                    }
//                                }
//                            }










                            //


//                            if (isCardAvailable) {
//                                flow?.Render()
//
//                                Button(
//                                    enabled = payButtonEnabled,
//                                    onClick = {
//                                        uiScope.launch {
//                                            flow?.submit()
//                                        }
//                                    },
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(top = 16.dp)
//                                ) {
//                                    Text("Pay Now")
//                                }
//                            }
                        }
                    }
                }
            }


        }
    }



}