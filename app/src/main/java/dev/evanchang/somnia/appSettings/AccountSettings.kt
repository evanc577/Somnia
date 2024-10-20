package dev.evanchang.somnia.appSettings

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Keep
@Serializable
@Parcelize
data class AccountSettings(
    // Auth
    val clientId: String,
    val refreshToken: String,
    val redirectUri: String,
) : Parcelable