package com.example.spirala_1

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import android.util.Base64
import android.util.Log

class Converters {

    private val TAG = "Converters"

    @TypeConverter
    fun toMedicinskaKoristList(value: String?): List<MedicinskaKorist> {
        Log.d(TAG, "toMedicinskaKoristList input: $value")
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            Gson().fromJson(value, object : TypeToken<List<MedicinskaKorist>>() {}.type)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing MedicinskaKorist list: ${e.message}")
            emptyList()
        }
    }

    @TypeConverter
    fun fromMedicinskaKoristList(list: List<MedicinskaKorist?>?): String {
        Log.d(TAG, "fromMedicinskaKoristList input: $list")
        if (list.isNullOrEmpty()) return ""
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toProfilOkusa(value: String?): ProfilOkusaBiljke? {
        Log.d(TAG, "toProfilOkusa input: $value")
        return value?.let {
            try {
                Gson().fromJson(it, ProfilOkusaBiljke::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing ProfilOkusa: ${e.message}")
                null
            }
        }
    }

    @TypeConverter
    fun fromProfilOkusa(profilOkusa: ProfilOkusaBiljke?): String {
        Log.d(TAG, "fromProfilOkusa input: $profilOkusa")
        return Gson().toJson(profilOkusa)
    }

    @TypeConverter
    fun toJelaList(value: String?): List<String> {
        Log.d(TAG, "toJelaList input: $value")
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Jela list: ${e.message}")
            emptyList()
        }
    }

    @TypeConverter
    fun fromJelaList(list: List<String?>?): String {
        Log.d(TAG, "fromJelaList input: $list")
        if (list.isNullOrEmpty()) return ""
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toKlimatskiTipList(value: String?): List<KlimatskiTip> {
        Log.d(TAG, "toKlimatskiTipList input: $value")
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            Gson().fromJson(value, object : TypeToken<List<KlimatskiTip>>() {}.type)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing KlimatskiTip list: ${e.message}")
            emptyList()
        }
    }

    @TypeConverter
    fun fromKlimatskiTipList(list: List<KlimatskiTip?>?): String {
        Log.d(TAG, "fromKlimatskiTipList input: $list")
        if (list.isNullOrEmpty()) return ""
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toZemljisteList(value: String?): List<Zemljiste> {
        Log.d(TAG, "toZemljisteList input: $value")
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            Gson().fromJson(value, object : TypeToken<List<Zemljiste>>() {}.type)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Zemljiste list: ${e.message}")
            emptyList()
        }
    }

    @TypeConverter
    fun fromZemljisteList(list: List<Zemljiste?>?): String {
        Log.d(TAG, "fromZemljisteList input: $list")
        if (list.isNullOrEmpty()) return ""
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap?): String? {
        return bitmap?.let {
            val outputStream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        }
    }

    @TypeConverter
    fun toBitmap(base64String: String?): Bitmap? {
        return base64String?.let {
            val byteArray = Base64.decode(it, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }
    }
}
