package com.example.trailnote.data.sample

import com.example.trailnote.domain.model.Milestone
import com.example.trailnote.domain.model.Project
import com.example.trailnote.domain.model.ShortTask

object SampleProjects {
    val projects = listOf(
        Project(
            id = "planner-app",
            title = "플래너 앱",
            description = "TrailNote의 정보 구조와 XML 화면 흐름을 정리한다.",
            category = "모바일 앱",
            status = "진행중",
            targetDate = "2026-06-30"
        ),
        Project(
            id = "card-game",
            title = "카드 오목",
            description = "가벼운 전략 카드 게임의 규칙과 보상 구조를 설계한다.",
            category = "게임",
            status = "진행중",
            targetDate = "2026-07-15"
        ),
        Project(
            id = "party-game",
            title = "파티 심리 게임",
            description = "질문, 결과 화면, 진행 흐름을 정리한다.",
            category = "게임",
            status = "완료",
            targetDate = "2026-05-30"
        )
    )

    val milestones = listOf(
        Milestone("class-structure", "planner-app", "클래스 구조 설계", "도메인 모델과 화면 상태 구조를 정리한다.", 1, "2026-06-09"),
        Milestone("schedule", "planner-app", "개발 일정 분배", "1차 XML 전환부터 DB 전 단계까지 일정을 나눈다.", 2, "2026-06-14"),
        Milestone("project-ui", "planner-app", "프로젝트 UI 작업", "프로젝트 상세와 중기 목표 화면을 구성한다.", 3, "2026-06-18"),
        Milestone("rule-design", "card-game", "규칙 설계", "핵심 루프와 승리 조건을 정리한다.", 1, "2026-06-20"),
        Milestone("result-flow", "party-game", "결과 화면 구성", "질문 결과와 공유 흐름을 정리한다.", 1, "2026-05-28")
    )

    val shortTasks = listOf(
        ShortTask("entity-class", "class-structure", "Entity 클래스 구조 설계하기", true, 1),
        ShortTask("repository-interface", "class-structure", "Repository 인터페이스 정리하기", true, 2),
        ShortTask("usecase-structure", "class-structure", "UseCase 구조 정리하기", false, 3),
        ShortTask("screen-state", "class-structure", "화면 상태 모델 설계하기", false, 4),
        ShortTask("week-plan", "schedule", "주간 작업량 나누기", true, 1),
        ShortTask("risk-check", "schedule", "빌드 위험 요소 정리하기", false, 2),
        ShortTask("detail-layout", "project-ui", "프로젝트 상세 XML 만들기", true, 1),
        ShortTask("milestone-layout", "project-ui", "중기 목표 상세 XML 만들기", false, 2),
        ShortTask("card-rule", "rule-design", "카드 배치 규칙 쓰기", false, 1),
        ShortTask("score-rule", "rule-design", "점수 규칙 쓰기", false, 2),
        ShortTask("result-copy", "result-flow", "결과 문구 정리", true, 1)
    )
}
