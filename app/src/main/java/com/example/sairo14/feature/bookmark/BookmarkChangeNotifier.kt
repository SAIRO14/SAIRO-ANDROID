package com.example.sairo14.feature.bookmark

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** 화면 간에 최근 성공한 북마크 변경만 전달하는 메모리 기반 통지자다.
 *
 * 서버 상태를 저장하거나 복원하지 않는다. 이전 화면이 살아 있는 동안에만 변경을 전달하며, 화면을 새로
 * 구성할 때의 초기 상태는 각 화면이 서버 응답으로 다시 결정한다.
 */
@Singleton
class BookmarkChangeNotifier @Inject constructor() {
    private val _changes = MutableSharedFlow<BookmarkChange>(extraBufferCapacity = 1)

    /** 현재 화면이 수신할 수 있는 최근 북마크 변경 흐름이다. */
    val changes: SharedFlow<BookmarkChange> = _changes.asSharedFlow()

    /** 저장 또는 해제 성공 결과를 이전 화면에 알린다.
     * @param change 서버 성공 뒤 화면 간에 동기화할 북마크 상태
     */
    fun notify(change: BookmarkChange) {
        _changes.tryEmit(change)
    }
}

/** 북마크 저장 또는 해제 성공 후 화면 간에 전달하는 최소 결과다.
 *
 * 요청 중 상태는 화면별 임시 상태이므로 포함하지 않는다.
 * @param courseId 변경된 코스의 안정적인 ID
 * @param isSaved 서버 성공 후의 저장 표시 상태
 * @param savedTripId 저장 상태면 삭제 API에 쓸 ID, 해제 상태면 `null`
 */
data class BookmarkChange(
    val courseId: String,
    val isSaved: Boolean,
    val savedTripId: String?,
)
