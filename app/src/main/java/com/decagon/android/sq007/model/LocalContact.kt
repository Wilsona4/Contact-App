package com.decagon.android.sq007.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocalContact(
    var firstName: String = "",
    var phoneNumber: String = "",
) : Parcelable
