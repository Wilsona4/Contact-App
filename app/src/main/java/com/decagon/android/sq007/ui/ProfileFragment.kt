package com.decagon.android.sq007.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.decagon.android.sq007.R
import com.decagon.android.sq007.databinding.FragmentProfileBinding
import com.decagon.android.sq007.util.Validator
import com.firebase.ui.auth.AuthUI
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private val TAG = "ProfileFragment"

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var firstName: TextInputEditText
    private lateinit var emailAddress: TextInputEditText
    private lateinit var btSave: TextView
    private lateinit var toolbar: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        /*Instantiating Views*/
        firstName = binding.firstNameProfile
        emailAddress = binding.emailAddressProfile
        btSave = binding.tvSaveProfile

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*Get User Profile*/
        getUserProfile()

        binding.tvSaveProfile.setOnClickListener {
            updateProfile()
            if (!emailAddress.text.isNullOrEmpty()) {
                updateEmail()
            }
            findNavController().popBackStack()
        }

        /*Initialise Toolbar and Set Menu Item*/
        toolbar = binding.profileToolbar
//        toolbar.inflateMenu(R.menu.profile_menu)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.mChangePassword -> {
                    /*Call the Reset Password Dialog*/
                    ResetPasswordDialog().show(childFragmentManager, "D")
                    true
                }
                R.id.mResetPassword -> {
                    sendPasswordReset()
                    true
                }
                R.id.mDeleteAccount -> {
                    deleteUser()
                    true
                }
                else -> false
            }
        }
    }

    /*Get User Profile*/
    private fun getUserProfile() {
        // [START get_user_profile]
        val user = Firebase.auth.currentUser
        user?.let {
            // Get Name and email address
            val name = user.displayName
            val email = user.email

            /*Set User Profile in Views*/
            firstName.setText(name)
            emailAddress.setText(email)
        }
        // [END get_user_profile]
    }

    /*Update User Profile*/
    private fun updateProfile() {
        // [START update_profile]
        val user = Firebase.auth.currentUser

        val profileUpdates = userProfileChangeRequest {
            displayName = firstName.text.toString()
        }

        user!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User profile updated.")
                }
            }
        // [END update_profile]
    }

    /*Update Email*/
    private fun updateEmail() {
        // [START update_email]
        val user = Firebase.auth.currentUser
        val newEmail = emailAddress.text.toString()

        /*Check if Email is Valid and Doesn't match the current mail*/
        if (Validator.validateEmail(newEmail)) {
            user!!.updateEmail(newEmail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User email address updated.")
                    }
                }
            Toast.makeText(requireContext(), "Profile Update Successful", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Invalid Mail", Toast.LENGTH_SHORT).show()
        }

        // [END update_email]
    }

    private fun sendPasswordReset() {
        // [START send_password_reset]
        val user = Firebase.auth.currentUser
        val emailAddress = user!!.email
        if (emailAddress != null) {
            AlertDialog.Builder(requireContext()).also {
                it.setTitle("Are You Sure You Want To Reset Your Password ?")
                it.setPositiveButton("YES") { dialog, which ->
                    Firebase.auth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "Email sent.")
                            }
                        }
                    Toast.makeText(requireContext(), "Password Reset Successful. \nCheck Mail for New Password", Toast.LENGTH_SHORT).show()
                    signOut()
                    Toast.makeText(
                        requireContext(),
                        "We will Miss You",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                it.setNegativeButton("NO") { dialog, which ->
                    dialog.cancel()
                }
            }.create().show()
        } else {
            Toast.makeText(requireContext(), "No User Mail in Database", Toast.LENGTH_SHORT).show()
        }

        // [END send_password_reset]
    }

    private fun deleteUser() {
        // [START delete_user]
        val user = Firebase.auth.currentUser!!
        AlertDialog.Builder(requireContext()).also {
            it.setTitle("Are You Sure You Want To Delete Your Account ?")
            it.setPositiveButton("YES") { dialog, which ->
                user.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "User account deleted.")
                        }
                    }
                signOut()
                Toast.makeText(
                    requireContext(),
                    "We will Miss You",
                    Toast.LENGTH_SHORT
                ).show()
            }
            it.setNegativeButton("NO") { dialog, which ->
                dialog.cancel()
            }
        }.create().show()

        // [END delete_user]
    }

    /*Sign Out*/
    private fun signOut() {
        // [START auth_fui_signOut]
        AuthUI.getInstance()
            .signOut(requireContext())
            .addOnCompleteListener {
                findNavController().navigate(R.id.loginRegisterFragment)
            }
        // [END auth_fui_signOut]
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
