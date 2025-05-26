package com.example.spirala_1


import android.content.Context
import androidx.media3.test.utils.TestUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions

import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.core.Is
import org.junit.*
import java.io.IOException
import android.graphics.Bitmap
import androidx.core.database.getStringOrNull
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.matcher.ViewMatchers
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.greaterThan
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters







@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private val countBiljka = "SELECT COUNT(*) AS broj_biljaka FROM Biljka"
    private val countBiljkaBitmaps = "SELECT COUNT(*) AS broj_bitmapa FROM BiljkaBitmap"

    private val describeBiljka = "pragma table_info('Biljka')"
    private val describeBiljkaBitmap = "pragma table_info('BiljkaBitmap')"

    private val kolone = mapOf(
        "BiljkaBitmap" to arrayListOf("idBiljke", "bitmap"),
        "Biljka" to arrayListOf(
            "id",
            "naziv",
            "family",
            "medicinskoUpozorenje",
            "jela",
            "klimatskiTipovi",
            "zemljisniTipovi",
            "medicinskeKoristi"
        )
    )

    companion object {
        lateinit var db: SupportSQLiteDatabase
        lateinit var context: Context
        lateinit var roomDb: BiljkaDatabase
        lateinit var biljkaDAO: BiljkaDAO

        @BeforeClass
        @JvmStatic
        fun createDB() = runBlocking {
            val scenarioRule = ActivityScenario.launch(MainActivity::class.java)
            context = ApplicationProvider.getApplicationContext()
            roomDb = Room.inMemoryDatabaseBuilder(context, BiljkaDatabase::class.java).build()
            biljkaDAO = roomDb.biljkaDao()
            biljkaDAO.getAllBiljkas()
            db = roomDb.openHelper.readableDatabase

        }
    }

    @Test
    @Throws(Exception::class)
    fun testFixOfflineBiljka() {
        val biljka1 = Biljka(
            naziv = "Biljka (Eriogonum thymoides)",
            porodica = "porodica",
            medicinskoUpozorenje = "Medicinsko upozorenje",
            medicinskeKoristi = listOf(),
            profilOkusa = ProfilOkusaBiljke.SLATKI,
            jela = listOf("jelo"),
            klimatskiTipovi = listOf(),
            zemljisniTipovi = listOf()
        )
        val biljka2 = Biljka(
            naziv = "Biljka (Taraxacum palustre)",
            porodica = "porodica",
            medicinskoUpozorenje = "Medicinsko upozorenje",
            medicinskeKoristi = listOf(),
            profilOkusa = ProfilOkusaBiljke.SLATKI,
            jela = listOf("jelo"),
            klimatskiTipovi = listOf(),
            zemljisniTipovi = listOf()
        )

        GlobalScope.launch(Dispatchers.IO) {
            biljkaDAO.saveBiljka(biljka1)
            biljkaDAO.saveBiljka(biljka2)

            val updatedCount = biljkaDAO.fixOfflineBiljka()

            val updatedBiljka1 = biljkaDAO.getAllBiljkas().find { it.naziv == biljka1.naziv }
            val updatedBiljka2 = biljkaDAO.getAllBiljkas().find { it.naziv == biljka2.naziv }

            assertEquals(2, updatedCount)
            assertEquals(true, updatedBiljka1?.onlineChecked)
            assertEquals(true, updatedBiljka2?.onlineChecked)
        }
    }

    private fun executeCountAndCheck(query: String, column: String, value: Long) {
        var rezultat = BiljkeDB4test.db.query(query)
        rezultat.moveToFirst()
        var brojOdgovora = rezultat.getLong(0)
        MatcherAssert.assertThat(brojOdgovora, CoreMatchers.`is`(CoreMatchers.equalTo(value)))
    }

    private fun checkColumns(query: String, naziv: String) {
        var rezultat = BiljkeDB4test.db.query(query)
        val list = (1..rezultat.count).map {
            rezultat.moveToNext()
            rezultat.getString(1)
        }
        ViewMatchers.assertThat(list, CoreMatchers.hasItems(*kolone[naziv]!!.toArray()))
    }

    @get:Rule
    val intentsTestRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testDodavanjaBiljkeUBazu() = runBlocking{
        biljkaDAO.clearData()
        val biljka: Biljka = Biljka(null,"(Campanula rapunculus)", "Campanulaceae", "Nemoj dirati",
            emptyList(), ProfilOkusaBiljke.AROMATICNO, emptyList(), emptyList(), emptyList() )
        biljkaDAO.saveBiljka(biljka)
        val sveBiljke = biljkaDAO.getAllBiljkas()
        val velicina: Int = sveBiljke.size
        assertThat(sveBiljke[velicina-1].naziv, `is`("(Campanula rapunculus)"))
    }

    @Test
    fun testCiscenjaBazeSaClearData() = runBlocking{
        val biljka: Biljka = Biljka(null,"(Campanula rapunculus)", "Campanulaceae", "Nemoj dirati",
            emptyList(), ProfilOkusaBiljke.AROMATICNO, emptyList(), emptyList(), emptyList() )
        biljkaDAO.saveBiljka(biljka)
        val brojBiljki: Int = biljkaDAO.getAllBiljkas().size
        System.out.println(brojBiljki)
        assertThat(brojBiljki, greaterThan(0))
        biljkaDAO.clearData()
        val noviBrojBiljki = biljkaDAO.getAllBiljkas().size
        System.out.println(noviBrojBiljki)
        assertThat(noviBrojBiljki, equalTo(0))

    }

}

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class BiljkeDB4test {

    private val countBiljka = "SELECT COUNT(*) AS broj_biljaka FROM Biljka"
    private val countBiljkaBitmaps = "SELECT COUNT(*) AS broj_bitmapa FROM BiljkaBitmap"

    private val describeBiljka = "pragma table_info('Biljka')"
    private val describeBiljkaBitmap = "pragma table_info('BiljkaBitmap')"

    private val kolone = mapOf(
        "BiljkaBitmap" to arrayListOf("idBiljke", "bitmap"),
        "Biljka" to arrayListOf(
            "id",
            "naziv",
            "family",
            "medicinskoUpozorenje",
            "jela",
            "klimatskiTipovi",
            "zemljisniTipovi",
            "medicinskeKoristi"
        )
    )

    companion object {
        lateinit var db: SupportSQLiteDatabase
        lateinit var context: Context
        lateinit var roomDb: BiljkaDatabase
        lateinit var biljkaDAO: BiljkaDAO

        @BeforeClass
        @JvmStatic
        fun createDB() = runBlocking {
            val scenarioRule = ActivityScenario.launch(MainActivity::class.java)
            context = ApplicationProvider.getApplicationContext()
            roomDb = Room.inMemoryDatabaseBuilder(context, BiljkaDatabase::class.java).build()
            biljkaDAO = roomDb.biljkaDao()
            biljkaDAO.getAllBiljkas()
            db = roomDb.openHelper.readableDatabase

        }
    }

    private fun executeCountAndCheck(query: String, column: String, value: Long) {
        var rezultat = db.query(query)
        rezultat.moveToFirst()
        var brojOdgovora = rezultat.getLong(0)
        assertThat(brojOdgovora, `is`(equalTo(value)))
    }

    private fun checkColumns(query: String, naziv: String) {
        var rezultat = db.query(query)
        val list = (1..rezultat.count).map {
            rezultat.moveToNext()
            rezultat.getString(1)
        }
        assertThat(list, hasItems(*kolone[naziv]!!.toArray()))
    }

    @get:Rule
    val intentsTestRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun a0_insertFirstBiljka() = runBlocking {
        biljkaDAO.saveBiljka(
            Biljka(
                naziv = "Bosiljak (Ocimum basilicum)",
                porodica = "Lamiaceae (usnate)",
                medicinskoUpozorenje = "Može iritati kožu osjetljivu na sunce. Preporučuje se oprezna upotreba pri korištenju ulja bosiljka.",
                medicinskeKoristi = listOf(
                    MedicinskaKorist.SMIRENJE,
                    MedicinskaKorist.REGULACIJAPROBAVE
                ),
                profilOkusa = ProfilOkusaBiljke.BEZUKUSNO,
                jela = listOf("Salata od paradajza", "Punjene tikvice"),
                klimatskiTipovi = listOf(KlimatskiTip.SREDOZEMNA, KlimatskiTip.SUBTROPSKA),
                zemljisniTipovi = listOf(Zemljiste.PJESKOVITO, Zemljiste.ILOVACA)
            )
        )

        assertThat(biljkaDAO.getAllBiljkas().size, `is`(1))
    }

    @Test
    fun a1_tableBiljkaHasAllColumns() = runBlocking {
        checkColumns(describeBiljka, "Biljka")
    }

    @Test
    fun a2_tableBiljkaBitmapHasAllColumns() = runBlocking {
        checkColumns(describeBiljkaBitmap, "BiljkaBitmap")
    }

    @Test
    fun a3_insertZaistaUpisaoUBazu() = runBlocking {
        executeCountAndCheck(countBiljka, "broj_biljaka", 1)
    }

    @Test
    fun a4_dodajJosJednuBiljkuIGetAllBiljkas() = runBlocking {
        biljkaDAO.saveBiljka(
            Biljka(
                naziv = "Kamilica (Matricaria chamomilla)",
                porodica = "Asteraceae (glavočike)",
                medicinskoUpozorenje = "Može uzrokovati alergijske reakcije kod osjetljivih osoba.",
                medicinskeKoristi = listOf(
                    MedicinskaKorist.SMIRENJE,
                    MedicinskaKorist.PROTUUPALNO
                ),
                profilOkusa = ProfilOkusaBiljke.AROMATICNO,
                jela = listOf("Čaj od kamilice"),
                klimatskiTipovi = listOf(KlimatskiTip.UMJERENA, KlimatskiTip.SUBTROPSKA),
                zemljisniTipovi = listOf(Zemljiste.PJESKOVITO, Zemljiste.KRECNJACKO)
            )
        )
        assertThat(biljkaDAO.getAllBiljkas().size, `is`(2))
        executeCountAndCheck(countBiljka, "broj_biljaka", 2)
    }

    @Test
    fun a5_provjeriDaUSvimBiljkamaPostojiKoristSmirenje() = runBlocking {
        var biljke = biljkaDAO.getAllBiljkas()
        for (biljka in biljke) {
            assertThat(biljka.medicinskeKoristi?.contains(MedicinskaKorist.SMIRENJE) , `is`(true))
        }
    }

    @Test
    fun a6_addSlikaAndCheckItsAdded() = runBlocking {
        executeCountAndCheck(countBiljkaBitmaps, "broj_bitmapa", 0)
        var biljka1 = biljkaDAO.getAllBiljkas().get(0).id
        var bitmap = Bitmap.createBitmap(200, 300, Bitmap.Config.ARGB_8888)
        //napravite da je prvi parametar BiljkaBitmap id koji je PrimaryKey(autoGenerate=true)
        biljkaDAO.addImage(biljka1 ?: 0, bitmap)
        executeCountAndCheck(countBiljkaBitmaps, "broj_bitmapa", 1)
        var bitmapCursor = db.query("SELECT bitmap FROM BiljkaBitmap")
        bitmapCursor.moveToFirst()
        assertThat(bitmapCursor.getStringOrNull(0)?.length ?: 0, greaterThan(100))
    }

    @Test
    fun a7_deleteAllAndCheckIfItsEmpty() = runBlocking {
        biljkaDAO.clearData()
        executeCountAndCheck(countBiljkaBitmaps, "broj_bitmapa", 0)
        executeCountAndCheck(countBiljka, "broj_biljaka", 0)
    }

}


