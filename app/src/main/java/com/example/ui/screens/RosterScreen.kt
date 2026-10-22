package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Gender
import com.example.data.model.Student
import com.example.ui.components.AddEditStudentDialog
import com.example.ui.components.MetricCard
import com.example.ui.components.ResultSheetScannerDialog
import com.example.ui.theme.ActionNegative
import com.example.ui.theme.ActionPositive
import com.example.ui.theme.SportAmber
import com.example.ui.theme.SportBlue
import com.example.ui.viewmodel.EpsViewModel

@Composable
fun RosterScreen(
    viewModel: EpsViewModel,
    modifier: Modifier = Modifier
) {
    val students by viewModel.currentClassStudents.collectAsState()
    val classes by viewModel.classes.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val continuousAssessments by viewModel.continuousAssessments.collectAsState()
    val criteria by viewModel.criteria.collectAsState()
    val attendances by viewModel.attendances.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<Student?>(null) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }
    var studentToReset by remember { mutableStateOf<Student?>(null) }
    var showDeleteClassDialog by remember { mutableStateOf(false) }
    var showScannerDialog by remember { mutableStateOf(false) }

    val totalCount = students.size
    val maleCount = students.count { it.gender == Gender.MALE }
    val femaleCount = students.count { it.gender == Gender.FEMALE }
    val exemptCount = students.count { it.isMedicallyExempt }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Stats Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedClass?.name ?: "قائمة التلاميذ",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${selectedClass?.level ?: ""} • ${selectedClass?.academicYear ?: ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            if (classes.size > 1 && selectedClass != null) {
                                IconButton(
                                    onClick = { showDeleteClassDialog = true },
                                    modifier = Modifier.testTag("delete_current_class_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف القسم",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatPill(
                                label = "المجموع",
                                count = "$totalCount",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            StatPill(
                                label = "ذكور",
                                count = "$maleCount",
                                color = SportBlue,
                                modifier = Modifier.weight(1f)
                            )
                            StatPill(
                                label = "إناث",
                                count = "$femaleCount",
                                color = Color(0xFFC2185B),
                                modifier = Modifier.weight(1f)
                            )
                            StatPill(
                                label = "معفى",
                                count = "$exemptCount",
                                color = ActionNegative,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Quick Scan & Upload Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showScannerDialog = true }
                        .testTag("scan_sheet_banner_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "تسجيل النتائج والأسماء بصورة لورقة النتائج",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = SportAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "تصوير بالكاميرا، تحميل صورة، أو استيراد ملف نصي لاستخراج الأسماء والنقاط فورياً",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Student Items
            if (students.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "لا يوجد تلاميذ في هذا الفوج حالياً",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "اضغط على زر (+) لإضافة تلميذ جديد",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(students, key = { it.id }) { student ->
                    val assessment = continuousAssessments[student.id]
                    val crit = criteria[student.id]
                    val absentCount = attendances.count {
                        it.studentId == student.id &&
                                (it.status.name.startsWith("ABSENT"))
                    }

                    StudentCard(
                        student = student,
                        termAverage = assessment?.termAverage,
                        criterionPercentage = crit?.percentage,
                        absentCount = absentCount,
                        onEdit = { studentToEdit = student },
                        onDelete = { studentToDelete = student },
                        onReset = { studentToReset = student }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FloatingActionButton(
                onClick = { showScannerDialog = true },
                modifier = Modifier.testTag("scan_sheet_fab"),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مسح ورقة النتائج", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_student_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة تلميذ")
            }
        }
    }

    if (showAddDialog) {
        selectedClass?.let { currentClass ->
            AddEditStudentDialog(
                classId = currentClass.id,
                nextRegNumber = students.size + 1,
                onDismiss = { showAddDialog = false },
                onSave = { newStudent ->
                    viewModel.addOrUpdateStudent(newStudent)
                    showAddDialog = false
                }
            )
        }
    }

    studentToEdit?.let { student ->
        AddEditStudentDialog(
            classId = student.classId,
            initialStudent = student,
            onDismiss = { studentToEdit = null },
            onSave = { updated ->
                viewModel.addOrUpdateStudent(updated)
                studentToEdit = null
            }
        )
    }

    studentToDelete?.let { student ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = { Text("حذف التلميذ") },
            text = { Text("هل أنت متأكد من حذف التلميذ \"${student.fullName}\" مع جميع بياناته؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteStudent(student.id)
                        studentToDelete = null
                    }
                ) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showDeleteClassDialog && selectedClass != null) {
        AlertDialog(
            onDismissRequest = { showDeleteClassDialog = false },
            title = { Text("حذف القسم نهائياً") },
            text = { Text("هل تريد بالتأكيد حذف قسم \"${selectedClass!!.name}\"؟ سيتم حذف جميع التلاميذ والتقويمات المرتبطة به نهائياً.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteClass(selectedClass!!.id)
                        showDeleteClassDialog = false
                    }
                ) {
                    Text("نعم، حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteClassDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (studentToReset != null) {
        val student = studentToReset!!
        AlertDialog(
            onDismissRequest = { studentToReset = null },
            title = { Text("تصفير نتائج التلميذ") },
            text = { Text("هل تريد بالتأكيد تصفير جميع نتائج التلميذ \"${student.fullName}\"؟ سيتم إعادة جميع نقاط الفصول والتقويم المستمر إلى 0.0 دون حذف بيانات التلميذ.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetStudentResults(student.id, null)
                        studentToReset = null
                    }
                ) {
                    Text("تصفير النتائج", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToReset = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showScannerDialog) {
        ResultSheetScannerDialog(
            viewModel = viewModel,
            onDismiss = { showScannerDialog = false }
        )
    }
}

@Composable
private fun StudentCard(
    student: Student,
    termAverage: Double?,
    criterionPercentage: Double?,
    absentCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_card_${student.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reg Number Circle
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (student.gender == Gender.MALE) SportBlue.copy(alpha = 0.15f)
                        else Color(0xFFC2185B).copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${student.regNumber}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (student.gender == Gender.MALE) SportBlue else Color(0xFFC2185B)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (student.gender == Gender.MALE) Icons.Default.Male else Icons.Default.Female,
                        contentDescription = student.gender.labelAr,
                        modifier = Modifier.size(16.dp),
                        tint = if (student.gender == Gender.MALE) SportBlue else Color(0xFFC2185B)
                    )
                }

                // Badges row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (student.isMedicallyExempt) {
                        Surface(
                            color = ActionNegative.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = ActionNegative
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "معفى طبياً",
                                    fontSize = 11.sp,
                                    color = ActionNegative,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        termAverage?.let { avg ->
                            Surface(
                                color = if (avg >= 10.0) ActionPositive.copy(alpha = 0.15f) else ActionNegative.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "معدل: $avg/20",
                                    fontSize = 11.sp,
                                    color = if (avg >= 10.0) ActionPositive else ActionNegative,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        criterionPercentage?.let { pct ->
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "معيار: $pct%",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (absentCount > 0) {
                        Surface(
                            color = SportAmber.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "غياب: $absentCount",
                                fontSize = 11.sp,
                                color = SportAmber,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Reset, Edit & Delete actions
            IconButton(onClick = onReset, modifier = Modifier.testTag("reset_student_btn_${student.id}")) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "تصفير نتائج التلميذ",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_student_${student.id}")) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "تعديل",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_student_${student.id}")) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.85f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
