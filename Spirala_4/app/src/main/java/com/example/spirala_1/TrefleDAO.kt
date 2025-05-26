package com.example.spirala_1

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder

class TrefleDAO() {

    private  val API_KEY: String = "udPaDhxPIymSOB1j1rlJVPZzgrx5gsQkbnXs3bSGCKg"
    private  lateinit var defaultBitmap: Bitmap
    val defaultBiljka: Biljka =
        Biljka(0,"", null, null, null,null, null, null, null,false)

 fun setBitmap(context:Context){
        defaultBitmap = BitmapFactory.decodeResource(context.resources , R.drawable.default_image)
}

    @SuppressLint("SuspiciousIndentation")
    suspend fun getImage(biljka: Biljka): Bitmap {

        return withContext(Dispatchers.IO) {

            try {

                var bitmapURL: String = ""
                val biljke = arrayListOf<Biljka>()
                val url1 =
                    "http:trefle.io/api/v1/species/search?token=$API_KEY&q=${dajLatinskiNaziv(biljka.naziv)}"
                val url = URL(url1)
                (url.openConnection() as? HttpURLConnection)?.run {
                    val result = this.inputStream.bufferedReader().use { it.readText() }
                    val jo = JSONObject(result)
                    val results = jo.getJSONArray("data")
                    for (i in 0 until results.length()) {
                        val b = results.getJSONObject(i)
                        bitmapURL = b.getString("image_url")
                        if (i == 0) break
                    }
                }
                if(bitmapURL=="") return@withContext defaultBitmap
               return@withContext getBitmapFromURL(bitmapURL)!!
            } catch (e: MalformedURLException) {
                return@withContext defaultBitmap
            } catch (e: IOException) {
               return@withContext defaultBitmap
           } catch (e: JSONException) {
                return@withContext defaultBitmap
            } catch(e:Exception) {
                return@withContext defaultBitmap
            }
        }

    }