suspend fun suspendsaveBiljka(biljka:Biljka , context: Context) : Boolean{
    return withContext(Dispatchers.IO) {
        var dba = BiljkaDatabase.getInstance(context)
        var bool = dba!!.biljkaDao().saveBiljka(biljka)
        return@withContext bool
    }
}

fun saveBiljka(biljka:Biljka,context:Context){
    val scope = CoroutineScope(Job() + Dispatchers.Main)
    // Create a new coroutine on the UI thread
    scope.launch{

        // Make the network call and suspend execution until it finishes
        val result = suspendsaveBiljka(biljka,context)

        // Display result of the network request to the user

    }
}


suspend fun suspendGetAllBiljkas(context: Context) : List<Biljka>{
    return withContext(Dispatchers.IO) {
        var dba = BiljkaDatabase.getInstance(context)
        var bool = dba!!.biljkaDao().getAllBiljkas()
        return@withContext bool
    }
}

fun getAllBiljkas(context:Context):List<Biljka>{
    val scope = CoroutineScope(Job() + Dispatchers.Main)
    // Create a new coroutine on the UI thread
    var result:List<Biljka> = listOf()
    scope.launch{

        // Make the network call and suspend execution until it finishes
         result = suspendGetAllBiljkas(context)

        // Display result of the network request to the user

    }
    return result
}





