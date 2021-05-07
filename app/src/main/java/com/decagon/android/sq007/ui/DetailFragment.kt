package com.decagon.android.sq007.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.decagon.android.sq007.R
import com.decagon.android.sq007.databinding.FragmentDetailBinding
import com.decagon.android.sq007.model.Contact
import com.decagon.android.sq007.util.LocalContactActions
import com.decagon.android.sq007.viewModel.ContactViewModel

class DetailFragment : Fragment() {

    private val args by navArgs<DetailFragmentArgs>()
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var retrievedContact: Contact
    private lateinit var viewModel: ContactViewModel
    private lateinit var toolbar: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ContactViewModel::class.java)
        retrievedContact = args.detailContactArgs

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*Initialise Toolbar and Set Menu Item*/
        toolbar = binding.toolbar
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.mEdit -> {
                    val action = DetailFragmentDirections.actionDetailFragmentToEditContactFragment(
                        retrievedContact
                    )
                    findNavController().navigate(action)
                    true
                }
                R.id.mShare -> {
                    shareContact()
                    true
                }
                R.id.mDelete -> {
                    deleteContact()
                    true
                }

                else -> false
            }
        }

        binding.tvDetailsToolbar.text = "${retrievedContact.firstName} ${retrievedContact.lastName}"
        binding.tvDetailNumber.text = retrievedContact.phoneNumber
        binding.tvDetailEmail.text = retrievedContact.emailAddress

        /*Make Call*/
        binding.ivDetailsCall.setOnClickListener {
            val phoneIntent = Intent(Intent.ACTION_DIAL)
            val uri = Uri.parse("tel:" + retrievedContact.phoneNumber)
            phoneIntent.data = uri

            startActivity(phoneIntent)
        }

        /*Send SMS*/
        binding.ivDetailsMessage.setOnClickListener {
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                type = "text/plain"
                data = Uri.parse("smsto: ${retrievedContact.phoneNumber}")
            }
            startActivity(smsIntent)
        }

        /*Send Email*/
        binding.cvDetailEmail.setOnClickListener {
            val emailAddress = binding.tvDetailEmail.text

            if (emailAddress.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto: ${retrievedContact.emailAddress}") // only email apps should handle this
                    putExtra(Intent.EXTRA_EMAIL, emailAddress)
                }
                startActivity(intent)
            } else return@setOnClickListener
        }
    }

    /*Create Menu*/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.details_menu, menu)
    }

    /*Handle Menu Item Selections*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mEdit -> {
                val action = DetailFragmentDirections.actionDetailFragmentToEditContactFragment(
                    retrievedContact
                )
                findNavController().navigate(action)
                true
            }
            R.id.mShare -> {
                shareContact()
                true
            }
            R.id.mDelete -> {
                deleteContact()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /*Share contact*/
    private fun shareContact() {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "${retrievedContact.firstName} \n ${retrievedContact.lastName} " +
                    "\n ${retrievedContact.phoneNumber} \n ${retrievedContact.emailAddress}"
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(intent, null)
        startActivity(shareIntent)
    }

    /*Delete Contact*/
    private fun deleteContact() {
        AlertDialog.Builder(requireContext()).also {
            it.setTitle("Are You Sure You Want To Delete This Contact?")
            it.setPositiveButton("YES") { dialog, which ->

                /*Check Navigation Start Destination*/
                if (findNavController().previousBackStackEntry?.destination?.id == R.id.localContactsFragment) {
                    LocalContactActions.deleteContact(retrievedContact)
                } else {
                    viewModel.deleteContact(retrievedContact)
                }

                findNavController().popBackStack()
                Toast.makeText(requireContext(), "Contact Deleted Successfully", Toast.LENGTH_SHORT).show()
            }
            it.setNegativeButton("NO") { dialog, which ->
                dialog.cancel()
            }
        }.create().show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
