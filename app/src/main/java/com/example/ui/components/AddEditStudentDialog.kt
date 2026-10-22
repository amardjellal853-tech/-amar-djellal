package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.data.model.Gender
import com.example.data.model.Student

@Composable
fun AddEditStudentDialog(
    classId: String,
    initialStudent: Student? = null,
    nextRegNumber: Int = 1,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    var fullName by remember { mutableStateOf(initialStudent?.fullName ?: "") }
    var regNumberStr by remember {
        mutableStateOf((initialStudent?.regNumber ?: nextRegNumber).toString())
    }
    var gender by remember { mutableStateOf(initialStudent?.gender ?: Gender.MALE) }
    var isMedicallyExempt by remember { mutableStateOf(initialStudent?.isMedicallyExempt ?: false) }
    var notes by remember { mutableStateOf(initialStudent?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialStudent == null) "إضافة تلميذ جديد" else "تعديل بيانات التلميذ")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        errorMessage = null
                    },
                    label = { Text("الاسم واللقب") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = regNumberStr,
                    onValueChange = { regNumberStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("رقم الترتيب / التسجيل") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_reg_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "الجنس:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = (gender == Gender.MALE),
                                onClick = { gender = Gender.MALE },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (gender == Gender.MALE), onClick = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ذكر")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = (gender == Gender.FEMALE),
                                onClick = { gender = Gender.FEMALE },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (gender == Gender.FEMALE), onClick = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("أنثى")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isMedicallyExempt,
                        onCheckedChange = { isMedicallyExempt = it },
                        modifier = Modifier.testTag("med_exempt_checkbox")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "معفى طبياً من ممارسة الرياضة",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات / تشخيص أولي") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_notes_input"),
                    maxLines = 2
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.trim().isEmpty()) {
                        errorMessage = "يرجى إدخال اسم التلميذ"
                        return@Button
                    }
                    val regNum = regNumberStr.toIntOrNull() ?: nextRegNumber
                    val student = Student(
                        id = initialStudent?.id ?: "s_${System.currentTimeMillis()}",
                        classId = classId,
                        regNumber = regNum,
                        fullName = fullName.trim(),
                        gender = gender,
                        isMedicallyExempt = isMedicallyExempt,
                        notes = notes.trim()
                    )
                    onSave(student)
                },
                modifier = Modifier.testTag("save_student_btn")
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
