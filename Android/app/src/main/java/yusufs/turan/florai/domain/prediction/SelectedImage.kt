package yusufs.turan.florai.domain.prediction

data class SelectedImage(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray
) {
    val sizeBytes: Int = bytes.size
}
