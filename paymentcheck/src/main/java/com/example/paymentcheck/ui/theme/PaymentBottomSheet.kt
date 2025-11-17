package com.example.paymentcheck.ui.theme


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
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
import com.checkout.components.interfaces.model.PaymentSessionResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ✅ Add your Checkout SDK imports

class PaymentBottomSheet : BottomSheetDialogFragment() {
    private lateinit var checkoutComponents: CheckoutComponents
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var isComponentValid by mutableStateOf(false)
    private var id: String? = null
    private var paymentSessionToken: String? = null
    private var paymentSessionSecret: String? = null
    private var publicKey: String? = null
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Safely retrieve arguments
        arguments?.let {
            id = it.getString("id")
            paymentSessionToken = it.getString("paymentSessionToken")
            paymentSessionSecret = it.getString("paymentSessionSecret")
            publicKey = it.getString("publicKey")
            email = it.getString("email")
        }

        Log.d("PaymentBottomSheet", "Received ID: $id")
        Log.d("PaymentBottomSheet", "Received Token: $paymentSessionToken")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val composeView = ComposeView(requireContext())
        composeView.setContent {
            PaymentScreen()
        }
        return composeView
    }

    @Composable
    private fun PaymentScreen() {
        var flow by remember { mutableStateOf<PaymentMethodComponent?>(null) }
        var selectedMethod by remember { mutableStateOf("") }
        var paymentSessionID by remember { mutableStateOf(arguments?.getString("id") ?: "") }
        var paymentSessionSecret by remember { mutableStateOf(arguments?.getString("paymentSessionSecret") ?: "") }
        var publicKey by remember { mutableStateOf(arguments?.getString("publicKey") ?: "") }
        var email by remember { mutableStateOf(arguments?.getString("email") ?: "") }

        val payButtonEnabled = isComponentValid

        LaunchedEffect(selectedMethod) {
            if (selectedMethod == "card") {
                val (options, config) = createConfigs(paymentSessionID, paymentSessionSecret, email, publicKey)
                withContext(Dispatchers.IO) {
                    try {
                        checkoutComponents = CheckoutComponentsFactory(config = config).create()
                        flow = checkoutComponents.create(ComponentName.Flow, options)
                        isComponentValid = flow?.isValid() ?: false
                    } catch (checkoutError: CheckoutError) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Error: ${checkoutError.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                LaunchedEffect(Unit) {
                    if (paymentSessionID.isNotBlank() && paymentSessionSecret.isNotBlank()) {
                        selectedMethod = "card"
                    }
                }
            }

            item {
                if (flow != null) {
                    flow?.Render()
                    Button(
                        enabled = payButtonEnabled,
                        onClick = { uiScope.launch { flow?.submit() } },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) { Text("Pay Now") }
                }
            }
        }
    }

    private fun createConfigs(
        paymentSessionID: String, paymentSessionSecret: String, email: String, publicKey: String
    ): Pair<ComponentOption, CheckoutComponentConfiguration> {
        val rememberMeConfiguration = RememberMeConfiguration(
            data = RememberMeConfiguration.Data(email = email),
            showPayButton = false
        )
        val componentOption = ComponentOption(showPayButton = false, rememberMeConfiguration = rememberMeConfiguration)
        val config = CheckoutComponentConfiguration(
            context = requireContext(),
            paymentSession = PaymentSessionResponse(
                id = paymentSessionID,
                secret = paymentSessionSecret
            ),
            componentCallback = customComponentCallback,
            publicKey = publicKey,
            environment = Environment.SANDBOX
        )
        return componentOption to config
    }

    private val customComponentCallback = ComponentCallback(
        onReady = { component -> uiScope.launch { isComponentValid = component.isValid() } },
        onChange = { component -> uiScope.launch { isComponentValid = component.isValid() } },
        onSubmit = { component -> Log.d("PaymentBottomSheet", "onSubmit: ${component.name}") },
        onSuccess = { component, paymentID ->
            Toast.makeText(requireContext(), "Success: $paymentID", Toast.LENGTH_SHORT).show()
            dismiss() // close after success
        },
        onError = { _, checkoutError ->
            Toast.makeText(requireContext(), "Error: ${checkoutError.message}", Toast.LENGTH_LONG)
                .show()
        }
    )
}
