package com.example.quixhouse.model

import android.os.Parcelable
import com.example.quixhouse.helper.FirebaseHelper
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
    var id: String = "",
    var image: String = "",
    var description: String = "",
    var locationAddress: LocationAddress = LocationAddress()
) : Parcelable {
    init {
        if(this.id == "")
            this.id = FirebaseHelper.getDatabase().push().key ?: ""
    }
}