package com.decagon.android.sq007.util

import com.decagon.android.sq007.model.Contact

object LocalContactActions {

    var localContactList: MutableList<Contact> = ArrayList()

    fun addContact(item: Contact) {
        localContactList.add(item)
    }

    fun deleteContact(item: Contact) {
        localContactList.remove(item)
    }

    fun getContacts(): List<Contact> {
        return localContactList
    }

    fun clearContacts() {
        localContactList.clear()
    }
}
