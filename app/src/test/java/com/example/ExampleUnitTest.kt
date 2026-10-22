package com.example

import com.example.data.model.IndividualSportScale
import com.example.data.model.SportScaleConfig
import com.example.data.model.TermAssessment
import com.example.data.model.TermType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {

    @Test
    fun testTermSportsConfiguration() {
        // الفصل الأول: الجري السريع 60م (بالثواني) وكرة اليد
        assertEquals("الجري السريع 60م", TermType.TERM_1.individualSportAr)
        assertEquals("كرة اليد", TermType.TERM_1.collectiveSportAr)
        assertEquals("ثانية", TermType.TERM_1.individualUnit)

        // الفصل الثاني: الوثب الطويل (بالمتر) وكرة السلة
        assertEquals("الوثب الطويل", TermType.TERM_2.individualSportAr)
        assertEquals("كرة السلة", TermType.TERM_2.collectiveSportAr)
        assertEquals("متر", TermType.TERM_2.individualUnit)

        // الفصل الثالث: دفع الجلة (بالمتر) وكرة الطائرة
        assertEquals("دفع الجلة", TermType.TERM_3.individualSportAr)
        assertEquals("كرة الطائرة", TermType.TERM_3.collectiveSportAr)
        assertEquals("متر", TermType.TERM_3.individualUnit)
    }

    @Test
    fun testSprintSummativeScale_5secIs14pts_andEach1secIncreasesDeducts1pt() {
        // مقياس الأستاذ: 5 ثوانٍ تعني 14 نقطة وكل ما زادت 1 ثانية نقصت نقطة
        val score5_0 = IndividualSportScale.calculateDiagnosticScore14(TermType.TERM_1, 5.0)
        assertEquals(14.0, score5_0, 0.001)

        val score6_0 = IndividualSportScale.calculateDiagnosticScore14(TermType.TERM_1, 6.0)
        assertEquals(13.0, score6_0, 0.001)

        val score7_0 = IndividualSportScale.calculateDiagnosticScore14(TermType.TERM_1, 7.0)
        assertEquals(12.0, score7_0, 0.001)

        val score8_0 = IndividualSportScale.calculateDiagnosticScore14(TermType.TERM_1, 8.0)
        assertEquals(11.0, score8_0, 0.001)

        val score9_0 = IndividualSportScale.calculateDiagnosticScore14(TermType.TERM_1, 9.0)
        assertEquals(10.0, score9_0, 0.001)
    }

    @Test
    fun testPositiveProgression_EarnsPoints_AndNegativeRegression_GivesZeroPoints() {
        // شرط الأستاذ المحدد:
        // "اذا كان التطور ايجابي اي تحسن في النتيجة يحسب واذا كان سلبي يعني صفر في علامة التطور الحاصل"

        // 1) في الجري السريع (بالثواني):
        val posProg = IndividualSportScale.calculateProgressionScore6(TermType.TERM_1, 8.0, 7.0)
        assertEquals(2.0, posProg, 0.001) // 1.0 ثانية / 0.5 = 2 نقطتان

        val negProg = IndividualSportScale.calculateProgressionScore6(TermType.TERM_1, 7.0, 8.0)
        assertEquals(0.0, negProg, 0.001) // تراجع سلبي -> 0 (صفر)

        val zeroProg = IndividualSportScale.calculateProgressionScore6(TermType.TERM_1, 7.5, 7.5)
        assertEquals(0.0, zeroProg, 0.001)

        // 2) في الوثب الطويل (بالمتر):
        val jumpPos = IndividualSportScale.calculateProgressionScore6(TermType.TERM_2, 3.0, 3.5)
        assertEquals(4.0, jumpPos, 0.001) // زيادة 0.5م بالمتر / 0.125 = 4 نقاط

        val jumpNeg = IndividualSportScale.calculateProgressionScore6(TermType.TERM_2, 3.5, 3.0)
        assertEquals(0.0, jumpNeg, 0.001) // تراجع بالمتر -> 0 صفر

        // 3) في دفع الجلة (بالمتر):
        val shotPos = IndividualSportScale.calculateProgressionScore6(TermType.TERM_3, 6.0, 7.0)
        assertEquals(4.0, shotPos, 0.001) // زيادة 1.0م بالمتر / 0.25 = 4 نقاط

        val shotNeg = IndividualSportScale.calculateProgressionScore6(TermType.TERM_3, 7.0, 6.0)
        assertEquals(0.0, shotNeg, 0.001) // تراجع بالمتر -> 0 صفر
    }

    @Test
    fun testTerm2_LongJump_CalculatedInMeters() {
        // الفصل الثاني: الوثب الطويل الحساب بالمتر
        val diagMeters = 3.50
        val summMeters = 4.00

        // 1) متوسط المسافة بالمتر: (3.50 + 4.00) / 2 = 3.75 م
        val avgMeters = IndividualSportScale.calculateAverageMeasurement(diagMeters, summMeters)
        assertEquals(3.75, avgMeters!!, 0.001)

        // 2) نقطة الاختبار على 14 من متوسط المسافة بالمتر:
        // 4.50 م = 14ن، العجز: 4.50 - 3.75 = 0.75 م -> الخصم: 0.75 / 0.25 = 3 نقاط -> 11.0 / 14
        val score14 = IndividualSportScale.calculateCombinedScore14(TermType.TERM_2, diagMeters, summMeters)
        assertEquals(11.0, score14, 0.001)

        // 3) علامة التطور الحاصل بالمتر: زيادة 0.50 م -> 0.50 / 0.125 = 4.0 / 6
        val progScore6 = IndividualSportScale.calculateProgressionScore6(TermType.TERM_2, diagMeters, summMeters)
        assertEquals(4.0, progScore6, 0.001)

        // 4) مجموع النشاط الفردي بالمتر = 11.0 + 4.0 = 15.0 من 20
        val assessment = TermAssessment(
            studentId = "s2",
            termType = TermType.TERM_2,
            diagnosticMeasurement = diagMeters,
            summativeMeasurement = summMeters,
            individualDiagnosticScore14 = score14,
            individualProgressionScore6 = progScore6
        )
        assertEquals(15.0, assessment.individualSportTotal20, 0.001)
    }

    @Test
    fun testTerm3_ShotPut_CalculatedInMeters() {
        // الفصل الثالث: دفع الجلة الحساب بالمتر
        val diagMeters = 6.00
        val summMeters = 7.00

        // 1) متوسط المسافة بالمتر: (6.00 + 7.00) / 2 = 6.50 م
        val avgMeters = IndividualSportScale.calculateAverageMeasurement(diagMeters, summMeters)
        assertEquals(6.50, avgMeters!!, 0.001)

        // 2) نقطة الاختبار على 14 من متوسط المسافة بالمتر:
        // 8.00 م = 14ن، العجز: 8.00 - 6.50 = 1.50 م -> الخصم: 1.50 / 0.50 = 3 نقاط -> 11.0 / 14
        val score14 = IndividualSportScale.calculateCombinedScore14(TermType.TERM_3, diagMeters, summMeters)
        assertEquals(11.0, score14, 0.001)

        // 3) علامة التطور الحاصل بالمتر: زيادة 1.00 م -> 1.00 / 0.25 = 4.0 / 6
        val progScore6 = IndividualSportScale.calculateProgressionScore6(TermType.TERM_3, diagMeters, summMeters)
        assertEquals(4.0, progScore6, 0.001)

        // 4) مجموع النشاط الفردي بالمتر = 11.0 + 4.0 = 15.0 من 20
        val assessment = TermAssessment(
            studentId = "s3",
            termType = TermType.TERM_3,
            diagnosticMeasurement = diagMeters,
            summativeMeasurement = summMeters,
            individualDiagnosticScore14 = score14,
            individualProgressionScore6 = progScore6
        )
        assertEquals(15.0, assessment.individualSportTotal20, 0.001)
    }

    @Test
    fun testScore14_FromAverageOfDiagnosticAndSummativeTimes() {
        val diagTime = 8.0
        val summTime = 7.0

        val avgTime = IndividualSportScale.calculateAverageMeasurement(diagTime, summTime)
        assertEquals(7.5, avgTime!!, 0.001)

        val score14 = IndividualSportScale.calculateCombinedScore14(TermType.TERM_1, diagTime, summTime)
        assertEquals(11.5, score14, 0.001)

        val progScore6 = IndividualSportScale.calculateProgressionScore6(TermType.TERM_1, diagTime, summTime)
        assertEquals(2.0, progScore6, 0.001)

        val assessment = TermAssessment(
            studentId = "s1",
            termType = TermType.TERM_1,
            diagnosticMeasurement = diagTime,
            summativeMeasurement = summTime,
            individualDiagnosticScore14 = score14,
            individualProgressionScore6 = progScore6
        )
        assertEquals(13.5, assessment.individualSportTotal20, 0.001)
    }

    @Test
    fun testCustomScaleAdjustment() {
        // اختبار إمكانية تعديل المقياس يدوياً بالمتر
        val customConfig = SportScaleConfig(
            jumpBaseDistMeters = 4.00,          // جعل 4 أمتار هي العلامة الكاملة
            jumpDistPerMinusPoint = 0.20,       // خصم نقطة لكل 0.20 م
            jumpDistPerProgressionPoint = 0.10   // نقطة لكل 0.10 م زيادة
        )

        // 4.00 م = 14ن
        val score4 = IndividualSportScale.calculateDiagnosticScore14(TermType.TERM_2, 4.00, customConfig)
        assertEquals(14.0, score4, 0.001)

        // 3.60 م = 14 - (0.40 / 0.20) = 12ن
        val score3_6 = IndividualSportScale.calculateDiagnosticScore14(TermType.TERM_2, 3.60, customConfig)
        assertEquals(12.0, score3_6, 0.001)
    }

    @Test
    fun testSixContinuousAssessmentCriteria_SumsTo20Points() {
        // التحقق من معايير التقويم المستمر الستة ومجموعها على 20ن:
        // 1. الغيابات والمواظبة (4ن)
        // 2. اللباس الرياضي (3ن)
        // 3. السلوك والانضباط (4ن)
        // 4. إنجاز المهام الفردية (3ن)
        // 5. العمل ضمن الفريق (3ن)
        // 6. الإبداع (3ن)
        val defaultAssessment = TermAssessment(studentId = "s_test", termType = TermType.TERM_1)
        assertEquals(4.0, defaultAssessment.absenceScore, 0.001)
        assertEquals(3.0, defaultAssessment.sportswearScore, 0.001)
        assertEquals(4.0, defaultAssessment.behaviorScore, 0.001)
        assertEquals(3.0, defaultAssessment.individualTaskScore, 0.001)
        assertEquals(3.0, defaultAssessment.teamworkScore, 0.001)
        assertEquals(3.0, defaultAssessment.creativityScore, 0.001)
        assertEquals(20.0, defaultAssessment.continuousAssessmentTotal20, 0.001)
    }

    @Test
    fun testContinuousAssessmentScale_DailyRecordsCalculation() {
        val student = com.example.data.model.Student(
            id = "s_demo",
            classId = "c1",
            regNumber = 1,
            fullName = "تلميذ اختبار",
            gender = com.example.data.model.Gender.MALE
        )

        // 1) تلميذ حاضر بكل الحصص، لباس كامل، سلوك منضبط، مهام منجزة، عمل جماعي، إبداع
        val perfectRecords = listOf(
            com.example.data.model.AttendanceRecord(
                id = "att_1",
                studentId = student.id,
                date = "2024-10-01",
                status = com.example.data.model.AttendanceStatus.PRESENT,
                hasSportswear = true,
                goodBehavior = true,
                individualTaskCompleted = true,
                teamworkContribution = true,
                creativityDemonstrated = true
            ),
            com.example.data.model.AttendanceRecord(
                id = "att_2",
                studentId = student.id,
                date = "2024-10-08",
                status = com.example.data.model.AttendanceStatus.PRESENT,
                hasSportswear = true,
                goodBehavior = true,
                individualTaskCompleted = true,
                teamworkContribution = true,
                creativityDemonstrated = true
            )
        )
        val perfectScore = com.example.data.model.ContinuousAssessmentScale.calculateScoresFromRecords(student, perfectRecords)
        assertEquals(4.0, perfectScore.absenceScore, 0.001)
        assertEquals(3.0, perfectScore.sportswearScore, 0.001)
        assertEquals(4.0, perfectScore.behaviorScore, 0.001)
        assertEquals(3.0, perfectScore.individualTaskScore, 0.001)
        assertEquals(3.0, perfectScore.teamworkScore, 0.001)
        assertEquals(3.0, perfectScore.creativityScore, 0.001)
        assertEquals(20.0, perfectScore.totalScore20, 0.001)

        // 2) تلميذ لديه غياب غير مبرر (-1ن)، حصة بدون بذلة (-1ن)، مخالفة سلوك (-1ن)
        val recordsWithPenalties = listOf(
            com.example.data.model.AttendanceRecord(
                id = "att_1",
                studentId = student.id,
                date = "2024-10-01",
                status = com.example.data.model.AttendanceStatus.ABSENT_UNEXCUSED
            ),
            com.example.data.model.AttendanceRecord(
                id = "att_2",
                studentId = student.id,
                date = "2024-10-08",
                status = com.example.data.model.AttendanceStatus.PRESENT,
                hasSportswear = false, // بدون بذلة
                goodBehavior = false,  // مخالفة سلوك
                individualTaskCompleted = true,
                teamworkContribution = true,
                creativityDemonstrated = true // +0.5 إلى قاعدة 2.0 = 2.5
            )
        )
        val penaltyScore = com.example.data.model.ContinuousAssessmentScale.calculateScoresFromRecords(student, recordsWithPenalties)
        assertEquals(3.0, penaltyScore.absenceScore, 0.001) // 4.0 - 1.0 = 3.0
        assertEquals(2.0, penaltyScore.sportswearScore, 0.001) // 3.0 - 1.0 = 2.0
        assertEquals(3.0, penaltyScore.behaviorScore, 0.001) // 4.0 - 1.0 = 3.0
        assertEquals(3.0, penaltyScore.individualTaskScore, 0.001) // 100% من الحصص الحاضرة
        assertEquals(3.0, penaltyScore.teamworkScore, 0.001) // 100% من الحصص الحاضرة
        assertEquals(2.5, penaltyScore.creativityScore, 0.001) // قاعدة 2.0 + 0.5 = 2.5
        assertEquals(16.5, penaltyScore.totalScore20, 0.001) // 3 + 2 + 3 + 3 + 3 + 2.5 = 16.5
    }

    @Test
    fun testSecondarySchoolClassRoomModel() {
        val class1 = com.example.data.model.ClassRoom("c1", "1 ج م ع ت 1", "السنة الأولى ثانوي (جذع مشترك علوم وتكنولوجيا)")
        val class2 = com.example.data.model.ClassRoom("c2", "1 ج م آ 1", "السنة الأولى ثانوي (جذع مشترك آداب)")
        val class3 = com.example.data.model.ClassRoom("c3", "2 ع ت 1", "السنة الثانية ثانوي (علوم تجريبية)")
        val class4 = com.example.data.model.ClassRoom("c4", "3 ع ت 1", "السنة الثالثة ثانوي (علوم تجريبية - بكالوريا)")

        assertEquals("1 ج م ع ت 1", class1.name)
        assertEquals("السنة الأولى ثانوي (جذع مشترك علوم وتكنولوجيا)", class1.level)
        assertEquals("1 ج م آ 1", class2.name)
        assertEquals("2 ع ت 1", class3.name)
        assertEquals("3 ع ت 1", class4.name)
    }

    @Test
    fun testResultSheetTextParser_ExtractsStudentsAndScores() {
        val sampleText = """
            الرقم	اللقب والاسم	التقويم المستمر	الفرض	الاختبار
            1	بن علي أحمد	16.5	15.0	17.0
            2	بوزيد مريم خديجة	18.0	17.5	18.5
            3	رحماني شيماء	معفى
        """.trimIndent()

        val results = com.example.data.ocr.ResultSheetScannerService.parseTextSheet(sampleText)
        assertEquals(3, results.size)

        val s1 = results[0]
        assertEquals("بن علي أحمد", s1.fullName)
        assertEquals(16.5, s1.continuousScore ?: 0.0, 0.01)
        assertEquals(15.0, s1.testScore ?: 0.0, 0.01)
        assertEquals(17.0, s1.examScore ?: 0.0, 0.01)

        val s2 = results[1]
        assertEquals("بوزيد مريم خديجة", s2.fullName)
        assertEquals(18.0, s2.continuousScore ?: 0.0, 0.01)

        val s3 = results[2]
        assertEquals("رحماني شيماء", s3.fullName)
        org.junit.Assert.assertTrue(s3.isMedicallyExempt)
    }

    @Test
    fun testResultSheetJsonParser_ExtractsStructuredData() {
        val json = """
            [
              {
                "regNumber": 1,
                "fullName": "جابري صهيب إسلام",
                "gender": "MALE",
                "continuousScore": 17.5,
                "testScore": 16.0,
                "examScore": 18.0,
                "isExempt": false
              },
              {
                "regNumber": 2,
                "fullName": "حطاب ريان إكرام",
                "gender": "FEMALE",
                "continuousScore": 16.0,
                "testScore": 15.0,
                "examScore": 16.5,
                "isExempt": false
              }
            ]
        """.trimIndent()

        val results = com.example.data.ocr.ResultSheetScannerService.parseJsonResults(json)
        assertEquals(2, results.size)
        assertEquals("جابري صهيب إسلام", results[0].fullName)
        assertEquals(com.example.data.model.Gender.MALE, results[0].gender)
        assertEquals(17.5, results[0].continuousScore ?: 0.0, 0.01)

        assertEquals("حطاب ريان إكرام", results[1].fullName)
        assertEquals(com.example.data.model.Gender.FEMALE, results[1].gender)
    }

    @Test
    fun testPresetSampleSheets_NotEmpty() {
        val presets = com.example.data.ocr.ResultSheetScannerService.getPresetSampleSheets()
        org.junit.Assert.assertTrue(presets.isNotEmpty())
        org.junit.Assert.assertTrue(presets.values.first().size >= 10)
    }

    @Test
    fun testStopwatchSpeedAndScoreCalculation() {
        // اختبار حساب السرعة التلقائي: 60 متر في 8.0 ثوانٍ
        val distance = 60.0
        val timeSeconds = 8.0
        val speedMs = distance / timeSeconds
        val speedKmh = speedMs * 3.6

        // 60 / 8 = 7.5 م/ث -> 7.5 * 3.6 = 27.0 كم/سا
        assertEquals(7.5, speedMs, 0.001)
        assertEquals(27.0, speedKmh, 0.001)

        // علامة 8.0 ثانية على 14 من 20: 5.0ث = 14ن -> 8.0ث - 5.0ث = 3.0ث عجز = -3ن -> 11.0 / 14ن
        val score14 = IndividualSportScale.calculateDiagnosticScore14(TermType.TERM_1, timeSeconds)
        assertEquals(11.0, score14, 0.001)

        // اختبار 50 متر في 6.5 ثانية (الطور المتوسط):
        // 50 / 6.5 = 7.69 م/ث -> 7.69 * 3.6 = 27.69 كم/سا
        val dist50 = 50.0
        val time50 = 6.5
        val speed50Kmh = (dist50 / time50) * 3.6
        assertEquals(27.692, speed50Kmh, 0.01)
    }
}
