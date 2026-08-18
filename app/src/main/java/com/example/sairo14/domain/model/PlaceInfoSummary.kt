package com.example.sairo14.domain.model

import java.time.DayOfWeek

/** 장소의 자유 텍스트 정보를 태그 표시 정책에 맞는 의미 단위로 표현한다. */
data class PlaceInfoSummary(
    val operatingHours: OperatingHoursSummary?,
    val closedDays: List<ClosedDaysSummary>,
    val parking: ParkingSummary?,
    val contact: ContactSummary?,
)

/** 장소 운영시간의 표시 정책에 필요한 유형을 표현한다. */
sealed interface OperatingHoursSummary {
    data object AlwaysOpen : OperatingHoursSummary

    data object PhoneInquiry : OperatingHoursSummary

    data class TimeRange(val value: String) : OperatingHoursSummary

    data class Periods(val values: List<PeriodHours>) : OperatingHoursSummary

    data class WeekdayWeekend(
        val weekday: String?,
        val weekend: String?,
    ) : OperatingHoursSummary
}

/** 기간 라벨과 해당 기간의 운영시간을 함께 표현한다. */
data class PeriodHours(
    val label: String,
    val hours: String,
)

/** 장소 휴무일의 표시 정책에 필요한 유형을 표현한다. */
sealed interface ClosedDaysSummary {
    data object OpenAllYear : ClosedDaysSummary

    data object PublicHoliday : ClosedDaysSummary

    data object BadWeather : ClosedDaysSummary

    data object PhoneInquiry : ClosedDaysSummary

    data class Weekly(val dayOfWeek: DayOfWeek) : ClosedDaysSummary

    data class Literal(val value: String) : ClosedDaysSummary
}

/** 장소 주차 정보의 표시 정책에 필요한 유형을 표현한다. */
enum class ParkingSummary {
    Available,
    Unavailable,
    PhoneInquiry,
}

/** 장소 문의처의 표시 정책에 필요한 유형을 표현한다. */
sealed interface ContactSummary {
    data class PhoneNumber(val value: String) : ContactSummary

    data object PhoneInquiry : ContactSummary
}
