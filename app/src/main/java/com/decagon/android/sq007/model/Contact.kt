package com.decagon.android.sq007.model

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contact(
    @get:Exclude
    var id: String? = null,
    var firstName: String? = "",
    var lastName: String? = "",
    var phoneNumber: String? = "",
    var emailAddress: String? = "",

    @get:Exclude
    var isDeleted: Boolean = false
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        return if (other is Contact) {
            other.id == id
        } else false
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (firstName?.hashCode() ?: 0)
        result = 31 * result + (lastName?.hashCode() ?: 0)
        result = 31 * result + (phoneNumber?.hashCode() ?: 0)
        result = 31 * result + (emailAddress?.hashCode() ?: 0)
        result = 31 * result + isDeleted.hashCode()
        return result
    }
}
