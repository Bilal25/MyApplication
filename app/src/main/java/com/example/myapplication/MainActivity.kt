package com.example.myapplication

import BottomSheetFragment
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.paymentcheck.CheckActivity
import com.example.paymentcheck.OnDataPass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.HashMap
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity(),OnDataPass  {
    // val CALLBACK_URL = "myapp://payment/callback"
//    private val CALLBACK_URL = "myapp://payment/callback"
//    private lateinit var coordinator: GooglePayFlowCoordinator
    var listener: OnDataPass? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userData = hashMapOf(
            "id" to "ps_34jY4D8E31M3dqmZHMLQ2FirwuP",
            "paymentSessionToken" to "YmFzZTY0:eyJpZCI6InBzXzM0alk0RDhFMzFNM2RxbVpITUxRMkZpcnd1UCIsImVudGl0eV9pZCI6ImVudF83NWN5dm8yZ2c0NGVmYjZkb3drajRkbmdzcSIsImV4cGVyaW1lbnRzIjp7fSwicHJvY2Vzc2luZ19jaGFubmVsX2lkIjoicGNfem5weWhjZHF2bGh1bmo3MjJpN3pncm9vaWkiLCJhbW91bnQiOjEwMCwibG9jYWxlIjoiZW4tR0IiLCJjdXJyZW5jeSI6IkFFRCIsInBheW1lbnRfbWV0aG9kcyI6W3sidHlwZSI6InJlbWVtYmVyX21lIiwiY2FyZF9zY2hlbWVzIjpbIlZpc2EiLCJNYXN0ZXJjYXJkIiwiQW1leCJdLCJlbWFpbCI6InRlc3RhcnF1bWNoZWNrQHlvcG1haWwuY29tIiwicGhvbmUiOnsibnVtYmVyIjoiMzQwODcyNzY2NyIsImNvdW50cnlfY29kZSI6IjkyIn0sImJpbGxpbmdfYWRkcmVzcyI6eyJjaXR5IjoiRHViYWkiLCJjb3VudHJ5IjoiQUUifSwiZGlzcGxheV9tb2RlIjoiY2hlY2tib3gifSx7InR5cGUiOiJjYXJkIiwiY2FyZF9zY2hlbWVzIjpbIlZpc2EiLCJNYXN0ZXJjYXJkIiwiQW1leCJdLCJzY2hlbWVfY2hvaWNlX2VuYWJsZWQiOmZhbHNlLCJzdG9yZV9wYXltZW50X2RldGFpbHMiOiJlbmFibGVkIiwiYmlsbGluZ19hZGRyZXNzIjp7ImNpdHkiOiJEdWJhaSIsImNvdW50cnkiOiJBRSJ9fSx7InR5cGUiOiJhcHBsZXBheSIsImRpc3BsYXlfbmFtZSI6ImVaaGlyZSIsImNvdW50cnlfY29kZSI6IkdCIiwiY3VycmVuY3lfY29kZSI6IkFFRCIsIm1lcmNoYW50X2NhcGFiaWxpdGllcyI6WyJzdXBwb3J0czNEUyJdLCJzdXBwb3J0ZWRfbmV0d29ya3MiOlsidmlzYSIsIm1hc3RlckNhcmQiLCJhbWV4Il0sInRvdGFsIjp7ImxhYmVsIjoiZVpoaXJlIiwidHlwZSI6ImZpbmFsIiwiYW1vdW50IjoiMSJ9fSx7InR5cGUiOiJnb29nbGVwYXkiLCJtZXJjaGFudCI6eyJpZCI6IjA4MTEzMDg5Mzg2MjY4ODQ5OTgyIiwibmFtZSI6ImVaaGlyZSIsIm9yaWdpbiI6Imh0dHBzOi8vd3d3LmV6aGlyZS5tZSJ9LCJ0cmFuc2FjdGlvbl9pbmZvIjp7InRvdGFsX3ByaWNlX3N0YXR1cyI6IkZJTkFMIiwidG90YWxfcHJpY2UiOiIxIiwiY291bnRyeV9jb2RlIjoiR0IiLCJjdXJyZW5jeV9jb2RlIjoiQUVEIn0sImNhcmRfcGFyYW1ldGVycyI6eyJhbGxvd2VkX2F1dGhfbWV0aG9kcyI6WyJQQU5fT05MWSIsIkNSWVBUT0dSQU1fM0RTIl0sImFsbG93ZWRfY2FyZF9uZXR3b3JrcyI6WyJWSVNBIiwiTUFTVEVSQ0FSRCIsIkFNRVgiXX19XSwiZmVhdHVyZV9mbGFncyI6WyJhbmFseXRpY3Nfb2JzZXJ2YWJpbGl0eV9lbmFibGVkIiwiY2FyZF9maWVsZHNfZW5hYmxlZCIsImdldF93aXRoX3B1YmxpY19rZXlfZW5hYmxlZCIsImxvZ3Nfb2JzZXJ2YWJpbGl0eV9lbmFibGVkIiwicmlza19qc19lbmFibGVkIiwidXNlX25vbl9iaWNfaWRlYWxfaW50ZWdyYXRpb24iXSwicmlzayI6eyJlbmFibGVkIjpmYWxzZX0sIm1lcmNoYW50X25hbWUiOiJlWmhpcmUiLCJwYXltZW50X3Nlc3Npb25fc2VjcmV0IjoicHNzX2RjNWY3ZDQ5LWMwMmQtNGVhYi1iNzlmLTIxOGE3NDcxZGQxOSIsInBheW1lbnRfdHlwZSI6IlJlZ3VsYXIiLCJpbnRlZ3JhdGlvbl9kb21haW4iOiJkZXZpY2VzLmFwaS5zYW5kYm94LmNoZWNrb3V0LmNvbSJ9",
            "paymentSessionSecret" to "pss_dc5f7d49-c02d-4eab-b79f-218a7471dd19",
            "publicKey" to "pk_sbox_awubbtkehjl742o3t5v44vngcyu"
        )

//        val bottomSheetFragment = BottomSheetFragment(userData)
//        bottomSheetFragment.isCancelable = false
//        bottomSheetFragment.show(supportFragmentManager, "BottomSheetFragment")


        val intent = Intent(this@MainActivity, CheckActivity::class.java)
        intent.putExtra("userData", userData)
        paymentLauncher.launch(intent)


        // CheckoutFuctionImplement()
     //   setupCheckoutCard()


//        MainScope().launch {
//            checkoutWithGoogle()
//        }

    }


    private val paymentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val paymentId = result.data?.getStringExtra("paymentId")
                Log.d("PreviousActivity", "Payment successful: $paymentId")
                finish()
            }
        }

    override fun onDataSend(data: HashMap<String, Any>) {
         print(data)
    }

    override fun onITemClick(data: String) {
        print(data)
    }


