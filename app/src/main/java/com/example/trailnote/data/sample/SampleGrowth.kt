package com.example.trailnote.data.sample

import com.example.trailnote.domain.model.GrowthArea
import com.example.trailnote.domain.model.GrowthColorPalette
import com.example.trailnote.domain.model.GrowthTopic
import com.example.trailnote.domain.model.RepeatType
import com.example.trailnote.domain.model.Routine

object SampleGrowth {
    val areas = listOf(
        GrowthArea("sample-development", "개발", "코드를 읽고 수정한 내용을 짧게 정리합니다.", 1, 0, GrowthColorPalette.DEFAULT_COLOR),
        GrowthArea("sample-writing", "기록", "하루에 하나라도 생각을 남기는 습관을 만듭니다.", 1, 0, "#F9A8D4"),
        GrowthArea("sample-health", "건강", "작은 몸 관리 루틴으로 기본 컨디션을 지킵니다.", 1, 0, "#4ADE80")
    )

    val topics = listOf(
        GrowthTopic("sample-android", "sample-development", "Android", "앱 구조와 화면 동작을 조금씩 익힙니다.", 1, 0, 1),
        GrowthTopic("sample-memo-habit", "sample-writing", "메모 습관", "생각을 짧게 남기고 다시 볼 수 있게 합니다.", 1, 0, 1),
        GrowthTopic("sample-basic-fitness", "sample-health", "기본 체력", "부담 없는 움직임과 회복 루틴을 이어갑니다.", 1, 0, 1)
    )

    val routines = listOf(
        Routine("sample-review-code", "sample-android", "오늘 수정한 코드 5분 정리하기", "오늘 바꾼 파일과 이유를 한 줄씩 남깁니다.", true, true, RepeatType.Daily, false, 1),
        Routine("sample-read-android-docs", "sample-android", "Android 공식 문서 한 항목 읽기", "짧은 문서 하나를 읽고 핵심만 기록합니다.", false, true, RepeatType.Daily, false, 2),
        Routine("sample-write-ui-idea", "sample-android", "UI 개선 아이디어 하나 적기", "불편한 화면 하나와 개선 방향을 적어둡니다.", false, true, RepeatType.Daily, false, 3),
        Routine("sample-write-daily-memo", "sample-memo-habit", "하루 메모 1개 남기기", "오늘 떠오른 생각을 짧게 남깁니다.", true, true, RepeatType.Daily, false, 1),
        Routine("sample-summarize-learning", "sample-memo-habit", "오늘 배운 내용 한 줄 요약하기", "새로 알게 된 내용을 한 문장으로 정리합니다.", false, true, RepeatType.Daily, false, 2),
        Routine("sample-write-idea-title", "sample-memo-habit", "떠오른 아이디어 제목만 적기", "내용이 길지 않아도 제목만 먼저 남깁니다.", false, true, RepeatType.Daily, false, 3),
        Routine("sample-drink-water", "sample-basic-fitness", "물 한 컵 마시기", "작게 시작할 수 있는 컨디션 루틴입니다.", true, true, RepeatType.Daily, false, 1),
        Routine("sample-walk-10-min", "sample-basic-fitness", "10분 산책하기", "짧게 걸으며 몸을 깨웁니다.", false, true, RepeatType.Daily, false, 2),
        Routine("sample-stretch-neck", "sample-basic-fitness", "목과 어깨 스트레칭하기", "앉아 있는 시간이 길 때 긴장을 풀어줍니다.", false, true, RepeatType.Daily, false, 3)
    )
}
