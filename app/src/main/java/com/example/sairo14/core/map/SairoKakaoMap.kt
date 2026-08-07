package com.example.sairo14.core.map

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.CompetitionType
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelLayerOptions
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.OrderingType
import kotlin.math.roundToInt

/** 카카오 지도 SDK의 MapView를 Compose에 연결하고 순서 핀과 카메라 중심을 표시한다.
 *
 * 지도 SDK 수명주기는 이 Composable이 [LocalLifecycleOwner]에 맞춰 관리한다. 화면은 Domain 장소를
 * [SairoMapMarker]로 변환하고, 헤더·시트가 가린 영역은 [viewportPadding]으로 전달한다. 명시적인
 * [cameraTarget]이 없을 때만 첫 번째 마커를 카메라 중심으로 사용한다.
 * @param markers 지도에 순서대로 표시할 마커 목록
 * @param viewportPadding 지도 위에 겹친 UI를 피하기 위한 뷰포트 여백
 * @param cameraTarget 카메라 중심으로 이동할 장소 좌표. null이면 첫 번째 마커를 사용한다
 * @param cameraRequestId 같은 좌표를 다시 중심으로 이동시키기 위한 화면의 카메라 요청 식별자
 * @param modifier MapView 컨테이너에 적용할 Modifier
 * @param zoomLevel 카메라를 대상 좌표에 맞출 때 적용할 확대 수준
 * @param onMapError 지도 인증·시작 중 발생한 오류를 전달하는 콜백
 */
@Composable
fun SairoKakaoMap(
    markers: List<SairoMapMarker>,
    viewportPadding: SairoMapViewportPadding,
    cameraTarget: SairoMapCameraTarget? = null,
    cameraRequestId: Long = InitialCameraRequestId,
    modifier: Modifier = Modifier,
    zoomLevel: Int = DefaultZoomLevel,
    onMapError: (Exception) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val latestOnMapError = rememberUpdatedState(onMapError)
    val density = LocalDensity.current
    val viewportPaddingPx = with(density) {
        SairoMapViewportPaddingPx(
            left = viewportPadding.left.roundToPx(),
            top = viewportPadding.top.roundToPx(),
            right = viewportPadding.right.roundToPx(),
            bottom = viewportPadding.bottom.roundToPx(),
        )
    }
    val mapController = remember(context.applicationContext) {
        SairoKakaoMapController(
            context = context.applicationContext,
            onMapError = { error -> latestOnMapError.value(error) },
        )
    }
    val mapView = remember(context) {
        MapView(context).apply {
            setFinishManually(true)
        }
    }

    DisposableEffect(lifecycle, mapView) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.resumeIfStarted()
                Lifecycle.Event.ON_PAUSE -> mapView.pauseIfStarted()
                else -> Unit
            }
        }

        lifecycle.addObserver(lifecycleObserver)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mapView.resumeIfStarted()
        }

        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
            mapView.finish()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            if (KakaoMapSdk.isInitialized()) {
                mapView.start(
                    object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {
                            mapController.detach()
                        }

                        override fun onMapError(error: Exception) {
                            mapController.reportError(error)
                        }
                    },
                    object : KakaoMapReadyCallback() {
                        override fun onMapReady(kakaoMap: KakaoMap) {
                            mapController.attach(kakaoMap)
                        }
                    },
                )
            } else {
                mapController.reportError(
                    IllegalStateException("카카오 지도 SDK가 초기화되지 않았습니다."),
                )
            }
            mapView
        },
        update = {
            mapController.update(
                markers = markers,
                viewportPadding = viewportPaddingPx,
                cameraTarget = cameraTarget,
                cameraRequestId = cameraRequestId,
                zoomLevel = zoomLevel,
            )
        },
    )
}

