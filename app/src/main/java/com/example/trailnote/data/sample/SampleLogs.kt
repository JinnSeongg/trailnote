package com.example.trailnote.data.sample

import com.example.trailnote.domain.model.LogCategory
import com.example.trailnote.domain.model.LogCategoryType
import com.example.trailnote.domain.model.LogEntry
import com.example.trailnote.domain.model.LogTopic

object SampleLogs {
    val categories = listOf(
        LogCategory("memo", "메모", LogCategoryType.Memo),
        LogCategory("idea", "아이디어", LogCategoryType.Idea),
        LogCategory("resource", "자료", LogCategoryType.Resource),
        LogCategory("review", "회고", LogCategoryType.Review)
    )

    val topics = listOf(
        LogTopic("question", "memo", "의문", "계속 붙잡고 볼 질문", 1),
        LogTopic("keyword", "memo", "키워드", "반복해서 등장하는 개념", 2),
        LogTopic("game-material", "idea", "게임 소재", "게임으로 확장 가능한 소재", 1),
        LogTopic("ui-reference", "resource", "UI 참고", "레이아웃과 인터랙션 참고", 1),
        LogTopic("weekly-review", "review", "주간 회고", "작업 흐름과 문제 정리", 1)
    )

    val entries = listOf(
        LogEntry("entry-1", "question", "가속하는 기차 내부의 변화", "관찰자와 기준계가 달라질 때 설명이 어떻게 바뀌는지 정리한다.", "2026-05-21", "2026-05-21", listOf("물리", "질문")),
        LogEntry("entry-2", "question", "비탄성 기초 감각과 상호관계", "개념 사이의 연결을 짧은 문장으로 다시 쓴다.", "2026-05-21", "2026-05-21", listOf("메모")),
        LogEntry("entry-3", "keyword", "지식 기반 게임", "지식을 카드, 문제, 보상 구조로 바꾸는 방식을 모은다.", "2026-05-20", "2026-05-20", listOf("게임")),
        LogEntry("entry-4", "game-material", "체스 변형 게임", "말의 이동 규칙을 카드 효과로 바꿔보는 아이디어.", "2026-05-19", "2026-05-19", listOf("아이디어")),
        LogEntry("entry-5", "game-material", "오목 변형 카드", "턴마다 카드 효과로 판의 조건을 바꾸는 방식.", "2026-05-18", "2026-05-18", listOf("게임", "카드")),
        LogEntry("entry-6", "ui-reference", "미니멀 리스트 카드", "얇은 테두리와 낮은 대비로 정보 계층을 만든다.", "2026-05-17", "2026-05-17", listOf("UI")),
        LogEntry("entry-7", "weekly-review", "XML 전환 회고", "Compose 제거 후 Fragment 흐름과 ViewBinding 연결을 확인한다.", "2026-05-16", "2026-05-16", listOf("회고"))
    )
}
