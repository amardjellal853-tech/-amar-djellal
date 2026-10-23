package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ai.AiResult
import com.example.data.ai.GeminiAiService
import com.example.data.model.ClassRoom
import com.example.data.model.Student
import com.example.data.model.TeacherProfile
import com.example.data.model.TermAssessment
import com.example.data.model.TermType
import com.example.ui.theme.SportAmber
import com.example.ui.theme.SportBlue
import com.example.ui.theme.SportTeal
import com.example.util.FileDownloadHelper
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiPeAssistantDialog(
    selectedClass: ClassRoom?,
    selectedTerm: TermType,
    students: List<Student>,
    termAssessments: Map<String, TermAssessment>,
    teacherProfile: TeacherProfile,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: تحليل بيداغوجي, 1: تحضير مذكرة

    // State للتحليل البيداغوجي
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<String?>(null) }
    var analysisError by remember { mutableStateOf<String?>(null) }

    // State للمذكرة البيداغوجية
    var selectedSport by remember {
        mutableStateOf(selectedClass?.getIndividualSport(selectedTerm) ?: "الجري السريع 60م")
    }
    var sessionNumber by remember { mutableStateOf("1") }
    var sessionTopic by remember { mutableStateOf("تحسين الانطلاق السريع وتنسيق الخطوات") }
    var sessionGoal by remember { mutableStateOf("القدرة على رد الفعل السريع في وضعيات الانطلاق المختلفة") }
    var isGeneratingLesson by remember { mutableStateOf(false) }
    var lessonResult by remember { mutableStateOf<String?>(null) }
    var lessonError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "المساعد البيداغوجي الذكي",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Gemini 3.8 Flash",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "التحليل البيداغوجي لنتائج الأقسام وتوليد مذكرات الدروس بالذكاء الاصطناعي",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_gemini_dialog_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }
                }

                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("التحليل البيداغوجي للفوج") },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = null) },
                        modifier = Modifier.testTag("tab_gemini_analysis")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("توليد المذكرات البيداغوجية") },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                        modifier = Modifier.testTag("tab_gemini_lesson")
                    )
                }

                when (selectedTab) {
                    0 -> {
                        // تبويب التحليل البيداغوجي
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "بيانات الفوج الموجهة للتحليل:",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val cName = selectedClass?.name ?: "القسم الحالي"
                                        val indSport = selectedClass?.getIndividualSport(selectedTerm) ?: selectedTerm.individualSportAr
                                        val colSport = selectedClass?.getCollectiveSport(selectedTerm) ?: selectedTerm.collectiveSportAr
                                        val nonExempt = students.filter { !it.isMedicallyExempt }
                                        val averages = nonExempt.map { s ->
                                            val key = "${s.id}_${selectedTerm.termNumber}"
                                            termAssessments[key]?.termAverage ?: 0.0
                                        }
                                        val classAvg = if (averages.isNotEmpty()) averages.average() else 0.0
                                        val passCount = averages.count { it >= 10.0 }
                                        val passRate = if (averages.isNotEmpty()) (passCount.toDouble() / averages.size * 100) else 0.0

                                        Text(
                                            text = "• الفوج: $cName | الفصل: ${selectedTerm.labelAr}\n" +
                                                    "• النشاط الفردي: $indSport | النشاط الجماعي: $colSport\n" +
                                                    "• إجمالي التلاميذ: ${students.size} (الممارسون: ${nonExempt.size})\n" +
                                                    "• معدل القسم العام: ${String.format(Locale.US, "%.2f", classAvg)}/20 | نسبة النجاح: ${String.format(Locale.US, "%.1f", passRate)}%",
                                            fontSize = 12.sp,
                                            lineHeight = 20.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = {
                                                isAnalyzing = true
                                                analysisError = null
                                                scope.launch {
                                                    val snippetBuilder = StringBuilder()
                                                    students.take(15).forEach { st ->
                                                        val key = "${st.id}_${selectedTerm.termNumber}"
                                                        val ass = termAssessments[key] ?: TermAssessment(studentId = st.id, termType = selectedTerm)
                                                        snippetBuilder.append("- ${st.fullName}: فردي(${ass.individualSportTotal20}), جماعي(${ass.collectiveSportScore20}), فرض(${ass.manualTestScore}), اختبار(${ass.examScore20}), مستمر(${ass.continuousAssessmentTotal20}), معدل(${ass.termAverage})\n")
                                                    }

                                                    val res = GeminiAiService.generatePedagogicalAnalysis(
                                                        className = cName,
                                                        academicYear = selectedClass?.academicYear ?: "2026/2027",
                                                        termName = selectedTerm.labelAr,
                                                        indSport = indSport,
                                                        colSport = colSport,
                                                        teacherName = teacherProfile.fullName,
                                                        schoolName = teacherProfile.schoolName,
                                                        classAverage = classAvg,
                                                        passRate = passRate,
                                                        studentsCount = students.size,
                                                        exemptCount = students.count { it.isMedicallyExempt },
                                                        studentsDataSnippet = snippetBuilder.toString()
                                                    )
                                                    isAnalyzing = false
                                                    when (res) {
                                                        is AiResult.Success -> analysisResult = res.data
                                                        is AiResult.Error -> analysisError = res.message
                                                    }
                                                }
                                            },
                                            enabled = !isAnalyzing,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("run_gemini_analysis_btn"),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            if (isAnalyzing) {
                                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("جاري استخلاص التحليل عبر Gemini 3.8 Flash...")
                                            } else {
                                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("بدء التحليل البيداغوجي الذكي للفوج", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            if (analysisError != null) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                    ) {
                                        Text(
                                            text = "خطأ: $analysisError",
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                }
                            }

                            if (analysisResult != null) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "التقرير البيداغوجي المستخلص:",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Row {
                                                    IconButton(
                                                        onClick = {
                                                            clipboardManager.setText(AnnotatedString(analysisResult ?: ""))
                                                            Toast.makeText(context, "تم نسخ التقرير للحافظة", Toast.LENGTH_SHORT).show()
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ")
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            val html = """
                                                            <!DOCTYPE html>
                                                            <html lang="ar" dir="rtl">
                                                            <head>
                                                                <meta charset="UTF-8">
                                                                <title>تقرير بيداغوجي ذكي</title>
                                                                <style>
                                                                    body { font-family: Tahoma, sans-serif; direction: rtl; padding: 24px; color: #111; line-height: 1.8; }
                                                                    .btn { background: #0284c7; color: white; padding: 8px 16px; border: none; border-radius: 6px; cursor: pointer; }
                                                                    pre { white-space: pre-wrap; font-family: inherit; background: #f8fafc; padding: 16px; border-radius: 8px; border: 1px solid #cbd5e1; }
                                                                    @media print { .no-print { display: none; } }
                                                                </style>
                                                            </head>
                                                            <body>
                                                                <div class="no-print" style="margin-bottom: 20px; text-align: left;">
                                                                    <button class="btn" onclick="window.print()">🖨️ طباعة التقرير / حفظ PDF</button>
                                                                </div>
                                                                <h2>الجمهورية الجزائرية الديمقراطية الشعبية - وزارة التربية الوطنية</h2>
                                                                <h3>تقرير التحليل البيداغوجي الرسمي (مدعوم بـ Gemini AI)</h3>
                                                                <p><strong>الفوج:</strong> ${selectedClass?.name} | <strong>الأستاذ:</strong> ${teacherProfile.fullName} | <strong>المؤسسة:</strong> ${teacherProfile.schoolName}</p>
                                                                <hr>
                                                                <pre>$analysisResult</pre>
                                                            </body>
                                                            </html>
                                                            """.trimIndent()

                                                            FileDownloadHelper.saveFileToDownloads(
                                                                context = context,
                                                                baseFileName = "تقرير_بيداغوجي_${selectedClass?.name ?: "الفوج"}",
                                                                extension = "html",
                                                                mimeType = "text/html",
                                                                content = html,
                                                                openAfterSave = true
                                                            )
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.Download, contentDescription = "تنزيل")
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Surface(
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = analysisResult ?: "",
                                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 22.sp),
                                                    modifier = Modifier.padding(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // تبويب توليد مذكرات الدروس
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "إعداد مذكرة درس نموذجية بالذكاء الاصطناعي:",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        OutlinedTextField(
                                            value = selectedSport,
                                            onValueChange = { selectedSport = it },
                                            label = { Text("النشاط الرياضي") },
                                            modifier = Modifier.fillMaxWidth().testTag("ai_sport_input")
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        OutlinedTextField(
                                            value = sessionTopic,
                                            onValueChange = { sessionTopic = it },
                                            label = { Text("موضوع الحصة / الميدان") },
                                            modifier = Modifier.fillMaxWidth().testTag("ai_topic_input")
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        OutlinedTextField(
                                            value = sessionGoal,
                                            onValueChange = { sessionGoal = it },
                                            label = { Text("الهدف الإجرائي / الكفاءة الختامية") },
                                            modifier = Modifier.fillMaxWidth().testTag("ai_goal_input")
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = {
                                                isGeneratingLesson = true
                                                lessonError = null
                                                scope.launch {
                                                    val res = GeminiAiService.generateLessonPlan(
                                                        sportName = selectedSport,
                                                        gradeLevel = selectedClass?.level ?: "التعليم الثانوي",
                                                        sessionNumber = sessionNumber.toIntOrNull() ?: 1,
                                                        topic = sessionTopic,
                                                        fieldGoal = sessionGoal
                                                    )
                                                    isGeneratingLesson = false
                                                    when (res) {
                                                        is AiResult.Success -> lessonResult = res.data
                                                        is AiResult.Error -> lessonError = res.message
                                                    }
                                                }
                                            },
                                            enabled = !isGeneratingLesson,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("generate_lesson_plan_btn"),
                                            colors = ButtonDefaults.buttonColors(containerColor = SportTeal)
                                        ) {
                                            if (isGeneratingLesson) {
                                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("جاري تحضير المذكرة النموذجية عبر Gemini...")
                                            } else {
                                                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("توليد المذكرة البيداغوجية النموذجية 📘", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            if (lessonError != null) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                    ) {
                                        Text(
                                            text = "خطأ: $lessonError",
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                }
                            }

                            if (lessonResult != null) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "المذكرة البيداغوجية المولدة:",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = SportTeal
                                                )
                                                Row {
                                                    IconButton(
                                                        onClick = {
                                                            clipboardManager.setText(AnnotatedString(lessonResult ?: ""))
                                                            Toast.makeText(context, "تم نسخ المذكرة للحافظة", Toast.LENGTH_SHORT).show()
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ")
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            val html = """
                                                            <!DOCTYPE html>
                                                            <html lang="ar" dir="rtl">
                                                            <head>
                                                                <meta charset="UTF-8">
                                                                <title>مذكرة بيداغوجية - $selectedSport</title>
                                                                <style>
                                                                    body { font-family: Tahoma, sans-serif; direction: rtl; padding: 24px; color: #111; line-height: 1.8; }
                                                                    .btn { background: #059669; color: white; padding: 8px 16px; border: none; border-radius: 6px; cursor: pointer; }
                                                                    pre { white-space: pre-wrap; font-family: inherit; background: #f8fafc; padding: 16px; border-radius: 8px; border: 1px solid #cbd5e1; }
                                                                    @media print { .no-print { display: none; } }
                                                                </style>
                                                            </head>
                                                            <body>
                                                                <div class="no-print" style="margin-bottom: 20px; text-align: left;">
                                                                    <button class="btn" onclick="window.print()">🖨️ طباعة المذكرة / حفظ PDF</button>
                                                                </div>
                                                                <h2>الجمهورية الجزائرية الديمقراطية الشعبية - وزارة التربية الوطنية</h2>
                                                                <h3>مذكرة تربوية نموذجية لمادة التربية البدنية والرياضية (تحضير بالذكاء الاصطناعي)</h3>
                                                                <p><strong>النشاط:</strong> $selectedSport | <strong>الأستاذ:</strong> ${teacherProfile.fullName} | <strong>المؤسسة:</strong> ${teacherProfile.schoolName}</p>
                                                                <hr>
                                                                <pre>$lessonResult</pre>
                                                            </body>
                                                            </html>
                                                            """.trimIndent()

                                                            FileDownloadHelper.saveFileToDownloads(
                                                                context = context,
                                                                baseFileName = "مذكرة_تربوية_${selectedSport.replace(" ", "_")}",
                                                                extension = "html",
                                                                mimeType = "text/html",
                                                                content = html,
                                                                openAfterSave = true
                                                            )
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.Download, contentDescription = "تنزيل")
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Surface(
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = lessonResult ?: "",
                                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 22.sp),
                                                    modifier = Modifier.padding(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
