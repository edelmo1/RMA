package com.example.spirala_1

import android.graphics.Bitmap
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update


@Dao
interface BiljkaDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBiljka(biljka: Biljka): Long

    @Transaction
    suspend fun saveBiljka(biljka: Biljka): Boolean {
        val id = insertBiljka(biljka)
        return id != -1L
    }

    //ovo su dvije pomocne metode za fixOfflineBiljka
   @Query("SELECT * FROM Biljka WHERE onlineChecked=0")//ako ne radilo probaj zamijeniti sa false
   suspend fun getOfflineBiljka() : List<Biljka>

   @Update
   suspend fun updateBiljka(biljka: Biljka)

   @Transaction
   suspend fun fixOfflineBiljka(): Int {
       var offlineBiljke = ArrayList(getOfflineBiljka())
       var azurirano = 0

       for(i in offlineBiljke.indices){
           val original = offlineBiljke[i].copy()
           println(original.porodica)
           var biljka = offlineBiljke[i]
           if (!biljka.onlineChecked!!){
               var novaBiljka = TrefleDAO().fixData(biljka)
                biljka.onlineChecked=false
               System.out.println("Provjereno!")
               println(novaBiljka.porodica + " " + biljka.porodica)
               biljka = novaBiljka
           }
           if (biljka.porodica != original.porodica || biljka.medicinskoUpozorenje != original.medicinskoUpozorenje || biljka.klimatskiTipovi != original.klimatskiTipovi || biljka.zemljisniTipovi != original.zemljisniTipovi || biljka.jela != original.jela){
               biljka.onlineChecked = true
               updateBiljka(biljka)
               azurirano++
           }
       }
       return azurirano
   }

   //ovo su pomocne metode za addImage

   @Query("SELECT * FROM biljka WHERE id = :id")
   suspend fun getBiljkaById(id: Long?): Biljka?

   @Query("SELECT * FROM BiljkaBitmap WHERE idBiljke = :idBiljke")
   suspend fun getBiljkaBitmapById(idBiljke: Long?): BiljkaBitmap?

   @Insert
   suspend fun insertImage(biljkaBitmap: BiljkaBitmap) : Long

   @Transaction
   suspend fun addImage(idBiljke : Long? , bitmap : Bitmap):Boolean{
      if(getBiljkaById(idBiljke)==null || getBiljkaBitmapById(idBiljke)!=null){
         return false
}
      val newBiljkaBitmap = BiljkaBitmap(null,idBiljke, bitmap)
      val id = insertImage(newBiljkaBitmap)
       return id != -1L
   }

   @Query("SELECT * FROM Biljka")
   suspend fun getAllBiljkas():List<Biljka>

   //pomocne metode za clearData
   @Query("DELETE FROM biljka")
   suspend fun deleteAllBiljke()

   @Query("DELETE FROM BiljkaBitmap")
  suspend fun deleteAllBiljkaBitmaps()

   @Transaction
  suspend fun clearData() {
      deleteAllBiljkaBitmaps()
      deleteAllBiljke()
   }


    @Query("SELECT * FROM BiljkaBitmap WHERE idBiljke = :id")
    suspend fun provjeriPostojiLi(id: Long): List<BiljkaBitmap>

}

