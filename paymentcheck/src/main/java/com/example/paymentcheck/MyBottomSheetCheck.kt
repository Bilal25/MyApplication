import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.checkout.components.core.CheckoutComponentsFactory
import com.checkout.components.interfaces.Environment
import com.checkout.components.interfaces.component.CheckoutComponentConfiguration
import com.checkout.components.interfaces.component.ComponentCallback
import com.checkout.components.interfaces.model.PaymentMethodName
import com.checkout.components.interfaces.model.PaymentSessionResponse
import com.checkout.components.wallet.wrapper.GooglePayFlowCoordinator
import com.example.paymentcheck.OnDataPass
import com.example.paymentcheck.databinding.LayoutFlexibleBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BottomSheetFragment(var userData1: HashMap<String, String>) : BottomSheetDialogFragment() {

    private var _binding: LayoutFlexibleBottomSheetBinding? = null
    private lateinit var coordinator: GooglePayFlowCoordinator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutFlexibleBottomSheetBinding.inflate(inflater, container, false)
       // containerView = findViewById(R.id.checkoutContainer)

        MainScope().launch {
           // val userData = activity?.intent?.getSerializableExtra("userData") as? HashMap<String, String>
            var id = ""
            var paymentSessionToken = ""
            var paymentSessionSecret = ""
            var publicKey = ""
            userData1?.let {
                id = it["id"]!!
                paymentSessionToken = it["paymentSessionToken"]!!
                paymentSessionSecret = it["paymentSessionSecret"]!!
                publicKey = it["publicKey"]!!
            }
            checkoutWithGoogle(id,paymentSessionToken,paymentSessionSecret,publicKey)
        }

        return _binding!!.root
    }
    private suspend fun checkoutWithGoogle(
        id: String,
        paymentSessionToken: String,
        paymentSessionSecret: String,
        publicKey: String
    ) {
        try {



            // ✅ Initialize the coordinator
            coordinator = GooglePayFlowCoordinator(
                context = requireActivity(),
                handleActivityResult = { resultCode, data ->
                    Log.d("GooglePay", "Activity result received: $resultCode")
                }
            )

            val flowCoordinators = mapOf(
                PaymentMethodName.GooglePay to coordinator
            )

            val configuration = CheckoutComponentConfiguration(
                context = requireActivity(),
                paymentSession = PaymentSessionResponse(
                    id = id,
                    paymentSessionToken = paymentSessionToken,
                    paymentSessionSecret = paymentSessionSecret
                ),
                publicKey = publicKey,
                environment = Environment.SANDBOX,
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
                        Log.e("Checkout", "❌ onError ${component.name}: $checkoutError")
                    }
                ),
                flowCoordinators = flowCoordinators
            )

            val checkoutComponents = CheckoutComponentsFactory(config = configuration).create()

            val cardComponent = checkoutComponents.create(PaymentMethodName.Card)
            val googlePayComponent = checkoutComponents.create(PaymentMethodName.GooglePay)

            withContext(Dispatchers.Main) {
                _binding?.checkoutContainer?.removeAllViews()
                _binding?.checkoutContainer?.addView(cardComponent.provideView( _binding!!.checkoutContainer))
                _binding?.checkoutContainer?.addView(googlePayComponent.provideView( _binding!!.checkoutContainer))
                Log.d("Checkout", "✅ Card & Google Pay views added")
            }
        }catch (e : Exception){
            e.stackTrace
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//
//        binding.ivCloseBtn.setOnClickListener {
//            dismiss()
//        }


    }

    override fun onStart() {
        super.onStart()
        // Optional: Make full height if needed
//        dialog?.let { dialog ->
//            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//            bottomSheet?.let {
//                val behavior = BottomSheetBehavior.from(it)
//                behavior.state = BottomSheetBehavior.STATE_EXPANDED
//                behavior.skipCollapsed = true
//            }
//        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    internal var itemClickedLocation: OnDataPass = object : OnDataPass {
        override fun onDataSend(data: HashMap<String, Any>) {
            dismiss()
        }

        override fun onITemClick(data: String) {
            dismiss()
        }

    }


}
