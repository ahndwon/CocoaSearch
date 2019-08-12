package xyz.thingapps.cocoasearch.repository

import xyz.thingapps.cocoasearch.vo.Document

interface ImageRepository {
    fun imageSearchResult(searchWord: String, sortType: String, pageSize: Int): Listing<Document>
}