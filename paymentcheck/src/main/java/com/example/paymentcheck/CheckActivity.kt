package com.example.paymentcheck

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.checkout.components.core.CheckoutComponentsFactory
import com.checkout.components.interfaces.Environment
import com.checkout.components.interfaces.api.CheckoutComponents
import com.checkout.components.interfaces.component.CheckoutComponentConfiguration
import com.checkout.components.interfaces.component.ComponentCallback
import com.checkout.components.interfaces.component.ComponentOption
import com.checkout.components.interfaces.component.FlowCoordinator
import com.checkout.components.interfaces.error.CheckoutError
import com.checkout.components.interfaces.model.ComponentName
import com.checkout.components.interfaces.model.PaymentMethodName
import com.checkout.components.interfaces.model.PaymentSessionResponse
import com.checkout.components.wallet.wrapper.GooglePayFlowCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class CheckActivity : AppCompatActivity(),OnDataPass {
    private lateinit var coordinator: GooglePayFlowCoordinator
    var  paymentMethodSupportedList: List<String> = listOf("card", "googlepay")
    private val googlePayFlowCoordinator = MutableStateFlow<FlowCoordinator?>(null)
     var checkoutComponents: CheckoutComponents? = null

    private lateinit var containerView: FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        enableEdgeToEdge()
        setContentView(R.layout.activity_check)


        ///val bottomSheetLayout: View = findViewById(R.id.bottomSheetLayout)
        containerView = findViewById(R.id.checkoutContainer)
      //  val titleText: TextView = bottomSheetLayout.findViewById(R.id.titleText)


//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }


        MainScope().launch {
            val userData = intent.getSerializableExtra("userData") as? HashMap<String, String>
            var id = ""
            var paymentSessionToken = ""
            var paymentSessionSecret = ""
            var publicKey = ""
            userData?.let {
                 id = it["id"]!!
                 paymentSessionToken = it["paymentSessionToken"]!!
                 paymentSessionSecret = it["paymentSessionSecret"]!!
                 publicKey = it["publicKey"]!!
            }
            checkoutWithGoogle(id,paymentSessionToken,paymentSessionSecret,publicKey)
             // CheckoutFuctionImplement(id,paymentSessionToken,paymentSessionSecret,publicKey)
           // testlayout(id,paymentSessionToken,paymentSessionSecret,publicKey)
        }

    }


    private suspend fun checkoutWithGoogle(
        id: String,
        paymentSessionToken: String,
        paymentSessionSecret: String,
        publicKey: String
    ) {
        try {


// Map of components to their specific configurations

        // ✅ Initialize the coordinator
        coordinator = GooglePayFlowCoordinator(
            context = this@CheckActivity,
            handleActivityResult = { resultCode, data ->
                checkoutComponents?.handleActivityResult(resultCode, data)
                Log.d("GooglePay", "Activity result received: $resultCode ")
            }
        )

        val flowCoordinators = mapOf(
            PaymentMethodName.GooglePay to coordinator
        )

        val configuration = CheckoutComponentConfiguration(
            context = this@CheckActivity,
            paymentSession = PaymentSessionResponse(
                id = id,
                paymentSessionToken = paymentSessionToken,
                paymentSessionSecret = paymentSessionSecret
            ),

            publicKey = publicKey,
            environment = Environment.SANDBOX,
            flowCoordinators =
                flowCoordinators,
             componentCallback = ComponentCallback(
                onReady = { component ->
                    Log.d("Checkout", "onReady: ${component.name}")
                },
                onSubmit = { component ->
                    Log.d("Checkout", "onSubmit: ${component.name}")
                },
                onSuccess = { component, paymentId ->
                    itemClickedLocation.onITemClick(paymentId)
                    Log.d("Checkout", "✅ onSuccess: ${component.name} - $paymentId")
                },
                onError = { component, checkoutError ->
                    Toast.makeText(this@CheckActivity,"Please Checkout Payment Failed", Toast.LENGTH_LONG).show()
                    Log.e("Checkout", "❌ onError ${component.name}: $checkoutError")
                }
            ),

           // flowCoordinators = flowCoordinators
        )


            checkoutComponents = CheckoutComponentsFactory(config = configuration).create()
             val cardComponent = checkoutComponents?.create(ComponentName.Flow)
                //val cardComponent = checkoutComponents.create(PaymentMethodName.Card)
           //  val googlePayComponent = checkoutComponents.create(PaymentMethodName.GooglePay)

        withContext(Dispatchers.Main) {
            containerView.removeAllViews()
            containerView.addView(cardComponent!!.provideView(containerView))



           // containerView.addView(googlePayComponent.provideView(containerView))

           // val googlePayView = googlePayComponent.provideView(containerView)
           // Log.d("GooglePay", "Provided view: $googlePayComponent")

//            withContext(Dispatchers.Main) {
//                val googlePayView = googlePayComponent.provideView(containerView)
//                googlePayView?.layoutParams = LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//                )
//
//                containerView.removeAllViews()
//                containerView.addView(googlePayView)
//            }


            Log.d("Checkout", "✅ Card & Google Pay views added")
        }
        }catch (e : Exception){
            e.stackTrace
        }

    }




    private fun CheckoutFuctionImplement(
        id: String,
        paymentSessionToken: String,
        paymentSessionSecret: String,
        publicKey: String
    ) {
        val containerView = findViewById<FrameLayout>(R.id.checkoutContainer)

        val configuration = CheckoutComponentConfiguration(
            context = this,
            paymentSession = PaymentSessionResponse(
                id = id,
                paymentSessionToken = paymentSessionToken,
                paymentSessionSecret = paymentSessionSecret,
            ),
            publicKey = "pk_sbox_awubbtkehjl742o3t5v44vngcyu",
            environment = Environment.SANDBOX,
                )








        CoroutineScope(Dispatchers.IO).launch {
            try {
                // --- STEP 1: Create Configuration ---
                val configuration = CheckoutComponentConfiguration(
                    context = this@CheckActivity,
                    paymentSession = PaymentSessionResponse(
                        id = id,
                        paymentSessionToken = paymentSessionToken,
                        paymentSessionSecret = paymentSessionSecret,
                    ),
                    publicKey = "pk_sbox_awubbtkehjl742o3t5v44vngcyu",
                    environment = Environment.SANDBOX,
                    componentCallback = ComponentCallback(
                        onReady = { component ->
                            println("onReady: ${component.name}")
                        },
                        onSubmit = { component ->
                            println("onSubmit: ${component.name}")
                        },
                        onSuccess = { component, paymentId ->
                            println("✅ onSuccess: ${component.name} - $paymentId")

                            // STEP 3 — handle callback manually here:
                            val callbackUrl = "https://yourbackend.com/callback?payment_id=$paymentId"
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val response = URL(callbackUrl).readText()
                                    Log.d("CheckoutCallback", "Callback response: $response")
                                } catch (e: Exception) {
                                    Log.e("CheckoutCallback", "Callback failed", e)
                                }
                            }
                        },
                        onError = { component, checkoutError ->
                            Log.e("Checkout", "❌ onError ${component.name}: $checkoutError")
                        },
                    )
                )

                // --- STEP 2: Create Component Factory ---
                val checkoutComponents = CheckoutComponentsFactory(config = configuration).create()
                val cardComponent = checkoutComponents.create(PaymentMethodName.Card)

                // --- STEP 3: Add UI on Main Thread ---
                withContext(Dispatchers.Main) {
                    val cardView = cardComponent.provideView(containerView)
                    containerView.removeAllViews()
                    containerView.addView(cardView)
                    Log.d("Checkout", "✅ Card component view added")
                }

            } catch (checkoutError: CheckoutError) {
                Log.e("CheckoutError", "Error creating Checkout component", checkoutError)
            }
        }


