package com.example.data.model

data class DiagnosticEvaluation(
    val studentId: String,
    // القياسات الميدانية
    val sprint60mSeconds: Double? = null,
    val enduranceMeters: Int? = null,
    val longJumpMeters: Double? = null,
    val shotPutMeters: Double? = null,
    val gymnasticsScore: Double? = null,
    val collectiveSportScore: Double? = null,
    val teacherObservation: String = "",

    // النظام الرسمي المطلوب:
    // 1. علامة التقويم التشخيصي للرياضة الفردية (على 14 من 20)
    val diagnosticScore14: Double = 9.5,

    // 2. التقويم التحصيلي: علامة التطور الحاصل بعد التعلم (على 6 من 20)
    val progressionScore6: Double = 4.0,

    // ملاحظة التطور والتحصيل
    val summativeObservation: String = ""
) {
    // علامة النشاط الفردي الإجمالية = التشخيص (14) + التطور التحصيلي (6) = 20 من 20
    val individualSportTotal20: Double
        get() = String.format(
            java.util.Locale.US,
            "%.2f",
            (diagnosticScore14 + progressionScore6).coerceIn(0.0, 20.0)
        ).toDouble()

    // النسبة المئوية للنشاط الفردي
    val individualPercentage: Double
        get() = String.format(java.util.Locale.US, "%.1f", (individualSportTotal20 / 20.0) * 100.0).toDouble()

    // المستوى التشخيصي الإجمالي (من 20)
    val overallDiagnosticScore: Double
        get() = String.format(
            java.util.Locale.US,
            "%.1f",
            (diagnosticScore14 / 14.0 * 20.0).coerceIn(0.0, 20.0)
        ).toDouble()

    val masteryLevelAr: String
        get() = when {
            overallDiagnosticScore >= 16.0 -> "تحكم أقصى"
            overallDiagnosticScore >= 12.0 -> "تحكم مقبول"
            overallDiagnosticScore >= 9.0 -> "تحكم جزئي"
            else -> "تحكم محدود"
        }
}
