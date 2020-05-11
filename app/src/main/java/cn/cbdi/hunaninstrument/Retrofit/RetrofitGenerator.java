package cn.cbdi.hunaninstrument.Retrofit;

import com.blankj.utilcode.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Retrofit.ConnectApi.HNMBYApi;
import cn.cbdi.hunaninstrument.Retrofit.ConnectApi.HebeiApi;
import cn.cbdi.hunaninstrument.Retrofit.ConnectApi.LNApi;
import cn.cbdi.hunaninstrument.Retrofit.ConnectApi.SXApi;
import cn.cbdi.hunaninstrument.Retrofit.ConnectApi.WYYConnectApi;
import cn.cbdi.hunaninstrument.Retrofit.ConnectApi.XinWeiGuanApi;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Retrofit变量初始化
 * Created by SmileXie on 16/7/16.
 */
public class RetrofitGenerator {

    private static HNMBYApi hnmbyApi;

    private static WYYConnectApi wyyConnectApi;

    private static HebeiApi hebeiApi;

    private static SXApi sxApi;

    private static LNApi lnApi;

    private static XinWeiGuanApi xinWeiGuanApi;

    private HNMBYApi testHnmbyApi;

    private WYYConnectApi testWyyConnectApi;

    private HebeiApi testHebeiApi;

    private SXApi testSXApi;

    private LNApi testLNApi;

    private  XinWeiGuanApi testXinWeiGuanApi;


    private static OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
    private static Gson gson = new GsonBuilder()
            .setLenient()
            .create();

    private static <S> S createService(Class<S> serviceClass) {
        OkHttpClient client = okHttpClient.connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request()
                                .newBuilder()
                                .addHeader("Content-Type", "application/json; charset=UTF-8")
                                .build();

                        return chain.proceed(request);
                    }
                })
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .baseUrl(AppInit.getInstrumentConfig().getServerId()).client(client).build();
                .baseUrl(SPUtils.getInstance("config").getString("ServerId")).client(client).build();
        return retrofit.create(serviceClass);
    }

    private <S> S createService(Class<S> serviceClass, String url) {
        OkHttpClient client = okHttpClient.connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(url).client(client).build();
        return retrofit.create(serviceClass);
    }

    public HNMBYApi getHnmbyApi(String url) {
        if (testHnmbyApi == null) {
            testHnmbyApi = createService(HNMBYApi.class, url);
        }
        return testHnmbyApi;
    }

    public static HNMBYApi getHnmbyApi() {
        if (hnmbyApi == null) {
            hnmbyApi = createService(HNMBYApi.class);
        }
        return hnmbyApi;
    }

    public WYYConnectApi getWyyConnectApi(String url) {
        if (testWyyConnectApi == null) {
            testWyyConnectApi = createService(WYYConnectApi.class, url);
        }
        return testWyyConnectApi;
    }

    public static WYYConnectApi getWyyConnectApi() {
        if (wyyConnectApi == null) {
            wyyConnectApi = createService(WYYConnectApi.class);
        }
        return wyyConnectApi;
    }


    public HebeiApi getHebeiApi(String url) {
        if (testHebeiApi == null) {
            testHebeiApi = createService(HebeiApi.class, url);
        }
        return testHebeiApi;
    }

    public static HebeiApi getHebeiApi() {
        if (hebeiApi == null) {
            hebeiApi = createService(HebeiApi.class);
        }
        return hebeiApi;
    }


    public static SXApi getSxApi(){
        if(sxApi == null){
            sxApi = createService(SXApi.class);
        }
        return sxApi;
    }

    public SXApi getSXApi(String url) {
        if (testSXApi == null) {
            testSXApi = createService(SXApi.class, url);
        }
        return testSXApi;
    }

    public static LNApi getLNApi(){
        if(lnApi == null){
            lnApi = createService(LNApi.class);
        }
        return lnApi;
    }

    public LNApi getLNApi(String url) {
        if (testLNApi == null) {
            testLNApi = createService(LNApi.class, url);
        }
        return testLNApi;
    }

    public static XinWeiGuanApi getXinWeiGuanApi(){
        if(xinWeiGuanApi == null){
            xinWeiGuanApi = createService(XinWeiGuanApi.class);
        }
        return xinWeiGuanApi;
    }

    public XinWeiGuanApi getXinWeiGuanApi(String url) {
        if (testXinWeiGuanApi == null) {
            testXinWeiGuanApi = createService(XinWeiGuanApi.class, url);
        }
        return testXinWeiGuanApi;
    }

}
