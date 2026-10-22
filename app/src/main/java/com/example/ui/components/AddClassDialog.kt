package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SportBlue
import com.example.ui.theme.SportTeal

data class SecondaryStream(
    val code: String,
    val nameAr: String,
    val years: List<Int> // 1 for 1st year, 2 for 2nd year, 3 for 3rd year
)

val SecondaryStreamsList = listOf(
    SecondaryStream("ج م ع ت", "جذع مشترك علوم وتكنولوجيا", listOf(1)),
    SecondaryStream("ج م آ", "جذع مشترك آداب", listOf(1)),
    SecondaryStream("ع ت", "علوم تجريبية", listOf(2, 3)),
    SecondaryStream("ت ر", "تقني رياضي", listOf(2, 3)),
    SecondaryStream("ر", "رياضيات", listOf(2, 3)),
    SecondaryStream("ت إ", "تسيير واقتصاد", listOf(2, 3)),
    SecondaryStream("آ ف", "آداب وفلسفة", listOf(2, 3)),
    SecondaryStream("ل أ", "لغات أجنبية", listOf(2, 3))
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddClassDialog(
    onDismiss: () -> Unit,
    onAddClass: (name: String, level: String, academicYear: String, cycle: String) -> Unit
) {
    // الطور: متوسط أو ثانوي
    var selectedCycle by remember { mutableStateOf("الطور الثانوي") } // "الطور المتوسط" أو "الطور الثانوي"
    
    // السنة الدراسية يدوياً (الافتراض 2026/2027 مع إمكانية التعديل اليدوي الحر)
    var academicYear by remember { mutableStateOf("2026/2027") }

    // خيارات الطور الثانوي
    var selectedYearSecondary by remember { mutableIntStateOf(1) } // 1, 2, 3
    var selectedStream by remember { mutableStateOf(SecondaryStreamsList[0]) }
    var selectedGroupNum by remember { mutableIntStateOf(1) }

    // خيارات الطور المتوسط
    var selectedYearMiddle by remember { mutableIntStateOf(1) } // 1, 2, 3, 4
    var selectedMiddleGroupNum by remember { mutableIntStateOf(1) }

    fun buildDefaultName(): String {
        return if (selectedCycle == "الطور المتوسط") {
            "$selectedYearMiddle م $selectedMiddleGroupNum"
        } else {
            "$selectedYearSecondary ${selectedStream.code} $selectedGroupNum"
        }
    }

    fun buildDefaultLevel(): String {
        return if (selectedCycle == "الطور المتوسط") {
            when (selectedYearMiddle) {
                1 -> "السنة الأولى متوسط (CEM)"
                2 -> "السنة الثانية متوسط (CEM)"
                3 -> "السنة الثالثة متوسط (CEM)"
                else -> "السنة الرابعة متوسط (شهادة BEM)"
            }
        } else {
            val yearStr = when (selectedYearSecondary) {
                1 -> "السنة الأولى ثانوي"
                2 -> "السنة الثانية ثانوي"
                else -> "السنة الثالثة ثانوي (بكالوريا)"
            }
            "$yearStr (${selectedStream.nameAr})"
        }
    }

    var className by remember { mutableStateOf(buildDefaultName()) }
    var classLevel by remember { mutableStateOf(buildDefaultLevel()) }
    var error by remember { mutableStateOf<String?>(null) }

    fun syncNames() {
        className = buildDefaultName()
        classLevel = buildDefaultLevel()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("إضافة قسم أو فوج جديد", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("الطور المتوسط والطور الثانوي • إدخال يدوي", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. اختيار الطور التعليمي (المتوسط أو الثانوي)
                Text(
                    text = "1. الطور التعليمي:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("الطور المتوسط (CEM)", "الطور الثانوي (Lycée)").forEach { cycleOption ->
                        val isSelected = (selectedCycle == "الطور المتوسط" && cycleOption.contains("المتوسط")) ||
                                (selectedCycle == "الطور الثانوي" && cycleOption.contains("الثانوي"))
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCycle = if (cycleOption.contains("المتوسط")) "الطور المتوسط" else "الطور الثانوي"
                                syncNames()
                            },
                            label = { Text(cycleOption, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. السنة الدراسية يدوياً
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "2. السنة الدراسية (يدوياً):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                
                // اختيارات سريعة للسنة الدراسية
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("2026/2027", "2025/2026", "2024/2025").forEach { yr ->
                        FilterChip(
                            selected = (academicYear == yr),
                            onClick = { academicYear = yr },
                            label = { Text(yr, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SportTeal,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = academicYear,
                    onValueChange = { academicYear = it },
                    label = { Text("أدخل السنة الدراسية يدوياً") },
                    placeholder = { Text("مثال: 2026/2027") },
                    modifier = Modifier.fillMaxWidth().testTag("manual_academic_year_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // إذا كان الطور متوسط:
                if (selectedCycle == "الطور المتوسط") {
                    Text(
                        text = "3. مستوى التعليم المتوسط:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            1 to "1 متوسط",
                            2 to "2 متوسط",
                            3 to "3 متوسط",
                            4 to "4 متوسط (BEM)"
                        ).forEach { (yr, label) ->
                            FilterChip(
                                selected = selectedYearMiddle == yr,
                                onClick = {
                                    selectedYearMiddle = yr
                                    syncNames()
                                },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (selectedYearMiddle == yr) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "4. رقم الفوج (المتوسط):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..6).forEach { num ->
                            FilterChip(
                                selected = selectedMiddleGroupNum == num,
                                onClick = {
                                    selectedMiddleGroupNum = num
                                    syncNames()
                                },
                                label = { Text("فوج $num", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SportBlue,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    // الطور الثانوي:
                    Text(
                        text = "3. المستوى الدراسي بالثانوي:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            1 to "1 ثانوي",
                            2 to "2 ثانوي",
                            3 to "3 ثانوي (بكالوريا)"
                        ).forEach { (yr, label) ->
                            FilterChip(
                                selected = selectedYearSecondary == yr,
                                onClick = {
                                    selectedYearSecondary = yr
                                    val newStreams = SecondaryStreamsList.filter { yr in it.years }
                                    selectedStream = if (selectedStream in newStreams) selectedStream else newStreams.first()
                                    syncNames()
                                },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (selectedYearSecondary == yr) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "4. الشعبة والتخصص:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val availableStreams = SecondaryStreamsList.filter { selectedYearSecondary in it.years }
                        availableStreams.forEach { stream ->
                            val isSelected = selectedStream.code == stream.code
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedStream = stream
                                    syncNames()
                                },
                                label = { Text("${stream.code} (${stream.nameAr})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SportTeal,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "5. رقم الفوج:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..5).forEach { num ->
                            FilterChip(
                                selected = selectedGroupNum == num,
                                onClick = {
                                    selectedGroupNum = num
                                    syncNames()
                                },
                                label = { Text("فوج $num", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SportBlue,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // حقل اسم القسم النهائي
                OutlinedTextField(
                    value = className,
                    onValueChange = {
                        className = it
                        error = null
                    },
                    label = { Text("اسم القسم أو الفوج") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_class_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // حقل المستوى والشعبة النهائي
                OutlinedTextField(
                    value = classLevel,
                    onValueChange = { classLevel = it },
                    label = { Text("المستوى التعليمي والشعبة") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_class_level_input"),
                    singleLine = true
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (className.trim().isEmpty()) {
                        error = "يرجى كتابة اسم القسم"
                        return@Button
                    }
                    onAddClass(
                        className.trim(),
                        classLevel.trim(),
                        if (academicYear.trim().isNotEmpty()) academicYear.trim() else "2026/2027",
                        selectedCycle
                    )
                },
                modifier = Modifier.testTag("confirm_add_class_btn")
            ) {
                Text("إضافة القسم")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
