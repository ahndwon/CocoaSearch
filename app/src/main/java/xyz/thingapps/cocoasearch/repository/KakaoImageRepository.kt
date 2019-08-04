package xyz.thingapps.cocoasearch.repository

import xyz.thingapps.cocoasearch.net.Document

interface KakaoImageRepository {
    fun imageSearchResults(searchWord: String, pageNum: Int, pageSize: Int): Listing<Document>
}