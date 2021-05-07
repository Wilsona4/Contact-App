package com.decagon.android.sq007.util

object Validator {

    /*Function to validate Email*/
    fun validateEmail(email: String): Boolean {
        val pattern = "\\w+@[a-zA-Z_]+?\\.[a-zA-Z]{2,6}".toRegex()
        return email.isNotEmpty() && email.matches(pattern)
    }

    /*Function to Validate Phone Number*/
    fun validatePhoneNumber(phoneNumber: String): Boolean {
//        val pattern = "^(\\+234|234|0)[789][01]\\d{8}".toRegex()
//        && phoneNumber.matches(pattern)
        return phoneNumber.isNotEmpty()
    }

    /*Function to Validate FirstName*/
    fun validateFirstName(firstName: String): Boolean {
        return firstName.isNotEmpty()
    }
}
