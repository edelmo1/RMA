package com.example.spirala_1

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NovaBiljkaActivity : AppCompatActivity() {

    private lateinit var nazivET: EditText
    private lateinit var porodicaET: EditText
    private lateinit var medicinskoUpozorenjeET: EditText
    private lateinit var jeloET: EditText
    private lateinit var medicinskaKoristLV: ListView
    private lateinit var klimatskiTipLV: ListView
    private lateinit var zemljisniTipLV: ListView
    private lateinit var profilOkusaLV: ListView
    private lateinit var jelaLV: ListView
    private lateinit var dodajJelo: Button
    private lateinit var dodajBiljku: Button
    private lateinit var uslikajBiljku: Button
    private lateinit var slikaIV: ImageView
    private var imageCapture: ImageCapture? = null
    private var koristiLista = listOf(
        MedicinskaKorist.SMIRENJE,
        MedicinskaKorist.PROTUUPALNO,
        MedicinskaKorist.PROTIVBOLOVA,
        MedicinskaKorist.REGULACIJAPROBAVE,
        MedicinskaKorist.REGULACIJAPRITISKA,
        MedicinskaKorist.PODRSKAIMUNITETU
    )
    private lateinit var koristiAdapter: ArrayAdapter<MedicinskaKorist>


    private var klimatskiTipoviLista = listOf(
        KlimatskiTip.SREDOZEMNA,
        KlimatskiTip.TROPSKA,
        KlimatskiTip.SUBTROPSKA,
        KlimatskiTip.UMJERENA,
        KlimatskiTip.SUHA,
        KlimatskiTip.PLANINSKA
    )
    private lateinit var klimaAdapter: ArrayAdapter<KlimatskiTip>


    private var zemljisniTipoviLista = listOf(
        Zemljiste.PJESKOVITO,
        Zemljiste.GLINENO,
        Zemljiste.ILOVACA,
        Zemljiste.CRNICA,
        Zemljiste.SLJUNOVITO,
        Zemljiste.KRECNJACKO
    )
    private lateinit var zemljisniAdapter: ArrayAdapter<Zemljiste>


    private val profiliOkusaLista = listOf(
        ProfilOkusaBiljke.MENTA,
        ProfilOkusaBiljke.CITRUSNI,
        ProfilOkusaBiljke.SLATKI,
        ProfilOkusaBiljke.BEZUKUSNO,
        ProfilOkusaBiljke.LJUTO,
        ProfilOkusaBiljke.KORIJENASTO,
        ProfilOkusaBiljke.AROMATICNO,
        ProfilOkusaBiljke.GORKO
    )
    private lateinit var profiliOkusaAdapter: ArrayAdapter<ProfilOkusaBiljke>


    private val jelaLista = arrayListOf<String>()
    private lateinit var jelaAdapter: ArrayAdapter<String>

    //varijable za pohranu izabranih vrijednosti iz listi
    private var koristi = arrayListOf<MedicinskaKorist>()
    private var klime = arrayListOf<KlimatskiTip>()
    private var zemlja = arrayListOf<Zemljiste>()

    private var okus: ProfilOkusaBiljke = ProfilOkusaBiljke.GORKO
    private var izabranOkus = false

    //ZA SLIKU
    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_CAMERA_PERMISSION = 100
    }

    @SuppressLint("SuspiciousIndentation", "SetTextI18n", "QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nova_biljka)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        //inicijaliziramo sve atribute klase /view-ove

        DataRepository.uslikano=false
        nazivET = findViewById(R.id.nazivET)
        porodicaET = findViewById(R.id.porodicaET)
        medicinskoUpozorenjeET = findViewById(R.id.medicinskoUpozorenjeET)
        jeloET = findViewById(R.id.jeloET)

        dodajJelo = findViewById(R.id.dodajJeloBtn)
        dodajBiljku = findViewById(R.id.dodajBiljkuBtn)
        uslikajBiljku = findViewById(R.id.uslikajBiljkuBtn)

        slikaIV = findViewById(R.id.slikaIV)

        //povezimo adaptere sa kolekcijama
        medicinskaKoristLV = findViewById(R.id.medicinskaKoristLV)
        koristiAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, koristiLista)
        medicinskaKoristLV.adapter = koristiAdapter

        klimatskiTipLV = findViewById(R.id.klimatskiTipLV)
        klimaAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, klimatskiTipoviLista)
        klimatskiTipLV.adapter = klimaAdapter

        zemljisniTipLV = findViewById(R.id.zemljisniTipLV)
        zemljisniAdapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, zemljisniTipoviLista)
        zemljisniTipLV.adapter = zemljisniAdapter

        profilOkusaLV = findViewById(R.id.profilOkusaLV)
        profiliOkusaAdapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, profiliOkusaLista)
        profilOkusaLV.adapter = profiliOkusaAdapter

        jelaLV = findViewById(R.id.jelaLV)
        jelaAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, jelaLista)
        jelaLV.adapter = jelaAdapter


