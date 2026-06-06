package com.example.trailnote.core.sample

import com.example.trailnote.core.model.AchievementSample
import com.example.trailnote.core.model.CheckItem
import com.example.trailnote.core.model.GrowthAreaSample
import com.example.trailnote.core.model.HomeGoalSettings
import com.example.trailnote.core.model.LogSample
import com.example.trailnote.core.model.ProjectSample
import com.example.trailnote.core.model.RepeatType
import com.example.trailnote.core.model.Routine
import com.example.trailnote.core.model.SummaryStat

object SampleData {
    const val homeTodayDateKey = "2026-05-21"

    val homeGoalSettings = HomeGoalSettings(
        randomTodayGoalCount = 4
    )

    val homeStats = listOf(
        SummaryStat("오늘 할 일", "12", "체크리스트"),
        SummaryStat("진행 중 프로젝트", "5", "활성"),
        SummaryStat("오늘 목표", homeGoalSettings.randomTodayGoalCount.toString(), "루틴")
    )

    val todayTasks = listOf(
        CheckItem("task-1", "우선순위 정리하기", true),
        CheckItem("task-2", "보드 화면 흐름 다시 보기", true),
        CheckItem("task-3", "식빵 사기"),
        CheckItem("task-4", "영상주제 메모")
    )

    val projects = listOf(
        ProjectSample("project-roguelike", "로그라이크 게임", "게임", "진행중", "전투 흐름과 카드 보상 구조 설계", 0.72f, "목표 5/7"),
        ProjectSample("project-card", "카드 오목", "웹페이지", "진행중", "가벼운 웹 게임 UI와 룰 정리", 0.40f, "목표 2/5"),
        ProjectSample("project-party", "파티 심리 게임", "웹페이지", "완료", "대화형 질문과 결과 화면 구성", 0.17f, "목표 1/6"),
        ProjectSample("project-planner", "플래너 앱", "모바일 앱", "진행중", "오늘 할 일과 성장 루틴을 묶는 앱", 0.60f, "목표 5/8")
    )

    val projectGoals = listOf(
        ProjectSample("mid-1", "앱 컨셉 기획", "플래너 앱", "진행중", "핵심 화면과 사용자 흐름 정리", 0.72f, "최근 기록 3시간 전"),
        ProjectSample("mid-2", "개발 일정 분배", "플래너 앱", "진행중", "1단계부터 DB 확장까지 마일스톤 정리", 0.60f, "최근 기록 1일 전"),
        ProjectSample("mid-3", "클래스 구조 설계", "플래너 앱", "진행중", "도메인 모델과 화면 상태 설계", 0.40f, "최근 기록 1일 전"),
        ProjectSample("mid-4", "UI/UX 구조 설계", "플래너 앱", "진행중", "홈, 프로젝트, 성장 화면 와이어프레임", 0.17f, "최근 기록 5일 전")
    )

    val projectChecklist = listOf(
        CheckItem("class-1", "앱 전역 상태 클래스 구조 설계하기"),
        CheckItem("class-2", "공통 데이터(Entity) 클래스 구조 설계하기"),
        CheckItem("class-3", "홈 화면 데이터 흐름 클래스 구조 설계하기"),
        CheckItem("class-4", "프로젝트 클래스 구조 설계하기"),
        CheckItem("class-5", "기록/아이디어 클래스 구조 설계하기"),
        CheckItem("class-6", "성장 루틴 반복 시스템 클래스 구조 설계하기"),
        CheckItem("class-7", "화면 간 이벤트/상호작용 클래스 구조 설계하기")
    )

    val logs = listOf(
        LogSample("log-1", "메모", "독서 관련 메모", listOf("기억에 남는 문장과 질문 정리", "비슷한 자료를 함께 모아두기")),
        LogSample("log-2", "자료", "키워드 정리", listOf("자기 계발, 화면 전환, 색상 대비", "이미지 텍스트와 섹션별 흐름")),
        LogSample("log-3", "아이디어", "게임 소재 아이디어", listOf("체스 변형 게임", "룰 카드와 수치형 보상", "오목 변형 카드 시스템"))
    )

