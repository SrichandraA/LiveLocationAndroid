package chandu.sharelocation.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.PUT;

/**
 * Created by Chandu on 1/31/2018.
 */

public interface ServiceApi {

    @FormUrlEncoded
    @PUT("/updatelocation")
    Call<ResponseBody> updateLocation(@Field("id") int id
                                      ,@Field("latitude") double latitude
                                      ,@Field("longitude") double longitude
    );
}
