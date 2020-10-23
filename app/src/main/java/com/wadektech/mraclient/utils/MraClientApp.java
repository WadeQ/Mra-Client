package com.wadektech.mraclient.utils;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by WadeQ on 23/10/2020.
 */
public class MraClientApp extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    Timber.plant(new Timber.DebugTree());
  }
}
