package com.onthaset.app.reports

enum class ReportReason(val raw: String, val emoji: String) {
    InappropriateContent("Inappropriate Content", "⚠️"),
    Spam("Spam / Fake Event", "🚫"),
    HateSpeech("Hate Speech", "🛑"),
    WrongCategory("Wrong Category", "🏷️"),
    Duplicate("Duplicate Event", "📑"),
    Other("Other", "…"),
}
