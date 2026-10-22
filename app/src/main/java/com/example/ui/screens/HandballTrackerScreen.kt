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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsHandball
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.material.icons.filled.SwapHoriz
import com.example.ui.components.ChangeSportsDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.CollectiveMatchAction
import com.example.data.model.SportCriteriaDefinitions
import com.example.data.model.SportCriterionInfo
import com.example.data.model.Student
import com.example.data.model.TermType
import com.example.ui.theme.ActionNegative
import com.example.ui.theme.ActionPositive
import com.example.ui.theme.SportAmber
import com.example.ui.theme.SportBlue
import com.example.ui.viewmodel.EpsViewModel

@Composable
fun HandballTrackerScreen(
    viewModel: EpsViewModel,
    modifier: Modifier = Modifier
) {
    val students by viewModel.currentClassStudents.collectAsState()
    val collectiveActions by viewModel.collectiveActions.collectAsState()
    val selectedTerm by viewModel.selectedTerm.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()

    var selectedTeamFilter by remember { mutableStateOf("الكل") }
    var showResetDialog by remember { mutableStateOf(false) }
    var studentToReset by remember { mutableStateOf<Student?>(null) }
    var selectedStudentForDetail by remember { mutableStateOf<Student?>(null) }
    var showChangeSportsDialog by remember { mutableStateOf(false) }

    val playingStudents = students.filter { !it.isMedicallyExempt }

    fun getAction(studentId: String): CollectiveMatchAction {
        val key = "${studentId}_${selectedTerm.termNumber}"
        return collectiveActions[key] ?: CollectiveMatchAction(studentId = studentId, termType = selectedTerm)
    }

    val filteredStudents = when (selectedTeamFilter) {
        "الفريق أ" -> playingStudents.filter { getAction(it.id).team == "الفريق أ" }
        "الفريق ب" -> playingStudents.filter { getAction(it.id).team == "الفريق ب" }
        else -> playingStudents
    }

    val teamAPlayers = playingStudents.filter { getAction(it.id).team == "الفريق أ" }
    val teamBPlayers = playingStudents.filter { getAction(it.id).team == "الفريق ب" }

    val teamAPositive = teamAPlayers.sumOf { getAction(it.id).totalPositive }
    val teamANegative = teamAPlayers.sumOf { getAction(it.id).totalNegative }
    val teamBPositive = teamBPlayers.sumOf { getAction(it.id).totalPositive }
    val teamBNegative = teamBPlayers.sumOf { getAction(it.id).totalNegative }

    val currentCollectiveSport = selectedClass?.getCollectiveSport(selectedTerm) ?: selectedTerm.collectiveSportAr
    val criteriaList = SportCriteriaDefinitions.getCriteriaForSport(currentCollectiveSport)

    val sportIcon = when {
        currentCollectiveSport.contains("قدم") -> Icons.Default.SportsSoccer
        currentCollectiveSport.contains("سلة") -> Icons.Default.SportsBasketball
        currentCollectiveSport.contains("طائرة") -> Icons.Default.SportsVolleyball
        else -> Icons.Default.SportsHandball
    }

    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = sportIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "رصد مقابلة $currentCollectiveSport",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${selectedTerm.labelAr} • رصد المهارات الخمس (+) و (-)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showChangeSportsDialog = true },
                            modifier = Modifier.testTag("btn_change_sport_in_handball")
                        ) {
                            Icon(
                                Icons.Default.SwapHoriz,
                                contentDescription = "تغيير النشاط الرياضي",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.testTag("reset_handball_btn")
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "تصفير عدادات القسم بالكامل",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scoreboard card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TeamScoreBox(
                        teamName = "الفريق أ",
                        positiveCount = teamAPositive,
                        negativeCount = teamANegative,
                        color = SportBlue,
                        modifier = Modifier.weight(1f)
                    )
                    TeamScoreBox(
                        teamName = "الفريق ب",
                        positiveCount = teamBPositive,
                        negativeCount = teamBNegative,
                        color = SportAmber,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("الكل", "الفريق أ", "الفريق ب").forEach { team ->
                        FilterChip(
                            selected = (selectedTeamFilter == team),
                            onClick = { selectedTeamFilter = team },
                            label = { Text(team) },
                            modifier = Modifier.testTag("team_filter_$team"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredStudents, key = { it.id }) { student ->
                val action = getAction(student.id)
                CollectivePlayerCard(
                    student = student,
                    action = action,
                    termType = selectedTerm,
                    criteriaList = criteriaList,
                    onModifyAction = { isPositive, actionType ->
                        viewModel.modifyCollectiveAction(student.id, isPositive, actionType)
                    },
                    onChangeTeam = { newTeam ->
                        viewModel.setStudentCollectiveTeam(student.id, newTeam)
                    },
                    onOpenDetails = { selectedStudentForDetail = student },
                    onResetPlayer = { studentToReset = student }
                )
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("تصفير عدادات ${selectedTerm.collectiveSportAr} للقسم") },
            text = { Text("هل تريد بالتأكيد إعادة تصفير تسجيلات (+) و (-) لمقابلة ${selectedTerm.collectiveSportAr} لجميع تلاميذ هذا القسم؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetCollectiveForCurrentClassAndTerm()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("تصفير القسم")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    studentToReset?.let { student ->
        AlertDialog(
            onDismissRequest = { studentToReset = null },
            title = { Text("تصفير عدادات التلميذ") },
            text = { Text("هل تريد تصفير عدادات مقابلة ${selectedTerm.collectiveSportAr} للتلميذ \"${student.fullName}\" فقط؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetStudentResults(student.id, selectedTerm)
                        studentToReset = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("تصفير التلميذ")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToReset = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    selectedStudentForDetail?.let { student ->
        val action = getAction(student.id)
        CollectiveActionDetailDialog(
            student = student,
            action = action,
            termType = selectedTerm,
            criteriaList = criteriaList,
            onDismiss = { selectedStudentForDetail = null },
            onAction = { isPos, type ->
                viewModel.modifyCollectiveAction(student.id, isPos, type)
            },
            onResetPlayer = {
                viewModel.resetStudentResults(student.id, selectedTerm)
                selectedStudentForDetail = null
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
private fun TeamScoreBox(
    teamName: String,
    positiveCount: Int,
    negativeCount: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = teamName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "+$positiveCount",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ActionPositive
                )
                Text(
                    text = "  |  ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "-$negativeCount",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ActionNegative
                )
            }
            val total = positiveCount + negativeCount
            val pct = if (total > 0) (positiveCount.toDouble() / total * 100).toInt() else 0
            Text(
                text = "نجاعة: $pct%",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun CollectivePlayerCard(
    student: Student,
    action: CollectiveMatchAction,
    termType: TermType,
    criteriaList: List<SportCriterionInfo>,
    onModifyAction: (Boolean, String) -> Unit,
    onChangeTeam: (String) -> Unit,
    onOpenDetails: () -> Unit,
    onResetPlayer: () -> Unit
) {
    val isTeamA = action.team == "الفريق أ"
    val teamColor = if (isTeamA) SportBlue else SportAmber

    // Quick sub-actions based on sport
    val s1Label = criteriaList[0].labelAr // تمرير واستقبال أو إرسال
    val s5Label = criteriaList[4].labelAr // تهديف

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("col_player_card_${student.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(teamColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${student.regNumber}",
                        fontWeight = FontWeight.Bold,
                        color = teamColor
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${action.effectivenessLevel} • صافي: ${if (action.netScore >= 0) "+${action.netScore}" else action.netScore}",
                        fontSize = 11.sp,
                        color = if (action.netScore >= 0) ActionPositive else ActionNegative,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    onClick = { onChangeTeam(if (isTeamA) "الفريق ب" else "الفريق أ") },
                    shape = RoundedCornerShape(8.dp),
                    color = teamColor.copy(alpha = 0.15f),
                    modifier = Modifier.testTag("toggle_team_${student.id}")
                ) {
                    Text(
                        text = action.team,
                        color = teamColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(onClick = onResetPlayer, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.RestartAlt,
                        contentDescription = "تصفير التلميذ",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(onClick = onOpenDetails, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.MoreHoriz,
                        contentDescription = "تفاصيل المهارات",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = {
                    if (action.totalActions > 0) (action.totalPositive.toFloat() / action.totalActions) else 0.5f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ActionPositive,
                trackColor = ActionNegative.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main (+) and (-) buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onModifyAction(true, "skill1") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActionPositive,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_plus_${student.id}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إيجابي")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+ (${action.totalPositive})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Button(
                    onClick = { onModifyAction(false, "skill1") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActionNegative,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_minus_${student.id}")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "سلبي")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "- (${action.totalNegative})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick sub-actions based on current sport
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SubActionPill(
                    label = "+$s5Label",
                    count = action.positiveSkill5,
                    color = ActionPositive,
                    modifier = Modifier.weight(1f)
                ) {
                    onModifyAction(true, "skill5")
                }
                SubActionPill(
                    label = "+$s1Label",
                    count = action.positiveSkill1,
                    color = ActionPositive,
                    modifier = Modifier.weight(1f)
                ) {
                    onModifyAction(true, "skill1")
                }
                SubActionPill(
                    label = "-ضياع",
                    count = action.negativeSkill5,
                    color = ActionNegative,
                    modifier = Modifier.weight(1f)
                ) {
                    onModifyAction(false, "skill5")
                }
                SubActionPill(
                    label = "-خطأ",
                    count = action.negativeSkill4,
                    color = ActionNegative,
                    modifier = Modifier.weight(1f)
                ) {
                    onModifyAction(false, "skill4")
                }
            }
        }
    }
}

@Composable
private fun SubActionPill(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$label: $count",
                fontSize = 10.sp,
                color = color,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CollectiveActionDetailDialog(
    student: Student,
    action: CollectiveMatchAction,
    termType: TermType,
    criteriaList: List<SportCriterionInfo>,
    onDismiss: () -> Unit,
    onAction: (Boolean, String) -> Unit,
    onResetPlayer: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("تفاصيل مهارات ${termType.collectiveSportAr}: ${student.fullName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("رصد تفصيلي للمهارات الخمس ومؤشرات الإنجاز", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "الإجراءات الإيجابية (+):",
                    style = MaterialTheme.typography.titleSmall,
                    color = ActionPositive,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                ActionDetailRow(label = criteriaList[0].labelAr, count = action.positiveSkill1, color = ActionPositive, onAdd = { onAction(true, "skill1") })
                ActionDetailRow(label = criteriaList[1].labelAr, count = action.positiveSkill2, color = ActionPositive, onAdd = { onAction(true, "skill2") })
                ActionDetailRow(label = criteriaList[2].labelAr, count = action.positiveSkill3, color = ActionPositive, onAdd = { onAction(true, "skill3") })
                ActionDetailRow(label = criteriaList[3].labelAr, count = action.positiveSkill4, color = ActionPositive, onAdd = { onAction(true, "skill4") })
                ActionDetailRow(label = criteriaList[4].labelAr, count = action.positiveSkill5, color = ActionPositive, onAdd = { onAction(true, "skill5") })

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "الإجراءات السلبية (-):",
                    style = MaterialTheme.typography.titleSmall,
                    color = ActionNegative,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                ActionDetailRow(label = "خطأ في ${criteriaList[0].labelAr}", count = action.negativeSkill1, color = ActionNegative, onAdd = { onAction(false, "skill1") })
                ActionDetailRow(label = "ضياع في ${criteriaList[1].labelAr}", count = action.negativeSkill2, color = ActionNegative, onAdd = { onAction(false, "skill2") })
                ActionDetailRow(label = "تقصير في ${criteriaList[2].labelAr}", count = action.negativeSkill3, color = ActionNegative, onAdd = { onAction(false, "skill3") })
                ActionDetailRow(label = "خلل في ${criteriaList[3].labelAr}", count = action.negativeSkill4, color = ActionNegative, onAdd = { onAction(false, "skill4") })
                ActionDetailRow(label = "إخفاق في ${criteriaList[4].labelAr}", count = action.negativeSkill5, color = ActionNegative, onAdd = { onAction(false, "skill5") })

                Spacer(modifier = Modifier.height(12.dp))

                // Single student reset button
                Button(
                    onClick = onResetPlayer,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActionNegative.copy(alpha = 0.15f),
                        contentColor = ActionNegative
                    ),
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تصفير عدادات هذا التلميذ فقط", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("تم")
            }
        }
    )
}

@Composable
private fun ActionDetailRow(
    label: String,
    count: Int,
    color: Color,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$count",
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(
                onClick = onAdd,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "زيادة",
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
