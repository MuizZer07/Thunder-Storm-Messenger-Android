package com.muizzer07.thunderstormmessenger.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uid: String, val username: String, val profileImageUrl: String, val Token: String): Parcelable {
    constructor() : this("", "", "", "")
}
