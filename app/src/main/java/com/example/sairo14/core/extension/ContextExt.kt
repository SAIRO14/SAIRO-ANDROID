package com.example.sairo14.core.extension

import android.content.Context
import android.content.Intent

/** 주어진 제목과 텍스트를 Android 시스템 공유 화면으로 전달한다.
 *
 * 이 함수는 공유 앱 선택 화면만 열며 사용자가 실제로 공유를 완료했는지는 보장하지 않는다.
 * @param title 공유 콘텐츠의 제목
 * @param text 공유할 본문과 링크
 * @param chooserTitle 공유 앱 선택 화면에 표시할 제목
 */
fun Context.openTextShareSheet(
    title: String,
    text: String,
    chooserTitle: String,
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TITLE, title)
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, text)
    }

    startActivity(Intent.createChooser(sendIntent, chooserTitle))
}
