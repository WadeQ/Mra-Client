package com.wadektech.mraclient.models;

import com.firebase.geofire.GeoLocation;

/**
 * Created by WadeQ on 23/10/2020.
 */
public class JobSeekerGeolocation {
  private String key;
  private GeoLocation geoLocation ;
  private JobSeekerInfo jobSeekerInfo ;

  public JobSeekerGeolocation() {
  }

  public JobSeekerGeolocation(String key, GeoLocation geoLocation) {
    this.key = key;
    this.geoLocation = geoLocation;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public GeoLocation getGeoLocation() {
    return geoLocation;
  }

  public void setGeoLocation(GeoLocation geoLocation) {
    this.geoLocation = geoLocation;
  }

  public JobSeekerInfo getJobSeekerInfo() {
    return jobSeekerInfo;
  }

  public void setJobSeekerInfo(JobSeekerInfo jobSeekerInfo) {
    this.jobSeekerInfo = jobSeekerInfo;
  }
}
