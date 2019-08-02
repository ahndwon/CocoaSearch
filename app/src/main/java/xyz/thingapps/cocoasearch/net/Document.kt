package xyz.thingapps.cocoasearch.net

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
)