package com.wadektech.mraclient.remote;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by WadeQ on 30/10/2020.
 */
public interface IGoogleAPI {

  @GET("maps/api/directions/json")
  Observable<String> getDirections(
      String moving, @Query("mode") String mode,
      @Query("transit_routing_preference") String transit_routing,
      @Query("origin") String origin,
      @Query("key") String key
  );
}
