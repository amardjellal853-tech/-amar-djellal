package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IndividualSportScale
import com.example.data.model.SportScaleConfig
import com.example.data.model.Student
import com.example.data.model.TermAssessment
import com.example.data.model.TermType
import com.example.ui.components.MetricCard
import com.example.ui.components.SmartSpeedStopwatchDialog
import com.example.ui.theme.ActionNegative
import com.example.ui.theme.ActionPositive
import com.example.ui.theme.SportAmber
import com.example.ui.theme.SportBlue
import com.example.ui.theme.SportBlueContainer
import com.example.ui.theme.SportBlueDark
import com.example.ui.viewmodel.EpsViewModel

@Composable
fun DiagnosticScreen(
    viewModel: EpsViewModel,
    modifier: Modifier = Modifier
) {
    val students by viewModel.currentClassStudents.collectAsState()
    val termAssessments by viewModel.termAssessments.collectAsState()
    val selectedTerm by viewModel.selectedTerm.collectAsState()
    val scaleConfig by viewModel.scaleConfig.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()

    val indSport = selectedClass?.getIndividualSport(selectedTerm) ?: selectedTerm.individualSportAr

    var studentForEdit by remember { mutableStateOf<Pair<Student, TermAssessment>?>(null) }
    var showScaleExplanation by remember { mutableStateOf(false) }
    var showScaleSettingsDialog by remember { mutableStateOf(false) }
    var showGlobalStopwatch by remember { mutableStateOf(false) }
    var studentForStopwatch by remember { mutableStateOf<Student?>(null) }

    fun getAssessment(studentId: String): TermAssessment {
        val key = "${studentId}_${selectedTerm.termNumber}"
        return termAssessments[key] ?: TermAssessment(studentId = studentId, termType = selectedTerm)
    }

    val activeStudents = students.filter { !it.isMedicallyExempt }
    val avgDiag14 = if (activeStudents.isNotEmpty()) {
        activeStudents.map { getAssessment(it.id).individualDiagnosticScore14 }.average()
    } else 0.0

    val avgProg6 = if (activeStudents.isNotEmpty()) {
        activeStudents.map { getAssessment(it.id).individualProgressionScore6 }.average()
    } else 0.0

    val avgTotalIndividual = if (activeStudents.isNotEmpty()) {
        activeStudents.map { getAssessment(it.id).individualSportTotal20 }.average()
    } else 0.0

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Individual Sport Info Card with Collapsible Scale Explanation & Settings Button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تقويم $indSport - ${selectedTerm.labelAr}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            val unitDesc = if (selectedTerm == TermType.TERM_1) "بالثواني" else "بالمتر"
                            Text(
                                text = "متوسط القياسين $unitDesc (14ن) + التطور الحاصل (6ن) = 20ن",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }

                        IconButton(
                            onClick = { showScaleSettingsDialog = true },
                            modifier = Modifier.testTag("scale_settings_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "تعديل المقياس يدوياً",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { showScaleExplanation = !showScaleExplanation },
                            modifier = Modifier.testTag("toggle_scale_info_btn")
                        ) {
                            Icon(
                                imageVector = if (showScaleExplanation) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "عرض مقياس التنقيط المعتمد",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val generalScaleSummary = when (selectedTerm) {
                        TermType.TERM_1 -> "المقياس المعتمد (بالثواني): نقطة الاختبار بجمع زمني التشخيص والتحصيلي وقسمتهما على اثنين، ومنها تحسب النقطة على 14 من 20 (5.0ث=14ن، وكل +1ث تنقص 1ن). والتطور كل 0.5ث تحسن = 1ن (حتى 6ن، والتراجع = 0ن)."
                        TermType.TERM_2 -> "المقياس المعتمد بالمتر (الوثب الطويل): نقطة الاختبار بجمع مسافتي التشخيص والتحصيلي بالمتر وقسمتهما على اثنين، ومنها تحسب النقطة على 14 من 20 (4.50م=14ن، وكل نقص بـ 0.25م ينقص 1ن). والتطور بالمتر: كل +0.125م زيادة = 1ن (حتى 6ن، والتراجع = 0ن)."
                        TermType.TERM_3 -> "المقياس المعتمد بالمتر (دفع الجلة): نقطة الاختبار بجمع مسافتي التشخيص والتحصيلي بالمتر وقسمتهما على اثنين، ومنها تحسب النقطة على 14 من 20 (8.00م=14ن، وكل نقص بـ 0.50م ينقص 1ن). والتطور بالمتر: كل +0.25م زيادة = 1ن (حتى 6ن، والتراجع = 0ن)."
                    }

                    Text(
                        text = generalScaleSummary,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                        lineHeight = 18.sp
                    )

                    AnimatedVisibility(visible = showScaleExplanation) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📋 مقياس التنقيط وحساب نقطة الاختبار (${if (selectedTerm == TermType.TERM_1) "بالثواني" else "بالمتر"}):",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 13.sp
                                )
                                TextButton(
                                    onClick = { showScaleSettingsDialog = true },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تخصيص المقياس", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            when (selectedTerm) {
                                TermType.TERM_1 -> {
                                    Text(
                                        text = "1) نقطة الاختبار من متوسط الزمنين (على 14 من 20):",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = SportBlue
                                    )
                                    Text(
                                        text = "  • القاعدة: (زمن التشخيص + زمن التحصيلي) ÷ 2 = متوسط الزمن.",
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "  • المقياس: ${scaleConfig.sprintBaseTimeSec} ثوانٍ بالمتوسط تعني 14/14 كاملة.",
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "  • كل ما زاد متوسط الزمن ${scaleConfig.sprintSecPerMinusPoint} ثانية نقصت 1 نقطة (6ث=13ن، 7ث=12ن، 7.5ث=11.5ن ...).",
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "2) علامة التطور الحاصل (على 6 من 20):",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = SportAmber
                                    )
                                    Text(
                                        text = "  • شرط أساسي: التطور يُحسب فقط إذا كان إيجابياً (تحسن في النتيجة)، وإذا كان سلبياً (تراجع) فينال 0 (صفر).",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SportAmber
                                    )
                                    Text(
                                        text = "  • في الجري 60م: التحسن يعني نقص زمن الجري (تشخيص > تحصيلي). كل ${scaleConfig.sprintSecPerProgressionPoint} ثانية تحسن = 1 نقطة (حتى 6ن).",
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "  • إذا كان زمن التحصيلي أبطأ من التشخيص (تراجع سلبي) -> علامة التطور = 0 نقطة.",
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "★ مثال تحسن إيجابي: تشخيص 8ث وتحصيلي 7ث ➔ تحسن 1ث (+2ن) + متوسط 7.5ث (11.5ن) = 13.5 / 20.",
                                        fontWeight = FontWeight.Bold,
                                        color = ActionPositive,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                    Text(
                                        text = "★ مثال تراجع سلبي: تشخيص 7ث وتحصيلي 8ث ➔ تراجع 1ث (0ن تطور) + متوسط 7.5ث (11.5ن) = 11.5 / 20.",
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                    Text(
                                        text = "★ مثال العلامة الكاملة: تشخيص 5.5ث وتحصيلي 2.5ث ➔ متوسط 4.0ث (14/14) + تحسن 3ث (6/6) = 20 / 20.",
                                        fontWeight = FontWeight.SemiBold,
                                        color = SportBlue,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                                TermType.TERM_2 -> {
                                    Text(
                                        text = "1) نقطة الاختبار من متوسط المسافتين بالمتر (على 14 من 20):",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = SportBlue
                                    )
                                    Text(
                                        text = "  • القاعدة: (مسافة التشخيص بالمتر + مسافة التحصيلي بالمتر) ÷ 2 = متوسط مسافة الوثب بالمتر.",
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "  • المقياس: ${scaleConfig.jumpBaseDistMeters} م تنال 14/14 كاملة.",
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "  • كل ما نقصت المسافة ${scaleConfig.jumpDistPerMinusPoint} م نقصت 1 نقطة (4.25م=13ن، 4.00م=12ن، 3.75م=11ن، 3.50م=10ن ...).",
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "2) علامة التطور الحاصل بالمتر (على 6 من 20):",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = SportAmber
                                    )
                                    Text(
                                        text = "  • شرط أساسي: التطور بالمتر يُحسب فقط عند زيادة المسافة (تحصيلي > تشخيص). التراجع = 0 نقطة.",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SportAmber
                                    )
                                    Text(
                                        text = "  • كل +${scaleConfig.jumpDistPerProgressionPoint} م زيادة تمنح 1 نقطة (زيادة 0.25م = 2ن، زيادة 0.75م = 6ن كاملة).",
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "★ مثال تحسن إيجابي بالمتر: تشخيص 3.50م وتحصيلي 4.00م ➔ متوسط المسافة (3.50+4.00)÷2 = 3.75م (11.0ن/14) + زيادة 0.50م (+4ن/6) = 15.0 / 20.",
                                        fontWeight = FontWeight.Bold,
                                        color = ActionPositive,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                    Text(
                                        text = "★ مثال تراجع بالمتر: تشخيص 4.00م وتحصيلي 3.50م ➔ تراجع 0.50م (0ن تطور) + متوسط 3.75م (11.0ن) = 11.0 / 20.",
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                                TermType.TERM_3 -> {
                                    Text(
                                        text = "1) نقطة الاختبار من متوسط المسافتين بالمتر (على 14 من 20):",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = SportBlue
                                    )
                                    Text(
                                        text = "  • القاعدة: (مسافة التشخيص بالمتر + مسافة التحصيلي بالمتر) ÷ 2 = متوسط مسافة الرمي بالمتر.",
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "  • المقياس: ${scaleConfig.shotPutBaseDistMeters} م تنال 14/14 كاملة.",
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "  • كل ما نقصت المسافة ${scaleConfig.shotPutDistPerMinusPoint} م نقصت 1 نقطة (7.50م=13ن، 7.00م=12ن، 6.50م=11ن، 6.00م=10ن ...).",
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "2) علامة التطور الحاصل بالمتر (على 6 من 20):",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = SportAmber
                                    )
                                    Text(
                                        text = "  • شرط أساسي: التطور بالمتر يُحسب فقط عند زيادة المسافة (تحصيلي > تشخيص). التراجع = 0 نقطة.",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SportAmber
                                    )
                                    Text(
                                        text = "  • كل +${scaleConfig.shotPutDistPerProgressionPoint} م زيادة تمنح 1 نقطة (زيادة 0.50م = 2ن، زيادة 1.50م = 6ن كاملة).",
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "★ مثال تحسن إيجابي بالمتر: تشخيص 6.00م وتحصيلي 7.00م ➔ متوسط المسافة (6.00+7.00)÷2 = 6.50م (11.0ن/14) + زيادة 1.00م (+4ن/6) = 15.0 / 20.",
                                        fontWeight = FontWeight.Bold,
                                        color = ActionPositive,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                    Text(
                                        text = "★ مثال تراجع بالمتر: تشخيص 7.00م وتحصيلي 6.00م ➔ تراجع 1.00م (0ن تطور) + متوسط 6.50م (11.0ن) = 11.0 / 20.",
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Smart Speed Stopwatch Banner Card (ميقاتي السرعة والنتائج الميداني)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showGlobalStopwatch = true }
                    .testTag("diagnostic_open_stopwatch_banner"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SportBlueDark),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SportBlueDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "الميقاتي الذكي للسرعة والنتائج",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = ActionPositive.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "حساب آلي ⚡",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ActionPositive,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "قياس الجري (50م/60م/100م) • حساب السرعة الفورية (كم/سا) • استخراج النقطة (14ن و20ن) وحفظها",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Button(
                        onClick = { showGlobalStopwatch = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SportBlueDark),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تشغيل الميقاتي", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Metrics KPI Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "متوسط التشخيص",
                    value = "${String.format(java.util.Locale.US, "%.1f", avgDiag14)}/14",
                    subtitle = "العام للقسم",
                    icon = Icons.Default.Insights,
                    accentColor = SportBlue,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "متوسط التطور",
                    value = "${String.format(java.util.Locale.US, "%.1f", avgProg6)}/6",
                    subtitle = "التحصيلي",
                    icon = Icons.Default.TrendingUp,
                    accentColor = SportAmber,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "مجموع الفردي",
                    value = "${String.format(java.util.Locale.US, "%.1f", avgTotalIndividual)}/20",
                    subtitle = "14 + 6",
                    icon = Icons.Default.DirectionsRun,
                    accentColor = ActionPositive,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Student Diagnostic Cards
        items(students, key = { it.id }) { student ->
            val ass = getAssessment(student.id)
            DiagnosticStudentCard(
                student = student,
                assessment = ass,
                termType = selectedTerm,
                config = scaleConfig,
                onEdit = { studentForEdit = Pair(student, ass) },
                onLaunchStopwatch = { studentForStopwatch = student }
            )
        }

        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }

    studentForEdit?.let { (student, ass) ->
        EditDiagnosticDialog(
            student = student,
            initialAss = ass,
            termType = selectedTerm,
            config = scaleConfig,
            onDismiss = { studentForEdit = null },
            onSave = { updated ->
                viewModel.updateTermAssessment(updated)
                studentForEdit = null
            }
        )
    }

    if (showScaleSettingsDialog) {
        ScaleSettingsDialog(
            currentConfig = scaleConfig,
            termType = selectedTerm,
            onDismiss = { showScaleSettingsDialog = false },
            onSave = { newConfig ->
                viewModel.updateScaleConfig(newConfig)
                showScaleSettingsDialog = false
            }
        )
    }

    if (showGlobalStopwatch) {
        SmartSpeedStopwatchDialog(
            viewModel = viewModel,
            onDismiss = { showGlobalStopwatch = false }
        )
    }

    studentForStopwatch?.let { student ->
        SmartSpeedStopwatchDialog(
            viewModel = viewModel,
            initialStudentId = student.id,
            onDismiss = { studentForStopwatch = null }
        )
    }
}

@Composable
private fun DiagnosticStudentCard(
    student: Student,
    assessment: TermAssessment,
    termType: TermType,
    config: SportScaleConfig,
    onEdit: () -> Unit,
    onLaunchStopwatch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("diag_card_${student.id}"),
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
                        .size(36.dp)
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
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    if (student.isMedicallyExempt) {
                        Text(
                            text = "معفى طبياً من الأنشطة الميدانية",
                            style = MaterialTheme.typography.bodySmall,
                            color = ActionNegative
                        )
                    } else {
                        val diagMeasStr = assessment.diagnosticMeasurement?.let { "$it ${termType.individualUnit}" } ?: "--"
                        val summMeasStr = assessment.summativeMeasurement?.let { "$it ${termType.individualUnit}" } ?: "--"
                        val avgMeas = IndividualSportScale.calculateAverageMeasurement(assessment.diagnosticMeasurement, assessment.summativeMeasurement)
                        val avgMeasStr = avgMeas?.let { "$it ${termType.individualUnit}" } ?: "--"
                        Text(
                            text = "تشخيص: $diagMeasStr | تحصيلي: $summMeasStr | المتوسط: $avgMeasStr",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!student.isMedicallyExempt) {
                    Surface(
                        color = if (assessment.individualSportTotal20 >= 10.0) ActionPositive.copy(alpha = 0.15f) else ActionNegative.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${assessment.individualSportTotal20} / 20",
                            color = if (assessment.individualSportTotal20 >= 10.0) ActionPositive else ActionNegative,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    IconButton(
                        onClick = onLaunchStopwatch,
                        modifier = Modifier.testTag("stopwatch_student_${student.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "رصد توقيت وسرعة التلميذ بالميقاتي",
                            tint = SportBlueDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_diag_${student.id}")) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "تعديل القياسات والعلامات",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (!student.isMedicallyExempt) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DiagBadge(
                        label = "المتوسط (من 14)",
                        value = "${assessment.individualDiagnosticScore14}/14",
                        subtext = IndividualSportScale.formatAverageDetail(
                            termType,
                            assessment.diagnosticMeasurement,
                            assessment.summativeMeasurement
                        ),
                        color = SportBlue,
                        modifier = Modifier.weight(1.3f)
                    )
                    DiagBadge(
                        label = "التطور (من 6)",
                        value = "+${assessment.individualProgressionScore6}/6",
                        subtext = IndividualSportScale.formatImprovement(
                            termType,
                            assessment.diagnosticMeasurement,
                            assessment.summativeMeasurement,
                            config
                        ),
                        color = SportAmber,
                        modifier = Modifier.weight(1.2f)
                    )
                    DiagBadge(
                        label = "مجموع الفردي",
                        value = "${assessment.individualSportTotal20}/20",
                        subtext = if (assessment.individualSportTotal20 == 20.0) "علامة كاملة ★" else "14 + 6",
                        color = ActionPositive,
                        modifier = Modifier.weight(1.1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagBadge(
    label: String,
    value: String,
    subtext: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtext,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EditDiagnosticDialog(
    student: Student,
    initialAss: TermAssessment,
    termType: TermType,
    config: SportScaleConfig,
    onDismiss: () -> Unit,
    onSave: (TermAssessment) -> Unit
) {
    var diagMeasStr by remember {
        mutableStateOf(initialAss.diagnosticMeasurement?.toString() ?: initialAss.individualMeasurement?.toString() ?: "")
    }
    var summMeasStr by remember {
        mutableStateOf(initialAss.summativeMeasurement?.toString() ?: "")
    }

    var diag14 by remember { mutableDoubleStateOf(initialAss.individualDiagnosticScore14) }
    var prog6 by remember { mutableDoubleStateOf(initialAss.individualProgressionScore6) }

    val totalIndividual = (diag14 + prog6).coerceIn(0.0, 20.0)

    fun applyScaleAuto() {
        val dMeas = diagMeasStr.toDoubleOrNull()
        val sMeas = summMeasStr.toDoubleOrNull()
        diag14 = IndividualSportScale.calculateCombinedScore14(termType, dMeas, sMeas, config)
        if (dMeas != null && sMeas != null) {
            prog6 = IndividualSportScale.calculateProgressionScore6(termType, dMeas, sMeas, config)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "تقويم ${termType.individualSportAr}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "التلميذ: ${student.fullName} (رقم ${student.regNumber})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Score Preview Card
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
                        Column {
                            Text(
                                text = "مجموع النشاط الفردي: ${String.format(java.util.Locale.US, "%.1f", totalIndividual)} / 20",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            val impText = IndividualSportScale.formatImprovement(
                                termType,
                                diagMeasStr.toDoubleOrNull(),
                                summMeasStr.toDoubleOrNull(),
                                config
                            )
                            Text(
                                text = "التطور المحقق: $impText",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (totalIndividual >= 10.0) ActionPositive else ActionNegative
                        ) {
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.1f", totalIndividual)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Field 1: Diagnostic Measurement (التشخيص)
                OutlinedTextField(
                    value = diagMeasStr,
                    onValueChange = {
                        diagMeasStr = it
                        val dVal = it.toDoubleOrNull()
                        val sVal = summMeasStr.toDoubleOrNull()
                        if (dVal != null || sVal != null) {
                            diag14 = IndividualSportScale.calculateCombinedScore14(termType, dVal, sVal, config)
                        }
                        if (dVal != null && sVal != null) {
                            prog6 = IndividualSportScale.calculateProgressionScore6(termType, dVal, sVal, config)
                        }
                    },
                    label = { Text("إنجاز التقويم التشخيصي (${termType.individualUnit})") },
                    placeholder = {
                        Text(if (termType == TermType.TERM_1) "مثال: 7 ثوانٍ" else "مثال: 4.00 م")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_diag_meas"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Field 2: Summative Measurement (التحصيلي)
                OutlinedTextField(
                    value = summMeasStr,
                    onValueChange = {
                        summMeasStr = it
                        val sVal = it.toDoubleOrNull()
                        val dVal = diagMeasStr.toDoubleOrNull()
                        if (dVal != null || sVal != null) {
                            diag14 = IndividualSportScale.calculateCombinedScore14(termType, dVal, sVal, config)
                        }
                        if (dVal != null && sVal != null) {
                            prog6 = IndividualSportScale.calculateProgressionScore6(termType, dVal, sVal, config)
                        }
                    },
                    label = { Text("إنجاز التقويم التحصيلي (${termType.individualUnit})") },
                    placeholder = {
                        Text(if (termType == TermType.TERM_1) "مثال: 8 ثوانٍ" else "مثال: 4.50 م")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_summ_meas"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Formula Preview Surface
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(
                            text = "📐 جمع القياسين وقسمتهما على اثنين (لحساب الـ 14ن):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SportBlue
                        )
                        val avgDetail = IndividualSportScale.formatAverageDetail(
                            termType,
                            diagMeasStr.toDoubleOrNull(),
                            summMeasStr.toDoubleOrNull()
                        )
                        Text(
                            text = avgDetail,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { applyScaleAuto() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_auto_calc"),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("حساب آلي بالمقياس", fontSize = 11.sp)
                    }

                    when (termType) {
                        TermType.TERM_1 -> {
                            OutlinedButton(
                                onClick = {
                                    // تحسن إيجابي: 8ث تشخيص و 7ث تحصيلي -> تحسن 1ث (2ن) + متوسط 7.5ث (11.5ن) = 13.5/20
                                    diagMeasStr = "8.0"
                                    summMeasStr = "7.0"
                                    applyScaleAuto()
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text("تحسن (8ث➔7ث)", fontSize = 10.sp, color = ActionPositive)
                            }

                            OutlinedButton(
                                onClick = {
                                    // تراجع سلبي: 7ث تشخيص و 8ث تحصيلي -> تراجع (0ن للتطور) + متوسط 7.5ث (11.5ن) = 11.5/20
                                    diagMeasStr = "7.0"
                                    summMeasStr = "8.0"
                                    applyScaleAuto()
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text("تراجع (7ث➔8ث)", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                            }

                            OutlinedButton(
                                onClick = {
                                    diagMeasStr = "5.5"
                                    summMeasStr = "2.5"
                                    applyScaleAuto()
                                },
                                modifier = Modifier.weight(0.9f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text("كاملة (20ن)", fontSize = 10.sp, color = SportBlue)
                            }
                        }
                        TermType.TERM_2 -> {
                            OutlinedButton(
                                onClick = {
                                    // تحسن إيجابي بالمتر في الوثب الطويل
                                    diagMeasStr = "3.50"
                                    summMeasStr = "4.00"
                                    applyScaleAuto()
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text("تحسن (3.5م➔4م)", fontSize = 10.sp, color = ActionPositive)
                            }

                            OutlinedButton(
                                onClick = {
                                    // تراجع سلبي بالمتر
                                    diagMeasStr = "4.00"
                                    summMeasStr = "3.50"
                                    applyScaleAuto()
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text("تراجع (4م➔3.5م)", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                            }

                            OutlinedButton(
                                onClick = {
                                    diagMeasStr = "4.50"
                                    summMeasStr = "5.25"
                                    applyScaleAuto()
                                },
                                modifier = Modifier.weight(0.9f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text("كاملة (20ن)", fontSize = 10.sp, color = SportBlue)
                            }
                        }
                        TermType.TERM_3 -> {
                            OutlinedButton(
                                onClick = {
                                    // تحسن إيجابي بالمتر في دفع الجلة
                                    diagMeasStr = "6.00"
                                    summMeasStr = "7.00"
                                    applyScaleAuto()
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text("تحسن (6م➔7م)", fontSize = 10.sp, color = ActionPositive)
                            }

                            OutlinedButton(
                                onClick = {
                                    // تراجع سلبي بالمتر
                                    diagMeasStr = "7.00"
                                    summMeasStr = "6.00"
                                    applyScaleAuto()
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text("تراجع (7م➔6م)", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                            }

                            OutlinedButton(
                                onClick = {
                                    diagMeasStr = "8.00"
                                    summMeasStr = "9.50"
                                    applyScaleAuto()
                                },
                                modifier = Modifier.weight(0.9f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text("كاملة (20ن)", fontSize = 10.sp, color = SportBlue)
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Text(
                    text = "⚙️ التعديل اليدوي للعلامات (في الحالات الخاصة):",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Diagnostic / Summative Slider out of 14
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1. علامة الاختبار من المتوسط (من 14):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportBlue
                    )
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", diag14)} / 14",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportBlue
                    )
                }
                Slider(
                    value = diag14.toFloat(),
                    onValueChange = { diag14 = String.format(java.util.Locale.US, "%.1f", it).toDouble() },
                    valueRange = 0f..14f,
                    steps = 27
                )

                // Progression Slider out of 6
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. علامة التطور الحاصل (من 6):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportAmber
                    )
                    Text(
                        text = "+${String.format(java.util.Locale.US, "%.1f", prog6)} / 6",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportAmber
                    )
                }
                Slider(
                    value = prog6.toFloat(),
                    onValueChange = { prog6 = String.format(java.util.Locale.US, "%.1f", it).toDouble() },
                    valueRange = 0f..6f,
                    steps = 11
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dMeas = diagMeasStr.toDoubleOrNull()
                    val sMeas = summMeasStr.toDoubleOrNull()
                    val updated = initialAss.copy(
                        individualDiagnosticScore14 = diag14,
                        individualProgressionScore6 = prog6,
                        diagnosticMeasurement = dMeas,
                        summativeMeasurement = sMeas,
                        individualMeasurement = sMeas ?: dMeas
                    )
                    onSave(updated)
                },
                modifier = Modifier.testTag("save_diag_btn")
            ) {
                Text("حفظ العلامات")
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
private fun ScaleSettingsDialog(
    currentConfig: SportScaleConfig,
    termType: TermType,
    onDismiss: () -> Unit,
    onSave: (SportScaleConfig) -> Unit
) {
    var val1Str by remember {
        mutableStateOf(
            when (termType) {
                TermType.TERM_1 -> currentConfig.sprintBaseTimeSec.toString()
                TermType.TERM_2 -> currentConfig.jumpBaseDistMeters.toString()
                TermType.TERM_3 -> currentConfig.shotPutBaseDistMeters.toString()
            }
        )
    }
    var val2Str by remember {
        mutableStateOf(
            when (termType) {
                TermType.TERM_1 -> currentConfig.sprintSecPerMinusPoint.toString()
                TermType.TERM_2 -> currentConfig.jumpDistPerMinusPoint.toString()
                TermType.TERM_3 -> currentConfig.shotPutDistPerMinusPoint.toString()
            }
        )
    }
    var val3Str by remember {
        mutableStateOf(
            when (termType) {
                TermType.TERM_1 -> currentConfig.sprintSecPerProgressionPoint.toString()
                TermType.TERM_2 -> currentConfig.jumpDistPerProgressionPoint.toString()
                TermType.TERM_3 -> currentConfig.shotPutDistPerProgressionPoint.toString()
            }
        )
    }

    val (label1, default1) = when (termType) {
        TermType.TERM_1 -> Pair("زمن العلامة الكاملة 14/14 (ثوانٍ)", "5.0")
        TermType.TERM_2 -> Pair("مسافة العلامة الكاملة 14/14 بالمتر (الوثب)", "4.50")
        TermType.TERM_3 -> Pair("مسافة العلامة الكاملة 14/14 بالمتر (دفع الجلة)", "8.00")
    }

    val (label2, default2) = when (termType) {
        TermType.TERM_1 -> Pair("معدل خصم النقاط (ثانية لكل 1 نقطة نقص)", "1.0")
        TermType.TERM_2 -> Pair("معدل خصم النقاط بالمتر (لكل 1 نقطة نقص)", "0.25")
        TermType.TERM_3 -> Pair("معدل خصم النقاط بالمتر (لكل 1 نقطة نقص)", "0.50")
    }

    val (label3, default3) = when (termType) {
        TermType.TERM_1 -> Pair("معدل التطور الإيجابي (ثانية لكل 1 نقطة تطور)", "0.5")
        TermType.TERM_2 -> Pair("معدل التطور الإيجابي بالمتر (لكل 1 نقطة تطور)", "0.125")
        TermType.TERM_3 -> Pair("معدل التطور الإيجابي بالمتر (لكل 1 نقطة تطور)", "0.25")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تعديل مقياس ${termType.individualSportAr} يدوياً", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "يمكنك تعديل معايير المقياس (${if (termType == TermType.TERM_1) "بالثواني" else "بالمتر"}) لحساب علامة النشاط الفردي حسب ما تراه مناسباً:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = val1Str,
                    onValueChange = { val1Str = it },
                    label = { Text(label1) },
                    supportingText = { Text("الافتراضي: $default1 ${termType.individualUnit}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = val2Str,
                    onValueChange = { val2Str = it },
                    label = { Text(label2) },
                    supportingText = { Text("الافتراضي: $default2 ${termType.individualUnit}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = val3Str,
                    onValueChange = { val3Str = it },
                    label = { Text(label3) },
                    supportingText = { Text("الافتراضي: $default3 ${termType.individualUnit}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = {
                        val1Str = default1
                        val2Str = default2
                        val3Str = default3
                    },
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("استعادة المقياس الافتراضي", fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = when (termType) {
                        TermType.TERM_1 -> currentConfig.copy(
                            sprintBaseTimeSec = val1Str.toDoubleOrNull() ?: 5.0,
                            sprintSecPerMinusPoint = val2Str.toDoubleOrNull() ?: 1.0,
                            sprintSecPerProgressionPoint = val3Str.toDoubleOrNull() ?: 0.5
                        )
                        TermType.TERM_2 -> currentConfig.copy(
                            jumpBaseDistMeters = val1Str.toDoubleOrNull() ?: 4.50,
                            jumpDistPerMinusPoint = val2Str.toDoubleOrNull() ?: 0.25,
                            jumpDistPerProgressionPoint = val3Str.toDoubleOrNull() ?: 0.125
                        )
                        TermType.TERM_3 -> currentConfig.copy(
                            shotPutBaseDistMeters = val1Str.toDoubleOrNull() ?: 8.00,
                            shotPutDistPerMinusPoint = val2Str.toDoubleOrNull() ?: 0.50,
                            shotPutDistPerProgressionPoint = val3Str.toDoubleOrNull() ?: 0.25
                        )
                    }
                    onSave(updated)
                }
            ) {
                Text("حفظ المقياس")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
