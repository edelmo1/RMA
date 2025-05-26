package com.example.spirala_1

fun getBiljke() : List<Biljka> {

 return listOf(
     Biljka(
         id = 0,
         naziv = "Bosiljak (Ocimum basilicum)",
         porodica = "Lamiaceae (usnate)",
         medicinskoUpozorenje = "Može iritati kožu osjetljivu na sunce. Preporučuje se oprezna upotreba pri korištenju ulja bosiljka.",
         medicinskeKoristi = listOf(MedicinskaKorist.SMIRENJE, MedicinskaKorist.REGULACIJAPROBAVE),
         profilOkusa = ProfilOkusaBiljke.BEZUKUSNO,
         jela = listOf("Salata od paradajza", "Punjene tikvice"),
         klimatskiTipovi = listOf(KlimatskiTip.SREDOZEMNA, KlimatskiTip.SUBTROPSKA),
         zemljisniTipovi = listOf(Zemljiste.PJESKOVITO, Zemljiste.ILOVACA),

     ),
     Biljka(
         id = 1,
         naziv = "Nana (Mentha spicata)",
         porodica = "Lamiaceae (metvice)",
         medicinskoUpozorenje = "Nije preporučljivo za trudnice, dojilje i djecu mlađu od 3 godine.",
         medicinskeKoristi = listOf(MedicinskaKorist.PROTUUPALNO, MedicinskaKorist.PROTIVBOLOVA),
         profilOkusa = ProfilOkusaBiljke.MENTA,
         jela = listOf("Jogurt sa voćem", "Gulaš"),
         klimatskiTipovi = listOf(KlimatskiTip.SREDOZEMNA, KlimatskiTip.UMJERENA),
         zemljisniTipovi = listOf(Zemljiste.GLINENO, Zemljiste.CRNICA),

     ),
     Biljka(
         id = 2,
         naziv = "Kamilica (Matricaria chamomilla)",
         porodica = "Asteraceae (glavočike)",
         medicinskoUpozorenje = "Može uzrokovati alergijske reakcije kod osjetljivih osoba.",
         medicinskeKoristi = listOf(MedicinskaKorist.SMIRENJE, MedicinskaKorist.PROTUUPALNO),
         profilOkusa = ProfilOkusaBiljke.AROMATICNO,
         jela = listOf("Čaj od kamilice"),
         klimatskiTipovi = listOf(KlimatskiTip.UMJERENA, KlimatskiTip.SUBTROPSKA),
         zemljisniTipovi = listOf(Zemljiste.PJESKOVITO, Zemljiste.KRECNJACKO),

     ),
     Biljka(
         id = 3,
         naziv = "Ružmarin (Rosmarinus officinalis)",
         porodica = "Lamiaceae (metvice)",
         medicinskoUpozorenje = "Treba ga koristiti umjereno i konsultovati se sa ljekarom pri dugotrajnoj upotrebi ili upotrebi u većim količinama.",
         medicinskeKoristi = listOf(MedicinskaKorist.PROTUUPALNO, MedicinskaKorist.REGULACIJAPRITISKA),
         profilOkusa = ProfilOkusaBiljke.AROMATICNO,
         jela = listOf("Pečeno pile","Grah","Gulaš"),
         klimatskiTipovi = listOf(KlimatskiTip.SREDOZEMNA, KlimatskiTip.SUHA),
         zemljisniTipovi = listOf(Zemljiste.SLJUNOVITO, Zemljiste.KRECNJACKO),

     ),
     Biljka(
         id = 4,
         naziv = "Lavanda (Lavandula angustifolia)",
         porodica = "Lamiaceae (metvice)",
         medicinskoUpozorenje = "Nije preporučljivo za trudnice, dojilje i djecu mlađu od 3 godine. Također, treba izbjegavati kontakt lavanda ulja sa očima.",
         medicinskeKoristi = listOf(MedicinskaKorist.SMIRENJE, MedicinskaKorist.PODRSKAIMUNITETU),
         profilOkusa = ProfilOkusaBiljke.AROMATICNO,
         jela = listOf("Jogurt sa voćem"),
         klimatskiTipovi = listOf(KlimatskiTip.SREDOZEMNA, KlimatskiTip.SUHA),
         zemljisniTipovi = listOf(Zemljiste.PJESKOVITO, Zemljiste.KRECNJACKO),

     )
     ,

     Biljka(
         id = 5,
         naziv = "Kadulja (Salvia officinalis)",
         porodica = "Lamiaceae (metvice)",
         medicinskoUpozorenje = """Uslijed dugotrajnog i neprestanog korištenja kadulje, moguće je da dođe do uznemirenosti, povraćanja, vrtoglavice, drhtavice, epileptičkih napada, prijevremene kontrakcije tokom trudnoće. Osobe s epilepsijom trebaju izbjegavati kadulju.""",
         medicinskeKoristi = listOf(MedicinskaKorist.PODRSKAIMUNITETU,MedicinskaKorist.REGULACIJAPRITISKA),
         profilOkusa = ProfilOkusaBiljke.MENTA,
         jela = listOf("Topivi namazi od kadulje","Med","Čaj"),
         klimatskiTipovi = listOf(KlimatskiTip.SREDOZEMNA, KlimatskiTip.SUHA,KlimatskiTip.SUBTROPSKA),
         zemljisniTipovi = listOf(Zemljiste.GLINENO, Zemljiste.CRNICA,Zemljiste.ILOVACA),

     ),

     Biljka(
         id = 6,
         naziv = "Kantarion (Hypericum perforatum)",
         porodica = "Hypericaceae (goračevke)",
         medicinskoUpozorenje = """Zabilježeno je da može izazvati nuspojave koje uključuju umor, zatvor, mučninu, povraćanje, suha usta, glavobolju, vrtoglavicu.""",
         medicinskeKoristi = listOf(MedicinskaKorist.PROTUUPALNO,MedicinskaKorist.REGULACIJAPRITISKA),
         profilOkusa = ProfilOkusaBiljke.GORKO,
         jela = listOf("Ulje","Čaj"),
         klimatskiTipovi = listOf(KlimatskiTip.SREDOZEMNA,KlimatskiTip.SUHA,KlimatskiTip.UMJERENA),
         zemljisniTipovi = listOf(Zemljiste.KRECNJACKO,Zemljiste.ILOVACA,Zemljiste.PJESKOVITO),

     ),

     Biljka(
         id = 7,
         naziv = "Kim (Carum)",
         porodica = "Apiaceae (Štitarke)",
         medicinskoUpozorenje = "Uglavnom bez nuspojava, uz eventualno upozorenje za moguće grčeve u stomaku",
         medicinskeKoristi = listOf(MedicinskaKorist.PROTIVBOLOVA,MedicinskaKorist.PROTUUPALNO,MedicinskaKorist.REGULACIJAPROBAVE),
         profilOkusa = ProfilOkusaBiljke.GORKO,
         jela = listOf("Pite","Keksi","Peciva"),
         klimatskiTipovi = listOf(KlimatskiTip.SUHA,KlimatskiTip.UMJERENA,KlimatskiTip.SUBTROPSKA),
         zemljisniTipovi = listOf(Zemljiste.ILOVACA,Zemljiste.CRNICA,Zemljiste.GLINENO),

     ),

     Biljka(
         id = 8,
         naziv = "Lovor (Laurus nobilis)",
         porodica = "Lauraceae (Lovorovke)",
         medicinskoUpozorenje = """Čaj od lovora je relativno siguran, no, potreban je oprez kod osoba koje su sklone alergijama. U prevelikim dozama može izazvati pospanost,glavobolju, mučninu.""",
         medicinskeKoristi = listOf(MedicinskaKorist.PROTIVBOLOVA,MedicinskaKorist.REGULACIJAPRITISKA,),
         profilOkusa = ProfilOkusaBiljke.KORIJENASTO,
         jela = listOf("Čaj","Riba","Crveno meso"),
         klimatskiTipovi = listOf(KlimatskiTip.SREDOZEMNA,KlimatskiTip.SUHA,KlimatskiTip.SUBTROPSKA),
         zemljisniTipovi = listOf(Zemljiste.KRECNJACKO,Zemljiste.ILOVACA,Zemljiste.SLJUNOVITO),

     ),

     Biljka(
         id = 9,
         naziv = "Smilje (Helichrysum)",
         porodica = "Asteraceae (Glavočike)",
         medicinskoUpozorenje = "Uglavnom bez nuspojava. Nije preporučljivo za trudnice i malu djecu.",
         medicinskeKoristi = listOf(MedicinskaKorist.PODRSKAIMUNITETU,MedicinskaKorist.SMIRENJE),
         profilOkusa = ProfilOkusaBiljke.AROMATICNO,
         jela = listOf("Čaj","Ulje"),
         klimatskiTipovi = listOf(KlimatskiTip.SUHA,KlimatskiTip.SREDOZEMNA,KlimatskiTip.UMJERENA),
         zemljisniTipovi = listOf(Zemljiste.KRECNJACKO,Zemljiste.ILOVACA,Zemljiste.SLJUNOVITO),

     )
 )
}