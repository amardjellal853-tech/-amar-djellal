package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassRoom
import com.example.ui.theme.SportTeal

@Composable
fun ClassSelectorHeader(
    classes: List<ClassRoom>,
    selectedClassId: String,
    onSelectClass: (String) -> Unit,
    onAddClassClicked: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onUpdateAcademicYear: ((classId: String, newYear: String) -> Unit)? = null,
    onUpdateAllAcademicYears: ((newYear: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showEditYearDialog by remember { mutableStateOf(false) }

    val currentClass = classes.find { it.id == selectedClassId } ?: classes.firstOrNull()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Class chips row & Search button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                classes.forEach { classRoom ->
                    val isSelected = classRoom.id == selectedClassId
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectClass(classRoom.id) },
                        label = { Text(classRoom.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("class_chip_${classRoom.id}")
                    )
                }

                // Add class button
                FilterChip(
                    selected = false,
                    onClick = onAddClassClicked,
                    label = { Text("إضافة قسم +") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة قسم",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("add_class_chip")
                )

                Spacer(modifier = Modifier.weight(1f, fill = false))

                IconButton(
                    onClick = {
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) onSearchQueryChange("")
                    },
                    modifier = Modifier.testTag("toggle_search_btn")
                ) {
                    Icon(
                        imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "البحث",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Info row: Level + Academic Year badge (editable manually)
            currentClass?.let { cls ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${cls.level} (${cls.cycle})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )

                    // Academic year pill with edit icon
                    Surface(
                        onClick = { showEditYearDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.testTag("edit_academic_year_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "السنة: ${cls.academicYear}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "تعديل السنة الدراسية",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            if (isSearchExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_search_input"),
                    placeholder = { Text("بحث عن تلميذ بالاسم أو رقم التسجيل...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "مسح")
                            }
                        }
                    },
                    singleLine = true
                )
            }
        }
    }

    if (showEditYearDialog && currentClass != null) {
        var editedYear by remember { mutableStateOf(currentClass.academicYear) }
        var applyToAll by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showEditYearDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تعديل السنة الدراسية يدوياً", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "القسم الحالي: ${currentClass.name}",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("خيارات سريعة:", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("2026/2027", "2025/2026", "2024/2025").forEach { yr ->
                            FilterChip(
                                selected = (editedYear == yr),
                                onClick = { editedYear = yr },
                                label = { Text(yr, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SportTeal,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editedYear,
                        onValueChange = { editedYear = it },
                        label = { Text("أدخل السنة الدراسية يدوياً") },
                        placeholder = { Text("مثال: 2026/2027") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_academic_year_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = applyToAll,
                            onClick = { applyToAll = true },
                            label = { Text("تطبيق على جميع الأقسام", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = !applyToAll,
                            onClick = { applyToAll = false },
                            label = { Text("لهذا القسم فقط", fontSize = 11.sp) }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = editedYear.trim()
                        if (trimmed.isNotEmpty()) {
                            if (applyToAll) {
                                onUpdateAllAcademicYears?.invoke(trimmed)
                            } else {
                                onUpdateAcademicYear?.invoke(currentClass.id, trimmed)
                            }
                        }
                        showEditYearDialog = false
                    }
                ) {
                    Text("حفظ السنة الدراسية")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditYearDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
