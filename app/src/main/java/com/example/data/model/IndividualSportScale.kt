package com.example.data.model

import java.util.Locale
import kotlin.math.abs

object IndividualSportScale {

    /**
     * حساب متوسط القياس بين التقويم التشخيصي والتقويم التحصيلي
     * وفق قاعدة الأستاذ:
     * جمع زمن التقويم التشخيصي مع زمن التحصيلي وقسمتهما على اثنين
     */
    fun calculateAverageMeasurement(
        diagMeasurement: Double?,
        summativeMeasurement: Double?
    ): Double? {
        if (diagMeasurement != null && diagMeasurement > 0.0 && summativeMeasurement != null && summativeMeasurement > 0.0) {
            val avg = (diagMeasurement + summativeMeasurement) / 2.0
            return String.format(Locale.US, "%.2f", avg).toDouble()
        }
        return summativeMeasurement ?: diagMeasurement
    }

    /**
     * حساب نقطة الاختبار / الإنجاز على 14 من 20:
     * بجمع زمن التقويم التشخيصي مع زمن التحصيلي وقسمتهما على اثنين
     * والنتيجة نحسب من خلالها النقطة على 14 من 20
     */
    fun calculateCombinedScore14(
        termType: TermType,
        diagMeasurement: Double?,
        summativeMeasurement: Double?,
        config: SportScaleConfig = SportScaleConfig()
    ): Double {
        val effectiveMeasurement = calculateAverageMeasurement(diagMeasurement, summativeMeasurement)
        return calculateDiagnosticScore14(termType, effectiveMeasurement, config)
    }

    /**
     * حساب نقطة التقويم (على 14 من 20) من التوقيت أو القياس المحقق
     * وفق مقياس الأستاذ:
     * - في الجري السريع 60م: 5 ثوانٍ تعني 14 نقطة، وكل ما زادت 1 ثانية نقصت نقطة واحدة
     * - في الوثب الطويل: 4.50 م تعني 14 نقطة، وكل ما نقص 0.25 م نقصت نقطة
     * - في دفع الجلة: 8.00 م تعني 14 نقطة، وكل ما نقص 0.50 م نقصت نقطة
     */
    fun calculateDiagnosticScore14(
        termType: TermType,
        measurement: Double?,
        config: SportScaleConfig = SportScaleConfig()
    ): Double {
        if (measurement == null || measurement <= 0.0) return 0.0
        return when (termType) {
            TermType.TERM_1 -> {
                val baseTime = config.sprintBaseTimeSec
                if (measurement <= baseTime) 14.0
                else {
                    val excessSeconds = measurement - baseTime
                    val deduction = (excessSeconds / config.sprintSecPerMinusPoint) * 1.0
                    val score = 14.0 - deduction
                    formatScore(score.coerceIn(0.0, 14.0))
                }
            }
            TermType.TERM_2 -> {
                val baseDistance = config.jumpBaseDistMeters
                if (measurement >= baseDistance) 14.0
                else {
                    val deficitMeters = baseDistance - measurement
                    val deduction = (deficitMeters / config.jumpDistPerMinusPoint) * 1.0
                    val score = 14.0 - deduction
                    formatScore(score.coerceIn(0.0, 14.0))
                }
            }
            TermType.TERM_3 -> {
                val baseDistance = config.shotPutBaseDistMeters
                if (measurement >= baseDistance) 14.0
                else {
                    val deficitMeters = baseDistance - measurement
                    val deduction = (deficitMeters / config.shotPutDistPerMinusPoint) * 1.0
                    val score = 14.0 - deduction
                    formatScore(score.coerceIn(0.0, 14.0))
                }
            }
        }
    }

