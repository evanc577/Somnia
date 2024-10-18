package dev.evanchang.somnia.appSettings

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Keep
@Serializable
@Parcelize
data class AccountSettings(
    // ID
    val user: String,

    // Auth
    val refreshToken: String,
    val bearerToken: BearerToken,
    val redirectUri: String,
) : Parcelable

@Keep
@Serializable
@Parcelize
data class BearerToken(
    val token: String,
) : Parcelable