    @SuppressLint("SuspiciousIndentation")
    suspend fun fixData(biljka: Biljka): Biljka {

        return withContext(Dispatchers.IO) {
            try {

                //ovo je zahtjev kako bismo dobili ID biljke koja je prosljedjena kao parametar
                var idBiljke = 0
                val biljke = arrayListOf<Biljka>()
                val latinName = dajLatinskiNaziv(biljka.naziv)?.let { URLEncoder.encode(it, "UTF-8") }
                val url1 =
                    "http://trefle.io/api/v1/species/search?token=$API_KEY&q=$latinName"
                val url = URL(url1)
                (url.openConnection() as? HttpURLConnection)?.run {
                    val result = this.inputStream.bufferedReader().use { it.readText() }
                    val jo = JSONObject(result.toString())
                    if (!jo.has("data")) {
                        return@withContext defaultBiljka
                    }
                    val results = jo.getJSONArray("data")
                    for (i in 0 until results.length()) {
                        val b = results.getJSONObject(i)
                        idBiljke = b.getInt("id")
                        if (i == 0) break
                    }
                }
                // ovo je zahtjev putem kojeg cemo dobiti detaljne informacije o konkretnoj biljci

                val url2 =
                    "http://trefle.io/api/v1/species/$idBiljke?token=$API_KEY"
                val Url = URL(url2)
                (Url.openConnection() as? HttpURLConnection)?.run {
                    val result = this.inputStream.bufferedReader().use { it.readText() }
                    val jo = JSONObject(result)
                    val B = jo.getJSONObject("data")


                        if(B.getString("family")!=null && B.getString("family")!=biljka.porodica){
                            biljka.porodica=B.getString("family")
                        }
                    if(!B.isNull("edible")) {
                        if (B.getBoolean("edible") != null && B.getBoolean("edible") == false) {
                            biljka.jela = mutableListOf()
                            biljka.medicinskoUpozorenje += " NIJE JESTIVO."
                        }
                    }
                        val specifikacija = B.getJSONObject("specifications")

                    if(!specifikacija.isNull("toxicity")) {
                        if ( specifikacija.getString("toxicity") != "none") {

                            if (biljka.medicinskoUpozorenje?.contains("TOKSIČNO") == false) {
                                biljka.medicinskoUpozorenje += " TOKSIČNO"
                            }
                        }
                    }
                        val growth = B.getJSONObject("growth")
                        if(!growth.isNull("soil_texture")){
                            val soil = growth.getString("soil_texture")

                            //prvo provjerimo da li dodani tip zemljista ispunjava uslov
                         if(soil=="1" || soil=="2"){
                             biljka.zemljisniTipovi=listOf(Zemljiste.GLINENO)
                         }
                            else if(soil=="3" || soil=="4"){
                             biljka.zemljisniTipovi=listOf(Zemljiste.PJESKOVITO)
                         }
                         else if(soil=="5" || soil=="6"){
                             biljka.zemljisniTipovi=listOf(Zemljiste.ILOVACA)
                         }
                         else if(soil=="7" || soil=="8"){
                             biljka.zemljisniTipovi=listOf(Zemljiste.CRNICA)
                         }
                         else if(soil=="9"){
                             biljka.zemljisniTipovi=listOf(Zemljiste.SLJUNOVITO)
                         }
                         else if(soil=="10"){
                             biljka.zemljisniTipovi=listOf(Zemljiste.KRECNJACKO)
                         }

                        }

                        if(!growth.isNull("atmospheric_humidity") && !growth.isNull("light")){
                            val atm= growth.getInt("atmospheric_humidity")
                            val light = growth.getInt("light")
                            var klime : ArrayList<KlimatskiTip> = arrayListOf()
                            var sveKlime : ArrayList<KlimatskiTip> = arrayListOf(KlimatskiTip.SREDOZEMNA,
                                                                                 KlimatskiTip.TROPSKA,
                                                                                 KlimatskiTip.SUBTROPSKA,
                                                                                 KlimatskiTip.UMJERENA,
                                                                                 KlimatskiTip.SUHA,
                                                                                 KlimatskiTip.PLANINSKA)
                            for(klima in sveKlime){
                                if(klima==KlimatskiTip.SREDOZEMNA && light in 6..9 && atm in 1..5){
                                    klime.add(klima)
                                }
                                else if (klima==KlimatskiTip.TROPSKA && light in 8..10 && atm in 7..10){
                                 klime.add(klima)
                                }
                                else if (klima==KlimatskiTip.SUBTROPSKA && light in 6..9 && atm in 5..8){
                                    klime.add(klima)
                                }
                                else if (klima==KlimatskiTip.UMJERENA && light in 4..7&& atm in 3..7){
                                    klime.add(klima)
                                }
                                else if (klima==KlimatskiTip.SUHA && light in 7..9 && atm in 1..2){
                                    klime.add(klima)
                                }
                                else if (klima==KlimatskiTip.PLANINSKA && light in 0..5 && atm in 3..7){
                                    klime.add(klima)
                                }
                            }
                            var pravaListaKlima : List<KlimatskiTip> = klime
                            biljka.klimatskiTipovi=pravaListaKlima
                        }

                }
                biljka.onlineChecked=true

                return@withContext biljka

            } catch (e: MalformedURLException) {
                return@withContext defaultBiljka
            } catch (e: IOException) {
                return@withContext defaultBiljka
            } catch (e: JSONException) {
                return@withContext defaultBiljka
            } catch(e:Exception) {
                return@withContext defaultBiljka
            }
        }


    }

    suspend fun getPlantswithFlowerColor(flower_color: String, substr: String): List<Biljka> {
        return withContext(Dispatchers.IO) {
            try {
                val biljke = arrayListOf<Biljka>();
                var page = 1
                var totalPages = 1

                    val url1 = "http://trefle.io/api/v1/plants/search?filter[flower_color]=$flower_color&token=$API_KEY&q=$substr&page=$page";
                    val url = URL(url1);
                    (url.openConnection() as? HttpURLConnection)?.run {
                        val result = this.inputStream.bufferedReader().use { it.readText() };
                        val jo = JSONObject(result);
                        val results = jo.getJSONArray("data");
                        for (i in 0 until results.length()) {
                            val b = results.getJSONObject(i);

                                var biljka: Biljka = Biljka(0,"", null, null, null, null, null, null, null,false)
                                biljka.naziv = b.getString("scientific_name");
                                biljka.porodica = b.getString("family");
                                biljka = fixData(biljka); //??
                                biljke.add(biljka);

                        }
                    }

                return@withContext biljke;
            } catch (e: MalformedURLException) {
                return@withContext listOf();
            } catch (e: IOException) {
                return@withContext listOf();
            } catch (e: JSONException) {
                return@withContext listOf();
            } catch (e: Exception) {
                return@withContext listOf();
            }
        }
    }


    suspend fun getBitmapFromURL(src: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(src)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }


    fun dajLatinskiNaziv(name: String): String? {
        val startIndex = name.indexOf('(')

        if (startIndex == -1) return null

        val endIndex = name.indexOf(')', startIndex)

        if (endIndex == -1 || endIndex - startIndex <= 1) return null

        return name.substring(startIndex + 1, endIndex)
    }


}