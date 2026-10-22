package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsHandball
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassRoom
import com.example.data.model.SportCriteriaDefinitions
import com.example.data.model.TermType
import com.example.ui.theme.ActionPositive
import com.example.ui.theme.SportAmber
import com.example.ui.theme.SportBlue
import com.example.ui.theme.SportTeal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChangeSportsDialog(
    currentClass: ClassRoom,
    initialTerm: TermType,
    onDismiss: () -> Unit,
    onSave: (term: TermType, individualSport: String, collectiveSport: String, applyToAll: Boolean, applyToSameCycle: Boolean) -> Unit
) {
    var selectedTerm by remember { mutableStateOf(initialTerm) }

    val currentInd = currentClass.getIndividualSport(selectedTerm)
    val currentCol = currentClass.getCollectiveSport(selectedTerm)

    var individualSport by remember(selectedTerm) { mutableStateOf(currentInd) }
    var collectiveSport by remember(selectedTerm) { mutableStateOf(currentCol) }

    // 0: هذا القسم فقط, 1: جميع أقسام هذا الطور, 2: جميع الأقسام بالمؤسسة
    var scopeOption by remember { mutableIntStateOf(0) }

    // Preset school sports
    val popularCollectiveSports = listOf(
        "كرة اليد",
        "كرة السلة",
        "كرة الطائرة",
        "كرة القدم",
        "الركبي المدرسي",
        "الجمباز والعروض الحركية"
    )

    val popularIndividualSports = listOf(
        "الجري السريع 60م",
        "الجري السريع 100م",
        "الوثب الطويل",
        "دفع الجلة",
        "الوثب العالي",
        "جري التحمل 800م",
        "جري المداومة 1000م",
        "رمي الجافلو (الرمح)",
        "الجمباز الفني الأرضي"
    )

    val criteriaPreview = remember(collectiveSport) {
        SportCriteriaDefinitions.getCriteriaForSport(collectiveSport)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SportBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = SportBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "تغيير النشاط الرياضي ومعاييره",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        fontSize = 17.sp
                    )
                    Text(
                        text = "${currentClass.name} • ${currentClass.cycle}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Term Selector Chips
                Text("اختر الفصل الدراسي للتعديل:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TermType.values().forEach { term ->
                        val isSelected = (term == selectedTerm)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTerm = term },
                            label = { Text(term.labelAr, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("select_term_in_sports_dialog_${term.termNumber}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (term) {
                                    TermType.TERM_1 -> SportBlue
                                    TermType.TERM_2 -> SportAmber
                                    TermType.TERM_3 -> SportTeal
                                },
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // 2. Collective Sport Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sports, contentDescription = null, tint = SportBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("النشاط الجماعي المبرمج (20ن):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            popularCollectiveSports.forEach { sport ->
                                val isSelected = collectiveSport.trim() == sport
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { collectiveSport = sport },
                                    label = { Text(sport, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SportBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = collectiveSport,
                            onValueChange = { collectiveSport = it },
                            label = { Text("اسم النشاط الجماعي (أو اكتب نشاطاً مخصصاً)") },
                            modifier = Modifier.fillMaxWidth().testTag("collective_sport_input"),
                            singleLine = true
                        )

                        // Real-time criteria preview
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "معايير التقويم المعتمدة لـ $collectiveSport (5 × 4ن = 20ن):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SportBlue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                criteriaPreview.forEachIndexed { idx, crit ->
                                    Text(
                                        text = "${idx + 1}. ${crit.labelAr} (4ن): ${crit.descriptionAr}",
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Individual Sport Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = SportTeal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("النشاط الفردي المبرمج (14ن تشخيص + 6ن تطور):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            popularIndividualSports.forEach { sport ->
                                val isSelected = individualSport.trim() == sport
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { individualSport = sport },
                                    label = { Text(sport, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SportTeal,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = individualSport,
                            onValueChange = { individualSport = it },
                            label = { Text("اسم النشاط الفردي (أو اكتب نشاطاً مخصصاً)") },
                            modifier = Modifier.fillMaxWidth().testTag("individual_sport_input"),
                            singleLine = true
                        )
                    }
                }

                // 4. Scope Selection
                Text("نطاق تطبيق هذا التعديل:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { scopeOption = 0 }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = scopeOption == 0,
                            onClick = { scopeOption = 0 },
                            modifier = Modifier.testTag("radio_scope_class_only")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تطبيق على هذا القسم فقط (${currentClass.name})",
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { scopeOption = 1 }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = scopeOption == 1,
                            onClick = { scopeOption = 1 },
                            modifier = Modifier.testTag("radio_scope_same_cycle")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تطبيق على جميع أقسام ${currentClass.cycle}",
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { scopeOption = 2 }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = scopeOption == 2,
                            onClick = { scopeOption = 2 },
                            modifier = Modifier.testTag("radio_scope_all_classes")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تطبيق على كافة الأقسام بالمؤسسة",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalInd = individualSport.trim().ifBlank { "الجري السريع 60م" }
                    val finalCol = collectiveSport.trim().ifBlank { "كرة اليد" }
                    onSave(
                        selectedTerm,
                        finalInd,
                        finalCol,
                        scopeOption == 2, // apply to all
                        scopeOption == 1  // apply to same cycle
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SportBlue),
                modifier = Modifier.testTag("confirm_save_sports_btn")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("حفظ وتطبيق النشاط", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