private class SairoKakaoMapController(
    context: Context,
    private val onMapError: (Exception) -> Unit,
) {
    private val markerBitmapFactory = SairoMapMarkerBitmapFactory(context)

    private var kakaoMap: KakaoMap? = null
    private var markerLayer: LabelLayer? = null
    private var requestedMarkers: List<SairoMapMarker> = emptyList()
    private var requestedViewportPadding = SairoMapViewportPaddingPx()
    private var requestedCameraTarget: SairoMapCameraTarget? = null
    private var requestedCameraRequestId = InitialCameraRequestId
    private var requestedZoomLevel = DefaultZoomLevel
    private var appliedMarkers: List<SairoMapMarker>? = null
    private var appliedViewportPadding: SairoMapViewportPaddingPx? = null
    private var appliedCameraTarget: SairoMapCameraTarget? = null
    private var appliedCameraRequestId: Long? = null
    private var appliedZoomLevel: Int? = null

    fun attach(kakaoMap: KakaoMap) {
        this.kakaoMap = kakaoMap
        markerLayer = checkNotNull(kakaoMap.labelManager).addLayer(
            LabelLayerOptions.from(MarkerLayerId)
                .setCompetitionType(CompetitionType.None)
                .setOrderingType(OrderingType.Rank)
                .setClickable(false),
        )
        appliedMarkers = null
        appliedViewportPadding = null
        appliedCameraTarget = null
        appliedCameraRequestId = null
        appliedZoomLevel = null
        applyRequestedState()
    }

    fun detach() {
        kakaoMap = null
        markerLayer = null
        appliedMarkers = null
        appliedViewportPadding = null
        appliedCameraTarget = null
        appliedCameraRequestId = null
        appliedZoomLevel = null
    }

    fun update(
        markers: List<SairoMapMarker>,
        viewportPadding: SairoMapViewportPaddingPx,
        cameraTarget: SairoMapCameraTarget?,
        cameraRequestId: Long,
        zoomLevel: Int,
    ) {
        requestedMarkers = markers.sortedBy(SairoMapMarker::order)
        requestedViewportPadding = viewportPadding
        requestedCameraTarget = cameraTarget
        requestedCameraRequestId = cameraRequestId
        requestedZoomLevel = zoomLevel
        applyRequestedState()
    }

    fun reportError(error: Exception) {
        onMapError(error)
    }

    private fun applyRequestedState() {
        val map = kakaoMap ?: return
        val layer = markerLayer ?: return

        if (requestedViewportPadding != appliedViewportPadding) {
            map.setPadding(
                requestedViewportPadding.left,
                requestedViewportPadding.top,
                requestedViewportPadding.right,
                requestedViewportPadding.bottom,
            )
            appliedViewportPadding = requestedViewportPadding
        }

        if (requestedMarkers != appliedMarkers) {
            layer.removeAll()
            requestedMarkers.forEach { marker ->
                val markerBitmap = markerBitmapFactory.create(marker.order)
                val markerStyle = LabelStyle
                    .from(markerBitmap)
                    .setApplyDpScale(false)
                    .setAnchorPoint(MarkerAnchorHorizontal, MarkerAnchorVertical)

                layer.addLabel(
                    LabelOptions
                        .from(marker.id, LatLng.from(marker.latitude, marker.longitude))
                        .setStyles(markerStyle)
                        .setRank(marker.order.toLong())
                        .setClickable(false),
                )
            }
            appliedMarkers = requestedMarkers
        }

        val cameraTarget = requestedCameraTarget ?: requestedMarkers.firstOrNull()?.toCameraTarget()
        if (cameraTarget == null) {
            appliedCameraTarget = null
            appliedCameraRequestId = requestedCameraRequestId
            appliedZoomLevel = null
            return
        }
        if (
            cameraTarget != appliedCameraTarget ||
            requestedCameraRequestId != appliedCameraRequestId ||
            requestedZoomLevel != appliedZoomLevel
        ) {
            map.moveCamera(
                CameraUpdateFactory.newCenterPosition(
                    LatLng.from(cameraTarget.latitude, cameraTarget.longitude),
                    requestedZoomLevel,
                ),
            )
            appliedCameraTarget = cameraTarget
            appliedCameraRequestId = requestedCameraRequestId
            appliedZoomLevel = requestedZoomLevel
        }
    }
}

private data class SairoMapViewportPaddingPx(
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0,
)

private const val MarkerLayerId = "sairo-travel-course-markers"
private const val DefaultZoomLevel = 12
private const val InitialCameraRequestId = 0L
private const val MarkerAnchorHorizontal = 0.5f
private const val MarkerAnchorVertical = 0.78f

private fun SairoMapMarker.toCameraTarget(): SairoMapCameraTarget =
    SairoMapCameraTarget(latitude = latitude, longitude = longitude)

private fun MapView.resumeIfStarted() {
    if (isStarted() && !isResumed()) {
        resume()
    }
}

private fun MapView.pauseIfStarted() {
    if (isStarted() && !isPaused()) {
        pause()
    }
}