//        CoroutineScope(Dispatchers.IO).launch {
//
//            try {
//                val checkoutComponents = CheckoutComponentsFactory(config = configuration).create()
//                val flow = checkoutComponents.create(PaymentMethodName.Card)
//                launch(Dispatchers.Main) {
//                    flow.provideView(containerView)
//
//                    Log.d("Checkout", "Checkout Flow view provided")
//                    containerView.addView(TextView(this@MainActivity).apply {
//                        text = "Checkout Card UI yahan appear hoti"
//                        gravity = Gravity.CENTER
//                        textSize = 18f
//                    })
//                }
//                val configuration = CheckoutComponentConfiguration(
//                    context = this,
//                    paymentSession = PaymentSessionResponse(
//                        id = "ps_34Pi9dRvt2SA4YqhbODm7lCiS78",
//                        paymentSessionToken = "YmFzZTY0:eyJpZCI6InBzXzM0UGk5ZFJ2dDJTQTRZcWhiT0RtN2xDaVM3OCIsImVudGl0eV9pZCI6ImVudF83NWN5dm8yZ2c0NGVmYjZkb3drajRkbmdzcSIsImV4cGVyaW1lbnRzIjp7fSwicHJvY2Vzc2luZ19jaGFubmVsX2lkIjoicGNfem5weWhjZHF2bGh1bmo3MjJpN3pncm9vaWkiLCJhbW91bnQiOjEwMCwibG9jYWxlIjoiZW4tR0IiLCJjdXJyZW5jeSI6IkFFRCIsInBheW1lbnRfbWV0aG9kcyI6W3sidHlwZSI6ImNhcmQiLCJjYXJkX3NjaGVtZXMiOlsiVmlzYSIsIk1hc3RlcmNhcmQiLCJBbWV4Il0sInNjaGVtZV9jaG9pY2VfZW5hYmxlZCI6ZmFsc2UsInN0b3JlX3BheW1lbnRfZGV0YWlscyI6ImRpc2FibGVkIiwiYmlsbGluZ19hZGRyZXNzIjp7ImNpdHkiOiJEdWJhaSIsImNvdW50cnkiOiJBRSJ9fSx7InR5cGUiOiJhcHBsZXBheSIsImRpc3BsYXlfbmFtZSI6ImVaaGlyZSIsImNvdW50cnlfY29kZSI6IkdCIiwiY3VycmVuY3lfY29kZSI6IkFFRCIsIm1lcmNoYW50X2NhcGFiaWxpdGllcyI6WyJzdXBwb3J0czNEUyJdLCJzdXBwb3J0ZWRfbmV0d29ya3MiOlsidmlzYSIsIm1hc3RlckNhcmQiLCJhbWV4Il0sInRvdGFsIjp7ImxhYmVsIjoiZVpoaXJlIiwidHlwZSI6ImZpbmFsIiwiYW1vdW50IjoiMSJ9fSx7InR5cGUiOiJnb29nbGVwYXkiLCJtZXJjaGFudCI6eyJpZCI6IjA4MTEzMDg5Mzg2MjY4ODQ5OTgyIiwibmFtZSI6ImVaaGlyZSIsIm9yaWdpbiI6Imh0dHBzOi8vd3d3LmV6aGlyZS5tZSJ9LCJ0cmFuc2FjdGlvbl9pbmZvIjp7InRvdGFsX3ByaWNlX3N0YXR1cyI6IkZJTkFMIiwidG90YWxfcHJpY2UiOiIxIiwiY291bnRyeV9jb2RlIjoiR0IiLCJjdXJyZW5jeV9jb2RlIjoiQUVEIn0sImNhcmRfcGFyYW1ldGVycyI6eyJhbGxvd2VkX2F1dGhfbWV0aG9kcyI6WyJQQU5fT05MWSIsIkNSWVBUT0dSQU1fM0RTIl0sImFsbG93ZWRfY2FyZF9uZXR3b3JrcyI6WyJWSVNBIiwiTUFTVEVSQ0FSRCIsIkFNRVgiXX19XSwiZmVhdHVyZV9mbGFncyI6WyJhbmFseXRpY3Nfb2JzZXJ2YWJpbGl0eV9lbmFibGVkIiwiY2FyZF9maWVsZHNfZW5hYmxlZCIsImdldF93aXRoX3B1YmxpY19rZXlfZW5hYmxlZCIsImxvZ3Nfb2JzZXJ2YWJpbGl0eV9lbmFibGVkIiwicmlza19qc19lbmFibGVkIiwidXNlX25vbl9iaWNfaWRlYWxfaW50ZWdyYXRpb24iXSwicmlzayI6eyJlbmFibGVkIjpmYWxzZX0sIm1lcmNoYW50X25hbWUiOiJlWmhpcmUiLCJwYXltZW50X3Nlc3Npb25fc2VjcmV0IjoicHNzXzBiYjdiMjBmLWFmODgtNDAzOS04YWYzLWUyOGQxYjI2OTk3OSIsInBheW1lbnRfdHlwZSI6IlJlZ3VsYXIiLCJpbnRlZ3JhdGlvbl9kb21haW4iOiJkZXZpY2VzLmFwaS5zYW5kYm94LmNoZWNrb3V0LmNvbSJ9",
//                        paymentSessionSecret = "pss_0bb7b20f-af88-4039-8af3-e28d1b269979",
//                    ),
//                    publicKey = "pk_sbox_awubbtkehjl742o3t5v44vngcyu",
//                    environment = Environment.SANDBOX,
//
//
////                val configuration = CheckoutComponentConfiguration(
////                    context = this@MainActivity,
////                    paymentSession = PaymentSessionResponse(
////                        id = "ps_34Pi9dRvt2SA4YqhbODm7lCiS78",
////                        paymentSessionToken = "YmFzZTY0:eyJpZCI6InBzXzM0UGk5ZFJ2dDJTQTRZcWhiT0RtN2xDaVM3OCIsImVudGl0eV9pZCI6ImVudF83NWN5dm8yZ2c0NGVmYjZkb3drajRkbmdzcSIsImV4cGVyaW1lbnRzIjp7fSwicHJvY2Vzc2luZ19jaGFubmVsX2lkIjoicGNfem5weWhjZHF2bGh1bmo3MjJpN3pncm9vaWkiLCJhbW91bnQiOjEwMCwibG9jYWxlIjoiZW4tR0IiLCJjdXJyZW5jeSI6IkFFRCIsInBheW1lbnRfbWV0aG9kcyI6W3sidHlwZSI6ImNhcmQiLCJjYXJkX3NjaGVtZXMiOlsiVmlzYSIsIk1hc3RlcmNhcmQiLCJBbWV4Il0sInNjaGVtZV9jaG9pY2VfZW5hYmxlZCI6ZmFsc2UsInN0b3JlX3BheW1lbnRfZGV0YWlscyI6ImRpc2FibGVkIiwiYmlsbGluZ19hZGRyZXNzIjp7ImNpdHkiOiJEdWJhaSIsImNvdW50cnkiOiJBRSJ9fSx7InR5cGUiOiJhcHBsZXBheSIsImRpc3BsYXlfbmFtZSI6ImVaaGlyZSIsImNvdW50cnlfY29kZSI6IkdCIiwiY3VycmVuY3lfY29kZSI6IkFFRCIsIm1lcmNoYW50X2NhcGFiaWxpdGllcyI6WyJzdXBwb3J0czNEUyJdLCJzdXBwb3J0ZWRfbmV0d29ya3MiOlsidmlzYSIsIm1hc3RlckNhcmQiLCJhbWV4Il0sInRvdGFsIjp7ImxhYmVsIjoiZVpoaXJlIiwidHlwZSI6ImZpbmFsIiwiYW1vdW50IjoiMSJ9fSx7InR5cGUiOiJnb29nbGVwYXkiLCJtZXJjaGFudCI6eyJpZCI6IjA4MTEzMDg5Mzg2MjY4ODQ5OTgyIiwibmFtZSI6ImVaaGlyZSIsIm9yaWdpbiI6Imh0dHBzOi8vd3d3LmV6aGlyZS5tZSJ9LCJ0cmFuc2FjdGlvbl9pbmZvIjp7InRvdGFsX3ByaWNlX3N0YXR1cyI6IkZJTkFMIiwidG90YWxfcHJpY2UiOiIxIiwiY291bnRyeV9jb2RlIjoiR0IiLCJjdXJyZW5jeV9jb2RlIjoiQUVEIn0sImNhcmRfcGFyYW1ldGVycyI6eyJhbGxvd2VkX2F1dGhfbWV0aG9kcyI6WyJQQU5fT05MWSIsIkNSWVBUT0dSQU1fM0RTIl0sImFsbG93ZWRfY2FyZF9uZXR3b3JrcyI6WyJWSVNBIiwiTUFTVEVSQ0FSRCIsIkFNRVgiXX19XSwiZmVhdHVyZV9mbGFncyI6WyJhbmFseXRpY3Nfb2JzZXJ2YWJpbGl0eV9lbmFibGVkIiwiY2FyZF9maWVsZHNfZW5hYmxlZCIsImdldF93aXRoX3B1YmxpY19rZXlfZW5hYmxlZCIsImxvZ3Nfb2JzZXJ2YWJpbGl0eV9lbmFibGVkIiwicmlza19qc19lbmFibGVkIiwidXNlX25vbl9iaWNfaWRlYWxfaW50ZWdyYXRpb24iXSwicmlzayI6eyJlbmFibGVkIjpmYWxzZX0sIm1lcmNoYW50X25hbWUiOiJlWmhpcmUiLCJwYXltZW50X3Nlc3Npb25fc2VjcmV0IjoicHNzXzBiYjdiMjBmLWFmODgtNDAzOS04YWYzLWUyOGQxYjI2OTk3OSIsInBheW1lbnRfdHlwZSI6IlJlZ3VsYXIiLCJpbnRlZ3JhdGlvbl9kb21haW4iOiJkZXZpY2VzLmFwaS5zYW5kYm94LmNoZWNrb3V0LmNvbSJ9",
////                        paymentSessionSecret = "pss_0bb7b20f-af88-4039-8af3-e28d1b269979"
////                    ),
//                    publicKey = "",
//                    environment = Environment.SANDBOX,
//                    callbackUrl = CALLBACK_URL,
//                    flowCoordinators = mapOf(
//                        PaymentMethodName.GooglePay to googlePayFlowCoordinator
//                    ),
//                    componentCallback = ComponentCallback(
//                        onReady = { component ->
//                            println("onReady: ${component.name}")
//                        },
//                        onSubmit = { component ->
//                            println("onSubmit: ${component.name}")
//                        },
//                        onSuccess = { component, paymentId ->
//                            println("onSuccess: ${component.name} - $paymentId")
//                        },
//                        onError = { component, checkoutError ->
//                            println("onError ${component.name}: $checkoutError")
//                        },
//                    ),
//                    componentOptions = TODO(),
//                    locale = TODO(),
//                    translations = TODO(),
//                    appearance = TODO(),
//                )
//
////                val configuration = CheckoutComponentConfiguration(
////                    context = this@MainActivity,
////                    paymentSession = PaymentSessionResponse(
////                        id = "",
////                        paymentSessionToken = "",
////                        paymentSessionSecret = "",
////                    ),
////                    publicKey = "",
////                    environment = Environment.SANDBOX,
////                    callbackUrl = "", // 👈 Add this line
////                    flowCoordinators = googlePayFlowCoordinator.value?.let {
////                        mapOf(PaymentMethodName.GooglePay to it)
////                    } ?: emptyMap(),
////                    componentCallback = ComponentCallback(
////                        onReady = { component ->
////                            println("onReady: ${component.name}")
////                        },
////                        onSubmit = { component ->
////                            println("onSubmit: ${component.name}")
////                        },
////                        onSuccess = { component, paymentId ->
////                            println("onSuccess: ${component.name} - $paymentId")
////                        },
////                        onError = { component, checkoutError ->
////                            println("onError ${component.name}: $checkoutError")
////                        },
////                    ),
////                )
//
//
//
//            } catch (checkoutError: CheckoutError) {
//                Log.e("CheckoutError", "Error creating Checkout component", checkoutError)
//            }
//        }



