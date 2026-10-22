package com.example.data.model

data class ImportedStudentResult(
    val id: String = "",
    val regNumber: Int = 0,
    val fullName: String,
    val gender: Gender = Gender.MALE,
    val isMedicallyExempt: Boolean = false,
    val continuousScore: Double? = null,
    val testScore: Double? = null,
    val examScore: Double? = null,
    val individualScore: Double? = null,
    val notes: String = ""
)
