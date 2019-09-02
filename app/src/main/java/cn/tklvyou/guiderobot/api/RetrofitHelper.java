package cn.tklvyou.guiderobot.api;

import android.content.Intent;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import cn.tklvyou.guiderobot.base.BaseResult;
import cn.tklvyou.guiderobot.base.MyApplication;
import cn.tklvyou.guiderobot.common.Contacts;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Retrofit 辅助类
 */

public class RetrofitHelper {
    private static String TGA = "RetrofitHelper";
    private long CONNECT_TIMEOUT = 60L;
    private long READ_TIMEOUT = 30L;
    private long WRITE_TIMEOUT = 30L;
    private static RetrofitHelper mInstance = null;
    private Retrofit mRetrofit = null;


    public static RetrofitHelper getInstance() {
        synchronized (RetrofitHelper.class) {
            if (mInstance == null) {
                mInstance = new RetrofitHelper();
            }
        }
        return mInstance;
    }

    private RetrofitHelper() {
        init();
    }

    private void init() {
        resetApp();
    }

    private void resetApp() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(Contacts.DEV_BASE_URL)
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }


    /**
     * 获取OkHttpClient实例
     *
     * @return
     */

    private OkHttpClient getOkHttpClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new RqInterceptor())
                .addInterceptor(new ResponseInterceptor())
                .addInterceptor(new LoggingInterceptor())
                .build();
        return okHttpClient;
    }


    private static final String TAG = "RetrofitHelper";

    /**
     * 添加返回结果统一处理拦截器
     */
    private class ResponseInterceptor implements Interceptor {

        @Override
        public Response intercept(final Chain chain) throws IOException {
            // 原始请求
            Request request = chain.request();
            Response response = chain.proceed(request);
            ResponseBody responseBody = response.body();
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            String respString = source.buffer().clone().readString(Charset.defaultCharset());

            // TODO 这里判断是否是登录超时的情况
            BaseResult<Object> result = new Gson().fromJson(respString, BaseResult.class);
//            JSONObject j = null;
//            try {
//                j = new JSONObject(respString);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

//            if (result != null && result.getStatus() == 2) {
//                Log.d(TAG, "--->登录失效，自动重新登录");
//                SPUtils.getInstance().put("isLogin", false);  //首页中判断是否登录
//                SPUtils.getInstance().put("running", false);  //极光推送 - 首次使用初始化别名
//                SPUtils.getInstance().put("token", "");
//                Intent intent = new Intent(MyApplication.getAppContext(), AccountActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                MyApplication.getAppContext().startActivity(intent);
//            }
            // 这里与后台约定的状态码700表示登录超时【后台是java，客户端自己维护cookie，没有token机制。但此处如果刷新token，方法也一样】
//            if (j != null && j.optInt("status") == 700) {
//
//                // TODO 本地获取到之前的user信息
//                UserInfo user = SysApplication.getInstance().getDB().getCurrentUser();
//                if (user == null) {
//                    Log.d(TAG, "--->用户为空需要用户主动去登录");
//                    // 扔出需要手动重新登录的异常（BaseSubscriber里处理）
//                    throw new ExtraApiException(700, "请登录");
//                }
//                String phoneNum = user.getPhoneNum();
//                String password = user.getPass();
//                Call<JsonObject> call = getReloginService().reLogin(phoneNum, password);
//                JsonObject json = call.execute().body();
//                // 判断是否登录成功了
//                if (json.get("status").getAsInt() == 200) {
//                    // TODO 登录成功后，根据需要保存用户信息、会话信息等
//                    // 最重要的是将当前请求重新执行一遍!!!
//                    response = chain.proceed(request);
//                    Log.d(TAG, "--->完成二次请求");
//                } else {
//                    Log.d(TAG, "--->自动登录失败");
//                    // TODO 扔出需要手动重新登录的异常（BaseSubscriber里处理，此时已经是自动重新登录也不行，如密码在其他终端修改了之类的）
//                    throw new ExtraApiException(700, "请重新登录");
//                }
//            }
            return response;

        }
    }

    /**
     * 请求拦截器
     */
    private class RqInterceptor implements Interceptor {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request request = chain.request()
                    .newBuilder()
                    .addHeader("X-APP-TYPE", "android")
                    .build();
            Response response = chain.proceed(request);
            return response;
        }
    }

    /**
     * 日志拦截器
     */
    private class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            long t1 = System.nanoTime();//请求发起的时间

            String method = request.method();
            if ("POST".equals(method)) {
                StringBuilder sb = new StringBuilder();
                if (request.body() instanceof FormBody) {
                    FormBody body = (FormBody) request.body();
                    for (int i = 0; i < body.size(); i++) {
                        sb.append(body.encodedName(i))
                                .append("=")
                                .append(body.encodedValue(i))
                                .append(",");
                    }
                    sb.delete(sb.length() - 1, sb.length());
                    Log.d("---POST---",
                            String.format("发送请求 %s on %s %n%s %nRequestParams:{%s}",
                                    request.url(),
                                    chain.connection(),
                                    request.headers(),
                                    sb.toString()));
                }
            } else {
                Log.d("---GET---", String.format("发送请求 %s on %s%n%s",
                        request.url(),
                        chain.connection(),
                        request.headers()));
            }

            Response response = chain.proceed(request);
            long t2 = System.nanoTime();//收到响应事件

            ResponseBody responseBody = response.peekBody(1024 * 1024);//关键代码

            LogUtils.d(String.format("接收响应: [%s] %n返回json:【%s】 %.1fms %n%s",
                    response.request().url(),
                    responseBody.string(),
                    (t2 - t1) / 1e6d,
                    response.headers()
            ));

            return response;
        }
    }


    public ApiService getServer() {
        return mRetrofit.create(ApiService.class);
    }
}
