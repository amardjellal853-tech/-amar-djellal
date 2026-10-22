package com.example.data.model

data class SportScaleConfig(
    // 1. مقياس الجري السريع 60م (الفصل الأول)
    val sprintBaseTimeSec: Double = 5.0,           // 5 ثوانٍ تعني 14 نقطة
    val sprintSecPerMinusPoint: Double = 1.0,       // كل ما زادت 1 ثانية نقصت نقطة واحدة
    val sprintSecPerProgressionPoint: Double = 0.5, // كل 0.5 ثانية تطور تقابلها 1 نقطة (3 ثوانٍ تطور = 6 نقاط كاملة)

    // 2. مقياس الوثب الطويل (الفصل الثاني)
    val jumpBaseDistMeters: Double = 4.50,         // 4.50 م تعني 14 نقطة
    val jumpDistPerMinusPoint: Double = 0.25,      // كل 0.25 م نقص ينقص نقطة
    val jumpDistPerProgressionPoint: Double = 0.125, // كل 0.125 م زيادة تقابلها 1 نقطة (0.75 م = 6 نقاط)

    // 3. مقياس دفع الجلة (الفصل الثالث)
    val shotPutBaseDistMeters: Double = 8.00,       // 8.00 م تعني 14 نقطة
    val shotPutDistPerMinusPoint: Double = 0.50,    // كل 0.50 م نقص ينقص نقطة
    val shotPutDistPerProgressionPoint: Double = 0.25 // كل 0.25 م زيادة تقابلها 1 نقطة (1.50 م = 6 نقاط)
)