    /**
     * حساب علامة التطور الحاصل على 6 من 20 بعد التقويم التحصيلي
     * شرط أساسي وفق توجيه الأستاذ:
     * إذا كان التطور إيجابياً (تحسن في النتيجة) يُحسب،
     * وإذا كان سلبياً (تراجع في النتيجة) يُعطى 0 (صفر).
     *
     * - في الجري السريع: التحسن يعني نقص في زمن الجري (تشخيص > تحصيلي).
     *   كل 0.5 ثانية تحسن إيجابي تقابلها 1 نقطة (حتى 6 نقاط).
     *   إذا كان زمن التحصيلي أكبر أو يساوي التشخيص (تراجع سلبي) -> 0 نقطة.
     *
     * - في الوثب ودفع الجلة: التحسن يعني زيادة في المسافة (تحصيلي > تشخيص).
     *   كل زيادة إيجابية تقابلها نقاط التطور، وإذا كانت المسافة أقل -> 0 نقطة.
     */
    fun calculateProgressionScore6(
        termType: TermType,
        diagMeasurement: Double?,
        summativeMeasurement: Double?,
        config: SportScaleConfig = SportScaleConfig()
    ): Double {
        if (diagMeasurement == null || summativeMeasurement == null) return 0.0
        return when (termType) {
            TermType.TERM_1 -> {
                // في الجري: التطور الإيجابي = زمن التشخيص - زمن التحصيلي (أي ربح ثوانٍ)
                val improvementSeconds = diagMeasurement - summativeMeasurement
                if (improvementSeconds <= 0.0) {
                    // تراجع سلبي أو لا يوجد تطور -> صفر
                    0.0
                } else {
                    // تحسن إيجابي: كل 0.5 ثانية تحسن = 1 نقطة
                    val score = (improvementSeconds / config.sprintSecPerProgressionPoint) * 1.0
                    formatScore(score.coerceIn(0.0, 6.0))
                }
            }
            TermType.TERM_2 -> {
                // في الوثب: التطور الإيجابي = زيادة المسافة في التحصيلي
                val improvementMeters = summativeMeasurement - diagMeasurement
                if (improvementMeters <= 0.0) {
                    0.0
                } else {
                    val score = (improvementMeters / config.jumpDistPerProgressionPoint) * 1.0
                    formatScore(score.coerceIn(0.0, 6.0))
                }
            }
            TermType.TERM_3 -> {
                // في دفع الجلة: التطور الإيجابي = زيادة المسافة في التحصيلي
                val improvementMeters = summativeMeasurement - diagMeasurement
                if (improvementMeters <= 0.0) {
                    0.0
                } else {
                    val score = (improvementMeters / config.shotPutDistPerProgressionPoint) * 1.0
                    formatScore(score.coerceIn(0.0, 6.0))
                }
            }
        }
    }

    /**
     * حساب مقدار التطور كنص وصفي منسق
     */
    fun formatImprovement(
        termType: TermType,
        diagMeasurement: Double?,
        summativeMeasurement: Double?,
        config: SportScaleConfig = SportScaleConfig()
    ): String {
        if (diagMeasurement == null || summativeMeasurement == null) return "غير محدد"
        return when (termType) {
            TermType.TERM_1 -> {
                val improvement = diagMeasurement - summativeMeasurement
                val points = calculateProgressionScore6(termType, diagMeasurement, summativeMeasurement, config)
                when {
                    improvement > 0.0 -> "+${String.format(Locale.US, "%.1f", improvement)} ث (${String.format(Locale.US, "%.1f", points)} ن)"
                    improvement == 0.0 -> "0.0 ث (0 ن)"
                    else -> "-${String.format(Locale.US, "%.1f", -improvement)} ث (0 ن تراجع)"
                }
            }
            TermType.TERM_2, TermType.TERM_3 -> {
                val improvement = summativeMeasurement - diagMeasurement
                val points = calculateProgressionScore6(termType, diagMeasurement, summativeMeasurement, config)
                when {
                    improvement > 0.0 -> "+${String.format(Locale.US, "%.2f", improvement)} م (${String.format(Locale.US, "%.1f", points)} ن)"
                    improvement == 0.0 -> "0.0 م (0 ن)"
                    else -> "${String.format(Locale.US, "%.2f", improvement)} م (0 ن تراجع)"
                }
            }
        }
    }

    /**
     * نص وصفي لصيغة جمع القياسين وقسمتهما على اثنين
     */
    fun formatAverageDetail(
        termType: TermType,
        diagMeasurement: Double?,
        summativeMeasurement: Double?
    ): String {
        val unit = termType.individualUnit
        if (diagMeasurement != null && summativeMeasurement != null) {
            val avg = calculateAverageMeasurement(diagMeasurement, summativeMeasurement)
            val dStr = String.format(Locale.US, if (termType == TermType.TERM_1) "%.1f" else "%.2f", diagMeasurement)
            val sStr = String.format(Locale.US, if (termType == TermType.TERM_1) "%.1f" else "%.2f", summativeMeasurement)
            val aStr = String.format(Locale.US, if (termType == TermType.TERM_1) "%.1f" else "%.2f", avg ?: 0.0)
            return "($dStr + $sStr) ÷ 2 = $aStr $unit"
        }
        val single = summativeMeasurement ?: diagMeasurement
        return if (single != null) {
            val str = String.format(Locale.US, if (termType == TermType.TERM_1) "%.1f" else "%.2f", single)
            "$str $unit"
        } else "غير مقاس"
    }

    private fun formatScore(score: Double): Double {
        return String.format(Locale.US, "%.1f", score).toDouble()
    }
}
