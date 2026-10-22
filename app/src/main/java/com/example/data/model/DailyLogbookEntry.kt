package com.example.data.model

data class DailyLogbookEntry(
    val id: String,
    val date: String, // YYYY-MM-DD
    val dayOfWeek: String = "الأحد",
    val timeSlot: String = "08:00 - 10:00",
    val classId: String,
    val className: String,
    val cycle: String = "الطور الثانوي",
    val activityType: String = "كرة اليد",
    val domain: String = "الميدان الجماعي",
    val sessionNumber: String = "الحصة 2 (تعلمية)",
    val objective: String = "التحكم في التمرير والاستقبال أثناء التنقل السريع نحو مرمى الخصم",
    val warmUpPhase: String = "تسخين عام (جري متدرج + حركات مفصلية) + تسخين خاص بالكرة (15 د)",
    val mainPhase: String = "وضعيات لعب 3 ضد 2: التمرير والاستقبال السريع مع التنطيط والمراوغة وبناء هجمة (65 د)",
    val coolDownPhase: String = "تمارين تهدئة واسترجاع + تقييم نقدي لمجريات الحصة وجمع العتاد (10 د)",
    val absentCount: Int = 0,
    val absentNames: String = "",
    val equipment: String = "كرات يد، أقماع، صدريات تمييز الفرق",
    val observations: String = "تجاوب حركي إيجابي وتحقيق أهداف الحصة المبرمجة"
)
