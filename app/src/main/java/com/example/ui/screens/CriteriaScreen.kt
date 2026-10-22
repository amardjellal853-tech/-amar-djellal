package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsHandball
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import com.example.ui.components.ChangeSportsDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.data.model.MasteryLevel
import com.example.data.model.SportCriteriaDefinitions
import com.example.data.model.SportCriterionInfo
import com.example.data.model.Student
import com.example.data.model.TermAssessment
import com.example.data.model.TermType
import com.example.ui.theme.ActionNegative
import com.example.ui.theme.ActionPositive
import com.example.ui.theme.SportAmber
import com.example.ui.theme.SportBlue
import com.example.ui.theme.SportTeal
import com.example.ui.viewmodel.EpsViewModel

@Composable
fun CriteriaScreen(
    viewModel: EpsViewModel,
    modifier: Modifier = Modifier
) {
    val students by viewModel.currentClassStudents.collectAsState()
    val termAssessments by viewModel.termAssessments.collectAsState()
    val selectedTerm by viewModel.selectedTerm.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()

    var studentForCriteriaEdit by remember { mutableStateOf<Pair<Student, TermAssessment>?>(null) }
    var studentToReset by remember { mutableStateOf<Student?>(null) }
    var showChangeSportsDialog by remember { mutableStateOf(false) }

    val currentCollectiveSport = selectedClass?.getCollectiveSport(selectedTerm) ?: selectedTerm.collectiveSportAr
    val criteriaList = SportCriteriaDefinitions.getCriteriaForSport(currentCollectiveSport)

    fun getAssessment(studentId: String): TermAssessment {
        val key = "${studentId}_${selectedTerm.termNumber}"
        return termAssessments[key] ?: TermAssessment(studentId = studentId, termType = selectedTerm)
    }

    val activeStudents = students.filter { !it.isMedicallyExempt }
    val totalActive = activeStudents.size

    val maxCount = activeStudents.count { getAssessment(it.id).masteryLevel == MasteryLevel.MAXIMAL }
    val accCount = activeStudents.count { getAssessment(it.id).masteryLevel == MasteryLevel.ACCEPTABLE }
    val parCount = activeStudents.count { getAssessment(it.id).masteryLevel == MasteryLevel.PARTIAL }
    val limCount = activeStudents.count { getAssessment(it.id).masteryLevel == MasteryLevel.LIMITED }

    val achievedCount = maxCount + accCount
    val achievementPercentage = if (totalActive > 0) (achievedCount.toDouble() / totalActive * 100) else 0.0

    val avgCollective = if (totalActive > 0) {
        activeStudents.map { getAssessment(it.id).effectiveCollectiveScore }.average()
    } else 0.0

    val sportIcon = when {
        currentCollectiveSport.contains("قدم") -> Icons.Default.SportsSoccer
        currentCollectiveSport.contains("سلة") -> Icons.Default.SportsBasketball
        currentCollectiveSport.contains("طائرة") -> Icons.Default.SportsVolleyball
        else -> Icons.Default.SportsHandball
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Collective Sport Criteria Rule Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = sportIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "معايير $currentCollectiveSport (${selectedTerm.labelAr})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "5 معايير × 4 نقاط = 20 نقطة • معايير مخصصة لكل نشاط",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.1f", avgCollective)}/20",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showChangeSportsDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("btn_change_sport_in_criteria"),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.9f)
                        )
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تغيير النشاط ومعاييره المدرسية ($currentCollectiveSport) 🔄",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Criteria names preview pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        criteriaList.forEachIndexed { idx, crit ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = crit.labelAr,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "نسبة تحقيق المعايير للقسم (تحكم أقصى + مقبول): ${String.format(java.util.Locale.US, "%.1f", achievementPercentage)}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (achievementPercentage / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = ActionPositive,
                        trackColor = Color.White.copy(alpha = 0.4f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LevelCountBox(title = "م4: أقصى", scoreRange = "16-20", count = maxCount, color = ActionPositive, modifier = Modifier.weight(1f))
                        LevelCountBox(title = "م3: مقبول", scoreRange = "12-15.5", count = accCount, color = SportBlue, modifier = Modifier.weight(1f))
                        LevelCountBox(title = "م2: جزئي", scoreRange = "8-11.5", count = parCount, color = SportAmber, modifier = Modifier.weight(1f))
                        LevelCountBox(title = "م1: محدود", scoreRange = "<8", count = limCount, color = ActionNegative, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Students Criteria List
        items(students, key = { it.id }) { student ->
            val ass = getAssessment(student.id)
            StudentCriterionCard(
                student = student,
                assessment = ass,
                termType = selectedTerm,
                criteriaList = criteriaList,
                onEdit = { studentForCriteriaEdit = Pair(student, ass) },
                onReset = { studentToReset = student }
            )
        }

        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }

    studentForCriteriaEdit?.let { (student, ass) ->
        EditCriterionDialog(
            student = student,
            initialAss = ass,
            termType = selectedTerm,
            criteriaList = criteriaList,
            onDismiss = { studentForCriteriaEdit = null },
            onSave = { updated ->
                viewModel.updateTermAssessment(updated)
                studentForCriteriaEdit = null
            },
            onResetStudent = {
                viewModel.resetStudentResults(student.id, selectedTerm)
                studentForCriteriaEdit = null
            }
        )
    }

    studentToReset?.let { student ->
        AlertDialog(
            onDismissRequest = { studentToReset = null },
            title = { Text("تصفير نتائج التلميذ") },
            text = { Text("هل تريد تصفير معايير ونتائج التلميذ \"${student.fullName}\" لهذا الفصل (${selectedTerm.labelAr})؟ سيتم إعادة جميع النقاط إلى 0.0 دون التأثير على بقية التلاميذ.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetStudentResults(student.id, selectedTerm)
                        studentToReset = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("تصفير الآن")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToReset = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showChangeSportsDialog && selectedClass != null) {
        ChangeSportsDialog(
            currentClass = selectedClass!!,
            initialTerm = selectedTerm,
            onDismiss = { showChangeSportsDialog = false },
            onSave = { term, ind, col, all, sameCycle ->
                viewModel.updateClassSports(
                    classId = selectedClass!!.id,
                    term = term,
                    individualSport = ind,
                    collectiveSport = col,
                    applyToAllClasses = all,
                    applyToSameCycle = sameCycle
                )
                showChangeSportsDialog = false
            }
        )
    }
}

@Composable
private fun LevelCountBox(
    title: String,
    scoreRange: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.95f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = scoreRange,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StudentCriterionCard(
    student: Student,
    assessment: TermAssessment,
    termType: TermType,
    criteriaList: List<SportCriterionInfo>,
    onEdit: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("criteria_card_${student.id}"),
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
                        text = if (assessment.effectiveCollectiveScore == 0.0) "غير مقيم بعد (0/20)" else "${assessment.masteryLevel.labelAr} (${termType.collectiveSportAr})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (assessment.effectiveCollectiveScore == 0.0) MaterialTheme.colorScheme.onSurfaceVariant else when (assessment.masteryLevel) {
                            MasteryLevel.MAXIMAL -> ActionPositive
                            MasteryLevel.ACCEPTABLE -> SportBlue
                            MasteryLevel.PARTIAL -> SportAmber
                            MasteryLevel.LIMITED -> ActionNegative
                        }
                    )
                }

                Surface(
                    color = when {
                        assessment.effectiveCollectiveScore == 0.0 -> MaterialTheme.colorScheme.surfaceVariant
                        assessment.masteryLevel == MasteryLevel.MAXIMAL -> ActionPositive.copy(alpha = 0.15f)
                        assessment.masteryLevel == MasteryLevel.ACCEPTABLE -> SportBlue.copy(alpha = 0.15f)
                        assessment.masteryLevel == MasteryLevel.PARTIAL -> SportAmber.copy(alpha = 0.15f)
                        else -> ActionNegative.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", assessment.effectiveCollectiveScore)} / 20",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = when {
                            assessment.effectiveCollectiveScore == 0.0 -> MaterialTheme.colorScheme.onSurfaceVariant
                            assessment.masteryLevel == MasteryLevel.MAXIMAL -> ActionPositive
                            assessment.masteryLevel == MasteryLevel.ACCEPTABLE -> SportBlue
                            assessment.masteryLevel == MasteryLevel.PARTIAL -> SportAmber
                            else -> ActionNegative
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(onClick = onReset, modifier = Modifier.testTag("reset_criteria_${student.id}")) {
                    Icon(
                        Icons.Default.RestartAlt,
                        contentDescription = "تصفير نتائج التلميذ",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_criteria_${student.id}")) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "تعديل المعايير",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5 Criteria pills (each out of 4 points)
            val scores = listOf(assessment.c1Score, assessment.c2Score, assessment.c3Score, assessment.c4Score, assessment.c5Score)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                criteriaList.forEachIndexed { idx, crit ->
                    val s = if (idx < scores.size) scores[idx] else 0.0
                    SubScorePill(label = crit.labelAr, score = s, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SubScorePill(label: String, score: Double, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 8.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = "${String.format(java.util.Locale.US, "%.1f", score)}/4",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (score > 0.0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EditCriterionDialog(
    student: Student,
    initialAss: TermAssessment,
    termType: TermType,
    criteriaList: List<SportCriterionInfo>,
    onDismiss: () -> Unit,
    onSave: (TermAssessment) -> Unit,
    onResetStudent: () -> Unit
) {
    var c1 by remember { mutableDoubleStateOf(initialAss.c1Score) }
    var c2 by remember { mutableDoubleStateOf(initialAss.c2Score) }
    var c3 by remember { mutableDoubleStateOf(initialAss.c3Score) }
    var c4 by remember { mutableDoubleStateOf(initialAss.c4Score) }
    var c5 by remember { mutableDoubleStateOf(initialAss.c5Score) }

    val calculatedTotal = String.format(java.util.Locale.US, "%.1f", (c1 + c2 + c3 + c4 + c5).coerceIn(0.0, 20.0)).toDouble()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("معايير ${termType.collectiveSportAr}: ${student.fullName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("5 معايير × 4 نقاط = 20 نقطة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المجموع: $calculatedTotal / 20",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        val level = when {
                            calculatedTotal >= 16.0 -> "المستوى 4 (تحكم أقصى)"
                            calculatedTotal >= 12.0 -> "المستوى 3 (تحكم مقبول)"
                            calculatedTotal >= 8.0 -> "المستوى 2 (تحكم جزئي)"
                            calculatedTotal > 0.0 -> "المستوى 1 (تحكم محدود)"
                            else -> "غير مقيم (0/20)"
                        }
                        Text(
                            text = level,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fast level buttons + Reset button
                Text(
                    text = "تحديد سريع لمستوى التحكم أو تصفير:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = calculatedTotal >= 16.0,
                        onClick = { c1 = 3.6; c2 = 3.6; c3 = 3.6; c4 = 3.6; c5 = 3.6 },
                        label = { Text("م4 (18)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = calculatedTotal in 12.0..15.9,
                        onClick = { c1 = 2.8; c2 = 2.8; c3 = 2.8; c4 = 2.8; c5 = 2.8 },
                        label = { Text("م3 (14)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = calculatedTotal in 8.0..11.9,
                        onClick = { c1 = 2.0; c2 = 2.0; c3 = 2.0; c4 = 2.0; c5 = 2.0 },
                        label = { Text("م2 (10)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = calculatedTotal in 0.1..7.9,
                        onClick = { c1 = 1.0; c2 = 1.0; c3 = 1.0; c4 = 1.0; c5 = 1.0 },
                        label = { Text("م1 (5)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Single-click reset to zero button
                Button(
                    onClick = {
                        c1 = 0.0; c2 = 0.0; c3 = 0.0; c4 = 0.0; c5 = 0.0
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
                    Text("تصفير جميع معايير هذا التلميذ (0.0)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // The 5 Criteria Sliders
                CriterionScoreRow(info = criteriaList[0], value = c1, onValueChange = { c1 = it })
                CriterionScoreRow(info = criteriaList[1], value = c2, onValueChange = { c2 = it })
                CriterionScoreRow(info = criteriaList[2], value = c3, onValueChange = { c3 = it })
                CriterionScoreRow(info = criteriaList[3], value = c4, onValueChange = { c4 = it })
                CriterionScoreRow(info = criteriaList[4], value = c5, onValueChange = { c5 = it })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = initialAss.copy(
                        c1Score = c1,
                        c2Score = c2,
                        c3Score = c3,
                        c4Score = c4,
                        c5Score = c5,
                        collectiveSportScore20 = calculatedTotal
                    )
                    onSave(updated)
                }
            ) {
                Text("حفظ المعايير")
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
private fun CriterionScoreRow(
    info: SportCriterionInfo,
    value: Double,
    onValueChange: (Double) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = info.labelAr,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (info.descriptionAr.isNotEmpty()) {
                    Text(
                        text = info.descriptionAr,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "${String.format(java.util.Locale.US, "%.1f", value)} / 4.0",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(String.format(java.util.Locale.US, "%.1f", it).toDouble()) },
            valueRange = 0f..4f,
            steps = 7, // 0, 0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4
            modifier = Modifier.fillMaxWidth()
        )
    }
}
