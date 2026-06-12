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
        LogTopic("sample-app-thoughts", "memo", "앱 사용 중 떠오른 생각", "사용하면서 바로 떠오른 개선점을 모읍니다.", 1),
        LogTopic("sample-feature-ideas", "idea", "기능 아이디어", "나중에 실험해볼 기능을 짧게 적어둡니다.", 1),
        LogTopic("sample-design-references", "resource", "참고한 디자인", "앱 톤에 맞는 화면 참고 자료를 정리합니다.", 1),
        LogTopic("sample-dev-review", "review", "개발 회고", "작업 후 남길 점과 다음 개선점을 기록합니다.", 1)
    )

    val entries = listOf(
        LogEntry("sample-memo-goal-count", "sample-app-thoughts", "오늘 목표는 너무 많으면 부담스럽다", "처음에는 적은 개수로 시작하고 익숙해지면 늘리는 편이 좋다.", "2026-06-08", "2026-06-08", listOf("홈", "목표")),
        LogEntry("sample-memo-checked-items", "sample-app-thoughts", "체크된 항목도 사라지지 않는 편이 좋다", "완료한 일을 눈으로 확인하면 하루 진행감이 더 잘 보인다.", "2026-06-08", "2026-06-08", listOf("홈", "체크")),
        LogEntry("sample-idea-daily-review", "sample-feature-ideas", "오늘의 회고 자동 생성", "완료한 일과 남긴 기록을 바탕으로 짧은 회고 초안을 만들 수 있다.", "2026-06-08", "2026-06-08", listOf("아이디어")),
        LogEntry("sample-idea-routine-difficulty", "sample-feature-ideas", "루틴 난이도 표시", "루틴마다 가벼움, 보통, 집중 같은 난이도를 표시하면 선택 부담이 줄어든다.", "2026-06-08", "2026-06-08", listOf("루틴")),
        LogEntry("sample-resource-minimal-card", "sample-design-references", "미니멀 카드형 UI", "정보는 카드 안에 담되 여백과 구분선은 과하지 않게 유지한다.", "2026-06-08", "2026-06-08", listOf("UI")),
        LogEntry("sample-review-single-source", "sample-dev-review", "데이터 소스를 하나로 모아야 한다", "화면마다 샘플을 직접 들고 있으면 실제 데이터와 어긋나기 쉽다.", "2026-06-08", "2026-06-08", listOf("구조"))
    )
}
