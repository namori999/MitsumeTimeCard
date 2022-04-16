package com.example.mitsumetimecard.user



import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
data class User(val Name: String? = null, val Visibility: String? = null)  {

    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.

}
