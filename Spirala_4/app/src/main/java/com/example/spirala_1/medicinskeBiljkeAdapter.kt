package com.example.spirala_1

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//adapter za medicinski layout

class KolekcijaMedicinskihBiljakaAdapter(
    val context:Context,
    val lifecycleScope: LifecycleCoroutineScope,
    var sveBiljke: ArrayList<Biljka>,
    var biljke: ArrayList<Biljka>,
    private val onClickListener: (biljka: Biljka) -> Unit

) : RecyclerView.Adapter<KolekcijaMedicinskihBiljakaAdapter.BiljkaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BiljkaViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.medicinski, parent, false)
        return BiljkaViewHolder(view)
    }

    override fun getItemCount(): Int = biljke.size

    override fun onBindViewHolder(holder: BiljkaViewHolder, position: Int) {
        val biljka = biljke[position]
        holder.naziv.text = biljka.naziv
        holder.upozorenje.text = biljka.medicinskoUpozorenje ?: ""
        holder.korist1.text = biljka.medicinskeKoristi?.getOrNull(0)?.opis ?: ""
        holder.korist2.text = biljka.medicinskeKoristi?.getOrNull(1)?.opis ?: ""
        holder.korist3.text = biljka.medicinskeKoristi?.getOrNull(2)?.opis ?: ""


        val scope = CoroutineScope(Job() + Dispatchers.Main)
        val context : Context = holder.slika.context
        scope.launch {
            val result1 = biljka.id?.let { provjera(context, it) }
            when(result1) {
                is Boolean->{
                    if (result1){
                        val result2 = getImageFromDatabase(context, biljka.id!!)
                        when(result2){
                            is Bitmap ->{
                                holder . slika . setImageBitmap (result2)
                            }
                            else ->{
                                val result = TrefleDAO().getImage(biljka)
                                biljka.id?.let { dodajSliku(context, it, result) }
                                holder . slika. setImageBitmap (result)

                            }
                        }
                    }else{
                        val result = TrefleDAO().getImage(biljka)
                        biljka.id?.let { dodajSliku(context, it, result) }
                        holder . slika . setImageBitmap (result)
                    }
                }
                else ->{
                    val result = TrefleDAO().getImage(biljka)
                    biljka.id?.let { dodajSliku(context, it, result) }
                    holder . slika . setImageBitmap (result)
                }
            }
        }

        holder.itemView.setOnClickListener {
            onClickListener(biljka)
        }
    }

    fun updateBiljke(biljke: ArrayList<Biljka>) {
        this.biljke = biljke
        notifyDataSetChanged()
    }


    inner class BiljkaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val slika: ImageView = itemView.findViewById(R.id.slikaItem)
        val naziv: TextView = itemView.findViewById(R.id.nazivItem)
        val upozorenje: TextView = itemView.findViewById(R.id.upozorenjeItem)
        val korist1: TextView = itemView.findViewById(R.id.korist1Item)
        val korist2: TextView = itemView.findViewById(R.id.korist2Item)
        val korist3: TextView = itemView.findViewById(R.id.korist3Item)
    }

    fun addImage(context: Context, biljka: Biljka,bitmap:Bitmap) {
        val scope = CoroutineScope(Job() + Dispatchers.Main)

        // Create a new coroutine on the UI thread
        scope.launch {
            // Make the network call and suspend execution until it finishes
            val result = BiljkeRepository().addImage(context, biljka,bitmap)

            // Display result of the network request to the user
            if (result == true) {
                //onSuccess()
            } else {
                //onError()
            }
        }
    }
    suspend fun dodajSliku(context: Context, idBiljke: Long,bitmap: Bitmap): Boolean?{
        return withContext(Dispatchers.IO){
            try {
                var db = BiljkaDatabase.getInstance(context)
                val cropWidth = 100
                val cropHeight = 100
                val croppedBitmap = cropBitmap(bitmap, cropWidth, cropHeight)
                db.biljkaDao().addImage(idBiljke, croppedBitmap)
                return@withContext true
            }catch(error: Exception){
                System.out.println("Isti bitmap pokusano dodati")
                return@withContext null
            }
        }
    }

    suspend fun provjera(context: Context, idBiljke: Long): Boolean?{
        return withContext(Dispatchers.IO){
            try{
                var db = BiljkaDatabase.getInstance(context)
                var result = db.biljkaDao().provjeriPostojiLi(idBiljke)
                if (result.isNotEmpty()){ return@withContext true}
                else {return@withContext false}
            }catch(error: Exception){
                return@withContext null
            }
        }
    }

    suspend fun getImageFromDatabase(context: Context, idBiljke: Long): Bitmap?{
        return withContext(Dispatchers.IO){
            try{
                var db = BiljkaDatabase.getInstance(context)
                var result = db.biljkaDao().provjeriPostojiLi(idBiljke)
                if (result.isNotEmpty()){
                    return@withContext result[0].bitmap
                }else{
                    return@withContext null
                }
            }catch(error: Exception){
                return@withContext null
            }
        }
    }

    fun cropBitmap(bitmap: Bitmap, cropWidth: Int, cropHeight: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        val startX = (originalWidth - cropWidth) / 2
        val startY = (originalHeight - cropHeight) / 2
        return Bitmap.createBitmap(bitmap, startX, startY, cropWidth, cropHeight)
    }

        }


