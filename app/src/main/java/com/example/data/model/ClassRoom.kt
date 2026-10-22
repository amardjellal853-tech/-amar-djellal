package com.example.data.model

data class ClassRoom(
    val id: String,
    val name: String,
    val level: String,
    val academicYear: String = "2026/2027",
    val cycle: String = "الطور الثانوي",
    val term1IndividualSport: String = "الجري السريع 60م",
    val term1CollectiveSport: String = "كرة اليد",
    val term2IndividualSport: String = "الوثب الطويل",
    val term2CollectiveSport: String = "كرة السلة",
    val term3IndividualSport: String = "دفع الجلة",
    val term3CollectiveSport: String = "كرة الطائرة"
) {
    fun getIndividualSport(term: TermType): String = when (term) {
        TermType.TERM_1 -> term1IndividualSport
        TermType.TERM_2 -> term2IndividualSport
        TermType.TERM_3 -> term3IndividualSport
    }

    fun getCollectiveSport(term: TermType): String = when (term) {
        TermType.TERM_1 -> term1CollectiveSport
        TermType.TERM_2 -> term2CollectiveSport
        TermType.TERM_3 -> term3CollectiveSport
    }
}
