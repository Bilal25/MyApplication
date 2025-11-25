package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.example.paymentcheck.CheckActivity
import com.example.paymentcheck.MainActivity2
import com.example.paymentcheck.MainActivity3
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

class MainActivity : AppCompatActivity()  {
    // val CALLBACK_URL = "myapp://payment/callback"
//    private val CALLBACK_URL = "myapp://payment/callback"
//    private lateinit var coordinator: GooglePayFlowCoordinator
    var listener: OnDataPass? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)







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