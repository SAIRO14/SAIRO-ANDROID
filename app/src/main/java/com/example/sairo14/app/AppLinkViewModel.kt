package com.example.sairo14.app

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.sairo14.core.navigation.SharedCourseLinkParser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

/** 외부 공유 URL을 앱 시작·재개 흐름에서 한 번만 내비게이션 목적지로 전달한다.
 *
 * 유효한 공유 ID는 [SavedStateHandle]에 보관해 앱 시작 상태 확인이나 Activity 재생성 전에도 유지한다.
 * 실제 Intent 해석은 Activity가 URL 문자열로 제한하고, 이 ViewModel은 Navigation에 필요한 ID만 소유한다.
 */
@HiltViewModel
class AppLinkViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val pendingSharedCourseId: StateFlow<String?> =
        savedStateHandle.getStateFlow(PendingSharedCourseIdKey, null)

    /** URL이 유효한 Sairo 공유 코스 링크이면 이후 소비할 공유 ID로 보관한다.
     * @param url Activity가 전달한 외부 URL 문자열
     */
    fun handleUrl(url: String?) {
        SharedCourseLinkParser.parseShareIdOrNull(url)?.let { shareId ->
            savedStateHandle[PendingSharedCourseIdKey] = shareId
        }
    }

    /** 현재 대기 중인 공유 ID와 같을 때만 소비 처리해 중복 화면 이동을 막는다.
     * @param shareId Navigation에 추가한 공유 스냅샷 식별자
     */
    fun consumeSharedCourse(shareId: String) {
        if (pendingSharedCourseId.value == shareId) {
            savedStateHandle[PendingSharedCourseIdKey] = null
        }
    }
}

private const val PendingSharedCourseIdKey = "pending_shared_course_id"
