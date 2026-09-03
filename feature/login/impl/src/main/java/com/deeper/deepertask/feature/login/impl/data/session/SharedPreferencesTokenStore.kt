package com.deeper.deepertask.feature.login.impl.data.session

import android.content.Context
import com.deeper.deepertask.feature.login.api.TokenStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.core.content.edit

internal class SharedPreferencesTokenStore @Inject constructor(
    @ApplicationContext context: Context,
) : TokenStore {
    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    override fun read(): String? = preferences
        .getString(TOKEN_KEY, null)
        ?.takeIf(String::isNotBlank)

    override fun save(token: String) {
        preferences.edit { putString(TOKEN_KEY, token) }
    }

    override fun clear() {
        preferences.edit { remove(TOKEN_KEY) }
    }

    private companion object {
        const val PREFERENCES_NAME = "authentication"
        const val TOKEN_KEY = "token"
    }
}
