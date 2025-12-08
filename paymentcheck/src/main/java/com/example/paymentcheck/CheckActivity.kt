package com.example.paymentcheck

import android.app.Dialog
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentBottomSheet(
    private val userDataGooglePay2: HashMap<String, String>,
    private val onPaymentSuccess: (String, Boolean) -> Unit
) : BottomSheetDialogFragment() {
    private var GooglePayEnabled = true

    private lateinit var checkoutComponents: CheckoutComponents
    private lateinit var coordinator: GooglePayFlowCoordinator

    private var isComponentValid by mutableStateOf(false)
    private var isLoading by mutableStateOf(false)
    private var payButtonEnabled by mutableStateOf(false)

    private val uiScope = CoroutineScope(Dispatchers.Main)

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

            if (component.name == PaymentMethodName.GooglePay) {
                Log.d("flow component", "Goo")
                GooglePayEnabled = true
                // Your GooglePay logic here
            }

            dismiss()
        },
        onSuccess = { component, paymentID ->
            isLoading = false
            payButtonEnabled = false
            onPaymentSuccess(paymentID,GooglePayEnabled)     // callback to Activity
            dismiss()
        },
        onError = { component, checkoutError ->
            isLoading = false
            payButtonEnabled = false
            isComponentValid = false

            Toast.makeText(
                requireContext(),
                "Error: ${checkoutError.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    )


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme)
    }

    companion object {
        private const val ARG_USER_DATA = "userData"
        lateinit var userDataGooglePay: HashMap<String, String>


    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                PaymentBottomSheetContent(userDataGooglePay)
            }
        }
    }


    // -----------------------------------------
    //  BOTTOM SHEET COMPOSE UI CONTENT
    // -----------------------------------------
    @Composable
    fun PaymentBottomSheetContent(userData: HashMap<String, String>) {

        val paymentSessionID = userData["id"] ?: ""
        val paymentSessionSecret = userData["paymentSessionSecret"] ?: ""
        val publicKey = userData["publicKey"] ?: ""
        val email = userData["email"] ?: ""
        val envValue = userData["env"] ?: ""

        var flow by remember { mutableStateOf<PaymentMethodComponent?>(null) }
        var selectedMethod by remember { mutableStateOf("") }
        var isCardAvailable by remember { mutableStateOf(false) }
        var showLogo by remember { mutableStateOf(false) }
        var isFlowUIReady by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(2000)
            showLogo = true
            selectedMethod = "card"
        }

        // INITIALIZE FLOW
        LaunchedEffect(selectedMethod) {
            if (selectedMethod == "card") {
                val (options, config) = createConfigs(
                    paymentSessionID,
                    paymentSessionSecret,
                    email,
                    publicKey,
                    envValue
                )

                withContext(Dispatchers.IO) {
                    runCatching {
                        checkoutComponents = CheckoutComponentsFactory(config).create()
                        flow = checkoutComponents.create(PaymentMethodName.GooglePay, options)
                    }.onSuccess {
                        withContext(Dispatchers.Main) {
                            isCardAvailable = flow?.isAvailable() ?: false
                            isComponentValid = flow?.isValid() ?: false
                            payButtonEnabled = isComponentValid
                            isFlowUIReady = true
                        }
                    }.onFailure {
                        withContext(Dispatchers.Main) {
                            isFlowUIReady = true
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // CLOSE ICON
            IconButton(
                onClick = { dismiss() },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Close"
                )
            }

            if (showLogo) {
                Image(
                    painter = painterResource(id = R.drawable.ic_ezhire_big_logo),
                    contentDescription = null,
                    modifier = Modifier.height(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            when {
                !isFlowUIReady -> {
                    CircularProgressIndicator(Modifier.size(50.dp))
                }

                selectedMethod == "card" && isCardAvailable -> {
                    flow?.Render()

                    Spacer(Modifier.height(20.dp))


                }
            }
        }
    }


    // -----------------------------------------
    // CREATE CONFIGS SAME AS YOUR ACTIVITY CODE
    // -----------------------------------------
    private fun createConfigs(
        paymentSessionID: String,
        paymentSessionSecret: String,
        email: String,
        publicKey: String,
        envValue: String
    ): Pair<ComponentOption, CheckoutComponentConfiguration> {

        val rememberMeConfig = RememberMeConfiguration(
            data = RememberMeConfiguration.Data(email = email),
            showPayButton = false
        )

        val componentOption = ComponentOption(
            showPayButton = false,
            rememberMeConfiguration = rememberMeConfig
        )

        coordinator = GooglePayFlowCoordinator(
            context = requireContext(),
            handleActivityResult = { resultCode, data ->
                checkoutComponents.handleActivityResult(resultCode, data)
            }
        )

        val flowCoordinators = mapOf(PaymentMethodName.GooglePay to coordinator)

        val config = CheckoutComponentConfiguration(
            context = requireContext(),
            paymentSession = PaymentSessionResponse(
                id = paymentSessionID,
                secret = paymentSessionSecret
            ),
            componentCallback = customComponentCallback,
            publicKey = publicKey,
            flowCoordinators = flowCoordinators,
            environment = if (envValue == "true" || envValue == "1") {
                Environment.PRODUCTION
            } else {
                Environment.SANDBOX
            }
        )

        return componentOption to config
    }
}

