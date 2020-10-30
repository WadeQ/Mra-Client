package com.wadektech.mraclient.remote;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by WadeQ on 30/10/2020.
 */
public class RetrofitClient {
  private static Retrofit instance ;

  public static Retrofit getInstance(){
    return instance == null ? new Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build() : instance ;
  }
}
