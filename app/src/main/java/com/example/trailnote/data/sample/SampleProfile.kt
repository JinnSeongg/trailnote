package com.example.trailnote.data.sample

import com.example.trailnote.domain.model.Achievement
import com.example.trailnote.domain.model.ActivityRecord
import com.example.trailnote.domain.model.ProfileSummary

object SampleProfile {
    val summary = ProfileSummary(
        name = "진성",
        level = 3,
        exp = 42,
        completedTaskCount = 461,
        activeProjectCount = 3,
        logCount = 137,
        completedRoutineCount = 238
    )

    val activityRecords = listOf(
        ActivityRecord("record-1", "4/20", 25),
        ActivityRecord("record-2", "4/27", 14),
        ActivityRecord("record-3", "5/4", 74),
        ActivityRecord("record-4", "5/11", 16),
        ActivityRecord("record-5", "5/18", 40)
    )

    val achievements = listOf(
        achievement("task-complete-1", "첫 번째 체크", "오늘 할 일을 1개 완료하면 획득합니다.", "할 일", "일반", "✓"),
        achievement("task-complete-10", "작은 성공", "오늘 할 일을 누적 10개 완료하면 획득합니다.", "할 일", "일반", "✓"),
        achievement("task-complete-30", "하루 정리 입문", "오늘 할 일을 누적 30개 완료하면 획득합니다.", "할 일", "일반", "✓"),
        achievement("task-complete-50", "꾸준한 실행자", "오늘 할 일을 누적 50개 완료하면 획득합니다.", "할 일", "희귀", "✓"),
        achievement("task-complete-100", "할 일 관리자", "오늘 할 일을 누적 100개 완료하면 획득합니다.", "할 일", "희귀", "✓"),
        achievement("task-complete-200", "계획의 인간", "오늘 할 일을 누적 200개 완료하면 획득합니다.", "할 일", "희귀", "✓"),
        achievement("task-complete-300", "실행의 장인", "오늘 할 일을 누적 300개 완료하면 획득합니다.", "할 일", "유니크", "✓"),
        achievement("task-complete-500", "체크의 달인", "오늘 할 일을 누적 500개 완료하면 획득합니다.", "할 일", "유니크", "✓"),
        achievement("task-complete-1000", "천 번의 완료", "오늘 할 일을 누적 1000개 완료하면 획득합니다.", "할 일", "유니크", "✓"),

        achievement("project-complete-1", "첫 프로젝트 완성", "프로젝트를 1개 완료하면 획득합니다.", "프로젝트", "일반", "■"),
        achievement("project-complete-3", "작은 결과물들", "프로젝트를 누적 3개 완료하면 획득합니다.", "프로젝트", "일반", "■"),
        achievement("project-complete-5", "결과를 만드는 사람", "프로젝트를 누적 5개 완료하면 획득합니다.", "프로젝트", "일반", "■"),
        achievement("project-complete-10", "프로젝트 메이커", "프로젝트를 누적 10개 완료하면 획득합니다.", "프로젝트", "일반", "■"),
        achievement("project-complete-20", "완성의 기록", "프로젝트를 누적 20개 완료하면 획득합니다.", "프로젝트", "일반", "■"),
        achievement("project-complete-50", "마스터 빌더", "프로젝트를 누적 50개 완료하면 획득합니다.", "프로젝트", "희귀", "■"),

        achievement("short-task-complete-1", "첫 발걸음", "단기목표를 1개 완료하면 획득합니다.", "프로젝트", "일반", "◆"),
        achievement("short-task-complete-30", "작은 발자국", "단기목표를 누적 30개 완료하면 획득합니다.", "프로젝트", "일반", "◆"),
        achievement("short-task-complete-100", "실행 루틴", "단기목표를 누적 100개 완료하면 획득합니다.", "프로젝트", "희귀", "◆"),
        achievement("short-task-complete-300", "촘촘한 실행가", "단기목표를 누적 300개 완료하면 획득합니다.", "프로젝트", "희귀", "◆"),
        achievement("short-task-complete-500", "목표 추적자", "단기목표를 누적 500개 완료하면 획득합니다.", "프로젝트", "유니크", "◆"),
        achievement("short-task-complete-1000", "천 개의 발자국", "단기목표를 누적 1000개 완료하면 획득합니다.", "프로젝트", "유니크", "◆"),

        achievement("log-total-1", "첫 기록", "기록을 1개 작성하면 획득합니다.", "기록", "일반", "●"),
        achievement("log-total-10", "생각의 조각", "기록을 누적 10개 작성하면 획득합니다.", "기록", "일반", "●"),
        achievement("log-total-30", "기록 습관", "기록을 누적 30개 작성하면 획득합니다.", "기록", "일반", "●"),
        achievement("log-total-100", "기록가", "기록을 누적 100개 작성하면 획득합니다.", "기록", "희귀", "●"),
        achievement("log-total-300", "아카이브 구축", "기록을 누적 300개 작성하면 획득합니다.", "기록", "희귀", "●"),
        achievement("log-total-500", "개인 문서관", "기록을 누적 500개 작성하면 획득합니다.", "기록", "유니크", "●"),
        achievement("log-total-1000", "천 개의 기록", "기록을 누적 1000개 작성하면 획득합니다.", "기록", "유니크", "●"),

        achievement("log-memo-10", "메모의 시작", "메모 기록을 누적 10개 작성하면 획득합니다.", "기록", "일반", "M"),
        achievement("log-memo-50", "메모 습관", "메모 기록을 누적 50개 작성하면 획득합니다.", "기록", "희귀", "M"),
        achievement("log-memo-100", "생각 정리가", "메모 기록을 누적 100개 작성하면 획득합니다.", "기록", "희귀", "M"),
        achievement("log-idea-1", "첫 아이디어", "아이디어 기록을 1개 작성하면 획득합니다.", "기록", "일반", "I"),
        achievement("log-idea-10", "아이디어 수집가", "아이디어 기록을 누적 10개 작성하면 획득합니다.", "기록", "일반", "I"),
        achievement("log-idea-30", "번뜩이는 생각", "아이디어 기록을 누적 30개 작성하면 획득합니다.", "기록", "일반", "I"),
        achievement("log-idea-50", "아이디어 제조기", "아이디어 기록을 누적 50개 작성하면 획득합니다.", "기록", "희귀", "I"),
        achievement("log-idea-100", "발상의 창고", "아이디어 기록을 누적 100개 작성하면 획득합니다.", "기록", "희귀", "I"),
        achievement("log-resource-10", "자료 수집가", "자료 기록을 누적 10개 작성하면 획득합니다.", "기록", "일반", "R"),
        achievement("log-resource-30", "자료 관리자", "자료 기록을 누적 30개 작성하면 획득합니다.", "기록", "일반", "R"),
        achievement("log-resource-100", "자료 아카이브", "자료 기록을 누적 100개 작성하면 획득합니다.", "기록", "희귀", "R"),
        achievement("log-review-10", "회고 입문", "회고 기록을 누적 10개 작성하면 획득합니다.", "기록", "일반", "V"),
        achievement("log-review-50", "되돌아보는 사람", "회고 기록을 누적 50개 작성하면 획득합니다.", "기록", "희귀", "V"),
        achievement("log-review-100", "성장의 기록", "회고 기록을 누적 100개 작성하면 획득합니다.", "기록", "희귀", "V"),

        achievement("routine-complete-1", "첫 루틴 완료", "루틴을 1회 완료하면 획득합니다.", "루틴", "일반", "▲"),
        achievement("routine-complete-10", "루틴 입문자", "루틴을 누적 10회 완료하면 획득합니다.", "루틴", "일반", "▲"),
        achievement("routine-complete-30", "습관의 시작", "루틴을 누적 30회 완료하면 획득합니다.", "루틴", "일반", "▲"),
        achievement("routine-complete-50", "꾸준함의 증거", "루틴을 누적 50회 완료하면 획득합니다.", "루틴", "희귀", "▲"),
        achievement("routine-complete-100", "루틴 빌더", "루틴을 누적 100회 완료하면 획득합니다.", "루틴", "희귀", "▲"),
        achievement("routine-complete-300", "습관 설계자", "루틴을 누적 300회 완료하면 획득합니다.", "루틴", "희귀", "▲"),
        achievement("routine-complete-500", "루틴 마스터", "루틴을 누적 500회 완료하면 획득합니다.", "루틴", "유니크", "▲"),
        achievement("routine-complete-1000", "천 번의 반복", "루틴을 누적 1000회 완료하면 획득합니다.", "루틴", "유니크", "▲"),

        achievement("fixed-routine-1", "매일의 약속", "고정 루틴을 1개 보유하면 획득합니다.", "루틴", "일반", "F"),
        achievement("fixed-routine-3", "하루의 기본", "고정 루틴을 3개 보유하면 획득합니다.", "루틴", "일반", "F"),
        achievement("fixed-routine-5", "생활 설계", "고정 루틴을 5개 보유하면 획득합니다.", "루틴", "일반", "F"),
        achievement("fixed-routine-10", "고정 루틴 관리자", "고정 루틴을 10개 보유하면 획득합니다.", "루틴", "일반", "F"),
        achievement("fixed-routine-20", "흔들리지 않는 하루", "고정 루틴을 20개 보유하면 획득합니다.", "루틴", "일반", "F"),

        achievement("growth-area-1", "성장의 시작", "성장 분야를 1개 만들면 획득합니다.", "성장", "일반", "△"),
        achievement("growth-area-3", "관심사의 지도", "성장 분야를 3개 만들면 획득합니다.", "성장", "일반", "△"),
        achievement("growth-area-5", "넓어지는 성장", "성장 분야를 5개 만들면 획득합니다.", "성장", "일반", "△"),
        achievement("growth-area-10", "넓은 성장 지도", "성장 분야를 10개 만들면 획득합니다.", "성장", "일반", "△"),
        achievement("growth-topic-1", "구체화의 시작", "성장 하위 분야를 1개 만들면 획득합니다.", "성장", "일반", "▽"),
        achievement("growth-topic-5", "세분화의 첫 단계", "성장 하위 분야를 5개 만들면 획득합니다.", "성장", "일반", "▽"),
        achievement("growth-topic-10", "성장 설계자", "성장 하위 분야를 10개 만들면 획득합니다.", "성장", "일반", "▽"),
        achievement("growth-topic-20", "정교한 성장 지도", "성장 하위 분야를 20개 만들면 획득합니다.", "성장", "일반", "▽"),
        achievement("growth-topic-50", "자기계발 지도의 시작점", "성장 하위 분야를 50개 만들면 획득합니다.", "성장", "일반", "▽"),

        achievement("user-level-5", "성장의 첫 단계", "사용자 레벨 5에 도달하면 획득합니다.", "레벨", "일반", "L"),
        achievement("user-level-10", "꾸준한 사람", "사용자 레벨 10에 도달하면 획득합니다.", "레벨", "일반", "L"),
        achievement("user-level-15", "TrailNote 적응자", "사용자 레벨 15에 도달하면 획득합니다.", "레벨", "일반", "L"),
        achievement("user-level-20", "성장 궤도", "사용자 레벨 20에 도달하면 획득합니다.", "레벨", "일반", "L"),
        achievement("user-level-30", "자기관리 훈련자", "사용자 레벨 30에 도달하면 획득합니다.", "레벨", "일반", "L"),
        achievement("user-level-50", "TrailNote 마스터", "사용자 레벨 50에 도달하면 획득합니다.", "레벨", "유니크", "L"),
        achievement("user-level-100", "긴 여정의 기록자", "사용자 레벨 100에 도달하면 획득합니다.", "레벨", "유니크", "L"),

        achievement("visit-days-1", "첫 방문", "앱을 1일 방문하면 획득합니다.", "방문", "일반", "D"),
        achievement("visit-days-3", "다시 찾아온 사람", "앱을 누적 3일 방문하면 획득합니다.", "방문", "일반", "D"),
        achievement("visit-days-7", "일주일의 기록", "앱을 누적 7일 방문하면 획득합니다.", "방문", "일반", "D"),
        achievement("visit-days-30", "한 달의 흔적", "앱을 누적 30일 방문하면 획득합니다.", "방문", "희귀", "D"),
        achievement("visit-days-100", "백일의 기록", "앱을 누적 100일 방문하면 획득합니다.", "방문", "유니크", "D"),
        achievement("visit-days-365", "일 년의 여정", "앱을 누적 365일 방문하면 획득합니다.", "방문", "유니크", "D"),
        achievement("streak-days-3", "3일의 흐름", "3일 연속 방문하면 획득합니다.", "방문", "일반", "S"),
        achievement("streak-days-7", "일주일 연속", "7일 연속 방문하면 획득합니다.", "방문", "일반", "S"),
        achievement("streak-days-14", "두 주의 리듬", "14일 연속 방문하면 획득합니다.", "방문", "희귀", "S"),
        achievement("streak-days-30", "한 달의 루틴", "30일 연속 방문하면 획득합니다.", "방문", "희귀", "S"),
        achievement("streak-days-100", "백일의 습관", "100일 연속 방문하면 획득합니다.", "방문", "유니크", "S")
    )

    private fun achievement(
        id: String,
        title: String,
        description: String,
        category: String,
        grade: String,
        iconText: String
    ): Achievement {
        return Achievement(
            id = id,
            title = title,
            description = description,
            isUnlocked = false,
            category = category,
            grade = grade,
            iconText = iconText
        )
    }
}
