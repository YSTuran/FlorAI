package yusufs.turan.florai.ui.history

internal fun String?.toDisplayDate(): String {
    if (isNullOrBlank()) return "Tarih bilgisi yok"
    return replace("T", " ")
        .substringBefore(".")
        .substringBefore("+")
        .take(16)
}