//    private suspend fun checkoutWithGoogle() {
//        val containerView = findViewById<FrameLayout>(R.id.checkoutContainer)
//
//        // ✅ Initialize the coordinator
//        coordinator = GooglePayFlowCoordinator(
//            context = this@MainActivity,
//            handleActivityResult = { resultCode, data ->
//                Log.d("GooglePay", "Activity result received: $resultCode")
//            }
//        )
//
//        val flowCoordinators = mapOf(
//            PaymentMethodName.GooglePay to coordinator
//        )
//
//        val configuration = CheckoutComponentConfiguration(
//            context = this@MainActivity,
//            paymentSession = PaymentSessionResponse(
//                id = "ps_34QqtyZYOxVJur7XP2M2c5w3eyM",
//                paymentSessionToken = "YmFzZTY0:eyJpZCI6InBzXzM0UXF0eVpZT3hWSnVyN1hQMk0yYzV3M2V5TSIsImVudGl0eV9pZCI6ImVudF83NWN5dm8yZ2c0NGVmYjZkb3drajRkbmdzcSIsImV4cGVyaW1lbnRzIjp7fSwicHJvY2Vzc2luZ19jaGFubmVsX2lkIjoicGNfem5weWhjZHF2bGh1bmo3MjJpN3pncm9vaWkiLCJhbW91bnQiOjEwMDAwLCJsb2NhbGUiOiJlbi1HQiIsImN1cnJlbmN5IjoiQUVEIiwicGF5bWVudF9tZXRob2RzIjpbeyJ0eXBlIjoiY2FyZCIsImNhcmRfc2NoZW1lcyI6WyJWaXNhIiwiTWFzdGVyY2FyZCIsIkFtZXgiXSwic2NoZW1lX2Nob2ljZV9lbmFibGVkIjpmYWxzZSwic3RvcmVfcGF5bWVudF9kZXRhaWxzIjoiZGlzYWJsZWQiLCJiaWxsaW5nX2FkZHJlc3MiOnsiY2l0eSI6IkR1YmFpIiwiY291bnRyeSI6IkFFIn19LHsidHlwZSI6Imdvb2dsZXBheSIsIm1lcmNoYW50Ijp7ImlkIjoiMDgxMTMwODkzODYyNjg4NDk5ODIiLCJuYW1lIjoiTXkgVGVzdCBNZXJjaGFudCIsIm9yaWdpbiI6Imh0dHBzOi8vbXlhcHAuY29tIn19XSwiZmVhdHVyZV9mbGFncyI6WyJhbmFseXRpY3Nfb2JzZXJ2YWJpbGl0eV9lbmFibGVkIl0sIm1lcmNoYW50X25hbWUiOiJNeSBNZXJjaGFudCIsInBheW1lbnRfdHlwZSI6IlJlZ3VsYXIifQ==",
//                paymentSessionSecret = "pss_94af1c83-f5ef-4691-b927-bd2437c9c893"
//            ),
//            publicKey = "pk_sbox_awubbtkehjl742o3t5v44vngcyu",
//            environment = Environment.SANDBOX,
//            componentCallback = ComponentCallback(
//                onReady = { component ->
//                    Log.d("Checkout", "onReady: ${component.name}")
//                },
//                onSubmit = { component ->
//                    Log.d("Checkout", "onSubmit: ${component.name}")
//                },
//                onSuccess = { component, paymentId ->
//                    Log.d("Checkout", "✅ onSuccess: ${component.name} - $paymentId")
//                },
//                onError = { component, checkoutError ->
//                    Log.e("Checkout", "❌ onError ${component.name}: $checkoutError")
//                }
//            ),
//            flowCoordinators = flowCoordinators
//        )
//
//        val checkoutComponents = CheckoutComponentsFactory(config = configuration).create()
//
//        val cardComponent = checkoutComponents.create(PaymentMethodName.Card)
//        val googlePayComponent = checkoutComponents.create(PaymentMethodName.GooglePay)
//
//        withContext(Dispatchers.Main) {
//            containerView.removeAllViews()
//            containerView.addView(cardComponent.provideView(containerView))
//            containerView.addView(googlePayComponent.provideView(containerView))
//            Log.d("Checkout", "✅ Card & Google Pay views added")
//        }
//    }
//
//
//    private fun CheckoutFuctionImplement() {
//        val containerView = findViewById<FrameLayout>(R.id.checkoutContainer)
//
//        val configuration = CheckoutComponentConfiguration(
//            context = this,
//            paymentSession = PaymentSessionResponse(
//                id = "ps_34Pi9dRvt2SA4YqhbODm7lCiS78",
//                paymentSessionToken = "YmFzZTY0:eyJpZCI6InBzXzM0UGk5ZFJ2dDJTQTRZcWhiT0RtN2xDaVM3OCIsImVudGl0eV9pZCI6ImVudF83NWN5dm8yZ2c0NGVmYjZkb3drajRkbmdzcSIsImV4cGVyaW1lbnRzIjp7fSwicHJvY2Vzc2luZ19jaGFubmVsX2lkIjoicGNfem5weWhjZHF2bGh1bmo3MjJpN3pncm9vaWkiLCJhbW91bnQiOjEwMCwibG9jYWxlIjoiZW4tR0IiLCJjdXJyZW5jeSI6IkFFRCIsInBheW1lbnRfbWV0aG9kcyI6W3sidHlwZSI6ImNhcmQiLCJjYXJkX3NjaGVtZXMiOlsiVmlzYSIsIk1hc3RlcmNhcmQiLCJBbWV4Il0sInNjaGVtZV9jaG9pY2VfZW5hYmxlZCI6ZmFsc2UsInN0b3JlX3BheW1lbnRfZGV0YWlscyI6ImRpc2FibGVkIiwiYmlsbGluZ19hZGRyZXNzIjp7ImNpdHkiOiJEdWJhaSIsImNvdW50cnkiOiJBRSJ9fSx7InR5cGUiOiJhcHBsZXBheSIsImRpc3BsYXlfbmFtZSI6ImVaaGlyZSIsImNvdW50cnlfY29kZSI6IkdCIiwiY3VycmVuY3lfY29kZSI6IkFFRCIsIm1lcmNoYW50X2NhcGFiaWxpdGllcyI6WyJzdXBwb3J0czNEUyJdLCJzdXBwb3J0ZWRfbmV0d29ya3MiOlsidmlzYSIsIm1hc3RlckNhcmQiLCJhbWV4Il0sInRvdGFsIjp7ImxhYmVsIjoiZVpoaXJlIiwidHlwZSI6ImZpbmFsIiwiYW1vdW50IjoiMSJ9fSx7InR5cGUiOiJnb29nbGVwYXkiLCJtZXJjaGFudCI6eyJpZCI6IjA4MTEzMDg5Mzg2MjY4ODQ5OTgyIiwibmFtZSI6ImVaaGlyZSIsIm9yaWdpbiI6Imh0dHBzOi8vd3d3LmV6aGlyZS5tZSJ9LCJ0cmFuc2FjdGlvbl9pbmZvIjp7InRvdGFsX3ByaWNlX3N0YXR1cyI6IkZJTkFMIiwidG90YWxfcHJpY2UiOiIxIiwiY291bnRyeV9jb2RlIjoiR0IiLCJjdXJyZW5jeV9jb2RlIjoiQUVEIn0sImNhcmRfcGFyYW1ldGVycyI6eyJhbGxvd2VkX2F1dGhfbWV0aG9kcyI6WyJQQU5fT05MWSIsIkNSWVBUT0dSQU1fM0RTIl0sImFsbG93ZWRfY2FyZF9uZXR3b3JrcyI6WyJWSVNBIiwiTUFTVEVSQ0FSRCIsIkFNRVgiXX19XSwiZmVhdHVyZV9mbGFncyI6WyJhbmFseXRpY3Nfb2JzZXJ2YWJpbGl0eV9lbmFibGVkIiwiY2FyZF9maWVsZHNfZW5hYmxlZCIsImdldF93aXRoX3B1YmxpY19rZXlfZW5hYmxlZCIsImxvZ3Nfb2JzZXJ2YWJpbGl0eV9lbmFibGVkIiwicmlza19qc19lbmFibGVkIiwidXNlX25vbl9iaWNfaWRlYWxfaW50ZWdyYXRpb24iXSwicmlzayI6eyJlbmFibGVkIjpmYWxzZX0sIm1lcmNoYW50X25hbWUiOiJlWmhpcmUiLCJwYXltZW50X3Nlc3Npb25fc2VjcmV0IjoicHNzXzBiYjdiMjBmLWFmODgtNDAzOS04YWYzLWUyOGQxYjI2OTk3OSIsInBheW1lbnRfdHlwZSI6IlJlZ3VsYXIiLCJpbnRlZ3JhdGlvbl9kb21haW4iOiJkZXZpY2VzLmFwaS5zYW5kYm94LmNoZWNrb3V0LmNvbSJ9",
//                paymentSessionSecret = "pss_0bb7b20f-af88-4039-8af3-e28d1b269979",
//                ),
//            publicKey = "pk_sbox_awubbtkehjl742o3t5v44vngcyu",
//            environment = Environment.SANDBOX,
//        )
//
//// Create CheckoutComponents
////        CoroutineScope(Dispatchers.IO).launch {
////            try {
////                val checkoutComponents = CheckoutComponentsFactory(config = configuration).create()
 ////                val flow = checkoutComponents.create(ComponentName.Flow)