    val growthAreas = listOf(
        GrowthAreaSample("drawing", "그림", 3, "빛의 방향, 구도, 그림자 공부", 0.60f),
        GrowthAreaSample("health", "건강", 2, "걷기와 기본 체력 루틴", 0.85f),
        GrowthAreaSample("reading", "독서", 4, "문장 수집과 회고 기록", 0.23f)
    )

    val growthDetails = listOf(
        GrowthAreaSample("light", "빛", 3, "빛의 방향, 강도, 그림자 공부", 0.60f),
        GrowthAreaSample("color", "색감", 2, "색의 분위기와 조화 연습", 0.60f),
        GrowthAreaSample("structure", "구도", 4, "시선 흐름과 화면 무게 연습", 0.60f),
        GrowthAreaSample("space", "공간/투시", 5, "원근감과 입체감 연습", 0.60f),
        GrowthAreaSample("croquis", "크로키", 1, "빠르게 관찰하고 표현하는 연습", 0.60f)
    )

    val routines = listOf(
        Routine(
            id = "routine-drawing-light",
            growthTopicId = "drawing",
            title = "그림 분석: 집중감 / 자연광 / 역광 / 반사광",
            repeatType = RepeatType.Daily,
            isFixed = true,
            isActive = true,
            order = 1
        ),
        Routine(
            id = "routine-health-walk",
            growthTopicId = "health",
            title = "가벼운 산책 20분",
            repeatType = RepeatType.Daily,
            isFixed = true,
            isActive = true,
            order = 2
        ),
        Routine(
            id = "routine-reading-novel",
            growthTopicId = "reading",
            title = "소설 1시간 읽기",
            repeatType = RepeatType.Daily,
            isFixed = false,
            isActive = true,
            order = 3
        ),
        Routine(
            id = "routine-reading-bible",
            growthTopicId = "reading",
            title = "구약 성경 한 장 읽기",
            repeatType = RepeatType.Daily,
            isFixed = false,
            isActive = true,
            order = 4
        ),
        Routine(
            id = "routine-drawing-croquis",
            growthTopicId = "drawing",
            title = "크로키 드로잉 3장",
            repeatType = RepeatType.Daily,
            isFixed = false,
            isActive = true,
            order = 5
        ),
        Routine(
            id = "routine-drawing-composition",
            growthTopicId = "drawing",
            title = "그림 분석: 시선 유도, 분위기, 대비",
            repeatType = RepeatType.Weekly,
            isFixed = false,
            isActive = true,
            order = 6
        ),
        Routine(
            id = "routine-coding-test",
            growthTopicId = "etc",
            title = "코딩 테스트 20분",
            repeatType = RepeatType.Daily,
            isFixed = false,
            isActive = true,
            order = 7
        )
    )

    fun growthTopicTitle(growthTopicId: String): String? {
        return growthAreas.firstOrNull { it.id == growthTopicId }?.title
    }

    val profileStats = listOf(
        SummaryStat("완료한 일", "461", "누적"),
        SummaryStat("진행중 프로젝트", "3", "현재"),
        SummaryStat("작성한 기록", "137", "누적"),
        SummaryStat("완료한 루틴", "238", "누적")
    )

    val activityStats = listOf(
        SummaryStat("할일 작성", "23개", "▲ 18%"),
        SummaryStat("기록 작성", "16개", "▲ 42%"),
        SummaryStat("루틴 완료", "42개", "▲ 25%"),
        SummaryStat("방문 일수", "16일", "▼ 12%")
    )

    val achievements = listOf(
        AchievementSample("기록 100회", "생각을 꾸준히 남김"),
        AchievementSample("루틴 30회", "반복을 만든 주간"),
        AchievementSample("연속 10일", "흐름을 유지함")
    )
}
