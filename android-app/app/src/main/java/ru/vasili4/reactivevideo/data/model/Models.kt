package ru.vasili4.reactivevideo.data.model

data class UserCredentials(
    val login: String,
    val password: String,
)

data class UserResponse(
    val login: String,
)

data class ApiErrorResponse(
    val message: String? = null,
)

data class FileItem(
    val fileId: String,
    val bucket: String,
    val filePath: String,
) {
    val fileName: String
        get() = filePath.substringAfterLast("/")

    val type: FileType
        get() = FileType.from(filePath)
}

data class ImageRecognitionResponse(
    val labels: List<RecognitionLabel> = emptyList(),
)

data class RecognitionLabel(
    val name: String,
    val coords: List<Int>,
)

enum class FileType {
    IMAGE,
    GIF,
    VIDEO,
    TEXT,
    OTHER,
    ;

    val isVisual: Boolean
        get() = this == IMAGE || this == GIF || this == VIDEO

    companion object {
        fun from(filePath: String): FileType {
            return when (filePath.substringAfterLast('.', "").lowercase()) {
                "png", "jpg", "jpeg" -> IMAGE
                "gif" -> GIF
                "mp4" -> VIDEO
                "txt" -> TEXT
                else -> OTHER
            }
        }
    }
}
