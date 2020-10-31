package com.wadektech.mraclient.models;

import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by WadeQ on 30/10/2020.
 */
public class LocationAnimation {
  private boolean isRun ;
  private GeoQueryClass geoQueryClass ;
  private List<LatLng> polyLineList ;
  private Handler handler;
  private int index, next;
  private LatLng start , end;
  private float v ;
  private double lat, lng ;

  public LocationAnimation(boolean isRun, GeoQueryClass geoQueryClass) {
    this.isRun = isRun;
    this.geoQueryClass = geoQueryClass;
    this.handler = new Handler();
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

  public List<LatLng> getPolyLineList() {
    return polyLineList;
  }

  public void setPolyLineList(List<LatLng> polyLineList) {
    this.polyLineList = polyLineList;
  }

  public Handler getHandler() {
    return handler;
  }

  public void setHandler(Handler handler) {
    this.handler = handler;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public int getNext() {
    return next;
  }

  public void setNext(int next) {
    this.next = next;
  }

  public LatLng getStart() {
    return start;
  }

  public void setStart(LatLng start) {
    this.start = start;
  }

  public LatLng getEnd() {
    return end;
  }

  public void setEnd(LatLng end) {
    this.end = end;
  }

  public float getV() {
    return v;
  }

  public void setV(float v) {
    this.v = v;
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLng() {
    return lng;
  }

  public void setLng(double lng) {
    this.lng = lng;
  }
}