//dodavanje jela

        dodajJelo.setOnClickListener {
            //PITAJ
            if (jeloET.text.toString().length < 2 || jeloET.text.toString().length > 20)
                jeloET.setError("Neispravno jelo!")
            else {
                var postoji = false

                for (i in 0 until jelaLista.size) {
                    if (jelaLista[i].uppercase() == jeloET.text.toString().uppercase()) {
                        postoji = true
                        jeloET.setError("Jelo već postoji")
                    }
                }
                if (postoji == false) {
                    jelaLista.add(jeloET.text.toString())
                    jelaAdapter.notifyDataSetChanged()
                    jeloET.setText("")
                }
            }
        }

//izmjena jela

        jelaLV.setOnItemClickListener { _, _, position, _ ->
            val clickedItem = jelaAdapter.getItem(position) ?: return@setOnItemClickListener
            jeloET.setText(clickedItem)
            dodajJelo.text = "Izmijeni jelo"


            dodajJelo.setOnClickListener {
                val newText = jeloET.text.toString()
                if (newText.isEmpty()) {
                    jelaLista.removeAt(position)
                } else {
                    jelaLista[position] = newText
                }
                jelaAdapter.notifyDataSetChanged()
                dodajJelo.text = "Dodaj jelo"
                jeloET.setText("")

                dodajJelo.setOnClickListener {
                    val newText = jeloET.text.toString()
                    if (newText.isNotEmpty()) {
                        jelaLista.add(newText)
                        jelaAdapter.notifyDataSetChanged()
                        jeloET.setText("")
                    }
                }
            }
        }

        //odabir profila okusa
        profilOkusaLV.setOnItemClickListener { parent, view, position, id ->
            val clickedItem = profiliOkusaAdapter.getItem(position)

            if (clickedItem != null) {
                okus = clickedItem
            }
            izabranOkus = true

        }


        //odabir medicinske koristi

        medicinskaKoristLV.setOnItemClickListener { parent, view, position, id ->
            val clickedItem = koristiAdapter.getItem(position)
            var postoji = false
            if (clickedItem != null) {

                for (i in 0 until koristi.size) {
                            if (koristi[i] == clickedItem) {
                                postoji = true
                    }
                }

                if (postoji == false) koristi.add(clickedItem)
            }
        }

        //odabir klimatskih tipova

        klimatskiTipLV.setOnItemClickListener { parent, view, position, id ->
            val clickedItem = klimaAdapter.getItem(position)
            var postoji = false
            if (clickedItem != null) {

                for (i in 0 until klime.size) {
                    if (klime[i] == clickedItem) {
                        postoji = true
                    }
                }

                if (postoji == false) klime.add(clickedItem)
            }
        }

        //odabir zemljisnih tipova

        zemljisniTipLV.setOnItemClickListener { parent, view, position, id ->
            val clickedItem = zemljisniAdapter.getItem(position)
            var postoji = false
            if (clickedItem != null) {

                for (i in 0 until zemlja.size) {
                    if (zemlja[i] == clickedItem) {
                        postoji = true
                    }
                }

                if (postoji == false) zemlja.add(clickedItem)
            }
        }

        //dodavanje slike

            uslikajBiljku.setOnClickListener {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_CAMERA_PERMISSION
                    )
                } else {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                }
                DataRepository.uslikano=true
            }


            //dodavanje biljke

            dodajBiljku.setOnClickListener {
                //VALIDACIJA
                var validno = true

                if (nazivET.text.toString().length < 2 || nazivET.text.toString().length > 40) {
                    nazivET.setError("Neispravan naziv!")
                    validno = false
                }
                if (porodicaET.text.toString().length < 2 || porodicaET.text.toString().length > 20) {
                    porodicaET.setError("Neispravna porodica!")
                    validno = false
                }
                if (medicinskoUpozorenjeET.text.toString().length < 2 || medicinskoUpozorenjeET.text.toString().length > 20) {
                    medicinskoUpozorenjeET.setError("Neispravno upozorenje!")
                    validno = false
                }
                if (jelaLista.isEmpty()) {
                    jeloET.setError("Nedovoljno jela!")
                    validno = false
                }

                //provjera da li je izabran bar jedan element multiple listi
                if (koristi.isEmpty() == true) {
                    val toast = Toast.makeText(
                        this,
                        "Nije izabrana niti jedna medicinska korist",
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                    validno = false
                }

                if (klime.isEmpty() == true) {
                    val toast = Toast.makeText(
                        this,
                        "Nije izabran niti jedan klimatski tip",
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                    validno = false

                }

                if (zemlja.isEmpty() == true) {
                    val toast = Toast.makeText(
                        this,
                        "Nije izabran niti jedan zemljani tip",
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                    validno = false

                }


                // provjera je li lista jela prazna
                if (jelaLista.isEmpty() == true) {
                    val toast = Toast.makeText(this, "Lista jela prazna", Toast.LENGTH_SHORT)
                    toast.show()
                    validno = false

                }

                //provjera da li je izabran okus
                if (izabranOkus == false) {
                    val toast =
                        Toast.makeText(this, "Nije izabran niti jedan okus", Toast.LENGTH_SHORT)
                    toast.show()
                    validno = false

                }

                if (validno == true) {
                    //NAPRAVIMO BILJKU
                    var biljka: Biljka = Biljka(  //prvi i zadnji atribut sa predefinisanim vrijednostima??
                        0,
                        nazivET.text.toString(),
                        porodicaET.text.toString(),
                        medicinskoUpozorenjeET.text.toString(),
                        koristi,
                        okus,
                        jelaLista,
                        klime,
                        zemlja,
                        false

                    )
                   // fixData(biljka)

                    val scope = CoroutineScope(Job() + Dispatchers.Main)
                    scope.launch {
                        // Make the network call and suspend execution until it finishes
                        val trefleDAO: TrefleDAO = TrefleDAO()
                        trefleDAO.setBitmap(this@NovaBiljkaActivity)
                        val result = trefleDAO.fixData(biljka)
                        val rezultat = saveBiljka(this@NovaBiljkaActivity, result)
                        val tacno: String = "success"
                        when(rezultat){
                            is String -> {
                                DataRepository.lista.add(biljka)
                                DataRepository.filtriranaLista.add(biljka)

                                setResult(Activity.RESULT_OK)
                                finish()}
                            else -> {throw Exception("Greska")}
                        }
                    }

                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("biljka", biljka)
                    }
                    startActivity(intent)
                }
            }

        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
                val imageBitmap = data?.extras?.get("data") as? Bitmap
                slikaIV.setImageBitmap(imageBitmap)
            }
        }

    fun fixData(biljka:Biljka){
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        // Kreira se Coroutine na UI

            // Vrti se poziv servisa i suspendira se rutina dok se `withContext` ne zavrsi
        scope.launch {
            val trefleDAO: TrefleDAO = TrefleDAO()
            trefleDAO.setBitmap(this@NovaBiljkaActivity)
            val result = trefleDAO.fixData(biljka)
            // Prikaze se rezultat korisniku na glavnoj niti
           searchDone(result)
        }
    }

    fun searchDone(biljka:Biljka) {
        val toast = Toast.makeText(this, "Biljka uspješno dodana", Toast.LENGTH_SHORT)
        toast.show()
    }
    fun onError() {
        val toast = Toast.makeText(this, "Greška pri dodavanju", Toast.LENGTH_SHORT)
        toast.show()
    }

    suspend fun saveBiljka(context: Context, biljka: Biljka): String?{
        return withContext(Dispatchers.IO){
            try{
                var db = BiljkaDatabase.getInstance(context)
               var sveBiljke = db.biljkaDao().getAllBiljkas()
                biljka.id= sveBiljke.size.toLong()
                db.biljkaDao().saveBiljka(biljka)
                if(DataRepository.uslikano==true){
                    imageViewToBitmap(slikaIV)?.let { db.biljkaDao().addImage(biljka.id, it) }
                }
                return@withContext "success"
            }catch(error:Exception){
                return@withContext null
            }
        }
    }

    fun imageViewToBitmap(imageView: ImageView): Bitmap? {
        val drawable = imageView.drawable ?: return null
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

}