//            try {
//                val checkoutComponents = CheckoutComponentsFactory(config = configuration).create()
//                val flow = checkoutComponents.create(ComponentName.Flow)
//
//                // Switch to Main thread to update UI
//                launch(Dispatchers.Main) {
//                    flow.provideView(containerView)
//                }
//
//            } catch (checkoutError: CheckoutError) {
//                Log.e("TAG", "CheckoutFuctionImplement: ", checkoutError)
//            }
//        }

    }

    private fun setupCheckoutCard() {
        val containerView = findViewById<FrameLayout>(R.id.checkoutContainer)

        val configuration = CheckoutComponentConfiguration(
            context = this,
            paymentSession = PaymentSessionResponse(
                id = "ps_34QIeTVy3LTlizWwEB4pZAHswyf",
                paymentSessionToken = "YmFzZTY0:eyJpZCI6InBzXzM0UUllVFZ5M0xUbGl6V3dFQjRwWkFIc3d5ZiIsImVudGl0eV9pZCI6ImVudF83NWN5dm8yZ2c0NGVmYjZkb3drajRkbmdzcSIsImV4cGVyaW1lbnRzIjp7fSwicHJvY2Vzc2luZ19jaGFubmVsX2lkIjoicGNfem5weWhjZHF2bGh1bmo3MjJpN3pncm9vaWkiLCJhbW91bnQiOjEwMDAwLCJsb2NhbGUiOiJlbi1HQiIsImN1cnJlbmN5IjoiQUVEIiwicGF5bWVudF9tZXRob2RzIjpbeyJ0eXBlIjoiY2FyZCIsImNhcmRfc2NoZW1lcyI6WyJWaXNhIiwiTWFzdGVyY2FyZCIsIkFtZXgiXSwic2NoZW1lX2Nob2ljZV9lbmFibGVkIjpmYWxzZSwic3RvcmVfcGF5bWVudF9kZXRhaWxzIjoiZGlzYWJsZWQiLCJiaWxsaW5nX2FkZHJlc3MiOnsiY2l0eSI6IkR1YmFpIiwiY291bnRyeSI6IkFFIn19LHsidHlwZSI6ImFwcGxlcGF5IiwiZGlzcGxheV9uYW1lIjoiZVpoaXJlIiwiY291bnRyeV9jb2RlIjoiR0IiLCJjdXJyZW5jeV9jb2RlIjoiQUVEIiwibWVyY2hhbnRfY2FwYWJpbGl0aWVzIjpbInN1cHBvcnRzM0RTIl0sInN1cHBvcnRlZF9uZXR3b3JrcyI6WyJ2aXNhIiwibWFzdGVyQ2FyZCIsImFtZXgiXSwidG90YWwiOnsibGFiZWwiOiJlWmhpcmUiLCJ0eXBlIjoiZmluYWwiLCJhbW91bnQiOiIxMDAifX0seyJ0eXBlIjoiZ29vZ2xlcGF5IiwibWVyY2hhbnQiOnsiaWQiOiIwODExMzA4OTM4NjI2ODg0OTk4MiIsIm5hbWUiOiJlWmhpcmUiLCJvcmlnaW4iOiJodHRwczovL3d3dy5lemhpcmUubWUifSwidHJhbnNhY3Rpb25faW5mbyI6eyJ0b3RhbF9wcmljZV9zdGF0dXMiOiJGSU5BTCIsInRvdGFsX3ByaWNlIjoiMTAwIiwiY291bnRyeV9jb2RlIjoiR0IiLCJjdXJyZW5jeV9jb2RlIjoiQUVEIn0sImNhcmRfcGFyYW1ldGVycyI6eyJhbGxvd2VkX2F1dGhfbWV0aG9kcyI6WyJQQU5fT05MWSIsIkNSWVBUT0dSQU1fM0RTIl0sImFsbG93ZWRfY2FyZF9uZXR3b3JrcyI6WyJWSVNBIiwiTUFTVEVSQ0FSRCIsIkFNRVgiXX19XSwiZmVhdHVyZV9mbGFncyI6WyJhbmFseXRpY3Nfb2JzZXJ2YWJpbGl0eV9lbmFibGVkIiwiY2FyZF9maWVsZHNfZW5hYmxlZCIsImdldF93aXRoX3B1YmxpY19rZXlfZW5hYmxlZCIsImxvZ3Nfb2JzZXJ2YWJpbGl0eV9lbmFibGVkIiwicmlza19qc19lbmFibGVkIiwidXNlX25vbl9iaWNfaWRlYWxfaW50ZWdyYXRpb24iXSwicmlzayI6eyJlbmFibGVkIjpmYWxzZX0sIm1lcmNoYW50X25hbWUiOiJlWmhpcmUiLCJwYXltZW50X3Nlc3Npb25fc2VjcmV0IjoicHNzX2E3M2RiMzU4LWFmNTMtNGI0MS05OTEyLTEzY2EzYTNlMDAwMCIsInBheW1lbnRfdHlwZSI6IlJlZ3VsYXIiLCJpbnRlZ3JhdGlvbl9kb21haW4iOiJkZXZpY2VzLmFwaS5zYW5kYm94LmNoZWNrb3V0LmNvbSJ9",
                paymentSessionSecret = "pss_a73db358-af53-4b41-9912-13ca3a3e0000",
            ),
            publicKey = "pk_sbox_awubbtkehjl742o3t5v44vngcyu",
            environment = Environment.SANDBOX,
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val checkoutComponents = CheckoutComponentsFactory(config = configuration).create()
                val cardComponent = checkoutComponents.create(PaymentMethodName.Card)

                launch(Dispatchers.Main) {
//                    cardComponent.provideView(containerView)
                    val view = cardComponent.provideView(containerView)
                    containerView.removeAllViews()
                    containerView.addView(view)
                    Log.d("Checkout", "Card component view added manually")
                    Log.d("Checkout", "Card component view provided")

                }


            } catch (checkoutError: CheckoutError) {
                Log.e("CheckoutError", "Error creating Checkout component", checkoutError)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (::coordinator.isInitialized) {
            Log.d("TAG", "onActivityResult: ")
            //coordinator.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDataSend(data: HashMap<String, Any>) {

    }

    override fun onITemClick(data: String) {
        TODO("Not yet implemented")
    }

//    private suspend fun checkoutWithGooglev2(
//        id: String,
//        paymentSessionToken: String,
//        paymentSessionSecret: String,
//        publicKey: String
//    ) {
//        try {
//            // ✅ Initialize the coordinator
//            coordinator = GooglePayFlowCoordinator(
//                context = this@CheckActivity,
//                handleActivityResult = { resultCode, data ->
//                    Log.d("GooglePay", "Activity result received: $resultCode")
//                }
//            )
//
//            val flowCoordinators = mapOf(
//                PaymentMethodName.GooglePay to coordinator
//            )
//
//            // ✅ Add Google Pay configuration (IMPORTANT)
//            val configuration = CheckoutComponentConfiguration(
//                context = this@CheckActivity,
//                paymentSession = PaymentSessionResponse(
//                    id = id,
//                    paymentSessionToken = paymentSessionToken,
//                    paymentSessionSecret = paymentSessionSecret
//                ),
//                publicKey = publicKey,
//                environment = Environment.SANDBOX,
//
//                googlePayConfiguration = GooglePayConfiguration(
//                    merchantName = "eZhire Rentals", // 👈 your brand name here
//                    environment = GooglePayEnvironment.TEST // or PRODUCTION for live
//                ),
//
//                componentCallback = ComponentCallback(
//                    onReady = { component ->
//                        Log.d("Checkout", "onReady: ${component.name}")
//                    },
//                    onSubmit = { component ->
//                        Log.d("Checkout", "onSubmit: ${component.name}")
//                    },
//                    onSuccess = { component, paymentId ->
//                        itemClickedLocation.onITemClick(paymentId)
//                        Log.d("Checkout", "✅ onSuccess: ${component.name} - $paymentId")
//                    },
//                    onError = { component, checkoutError ->
//                        Toast.makeText(
//                            this@CheckActivity,
//                            "Please Checkout Payment Failed",
//                            Toast.LENGTH_LONG
//                        ).show()
//                        Log.e("Checkout", "❌ onError ${component.name}: $checkoutError")
//                    }
//                ),
//
//                flowCoordinators = flowCoordinators
//            )
//
//            val checkoutComponents = CheckoutComponentsFactory(config = configuration).create()
//            val googlePayComponent = checkoutComponents.create(PaymentMethodName.GooglePay)
//
//            withContext(Dispatchers.Main) {
//                containerView.removeAllViews()
//
//                // ✅ Check if Google Pay is available before adding view
////                googlePayComponent.isReadyToPay(
////                    onReady = { ready ->
////                        if (ready) {
////                            val gPayView = googlePayComponent.provideView(containerView)
////                            containerView.addView(gPayView)
////                            Log.d("Checkout", "✅ Google Pay button added")
////                        } else {
////                            Log.w("Checkout", "⚠️ Google Pay not available on this device/config")
////                            Toast.makeText(
////                                this@CheckActivity,
////                                "Google Pay not available on this device",
////                                Toast.LENGTH_SHORT
////                            ).show()
////                        }
////                    },
////                    onError = { error ->
////                        Log.e("Checkout", "❌ Google Pay readiness check failed: $error")
////                    }
////                )
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }


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

}