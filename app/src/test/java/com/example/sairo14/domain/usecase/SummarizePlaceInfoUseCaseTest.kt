package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.ClosedDaysSummary
import com.example.sairo14.domain.model.ContactSummary
import com.example.sairo14.domain.model.CoursePlace
import com.example.sairo14.domain.model.OperatingHoursSummary
import com.example.sairo14.domain.model.ParkingSummary
import com.example.sairo14.domain.model.PeriodHours
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SummarizePlaceInfoUseCaseTest {
    private val summarize = SummarizePlaceInfoUseCase()

    @Test
    fun `상시 개방과 일출 일몰 기준 운영시간을 상시 개방으로 요약한다`() {
        assertEquals(
            OperatingHoursSummary.AlwaysOpen,
            summarize(place(operatingHours = "상시 개방")).operatingHours,
        )
        assertEquals(
            OperatingHoursSummary.AlwaysOpen,
            summarize(place(operatingHours = "일출부터 일몰 전까지 이용 권장")).operatingHours,
        )
    }

    @Test
    fun `입장 및 매표 마감을 제외한 단순 운영시간을 요약한다`() {
        val summary = summarize(
            place(operatingHours = "09:00~18:00 (입장 마감 17:00, 매표 마감 16:30)"),
        )

        assertEquals(OperatingHoursSummary.TimeRange("09:00~18:00"), summary.operatingHours)
    }

    @Test
    fun `절기와 월 범위 운영시간을 모두 요약한다`() {
        val seasonal = summarize(
            place(operatingHours = "[하절기]\n09:00~18:00\n[동절기]\n09:00~17:00"),
        )
        val monthly = summarize(
            place(operatingHours = "[3월~5월] 09:00~19:00\n[6월~8월] 09:00~20:00"),
        )

        assertEquals(
            OperatingHoursSummary.Periods(
                listOf(PeriodHours("하절기", "09:00~18:00"), PeriodHours("동절기", "09:00~17:00")),
            ),
            seasonal.operatingHours,
        )
        assertEquals(
            OperatingHoursSummary.Periods(
                listOf(PeriodHours("3월~5월", "09:00~19:00"), PeriodHours("6월~8월", "09:00~20:00")),
            ),
            monthly.operatingHours,
        )
    }

    @Test
    fun `평일과 주말 운영시간을 하나의 의미 단위로 요약한다`() {
        val summary = summarize(place(operatingHours = "• 평일 12:00~20:00\n- 주말 12:00~21:00"))

        assertEquals(
            OperatingHoursSummary.WeekdayWeekend(
                weekday = "12:00~20:00",
                weekend = "12:00~21:00",
            ),
            summary.operatingHours,
        )
    }

    @Test
    fun `시설별 상이하거나 파싱할 수 없는 운영시간은 전화문의로 요약한다`() {
        assertEquals(
            OperatingHoursSummary.PhoneInquiry,
            summarize(
                place(
                    operatingHours = "[마라도 등대] 09:00~18:00\n[운진항 여객터미널] 09:40~15:50",
                ),
            ).operatingHours,
        )
        assertEquals(
            OperatingHoursSummary.PhoneInquiry,
            summarize(place(operatingHours = "방문 전 확인 필요")).operatingHours,
        )
    }

    @Test
    fun `기상 조건은 기존 휴무일과 함께 별도 휴무 태그로 요약한다`() {
        val summary = summarize(
            place(
                operatingHours = "09:00~17:30\n※ 기상 악화 시 통제될 수 있습니다",
                closedDays = "연중무휴",
            ),
        )

        assertEquals(OperatingHoursSummary.TimeRange("09:00~17:30"), summary.operatingHours)
        assertEquals(
            listOf(ClosedDaysSummary.OpenAllYear, ClosedDaysSummary.BadWeather),
            summary.closedDays,
        )
    }

    @Test
    fun `휴무일의 요일 명절 공휴일 특정 주차와 예외를 요약한다`() {
        assertEquals(
            listOf(ClosedDaysSummary.Weekly(DayOfWeek.MONDAY)),
            summarize(place(closedDays = "매주 월요일 / 1월1일 / 설·추석 연휴")).closedDays,
        )
        assertEquals(
            listOf(ClosedDaysSummary.PublicHoliday),
            summarize(place(closedDays = "설날 및 공휴일 휴무")).closedDays,
        )
        assertEquals(
            listOf(ClosedDaysSummary.Literal("매월 둘째·넷째 월요일")),
            summarize(place(closedDays = "매월 둘째·넷째 월요일")).closedDays,
        )
        assertEquals(
            listOf(ClosedDaysSummary.PhoneInquiry),
            summarize(place(closedDays = "휴무 일정은 현장 상황에 따라 달라집니다")).closedDays,
        )
    }

    @Test
    fun `주차 가능 불가능 및 예외 값을 요약한다`() {
        assertEquals(ParkingSummary.Available, summarize(place(parking = "가능\n요금 (무료)")).parking)
        assertEquals(ParkingSummary.Unavailable, summarize(place(parking = "주차 불가능")).parking)
        assertEquals(ParkingSummary.PhoneInquiry, summarize(place(parking = "현장 상황에 따라 상이")).parking)
    }

    @Test
    fun `문의처에서 첫 번째 전화번호만 요약하고 없으면 전화문의로 처리한다`() {
        assertEquals(
            ContactSummary.PhoneNumber("064-740-6000"),
            summarize(
                place(contact = "제주관광정보센터 064-740-6000\n한림읍 사무소 064-728-1521"),
            ).contact,
        )
        assertEquals(
            ContactSummary.PhoneNumber("1670-1188"),
            summarize(place(contact = "대표번호 1670-1188")).contact,
        )
        assertEquals(ContactSummary.PhoneInquiry, summarize(place(contact = "관광안내소로 문의")).contact)
    }

    @Test
    fun `원문 정보가 없으면 해당 요약 항목을 생략한다`() {
        val summary = summarize(place())

        assertNull(summary.operatingHours)
        assertEquals(emptyList<ClosedDaysSummary>(), summary.closedDays)
        assertNull(summary.parking)
        assertNull(summary.contact)
    }

    private fun place(
        operatingHours: String? = null,
        closedDays: String? = null,
        parking: String? = null,
        contact: String? = null,
    ) = CoursePlace(
        placeId = "place-1",
        name = "테스트 장소",
        imageUrl = null,
        tags = emptyList(),
        coordinate = null,
        operatingHours = operatingHours,
        closedDays = closedDays,
        parking = parking,
        contact = contact,
    )
}
