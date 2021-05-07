package com.decagon.android.sq007.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.decagon.android.sq007.R
import com.decagon.android.sq007.databinding.FragmentAddContactBinding
import com.decagon.android.sq007.model.Contact
import com.decagon.android.sq007.util.Validator.validateEmail
import com.decagon.android.sq007.util.Validator.validateFirstName
import com.decagon.android.sq007.util.Validator.validatePhoneNumber
import com.decagon.android.sq007.viewModel.ContactViewModel

class AddContactFragment : Fragment() {

    private var _binding: FragmentAddContactBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ContactViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddContactBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(ContactViewModel::class.java)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /*Add Contact to Database*/
        binding.tvSaveToolbar.setOnClickListener {
            /*Retrieve View Data*/
            val firstName = binding.firstNameAdd.text.toString().trim()
            val lastName = binding.lastNameAdd.text.toString().trim()
            val phoneNumber = binding.phoneNumberAdd.text.toString().trim()
            val emailAddress = binding.emailAddressAdd.text.toString().trim()

            /*Validate Input Fields*/
            if (!validateFirstName(firstName)) {
                binding.firstNameAdd.error = getString(R.string.error_requireld_field)
                Toast.makeText(requireContext(), "First Name is Required", Toast.LENGTH_SHORT)
                return@setOnClickListener
            }

            if (!validatePhoneNumber(phoneNumber)) {
                binding.phoneNumberAdd.error = getString(R.string.error_requireld_field)
                return@setOnClickListener
            }
            if (emailAddress.isNotEmpty() && !validateEmail(emailAddress)) {
                binding.emailAddressAdd.error = "Invalid Email Address"
                return@setOnClickListener
            }

            val contact = Contact()
            contact.firstName = firstName
            contact.lastName = lastName
            contact.emailAddress = emailAddress
            contact.phoneNumber = phoneNumber

            viewModel.addContact(contact)
            findNavController().popBackStack()
        }

        /*Check if Data Uploaded Successfully*/
        viewModel.uploadStatus.observe(
            viewLifecycleOwner,
            {
                if (it == null) {
                    Toast.makeText(requireContext(), "Contact Added Successfully", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
