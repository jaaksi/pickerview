package util;

import android.support.annotation.Nullable;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * 数据解析工具类
 */

public class DataParseUtil {

  private static final String TAG = "DataParseUtil";

  /**
   * GSON转换类
   */
  public static final Gson mGson = new Gson();

  public static String toJson(Object object) {
    return mGson.toJson(object);
  }

  /**
   * 将JSON字符串转化成对象
   */
  public static <T> T fromJson(String json, Class<T> cls) {
    try {
      return mGson.fromJson(json, cls);
    } catch (JsonSyntaxException e) {
      Log.e(TAG, e.toString());
    }
    return null;
  }

  /**
   * 将字符串转化成对象集合
   *
   * @param cls 不能是简单数据类型，如String等
   */
  @Nullable
  public static <T> ArrayList<T> toList(String json, Class<T> cls) {
    Type type = new TypeToken<ArrayList<JsonObject>>() {
    }.getType();
    ArrayList<T> arrayList = null;
    try {
      ArrayList<JsonObject> jsonObjects = mGson.fromJson(json, type);
      if (jsonObjects != null && jsonObjects.size() > 0) {
        arrayList = new ArrayList<>();
        for (JsonObject jsonObject : jsonObjects) {
          arrayList.add(mGson.fromJson(jsonObject, cls));
        }
      }
    } catch (JsonSyntaxException e) {
      Log.e(TAG, e.toString());
    }
    return arrayList;
  }
}
