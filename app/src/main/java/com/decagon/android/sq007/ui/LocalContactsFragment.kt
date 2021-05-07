package com.decagon.android.sq007.ui

import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.decagon.android.sq007.adapter.LocalContactAdapter
import com.decagon.android.sq007.databinding.FragmentLocalContactsBinding
import com.decagon.android.sq007.model.Contact
import com.decagon.android.sq007.util.LocalContactActions.addContact
import com.decagon.android.sq007.util.LocalContactActions.getContacts
import com.decagon.android.sq007.util.LocalContactActions.localContactList

class LocalContactsFragment : Fragment(), LocalContactAdapter.Interaction {

    private var _binding: FragmentLocalContactsBinding? = null
    private val binding get() = _binding!!

    private lateinit var localContactsAdapter: LocalContactAdapter
    private lateinit var progressBar: ProgressBar
    private val RECORD_REQUEST_CODE = 10

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLocalContactsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = binding.localProgressBar
        /*Check Permission and load user data*/
        setUpPermission()
    }

    /*Function to check and request permission.*/
    private fun setUpPermission() {
        val permission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.READ_CONTACTS
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makePermissionRequest()
        } else {
            progressBar.visibility = View.VISIBLE
            getLocalContacts()
            progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(ContentValues.TAG, "Permission has been denied by user")
                    Toast.makeText(
                        requireContext(),
                        "Read Contacts permission denied",
                        Toast.LENGTH_SHORT
                    ).show()
                    displayPermissionErrorMessage()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Read Contacts permission Granted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    /*Make Permission Request*/
    private fun makePermissionRequest() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.READ_CONTACTS),
            RECORD_REQUEST_CODE
        )
    }

    /*Displays error message and Rationale if the user refuses to grant permission*/
    private fun displayPermissionErrorMessage() {
        AlertDialog.Builder(requireContext()).also {
            it.setTitle("App needs Real_Contacts Permissions to Perform Effectively")
            it.setPositiveButton(
                "YES",
                DialogInterface.OnClickListener { dialogInterface, i ->
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(android.Manifest.permission.READ_CONTACTS),
                        RECORD_REQUEST_CODE
                    )
                }
            ).create().show()
        }
    }

    /*Gets all the Contacts on the phone and Add them to the Remote DataBase*/
    private fun getLocalContacts() {
        val contactList = getContacts()

        if (contactList.isEmpty()) {
            val cursor = activity?.contentResolver?.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
                null, null
            )
            val count = 0

            while (cursor!!.moveToNext() && count < 100) {
                val name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                val contact = Contact()
                contact.firstName = name
                contact.phoneNumber = phoneNumber

                addContact(contact)
            }

            cursor.close()
        }
        Log.d("CHECKER", "OKAY LIST ${getContacts()}")

        /*Set Contacts to Recycler view*/
        binding.localRecyclerView.apply {
            val list = contactList.sortedWith(compareBy { it.firstName })
            Log.d("CHECKER", "OKAY LIST $contactList")
            layoutManager = LinearLayoutManager(activity)
            localContactsAdapter = LocalContactAdapter(list, this@LocalContactsFragment)
            adapter = localContactsAdapter
        }
    }

    override fun onItemSelected(position: Int, item: Contact) {
        Toast.makeText(requireActivity(), localContactList[position].firstName, Toast.LENGTH_SHORT)
            .show()

        val currentContact = localContactList[position]
        val action = LocalContactsFragmentDirections.actionLocalContactsFragmentToDetailFragment2(
            currentContact
        )
        findNavController().navigate(action)
    }
}
