package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.ContinuousAssessmentScale
import com.example.data.model.Student
import com.example.data.model.TermAssessment
import com.example.data.model.TermType
import com.example.ui.components.MetricCard
import com.example.ui.components.ResultSheetScannerDialog
import com.example.ui.theme.ActionNegative
import com.example.ui.theme.ActionPositive
import com.example.ui.theme.SportAmber
import com.example.ui.theme.SportBlue
import com.example.ui.theme.SportTeal
import com.example.ui.viewmodel.EpsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AssessmentAttendanceScreen(
    viewModel: EpsViewModel,
    modifier: Modifier = Modifier
) {
    val students by viewModel.currentClassStudents.collectAsState()
    val termAssessments by viewModel.termAssessments.collectAsState()
    val attendances by viewModel.attendances.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedTerm by viewModel.selectedTerm.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()

    val indSport = selectedClass?.getIndividualSport(selectedTerm) ?: selectedTerm.individualSportAr
    val colSport = selectedClass?.getCollectiveSport(selectedTerm) ?: selectedTerm.collectiveSportAr

    var selectedTab by remember { mutableIntStateOf(0) } // 0: علامات الفصل والتقويم المستمر, 1: سجل المتابعة اليومية
    var studentForAssessmentEdit by remember { mutableStateOf<Pair<Student, TermAssessment>?>(null) }
    var studentToReset by remember { mutableStateOf<Student?>(null) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showScannerDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun getAssessment(studentId: String): TermAssessment {
        val key = "${studentId}_${selectedTerm.termNumber}"
        return termAssessments[key] ?: TermAssessment(studentId = studentId, termType = selectedTerm)
    }

    val activeStudents = students.filter { !it.isMedicallyExempt }
    val termAverages = activeStudents.map { getAssessment(it.id).termAverage }
    val classAvg = if (termAverages.isNotEmpty()) termAverages.average() else 0.0
    val passCount = termAverages.count { it >= 10.0 }
    val passRate = if (termAverages.isNotEmpty()) (passCount.toDouble() / termAverages.size * 100) else 0.0

    // Date navigation helpers
    fun shiftDate(days: Int) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance()
            cal.time = sdf.parse(selectedDate) ?: Calendar.getInstance().time
            cal.add(Calendar.DAY_OF_YEAR, days)
            viewModel.updateDate(sdf.format(cal.time))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("علامات ${selectedTerm.labelAr}", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Grade, contentDescription = null) },
                    modifier = Modifier.testTag("tab_continuous")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("سجل المتابعة اليومية (المعايير الـ 6)", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.EventNote, contentDescription = null) },
                    modifier = Modifier.testTag("tab_attendance")
                )
            }

            if (selectedTab == 0) {
                // Tab 0: Marks and Continuous Assessment
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Official Algerian EPS Calculation Rules
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Calculate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "طريقة الحساب الرسمية لـ ${selectedTerm.labelAr}:",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "• الاختبار (20ن) = ($indSport + $colSport) ÷ 2\n" +
                                            "• الفرض (20ن) = يملأه الأستاذ يدوياً\n" +
                                            "• التقويم المستمر (20ن) = من معايير المتابعة اليومية الستة:\n" +
                                            "  [غياب ومواظبة 4ن + لباس 3ن + سلوك 4ن + مهام فردية 3ن + فريق 3ن + إبداع 3ن = 20ن]\n" +
                                            "• معدل الفصل = (التقويم المستمر + الفرض + الاختبار × 2) ÷ 4",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricCard(
                                title = "معدل ${selectedTerm.labelAr}",
                                value = "${String.format(Locale.US, "%.2f", classAvg)}/20",
                                subtitle = "العام للقسم",
                                icon = Icons.Default.Grade,
                                accentColor = SportBlue,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "نسبة النجاح (>=10)",
                                value = "${String.format(Locale.US, "%.1f", passRate)}%",
                                subtitle = "$passCount من أصل ${activeStudents.size}",
                                icon = Icons.Default.Check,
                                accentColor = ActionPositive,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Banner for Scanning/Uploading Result Sheet
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showScannerDialog = true }
                                .testTag("scan_marks_sheet_card"),
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
                                    Text(
                                        text = "تسجيل علامات الفصل بمسح ورقة كشف النقاط",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "التقاط صورة للورقة بالكاميرا أو استيراد ملف لرصد نقاط التقويم والفرض والاختبار",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    items(students, key = { it.id }) { student ->
                        val ass = getAssessment(student.id)
                        val annualAvg = viewModel.getStudentAnnualAverage(student.id)

                        ContinuousStudentCard(
                            student = student,
                            assessment = ass,
                            annualAvg = annualAvg,
                            termType = selectedTerm,
                            indSport = indSport,
                            colSport = colSport,
                            onEdit = { studentForAssessmentEdit = Pair(student, ass) },
                            onReset = { studentToReset = student }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            } else {
                // Tab 1: Daily Tracking Register (سجل الغيابات والمواظبة واللباس والسلوك والمهام والفريق والإبداع)
                val todayRecords = attendances.filter { it.date == selectedDate }
                val presentCount = students.count { s ->
                    val rec = todayRecords.firstOrNull { it.studentId == s.id }
                    rec?.status == AttendanceStatus.PRESENT || (rec == null && !s.isMedicallyExempt)
                }
                val absentCount = students.count { s ->
                    val rec = todayRecords.firstOrNull { it.studentId == s.id }
                    rec?.status == AttendanceStatus.ABSENT_UNEXCUSED || rec?.status == AttendanceStatus.ABSENT_EXCUSED
                }
                val missingUniformCount = todayRecords.count { it.status == AttendanceStatus.PRESENT && !it.hasSportswear }
                val misbehaviorCount = todayRecords.count { it.status == AttendanceStatus.PRESENT && !it.goodBehavior }
                val taskDoneCount = todayRecords.count { it.status == AttendanceStatus.PRESENT && it.individualTaskCompleted }
                val teamworkCount = todayRecords.count { it.status == AttendanceStatus.PRESENT && it.teamworkContribution }
                val creativeCount = todayRecords.count { it.status == AttendanceStatus.PRESENT && it.creativityDemonstrated }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Card with comprehensive title and explanation
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.EventNote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "سجل الغيابات والمواظبة واللباس الرياضي والسلوك وإنجاز المهام الفردية والعمل ضمن الفريق والإبداع",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            lineHeight = 20.sp
                                        )
                                        Text(
                                            text = "متابعة يومية مستمرة لكل حصة — تُحدد من خلالها نقطة التقويم المستمر الرسمية (من 20)",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Date navigation row
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        OutlinedButton(
                                            onClick = { shiftDate(-7) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الحصة السابقة", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("الحصة السابقة", fontSize = 11.sp)
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { showDatePickerDialog = true }
                                        ) {
                                            Icon(
                                                Icons.Default.CalendarToday,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = selectedDate,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "انقر لتغيير التاريخ",
                                                    fontSize = 9.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = { shiftDate(7) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("الحصة القادمة", fontSize = 11.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "الحصة القادمة", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Summary Pills for the 6 criteria today
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    SmallSummaryChip("حاضرون", "$presentCount", ActionPositive)
                                    SmallSummaryChip("غياب", "$absentCount", ActionNegative)
                                    SmallSummaryChip("بدون بذلة", "$missingUniformCount", if (missingUniformCount > 0) ActionNegative else SportTeal)
                                    SmallSummaryChip("سلوك غير منضبط", "$misbehaviorCount", if (misbehaviorCount > 0) ActionNegative else ActionPositive)
                                    SmallSummaryChip("أنجزوا المهام", "$taskDoneCount", SportBlue)
                                    SmallSummaryChip("عمل جماعي", "$teamworkCount", SportTeal)
                                    SmallSummaryChip("إبداع ★", "$creativeCount", SportAmber)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Batch recalculate button
                                Button(
                                    onClick = {
                                        viewModel.recalculateContinuousAssessmentsFromDaily()
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("تم تحديث علامة التقويم المستمر لجميع تلاميذ القسم بنجاح وفق سجل المتابعة اليومية!")
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_recalculate_continuous"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("⚡ تحديث علامات التقويم المستمر آلياً لجميع التلاميذ من هذا السجل", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Student Rows
                    items(students, key = { it.id }) { student ->
                        val currentRec = todayRecords.firstOrNull { it.studentId == student.id }
                        val currentStatus = when {
                            student.isMedicallyExempt -> AttendanceStatus.MEDICALLY_EXEMPT
                            currentRec != null -> currentRec.status
                            else -> AttendanceStatus.PRESENT
                        }

                        // Cumulative student records for the whole term
                        val studentAllRecords = attendances.filter { it.studentId == student.id }
                        val calculatedScores = ContinuousAssessmentScale.calculateScoresFromRecords(student, studentAllRecords)

                        DailyTrackingStudentRow(
                            student = student,
                            record = currentRec,
                            status = currentStatus,
                            calculatedScores = calculatedScores,
                            onSetStatus = { newStatus ->
                                viewModel.setStudentAttendance(student.id, newStatus)
                            },
                            onToggleCriterion = { criterionName, value ->
                                when (criterionName) {
                                    "sportswear" -> viewModel.setStudentDailyCriterion(student.id, hasSportswear = value)
                                    "behavior" -> viewModel.setStudentDailyCriterion(student.id, goodBehavior = value)
                                    "task" -> viewModel.setStudentDailyCriterion(student.id, individualTaskCompleted = value)
                                    "teamwork" -> viewModel.setStudentDailyCriterion(student.id, teamworkContribution = value)
                                    "creativity" -> viewModel.setStudentDailyCriterion(student.id, creativityDemonstrated = value)
                                }
                            },
                            onApplyCalculatedToAssessment = {
                                val currentAss = getAssessment(student.id)
                                val updated = currentAss.copy(
                                    absenceScore = calculatedScores.absenceScore,
                                    sportswearScore = calculatedScores.sportswearScore,
                                    behaviorScore = calculatedScores.behaviorScore,
                                    individualTaskScore = calculatedScores.individualTaskScore,
                                    teamworkScore = calculatedScores.teamworkScore,
                                    creativityScore = calculatedScores.creativityScore,
                                    teamPlayContributionScore = calculatedScores.teamworkScore
                                )
                                viewModel.updateTermAssessment(updated)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("تم تطبيق علامة ${student.fullName} (${calculatedScores.totalScore20}/20) بنجاح!")
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 70.dp)
        )
    }

    if (showDatePickerDialog) {
        var inputDate by remember { mutableStateOf(selectedDate) }
        AlertDialog(
            onDismissRequest = { showDatePickerDialog = false },
            title = { Text("تحديد تاريخ الحصة") },
            text = {
                Column {
                    Text("أدخل تاريخ الحصة بصيغة (YYYY-MM-DD):", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputDate,
                        onValueChange = { inputDate = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            inputDate = sdf.format(Calendar.getInstance().time)
                        }) {
                            Text("اليوم")
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (inputDate.isNotBlank()) {
                        viewModel.updateDate(inputDate.trim())
                    }
                    showDatePickerDialog = false
                }) {
                    Text("تأكيد")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    studentForAssessmentEdit?.let { (student, assessment) ->
        val calculated = viewModel.getCalculatedContinuousScores(student.id)
        EditAssessmentDialog(
            student = student,
            initialAssessment = assessment,
            termType = selectedTerm,
            indSport = indSport,
            colSport = colSport,
            calculatedScores = calculated,
            onDismiss = { studentForAssessmentEdit = null },
            onSave = { updated ->
                viewModel.updateTermAssessment(updated)
                studentForAssessmentEdit = null
            }
        )
    }

    if (studentToReset != null) {
        val student = studentToReset!!
        AlertDialog(
            onDismissRequest = { studentToReset = null },
            title = { Text("تصفير نتائج التلميذ") },
            text = { Text("هل تريد بالتأكيد تصفير نتائج وعلامات التلميذ \"${student.fullName}\" لهذا الفصل (${selectedTerm.labelAr})؟ سيتم إعادة جميع النقاط إلى 0.0 دون المساس ببقية التلاميذ.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetStudentResults(student.id, selectedTerm)
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
private fun SmallSummaryChip(label: String, count: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$count ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = color)
            Text(text = label, fontSize = 10.sp, color = color)
        }
    }
}

/**
 * بطاقة التلميذ في سجل المتابعة اليومية التي تشمل المعايير الستة:
 * 1. الغيابات والمواظبة (ح، غ، م، ع)
 * 2. اللباس الرياضي (بذلة كاملة / بدون بذلة)
 * 3. السلوك والانضباط (منضبط / غير منضبط)
 * 4. إنجاز المهام الفردية (أنجز المهمة / لم ينجز)
 * 5. العمل ضمن الفريق (متعاون ومشارك / غير متعاون)
 * 6. الإبداع (مبدع ومتميز ★ / عادي)
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun DailyTrackingStudentRow(
    student: Student,
    record: AttendanceRecord?,
    status: AttendanceStatus,
    calculatedScores: ContinuousAssessmentScale.CalculatedScores,
    onSetStatus: (AttendanceStatus) -> Unit,
    onToggleCriterion: (String, Boolean) -> Unit,
    onApplyCalculatedToAssessment: () -> Unit
) {
    val isPresent = status == AttendanceStatus.PRESENT
    val hasSportswear = record?.hasSportswear ?: true
    val goodBehavior = record?.goodBehavior ?: true
    val individualTask = record?.individualTaskCompleted ?: true
    val teamwork = record?.teamworkContribution ?: true
    val creativity = record?.creativityDemonstrated ?: false

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_card_${student.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Reg, Name, Attendance Status Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${student.regNumber}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "نقاط المتابعة المحسوبة: ${calculatedScores.totalScore20}/20",
                        fontSize = 10.sp,
                        color = if (calculatedScores.totalScore20 >= 10.0) ActionPositive else ActionNegative,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (student.isMedicallyExempt) {
                    Surface(
                        color = ActionNegative.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "معفى طبياً",
                            color = ActionNegative,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    // Attendance status buttons: ح, غ, م
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        StatusToggleChip(
                            symbol = "ح",
                            isSelected = status == AttendanceStatus.PRESENT,
                            selectedColor = ActionPositive,
                            tooltip = "حاضر"
                        ) { onSetStatus(AttendanceStatus.PRESENT) }

                        StatusToggleChip(
                            symbol = "غ",
                            isSelected = status == AttendanceStatus.ABSENT_UNEXCUSED,
                            selectedColor = ActionNegative,
                            tooltip = "غائب غير مبرر"
                        ) { onSetStatus(AttendanceStatus.ABSENT_UNEXCUSED) }

                        StatusToggleChip(
                            symbol = "م",
                            isSelected = status == AttendanceStatus.ABSENT_EXCUSED,
                            selectedColor = SportAmber,
                            tooltip = "غائب بمبرر"
                        ) { onSetStatus(AttendanceStatus.ABSENT_EXCUSED) }
                    }
                }
            }

            if (isPresent && !student.isMedicallyExempt) {
                Spacer(modifier = Modifier.height(10.dp))

                // The other 5 criteria for the day
                Text(
                    text = "المعايير اليومية للحصة:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1. اللباس الرياضي
                    DailyCriterionPill(
                        label = if (hasSportswear) "بذلة كاملة ✔" else "بدون بذلة ✘",
                        icon = Icons.Default.Checkroom,
                        isSelected = hasSportswear,
                        selectedColor = ActionPositive,
                        unselectedColor = ActionNegative,
                        onClick = { onToggleCriterion("sportswear", !hasSportswear) }
                    )

                    // 2. السلوك والانضباط
                    DailyCriterionPill(
                        label = if (goodBehavior) "سلوك منضبط ✔" else "مخالفة سلوك ✘",
                        icon = Icons.Default.SentimentSatisfiedAlt,
                        isSelected = goodBehavior,
                        selectedColor = ActionPositive,
                        unselectedColor = ActionNegative,
                        onClick = { onToggleCriterion("behavior", !goodBehavior) }
                    )

                    // 3. إنجاز المهام الفردية
                    DailyCriterionPill(
                        label = if (individualTask) "أنجز المهام ✔" else "لم ينجز ✘",
                        icon = Icons.Default.AssignmentTurnedIn,
                        isSelected = individualTask,
                        selectedColor = SportBlue,
                        unselectedColor = MaterialTheme.colorScheme.outline,
                        onClick = { onToggleCriterion("task", !individualTask) }
                    )

                    // 4. العمل ضمن الفريق
                    DailyCriterionPill(
                        label = if (teamwork) "متعاون بالفريق ✔" else "غير متعاون ✘",
                        icon = Icons.Default.Groups,
                        isSelected = teamwork,
                        selectedColor = SportTeal,
                        unselectedColor = MaterialTheme.colorScheme.outline,
                        onClick = { onToggleCriterion("teamwork", !teamwork) }
                    )

                    // 5. الإبداع والمبادرة
                    DailyCriterionPill(
                        label = if (creativity) "مبدع ومتميز ★" else "إبداع عادي",
                        icon = Icons.Default.AutoAwesome,
                        isSelected = creativity,
                        selectedColor = SportAmber,
                        unselectedColor = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { onToggleCriterion("creativity", !creativity) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer bar with breakdown and quick apply button
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "غياب: ${calculatedScores.absenceScore}/4 | لباس: ${calculatedScores.sportswearScore}/3 | سلوك: ${calculatedScores.behaviorScore}/4 | مهام: ${calculatedScores.individualTaskScore}/3 | فريق: ${calculatedScores.teamworkScore}/3 | إبداع: ${calculatedScores.creativityScore}/3",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 12.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    TextButton(
                        onClick = onApplyCalculatedToAssessment,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("تطبيق (${calculatedScores.totalScore20}ن)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyCriterionPill(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) selectedColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.border(
            width = 1.dp,
            color = if (isSelected) selectedColor else unselectedColor.copy(alpha = 0.5f),
            shape = RoundedCornerShape(20.dp)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusToggleChip(
    symbol: String,
    isSelected: Boolean,
    selectedColor: Color,
    tooltip: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) selectedColor else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ContinuousStudentCard(
    student: Student,
    assessment: TermAssessment,
    annualAvg: Double,
    termType: TermType,
    indSport: String = termType.individualSportAr,
    colSport: String = termType.collectiveSportAr,
    onEdit: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("assessment_card_${student.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${student.regNumber}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${assessment.appreciationAr} • سنوي: $annualAvg/20",
                        fontSize = 11.sp,
                        color = if (assessment.termAverage >= 10.0) ActionPositive else ActionNegative,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    color = if (assessment.termAverage >= 10.0) ActionPositive.copy(alpha = 0.15f) else ActionNegative.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${assessment.termAverage}/20",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (assessment.termAverage >= 10.0) ActionPositive else ActionNegative,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(onClick = onReset, modifier = Modifier.testTag("reset_assessment_${student.id}")) {
                    Icon(
                        Icons.Default.RestartAlt,
                        contentDescription = "تصفير نتائج التلميذ",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_assessment_${student.id}")) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "تعديل العلامات",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Score Chips: Exam, Test, Continuous Assessment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GradeChip(
                    label = "الاختبار (ف+ج)÷2",
                    score = "${assessment.examScore20}",
                    color = SportBlue,
                    modifier = Modifier.weight(1.3f)
                )
                GradeChip(
                    label = "الفرض (يدوياً)",
                    score = "${assessment.manualTestScore}",
                    color = SportAmber,
                    modifier = Modifier.weight(1.1f)
                )
                GradeChip(
                    label = "التقويم المستمر",
                    score = "${assessment.continuousAssessmentTotal20}",
                    color = ActionPositive,
                    modifier = Modifier.weight(1.2f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Breakdown of sports and the 6 continuous assessment criteria
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SmallPill(label = indSport, score = "${assessment.individualSportTotal20}/20", modifier = Modifier.weight(1.1f))
                SmallPill(label = colSport, score = "${assessment.collectiveSportScore20}/20", modifier = Modifier.weight(1.1f))
                SmallPill(label = "غياب", score = "${assessment.absenceScore}/4", modifier = Modifier.weight(0.8f))
                SmallPill(label = "لباس", score = "${assessment.sportswearScore}/3", modifier = Modifier.weight(0.8f))
                SmallPill(label = "سلوك", score = "${assessment.behaviorScore}/4", modifier = Modifier.weight(0.8f))
                SmallPill(label = "مهام", score = "${assessment.individualTaskScore}/3", modifier = Modifier.weight(0.8f))
                SmallPill(label = "فريق", score = "${assessment.teamworkScore}/3", modifier = Modifier.weight(0.8f))
                SmallPill(label = "إبداع", score = "${assessment.creativityScore}/3", modifier = Modifier.weight(0.8f))
            }
        }
    }
}

@Composable
private fun SmallPill(label: String, score: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 7.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text(
                text = score,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun GradeChip(label: String, score: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 9.sp, color = color, fontWeight = FontWeight.Medium)
            Text(
                text = "$score / 20",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = color
            )
        }
    }
}

@Composable
private fun EditAssessmentDialog(
    student: Student,
    initialAssessment: TermAssessment,
    termType: TermType,
    indSport: String = termType.individualSportAr,
    colSport: String = termType.collectiveSportAr,
    calculatedScores: ContinuousAssessmentScale.CalculatedScores,
    onDismiss: () -> Unit,
    onSave: (TermAssessment) -> Unit
) {
    var manualTestStr by remember { mutableStateOf(initialAssessment.manualTestScore.toString()) }
    var absenceScore by remember { mutableDoubleStateOf(initialAssessment.absenceScore) }
    var sportswearScore by remember { mutableDoubleStateOf(initialAssessment.sportswearScore) }
    var behaviorScore by remember { mutableDoubleStateOf(initialAssessment.behaviorScore) }
    var individualTaskScore by remember { mutableDoubleStateOf(initialAssessment.individualTaskScore) }
    var teamworkScore by remember { mutableDoubleStateOf(initialAssessment.teamworkScore) }
    var creativityScore by remember { mutableDoubleStateOf(initialAssessment.creativityScore) }

    val contTotal = String.format(
        Locale.US,
        "%.1f",
        (absenceScore + sportswearScore + behaviorScore + individualTaskScore + teamworkScore + creativityScore).coerceIn(0.0, 20.0)
    ).toDouble()

    val testVal = manualTestStr.toDoubleOrNull() ?: initialAssessment.manualTestScore
    val examVal = initialAssessment.examScore20
    val calculatedTermAvg = String.format(Locale.US, "%.2f", (contTotal + testVal + (examVal * 2)) / 4.0).toDouble()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("علامات ${termType.labelAr}: ${student.fullName}") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "علامة الاختبار: ${examVal} / 20",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "معدل ${termType.labelAr}: $calculatedTermAvg / 20",
                                    fontWeight = FontWeight.Bold,
                                    color = ActionPositive,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "($indSport: ${initialAssessment.individualSportTotal20} + $colSport: ${initialAssessment.collectiveSportScore20}) ÷ 2",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = manualTestStr,
                        onValueChange = { manualTestStr = it },
                        label = { Text("علامة الفرض (تُملأ يدوياً من 20)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_test_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "معايير التقويم المستمر الستة ($contTotal / 20):",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = {
                            absenceScore = calculatedScores.absenceScore
                            sportswearScore = calculatedScores.sportswearScore
                            behaviorScore = calculatedScores.behaviorScore
                            individualTaskScore = calculatedScores.individualTaskScore
                            teamworkScore = calculatedScores.teamworkScore
                            creativityScore = calculatedScores.creativityScore
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("استيراد الحساب الآلي من سجل المتابعة (${calculatedScores.totalScore20}/20)", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            manualTestStr = "0.0"
                            absenceScore = 0.0
                            sportswearScore = 0.0
                            behaviorScore = 0.0
                            individualTaskScore = 0.0
                            teamworkScore = 0.0
                            creativityScore = 0.0
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ActionNegative.copy(alpha = 0.15f),
                            contentColor = ActionNegative
                        ),
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تصفير علامات هذا التلميذ (0.0)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    CriteriaSlider(label = "1. الغيابات والمواظبة", value = absenceScore, max = 4.0) { absenceScore = it }
                    CriteriaSlider(label = "2. اللباس الرياضي", value = sportswearScore, max = 3.0) { sportswearScore = it }
                    CriteriaSlider(label = "3. السلوك والانضباط", value = behaviorScore, max = 4.0) { behaviorScore = it }
                    CriteriaSlider(label = "4. إنجاز المهام الفردية", value = individualTaskScore, max = 3.0) { individualTaskScore = it }
                    CriteriaSlider(label = "5. العمل ضمن الفريق", value = teamworkScore, max = 3.0) { teamworkScore = it }
                    CriteriaSlider(label = "6. الإبداع والمبادرة", value = creativityScore, max = 3.0) { creativityScore = it }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = initialAssessment.copy(
                        manualTestScore = testVal.coerceIn(0.0, 20.0),
                        absenceScore = absenceScore,
                        sportswearScore = sportswearScore,
                        behaviorScore = behaviorScore,
                        individualTaskScore = individualTaskScore,
                        teamworkScore = teamworkScore,
                        creativityScore = creativityScore,
                        teamPlayContributionScore = teamworkScore
                    )
                    onSave(updated)
                },
                modifier = Modifier.testTag("save_assessment_btn")
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

@Composable
private fun CriteriaSlider(label: String, value: Double, max: Double, onValueChange: (Double) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 11.sp)
            Text(
                text = "${String.format(Locale.US, "%.1f", value)} / ${String.format(Locale.US, "%.0f", max)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(String.format(Locale.US, "%.1f", it).toDouble()) },
            valueRange = 0f..max.toFloat(),
            steps = (max * 2 - 1).toInt().coerceAtLeast(0)
        )
    }
}
