package cn.cbdi.hunaninstrument.Retrofit.ConnectApi;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface HNMBYApi {

    @FormUrlEncoded
    @POST("cjy/s/updata")
    Observable<String> withDataRs(@Field("dataType") String dataType, @Field("key") String key, @Field("jsonData") String jsonData);


    @FormUrlEncoded
    @POST("cjy/s/updata")
    Observable<ResponseBody> withDataRr(@Field("dataType") String dataType, @Field("key") String key, @Field("jsonData") String jsonData);


    @FormUrlEncoded
    @POST("cjy/s/updata")
    Observable<ResponseBody> recentPic(@Field("dataType") String dataType, @Field("key") String key,  @Field("idcard") String id);


    @FormUrlEncoded
    @POST("cjy/s/personInfo")
    Observable<ResponseBody> queryPersonInfo(@Field("dataType") String dataType, @Field("key") String key, @Field("idcard") String id);

    @FormUrlEncoded
    @POST("cjy/s/personInfo")
    Observable<String> syncPersonInfo(@Field("dataType") String dataType, @Field("key") String key, @Field("persionType") int persionType);

}
