package com.redforge.app.domain.schedule

data class TemplateDay(val name: String, val isRestDay: Boolean = false)

enum class SplitTemplate(val label: String, val days: List<TemplateDay>) {
    CUSTOM("Start blank", emptyList()),
    PPL(
        "Push / Pull / Legs",
        listOf(TemplateDay("Push Day"), TemplateDay("Pull Day"), TemplateDay("Leg Day"))
    ),
    UPPER_LOWER(
        "Upper / Lower",
        listOf(
            TemplateDay("Upper A"), TemplateDay("Lower A"), TemplateDay("Rest Day", isRestDay = true),
            TemplateDay("Upper B"), TemplateDay("Lower B")
        )
    ),
    FULL_BODY(
        "Full Body",
        listOf(TemplateDay("Full Body A"), TemplateDay("Full Body B"), TemplateDay("Full Body C"))
    )
}
