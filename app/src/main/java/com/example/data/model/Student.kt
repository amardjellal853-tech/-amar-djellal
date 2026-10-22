package com.example.data.model

enum class Gender(val labelAr: String) {
    MALE("ذكر"),
    FEMALE("أنثى")
}

data class Student(
    val id: String,
    val classId: String,
    val regNumber: Int,
    val fullName: String,
    val gender: Gender = Gender.MALE,
    val isMedicallyExempt: Boolean = false,
    val notes: String = ""
)
