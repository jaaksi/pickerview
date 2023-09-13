package org.jaaksi.pickerview.demo.util

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


object DataParseUtil {

    /**
     * GSON转换类
     */
    val mGson = Gson()

    fun toJson(`object`: Any?): String? {
        return if (`object` == null) null else mGson.toJson(`object`)
    }

    /**
     * 将JSON字符串转化成对象
     */
    fun <T> fromJson(json: String?, cls: Class<T>): T? {
        try {
            return mGson.fromJson(json, cls)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
        }
        return null
    }

    fun <T> fromJson(json: String?, type: Type): T? {
        json ?: return null
        return try {
            mGson.fromJson(json, type)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }

    }

    inline fun <reified T> fromJsonArray(json: String?): T? {
        return try {
            mGson.fromJson<T>(json, object : TypeToken<T>() {}.type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}