////                //checkoutComponent.provideView(containerView)
////                flow.provideView(containerView)
////
////            } catch (checkoutError: CheckoutError) {
////                Log.e("TAG", "CheckoutFuctionImplement: ",checkoutError )
////              //  handleError(checkoutError)
////            }
////        }
//
//
//
//
//
//
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                // --- STEP 1: Create Configuration ---
//                val configuration = CheckoutComponentConfiguration(
//                    context = this@MainActivity,
//                    paymentSession = PaymentSessionResponse(
//                        id = "ps_34QqtyZYOxVJur7XP2M2c5w3eyM",
//                        paymentSessionToken = "YmFzZTY0:eyJpZCI6InBzXzM0UXF0eVpZT3hWSnVyN1hQMk0yYzV3M2V5TSIsImVudGl0eV9pZCI6ImVudF83NWN5dm8yZ2c0NGVmYjZkb3drajRkbmdzcSIsImV4cGVyaW1lbnRzIjp7fSwicHJvY2Vzc2luZ19jaGFubmVsX2lkIjoicGNfem5weWhjZHF2bGh1bmo3MjJpN3pncm9vaWkiLCJhbW91bnQiOjEwMDAwLCJsb2NhbGUiOiJlbi1HQiIsImN1cnJlbmN5IjoiQUVEIiwicGF5bWVudF9tZXRob2RzIjpbeyJ0eXBlIjoiY2FyZCIsImNhcmRfc2NoZW1lcyI6WyJWaXNhIiwiTWFzdGVyY2FyZCIsIkFtZXgiXSwic2NoZW1lX2Nob2ljZV9lbmFibGVkIjpmYWxzZSwic3RvcmVfcGF5bWVudF9kZXRhaWxzIjoiZGlzYWJsZWQiLCJiaWxsaW5nX2FkZHJlc3MiOnsiY2l0eSI6IkR1YmFpIiwiY291bnRyeSI6IkFFIn19LHsidHlwZSI6ImFwcGxlcGF5IiwiZGlzcGxheV9uYW1lIjoiZVpoaXJlIiwiY291bnRyeV9jb2RlIjoiR0IiLCJjdXJyZW5jeV9jb2RlIjoiQUVEIiwibWVyY2hhbnRfY2FwYWJpbGl0aWVzIjpbInN1cHBvcnRzM0RTIl0sInN1cHBvcnRlZF9uZXR3b3JrcyI6WyJ2aXNhIiwibWFzdGVyQ2FyZCIsImFtZXgiXSwidG90YWwiOnsibGFiZWwiOiJlWmhpcmUiLCJ0eXBlIjoiZmluYWwiLCJhbW91bnQiOiIxMDAifX0seyJ0eXBlIjoiZ29vZ2xlcGF5IiwibWVyY2hhbnQiOnsiaWQiOiIwODExMzA4OTM4NjI2ODg0OTk4MiIsIm5hbWUiOiJlWmhpcmUiLCJvcmlnaW4iOiJodHRwczovL3d3dy5lemhpcmUubWUifSwidHJhbnNhY3Rpb25faW5mbyI6eyJ0b3RhbF9wcmljZV9zdGF0dXMiOiJGSU5BTCIsInRvdGFsX3ByaWNlIjoiMTAwIiwiY291bnRyeV9jb2RlIjoiR0IiLCJjdXJyZW5jeV9jb2RlIjoiQUVEIn0sImNhcmRfcGFyYW1ldGVycyI6eyJhbGxvd2VkX2F1dGhfbWV0aG9kcyI6WyJQQU5fT05MWSIsIkNSWVBUT0dSQU1fM0RTIl0sImFsbG93ZWRfY2FyZF9uZXR3b3JrcyI6WyJWSVNBIiwiTUFTVEVSQ0FSRCIsIkFNRVgiXX19XSwiZmVhdHVyZV9mbGFncyI6WyJhbmFseXRpY3Nfb2JzZXJ2YWJpbGl0eV9lbmFibGVkIiwiY2FyZF9maWVsZHNfZW5hYmxlZCIsImdldF93aXRoX3B1YmxpY19rZXlfZW5hYmxlZCIsImxvZ3Nfb2JzZXJ2YWJpbGl0eV9lbmFibGVkIiwicmlza19qc19lbmFibGVkIiwidXNlX25vbl9iaWNfaWRlYWxfaW50ZWdyYXRpb24iXSwicmlzayI6eyJlbmFibGVkIjpmYWxzZX0sIm1lcmNoYW50X25hbWUiOiJlWmhpcmUiLCJwYXltZW50X3Nlc3Npb25fc2VjcmV0IjoicHNzXzk0YWYxYzgzLWY1ZWYtNDY5MS1iOTI3LWJkMjQzN2M5Yzg5MyIsInBheW1lbnRfdHlwZSI6IlJlZ3VsYXIiLCJpbnRlZ3JhdGlvbl9kb21haW4iOiJkZXZpY2VzLmFwaS5zYW5kYm94LmNoZWNrb3V0LmNvbSJ9",
//                        paymentSessionSecret = "pss_94af1c83-f5ef-4691-b927-bd2437c9c893"
//                    ),
//                    publicKey = "pk_sbox_awubbtkehjl742o3t5v44vngcyu",
//                    environment = Environment.SANDBOX,
//                    componentCallback = ComponentCallback(
//                        onReady = { component ->
//                            println("onReady: ${component.name}")
//                        },
//                        onSubmit = { component ->
//                            println("onSubmit: ${component.name}")
//                        },
//                        onSuccess = { component, paymentId ->
//                            println("✅ onSuccess: ${component.name} - $paymentId")
//
//                            // STEP 3 — handle callback manually here:
//                            val callbackUrl = "https://yourbackend.com/callback?payment_id=$paymentId"
//                            CoroutineScope(Dispatchers.IO).launch {
//                                try {
//                                    val response = URL(callbackUrl).readText()
//                                    Log.d("CheckoutCallback", "Callback response: $response")
//                                } catch (e: Exception) {
//                                    Log.e("CheckoutCallback", "Callback failed", e)
//                                }
//                            }
//                        },
//                        onError = { component, checkoutError ->
//                            Log.e("Checkout", "❌ onError ${component.name}: $checkoutError")
//                        },
//                    )
//                )
//
//                // --- STEP 2: Create Component Factory ---
//                val checkoutComponents = CheckoutComponentsFactory(config = configuration).create()
//                val cardComponent = checkoutComponents.create(PaymentMethodName.Card)
//
//                // --- STEP 3: Add UI on Main Thread ---
//                withContext(Dispatchers.Main) {
//                    val cardView = cardComponent.provideView(containerView)
//                    containerView.removeAllViews()
//                    containerView.addView(cardView)
//                    Log.d("Checkout", "✅ Card component view added")
//                }
//
//            } catch (checkoutError: CheckoutError) {
//                Log.e("CheckoutError", "Error creating Checkout component", checkoutError)
//            }
//        }
//
//
////        CoroutineScope(Dispatchers.IO).launch {
////
////            try {
////                val checkoutComponents = CheckoutComponentsFactory(config = configuration).create()
////                val flow = checkoutComponents.create(PaymentMethodName.Card)
////                launch(Dispatchers.Main) {
////                    flow.provideView(containerView)
////
////                    Log.d("Checkout", "Checkout Flow view provided")
////                    containerView.addView(TextView(this@MainActivity).apply {
////                        text = "Checkout Card UI yahan appear hoti"
////                        gravity = Gravity.CENTER
////                        textSize = 18f
////                    })
////                }
////                val configuration = CheckoutComponentConfiguration(
////                    context = this,
////                    paymentSession = PaymentSessionResponse(
////                        id = "ps_34Pi9dRvt2SA4YqhbODm7lCiS78",
////                        paymentSessionToken = "YmFzZTY0:eyJpZCI6InBzXzM0UGk5ZFJ2dDJTQTRZcWhiT0RtN2xDaVM3OCIsImVudGl0eV9pZCI6ImVudF83NWN5dm8yZ2c0NGVmYjZkb3drajRkbmdzcSIsImV4cGVyaW1lbnRzIjp7fSwicHJvY2Vzc2luZ19jaGFubmVsX2lkIjoicGNfem5weWhjZHF2bGh1bmo3MjJpN3pncm9vaWkiLCJhbW91bnQiOjEwMCwibG9jYWxlIjoiZW4tR0IiLCJjdXJyZW5jeSI6IkFFRCIsInBheW1lbnRfbWV0aG9kcyI6W3sidHlwZSI6ImNhcmQiLCJjYXJkX3NjaGVtZXMiOlsiVmlzYSIsIk1hc3RlcmNhcmQiLCJBbWV4Il0sInNjaGVtZV9jaG9pY2VfZW5hYmxlZCI6ZmFsc2UsInN0b3JlX3BheW1lbnRfZGV0YWlscyI6ImRpc2FibGVkIiwiYmlsbGluZ19hZGRyZXNzIjp7ImNpdHkiOiJEdWJhaSIsImNvdW50cnkiOiJBRSJ9fSx7InR5cGUiOiJhcHBsZXBheSIsImRpc3BsYXlfbmFtZSI6ImVaaGlyZSIsImNvdW50cnlfY29kZSI6IkdCIiwiY3VycmVuY3lfY29kZSI6IkFFRCIsIm1lcmNoYW50X2NhcGFiaWxpdGllcyI6WyJzdXBwb3J0czNEUyJdLCJzdXBwb3J0ZWRfbmV0d29ya3MiOlsidmlzYSIsIm1hc3RlckNhcmQiLCJhbWV4Il0sInRvdGFsIjp7ImxhYmVsIjoiZVpoaXJlIiwidHlwZSI6ImZpbmFsIiwiYW1vdW50IjoiMSJ9fSx7InR5cGUiOiJnb29nbGVwYXkiLCJtZXJjaGFudCI6eyJpZCI6IjA4MTEzMDg5Mzg2MjY4ODQ5OTgyIiwibmFtZSI6ImVaaGlyZSIsIm9yaWdpbiI6Imh0dHBzOi8vd3d3LmV6aGlyZS5tZSJ9LCJ0cmFuc2FjdGlvbl9pbmZvIjp7InRvdGFsX3ByaWNlX3N0YXR1cyI6IkZJTkFMIiwidG90YWxfcHJpY2UiOiIxIiwiY291bnRyeV9jb2RlIjoiR0IiLCJjdXJyZW5jeV9jb2RlIjoiQUVEIn0sImNhcmRfcGFyYW1ldGVycyI6eyJhbGxvd2VkX2F1dGhfbWV0aG9kcyI6WyJQQU5fT05MWSIsIkNSWVBUT0dSQU1fM0RTIl0sImFsbG93ZWRfY2FyZF9uZXR3b3JrcyI6WyJWSVNBIiwiTUFTVEVSQ0FSRCIsIkFNRVgiXX19XSwiZmVhdHVyZV9mbGFncyI6WyJhbmFseXRpY3Nfb2JzZXJ2YWJpbGl0eV9lbmFibGVkIiwiY2FyZF9maWVsZHNfZW5hYmxlZCIsImdldF93aXRoX3B1YmxpY19rZXlfZW5hYmxlZCIsImxvZ3Nfb2JzZXJ2YWJpbGl0eV9lbmFibGVkIiwicmlza19qc19lbmFibGVkIiwidXNlX25vbl9iaWNfaWRlYWxfaW50ZWdyYXRpb24iXSwicmlzayI6eyJlbmFibGVkIjpmYWxzZX0sIm1lcmNoYW50X25hbWUiOiJlWmhpcmUiLCJwYXltZW50X3Nlc3Npb25fc2VjcmV0IjoicHNzXzBiYjdiMjBmLWFmODgtNDAzOS04YWYzLWUyOGQxYjI2OTk3OSIsInBheW1lbnRfdHlwZSI6IlJlZ3VsYXIiLCJpbnRlZ3JhdGlvbl9kb21haW4iOiJkZXZpY2VzLmFwaS5zYW5kYm94LmNoZWNrb3V0LmNvbSJ9",
////                        paymentSessionSecret = "pss_0bb7b20f-af88-4039-8af3-e28d1b269979",
////                    ),
////                    publicKey = "pk_sbox_awubbtkehjl742o3t5v44vngcyu",
////                    environment = Environment.SANDBOX,
////
////
//////                val configuration = CheckoutComponentConfiguration(
//////                    context = this@MainActivity,
//////                    paymentSession = PaymentSessionResponse(
//////                        id = "ps_34Pi9dRvt2SA4YqhbODm7lCiS78",
//////                        paymentSessionToken = "YmFzZTY0:eyJpZCI6InBzXzM0UGk5ZFJ2dDJTQTRZcWhiT0RtN2xDaVM3OCIsImVudGl0eV9pZCI6ImVudF83NWN5dm8yZ2c0NGVmYjZkb3drajRkbmdzcSIsImV4cGVyaW1lbnRzIjp7fSwicHJvY2Vzc2luZ19jaGFubmVsX2lkIjoicGNfem5weWhjZHF2bGh1bmo3MjJpN3pncm9vaWkiLCJhbW91bnQiOjEwMCwibG9jYWxlIjoiZW4tR0IiLCJjdXJyZW5jeSI6IkFFRCIsInBheW1lbnRfbWV0aG9kcyI6W3sidHlwZSI6ImNhcmQiLCJjYXJkX3NjaGVtZXMiOlsiVmlzYSIsIk1hc3RlcmNhcmQiLCJBbWV4Il0sInNjaGVtZV9jaG9pY2VfZW5hYmxlZCI6ZmFsc2UsInN0b3JlX3BheW1lbnRfZGV0YWlscyI6ImRpc2FibGVkIiwiYmlsbGluZ19hZGRyZXNzIjp7ImNpdHkiOiJEdWJhaSIsImNvdW50cnkiOiJBRSJ9fSx7InR5cGUiOiJhcHBsZXBheSIsImRpc3BsYXlfbmFtZSI6ImVaaGlyZSIsImNvdW50cnlfY29kZSI6IkdCIiwiY3VycmVuY3lfY29kZSI6IkFFRCIsIm1lcmNoYW50X2NhcGFiaWxpdGllcyI6WyJzdXBwb3J0czNEUyJdLCJzdXBwb3J0ZWRfbmV0d29ya3MiOlsidmlzYSIsIm1hc3RlckNhcmQiLCJhbWV4Il0sInRvdGFsIjp7ImxhYmVsIjoiZVpoaXJlIiwidHlwZSI6ImZpbmFsIiwiYW1vdW50IjoiMSJ9fSx7InR5cGUiOiJnb29nbGVwYXkiLCJtZXJjaGFudCI6eyJpZCI6IjA4MTEzMDg5Mzg2MjY4ODQ5OTgyIiwibmFtZSI6ImVaaGlyZSIsIm9yaWdpbiI6Imh0dHBzOi8vd3d3LmV6aGlyZS5tZSJ9LCJ0cmFuc2FjdGlvbl9pbmZvIjp7InRvdGFsX3ByaWNlX3N0YXR1cyI6IkZJTkFMIiwidG90YWxfcHJpY2UiOiIxIiwiY291bnRyeV9jb2RlIjoiR0IiLCJjdXJyZW5jeV9jb2RlIjoiQUVEIn0sImNhcmRfcGFyYW1ldGVycyI6eyJhbGxvd2VkX2F1dGhfbWV0aG9kcyI6WyJQQU5fT05MWSIsIkNSWVBUT0dSQU1fM0RTIl0sImFsbG93ZWRfY2FyZF9uZXR3b3JrcyI6WyJWSVNBIiwiTUFTVEVSQ0FSRCIsIkFNRVgiXX19XSwiZmVhdHVyZV9mbGFncyI6WyJhbmFseXRpY3Nfb2JzZXJ2YWJpbGl0eV9lbmFibGVkIiwiY2FyZF9maWVsZHNfZW5hYmxlZCIsImdldF93aXRoX3B1YmxpY19rZXlfZW5hYmxlZCIsImxvZ3Nfb2JzZXJ2YWJpbGl0eV9lbmFibGVkIiwicmlza19qc19lbmFibGVkIiwidXNlX25vbl9iaWNfaWRlYWxfaW50ZWdyYXRpb24iXSwicmlzayI6eyJlbmFibGVkIjpmYWxzZX0sIm1lcmNoYW50X25hbWUiOiJlWmhpcmUiLCJwYXltZW50X3Nlc3Npb25fc2VjcmV0IjoicHNzXzBiYjdiMjBmLWFmODgtNDAzOS04YWYzLWUyOGQxYjI2OTk3OSIsInBheW1lbnRfdHlwZSI6IlJlZ3VsYXIiLCJpbnRlZ3JhdGlvbl9kb21haW4iOiJkZXZpY2VzLmFwaS5zYW5kYm94LmNoZWNrb3V0LmNvbSJ9",
//////                        paymentSessionSecret = "pss_0bb7b20f-af88-4039-8af3-e28d1b269979"
//////                    ),
////                    publicKey = "",
////                    environment = Environment.SANDBOX,
////                    callbackUrl = CALLBACK_URL,
////                    flowCoordinators = mapOf(
////                        PaymentMethodName.GooglePay to googlePayFlowCoordinator
////                    ),
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
////                    componentOptions = TODO(),
////                    locale = TODO(),
////                    translations = TODO(),
////                    appearance = TODO(),
////                )
////
//////                val configuration = CheckoutComponentConfiguration(
//////                    context = this@MainActivity,
//////                    paymentSession = PaymentSessionResponse(
//////                        id = "",
//////                        paymentSessionToken = "",
//////                        paymentSessionSecret = "",
//////                    ),
//////                    publicKey = "",
//////                    environment = Environment.SANDBOX,
//////                    callbackUrl = "", // 👈 Add this line
//////                    flowCoordinators = googlePayFlowCoordinator.value?.let {
//////                        mapOf(PaymentMethodName.GooglePay to it)
//////                    } ?: emptyMap(),
//////                    componentCallback = ComponentCallback(
//////                        onReady = { component ->
//////                            println("onReady: ${component.name}")
//////                        },
//////                        onSubmit = { component ->
//////                            println("onSubmit: ${component.name}")
//////                        },
//////                        onSuccess = { component, paymentId ->
//////                            println("onSuccess: ${component.name} - $paymentId")
//////                        },
//////                        onError = { component, checkoutError ->
//////                            println("onError ${component.name}: $checkoutError")
//////                        },
//////                    ),
//////                )
////
////
////
////            } catch (checkoutError: CheckoutError) {
////                Log.e("CheckoutError", "Error creating Checkout component", checkoutError)
////            }
////        }
//
//
//
////            try {
////                val checkoutComponents = CheckoutComponentsFactory(config = configuration).create()
////                val flow = checkoutComponents.create(ComponentName.Flow)
////
////                // Switch to Main thread to update UI
////                launch(Dispatchers.Main) {
////                    flow.provideView(containerView)
////                }
////
////            } catch (checkoutError: CheckoutError) {
////                Log.e("TAG", "CheckoutFuctionImplement: ", checkoutError)
////            }
////        }
//
//        }
//
//    private fun setupCheckoutCard() {
//        val containerView = findViewById<FrameLayout>(R.id.checkoutContainer)
//
//        val configuration = CheckoutComponentConfiguration(
//            context = this,
//            paymentSession = PaymentSessionResponse(
//                id = "ps_34QIeTVy3LTlizWwEB4pZAHswyf",
//                paymentSessionToken = "YmFzZTY0:eyJpZCI6InBzXzM0UUllVFZ5M0xUbGl6V3dFQjRwWkFIc3d5ZiIsImVudGl0eV9pZCI6ImVudF83NWN5dm8yZ2c0NGVmYjZkb3drajRkbmdzcSIsImV4cGVyaW1lbnRzIjp7fSwicHJvY2Vzc2luZ19jaGFubmVsX2lkIjoicGNfem5weWhjZHF2bGh1bmo3MjJpN3pncm9vaWkiLCJhbW91bnQiOjEwMDAwLCJsb2NhbGUiOiJlbi1HQiIsImN1cnJlbmN5IjoiQUVEIiwicGF5bWVudF9tZXRob2RzIjpbeyJ0eXBlIjoiY2FyZCIsImNhcmRfc2NoZW1lcyI6WyJWaXNhIiwiTWFzdGVyY2FyZCIsIkFtZXgiXSwic2NoZW1lX2Nob2ljZV9lbmFibGVkIjpmYWxzZSwic3RvcmVfcGF5bWVudF9kZXRhaWxzIjoiZGlzYWJsZWQiLCJiaWxsaW5nX2FkZHJlc3MiOnsiY2l0eSI6IkR1YmFpIiwiY291bnRyeSI6IkFFIn19LHsidHlwZSI6ImFwcGxlcGF5IiwiZGlzcGxheV9uYW1lIjoiZVpoaXJlIiwiY291bnRyeV9jb2RlIjoiR0IiLCJjdXJyZW5jeV9jb2RlIjoiQUVEIiwibWVyY2hhbnRfY2FwYWJpbGl0aWVzIjpbInN1cHBvcnRzM0RTIl0sInN1cHBvcnRlZF9uZXR3b3JrcyI6WyJ2aXNhIiwibWFzdGVyQ2FyZCIsImFtZXgiXSwidG90YWwiOnsibGFiZWwiOiJlWmhpcmUiLCJ0eXBlIjoiZmluYWwiLCJhbW91bnQiOiIxMDAifX0seyJ0eXBlIjoiZ29vZ2xlcGF5IiwibWVyY2hhbnQiOnsiaWQiOiIwODExMzA4OTM4NjI2ODg0OTk4MiIsIm5hbWUiOiJlWmhpcmUiLCJvcmlnaW4iOiJodHRwczovL3d3dy5lemhpcmUubWUifSwidHJhbnNhY3Rpb25faW5mbyI6eyJ0b3RhbF9wcmljZV9zdGF0dXMiOiJGSU5BTCIsInRvdGFsX3ByaWNlIjoiMTAwIiwiY291bnRyeV9jb2RlIjoiR0IiLCJjdXJyZW5jeV9jb2RlIjoiQUVEIn0sImNhcmRfcGFyYW1ldGVycyI6eyJhbGxvd2VkX2F1dGhfbWV0aG9kcyI6WyJQQU5fT05MWSIsIkNSWVBUT0dSQU1fM0RTIl0sImFsbG93ZWRfY2FyZF9uZXR3b3JrcyI6WyJWSVNBIiwiTUFTVEVSQ0FSRCIsIkFNRVgiXX19XSwiZmVhdHVyZV9mbGFncyI6WyJhbmFseXRpY3Nfb2JzZXJ2YWJpbGl0eV9lbmFibGVkIiwiY2FyZF9maWVsZHNfZW5hYmxlZCIsImdldF93aXRoX3B1YmxpY19rZXlfZW5hYmxlZCIsImxvZ3Nfb2JzZXJ2YWJpbGl0eV9lbmFibGVkIiwicmlza19qc19lbmFibGVkIiwidXNlX25vbl9iaWNfaWRlYWxfaW50ZWdyYXRpb24iXSwicmlzayI6eyJlbmFibGVkIjpmYWxzZX0sIm1lcmNoYW50X25hbWUiOiJlWmhpcmUiLCJwYXltZW50X3Nlc3Npb25fc2VjcmV0IjoicHNzX2E3M2RiMzU4LWFmNTMtNGI0MS05OTEyLTEzY2EzYTNlMDAwMCIsInBheW1lbnRfdHlwZSI6IlJlZ3VsYXIiLCJpbnRlZ3JhdGlvbl9kb21haW4iOiJkZXZpY2VzLmFwaS5zYW5kYm94LmNoZWNrb3V0LmNvbSJ9",
//                paymentSessionSecret = "pss_a73db358-af53-4b41-9912-13ca3a3e0000",
//            ),
//            publicKey = "pk_sbox_awubbtkehjl742o3t5v44vngcyu",
//            environment = Environment.SANDBOX,
//        )
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val checkoutComponents = CheckoutComponentsFactory(config = configuration).create()
//                val cardComponent = checkoutComponents.create(PaymentMethodName.Card)
//
//                launch(Dispatchers.Main) {
////                    cardComponent.provideView(containerView)
//                    val view = cardComponent.provideView(containerView)
//                    containerView.removeAllViews()
//                    containerView.addView(view)
//                    Log.d("Checkout", "Card component view added manually")
//                    Log.d("Checkout", "Card component view provided")
//
//                }
//
//
//            } catch (checkoutError: CheckoutError) {
//                Log.e("CheckoutError", "Error creating Checkout component", checkoutError)
//            }
//        }
//
//      }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (::coordinator.isInitialized) {
//            Log.d("TAG", "onActivityResult: ")
//            //coordinator.onActivityResult(requestCode, resultCode, data)
//        }
//    }
}