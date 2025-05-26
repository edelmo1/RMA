package com.example.spirala_1

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val  fokusi = arrayListOf<String>("Medicinski","Botanicki","Kuharski")
    private lateinit var adapter : SpinnerAdapter
    private lateinit var spiner : Spinner
    private lateinit var button : Button
    private lateinit var biljke : RecyclerView
    private var fokus : String = "Medicinski"
    private lateinit var medicinskeBiljkeAdapter: KolekcijaMedicinskihBiljakaAdapter
    private lateinit var botanickeBiljkeAdapter: KolekcijaBotanickihBiljakaAdapter
    private lateinit var kuharskeBiljkeAdapter: KolekcijaKuharskihBiljakaAdapter
    private var immutableFiltriranaLista = getBiljke()
    private lateinit var dodajBiljku : Button

    private lateinit var bojeSpiner : Spinner
    private lateinit var buttonPretrazi : Button
    private lateinit var pretragaET : EditText
    private val boje : ArrayList<String> = arrayListOf("Odaberi boju: ","red","blue","yellow","orange","purple","brown","green")


    var boja : String = ""
    @SuppressLint("NotifyDataSetChanged", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)



        //povezati elemente UI-a sa varijablama
        spiner = findViewById(R.id.modSpinner)
        button = findViewById(R.id.resetBtn)


        biljke = findViewById(R.id.biljkeRV)
        adapter = ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_list_item_1, fokusi)
        spiner.setAdapter(adapter)

        bojeSpiner = findViewById(R.id.bojaSPIN)
        buttonPretrazi = findViewById(R.id.brzaPretraga)
        pretragaET = findViewById(R.id.pretragaET)


        //spinner za boje
        var bojeAdapter: ArrayAdapter<String> =
            ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, boje)
        bojeSpiner.setAdapter(bojeAdapter)


        bojeSpiner.visibility = View.INVISIBLE
        pretragaET.visibility = View.INVISIBLE
        buttonPretrazi.visibility = View.INVISIBLE


        lifecycleScope.launch {

            val biljkeInDb = getAllBiljkas(this@MainActivity)
            if (biljkeInDb != null && DataRepository.prviPut) {
                for (biljka in immutableFiltriranaLista) {
                    val result = saveBiljka(this@MainActivity, biljka)
                    if (!result) {
                        onError()
                        return@launch
                    }
                }
                DataRepository.prviPut = false
            }

            var lista = getAllBiljkas(this@MainActivity)
            if (lista != null) {
                DataRepository.lista.clear()
                DataRepository.filtriranaLista.clear()
                DataRepository.lista.addAll(lista)
                DataRepository.filtriranaLista.addAll(lista)
            }

            //postavljanje defaultnog moda
            biljke.layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            medicinskeBiljkeAdapter = KolekcijaMedicinskihBiljakaAdapter(
                this@MainActivity,
                lifecycleScope,
                DataRepository.lista,
                DataRepository.filtriranaLista
            ) { biljka -> filterOnClick(biljka) }
            biljke.adapter = medicinskeBiljkeAdapter
            medicinskeBiljkeAdapter.updateBiljke(DataRepository.filtriranaLista)
            medicinskeBiljkeAdapter.notifyDataSetChanged()

            botanickeBiljkeAdapter = KolekcijaBotanickihBiljakaAdapter(
                this@MainActivity,
                lifecycleScope,
                DataRepository.lista,
                DataRepository.filtriranaLista
            ) { biljka -> filterOnClick(biljka) }
            kuharskeBiljkeAdapter = KolekcijaKuharskihBiljakaAdapter(
                this@MainActivity,
                lifecycleScope,
                DataRepository.lista,
                DataRepository.filtriranaLista
            ) { biljka -> filterOnClick(biljka) }

            bojeSpiner.visibility = View.INVISIBLE
            pretragaET.visibility = View.INVISIBLE
            buttonPretrazi.visibility = View.INVISIBLE

            //reset
            button.setOnClickListener {
lifecycleScope.launch{
                if (DataRepository.webPoziv == true) {
                    DataRepository.filtriranaLista.clear()
                    var lista = getAllBiljkas(this@MainActivity)//getBiljke()
                    if (lista != null) {
                        for (b in lista) {
                            DataRepository.filtriranaLista.add(b)
                        }
                    }
                    DataRepository.webPoziv = false
                } else {
                    DataRepository.filtriranaLista.clear()
                    for (b in DataRepository.lista) {
                        DataRepository.filtriranaLista.add(b)
                    }
                }
                if (fokus == "Medicinski") {
                    medicinskeBiljkeAdapter.updateBiljke(DataRepository.filtriranaLista)
                    medicinskeBiljkeAdapter.notifyDataSetChanged()
                } else if (fokus == "Botanicki") {
                    botanickeBiljkeAdapter.updateBiljke(DataRepository.filtriranaLista)
                    botanickeBiljkeAdapter.notifyDataSetChanged()
                } else {
                    kuharskeBiljkeAdapter.updateBiljke(DataRepository.filtriranaLista)
                    kuharskeBiljkeAdapter.notifyDataSetChanged()
                }
            }
            }

            //promjena fokusa/modova
            spiner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val text = parent?.getItemAtPosition(position).toString()

                    if (text.equals("Medicinski")) {

                        if (fokus == "Botanicki") {
                            if (DataRepository.webPoziv == true) {

                                DataRepository.filtriranaLista.clear()
                                for (b in DataRepository.staraLista) {
                                    DataRepository.filtriranaLista.add(b)
                                }
                                DataRepository.webPoziv = false
                            } else {
                                DataRepository.filtriranaLista = botanickeBiljkeAdapter.biljke
                            }
                            bojeSpiner.visibility = View.INVISIBLE
                            pretragaET.visibility = View.INVISIBLE
                            buttonPretrazi.visibility = View.INVISIBLE
                        } else if (fokus == "Kuharski") {
                            DataRepository.filtriranaLista = kuharskeBiljkeAdapter.biljke
                        }
                        fokus = "Medicinski"
                        biljke.layoutManager = LinearLayoutManager(
                            this@MainActivity,
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        medicinskeBiljkeAdapter = KolekcijaMedicinskihBiljakaAdapter(
                            this@MainActivity,
                            lifecycleScope,
                            DataRepository.lista,
                            DataRepository.filtriranaLista
                        ) { biljka -> filterOnClick(biljka) }
                        biljke.adapter = medicinskeBiljkeAdapter

                    } else if (text.equals("Botanicki")) {

                        if (fokus == "Medicinski") {
                            DataRepository.filtriranaLista = medicinskeBiljkeAdapter.biljke
                        } else if (fokus == "Kuharski") {
                            DataRepository.filtriranaLista = kuharskeBiljkeAdapter.biljke
                        }
                        fokus = "Botanicki"
                        biljke.layoutManager = LinearLayoutManager(
                            this@MainActivity,
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        botanickeBiljkeAdapter = KolekcijaBotanickihBiljakaAdapter(
                            this@MainActivity,
                            lifecycleScope,
                            DataRepository.lista,
                            DataRepository.filtriranaLista
                        ) { biljka -> filterOnClick(biljka) }
                        biljke.adapter = botanickeBiljkeAdapter

                        bojeSpiner.visibility = View.VISIBLE
                        pretragaET.visibility = View.VISIBLE
                        buttonPretrazi.visibility = View.VISIBLE
                    } else {

                        if (fokus == "Medicinski") {
                            DataRepository.filtriranaLista = medicinskeBiljkeAdapter.biljke
                        } else if (fokus == "Botanicki") {
                            if (DataRepository.webPoziv == true) {
                                DataRepository.filtriranaLista.clear()
                                for (b in DataRepository.staraLista) {
                                    DataRepository.filtriranaLista.add(b)
                                }
                                DataRepository.webPoziv = false
                            } else {
                                DataRepository.filtriranaLista = botanickeBiljkeAdapter.biljke
                            }
                            bojeSpiner.visibility = View.INVISIBLE
                            pretragaET.visibility = View.INVISIBLE
                            buttonPretrazi.visibility = View.INVISIBLE
                        }
                        fokus = "Kuharski"
                        biljke.layoutManager =
                            LinearLayoutManager(
                                this@MainActivity,
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                        kuharskeBiljkeAdapter = KolekcijaKuharskihBiljakaAdapter(
                            this@MainActivity,
                            lifecycleScope,
                            DataRepository.lista,
                            DataRepository.filtriranaLista
                        ) { biljka -> filterOnClick(biljka) }
                        biljke.adapter = kuharskeBiljkeAdapter
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

            dodajBiljku = findViewById(R.id.novaBiljkaBtn)

            dodajBiljku.setOnClickListener {

                val intent = Intent(this@MainActivity, NovaBiljkaActivity::class.java).apply {
                }
                startActivity(intent)
            }

            if (intent.extras != null) {
                medicinskeBiljkeAdapter.notifyDataSetChanged()
                botanickeBiljkeAdapter.notifyDataSetChanged()
                kuharskeBiljkeAdapter.notifyDataSetChanged()
            }

            bojeSpiner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    val text = parent?.getItemAtPosition(position).toString()
                    if (!text.equals("Odaberi boju: ")) {
                        boja = text
                        if (parent != null) {
                            parent.setSelection(0)
                        };
                    }
                }


                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

            buttonPretrazi.setOnClickListener {
                if (boja != "" && boja != "Odaberi boju: ") {
                    val pretragaText = pretragaET.text.toString().trim()
                    if (pretragaText.isNotEmpty() && boja.isNotEmpty()) {

                        getPlantswithFlowerColor(boja, pretragaText)
                        boja = ""
                        pretragaET.text.clear()

                    } else {
                        val toast =
                            Toast.makeText(
                                this@MainActivity,
                                "Nije naveden podstring",
                                Toast.LENGTH_SHORT
                            )
                        toast.show()
                    }
                } else {
                    val toast =
                        Toast.makeText(this@MainActivity, "Nije izabrana boja", Toast.LENGTH_SHORT)
                    toast.show()
                }
            }

        }
    }
    fun filterOnClick(clickedItem : Biljka){
        if(fokus=="Medicinski"){
            val poredbena = clickedItem.medicinskeKoristi ?: emptyList()
            var kolekcijaBiljaka = medicinskeBiljkeAdapter.sveBiljke.filter{
                    biljka->biljka.medicinskeKoristi?.any{korist ->korist in poredbena} ?: false
            }

            DataRepository.filtriranaLista=kolekcijaBiljaka as ArrayList<Biljka>
            medicinskeBiljkeAdapter.biljke=DataRepository.filtriranaLista
            medicinskeBiljkeAdapter.notifyDataSetChanged()
        }
        else if(fokus=="Botanicki") {
            if (DataRepository.webPoziv == false){
            val poredbenaPorodica = clickedItem.porodica
            var poredbeneKlime = clickedItem.klimatskiTipovi ?: emptyList()
            var poredbenaZemljista = clickedItem.zemljisniTipovi ?: emptyList()
            var kolekcijaBiljaka: List<Biljka> = listOf()
            for (b in botanickeBiljkeAdapter.sveBiljke) {
                if (b.porodica == poredbenaPorodica) {
                    var klime = botanickeBiljkeAdapter.nadjiIsteKlime(
                        poredbeneKlime,
                        b.klimatskiTipovi ?: emptyList()
                    )
                    if (klime.isNotEmpty()) kolekcijaBiljaka = kolekcijaBiljaka + b
                    else {
                        var zemljista = botanickeBiljkeAdapter.nadjiIstaZemljista(
                            poredbenaZemljista,
                            b.zemljisniTipovi ?: emptyList()
                        )
                        if (zemljista.isNotEmpty()) kolekcijaBiljaka = kolekcijaBiljaka + b
                    }

                }
            }
            DataRepository.filtriranaLista = kolekcijaBiljaka as ArrayList<Biljka>
            botanickeBiljkeAdapter.biljke = DataRepository.filtriranaLista
            botanickeBiljkeAdapter.notifyDataSetChanged()
        }
        }
        else {
            val poredbeniOkus = clickedItem.profilOkusa
            val poredbenaJela = clickedItem.jela
            var kolekcijaBiljaka : List<Biljka> = listOf()
            for(b in kuharskeBiljkeAdapter.sveBiljke){
                if(b.profilOkusa==poredbeniOkus) kolekcijaBiljaka=kolekcijaBiljaka+b
                else{
                    var istaJela = kuharskeBiljkeAdapter.nadjiIstaJela(b.jela ?: emptyList(),poredbenaJela ?: emptyList())
                    if(istaJela.isNotEmpty()) kolekcijaBiljaka=kolekcijaBiljaka+b
                }
            }
            DataRepository.filtriranaLista = kolekcijaBiljaka as ArrayList<Biljka>
            kuharskeBiljkeAdapter.biljke=DataRepository.filtriranaLista
            kuharskeBiljkeAdapter.notifyDataSetChanged()
        }
    }

    fun getPlantswithFlowerColor(flower_color:String , substr : String) {
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        // Kreira se Coroutine na UI

        // Vrti se poziv servisa i suspendira se rutina dok se `withContext` ne zavrsi
        scope.launch {
            val trefleDAO: TrefleDAO = TrefleDAO()
            trefleDAO.setBitmap(this@MainActivity)

            val result = trefleDAO.getPlantswithFlowerColor(flower_color,substr)
            // Prikaze se rezultat korisniku na glavnoj niti
            searchDone(result)
        }
    }
    fun searchDone(biljke: List<Biljka>) {

        DataRepository.staraLista.clear()
        for(b in DataRepository.filtriranaLista){
            DataRepository.staraLista.add(b)
        }

        DataRepository.webPoziv=true
        DataRepository.filtriranaLista.clear()
        for(b in biljke){
            DataRepository.filtriranaLista.add(b)
        }
        if(fokus=="Medicinski"){
            medicinskeBiljkeAdapter.updateBiljke(DataRepository.filtriranaLista)
            medicinskeBiljkeAdapter.notifyDataSetChanged()
        }
        else if (fokus=="Botanicki"){
            botanickeBiljkeAdapter.updateBiljke(DataRepository.filtriranaLista)
            botanickeBiljkeAdapter.notifyDataSetChanged()
        }
        else {
            kuharskeBiljkeAdapter.updateBiljke(DataRepository.filtriranaLista)
            kuharskeBiljkeAdapter.notifyDataSetChanged()
        }
    }

    fun onError() {
        val toast = Toast.makeText(this, "Greška pri dohvatanju biljke iz baze", Toast.LENGTH_SHORT)
        toast.show()
    }
    suspend fun getAllBiljkas(context: Context): List<Biljka>?{
        return withContext(Dispatchers.IO){
            try {
                var db = BiljkaDatabase.getInstance(context)
                var sveBiljke = db!!.biljkaDao().getAllBiljkas()

                return@withContext sveBiljke
            }catch(error: Exception){

                return@withContext null
            }
        }
    }


    suspend fun saveBiljka(context: Context, biljka: Biljka): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val db = BiljkaDatabase.getInstance(context)
                db.biljkaDao().saveBiljka(biljka)
                true
            } catch (error: Exception) {
                false
            }
        }
    }

    suspend fun fixOffline(context: Context): Int{
        return withContext(Dispatchers.IO){
            try {
                var db = BiljkaDatabase.getInstance(context)

                var sveBiljke = db!!.biljkaDao().fixOfflineBiljka()

                return@withContext sveBiljke
            }catch(error: Exception){
        return@withContext -1

            }
        }
    }


}



