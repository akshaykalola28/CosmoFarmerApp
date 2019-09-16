package com.project.cosmofarmerapp.services;

import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIServices {

    @POST("user/register")
    Call<JsonObject> registerUser(@Body JsonObject jsonObject);

    @POST("user/login")
    Call<JsonObject> loginUser(@Body JsonObject jsonObject);

    @POST("crop/add")
    Call<JsonObject> addCrop(@Body JsonObject jsonObject);

    @POST("crop/update")
    Call<JsonObject> updateCrop(@Body JsonObject jsonObject);

    @GET("crop/{username}")
    Call<List<JsonObject>> getCropList(@Path("username") String username);

    @DELETE("crop/{username}/{keyId}")
    Call<JsonObject> deleteCrop(@Path("username")String username, @Path("keyId") String keyId);

    @GET("weather")
    Call<JsonObject> getWeather(@Query("lat") double lat, @Query("lon") double lon, @Query("appid") String appid);

    @GET("forecast")
    Call<JsonObject> getForecast(@Query("lat") double lat, @Query("lon") double lon, @Query("appid") String appid);

    @POST("land/add")
    Call<JsonObject> addLand(@Body JsonObject jsonObject);

    @POST("land/update")
    Call<JsonObject> updateLand(@Body JsonObject jsonObject);

    @GET("land/{username}")
    Call<List<JsonObject>> getLandList(@Path("username") String username);

    @DELETE("land/{username}/{keyId}")
    Call<JsonObject> deleteLand(@Path("username")String username, @Path("keyId") String keyId);

    @GET("crop/products")
    Call<List<JsonObject>> getProductList();

    @GET("videos/organic")
    Call<List<JsonObject>> getOrganicFramingVideoList();

    @GET("videos/innovation")
    Call<List<JsonObject>> getInnovationMachineVideoList();
}
