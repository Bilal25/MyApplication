package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.example.paymentcheck.MainActivity2
import com.example.paymentcheck.MainActivity3
import com.example.paymentcheck.OnDataPass
import com.example.paymentcheck.PaymentBottomSheet
import com.example.paymentcheck.PaymentBottomSheet.Companion.userDataGooglePay
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

class MainActivity : AppCompatActivity()  {
    // val CALLBACK_URL = "myapp://payment/callback"
//    private val CALLBACK_URL = "myapp://payment/callback"
//    private lateinit var coordinator: GooglePayFlowCoordinator
    var listener: OnDataPass? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)






//        userDataGooglePay = hashMapOf(
//            "id" to "ps_36YfYkQjtcEK3ajizhzvkgiAbI7",
//            "paymentSessionToken" to "YmFzZTY0:eyJpZCI6InBzXzM2WU5zblNsenBRUjBqMVRhTzdmM3hsOTZDTSIsImVudGl0eV9pZCI6ImVudF83NWN5dm8yZ2c0NGVmYjZkb3drajRkbmdzcSIsImV4cGVyaW1lbnRzIjp7fSwicHJvY2Vzc2luZ19jaGFubmVsX2lkIjoicGNfem5weWhjZHF2bGh1bmo3MjJpN3pncm9vaWkiLCJhbW91bnQiOjEwMCwibG9jYWxlIjoiZW4tR0IiLCJjdXJyZW5jeSI6IkFFRCIsInBheW1lbnRfbWV0aG9kcyI6W3sidHlwZSI6InJlbWVtYmVyX21lIiwiZW1haWwiOiJhbmRyb2lkMTU1QHlvcG1haWwuY29tIiwicGhvbmUiOnsibnVtYmVyIjoiKzk3MTYzNzk4MjkyMTAiLCJjb3VudHJ5X2NvZGUiOiI5MiJ9LCJjYXJkX3NjaGVtZXMiOlsiVmlzYSIsIk1hc3RlcmNhcmQiLCJBbWV4Il0sImJpbGxpbmdfYWRkcmVzcyI6eyJjaXR5IjoiRHViYWkiLCJjb3VudHJ5IjoiQUUifSwiZGlzcGxheV9tb2RlIjoiY2hlY2tib3gifSx7InR5cGUiOiJjYXJkIiwiY2FyZF9zY2hlbWVzIjpbIlZpc2EiLCJNYXN0ZXJjYXJkIiwiQW1leCJdLCJzY2hlbWVfY2hvaWNlX2VuYWJsZWQiOmZhbHNlLCJzdG9yZV9wYXltZW50X2RldGFpbHMiOiJpbXBsaWNpdGx5X2VuYWJsZWQiLCJiaWxsaW5nX2FkZHJlc3MiOnsiY2l0eSI6IkR1YmFpIiwiY291bnRyeSI6IkFFIn19LHsidHlwZSI6ImFwcGxlcGF5IiwiZGlzcGxheV9uYW1lIjoiZVpoaXJlIiwiY291bnRyeV9jb2RlIjoiR0IiLCJjdXJyZW5jeV9jb2RlIjoiQUVEIiwibWVyY2hhbnRfY2FwYWJpbGl0aWVzIjpbInN1cHBvcnRzM0RTIl0sInN1cHBvcnRlZF9uZXR3b3JrcyI6WyJ2aXNhIiwibWFzdGVyQ2FyZCIsImFtZXgiXSwidG90YWwiOnsibGFiZWwiOiJlWmhpcmUiLCJ0eXBlIjoiZmluYWwiLCJhbW91bnQiOiIxIn19LHsidHlwZSI6Imdvb2dsZXBheSIsIm1lcmNoYW50Ijp7ImlkIjoiMDgxMTMwODkzODYyNjg4NDk5ODIiLCJuYW1lIjoiZVpoaXJlIiwib3JpZ2luIjoiaHR0cHM6Ly93d3cuZXpoaXJlLm1lIn0sInRyYW5zYWN0aW9uX2luZm8iOnsidG90YWxfcHJpY2Vfc3RhdHVzIjoiRklOQUwiLCJ0b3RhbF9wcmljZSI6IjEiLCJjb3VudHJ5X2NvZGUiOiJHQiIsImN1cnJlbmN5X2NvZGUiOiJBRUQifSwiY2FyZF9wYXJhbWV0ZXJzIjp7ImFsbG93ZWRfYXV0aF9tZXRob2RzIjpbIlBBTl9PTkxZIiwiQ1JZUFRPR1JBTV8zRFMiXSwiYWxsb3dlZF9jYXJkX25ldHdvcmtzIjpbIlZJU0EiLCJNQVNURVJDQVJEIiwiQU1FWCJdfX1dLCJmZWF0dXJlX2ZsYWdzIjpbImFuYWx5dGljc19vYnNlcnZhYmlsaXR5X2VuYWJsZWQiLCJjYXJkX2ZpZWxkc19lbmFibGVkIiwiZ2V0X3dpdGhfcHVibGljX2tleV9lbmFibGVkIiwibG9nc19vYnNlcnZhYmlsaXR5X2VuYWJsZWQiLCJyaXNrX2pzX2VuYWJsZWQiLCJ1c2VfYmlsbGluZ19hZGRyZXNzX2Zyb21fY29uZmlnX2Zvcl90b2tlbml6YXRpb24iLCJ1c2Vfbm9uX2JpY19pZGVhbF9pbnRlZ3JhdGlvbiJdLCJyaXNrIjp7ImVuYWJsZWQiOmZhbHNlfSwibWVyY2hhbnRfbmFtZSI6ImVaaGlyZSIsInBheW1lbnRfc2Vzc2lvbl9zZWNyZXQiOiJwc3NfMTViMDMwMGMtMjQ0Mi00YWYwLWIxNmUtMjNlYjQ2MThhOTBjIiwicGF5bWVudF90eXBlIjoiVW5zY2hlZHVsZWQiLCJpbnRlZ3JhdGlvbl9kb21haW4iOiJkZXZpY2VzLmFwaS5zYW5kYm94LmNoZWNrb3V0LmNvbSJ9",
//            "paymentSessionSecret" to "pss_084df275-f99e-4016-ac88-fbb353f44294",
//            "publicKey" to "pk_sbox_awubbtkehjl742o3t5v44vngcyu",
//            "email" to "android155@yomail.com",
//            "env" to "0"
//        )
//        val intent = Intent(this@MainActivity, MainActivity3::class.java)
//        intent.putExtra("userData", userDataGooglePay)
//         paymentLauncher.launch(intent)



        val sheet = PaymentBottomSheet(userDataGooglePay) { paymentId ->
            val resultIntent = Intent()
            resultIntent.putExtra("paymentId", paymentId)
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        sheet.show(supportFragmentManager, "paymentSheet")




//        val sheet = PaymentBottomSheet().apply {
//            arguments = Bundle().apply {
//                putString("id", userData["id"])
//                putString("paymentSessionToken", userData["paymentSessionToken"])
//                putString("paymentSessionSecret", userData["paymentSessionSecret"])
//                putString("publicKey", userData["publicKey"])
//                putString("email", userData["email"])
//            }
//        }
//
//        sheet.show(supportFragmentManager, "PaymentBottomSheet")


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



}