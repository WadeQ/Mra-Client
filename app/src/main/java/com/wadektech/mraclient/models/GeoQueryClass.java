package com.wadektech.mraclient.models;

import java.util.ArrayList;

/**
 * Created by WadeQ on 23/10/2020.
 */
public class GeoQueryClass {
  private String string;
  private ArrayList<Double> arrayList ;

  public GeoQueryClass() {
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public ArrayList<Double> getArrayList() {
    return arrayList;
  }

  public void setArrayList(ArrayList<Double> arrayList) {
    this.arrayList = arrayList;
  }
}
