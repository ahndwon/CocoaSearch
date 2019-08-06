package xyz.thingapps.cocoasearch.repository

import xyz.thingapps.cocoasearch.net.Document

interface ImageRepository {
    fun imageSearchResult(searchWord: String, sortType: String, pageSize: Int): Listing<Document>
}