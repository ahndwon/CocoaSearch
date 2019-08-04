package xyz.thingapps.cocoasearch.net

data class SearchResponse(
        val documents: List<Document>,
        val meta: Meta
)