@RunWith(AndroidJUnit4::class)
class TestS1a {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(
        MainActivity::class.java
    )

    @Test
    fun prikazujuSePocetneBiljke() {
        val recyclerView = activityScenarioRule.scenario.onActivity {
            val recyclerView = it.findViewById<RecyclerView>(R.id.biljkeRV)
            val itemCount = recyclerView.adapter!!.itemCount
            assertThat(itemCount, greaterThan(4))
        }
        val listaNaziva = listOf(
            "Bosiljak (Ocimum basilicum)",
            "Nana (Mentha spicata)",
            "Kamilica (Matricaria chamomilla)",
            "RuÅ¾marin (Rosmarinus officinalis)",
            "Lavanda (Lavandula angustifolia)"
        )
        for (naziv in listaNaziva)
            onView(withId(R.id.biljkeRV)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    allOf(
                        hasDescendant(withText(naziv)),
                        hasDescendant(withId(R.id.nazivItem))
                    )
                )
            )

    }

    @Test
    fun spinnerImaSveModove() {
        val listaNazivaModova = listOf("Medicin", "Kuha", "Botan")
        for (naziv in listaNazivaModova) {
            onView(withId(R.id.modSpinner)).perform(click())
            onData(
                allOf(
                    Is(instanceOf(String::class.java)),
                    containsString(naziv)
                )
            ).perform(click())
        }
    }

    @Test
    fun modChange() {
        onView(withId(R.id.modSpinner)).perform(click())
        onData(
            allOf(
                Is(instanceOf(String::class.java)),
                containsString("Medicin")
            )
        ).perform(click())
        onView(withId(R.id.biljkeRV)).perform(
            RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                allOf(
                    hasDescendant(withText("Bosiljak (Ocimum basilicum)")),
                    hasDescendant(withText(containsString("Smirenje")))
                )
            )
        )


        onView(withId(R.id.modSpinner)).perform(click())
        onData(allOf(Is(instanceOf(String::class.java)), containsString("Kuha"))).perform(click())
        onView(withId(R.id.biljkeRV)).perform(
            RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                allOf(
                    hasDescendant(withText("Bosiljak (Ocimum basilicum)")),
                    not(hasDescendant(withText(containsString("Smirenje")))),
                    hasDescendant(withText(containsString("Salata")))
                )
            )
        )
    }

    @Test
    fun onFilterInMedicinskiModBosiljak() {
        onView(withId(R.id.modSpinner)).perform(click())
        onData(
            allOf(
                Is(instanceOf(String::class.java)),
                containsString("Medicin")
            )
        ).perform(click())
        onView(withId(R.id.biljkeRV)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                allOf(
                    hasDescendant(withText("Bosiljak (Ocimum basilicum)")),
                    hasDescendant(withText(containsString("Smirenje")))
                ), click()
            )
        )
        val listaVidljivihNaziva = listOf(
            "Bosiljak (Ocimum basilicum)",
            "Kamilica (Matricaria chamomilla)",
            "Lavanda (Lavandula angustifolia)"
        )
        val listaNevidljivihNaziva =
            listOf("Nana (Mentha spicata)", "RuÅ¾marin (Rosmarinus officinalis)")
        for (naziv in listaVidljivihNaziva)
            onView(withId(R.id.biljkeRV)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    allOf(
                        hasDescendant(withText(naziv)),
                        hasDescendant(withId(R.id.nazivItem))
                    )
                )
            )
        for (naziv in listaNevidljivihNaziva) {
            try {
                onView(withId(R.id.biljkeRV)).perform(
                    RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                        allOf(
                            hasDescendant(withText(naziv)),
                            hasDescendant(withId(R.id.nazivItem))
                        )
                    )
                )
                assert(
                    false,
                    { "Scroll treba pasti sljedeÄ‡a biljka se prikazuje nakon filtriranja a ne treba. Biljka:  " + naziv })
            } catch (e: Exception) {
                assertThat(e.message, containsString("Error performing"))
            }
        }
    }

    @Test
    fun svakiModImaIspravneIdeve() {
        onView(withId(R.id.modSpinner)).perform(click())
        onData(
            allOf(
                Is(instanceOf(String::class.java)),
                containsString("Medicin")
            )
        ).perform(click())
        val listMedicinskiIds = listOf(
            R.id.nazivItem,
            R.id.slikaItem,
            R.id.korist1Item,
            R.id.korist2Item,
            R.id.korist3Item,
            R.id.upozorenjeItem
        )
        for (id in listMedicinskiIds) {
            onView(withId(R.id.biljkeRV)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    allOf(
                        hasDescendant(withText("Bosiljak (Ocimum basilicum)")),
                        hasDescendant(withText(containsString("Smirenje"))),
                        hasDescendant(withId(id))
                    )
                )
            )
        }

        onView(withId(R.id.modSpinner)).perform(click())
        onData(allOf(Is(instanceOf(String::class.java)), containsString("Kuh"))).perform(click())
        val listKuharskiIds = listOf(
            R.id.nazivItem,
            R.id.slikaItem,
            R.id.jelo1Item,
            R.id.jelo2Item,
            R.id.jelo3Item,
            R.id.profilOkusaItem
        )
        for (id in listKuharskiIds) {
            onView(withId(R.id.biljkeRV)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    allOf(
                        hasDescendant(withText("Bosiljak (Ocimum basilicum)")),
                        hasDescendant(withText(containsString("biljni okus"))),
                        hasDescendant(withId(id))
                    )
                )
            )
        }

        onView(withId(R.id.modSpinner)).perform(click())
        onData(allOf(Is(instanceOf(String::class.java)), containsString("Botan"))).perform(click())
        val listBotanickiIds = listOf(
            R.id.nazivItem,
            R.id.slikaItem,
            R.id.porodicaItem,
            R.id.zemljisniTipItem,
            R.id.klimatskiTipItem
        )
        for (id in listBotanickiIds) {
            onView(withId(R.id.biljkeRV)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    allOf(
                        hasDescendant(withText("Bosiljak (Ocimum basilicum)")),
                        hasDescendant(withText(containsString("Mediteranska"))),
                        hasDescendant(withId(id))
                    )
                )
            )
        }

    }

    @Test
    fun onFilterInKuhanjeMenta() {
        onView(withId(R.id.modSpinner)).perform(click())
        onData(
            allOf(
                Is(instanceOf(String::class.java)),
                containsString("Kuha")
            )
        ).perform(click())
        onView(withId(R.id.biljkeRV)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                allOf(
                    hasDescendant(withText("Nana (Mentha spicata)")),
                    hasDescendant(withText(containsString("Gulaš")))
                ), click()
            )
        )
        val listaVidljivihNaziva = listOf(
            "Nana (Mentha spicata)",
            "RuÅ¾marin (Rosmarinus officinalis)",
            "Lavanda (Lavandula angustifolia)"
        )
        val listaNevidljivihNaziva =
            listOf("Bosiljak (Ocimum basilicum)", "Kamilica (Matricaria chamomilla)")
        for (naziv in listaVidljivihNaziva)
            onView(withId(R.id.biljkeRV)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    allOf(
                        hasDescendant(withText(naziv)),
                        hasDescendant(withId(R.id.nazivItem))
                    )
                )
            )
        for (naziv in listaNevidljivihNaziva) {
            try {
                onView(withId(R.id.biljkeRV)).perform(
                    RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                        allOf(
                            hasDescendant(withText(naziv)),
                            hasDescendant(withId(R.id.nazivItem))
                        )
                    )
                )
                assert(
                    false,
                    { "Scroll treba pasti sljedeÄ‡a biljka se prikazuje nakon filtriranja a ne treba. Biljka:  " + naziv })
            } catch (e: Exception) {
                assertThat(e.message, containsString("Error performing"))
            }
        }
    }

    @Test
    fun onFilterInBotanicLavanda() {
        onView(withId(R.id.modSpinner)).perform(click())
        onData(
            allOf(
                Is(instanceOf(String::class.java)),
                containsString("Bota")
            )
        ).perform(click())
        onView(withId(R.id.biljkeRV)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                allOf(
                    hasDescendant(withText("Lavanda (Lavandula angustifolia)")),
                    hasDescendant(withText(containsString("Mediteranska")))
                ), click()
            )
        )
        val listaVidljivihNaziva = listOf(
            "RuÅ¾marin (Rosmarinus officinalis)",
            "Lavanda (Lavandula angustifolia)"
        )
        val listaNevidljivihNaziva =
            listOf(
                "Nana (Mentha spicata)", "Kamilica (Matricaria chamomilla)",
                "Bosiljak (Ocimum basilicum)",)
        for (naziv in listaVidljivihNaziva)
            onView(withId(R.id.biljkeRV)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    allOf(
                        hasDescendant(withText(naziv)),
                        hasDescendant(withId(R.id.nazivItem))
                    )
                )
            )
        for (naziv in listaNevidljivihNaziva) {
            try {
                onView(withId(R.id.biljkeRV)).perform(
                    RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                        allOf(
                            hasDescendant(withText(naziv)),
                            hasDescendant(withId(R.id.nazivItem))
                        )
                    )
                )
                assert(
                    false,
                    { "Scroll treba pasti sljedeÄ‡a biljka se prikazuje nakon filtriranja a ne treba. Biljka:  " + naziv })
            } catch (e: Exception) {
                assertThat(e.message, containsString("Error performing"))
            }
        }
    }


    @Test
    fun onFilterInBotanicLavandaWithModChange() {
        onView(withId(R.id.modSpinner)).perform(click())
        onData(
            allOf(
                Is(instanceOf(String::class.java)),
                containsString("Bota")
            )
        ).perform(click())
        onView(withId(R.id.biljkeRV)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                allOf(
                    hasDescendant(withText("Lavanda (Lavandula angustifolia)")),
                    hasDescendant(withText(containsString("Mediteranska")))
                ), click()
            )
        )
        val listaVidljivihNaziva = listOf(
            "RuÅ¾marin (Rosmarinus officinalis)",
            "Lavanda (Lavandula angustifolia)"
        )
        val listaNevidljivihNaziva =
            listOf(
                "Nana (Mentha spicata)", "Kamilica (Matricaria chamomilla)",
                "Bosiljak (Ocimum basilicum)",)
        for (naziv in listaVidljivihNaziva)
            onView(withId(R.id.biljkeRV)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    allOf(
                        hasDescendant(withText(naziv)),
                        hasDescendant(withId(R.id.nazivItem))
                    )
                )
            )
        for (naziv in listaNevidljivihNaziva) {
            try {
                onView(withId(R.id.biljkeRV)).perform(
                    RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                        allOf(
                            hasDescendant(withText(naziv)),
                            hasDescendant(withId(R.id.nazivItem))
                        )
                    )
                )
                assert(
                    false,
                    { "Scroll treba pasti sljedeÄ‡a biljka se prikazuje nakon filtriranja a ne treba. Biljka:  " + naziv })
            } catch (e: Exception) {
                assertThat(e.message, containsString("Error performing"))
            }
        }

        onView(withId(R.id.modSpinner)).perform(click())
        onData(
            allOf(
                Is(instanceOf(String::class.java)),
                containsString("Medicin")
            )
        ).perform(click())
        for (naziv in listaVidljivihNaziva)
            onView(withId(R.id.biljkeRV)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    allOf(
                        hasDescendant(withText(naziv)),
                        hasDescendant(withId(R.id.nazivItem))
                    )
                )
            )
        for (naziv in listaNevidljivihNaziva) {
            try {
                onView(withId(R.id.biljkeRV)).perform(
                    RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                        allOf(
                            hasDescendant(withText(naziv)),
                            hasDescendant(withId(R.id.nazivItem))
                        )
                    )
                )
                assert(
                    false,
                    { "Scroll treba pasti sljedeÄ‡a biljka se prikazuje nakon filtriranja a ne treba. Biljka:  " + naziv })
            } catch (e: Exception) {
                assertThat(e.message, containsString("Error performing"))
            }
        }
        onView(withId(R.id.modSpinner)).perform(click())
        onData(
            allOf(
                Is(instanceOf(String::class.java)),
                containsString("Kuha")
            )
        ).perform(click())
        for (naziv in listaVidljivihNaziva)
            onView(withId(R.id.biljkeRV)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    allOf(
                        hasDescendant(withText(naziv)),
                        hasDescendant(withId(R.id.nazivItem))
                    )
                )
            )
        for (naziv in listaNevidljivihNaziva) {
            try {
                onView(withId(R.id.biljkeRV)).perform(
                    RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                        allOf(
                            hasDescendant(withText(naziv)),
                            hasDescendant(withId(R.id.nazivItem))
                        )
                    )
                )
                assert(
                    false,
                    { "Scroll treba pasti sljedeÄ‡a biljka se prikazuje nakon filtriranja a ne treba. Biljka:  " + naziv })
            } catch (e: Exception) {
                assertThat(e.message, containsString("Error performing"))
            }
        }
    }
    @Test
    fun onFilterInKuhanjeMentaWithModChange() {
        onView(withId(R.id.modSpinner)).perform(click())
        onData(
            allOf(
                Is(instanceOf(String::class.java)),
                containsString("Kuha")
            )
        ).perform(click())
        onView(withId(R.id.biljkeRV)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                allOf(
                    hasDescendant(withText("Nana (Mentha spicata)")),
                    hasDescendant(withText(containsString("Gulaš")))
                ), click()
            )
        )
        val listaVidljivihNaziva = listOf(
            "Nana (Mentha spicata)",
            "RuÅ¾marin (Rosmarinus officinalis)",
            "Lavanda (Lavandula angustifolia)"
        )
        val listaNevidljivihNaziva =
            listOf("Bosiljak (Ocimum basilicum)", "Kamilica (Matricaria chamomilla)")
        for (naziv in listaVidljivihNaziva)
            onView(withId(R.id.biljkeRV)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    allOf(
                        hasDescendant(withText(naziv)),
                        hasDescendant(withId(R.id.nazivItem))
                    )
                )
            )
        for (naziv in listaNevidljivihNaziva) {
            try {
                onView(withId(R.id.biljkeRV)).perform(
                    RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                        allOf(
                            hasDescendant(withText(naziv)),
                            hasDescendant(withId(R.id.nazivItem))
                        )
                    )
                )
                assert(
                    false,
                    { "Scroll treba pasti sljedeÄ‡a biljka se prikazuje nakon filtriranja a ne treba. Biljka:  " + naziv })
            } catch (e: Exception) {
                assertThat(e.message, containsString("Error performing"))
            }
        }
        onView(withId(R.id.modSpinner)).perform(click())
        onData(
            allOf(
                Is(instanceOf(String::class.java)),
                containsString("Medicin")
            )
        ).perform(click())
        for (naziv in listaVidljivihNaziva)
            onView(withId(R.id.biljkeRV)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    allOf(
                        hasDescendant(withText(naziv)),
                        hasDescendant(withId(R.id.nazivItem))
                    )
                )
            )
        for (naziv in listaNevidljivihNaziva) {
            try {
                onView(withId(R.id.biljkeRV)).perform(
                    RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                        allOf(
                            hasDescendant(withText(naziv)),
                            hasDescendant(withId(R.id.nazivItem))
                        )
                    )
                )
                assert(
                    false,
                    { "Scroll treba pasti sljedeÄ‡a biljka se prikazuje nakon filtriranja a ne treba. Biljka:  " + naziv })
            } catch (e: Exception) {
                assertThat(e.message, containsString("Error performing"))
            }
        }
        onView(withId(R.id.modSpinner)).perform(click())
        onData(
            allOf(
                Is(instanceOf(String::class.java)),
                containsString("Botan")
            )
        ).perform(click())
        for (naziv in listaVidljivihNaziva)
            onView(withId(R.id.biljkeRV)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    allOf(
                        hasDescendant(withText(naziv)),
                        hasDescendant(withId(R.id.nazivItem))
                    )
                )
            )
        for (naziv in listaNevidljivihNaziva) {
            try {
                onView(withId(R.id.biljkeRV)).perform(
                    RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                        allOf(
                            hasDescendant(withText(naziv)),
                            hasDescendant(withId(R.id.nazivItem))
                        )
                    )
                )
                assert(
                    false,
                    { "Scroll treba pasti sljedeÄ‡a biljka se prikazuje nakon filtriranja a ne treba. Biljka:  " + naziv })
            } catch (e: Exception) {
                assertThat(e.message, containsString("Error performing"))
            }
        }
    }


    @Test
    fun onFilterInMedicinskiModBosiljakWithModChange() {
        onView(withId(R.id.modSpinner)).perform(click())
        onData(
            allOf(
                Is(instanceOf(String::class.java)),
                containsString("Medicin")
            )
        ).perform(click())
        onView(withId(R.id.biljkeRV)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                allOf(
                    hasDescendant(withText("Bosiljak (Ocimum basilicum)")),
                    hasDescendant(withText(containsString("Smirenje")))
                ), click()
            )
        )
        val listaVidljivihNaziva = listOf(
            "Bosiljak (Ocimum basilicum)",
            "Kamilica (Matricaria chamomilla)",
            "Lavanda (Lavandula angustifolia)"
        )
        val listaNevidljivihNaziva =
            listOf("Nana (Mentha spicata)", "RuÅ¾marin (Rosmarinus officinalis)")
        for (naziv in listaVidljivihNaziva)
            onView(withId(R.id.biljkeRV)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    allOf(
                        hasDescendant(withText(naziv)),
                        hasDescendant(withId(R.id.nazivItem))
                    )
                )
            )
        for (naziv in listaNevidljivihNaziva) {
            try {
                onView(withId(R.id.biljkeRV)).perform(
                    RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                        allOf(
                            hasDescendant(withText(naziv)),
                            hasDescendant(withId(R.id.nazivItem))
                        )
                    )
                )
                assert(
                    false,
                    { "Scroll treba pasti sljedeÄ‡a biljka se prikazuje nakon filtriranja a ne treba. Biljka:  " + naziv })
            } catch (e: Exception) {
                assertThat(e.message, containsString("Error performing"))
            }
        }

        onView(withId(R.id.modSpinner)).perform(click())
        onData(
            allOf(
                Is(instanceOf(String::class.java)),
                containsString("Kuha")
            )
        ).perform(click())
        for (naziv in listaVidljivihNaziva)
            onView(withId(R.id.biljkeRV)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    allOf(
                        hasDescendant(withText(naziv)),
                        hasDescendant(withId(R.id.nazivItem))
                    )
                )
            )
        for (naziv in listaNevidljivihNaziva) {
            try {
                onView(withId(R.id.biljkeRV)).perform(
                    RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                        allOf(
                            hasDescendant(withText(naziv)),
                            hasDescendant(withId(R.id.nazivItem))
                        )
                    )
                )
                assert(
                    false,
                    { "Scroll treba pasti sljedeÄ‡a biljka se prikazuje nakon filtriranja a ne treba. Biljka:  " + naziv })
            } catch (e: Exception) {
                assertThat(e.message, containsString("Error performing"))
            }
        }
        onView(withId(R.id.modSpinner)).perform(click())
        onData(
            allOf(
                Is(instanceOf(String::class.java)),
                containsString("Botan")
            )
        ).perform(click())
        for (naziv in listaVidljivihNaziva)
            onView(withId(R.id.biljkeRV)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    allOf(
                        hasDescendant(withText(naziv)),
                        hasDescendant(withId(R.id.nazivItem))
                    )
                )
            )
        for (naziv in listaNevidljivihNaziva) {
            try {
                onView(withId(R.id.biljkeRV)).perform(
                    RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                        allOf(
                            hasDescendant(withText(naziv)),
                            hasDescendant(withId(R.id.nazivItem))
                        )
                    )
                )
                assert(
                    false,
                    { "Scroll treba pasti sljedeÄ‡a biljka se prikazuje nakon filtriranja a ne treba. Biljka:  " + naziv })
            } catch (e: Exception) {
                assertThat(e.message, containsString("Error performing"))
            }
        }
    }


    @Test
    fun onFilterInMedicinskiModBosiljakWithModChangeAndReset() {
        onView(withId(R.id.modSpinner)).perform(click())
        onData(
            allOf(
                Is(instanceOf(String::class.java)),
                containsString("Medicin")
            )
        ).perform(click())
        onView(withId(R.id.biljkeRV)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                allOf(
                    hasDescendant(withText("Bosiljak (Ocimum basilicum)")),
                    hasDescendant(withText(containsString("Smirenje")))
                ), click()
            )
        )
        val listaVidljivihNaziva = listOf(
            "Bosiljak (Ocimum basilicum)",
            "Kamilica (Matricaria chamomilla)",
            "Lavanda (Lavandula angustifolia)"
        )
        val listaNevidljivihNaziva =
            listOf("Nana (Mentha spicata)", "RuÅ¾marin (Rosmarinus officinalis)")
        for (naziv in listaVidljivihNaziva)
            onView(withId(R.id.biljkeRV)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    allOf(
                        hasDescendant(withText(naziv)),
                        hasDescendant(withId(R.id.nazivItem))
                    )
                )
            )
        for (naziv in listaNevidljivihNaziva) {
            try {
                onView(withId(R.id.biljkeRV)).perform(
                    RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                        allOf(
                            hasDescendant(withText(naziv)),
                            hasDescendant(withId(R.id.nazivItem))
                        )
                    )
                )
                assert(
                    false,
                    { "Scroll treba pasti sljedeÄ‡a biljka se prikazuje nakon filtriranja a ne treba. Biljka:  " + naziv })
            } catch (e: Exception) {
                assertThat(e.message, containsString("Error performing"))
            }
        }
        onView(withId(R.id.modSpinner)).perform(click())
        onData(
            allOf(
                Is(instanceOf(String::class.java)),
                containsString("Bota")
            )
        ).perform(click())
        for (naziv in listaVidljivihNaziva)
            onView(withId(R.id.biljkeRV)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    allOf(
                        hasDescendant(withText(naziv)),
                        hasDescendant(withId(R.id.nazivItem))
                    )
                )
            )
        for (naziv in listaNevidljivihNaziva) {
            try {
                onView(withId(R.id.biljkeRV)).perform(
                    RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                        allOf(
                            hasDescendant(withText(naziv)),
                            hasDescendant(withId(R.id.nazivItem))
                        )
                    )
                )
                assert(
                    false,
                    { "Scroll treba pasti sljedeÄ‡a biljka se prikazuje nakon filtriranja a ne treba. Biljka:  " + naziv })
            } catch (e: Exception) {
                assertThat(e.message, containsString("Error performing"))
            }
        }
        onView(withId(R.id.resetBtn)).perform(click())
        val listSvihNaziva = listOf(
            "Bosiljak (Ocimum basilicum)",
            "Kamilica (Matricaria chamomilla)",
            "Lavanda (Lavandula angustifolia)",
            "Nana (Mentha spicata)",
            "RuÅ¾marin (Rosmarinus officinalis)"
        )
        for (naziv in listSvihNaziva)
            onView(withId(R.id.biljkeRV)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    allOf(
                        hasDescendant(withText(naziv)),
                        hasDescendant(withId(R.id.nazivItem))
                    )
                )
            )
    }

}