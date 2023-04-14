package com.example.uberclone

import com.example.uberclone.Model.DriverInfoModel

object Common {

    var currentUser: DriverInfoModel? = null
    val DRIVER_INFO_REFERENCE: String = "DriverInfo"

    fun buildWelcomeMessage(): String {
        return StringBuilder("Добро пожаловать, ")
            .append(currentUser!!.firstName)
            .append(" ")
            .append(currentUser!!.lastName)
            .toString()
    }
}