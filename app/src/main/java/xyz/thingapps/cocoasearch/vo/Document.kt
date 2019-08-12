package xyz.thingapps.cocoasearch.vo

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Document(
        val collection: String,
        val datetime: String,
        val height: Int,
        val width: Int,
        @SerializedName("display_sitename") val displaySiteName: String,
        @SerializedName("doc_url") val docUrl: String,
        @SerializedName("image_url") val imageUrl: String,
        @SerializedName("thumbnail_url") val thumbnailUrl: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(collection)
        parcel.writeString(datetime)
        parcel.writeInt(height)
        parcel.writeInt(width)
        parcel.writeString(displaySiteName)
        parcel.writeString(docUrl)
        parcel.writeString(imageUrl)
        parcel.writeString(thumbnailUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Document> {
        override fun createFromParcel(parcel: Parcel): Document {
            return Document(parcel)
        }

        override fun newArray(size: Int): Array<Document?> {
            return arrayOfNulls(size)
        }
    }

}