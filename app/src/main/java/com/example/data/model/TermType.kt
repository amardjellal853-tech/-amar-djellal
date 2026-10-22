package com.example.data.model

enum class TermType(
    val termNumber: Int,
    val labelAr: String,
    val individualSportAr: String,
    val collectiveSportAr: String,
    val individualUnit: String
) {
    TERM_1(
        termNumber = 1,
        labelAr = "الفصل الأول",
        individualSportAr = "الجري السريع 60م",
        collectiveSportAr = "كرة اليد",
        individualUnit = "ثانية"
    ),
    TERM_2(
        termNumber = 2,
        labelAr = "الفصل الثاني",
        individualSportAr = "الوثب الطويل",
        collectiveSportAr = "كرة السلة",
        individualUnit = "متر"
    ),
    TERM_3(
        termNumber = 3,
        labelAr = "الفصل الثالث",
        individualSportAr = "دفع الجلة",
        collectiveSportAr = "كرة الطائرة",
        individualUnit = "متر"
    )
}
