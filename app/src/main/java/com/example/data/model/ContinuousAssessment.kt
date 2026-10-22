package com.example.data.model

data class ContinuousAssessment(
    val studentId: String,

    // 1. علامة الفرض (تُملأ يدوياً من 20)
    val manualTestScore: Double = 14.0,

    // 2. معايير التقويم المستمر الستة (المجموع 20):
    val absenceScore: Double = 4.0,              // معيار الغيابات والمواظبة (من 4)
    val sportswearScore: Double = 3.0,           // معيار اللباس الرياضي (من 3)
    val behaviorScore: Double = 4.0,             // معيار السلوك والانضباط (من 4)
    val individualTaskScore: Double = 3.0,       // معيار إنجاز المهام الفردية (من 3)
    val teamworkScore: Double = 3.0,             // معيار العمل ضمن الفريق (من 3)
    val creativityScore: Double = 3.0,           // معيار الإبداع (من 3)
    val teamPlayContributionScore: Double = 3.0, // للتوافق مع الإصدارات السابقة

    // 3. علامات الأنشطة للاختبار الأول (يمكن جلبها مباشرة من النشاط الفردي والجماعي):
    val individualSportScore: Double = 13.5,     // علامة النشاط الفردي (تشخيص 14 + تطور 6 = من 20)
    val collectiveSportScore: Double = 14.0      // علامة النشاط الجماعي (وفق المعايير والمستويات الـ 4 = من 20)
) {
    // علامة التقويم المستمر الإجمالية (مجموع المعايير الستة من 20)
    val continuousAssessmentTotal20: Double
        get() = String.format(
            java.util.Locale.US,
            "%.2f",
            (absenceScore + sportswearScore + behaviorScore + individualTaskScore + teamworkScore + creativityScore).coerceIn(0.0, 20.0)
        ).toDouble()

    // علامة الاختبار الأول = (علامة النشاط الفردي + علامة النشاط الجماعي) / 2
    val firstTermExamScore20: Double
        get() = String.format(
            java.util.Locale.US,
            "%.2f",
            ((individualSportScore + collectiveSportScore) / 2.0).coerceIn(0.0, 20.0)
        ).toDouble()

    // المعدل الفصلي الرسمي (التقويم المستمر + الفرض + الاختبار * 2) / 4
    val termAverage: Double
        get() {
            val avg = (continuousAssessmentTotal20 + manualTestScore + (firstTermExamScore20 * 2)) / 4.0
            return String.format(java.util.Locale.US, "%.2f", avg.coerceIn(0.0, 20.0)).toDouble()
        }

    // المعدل البسيط البديل (التقويم المستمر + الفرض + الاختبار) / 3
    val simpleAverage: Double
        get() {
            val avg = (continuousAssessmentTotal20 + manualTestScore + firstTermExamScore20) / 3.0
            return String.format(java.util.Locale.US, "%.2f", avg.coerceIn(0.0, 20.0)).toDouble()
        }

    val appreciationAr: String
        get() = when {
            termAverage >= 16.0 -> "ممتاز (تهنئة)"
            termAverage >= 14.0 -> "جيد جداً (تشجيع)"
            termAverage >= 12.0 -> "جيد (لوحة شرف)"
            termAverage >= 10.0 -> "مقبول"
            termAverage >= 8.0 -> "دون المتوسط (يحتاج استدراك)"
            else -> "ضعيف (إنذار)"
        }
}
