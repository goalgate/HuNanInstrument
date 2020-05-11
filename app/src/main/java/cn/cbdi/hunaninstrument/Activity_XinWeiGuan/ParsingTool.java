package cn.cbdi.hunaninstrument.Activity_XinWeiGuan;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;

public class ParsingTool {


    public static String extractMainContent(ResponseBody body){
        try {
            JSONObject jsonObject = new JSONObject(body.string());
            String content = jsonObject.getString("data");
            return content;
        }catch (Exception e){
            try {
                return body.string();
            }catch (IOException e1){
                Log.e("ParsingTool",e1.toString());
            }

        }
        return null;
    }




}
