package cn.cbdi.hunaninstrument.Retrofit.ConnectApi;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface HebeiApi {

    @FormUrlEncoded
    @POST("da_gzmb_updata")
    Observable<String> faceUpload(@Field("dataType") String dataType, @Field("daid") String daid, @Field("pass") String pass, @Field("jsonData") String jsonData);

    @FormUrlEncoded
    @POST("da_gzmb_updata")
    Observable<String> recentPic(@Field("dataType") String dataType, @Field("daid") String daid, @Field("pass") String pass, @Field("idcard") String id);

    @FormUrlEncoded
    @POST("da_gzmb_updata")
    Observable<String> getAllFace(@Field("dataType") String dataType, @Field("daid") String daid, @Field("pass") String pass);


    @POST("da_gzmb_updata")
    Observable<String> GeneralUpdata(@QueryMap Map<String, String> map);


    @POST("da_gzmb_persionInfo")
    Observable<String> GeneralPersionInfo(@QueryMap Map<String, String> map);

}
