package com.decagon.android.sq007.ui

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.decagon.android.sq007.databinding.FragmentResetPasswordDialogBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ResetPasswordDialog : DialogFragment() {

    private var _binding: FragmentResetPasswordDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentResetPasswordDialogBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /*Reset Password*/
        binding.btPasswordReset.setOnClickListener {
            val password = binding.setPassword.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()
            /*Check if both fields are same*/
            if (password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    // [START update_password]
                    val user = Firebase.auth.currentUser
                    user!!.updatePassword(password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "User password updated.")
                            }
                        }
                    Toast.makeText(requireContext(), "Password Reset Successful", Toast.LENGTH_SHORT).show()
                    // [END update_password]
                } else {
                    binding.confirmPassword.error = "Both Fields Do Not Match"
                }
            }
        }
    }
}
