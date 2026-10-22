package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.IndividualSportScale
import com.example.data.model.SportScaleConfig
import com.example.data.model.Student
import com.example.data.model.TermAssessment
import com.example.data.model.TermType
import com.example.ui.theme.ActionNegative
import com.example.ui.theme.ActionPositive
import com.example.ui.theme.SportAmber
import com.example.ui.theme.SportAmberContainer
import com.example.ui.theme.SportAmberDark
import com.example.ui.theme.SportBlue
import com.example.ui.theme.SportBlueContainer
import com.example.ui.theme.SportBlueDark
import com.example.ui.viewmodel.EpsViewModel
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * نتيجة مسجلة في الميقاتي لعداء معين
 */
data class HeatRunnerResult(
    val student: Student,
    val lane: Int,
    var timeMillis: Long = 0L,
    var isFinished: Boolean = false,
    var speedKmh: Double = 0.0,
    var speedMs: Double = 0.0,
    var score14: Double = 0.0,
    var progression6: Double = 0.0,
    var isSaved: Boolean = false
)

@Composable
fun SmartSpeedStopwatchDialog(
    viewModel: EpsViewModel,
    initialStudentId: String? = null,
    onDismiss: () -> Unit
) {
    val students by viewModel.currentClassStudents.collectAsState()
    val selectedTerm by viewModel.selectedTerm.collectAsState()
    val scaleConfig by viewModel.scaleConfig.collectAsState()
    val termAssessments by viewModel.termAssessments.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()

    // وضع الميقاتي: 0 = تلميذ فردي (Solo)، 1 = فوج متسابقين (Heat)
    var selectedTab by remember { mutableIntStateOf(0) }

    // مسافة السباق بالأمتار (الافتراضي 60م للجري السريع في الطور الثانوي/المعتمد)
    var raceDistanceMeters by remember { mutableDoubleStateOf(60.0) }
    var isCustomDistance by remember { mutableStateOf(false) }
    var customDistanceInput by remember { mutableStateOf("60") }

    // نوع التقويم: هل نسجل في التقويم التشخيصي أم التحصيلي؟
    var isSummativeEvaluation by remember { mutableStateOf(false) }

    // حالة الميقاتي العام
    var isRunning by remember { mutableStateOf(false) }
    var elapsedMillis by remember { mutableLongStateOf(0L) }
    var startTimeMillis by remember { mutableLongStateOf(0L) }

    // للتلميذ الفردي
    val activeStudents = remember(students) { students.filter { !it.isMedicallyExempt } }
    var selectedStudentId by remember {
        mutableStateOf(
            initialStudentId ?: activeStudents.firstOrNull()?.id ?: ""
        )
    }

    val currentStudent = activeStudents.firstOrNull { it.id == selectedStudentId }
    val currentAssessment = selectedStudentId.let { id ->
        val key = "${id}_${selectedTerm.termNumber}"
        termAssessments[key] ?: TermAssessment(studentId = id, termType = selectedTerm)
    }

    // رسالة تأكيد الحفظ
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    // عدائي الفوج (Heat Mode)
    val heatRunners = remember { mutableStateListOf<HeatRunnerResult>() }

    // تهيئة الفوج عند التبديل لنمط الأفواج أو بدء التشغيل
    LaunchedEffect(selectedTab, activeStudents) {
        if (selectedTab == 1 && heatRunners.isEmpty() && activeStudents.isNotEmpty()) {
            val initialHeat = activeStudents.take(4).mapIndexed { idx, st ->
                HeatRunnerResult(student = st, lane = idx + 1)
            }
            heatRunners.addAll(initialHeat)
        }
    }

    // حلقة عداد الميقاتي الزمني الدقيق (تحديث كل 10 ميلي ثانية)
    LaunchedEffect(isRunning) {
        if (isRunning) {
            val baseTime = System.currentTimeMillis() - elapsedMillis
            while (isRunning) {
                elapsedMillis = System.currentTimeMillis() - baseTime
                delay(10)
            }
        }
    }

    // دالة حساب السرعة والنتائج
    fun calculateSpeedAndScores(timeSec: Double, studentId: String): Triple<Double, Double, Pair<Double, Double>> {
        if (timeSec <= 0.0 || raceDistanceMeters <= 0.0) {
            return Triple(0.0, 0.0, Pair(0.0, 0.0))
        }
        val speedMs = raceDistanceMeters / timeSec
        val speedKmh = speedMs * 3.6

        val key = "${studentId}_${selectedTerm.termNumber}"
        val existingAss = termAssessments[key] ?: TermAssessment(studentId = studentId, termType = selectedTerm)

        val diagMeas = if (isSummativeEvaluation) existingAss.diagnosticMeasurement else timeSec
        val summMeas = if (isSummativeEvaluation) timeSec else existingAss.summativeMeasurement

        val score14 = IndividualSportScale.calculateCombinedScore14(selectedTerm, diagMeas, summMeas, scaleConfig)
        val prog6 = IndividualSportScale.calculateProgressionScore6(selectedTerm, diagMeas, summMeas, scaleConfig)

        return Triple(speedKmh, speedMs, Pair(score14, prog6))
    }

    val elapsedSeconds = elapsedMillis / 1000.0
    val calculatedSpeedKmh = if (elapsedSeconds > 0.0 && raceDistanceMeters > 0.0) {
        (raceDistanceMeters / elapsedSeconds) * 3.6
    } else 0.0
    val calculatedSpeedMs = if (elapsedSeconds > 0.0 && raceDistanceMeters > 0.0) {
        raceDistanceMeters / elapsedSeconds
    } else 0.0

    // حساب العلامة اللحظية للتلميذ الفردي
    val soloScores = if (currentStudent != null && elapsedSeconds > 0.0) {
        val diagMeas = if (isSummativeEvaluation) currentAssessment.diagnosticMeasurement else elapsedSeconds
        val summMeas = if (isSummativeEvaluation) elapsedSeconds else currentAssessment.summativeMeasurement
        val s14 = IndividualSportScale.calculateCombinedScore14(selectedTerm, diagMeas, summMeas, scaleConfig)
        val p6 = IndividualSportScale.calculateProgressionScore6(selectedTerm, diagMeas, summMeas, scaleConfig)
        Pair(s14, p6)
    } else Pair(0.0, 0.0)

    Dialog(
        onDismissRequest = {
            isRunning = false
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 1. Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SportBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = SportBlueDark,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "الميقاتي الذكي للسرعة والنتائج",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "حساب السرعة (كم/سا) والعلامة آلياً • ${selectedClass?.name ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            isRunning = false
                            onDismiss()
                        },
                        modifier = Modifier.testTag("close_stopwatch_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق الميقاتي",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Mode Selector: Solo vs Heat
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تلميذ فردي (سباق مفرد)", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("فوج متسابقين (أروقة)", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Distance & Target Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Distance Chips
                    Text(
                        text = "المسافة:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    listOf(50.0 to "50 م", 60.0 to "60 م (رسمي)", 100.0 to "100 م").forEach { (dist, label) ->
                        FilterChip(
                            selected = raceDistanceMeters == dist && !isCustomDistance,
                            onClick = {
                                raceDistanceMeters = dist
                                isCustomDistance = false
                            },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SportBlueDark,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    FilterChip(
                        selected = isCustomDistance,
                        onClick = { isCustomDistance = true },
                        label = { Text("أخرى", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SportBlueDark,
                            selectedLabelColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Evaluation Type Toggle: التشخيص vs التحصيلي
                    FilterChip(
                        selected = !isSummativeEvaluation,
                        onClick = { isSummativeEvaluation = false },
                        label = { Text("تقويم تشخيصي", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SportBlueContainer,
                            selectedLabelColor = SportBlueDark
                        )
                    )
                    FilterChip(
                        selected = isSummativeEvaluation,
                        onClick = { isSummativeEvaluation = true },
                        label = { Text("تقويم تحصيلي", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SportAmberContainer,
                            selectedLabelColor = SportAmberDark
                        )
                    )
                }

                if (isCustomDistance) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = customDistanceInput,
                            onValueChange = {
                                customDistanceInput = it
                                it.toDoubleOrNull()?.let { d -> raceDistanceMeters = d }
                            },
                            label = { Text("أدخل مسافة السباق بالمتر (مثلاً: 30 أو 80 أو 200)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Text("متر", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Content Area
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 4. Large Digital Stopwatch Panel (شاشة الميقاتي الرقمي الاحترافي)
                    item {
                        DigitalTimerDisplayCard(
                            elapsedMillis = elapsedMillis,
                            isRunning = isRunning,
                            calculatedSpeedKmh = calculatedSpeedKmh,
                            calculatedSpeedMs = calculatedSpeedMs,
                            distanceMeters = raceDistanceMeters
                        )
                    }

                    // 5. Controls Row (أزرار التحكم الكبيرة المصممة لضوء الشمس والملعب)
                    item {
                        StopwatchControlsRow(
                            isRunning = isRunning,
                            hasTime = elapsedMillis > 0,
                            onStartPause = { isRunning = !isRunning },
                            onReset = {
                                isRunning = false
                                elapsedMillis = 0L
                                saveSuccessMessage = null
                                heatRunners.forEach { runner ->
                                    runner.timeMillis = 0L
                                    runner.isFinished = false
                                    runner.isSaved = false
                                    runner.speedKmh = 0.0
                                    runner.score14 = 0.0
                                    runner.progression6 = 0.0
                                }
                            }
                        )
                    }

                    // Feedback Message
                    saveSuccessMessage?.let { msg ->
                        item {
                            Surface(
                                color = ActionPositive.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = ActionPositive,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = msg,
                                        color = ActionPositive,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // 6. Mode Specific Content: Solo vs Heat
                    if (selectedTab == 0) {
                        // Solo Student Mode
                        item {
                            SoloRunnerResultsPanel(
                                students = activeStudents,
                                selectedStudentId = selectedStudentId,
                                onSelectStudent = {
                                    selectedStudentId = it
                                    saveSuccessMessage = null
                                },
                                currentStudent = currentStudent,
                                currentAssessment = currentAssessment,
                                elapsedSeconds = elapsedSeconds,
                                speedKmh = calculatedSpeedKmh,
                                speedMs = calculatedSpeedMs,
                                distanceMeters = raceDistanceMeters,
                                isSummative = isSummativeEvaluation,
                                score14 = soloScores.first,
                                progression6 = soloScores.second,
                                onSaveResult = {
                                    if (currentStudent != null && elapsedSeconds > 0.0) {
                                        val newDiag = if (isSummativeEvaluation) currentAssessment.diagnosticMeasurement else elapsedSeconds
                                        val newSumm = if (isSummativeEvaluation) elapsedSeconds else currentAssessment.summativeMeasurement
                                        val s14 = IndividualSportScale.calculateCombinedScore14(selectedTerm, newDiag, newSumm, scaleConfig)
                                        val p6 = IndividualSportScale.calculateProgressionScore6(selectedTerm, newDiag, newSumm, scaleConfig)

                                        val updatedAss = currentAssessment.copy(
                                            diagnosticMeasurement = newDiag,
                                            summativeMeasurement = newSumm,
                                            individualDiagnosticScore14 = s14,
                                            individualProgressionScore6 = p6,
                                            individualMeasurement = newDiag ?: elapsedSeconds
                                        )
                                        viewModel.updateTermAssessment(updatedAss)
                                        val evalName = if (isSummativeEvaluation) "التحصيلي" else "التشخيصي"
                                        val speedStr = String.format(Locale.US, "%.1f", calculatedSpeedKmh)
                                        val totalStr = String.format(Locale.US, "%.1f", s14 + p6)
                                        saveSuccessMessage = "تم حفظ نتيجة ${currentStudent.fullName} في التقويم $evalName بنجاح! الزمن: ${String.format(Locale.US, "%.2f", elapsedSeconds)} ثانية | السرعة: $speedStr كم/سا | النقطة: $totalStr / 20"
                                    }
                                }
                            )
                        }
                    } else {
                        // Heat / Lanes Multi-runner Mode
                        item {
                            HeatRunnersPanel(
                                heatRunners = heatRunners,
                                isRunning = isRunning,
                                elapsedMillis = elapsedMillis,
                                distanceMeters = raceDistanceMeters,
                                isSummative = isSummativeEvaluation,
                                onRecordFinish = { runnerIndex ->
                                    val runner = heatRunners[runnerIndex]
                                    val finishTimeSec = elapsedMillis / 1000.0
                                    val (spdKmh, spdMs, scores) = calculateSpeedAndScores(finishTimeSec, runner.student.id)
                                    heatRunners[runnerIndex] = runner.copy(
                                        timeMillis = elapsedMillis,
                                        isFinished = true,
                                        speedKmh = spdKmh,
                                        speedMs = spdMs,
                                        score14 = scores.first,
                                        progression6 = scores.second
                                    )
                                },
                                onSaveAllFinished = {
                                    var count = 0
                                    heatRunners.forEachIndexed { idx, runner ->
                                        if (runner.isFinished && !runner.isSaved) {
                                            val timeSec = runner.timeMillis / 1000.0
                                            val key = "${runner.student.id}_${selectedTerm.termNumber}"
                                            val ass = termAssessments[key] ?: TermAssessment(studentId = runner.student.id, termType = selectedTerm)
                                            val newDiag = if (isSummativeEvaluation) ass.diagnosticMeasurement else timeSec
                                            val newSumm = if (isSummativeEvaluation) timeSec else ass.summativeMeasurement
                                            val s14 = IndividualSportScale.calculateCombinedScore14(selectedTerm, newDiag, newSumm, scaleConfig)
                                            val p6 = IndividualSportScale.calculateProgressionScore6(selectedTerm, newDiag, newSumm, scaleConfig)

                                            val updated = ass.copy(
                                                diagnosticMeasurement = newDiag,
                                                summativeMeasurement = newSumm,
                                                individualDiagnosticScore14 = s14,
                                                individualProgressionScore6 = p6,
                                                individualMeasurement = newDiag ?: timeSec
                                            )
                                            viewModel.updateTermAssessment(updated)
                                            heatRunners[idx] = runner.copy(isSaved = true)
                                            count++
                                        }
                                    }
                                    if (count > 0) {
                                        saveSuccessMessage = "تم حفظ نتائج $count متسابقين في السجل العام بنجاح بضغطة واحدة!"
                                    }
                                },
                                onAddStudentToHeat = { student ->
                                    if (heatRunners.none { it.student.id == student.id }) {
                                        heatRunners.add(
                                            HeatRunnerResult(student = student, lane = heatRunners.size + 1)
                                        )
                                    }
                                },
                                onRemoveRunner = { index ->
                                    if (index in heatRunners.indices) {
                                        heatRunners.removeAt(index)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * شاشة الميقاتي الرقمي الكبير عالي التباين
 */
@Composable
private fun DigitalTimerDisplayCard(
    elapsedMillis: Long,
    isRunning: Boolean,
    calculatedSpeedKmh: Double,
    calculatedSpeedMs: Double,
    distanceMeters: Double
) {
    val totalSeconds = elapsedMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hundredths = (elapsedMillis % 1000) / 10

    val timeFormatted = String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, hundredths)

    val cardBorderColor by animateColorAsState(
        targetValue = if (isRunning) ActionPositive else SportBlueDark,
        label = "timerBorder"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, cardBorderColor, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isRunning) ActionPositive.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isRunning) ActionPositive else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRunning) "السباق جاري ⚡" else if (elapsedMillis > 0) "تم إيقاف الميقاتي ⏱️" else "جاهز للانطلاق 🏁",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRunning) ActionPositive else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = SportBlueContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "مسافة: ${String.format(Locale.US, "%.0f", distanceMeters)} م",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportBlueDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Massive Digital Digits
            Text(
                text = timeFormatted,
                fontSize = 46.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Live Calculated Speed Metrics
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SpeedMetricItem(
                    title = "السرعة المحسوبة",
                    value = if (elapsedMillis > 0) String.format(Locale.US, "%.1f", calculatedSpeedKmh) else "--",
                    unit = "كم / ساعة",
                    color = SportBlueDark
                )

                VerticalDivider(modifier = Modifier.height(30.dp))

                SpeedMetricItem(
                    title = "السرعة اللحظية",
                    value = if (elapsedMillis > 0) String.format(Locale.US, "%.2f", calculatedSpeedMs) else "--",
                    unit = "متر / ثانية",
                    color = SportAmberDark
                )

                VerticalDivider(modifier = Modifier.height(30.dp))

                SpeedMetricItem(
                    title = "معادلة القياس",
                    value = "v = d / t",
                    unit = "${String.format(Locale.US, "%.0f", distanceMeters)}م ÷ الزمن",
                    color = ActionPositive
                )
            }
        }
    }
}

@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun SpeedMetricItem(
    title: String,
    value: String,
    unit: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
        Text(
            text = unit,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

/**
 * أزرار التحكم الكبيرة للميقاتي
 */
@Composable
private fun StopwatchControlsRow(
    isRunning: Boolean,
    hasTime: Boolean,
    onStartPause: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Start / Pause Primary Button
        Button(
            onClick = onStartPause,
            modifier = Modifier
                .weight(1.5f)
                .height(52.dp)
                .testTag("stopwatch_start_pause_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) ActionNegative else ActionPositive
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isRunning) "إيقاف مؤقت (Pause)" else if (hasTime) "متابعة التوقيت" else "انطلاق السباق (Start) 🏁",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        // Reset Button
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .testTag("stopwatch_reset_btn"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "تصفير (Reset)",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}

/**
 * لوحة نتائج التلميذ الفردي
 */
@Composable
private fun SoloRunnerResultsPanel(
    students: List<Student>,
    selectedStudentId: String,
    onSelectStudent: (String) -> Unit,
    currentStudent: Student?,
    currentAssessment: TermAssessment,
    elapsedSeconds: Double,
    speedKmh: Double,
    speedMs: Double,
    distanceMeters: Double,
    isSummative: Boolean,
    score14: Double,
    progression6: Double,
    onSaveResult: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "اختيار التلميذ لحساب النتيجة وحفظها:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Student Picker Chips (Horizontal or Flow)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                students.take(8).forEach { st ->
                    FilterChip(
                        selected = st.id == selectedStudentId,
                        onClick = { onSelectStudent(st.id) },
                        label = {
                            Text(
                                "${st.regNumber}. ${st.fullName.split(" ").take(2).joinToString(" ")}",
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SportBlueDark,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (currentStudent != null) {
                Spacer(modifier = Modifier.height(10.dp))

                // Student Identity & Previous History Card
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${currentStudent.fullName} (رقم ${currentStudent.regNumber})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                val prevDiag = currentAssessment.diagnosticMeasurement?.let { "$it ثانية" } ?: "غير مسجل"
                                val prevSumm = currentAssessment.summativeMeasurement?.let { "$it ثانية" } ?: "غير مسجل"
                                Text(
                                    text = "التشخيص السابق: $prevDiag | التحصيلي: $prevSumm",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Performance Badge
                            val performanceLabel = when {
                                elapsedSeconds == 0.0 -> "في انتظار التوقيت"
                                score14 >= 13.0 -> "ممتاز ★★★"
                                score14 >= 11.0 -> "جيد جداً ★★"
                                score14 >= 9.0 -> "متوسط ★"
                                else -> "بحاجة لتطوير السرعة"
                            }
                            val performanceColor = when {
                                score14 >= 13.0 -> ActionPositive
                                score14 >= 11.0 -> SportBlueDark
                                score14 >= 9.0 -> SportAmberDark
                                else -> ActionNegative
                            }

                            Surface(
                                color = performanceColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = performanceLabel,
                                    color = performanceColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Results Preview Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ScoreItemCard(
                                title = "الزمن المحقق",
                                value = if (elapsedSeconds > 0) "${String.format(Locale.US, "%.2f", elapsedSeconds)} ث" else "--",
                                subtitle = "المسافة: ${String.format(Locale.US, "%.0f", distanceMeters)}م",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )

                            ScoreItemCard(
                                title = "السرعة المحسوبة",
                                value = if (speedKmh > 0) "${String.format(Locale.US, "%.1f", speedKmh)}" else "--",
                                subtitle = "كم / ساعة (${String.format(Locale.US, "%.1f", speedMs)} م/ث)",
                                color = SportBlueDark,
                                modifier = Modifier.weight(1.2f)
                            )

                            ScoreItemCard(
                                title = "نقطة الإنجاز (14)",
                                value = if (elapsedSeconds > 0) "${String.format(Locale.US, "%.1f", score14)} / 14" else "--",
                                subtitle = "المقياس المعتمد",
                                color = SportAmberDark,
                                modifier = Modifier.weight(1.1f)
                            )

                            if (isSummative) {
                                ScoreItemCard(
                                    title = "التطور (6ن)",
                                    value = if (elapsedSeconds > 0) "+${String.format(Locale.US, "%.1f", progression6)} / 6" else "--",
                                    subtitle = "مقارنة بالتشخيص",
                                    color = ActionPositive,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Total Score Banner
                        val totalScore = (score14 + progression6).coerceIn(0.0, 20.0)
                        Surface(
                            color = SportBlueContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "مجموع النشاط الفردي المحسوب آلياً:",
                                        fontSize = 11.sp,
                                        color = SportBlueDark
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", totalScore)} من 20",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SportBlueDark
                                    )
                                }

                                Button(
                                    onClick = onSaveResult,
                                    enabled = elapsedSeconds > 0.0,
                                    colors = ButtonDefaults.buttonColors(containerColor = SportBlueDark),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("save_solo_stopwatch_result")
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "حفظ النتيجة في سجل التلميذ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
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

@Composable
private fun ScoreItemCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
            Text(subtitle, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
        }
    }
}

/**
 * لوحة سباقات الأفواج المتعددة (Heats / Lanes Mode)
 */
@Composable
private fun HeatRunnersPanel(
    heatRunners: List<HeatRunnerResult>,
    isRunning: Boolean,
    elapsedMillis: Long,
    distanceMeters: Double,
    isSummative: Boolean,
    onRecordFinish: (Int) -> Unit,
    onSaveAllFinished: () -> Unit,
    onAddStudentToHeat: (Student) -> Unit,
    onRemoveRunner: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "سباق الفوج (انطلاق جماعي):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "اضغط على زر وصول كل تلميذ لحظة قطعه خط النهاية لحساب سرعته وتوقيته فوراً",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onSaveAllFinished,
                    enabled = heatRunners.any { it.isFinished && !it.isSaved },
                    colors = ButtonDefaults.buttonColors(containerColor = SportBlueDark),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حفظ نتائج الجميع", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            heatRunners.forEachIndexed { index, runner ->
                HeatRunnerRowItem(
                    runner = runner,
                    laneNumber = index + 1,
                    isRunning = isRunning,
                    onFinish = { onRecordFinish(index) },
                    onRemove = { onRemoveRunner(index) }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun HeatRunnerRowItem(
    runner: HeatRunnerResult,
    laneNumber: Int,
    isRunning: Boolean,
    onFinish: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = if (runner.isFinished) androidx.compose.foundation.BorderStroke(1.5.dp, ActionPositive) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lane Badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (runner.isFinished) ActionPositive else SportBlueDark),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$laneNumber",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Student Info & Results
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = runner.student.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                if (runner.isFinished) {
                    val timeSec = runner.timeMillis / 1000.0
                    val speedStr = String.format(Locale.US, "%.1f", runner.speedKmh)
                    val s14Str = String.format(Locale.US, "%.1f", runner.score14)
                    Text(
                        text = "الزمن: ${String.format(Locale.US, "%.2f", timeSec)} ث • السرعة: $speedStr كم/سا • النقطة: $s14Str/14",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ActionPositive
                    )
                } else {
                    Text(
                        text = if (isRunning) "في السباق... انتظر الوصول" else "جاهز عند خط الانطلاق",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action / Finish Button
            if (runner.isFinished) {
                Surface(
                    color = ActionPositive.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = ActionPositive,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (runner.isSaved) "تم الحفظ" else "تم الرصد",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ActionPositive
                        )
                    }
                }
            } else {
                Button(
                    onClick = onFinish,
                    enabled = isRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = ActionPositive),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("وصول 🏁", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
