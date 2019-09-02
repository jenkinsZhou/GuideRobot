package cn.tklvyou.guiderobot.api;


import cn.tklvyou.guiderobot.base.BaseResult;
import cn.tklvyou.guiderobot.model.LocationModel;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

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


}