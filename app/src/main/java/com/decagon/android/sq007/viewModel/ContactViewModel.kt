package com.decagon.android.sq007.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.decagon.android.sq007.model.Contact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ContactViewModel : ViewModel() {

    private val database = Firebase.database
    private val dbContactsRef = database.getReference(CONTACTS)
    private val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    /*Variable to store upload Status (success/failure)*/
    private val _uploadStatus = MutableLiveData<Exception?>()
    val uploadStatus: LiveData<Exception?> get() = _uploadStatus

    /*Variable to Listen for Contact Change*/
    private var _contactStatus = MutableLiveData<List<Contact>>()
    val contactStatus: LiveData<List<Contact>> get() = _contactStatus

    /*Function to Add New Contact*/
    fun addContact(contact: Contact) {
        contact.id = dbContactsRef.push().key
        dbContactsRef.child(user!!.uid).child(contact.id!!).setValue(contact)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _uploadStatus.value = null
                } else {
                    _uploadStatus.value = it.exception
                }
            }
    }

    /*Create a Database Event Listener*/
    private val contactEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val contacts = mutableListOf<Contact>()
            if (snapshot.exists()) {
                for (contactSnapshot in snapshot.children) {
                    val contact = contactSnapshot.getValue(Contact::class.java)

                    contact?.id = contactSnapshot.key

                    contact?.let { contacts.add(it) }
                }
            } else {
                contacts.clear()
            }
            _contactStatus.value = contacts
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }
    }

    /*Get Real-Time Changes in the Database*/
    fun displayContactChanges() {
        dbContactsRef.child(user!!.uid).addValueEventListener(contactEventListener)
    }

    /*Update Contact in Database*/
    fun updateContact(contact: Contact) {
        if (contact.id?.let { dbContactsRef.child(user!!.uid).child(it) } != null) {
            dbContactsRef.child(user!!.uid).child(contact.id!!).setValue(contact)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        _uploadStatus.value = null
                    } else {
                        _uploadStatus.value = it.exception
                    }
                }
        }
    }

    /*Delete Contact from DataBase*/
    fun deleteContact(contact: Contact) {
        dbContactsRef.child(user!!.uid).child(contact.id!!).setValue(null)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _uploadStatus.value = null
                } else {
                    _uploadStatus.value = it.exception
                }
            }
    }

    /*Delete All Contacts from DataBase*/
    fun deleteAllContacts() {
        dbContactsRef.child(user!!.uid).setValue(null).addOnCompleteListener {
            if (it.isSuccessful) {
                _uploadStatus.value = null
            } else {
                _uploadStatus.value = it.exception
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        dbContactsRef.removeEventListener(contactEventListener)
    }

    companion object {
        const val CONTACTS = "contacts"
    }
}
