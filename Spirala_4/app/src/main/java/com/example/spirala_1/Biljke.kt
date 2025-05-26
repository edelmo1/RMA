package com.example.spirala_1

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.io.Serializable


enum class Zemljiste(val naziv: String) {
    PJESKOVITO("Pjeskovito zemljište"),
    GLINENO("Glinеno zemljište"),
    ILOVACA("Ilovača"),
    CRNICA("Crnica"),
    SLJUNOVITO("Šljunovito zemljište"),
    KRECNJACKO("Krečnjačko zemljište");
}

enum class KlimatskiTip(val opis: String) {
    SREDOZEMNA("Mediteranska klima - suha, topla ljeta i blage zime"),
    TROPSKA("Tropska klima - topla i vlažna tokom cijele godine"),
    SUBTROPSKA("Subtropska klima - blage zime i topla do vruća ljeta"),
    UMJERENA("Umjerena klima - topla ljeta i hladne zime"),
    SUHA("Sušna klima - niske padavine i visoke temperature tokom cijele godine"),
    PLANINSKA("Planinska klima - hladne temperature i kratke sezone rasta"),
}

enum class MedicinskaKorist(val opis: String) {
    SMIRENJE("Smirenje - za smirenje i relaksaciju"),
    PROTUUPALNO("Protuupalno - za smanjenje upale"),
    PROTIVBOLOVA("Protivbolova - za smanjenje bolova"),
    REGULACIJAPRITISKA("Regulacija pritiska - za regulaciju visokog/niskog pritiska"),
    REGULACIJAPROBAVE("Regulacija probave"),
    PODRSKAIMUNITETU("Podrška imunitetu"),
}

enum class ProfilOkusaBiljke(val opis: String) {
    MENTA("Mentol - osvježavajući, hladan ukus"),
    CITRUSNI("Citrusni - osvježavajući, aromatičan"),
    SLATKI("Sladak okus"),
    BEZUKUSNO("Obični biljni okus - travnat, zemljast ukus"),
    LJUTO("Ljuto ili papreno"),
    KORIJENASTO("Korenast - drvenast i gorak ukus"),
    AROMATICNO("Začinski - topli i aromatičan ukus"),
    GORKO("Gorak okus"),
}

@Entity
data class Biljka(

    @PrimaryKey(autoGenerate = true) var id:Long?=null,
    @ColumnInfo var naziv: String,
    @ColumnInfo("family") var porodica: String?,
    @ColumnInfo var medicinskoUpozorenje: String?,
    @ColumnInfo val medicinskeKoristi: List<MedicinskaKorist>?,
    @ColumnInfo val profilOkusa: ProfilOkusaBiljke?,
    @ColumnInfo var jela: List<String>?,
    @ColumnInfo var klimatskiTipovi: List<KlimatskiTip>?,
    @ColumnInfo var zemljisniTipovi: List<Zemljiste>?,
    @ColumnInfo var onlineChecked:Boolean? = false
): Serializable
{
    fun copy(): Biljka {
        return Biljka(
            id,
            naziv,
            porodica,
            medicinskoUpozorenje,
            medicinskeKoristi?.toList(),
            profilOkusa,
            jela?.toList(),
            klimatskiTipovi?.toList(),
            zemljisniTipovi?.toList(),
            onlineChecked
        )
    }
}



