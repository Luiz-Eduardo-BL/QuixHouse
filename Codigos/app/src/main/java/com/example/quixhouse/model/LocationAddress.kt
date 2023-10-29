package com.example.quixhouse.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationAddress(
    var address: String = "",
    var numberAp: String = "",
    var neighborhood: String = "",
    var city: String = "",
    var state: String = "",
    var zipCode: String = "",
    var country: String = "",
    var latitude: Double? = null,
    var longitude: Double? = null,
    ) : Parcelable
