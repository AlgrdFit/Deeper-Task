package com.deeper.deepertask.feature.login.api

interface TokenStore {
    fun read(): String?

    fun save(token: String)

    fun clear()
}
