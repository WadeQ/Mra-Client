package com.wadektech.mraclient.models;

/**
 * Created by WadeQ on 30/10/2020.
 */
public class LocationAnimation {
  private boolean isRun ;
  private GeoQueryClass geoQueryClass ;

  public LocationAnimation(boolean isRun, GeoQueryClass geoQueryClass) {
    this.isRun = isRun;
    this.geoQueryClass = geoQueryClass;
  }

  public boolean isRun() {
    return isRun;
  }

  public void setRun(boolean run) {
    isRun = run;
  }

  public GeoQueryClass getGeoQueryClass() {
    return geoQueryClass;
  }

  public void setGeoQueryClass(GeoQueryClass geoQueryClass) {
    this.geoQueryClass = geoQueryClass;
  }
}
