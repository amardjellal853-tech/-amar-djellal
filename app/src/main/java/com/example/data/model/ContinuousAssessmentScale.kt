package com.example.data.model

import java.util.Locale

/**
 * حساب علامة التقويم المستمر (من 20) وفق معايير المتابعة اليومية الستة التي حددها الأستاذ:
 * 1. الغيابات والمواظبة (من 4.0)
 * 2. اللباس الرياضي (من 3.0)
 * 3. السلوك والانضباط (من 4.0)
 * 4. إنجاز المهام الفردية (من 3.0)
 * 5. العمل ضمن الفريق (من 3.0)
 * 6. الإبداع والمبادرة (من 3.0)
 * المجموع = 20.0 نقطة
 */
object ContinuousAssessmentScale {
    const val MAX_ABSENCE = 4.0
    const val MAX_SPORTSWEAR = 3.0
    const val MAX_BEHAVIOR = 4.0
    const val MAX_INDIVIDUAL_TASK = 3.0
    const val MAX_TEAMWORK = 3.0
    const val MAX_CREATIVITY = 3.0
    const val MAX_TOTAL = 20.0

    data class CalculatedScores(
        val absenceScore: Double,
        val sportswearScore: Double,
        val behaviorScore: Double,
        val individualTaskScore: Double,
        val teamworkScore: Double,
        val creativityScore: Double,
        val totalScore20: Double,
        val attendedSessionsCount: Int,
        val totalSessionsCount: Int
    )

    fun calculateScoresFromRecords(
        student: Student,
        records: List<AttendanceRecord>
    ): CalculatedScores {
        if (student.isMedicallyExempt) {
            return CalculatedScores(
                absenceScore = MAX_ABSENCE,
                sportswearScore = MAX_SPORTSWEAR,
                behaviorScore = MAX_BEHAVIOR,
                individualTaskScore = MAX_INDIVIDUAL_TASK,
                teamworkScore = MAX_TEAMWORK,
                creativityScore = MAX_CREATIVITY,
                totalScore20 = MAX_TOTAL,
                attendedSessionsCount = 0,
                totalSessionsCount = records.size
            )
        }

        if (records.isEmpty()) {
            return CalculatedScores(
                absenceScore = MAX_ABSENCE,
                sportswearScore = MAX_SPORTSWEAR,
                behaviorScore = MAX_BEHAVIOR,
                individualTaskScore = MAX_INDIVIDUAL_TASK,
                teamworkScore = MAX_TEAMWORK,
                creativityScore = MAX_CREATIVITY,
                totalScore20 = MAX_TOTAL,
                attendedSessionsCount = 0,
                totalSessionsCount = 0
            )
        }

        // 1. الغيابات والمواظبة: 4 نقاط. يخصم 1.0 لكل غياب غير مبرر، و 0.5 لكل غياب بمبرر
        val unexcusedAbsences = records.count { it.status == AttendanceStatus.ABSENT_UNEXCUSED }
        val excusedAbsences = records.count { it.status == AttendanceStatus.ABSENT_EXCUSED }
        val absenceDeduction = (unexcusedAbsences * 1.0) + (excusedAbsences * 0.5)
        val calculatedAbsence = (MAX_ABSENCE - absenceDeduction).coerceIn(0.0, MAX_ABSENCE)

        val attendedRecords = records.filter { it.status == AttendanceStatus.PRESENT }
        val attendedCount = attendedRecords.size

        if (attendedCount == 0) {
            return CalculatedScores(
                absenceScore = round(calculatedAbsence),
                sportswearScore = 0.0,
                behaviorScore = 0.0,
                individualTaskScore = 0.0,
                teamworkScore = 0.0,
                creativityScore = 0.0,
                totalScore20 = round(calculatedAbsence),
                attendedSessionsCount = 0,
                totalSessionsCount = records.size
            )
        }

        // 2. اللباس الرياضي: 3 نقاط. يخصم 1.0 لكل حصة بدون بذلة رياضية
        val missingSportswear = attendedRecords.count { !it.hasSportswear }
        val calculatedSportswear = (MAX_SPORTSWEAR - (missingSportswear * 1.0)).coerceIn(0.0, MAX_SPORTSWEAR)

        // 3. السلوك: 4 نقاط. يخصم 1.0 لكل ملاحظة سوء سلوك
        val misbehaviorCount = attendedRecords.count { !it.goodBehavior }
        val calculatedBehavior = (MAX_BEHAVIOR - (misbehaviorCount * 1.0)).coerceIn(0.0, MAX_BEHAVIOR)

        // 4. إنجاز المهام الفردية: 3 نقاط. نسبة المهام المنجزة في الحصص
        val completedTasksCount = attendedRecords.count { it.individualTaskCompleted }
        val taskRatio = completedTasksCount.toDouble() / attendedCount
        val calculatedTask = (taskRatio * MAX_INDIVIDUAL_TASK).coerceIn(0.0, MAX_INDIVIDUAL_TASK)

        // 5. العمل ضمن الفريق: 3 نقاط. نسبة المشاركة الفعالة والعمل الجماعي
        val teamworkCount = attendedRecords.count { it.teamworkContribution }
        val teamworkRatio = teamworkCount.toDouble() / attendedCount
        val calculatedTeamwork = (teamworkRatio * MAX_TEAMWORK).coerceIn(0.0, MAX_TEAMWORK)

        // 6. الإبداع: 3 نقاط. قاعدة 2.0 + 0.5 لكل حصة فيها إبداع متميز (بحد أقصى 3)
        val creativeCount = attendedRecords.count { it.creativityDemonstrated }
        val calculatedCreativity = (2.0 + (creativeCount * 0.5)).coerceIn(0.0, MAX_CREATIVITY)

        val total = (calculatedAbsence + calculatedSportswear + calculatedBehavior + calculatedTask + calculatedTeamwork + calculatedCreativity).coerceIn(0.0, MAX_TOTAL)

        return CalculatedScores(
            absenceScore = round(calculatedAbsence),
            sportswearScore = round(calculatedSportswear),
            behaviorScore = round(calculatedBehavior),
            individualTaskScore = round(calculatedTask),
            teamworkScore = round(calculatedTeamwork),
            creativityScore = round(calculatedCreativity),
            totalScore20 = round(total),
            attendedSessionsCount = attendedCount,
            totalSessionsCount = records.size
        )
    }

    private fun round(v: Double): Double {
        return String.format(Locale.US, "%.1f", v).toDouble()
    }
}
