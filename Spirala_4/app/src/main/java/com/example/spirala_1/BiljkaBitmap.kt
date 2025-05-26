package com.example.spirala_1

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(
    tableName = "BiljkaBitmap",
    foreignKeys = [ForeignKey(
        entity = Biljka::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("idBiljke"),
        onDelete = ForeignKey.CASCADE
    )]
)

data class BiljkaBitmap (
    @PrimaryKey(autoGenerate = true) var id:Long?=null,
    @ColumnInfo var idBiljke : Long?,
    @ColumnInfo var bitmap: Bitmap?
)

