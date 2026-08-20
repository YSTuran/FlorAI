package yusufs.turan.florai.core.network

import com.google.gson.JsonParser
import retrofit2.HttpException

object HttpErrorParser {
    fun detail(error: HttpException): String? {
        return runCatching {
            val rawBody = error.response()?.errorBody()?.string()
                ?: return@runCatching null
            val detail = JsonParser.parseString(rawBody)
                .asJsonObject
                .get("detail")
                ?: return@runCatching null

            if (detail.isJsonPrimitive) detail.asString else null
        }.getOrNull()
    }
}
