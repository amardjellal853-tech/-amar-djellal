package com.example.data.model

data class AppBackupData(
    val exportDate: String,
    val appVersion: String = "2.0",
    val classes: List<ClassRoom>,
    val students: List<Student>,
    val diagnostics: List<DiagnosticEvaluation>,
    val handballActions: List<HandballAction>,
    val criteriaEvaluations: List<CriterionEvaluation>,
    val continuousAssessments: List<ContinuousAssessment>,
    val attendances: List<AttendanceRecord>
)
