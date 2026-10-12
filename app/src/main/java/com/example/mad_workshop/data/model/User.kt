package com.example.mad_workshop.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("name") val name: Name?,
    @SerializedName("address") val address: Address?,
    @SerializedName("phone") val phone: String?
) {
    val fullName: String
        get() = if (name != null) "${name.firstname.replaceFirstChar { it.uppercase() }} ${name.lastname.replaceFirstChar { it.uppercase() }}" else username

    val fullAddress: String
        get() = if (address != null) "${address.number} ${address.street}, ${address.city.replaceFirstChar { it.uppercase() }} ${address.zipcode}" else "N/A"

    val membershipTier: String
        get() = when (id % 3) {
            0 -> "VIP Gold Member"
            1 -> "Premium Shopper"
            else -> "Standard Member"
        }
}

data class Name(
    @SerializedName("firstname") val firstname: String,
    @SerializedName("lastname") val lastname: String
)

data class Address(
    @SerializedName("city") val city: String,
    @SerializedName("street") val street: String,
    @SerializedName("number") val number: Int,
    @SerializedName("zipcode") val zipcode: String
)
