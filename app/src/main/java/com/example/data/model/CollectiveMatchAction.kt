package com.example.data.model

data class CollectiveMatchAction(
    val studentId: String,
    val termType: TermType = TermType.TERM_1,
    val team: String = "الفريق أ",
    
    // الإجراءات الإيجابية للمهارات الخمس (كرة اليد والسلة: تمرير/استقبال، تنطيط/مراوغة، عمل جماعي، دفاع/هجوم، تهديف)
    // (كرة الطائرة: إرسال، استقبال، تمرير، بناء هجمة، تهديف/سحق)
    val positiveSkill1: Int = 0, // تمرير واستقبال (أو إرسال للطائرة)
    val positiveSkill2: Int = 0, // تنطيط ومراوغة (أو استقبال للطائرة)
    val positiveSkill3: Int = 0, // عمل جماعي وإبداع (أو تمرير للطائرة)
    val positiveSkill4: Int = 0, // بناء دفاع وهجوم (أو بناء هجمة للطائرة)
    val positiveSkill5: Int = 0, // تهديف (أو تهديف/سحق للطائرة)

    // الإجراءات السلبية للمهارات الخمس
    val negativeSkill1: Int = 0,
    val negativeSkill2: Int = 0,
    val negativeSkill3: Int = 0,
    val negativeSkill4: Int = 0,
    val negativeSkill5: Int = 0,

    // حقول التوافق مع النسخ السابقة
    val positivePasses: Int = positiveSkill1,
    val positiveShots: Int = positiveSkill5,
    val positiveInterceptions: Int = positiveSkill4,
    val positiveSupport: Int = positiveSkill3,
    val negativePasses: Int = negativeSkill1,
    val negativeShots: Int = negativeSkill5,
    val negativeTechnicalFouls: Int = negativeSkill4,
    val negativePersonalFouls: Int = negativeSkill2
) {
    val sportNameAr: String
        get() = termType.collectiveSportAr

    val totalPositive: Int
        get() = positiveSkill1 + positiveSkill2 + positiveSkill3 + positiveSkill4 + positiveSkill5

    val totalNegative: Int
        get() = negativeSkill1 + negativeSkill2 + negativeSkill3 + negativeSkill4 + negativeSkill5

    val totalActions: Int
        get() = totalPositive + totalNegative

    val successRate: Double
        get() = if (totalActions > 0) {
            String.format(java.util.Locale.US, "%.1f", (totalPositive.toDouble() / totalActions) * 100).toDouble()
        } else {
            0.0
        }

    val netScore: Int
        get() = totalPositive - totalNegative

    val effectivenessLevel: String
        get() = when {
            successRate >= 75.0 && totalActions >= 4 -> "فعالية عالية (+)"
            successRate >= 55.0 && totalActions >= 3 -> "فعالية مقبولة (+)"
            successRate >= 40.0 -> "فعالية متوسطة"
            totalActions == 0 -> "لم يشارك بعد"
            else -> "يحتاج دعم وتوجيه (-)"
        }

    fun createZeroed(): CollectiveMatchAction {
        return CollectiveMatchAction(
            studentId = studentId,
            termType = termType,
            team = team
        )
    }
}
