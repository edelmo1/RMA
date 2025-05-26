package com.example.spirala_1

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BiljkeRepository {

    suspend fun saveBiljka(context: Context, biljka: Biljka): Boolean? {
        return withContext(Dispatchers.IO) {
            val db = BiljkaDatabase.getInstance(context)
            val result = db?.biljkaDao()?.saveBiljka(biljka)
            return@withContext result
        }
    }

    suspend fun addImage(context: Context, biljka: Biljka,bitmap: Bitmap): Boolean? {
        return withContext(Dispatchers.IO) {
            val db = BiljkaDatabase.getInstance(context)
            val result = biljka.id?.let { db?.biljkaDao()?.addImage(it,bitmap) }
            return@withContext result
        }
    }

    suspend fun getAllBiljkas(context: Context): List<Biljka>? {
        return withContext(Dispatchers.IO) {
            val db = BiljkaDatabase.getInstance(context)
            val result = db?.biljkaDao()?.getAllBiljkas()
            return@withContext result
        }
    }

    suspend fun deleteAllBiljkas(context: Context): List<Biljka>? {
        return withContext(Dispatchers.IO) {
            val db = BiljkaDatabase.getInstance(context)
            db?.biljkaDao()?.deleteAllBiljke()
            return@withContext null
        }
    }
}