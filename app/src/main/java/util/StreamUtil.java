package util;

import android.content.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * 创建时间：2018/10/22 18:08 <br>
 * 作者：JeffLee <br>
 * 描述：
 */
public class StreamUtil {
  public static String get(Context context, int id) {
    InputStream stream = context.getResources().openRawResource(id);
    return read(stream);
  }

  public static String read(InputStream stream) {
    return read(stream, "utf-8");
  }

  public static String read(InputStream is, String encode) {
    if (is != null) {
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, encode));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
          sb.append(line + "\n");
        }
        is.close();
        return sb.toString();
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return "";
  }
}
