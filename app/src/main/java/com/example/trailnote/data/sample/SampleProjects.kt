package com.example.trailnote.data.sample

import com.example.trailnote.domain.model.Milestone
import com.example.trailnote.domain.model.Project
import com.example.trailnote.domain.model.ShortTask

object SampleProjects {
    val projects = listOf(
        Project(
            id = "sample-trailnote-improvement",
            title = "TrailNote 앱 개선",
            description = "앱의 핵심 화면을 직접 확인하며 사용 흐름을 다듬습니다.",
            category = "앱 개선",
            status = "진행중",
            targetDate = "미정"
        ),
        Project(
            id = "sample-portfolio-cleanup",
            title = "개인 포트폴리오 정리",
            description = "보여줄 프로젝트와 설명을 한곳에 정리합니다.",
            category = "포트폴리오",
            status = "진행중",
            targetDate = "미정"
        )
    )

    val milestones = listOf(
        Milestone("sample-home-usability", "sample-trailnote-improvement", "홈 화면 사용성 개선", "홈에서 반복해서 확인하는 정보와 동작을 점검합니다.", 1, "미정"),
        Milestone("sample-profile-stats", "sample-trailnote-improvement", "프로필 통계 정리", "프로필에 표시되는 통계와 대표 정보를 확인합니다.", 2, "미정"),
        Milestone("sample-project-list-cleanup", "sample-portfolio-cleanup", "프로젝트 목록 정리", "포트폴리오에 담을 프로젝트 후보를 좁힙니다.", 1, "미정")
    )

    val shortTasks = listOf(
        ShortTask("sample-check-today-goal-count", "sample-home-usability", "오늘 목표 수 설정 기능 확인", false, 1),
        ShortTask("sample-check-fixed-routine", "sample-home-usability", "고정 루틴 표시 상태 점검", false, 2),
        ShortTask("sample-align-checked-ui", "sample-home-usability", "체크 완료 UI 통일하기", false, 3),
        ShortTask("sample-count-completed-routines", "sample-profile-stats", "완료한 루틴 수 집계하기", false, 1),
        ShortTask("sample-check-profile-card", "sample-profile-stats", "대표 카드 표시 확인하기", false, 2),
        ShortTask("sample-define-stats-rule", "sample-profile-stats", "통계 집계 기준 정리하기", false, 3),
        ShortTask("sample-pick-portfolio-projects", "sample-project-list-cleanup", "대표 프로젝트 3개 고르기", false, 1),
        ShortTask("sample-write-project-features", "sample-project-list-cleanup", "각 프로젝트의 핵심 기능 적기", false, 2),
        ShortTask("sample-write-tech-stack", "sample-project-list-cleanup", "사용 기술 스택 정리하기", false, 3)
    )
}
