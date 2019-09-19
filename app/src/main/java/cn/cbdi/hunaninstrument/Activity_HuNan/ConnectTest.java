package cn.cbdi.hunaninstrument.Activity_HuNan;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.Tool.FileUtils;
import cn.cbdi.log.Lg;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class ConnectTest {

    private String TAG = ConnectTest.class.getSimpleName();

    private SPUtils config = SPUtils.getInstance("config");

    private void recentPic(){
        RetrofitGenerator.getHnmbyApi()
                .recentPic("recentPic", config.getString("key"), "440982199104204312")
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            JSONObject jsonObject = new JSONObject(responseBody.string());
                            String ps = jsonObject.getString("result");
                            Bitmap bitmap = FileUtils.base64ToBitmap(ps);
                            Lg.e(TAG, "");

                        } catch (Exception e) {
                            Lg.e(TAG, e.toString());
                        }

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void faceUpload(){
        JSONObject jsonObject = new JSONObject();
        try {
            Keeper keeper = AppInit.getInstance().getDaoSession().queryRaw(Keeper.class,
                    "where CARD_ID = '" + "441302199308100538".toUpperCase() + "'").get(0);
            jsonObject.put("cardID",keeper.getCardID());
            jsonObject.put("name",keeper.getName());
            jsonObject.put("headphoto",keeper.getHeadphoto());
            jsonObject.put("headphotoRGB",keeper.getHeadphotoRGB());
            jsonObject.put("headphotoBW",keeper.getHeadphotoBW());
            jsonObject.put("feature",keeper.getFeature());
            jsonObject.put("naturalFace","true");
        }catch (Exception e){
            e.printStackTrace();
        }

        RetrofitGenerator.getHnmbyApi().withDataRs("faceUpload",config.getString("key"),jsonObject.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void getAllFace(){
        RetrofitGenerator.getHnmbyApi().withDataRr("getAllFace",config.getString("key"),null)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            JSONObject jsonObject = new JSONObject(responseBody.string());
                            if (("true").equals(jsonObject.getString("result"))) {
                                JSONArray jsonArray = jsonObject.getJSONArray("returnData");
                                ToastUtils.showLong(String.valueOf(jsonArray.length()));
                            }
                        }catch (Exception e){
                            Lg.e(TAG,e.toString());
                        }

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
