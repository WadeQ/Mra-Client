package com.wadektech.mraclient.callbacks;


import com.wadektech.mraclient.models.JobSeekerGeolocation;

/**
 * Created by WadeQ on 23/10/2020.
 */
public interface IFirebaseJobSeekerInfoListener {
  void onJobSeekerInfoLoadSuccess(JobSeekerGeolocation jobSeekerGeolocation);
}
