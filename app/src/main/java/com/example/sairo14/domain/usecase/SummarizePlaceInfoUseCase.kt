package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.ClosedDaysSummary
import com.example.sairo14.domain.model.ContactSummary
import com.example.sairo14.domain.model.CoursePlace
import com.example.sairo14.domain.model.OperatingHoursSummary
import com.example.sairo14.domain.model.ParkingSummary
import com.example.sairo14.domain.model.PeriodHours
import com.example.sairo14.domain.model.PlaceInfoSummary
import java.time.DayOfWeek
import javax.inject.Inject

/** 장소의 자유 텍스트 정보를 상세 화면 태그에 사용할 의미 단위로 요약한다.
 *
 * 원문이 없으면 해당 항목을 생략하고, 원문을 안전하게 해석할 수 없으면 [PhoneInquiry]로 반환한다.
 * 화면에 표시할 한국어 문구와 태그 순서는 호출자가 관리한다.
 */
class SummarizePlaceInfoUseCase @Inject constructor() {
    /** 한 장소의 운영시간·휴무일·주차·문의처 정보를 함께 요약한다.
     *
     * 운영시간 또는 휴무일 원문에서 기상 통제 조건을 찾으면 휴무일에 [ClosedDaysSummary.BadWeather]를
     * 추가한다.
     * @param place 요약할 원문 장소 정보
     */
    operator fun invoke(place: CoursePlace): PlaceInfoSummary {
        val hasBadWeatherCondition = place.operatingHours.containsBadWeatherCondition() ||
            place.closedDays.containsBadWeatherCondition()

        return PlaceInfoSummary(
            operatingHours = place.operatingHours.toOperatingHoursSummary(),
            closedDays = place.closedDays.toClosedDaysSummary(hasBadWeatherCondition),
            parking = place.parking.toParkingSummary(),
            contact = place.contact.toContactSummary(),
        )
    }
}

private val timeRangePattern =
    "(?:[01]?\\d|2[0-3]):[0-5]\\d\\s*(?:~|-)\\s*(?:[01]?\\d|2[0-3]):[0-5]\\d"
private val timeRangeRegex = Regex(timeRangePattern)
private val periodHoursRegex = Regex("""\[([^]]+)]\s*($timeRangePattern)""")
private val weekOfMonthRegex = Regex("""매월\s*[^\n]*(?:월|화|수|목|금|토|일)요일""")
private val phoneNumberRegex = Regex(
    """(?<!\d)(?:0\d{1,3}-\d{3,4}-\d{4}|1[5-8]\d{2}-\d{4})(?!\d)""",
)

private fun String?.toOperatingHoursSummary(): OperatingHoursSummary? {
    val value = this?.trim()?.takeIf(String::isNotEmpty) ?: return null

    if (value.hasMultipleFacilityLabels()) return OperatingHoursSummary.PhoneInquiry
    if (value.containsSunriseSunsetCondition() || value.containsAlwaysOpenExpression()) {
        return OperatingHoursSummary.AlwaysOpen
    }

    value.toWeekdayWeekendSummary()?.let { return it }
    value.toPeriodHours().takeIf { it.isNotEmpty() }?.let { periods ->
        return OperatingHoursSummary.Periods(periods)
    }
    value.firstTimeRange()?.let { return OperatingHoursSummary.TimeRange(it) }

    return OperatingHoursSummary.PhoneInquiry
}

private fun String?.toClosedDaysSummary(
    hasBadWeatherCondition: Boolean,
): List<ClosedDaysSummary> = buildList {
    val value = this@toClosedDaysSummary?.trim()?.takeIf(String::isNotEmpty)
    value?.toBaseClosedDaysSummary()?.let(::add)
    if (hasBadWeatherCondition) add(ClosedDaysSummary.BadWeather)
}.distinct()

