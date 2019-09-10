package cn.tklvyou.guiderobot.api;


import cn.tklvyou.guiderobot.base.BaseResult;
import cn.tklvyou.guiderobot.model.AppConfigInfo;
import cn.tklvyou.guiderobot.model.LocationModel;
import cn.tklvyou.guiderobot.model.OrderInfo;
import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    /**
     * 提交位置信息
     */
    @FormUrlEncoded
    @POST("/swoft/add")
    Observable<BaseResult<Object>> addLocation(@Field("id") long id, @Field("name") String name);

    /**
     * 获取地点位置
     */
    @FormUrlEncoded
    @POST("/swoft/get")
    Observable<BaseResult<LocationModel>> getLocationMessage(@Field("id") long id);

    /**
     * 获取应用相关配置
     */
    @POST("/swoft/info")
    Observable<BaseResult<AppConfigInfo>> requestAppConfig();


    /**
     * 获取订单相关信息
     */

    @POST("/swoft/pay")
    Observable<BaseResult<OrderInfo>> requestOrderInfo();

}