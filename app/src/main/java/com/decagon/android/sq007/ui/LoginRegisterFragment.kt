package com.decagon.android.sq007.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.decagon.android.sq007.BuildConfig
import com.decagon.android.sq007.R
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.firebase.ui.auth.ui.email.TroubleSigningInFragment.TAG
import com.google.firebase.auth.FirebaseAuth

class LoginRegisterFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*Check if User is Already Signed In*/

        if (auth.currentUser != null) {
            // already signed in
            findNavController().navigate(R.id.action_loginRegisterFragment_to_listFragment)
        } else {
            // not signed in
            createSignInIntent()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login_register, container, false)
    }

    private fun createSignInIntent() {
        // [START auth_fui_create_intent]
        // Select authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(!BuildConfig.DEBUG, true)
                .setLogo(R.drawable.google_contacts_icon_68_128x128)
                .setTheme(R.style.LoginTheme)
                .build(),
            RC_SIGN_IN
        )
        // [END auth_fui_create_intent]
    }

    // [START auth_fui_result]
    @SuppressLint("LongLogTag")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                loadListFragment()
            } else {
                /*Sign in failed*/
                /*If response is null the user canceled the sign-in flow using the back button.*/

                if (response != null && response.error != null) {
                    val fullCredential = response.credentialForLinking
                    if (fullCredential != null) {

                        auth.signInWithCredential(fullCredential)
                            .addOnSuccessListener {
                                loadListFragment()
                            }
                    }
                } else if (response == null) {
                    // User pressed back button
                    Toast.makeText(requireContext(), "User Cancelled Sign-In", Toast.LENGTH_SHORT)
                        .show()
                    return
                }
                /*No Internet Connection*/
                else if (response.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_SHORT)
                        .show()
                    return
                }
                /*Otherwise Get Error Type*/
                Toast.makeText(requireContext(), "Unknown Error", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Sign-in error: ", response.error)
            }
        }
    }
    // [END auth_fui_result]

    /*Method to Navigate to List Fragment*/
    private fun loadListFragment() {
        findNavController().navigate(R.id.action_loginRegisterFragment_to_listFragment)
    }

    companion object {
        val auth = FirebaseAuth.getInstance()
        private const val RC_SIGN_IN = 123
        private const val USER = "user"
    }
}