private fun String.toBaseClosedDaysSummary(): ClosedDaysSummary = when {
    weekOfMonthRegex.containsMatchIn(this) -> ClosedDaysSummary.Literal(this)
    toDayOfWeekOrNull() != null -> ClosedDaysSummary.Weekly(checkNotNull(toDayOfWeekOrNull()))
    containsHolidayExpression() -> ClosedDaysSummary.PublicHoliday
    contains("연중무휴") -> ClosedDaysSummary.OpenAllYear
    else -> ClosedDaysSummary.PhoneInquiry
}

private fun String?.toParkingSummary(): ParkingSummary? {
    val value = this?.trim()?.takeIf(String::isNotEmpty) ?: return null

    return when {
        value.contains(Regex("""불가능|불가|주차\s*없음|주차장\s*없음""")) -> ParkingSummary.Unavailable
        value.contains(Regex("""가능|주차\s*있음|주차장\s*있음""")) -> ParkingSummary.Available
        else -> ParkingSummary.PhoneInquiry
    }
}

private fun String?.toContactSummary(): ContactSummary? {
    val value = this?.trim()?.takeIf(String::isNotEmpty) ?: return null
    val phoneNumber = phoneNumberRegex.find(value)?.value ?: return ContactSummary.PhoneInquiry

    return ContactSummary.PhoneNumber(value = phoneNumber)
}

private fun String.toWeekdayWeekendSummary(): OperatingHoursSummary.WeekdayWeekend? {
    val weekday = findTimeRangeAfter("평일")
    val weekend = findTimeRangeAfter("주말")
    return if (weekday == null && weekend == null) null else {
        OperatingHoursSummary.WeekdayWeekend(weekday = weekday, weekend = weekend)
    }
}

private fun String.toPeriodHours(): List<PeriodHours> = periodHoursRegex
    .findAll(this)
    .map { match ->
        PeriodHours(
            label = match.groupValues[1].trim(),
            hours = match.groupValues[2].normalizeTimeRange(),
        )
    }
    .toList()

private fun String.findTimeRangeAfter(label: String): String? = Regex(
    """$label[^\n]*?($timeRangePattern)""",
).find(this)
    ?.groupValues
    ?.getOrNull(1)
    ?.normalizeTimeRange()

private fun String.firstTimeRange(): String? = timeRangeRegex.find(this)?.value?.normalizeTimeRange()

private fun String.normalizeTimeRange(): String = replace(Regex("""\s*(?:~|-)\s*"""), "~")

private fun String.hasMultipleFacilityLabels(): Boolean = Regex("""\[([^]]+)]""")
    .findAll(this)
    .map { it.groupValues[1].trim() }
    .filterNot(String::isPeriodLabel)
    .distinct()
    .count() >= 2

private fun String.isPeriodLabel(): Boolean =
    contains(Regex("""(?:하절기|동절기|봄|여름|가을|겨울|성수기|비수기|\d{1,2}월\s*~\s*\d{1,2}월)"""))

private fun String.containsAlwaysOpenExpression(): Boolean =
    contains(Regex("""상시\s*(?:개방|운영)|24시간|연중\s*개방"""))

private fun String.containsSunriseSunsetCondition(): Boolean =
    contains(Regex("""일출|일몰"""))

private fun String?.containsBadWeatherCondition(): Boolean = this?.contains(
    Regex("""기상|악천후|통제|결항|휴항"""),
) == true

private fun String.containsHolidayExpression(): Boolean =
    contains(Regex("""공휴일|명절|설(?:날)?|추석|신정"""))

private fun String.toDayOfWeekOrNull(): DayOfWeek? = when {
    contains("월요일") -> DayOfWeek.MONDAY
    contains("화요일") -> DayOfWeek.TUESDAY
    contains("수요일") -> DayOfWeek.WEDNESDAY
    contains("목요일") -> DayOfWeek.THURSDAY
    contains("금요일") -> DayOfWeek.FRIDAY
    contains("토요일") -> DayOfWeek.SATURDAY
    contains("일요일") -> DayOfWeek.SUNDAY
    else -> null
}
