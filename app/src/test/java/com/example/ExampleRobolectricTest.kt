package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.MasteryLevel
import com.example.data.model.TermAssessment
import com.example.data.model.TermType
import com.example.data.repository.EpsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("أستاذ التربية البدنية والرياضية", appName)
  }

  @Test
  fun `preset classes and students are loaded correctly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = EpsRepository(context)

    val classes = repository.classes.value
    assertEquals(4, classes.size)
    assertEquals("1 ج م ع ت 1", classes[0].name)
    assertEquals("1 ج م آ 1", classes[1].name)
    assertEquals("2 ع ت 1", classes[2].name)
    assertEquals("3 ع ت 1", classes[3].name)

    val students = repository.students.value
    assertTrue(students.isNotEmpty())
    val class1Students = students.filter { it.classId == "c1" }
    val class2Students = students.filter { it.classId == "c2" }
    val class3Students = students.filter { it.classId == "c3" }
    val class4Students = students.filter { it.classId == "c4" }
    assertEquals(18, class1Students.size)
    assertEquals(18, class2Students.size)
    assertEquals(16, class3Students.size)
    assertEquals(14, class4Students.size)
  }

  @Test
  fun `verify three terms pedagogical sports distribution`() {
    // الفصل الأول: الجري السريع 60م + كرة اليد
    assertEquals("الجري السريع 60م", TermType.TERM_1.individualSportAr)
    assertEquals("كرة اليد", TermType.TERM_1.collectiveSportAr)

    // الفصل الثاني: الوثب الطويل + كرة السلة
    assertEquals("الوثب الطويل", TermType.TERM_2.individualSportAr)
    assertEquals("كرة السلة", TermType.TERM_2.collectiveSportAr)

    // الفصل الثالث: دفع الجلة + كرة الطائرة
    assertEquals("دفع الجلة", TermType.TERM_3.individualSportAr)
    assertEquals("كرة الطائرة", TermType.TERM_3.collectiveSportAr)
  }

  @Test
  fun `test term assessment formulas`() {
    val tAss = TermAssessment(
      studentId = "s_test",
      termType = TermType.TERM_1,
      individualDiagnosticScore14 = 10.0,
      individualProgressionScore6 = 4.5,
      collectiveSportScore20 = 15.5,
      manualTestScore = 14.0,
      absenceScore = 4.0,
      sportswearScore = 3.0,
      behaviorScore = 4.0,
      individualTaskScore = 3.0,
      teamworkScore = 3.0,
      creativityScore = 3.0
    )

    // علامة النشاط الفردي (تشخيص 14 + تطور 6) = 14.5
    assertEquals(14.5, tAss.individualSportTotal20, 0.01)

    // علامة الاختبار = (فردي + جماعي) / 2 = (14.5 + 15.5) / 2 = 15.0
    assertEquals(15.0, tAss.examScore20, 0.01)

    // التقويم المستمر = مجموع المعايير الستة = 20.0
    assertEquals(20.0, tAss.continuousAssessmentTotal20, 0.01)

    // معدل الفصل = (20 + 14 + (15 * 2)) / 4 = 64 / 4 = 16.0
    assertEquals(16.0, tAss.termAverage, 0.01)
  }
}
