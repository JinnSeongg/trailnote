package com.example.trailnote.data.sample

import com.example.trailnote.domain.model.GrowthArea
import com.example.trailnote.domain.model.GrowthTopic
import com.example.trailnote.domain.model.RepeatType
import com.example.trailnote.domain.model.Routine

object SampleGrowth {
    val areas = listOf(
        GrowthArea("drawing", "그림", "빛, 색감, 구도, 크로키를 반복해서 연습한다.", 3, 60),
        GrowthArea("health", "건강", "걷기와 기본 체력 루틴을 유지한다.", 2, 85),
        GrowthArea("reading", "독서", "문장 수집과 생각 기록을 쌓는다.", 4, 23)
    )

    val topics = listOf(
        GrowthTopic("light", "drawing", "빛", "빛의 방향, 강도, 그림자 공부", 3, 60, 1),
        GrowthTopic("color", "drawing", "색감", "색의 분위기와 조화 공부", 2, 60, 2),
        GrowthTopic("structure", "drawing", "구도", "시선과 화면 무게 연습", 4, 60, 3),
        GrowthTopic("walk", "health", "걷기", "20분 이상 걷기 유지", 2, 80, 1),
        GrowthTopic("note", "reading", "독서 노트", "읽은 문장과 생각 기록", 4, 40, 1)
    )

    val routines = listOf(
        Routine("routine-light-1", "light", "그림 분석: 광원 찾기", "광원, 그림자, 반사광을 표시한다.", true, true, RepeatType.Daily, false, 1),
        Routine("routine-light-2", "light", "명암 단계 연습", "5단계 명암을 짧게 반복한다.", false, true, RepeatType.Daily, false, 2),
        Routine("routine-color-1", "color", "팔레트 수집", "마음에 드는 색 조합을 저장한다.", false, true, RepeatType.Weekly, false, 1),
        Routine("routine-structure-1", "structure", "구도 썸네일 3개", "같은 주제로 구도를 다르게 잡는다.", false, true, RepeatType.Daily, true, 1),
        Routine("routine-walk-1", "walk", "20분 걷기", "가벼운 산책을 한다.", true, true, RepeatType.Daily, true, 1),
        Routine("routine-note-1", "note", "문장 3개 옮겨 쓰기", "문장과 짧은 생각을 함께 적는다.", false, true, RepeatType.Daily, false, 1)
    )
}
