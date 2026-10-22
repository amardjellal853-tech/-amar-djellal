package com.example.data.model

data class HandballAction(
    val studentId: String,
    val team: String = "الفريق أ", // "الفريق أ" أو "الفريق ب"
    val positivePasses: Int = 0,
    val positiveShots: Int = 0,
    val positiveInterceptions: Int = 0,
    val positiveSupport: Int = 0,
    val negativePasses: Int = 0,
    val negativeShots: Int = 0,
    val negativeTechnicalFouls: Int = 0,
    val negativePersonalFouls: Int = 0
) {
    val totalPositive: Int
        get() = positivePasses + positiveShots + positiveInterceptions + positiveSupport

    val totalNegative: Int
        get() = negativePasses + negativeShots + negativeTechnicalFouls + negativePersonalFouls

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
            successRate >= 75.0 && totalActions >= 4 -> "فعالية عالية جدًا (+)"
            successRate >= 55.0 && totalActions >= 3 -> "فعالية مقبولة (+)"
            successRate >= 40.0 -> "فعالية متوسطة"
            totalActions == 0 -> "لم يشارك بعد"
            else -> "يحتاج دعم وتوجيه (-)"
        }
}
