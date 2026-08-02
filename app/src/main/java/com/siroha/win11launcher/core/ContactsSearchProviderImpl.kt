package com.siroha.win11launcher.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.siroha.feature.search.ContactsSearchProvider
import com.siroha.feature.search.SearchResultItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsSearchProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ContactsSearchProvider {

    override val hasPermission: Boolean
        get() = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

    override suspend fun search(query: String): List<SearchResultItem.ContactResult> {
        if (!hasPermission || query.isBlank()) return emptyList()

        return withContext(Dispatchers.IO) {
            val results = mutableListOf<SearchResultItem.ContactResult>()
            val projection = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
            )
            val selection = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"
            val selectionArgs = arrayOf("%$query%")

            context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC LIMIT 10"
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
                val lookupIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY)
                val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)

                while (cursor.moveToNext()) {
                    results += SearchResultItem.ContactResult(
                        id = cursor.getString(idIndex),
                        title = cursor.getString(nameIndex).orEmpty(),
                        subtitle = "Contact",
                        lookupKey = cursor.getString(lookupIndex)
                    )
                }
            }

            results
        }
    }
}
