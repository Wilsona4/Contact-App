package com.decagon.android.sq007.ui

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.decagon.android.sq007.R
import com.decagon.android.sq007.adapter.RemoteContactsAdapter
import com.decagon.android.sq007.adapter.RemoteContactsAdapter.*
import com.decagon.android.sq007.databinding.FragmentListBinding
import com.decagon.android.sq007.model.Contact
import com.decagon.android.sq007.viewModel.ContactViewModel
import com.firebase.ui.auth.AuthUI

class ListFragment : Fragment(), Interaction {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var remoteContactsAdapter: RemoteContactsAdapter
    private lateinit var viewModel: ContactViewModel
    private lateinit var progressBar: ProgressBar

    private lateinit var toolbar: Toolbar
    private val RECORD_REQUEST_CODE = 10
    lateinit var contactList: List<Contact>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpPermission()
        /*Handle Back Press*/
        activity?.onBackPressedDispatcher?.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activity?.finish()
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentListBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ContactViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = binding.progressBar
        /*Initialise Toolbar and Set Menu Item*/
        toolbar = binding.listToolbar

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.mSearch -> {
                    true
                }
                R.id.mSettings -> {
                    findNavController().navigate(R.id.action_listFragment_to_profileFragment)
                    true
                }
                R.id.mLocalContacts -> {
                    findNavController().navigate(R.id.action_listFragment_to_localContactsFragment)
                    true
                }
                R.id.mSync -> {
                    progressBar.visibility = View.VISIBLE
                    syncLocalContacts()
                    progressBar.visibility = View.INVISIBLE
                    true
                }
                R.id.mDeleteAll -> {
                    deleteAllContacts()
                    true
                }
                R.id.mSignOut -> {
                    Toast.makeText(requireActivity(), "Bye-Bye", Toast.LENGTH_SHORT).show()
                    signOut()
                    true
                }
                else -> false
            }
        }

        /*Initialise RecyclerView*/
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            remoteContactsAdapter = RemoteContactsAdapter(this@ListFragment)
            adapter = remoteContactsAdapter
        }

        /*Configure FAB*/
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_addContactFragment)
        }

        /*Observe Contacts List LiveData*/
        viewModel.contactStatus.observe(
            viewLifecycleOwner,
            Observer {
                remoteContactsAdapter.submitList(it)
                contactList = it
            }
        )

        viewModel.displayContactChanges()

        /*Attach our Swipe Implementation to our Recycler View*/
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    /*Implement Delete and Call Contact Swipe Functions*/
    private var simpleCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val swipedContact = remoteContactsAdapter.differ.currentList[position]

                when (direction) {
                    ItemTouchHelper.RIGHT -> {

                        AlertDialog.Builder(requireContext()).also {
                            it.setTitle("Are You Sure You Want To Delete This Contact?")
                            it.setPositiveButton("YES") { dialog, which ->
                                viewModel.deleteContact(swipedContact)
                                Toast.makeText(
                                    requireContext(),
                                    "Contact Deleted Successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            it.setNegativeButton("NO") { dialog, which ->
                                dialog.cancel()
                            }
                        }.create().show()
                    }
                    ItemTouchHelper.LEFT -> {
                        /*Make a call*/
                        val phoneIntent = Intent(Intent.ACTION_DIAL)
                        val uri = Uri.parse("tel:" + swipedContact.phoneNumber)
                        phoneIntent.data = uri

                        startActivity(phoneIntent)
                    }
                }

                binding.recyclerView.adapter?.notifyDataSetChanged()
            }
        }

    /*Function to check and request permission.*/
    private fun setUpPermission() {
        val permission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.READ_CONTACTS
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makePermissionRequest()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission has been denied by user")
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
    private fun syncLocalContacts() {

        val cursor = activity?.contentResolver?.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
            null, null
        )

        var count = 0
        while (cursor!!.moveToNext() && count < 100) {
            val name =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            val contact = Contact()
            contact.firstName = name
            contact.phoneNumber = phoneNumber

            viewModel.addContact(contact)

            count++
        }

        cursor.close()
    }

    /*Method to Delete All contacts from the Database*/
    private fun deleteAllContacts() {
        AlertDialog.Builder(requireContext()).also {
            it.setTitle("Are You Sure You Want To Delete All Your Contacts ?")
            it.setPositiveButton("YES") { dialog, which ->
                viewModel.deleteAllContacts()
                binding.recyclerView.adapter?.notifyDataSetChanged()
                Toast.makeText(
                    requireContext(),
                    "Contact Deleted Successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
            it.setNegativeButton("NO") { dialog, which ->
                dialog.cancel()
            }
        }.create().show()
    }

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

    /*Handle Click on List Item*/
    override fun onItemSelected(position: Int, item: Contact) {
        Toast.makeText(requireActivity(), contactList[position].firstName, Toast.LENGTH_SHORT)
            .show()

        val currentContact = contactList[position]
        val action = ListFragmentDirections.actionListFragmentToDetailFragment(currentContact)
        findNavController().navigate(action)
    }
}
