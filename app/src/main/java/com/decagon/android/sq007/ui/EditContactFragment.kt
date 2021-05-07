package com.decagon.android.sq007.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.decagon.android.sq007.R
import com.decagon.android.sq007.databinding.FragmentEditContactBinding
import com.decagon.android.sq007.model.Contact
import com.decagon.android.sq007.util.Validator.validateEmail
import com.decagon.android.sq007.util.Validator.validateFirstName
import com.decagon.android.sq007.util.Validator.validatePhoneNumber
import com.decagon.android.sq007.viewModel.ContactViewModel

class EditContactFragment : Fragment() {

    private var _binding: FragmentEditContactBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ContactViewModel
    private val args by navArgs<EditContactFragmentArgs>()
    private lateinit var retrievedContact: Contact

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEditContactBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(ContactViewModel::class.java)

        retrievedContact = args.editContactArgs

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /*Set the Text to Selected Contact Details*/
        binding.firstNameUpdate.setText(retrievedContact.firstName)
        binding.lastNameUpdate.setText(retrievedContact.lastName)
        binding.phoneNumberUpdate.setText(retrievedContact.phoneNumber)
        binding.emailAddressUpdate.setText(retrievedContact.emailAddress)

        /*Add Contact to Database*/
        binding.tvUpdateToolbar.setOnClickListener {
            /*Retrieve View Data*/
            val firstName = binding.firstNameUpdate.text.toString().trim()
            val lastName = binding.lastNameUpdate.text.toString().trim()
            val phoneNumber = binding.phoneNumberUpdate.text.toString().trim()
            val emailAddress = binding.emailAddressUpdate.text.toString().trim()

            /*Validate Input Fields*/
            if (!validateFirstName(firstName)) {
                binding.firstNameUpdate.error = getString(R.string.error_requireld_field)
                Toast.makeText(requireContext(), "First Name is Required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!validatePhoneNumber(phoneNumber)) {
                binding.phoneNumberUpdate.error = getString(R.string.error_requireld_field)
                Toast.makeText(requireContext(), "Phone Number is Required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (emailAddress.isNotEmpty() && !validateEmail(emailAddress)) {
                binding.emailAddressUpdate.error = "Invalid Email Address"
                Toast.makeText(requireContext(), "Invalid Email Address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            retrievedContact.firstName = firstName
            retrievedContact.lastName = lastName
            retrievedContact.emailAddress = emailAddress
            retrievedContact.phoneNumber = phoneNumber

            viewModel.updateContact(retrievedContact)
            findNavController().popBackStack()
        }

        /*Check if Data Uploaded Successfully*/
        viewModel.uploadStatus.observe(
            viewLifecycleOwner,
            {
                if (it == null) {
                    Toast.makeText(requireContext(), "Contact Update Successfully", Toast.LENGTH_SHORT)
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
