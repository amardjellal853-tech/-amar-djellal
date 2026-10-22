package com.example.data.model

data class TermAssessment(
    val studentId: String,
    val termType: TermType = TermType.TERM_1,

    // 1. الرياضة الفردية:
    // قياس النتيجة في التقويم التشخيصي (بالثواني للجري 60م، أو بالأمتار للوثب/الجلة)
    val diagnosticMeasurement: Double? = null,
    // قياس النتيجة في التقويم التحصيلي (بالثواني للجري 60م، أو بالأمتار للوثب/الجلة)
    val summativeMeasurement: Double? = null,
    // علامة التقويم التشخيصي على 14 (تبدأ من 0.0 للتلميذ الجديد دون ملء تلقائي)
    val individualDiagnosticScore14: Double = 0.0,
    // علامة التطور الحاصل على 6 من 20 بعد التحصيلي
    val individualProgressionScore6: Double = 0.0,
    // حقل التوافق مع النسخ السابقة
    val individualMeasurement: Double? = null,

    // 2. الرياضة الجماعية: وفق المعايير الخمسة (من 4 نقاط لكل معيار = 20)
    // تبدأ من 0.0 لتلاميذ الأقسام الجديدة حتى يملأها الأستاذ بنفسه
    val c1Score: Double = 0.0, // التمرير والاستقبال (أو الإرسال للطائرة)
    val c2Score: Double = 0.0, // التنطيط والمراوغة (أو الاستقبال للطائرة)
    val c3Score: Double = 0.0, // العمل الجماعي والإبداع (أو التمرير للطائرة)
    val c4Score: Double = 0.0, // بناء دفاع وهجوم (أو بناء هجمة للطائرة)
    val c5Score: Double = 0.0, // تهديف (أو تهديف للطائرة)

    // علامة الرياضة الجماعية الإجمالية (تحسب من مجموع المعايير أو علامة مباشرة)
    val collectiveSportScore20: Double = 0.0,

    // حقول التوافق مع النسخ السابقة
    val c1MotorScore: Double = c1Score,
    val c2EfficiencyScore: Double = c2Score,
    val c3KnowledgeScore: Double = c3Score,
    val c4BehaviorScore: Double = c4Score,

    // 3. علامة الفرض (تُملأ يدوياً من 20، تبدأ من 0.0)
    val manualTestScore: Double = 0.0,

    // 4. معايير التقويم المستمر الستة (المجموع 20):
    // 1) الغيابات والمواظبة (من 4.0)
    val absenceScore: Double = 4.0,
    // 2) اللباس الرياضي (من 3.0)
    val sportswearScore: Double = 3.0,
    // 3) السلوك والانضباط (من 4.0)
    val behaviorScore: Double = 4.0,
    // 4) إنجاز المهام الفردية (من 3.0)
    val individualTaskScore: Double = 3.0,
    // 5) العمل ضمن الفريق (من 3.0)
    val teamworkScore: Double = 3.0,
    // 6) الإبداع (من 3.0)
    val creativityScore: Double = 3.0,
    // حقل للتوافق مع الإصدارات السابقة
    val teamPlayContributionScore: Double = 0.0
) {
    // علامة النشاط الفردي الإجمالية = تشخيص (14) + تطور حاصل (6) = 20
    val individualSportTotal20: Double
        get() = String.format(
            java.util.Locale.US,
            "%.2f",
            (individualDiagnosticScore14 + individualProgressionScore6).coerceIn(0.0, 20.0)
        ).toDouble()

    // مجموع المعايير الخمسة للرياضة الجماعية من 20
    val criteriaSum20: Double
        get() = String.format(
            java.util.Locale.US,
            "%.2f",
            (c1Score + c2Score + c3Score + c4Score + c5Score).coerceIn(0.0, 20.0)
        ).toDouble()

    // علامة الرياضة الجماعية الفعالة (إذا وضعت المعايير تستخدم، أو القيمة المباشرة إن كانت أكبر)
    val effectiveCollectiveScore: Double
        get() = if (criteriaSum20 > 0.0) criteriaSum20 else collectiveSportScore20

    // علامة اختبار الفصل = (النشاط الفردي + النشاط الجماعي) / 2
    val examScore20: Double
        get() = String.format(
            java.util.Locale.US,
            "%.2f",
            ((individualSportTotal20 + effectiveCollectiveScore) / 2.0).coerceIn(0.0, 20.0)
        ).toDouble()

    // مجموع علامة التقويم المستمر من 20 (وفق المعايير الـ 6: غياب 4 + لباس 3 + سلوك 4 + مهام 3 + فريق 3 + إبداع 3 = 20)
    val continuousAssessmentTotal20: Double
        get() = String.format(
            java.util.Locale.US,
            "%.2f",
            (absenceScore + sportswearScore + behaviorScore + individualTaskScore + teamworkScore + creativityScore).coerceIn(0.0, 20.0)
        ).toDouble()

    // معدل الفصل الرسمي = (التقويم المستمر + الفرض + الاختبار * 2) / 4
    val termAverage: Double
        get() {
            val avg = (continuousAssessmentTotal20 + manualTestScore + (examScore20 * 2.0)) / 4.0
            return String.format(java.util.Locale.US, "%.2f", avg.coerceIn(0.0, 20.0)).toDouble()
        }

    val masteryLevel: MasteryLevel
        get() = when {
            effectiveCollectiveScore >= 16.0 -> MasteryLevel.MAXIMAL
            effectiveCollectiveScore >= 12.0 -> MasteryLevel.ACCEPTABLE
            effectiveCollectiveScore >= 8.0 -> MasteryLevel.PARTIAL
            else -> MasteryLevel.LIMITED
        }

    val appreciationAr: String
        get() = when {
            termAverage == 0.0 -> "غير مقيم بعد"
            termAverage >= 16.0 -> "ممتاز (تهنئة)"
            termAverage >= 14.0 -> "جيد جداً (تشجيع)"
            termAverage >= 12.0 -> "جيد (لوحة شرف)"
            termAverage >= 10.0 -> "مقبول"
            termAverage >= 8.0 -> "دون المتوسط (استدراك)"
            else -> "ضعيف (إنذار)"
        }

    // دالة إنشاء نسخة مصفّرة بالكامل للتلميذ
    fun createZeroed(): TermAssessment {
        return TermAssessment(
            studentId = studentId,
            termType = termType,
            diagnosticMeasurement = null,
            summativeMeasurement = null,
            individualDiagnosticScore14 = 0.0,
            individualProgressionScore6 = 0.0,
            individualMeasurement = null,
            c1Score = 0.0,
            c2Score = 0.0,
            c3Score = 0.0,
            c4Score = 0.0,
            c5Score = 0.0,
            collectiveSportScore20 = 0.0,
            manualTestScore = 0.0,
            absenceScore = 0.0,
            sportswearScore = 0.0,
            behaviorScore = 0.0,
            individualTaskScore = 0.0,
            teamworkScore = 0.0,
            creativityScore = 0.0,
            teamPlayContributionScore = 0.0
        )
    }
}
