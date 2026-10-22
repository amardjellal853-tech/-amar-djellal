package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.ClassRoom
import com.example.data.model.CollectiveMatchAction
import com.example.data.model.ContinuousAssessment
import com.example.data.model.CriterionEvaluation
import com.example.data.model.DailyLogbookEntry
import com.example.data.model.DiagnosticEvaluation
import com.example.data.model.Gender
import com.example.data.model.HandballAction
import com.example.data.model.ImportedStudentResult
import com.example.data.model.SportScaleConfig
import com.example.data.model.Student
import com.example.data.model.TeacherProfile
import com.example.data.model.TermAssessment
import com.example.data.model.TermType
import com.example.data.repository.EpsRepository
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EpsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EpsRepository(application)

    val classes: StateFlow<List<ClassRoom>> = repository.classes
    val allStudents: StateFlow<List<Student>> = repository.students
    val termAssessments: StateFlow<Map<String, TermAssessment>> = repository.termAssessments
    val collectiveActions: StateFlow<Map<String, CollectiveMatchAction>> = repository.collectiveActions
    val attendances: StateFlow<List<AttendanceRecord>> = repository.attendances

    // Backward compatibility
    val diagnostics: StateFlow<Map<String, DiagnosticEvaluation>> = repository.diagnostics
    val handballActions: StateFlow<Map<String, HandballAction>> = repository.handballActions
    val criteria: StateFlow<Map<String, CriterionEvaluation>> = repository.criteria
    val continuousAssessments: StateFlow<Map<String, ContinuousAssessment>> = repository.continuousAssessments

    // معلومات الأستاذ والكراس اليومي
    val teacherProfile: StateFlow<TeacherProfile> = repository.teacherProfile
    val logbookEntries: StateFlow<List<DailyLogbookEntry>> = repository.logbookEntries

    private val _selectedClassId = MutableStateFlow("c1")
    val selectedClassId: StateFlow<String> = _selectedClassId.asStateFlow()

    // الفصول الثلاثة المعتمدة
    private val _selectedTerm = MutableStateFlow(TermType.TERM_1)
    val selectedTerm: StateFlow<TermType> = _selectedTerm.asStateFlow()

    // إعدادات مقياس التنقيط القابلة للتعديل
    private val _scaleConfig = MutableStateFlow(SportScaleConfig())
    val scaleConfig: StateFlow<SportScaleConfig> = _scaleConfig.asStateFlow()

    fun updateScaleConfig(newConfig: SportScaleConfig) {
        _scaleConfig.value = newConfig
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    )
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val currentClassStudents: StateFlow<List<Student>> = combine(
        allStudents,
        _selectedClassId,
        _searchQuery
    ) { students, classId, query ->
        students.filter { it.classId == classId }
            .filter {
                query.isBlank() || it.fullName.contains(query, ignoreCase = true) ||
                        it.regNumber.toString().contains(query)
            }
            .sortedBy { it.regNumber }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val selectedClass: StateFlow<ClassRoom?> = combine(
        classes,
        _selectedClassId
    ) { classList, id ->
        classList.firstOrNull { it.id == id } ?: classList.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun selectClass(classId: String) {
        _selectedClassId.value = classId
    }

    fun selectTerm(term: TermType) {
        _selectedTerm.value = term
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAttendanceDate(date: String) {
        _selectedDate.value = date
    }

    fun updateDate(date: String) {
        _selectedDate.value = date
    }

    fun addOrUpdateStudent(student: Student) {
        repository.addOrUpdateStudent(student)
    }

    fun deleteStudent(studentId: String) {
        repository.deleteStudent(studentId)
    }

    fun addClass(name: String, level: String, academicYear: String = "2026/2027", cycle: String = "الطور الثانوي") {
        repository.addClass(name, level, academicYear, cycle)
    }

    fun updateClassAcademicYear(classId: String, academicYear: String) {
        repository.updateClassAcademicYear(classId, academicYear)
    }

    fun updateAllClassesAcademicYear(academicYear: String) {
        repository.updateAllClassesAcademicYear(academicYear)
    }

    fun updateClassSports(
        classId: String,
        term: TermType,
        individualSport: String,
        collectiveSport: String,
        applyToAllClasses: Boolean = false,
        applyToSameCycle: Boolean = false
    ) {
        repository.updateClassSports(classId, term, individualSport, collectiveSport, applyToAllClasses, applyToSameCycle)
    }

    fun getIndividualSport(classRoom: ClassRoom?, term: TermType): String {
        return classRoom?.getIndividualSport(term) ?: term.individualSportAr
    }

    fun getCollectiveSport(classRoom: ClassRoom?, term: TermType): String {
        return classRoom?.getCollectiveSport(term) ?: term.collectiveSportAr
    }

    fun getCriteriaForCurrentClass(term: TermType): List<com.example.data.model.SportCriterionInfo> {
        return com.example.data.model.SportCriteriaDefinitions.getCriteriaForClassAndTerm(selectedClass.value, term)
    }

    fun resetStudentResults(studentId: String, termType: TermType? = null) {
        repository.resetStudentResults(studentId, termType)
    }

    fun deleteClass(classId: String) {
        repository.deleteClass(classId)
        if (_selectedClassId.value == classId) {
            _selectedClassId.value = classes.value.firstOrNull()?.id ?: "c1"
        }
    }

    fun importStudentsAndResults(
        classId: String,
        term: TermType,
        importedList: List<ImportedStudentResult>,
        importStudentsNames: Boolean = true,
        updateContinuousScore: Boolean = true,
        updateTestScore: Boolean = true,
        updateExamScore: Boolean = true
    ): Int {
        return repository.importStudentsAndResults(
            classId = classId,
            term = term,
            importedList = importedList,
            importStudentsNames = importStudentsNames,
            updateContinuousScore = updateContinuousScore,
            updateTestScore = updateTestScore,
            updateExamScore = updateExamScore
        )
    }

    // إدارة التقييمات الفصلية
    fun getAssessmentForStudentAndTerm(studentId: String, term: TermType): TermAssessment {
        val key = "${studentId}_${term.termNumber}"
        return termAssessments.value[key] ?: TermAssessment(studentId = studentId, termType = term)
    }

    fun updateTermAssessment(assessment: TermAssessment) {
        repository.updateTermAssessment(assessment)
    }

    fun getCollectiveActionForStudentAndTerm(studentId: String, term: TermType): CollectiveMatchAction {
        val key = "${studentId}_${term.termNumber}"
        return collectiveActions.value[key] ?: CollectiveMatchAction(studentId = studentId, termType = term)
    }

    fun modifyCollectiveAction(studentId: String, isPositive: Boolean, actionType: String) {
        repository.modifyCollectiveAction(studentId, _selectedTerm.value, isPositive, actionType)
    }

    fun setStudentCollectiveTeam(studentId: String, newTeam: String) {
        val key = "${studentId}_${_selectedTerm.value.termNumber}"
        val current = collectiveActions.value[key] ?: CollectiveMatchAction(studentId = studentId, termType = _selectedTerm.value)
        repository.updateCollectiveAction(current.copy(team = newTeam))
    }

    fun resetCollectiveForCurrentClassAndTerm() {
        repository.resetCollectiveForClass(_selectedClassId.value, _selectedTerm.value)
    }

    // التوافقية
    fun modifyHandballAction(studentId: String, deltaPositive: Boolean, actionType: String) {
        repository.modifyHandballActionField(studentId, deltaPositive, actionType)
    }

    fun setStudentTeam(studentId: String, newTeam: String) {
        setStudentCollectiveTeam(studentId, newTeam)
    }

    fun resetHandballForCurrentClass() {
        resetCollectiveForCurrentClassAndTerm()
    }

    fun updateDiagnostic(diag: DiagnosticEvaluation) {
        repository.updateDiagnostic(diag)
    }

    fun updateCriteria(criterion: CriterionEvaluation) {
        repository.updateCriteria(criterion)
    }

    fun updateContinuousAssessment(assessment: ContinuousAssessment) {
        repository.updateContinuousAssessment(assessment)
    }

    fun updateAttendanceRecord(record: AttendanceRecord) {
        repository.recordAttendance(record)
    }

    fun setStudentAttendance(studentId: String, status: AttendanceStatus) {
        val currentDate = _selectedDate.value
        val existing = attendances.value.firstOrNull { it.studentId == studentId && it.date == currentDate }
        val record = existing?.copy(status = status) ?: AttendanceRecord(
            id = "att_${studentId}_$currentDate",
            studentId = studentId,
            date = currentDate,
            status = status
        )
        repository.recordAttendance(record)
    }

    fun setStudentDailyCriterion(
        studentId: String,
        hasSportswear: Boolean? = null,
        goodBehavior: Boolean? = null,
        individualTaskCompleted: Boolean? = null,
        teamworkContribution: Boolean? = null,
        creativityDemonstrated: Boolean? = null
    ) {
        val currentDate = _selectedDate.value
        val existing = attendances.value.firstOrNull { it.studentId == studentId && it.date == currentDate }
            ?: AttendanceRecord(
                id = "att_${studentId}_$currentDate",
                studentId = studentId,
                date = currentDate,
                status = AttendanceStatus.PRESENT
            )
        val updated = existing.copy(
            hasSportswear = hasSportswear ?: existing.hasSportswear,
            goodBehavior = goodBehavior ?: existing.goodBehavior,
            individualTaskCompleted = individualTaskCompleted ?: existing.individualTaskCompleted,
            teamworkContribution = teamworkContribution ?: existing.teamworkContribution,
            creativityDemonstrated = creativityDemonstrated ?: existing.creativityDemonstrated
        )
        repository.recordAttendance(updated)
    }

    /**
     * احتساب وتحديث نقطة التقويم المستمر لجميع تلاميذ القسم آلياً من سجل المتابعة اليومية
     */
    fun recalculateContinuousAssessmentsFromDaily() {
        repository.recalculateContinuousAssessmentsFromDaily(_selectedTerm.value)
    }

    /**
     * حساب علامات التقويم المستمر لطالب معين
     */
    fun getCalculatedContinuousScores(studentId: String) =
        repository.calculateContinuousScoresForStudent(studentId)

    fun resetToDefaults() {
        repository.loadInitialPresetData()
    }

    fun getBackupJson(): String {
        return repository.serializeToJson()
    }

    fun restoreBackup(jsonString: String): Boolean {
        return try {
            repository.parseJson(jsonString)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // حساب المعدل السنوي العام
    fun getStudentAnnualAverage(studentId: String): Double {
        val t1 = termAssessments.value["${studentId}_1"]?.termAverage ?: 0.0
        val t2 = termAssessments.value["${studentId}_2"]?.termAverage ?: 0.0
        val t3 = termAssessments.value["${studentId}_3"]?.termAverage ?: 0.0
        val avg = (t1 + t2 + t3) / 3.0
        return String.format(Locale.US, "%.2f", avg).toDouble()
    }

    // التقارير والتصدير
    fun generateClassReportText(): String {
        val currentClass = selectedClass.value ?: return "لا توجد بيانات"
        val term = _selectedTerm.value
        val students = allStudents.value.filter { it.classId == currentClass.id }.sortedBy { it.regNumber }
        val indSport = currentClass.getIndividualSport(term)
        val colSport = currentClass.getCollectiveSport(term)

        val sb = StringBuilder()
        sb.append("═════════════════════════════════════════════════════════════\n")
        sb.append("   كشف نتائج وتقويم التربية البدنية والرياضية - ${term.labelAr}\n")
        sb.append("═════════════════════════════════════════════════════════════\n")
        sb.append("القسم: ${currentClass.name} | المستوى: ${currentClass.level}\n")
        sb.append("الأنشطة المبرمجة: [فردي: $indSport] | [جماعي: $colSport]\n")
        sb.append("السنة الدراسية: ${currentClass.academicYear} | التاريخ: ${_selectedDate.value}\n")
        sb.append("─────────────────────────────────────────────────────────────\n\n")

        students.forEach { s ->
            val ass = getAssessmentForStudentAndTerm(s.id, term)
            val exemptText = if (s.isMedicallyExempt) " [معفى طبياً]" else ""

            sb.append("${s.regNumber}. ${s.fullName}$exemptText\n")
            sb.append("   • $indSport: ${ass.individualSportTotal20}/20 (تشخيص: ${ass.individualDiagnosticScore14}/14 + تطور: ${ass.individualProgressionScore6}/6)\n")
            sb.append("   • $colSport: ${ass.collectiveSportScore20}/20 (${ass.masteryLevel.labelAr})\n")
            sb.append("   • علامة الاختبار: ${ass.examScore20}/20  [(فردي + جماعي) ÷ 2]\n")
            sb.append("   • علامة الفرض: ${ass.manualTestScore}/20\n")
            sb.append("   • التقويم المستمر: ${ass.continuousAssessmentTotal20}/20 (غياب: ${ass.absenceScore}/4 | لباس: ${ass.sportswearScore}/3 | سلوك: ${ass.behaviorScore}/4 | مهام: ${ass.individualTaskScore}/3 | فريق: ${ass.teamworkScore}/3 | إبداع: ${ass.creativityScore}/3)\n")
            sb.append("   ⭐ معدل ${term.labelAr}: ${ass.termAverage}/20  (${ass.appreciationAr})\n")
            sb.append("   🏆 المعدل السنوي التقديري: ${getStudentAnnualAverage(s.id)}/20\n\n")
        }

        sb.append("─────────────────────────────────────────────────────────────\n")
        val averages = students.filter { !it.isMedicallyExempt }.map { getAssessmentForStudentAndTerm(it.id, term).termAverage }
        val classAvg = if (averages.isNotEmpty()) averages.average() else 0.0
        val passCount = averages.count { it >= 10.0 }
        val passRate = if (averages.isNotEmpty()) (passCount.toDouble() / averages.size * 100) else 0.0

        sb.append("معدل القسم العام: ${String.format(Locale.US, "%.2f", classAvg)}/20\n")
        sb.append("نسبة النجاح: ${String.format(Locale.US, "%.1f", passRate)}%\n")
        sb.append("═════════════════════════════════════════════════════════════\n")
        return sb.toString()
    }

    fun generateCsvReport(): String {
        val currentClass = selectedClass.value ?: return ""
        val term = _selectedTerm.value
        val indSport = currentClass.getIndividualSport(term)
        val colSport = currentClass.getCollectiveSport(term)
        val students = allStudents.value.filter { it.classId == currentClass.id }.sortedBy { it.regNumber }
        val sb = StringBuilder()
        sb.append("الرقم,الاسم واللقب,الجنس,الحالة,تشخيص فردي (14),تطور فردي (6),مجموع الفردي ($indSport),جماعي ($colSport),علامة الاختبار,علامة الفرض,التقويم المستمر,معدل ${term.labelAr},المعدل السنوي العام\n")

        students.forEach { s ->
            val ass = getAssessmentForStudentAndTerm(s.id, term)
            val status = if (s.isMedicallyExempt) "معفى طبياً" else "ممارس"
            val annual = getStudentAnnualAverage(s.id)

            sb.append("${s.regNumber},\"${s.fullName}\",${s.gender.labelAr},$status,${ass.individualDiagnosticScore14},${ass.individualProgressionScore6},${ass.individualSportTotal20},${ass.collectiveSportScore20},${ass.examScore20},${ass.manualTestScore},${ass.continuousAssessmentTotal20},${ass.termAverage},$annual\n")
        }
        return sb.toString()
    }

    fun shareReport(context: Context, isCsv: Boolean) {
        val text = if (isCsv) generateCsvReport() else generateClassReportText()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (isCsv) "text/csv" else "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "كشف تقويم التربية البدنية (${_selectedTerm.value.labelAr}) - ${selectedClass.value?.name}")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة تقرير الفصل"))
    }

    // ==========================================
    // إدارة وتصدير وتحميل معلومات الأستاذ والكراس اليومي
    // ==========================================

    fun updateTeacherProfile(profile: TeacherProfile) {
        repository.updateTeacherProfile(profile)
    }

    fun addOrUpdateLogbookEntry(entry: DailyLogbookEntry) {
        repository.addOrUpdateLogbookEntry(entry)
    }

    fun deleteLogbookEntry(id: String) {
        repository.deleteLogbookEntry(id)
    }

    fun generatePresetLogbookForWeek() {
        repository.generatePresetLogbookForWeek()
    }

    fun getTeacherProfileReportText(): String = repository.generateTeacherProfileReport()

    fun getDailyLogbookReportText(targetDate: String? = null): String = repository.generateDailyLogbookReport(targetDate)

    /**
     * توليد كشف شامل لجميع الأقسام بصيغة CSV موحدة للرقمنة
     */
    fun generateAllClassesCsvReport(): String {
        val allClasses = classes.value
        val term = _selectedTerm.value
        val sb = StringBuilder()
        sb.append("القسم,الطور,السنة الدراسية,الرقم,اللقب والاسم,الجنس,الحالة,نشاط فردي,تشخيص (14),تطور (6),مجموع فردي (20),نشاط جماعي,مجموع جماعي (20),الفرض (20),الاختبار (20),التقويم المستمر (20),معدل ${term.labelAr},المعدل السنوي التقديري\n")

        allClasses.forEach { cls ->
            val indSport = cls.getIndividualSport(term)
            val colSport = cls.getCollectiveSport(term)
            val stList = allStudents.value.filter { it.classId == cls.id }.sortedBy { it.regNumber }
            stList.forEach { s ->
                val ass = getAssessmentForStudentAndTerm(s.id, term)
                val status = if (s.isMedicallyExempt) "معفى طبياً" else "ممارس"
                val annual = getStudentAnnualAverage(s.id)
                sb.append("\"${cls.name}\",\"${cls.level}\",\"${cls.academicYear}\",${s.regNumber},\"${s.fullName}\",${s.gender.labelAr},$status,\"$indSport\",${ass.individualDiagnosticScore14},${ass.individualProgressionScore6},${ass.individualSportTotal20},\"$colSport\",${ass.collectiveSportScore20},${ass.manualTestScore},${ass.examScore20},${ass.continuousAssessmentTotal20},${ass.termAverage},$annual\n")
            }
        }
        return sb.toString()
    }

    /**
     * توليد كشف HTML رسمي كامل ومنسق وجاهز للطباعة المباشرة كـ PDF
     */
    fun generateClassHtmlReport(): String {
        val currentClass = selectedClass.value ?: return ""
        val term = _selectedTerm.value
        val indSport = currentClass.getIndividualSport(term)
        val colSport = currentClass.getCollectiveSport(term)
        val students = allStudents.value.filter { it.classId == currentClass.id }.sortedBy { it.regNumber }
        val prof = teacherProfile.value

        // ترتيب التلاميذ حسب معدل الفصل لتحديد الرتبة
        val rankedList = students.sortedByDescending { getAssessmentForStudentAndTerm(it.id, term).termAverage }
        val rankMap = rankedList.mapIndexed { index, student -> student.id to (index + 1) }.toMap()

        val rows = students.map { s ->
            val ass = getAssessmentForStudentAndTerm(s.id, term)
            com.example.util.HtmlStudentRow(
                regNumber = s.regNumber,
                fullName = s.fullName,
                gender = s.gender.labelAr,
                status = if (s.isMedicallyExempt) "معفى طبياً" else "ممارس",
                isExempt = s.isMedicallyExempt,
                diagScore14 = ass.individualDiagnosticScore14,
                progScore6 = ass.individualProgressionScore6,
                indTotal20 = ass.individualSportTotal20,
                colScore20 = ass.collectiveSportScore20,
                testScore = ass.manualTestScore,
                examScore = ass.examScore20,
                continuousScore = ass.continuousAssessmentTotal20,
                termAverage = ass.termAverage,
                annualAverage = getStudentAnnualAverage(s.id),
                rank = rankMap[s.id] ?: s.regNumber,
                appreciation = ass.appreciationAr
            )
        }

        val nonExemptAverages = rows.filter { !it.isExempt }.map { it.termAverage }
        val classAvg = if (nonExemptAverages.isNotEmpty()) nonExemptAverages.average() else 0.0
        val passCount = nonExemptAverages.count { it >= 10.0 }
        val passRate = if (nonExemptAverages.isNotEmpty()) (passCount.toDouble() / nonExemptAverages.size * 100) else 0.0

        return com.example.util.FileDownloadHelper.generatePrintableHtmlReport(
            className = currentClass.name,
            academicYear = currentClass.academicYear,
            termLabel = term.labelAr,
            indSport = indSport,
            colSport = colSport,
            teacherName = prof.fullName,
            schoolName = prof.schoolName,
            studentsData = rows,
            classAverage = classAvg,
            passRate = passRate
        )
    }

    /**
     * تنزيل كشف نقاط القسم الحالي بصيغة Excel / CSV مباشرة لمجلد التنزيلات
     */
    fun downloadCurrentClassCsv(context: Context) {
        val currentClass = selectedClass.value ?: return
        val content = generateCsvReport()
        val fileName = "كشف_نقاط_${currentClass.name}_${_selectedTerm.value.labelAr}"
        com.example.util.FileDownloadHelper.saveFileToDownloads(
            context = context,
            baseFileName = fileName,
            extension = "csv",
            mimeType = "text/csv",
            content = content,
            prependUtf8Bom = true,
            openAfterSave = true
        )
    }

    /**
     * تنزيل كشف جميع الأقسام دفعة واحدة بصيغة Excel / CSV لمجلد التنزيلات
     */
    fun downloadAllClassesCsv(context: Context) {
        val content = generateAllClassesCsvReport()
        val fileName = "كشف_نقاط_جميع_الأقسام_${_selectedTerm.value.labelAr}"
        com.example.util.FileDownloadHelper.saveFileToDownloads(
            context = context,
            baseFileName = fileName,
            extension = "csv",
            mimeType = "text/csv",
            content = content,
            prependUtf8Bom = true,
            openAfterSave = true
        )
    }

    /**
     * تنزيل التقرير الرسمي بصيغة HTML جاهز للطباعة المباشرة وحفظه كـ PDF
     */
    fun downloadCurrentClassHtml(context: Context, openAfterSave: Boolean = true) {
        val currentClass = selectedClass.value ?: return
        val content = generateClassHtmlReport()
        val fileName = "تقرير_طباعة_رسمي_${currentClass.name}_${_selectedTerm.value.labelAr}"
        com.example.util.FileDownloadHelper.saveFileToDownloads(
            context = context,
            baseFileName = fileName,
            extension = "html",
            mimeType = "text/html",
            content = content,
            prependUtf8Bom = false,
            openAfterSave = openAfterSave
        )
    }

    /**
     * تنزيل ملف النسخة الاحتياطية الكاملة بصيغة JSON إلى مجلد التنزيلات
     */
    fun downloadBackupJsonFile(context: Context) {
        val content = getBackupJson()
        val fileName = "نسخة_احتياطية_كاملة_التربية_البدنية"
        com.example.util.FileDownloadHelper.saveFileToDownloads(
            context = context,
            baseFileName = fileName,
            extension = "json",
            mimeType = "application/json",
            content = content,
            prependUtf8Bom = false,
            openAfterSave = false
        )
    }

    /**
     * تنزيل الكراس اليومي وبطاقة الأستاذ بصيغة نصية أو HTML للطباعة
     */
    fun downloadTeacherLogbookFile(context: Context, isHtml: Boolean = true) {
        val prof = teacherProfile.value
        val logbookText = getDailyLogbookReportText()
        if (isHtml) {
            val htmlContent = """
            <!DOCTYPE html>
            <html lang="ar" dir="rtl">
            <head>
                <meta charset="UTF-8">
                <title>كراس أستاذ التربية البدنية والرياضية</title>
                <style>
                    body { font-family: Tahoma, sans-serif; direction: rtl; padding: 20px; background: #fff; color: #111; line-height: 1.6; }
                    .btn { background: #0284c7; color: white; padding: 10px 20px; border: none; border-radius: 6px; cursor: pointer; font-weight: bold; }
                    pre { background: #f8fafc; padding: 16px; border: 1px solid #cbd5e1; border-radius: 8px; font-family: inherit; white-space: pre-wrap; font-size: 14px; }
                    @media print { .no-print { display: none; } }
                </style>
            </head>
            <body>
                <div class="no-print" style="margin-bottom: 20px; text-align: left;">
                    <button class="btn" onclick="window.print()">🖨️ طباعة الكراس / حفظ PDF</button>
                </div>
                <h2>الجمهورية الجزائرية الديمقراطية الشعبية - وزارة التربية الوطنية</h2>
                <h3>دفتر النصوص والكراس اليومي البيداغوجي لمادة التربية البدنية والرياضية</h3>
                <p><strong>الأستاذ:</strong> ${prof.fullName} | <strong>المؤسسة:</strong> ${prof.schoolName} | <strong>الرتبة:</strong> ${prof.grade}</p>
                <hr>
                <pre>$logbookText</pre>
            </body>
            </html>
            """.trimIndent()

            com.example.util.FileDownloadHelper.saveFileToDownloads(
                context = context,
                baseFileName = "الكراس_اليومي_التربوي_${prof.fullName.ifEmpty { "أستاذ" }}",
                extension = "html",
                mimeType = "text/html",
                content = htmlContent,
                prependUtf8Bom = false,
                openAfterSave = true
            )
        } else {
            com.example.util.FileDownloadHelper.saveFileToDownloads(
                context = context,
                baseFileName = "الكراس_اليومي_التربوي_${prof.fullName.ifEmpty { "أستاذ" }}",
                extension = "txt",
                mimeType = "text/plain",
                content = logbookText,
                prependUtf8Bom = false,
                openAfterSave = true
            )
        }
    }

    /**
     * تحميل وتنزيل التقرير مباشرة إلى مجلد التحميلات (Downloads) بالجهاز مع فتح قائمة المشاركة
     */
    fun downloadOrShareReport(context: Context, title: String, content: String, fileName: String) {
        val extension = if (content.trimStart().startsWith("{") || content.trimStart().startsWith("[")) "json" else "txt"
        val mimeType = if (extension == "json") "application/json" else "text/plain"
        com.example.util.FileDownloadHelper.saveFileToDownloads(
            context = context,
            baseFileName = fileName,
            extension = extension,
            mimeType = mimeType,
            content = content,
            prependUtf8Bom = false,
            openAfterSave = false
        )
    }
}
