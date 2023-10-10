package com.example.quixhouse.model

import android.os.Parcelable
import com.example.quixhouse.helper.FirebaseHelper
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
    var id: String = "",
    var image: String = "",
    var decription: String = ""
) : Parcelable {
    init {
        this.id = FirebaseHelper.getDatabase().push().key ?: ""
    }
}