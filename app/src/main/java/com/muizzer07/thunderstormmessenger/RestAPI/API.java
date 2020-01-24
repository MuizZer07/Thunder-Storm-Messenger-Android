package com.muizzer07.thunderstormmessenger.RestAPI;

import com.muizzer07.thunderstormmessenger.helpers.Constants;
import com.muizzer07.thunderstormmessenger.helpers.Credentials;
import com.muizzer07.thunderstormmessenger.notification.NotificationRequest;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface API {

    @Headers({
            "Authorization: key=" + Credentials.AUTH_TOKEN,
            "Content-Type:application/json"
    })
    @POST("send")
    Call<ResponseBody> sendNotification(
            @Body NotificationRequest requestNotificaton
    );
}
