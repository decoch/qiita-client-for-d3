package com.example.tominaga.qiita_client_d3

data class Item(
        val id: String?,
        val title: String?,
        val body: String?,
        val url: String?,
        val comments_count: Int?,
        val likes_count: Int?,
        val created_at: String?,
        val user: User?
)
