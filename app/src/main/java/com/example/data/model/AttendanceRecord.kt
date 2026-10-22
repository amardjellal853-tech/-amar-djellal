package com.example.data.model

enum class AttendanceStatus(val labelAr: String, val shortSymbol: String) {
    PRESENT("حاضر", "ح"),
    ABSENT_UNEXCUSED("غائب غير مبرر", "غ"),
    ABSENT_EXCUSED("غائب بمبرر", "م"),
    MEDICALLY_EXEMPT("معفى طبياً", "ع")
}

data class AttendanceRecord(
    val id: String,
    val studentId: String,
    val date: String,
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    // 1. اللباس الرياضي: مرتدي للبذلة الرياضية الكاملة
    val hasSportswear: Boolean = true,
    // 2. السلوك والانضباط: سلوك حسن ومنضبط
    val goodBehavior: Boolean = true,
    // 3. إنجاز المهام الفردية: أنجز المهمة والتمارين الموكلة له
    val individualTaskCompleted: Boolean = true,
    // 4. العمل ضمن الفريق: مشاركة فعالة وروح تعاونية
    val teamworkContribution: Boolean = true,
    // 5. الإبداع: مبادرة وحلول إبداعية متميزة
    val creativityDemonstrated: Boolean = false,
    val remarks: String = ""
)
