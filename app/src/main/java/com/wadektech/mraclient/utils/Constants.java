package com.wadektech.mraclient.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.wadektech.mraclient.R;
import com.wadektech.mraclient.models.JobSeekerGeolocation;
import com.wadektech.mraclient.models.LocationAnimation;
import com.wadektech.mraclient.models.MraClient;

import java.net.CookieHandler;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by WadeQ on 22/10/2020.
 */
public class Constants {
  public static final String MRA_CLIENT_INFO_REFERENCE = "ClientInfo";
  public static final String MRA_CLIENT_LOCATION_REFERENCE = "ClientLocation" ;
  public static final String TOKEN_REFERENCE = "ClientToken";
  public static final String NOTIFICATIONS_TITLE = "NotificationsTitle";
  public static final String NOTIFICATION_BODY = "NotificationsBody";
  public static final String JOB_SEEKER_LOCATION_REFERENCE = "JobSeekerLocation";
  public static final String JOB_SEEKER_INFO_REFERENCE = "JobSeekerInfo";
  public static MraClient currentUser;
  public static Set<JobSeekerGeolocation> jobSeekerFound = new HashSet<>();
  public static HashMap<String, Marker> markerList = new HashMap<>();
  public static HashMap<String, LocationAnimation> jobSeekerLocationSubscribe = new HashMap<>();


  public static String userWelcomeBanner() {
    if (Constants.currentUser != null){
      return "Welcome " +
          Constants.currentUser.getFirstName() +
          " " +
          Constants.currentUser.getLastName();
    } else {
      return "";
    }
  }

  public static void showNotifications(Context context, int id, String title, String body, Intent intent) {
    PendingIntent pendingIntent = null ;
    if (intent != null){
      pendingIntent = PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT);
      String NOTIFICATION_CHANNEL_ID = "WadekTechnologies" ;
      NotificationManager notificationManager = (NotificationManager) context
          .getSystemService(Context.NOTIFICATION_SERVICE);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
            "Mra Client",
            NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription("Mra Client");
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.BLUE);
        notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
        notificationChannel.enableVibration(true);
        notificationManager.createNotificationChannel(notificationChannel);
      }

      NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
      builder
          .setContentTitle(title)
          .setContentText(body)
          .setAutoCancel(false)
          .setPriority(NotificationCompat.PRIORITY_HIGH)
          .setDefaults(Notification.DEFAULT_VIBRATE)
          .setSmallIcon(R.drawable.ic_notification_icon)
          .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_notification_icon));
      if (pendingIntent != null){
        builder.setContentIntent(pendingIntent);
      }
      Notification notification = builder.build();
      notificationManager.notify(id,notification);
    }
  }

  public static String buildName(String firstName, String lastName) {
    return firstName + " " + lastName;
  }
//Decode poly
  public static List<LatLng> decodePoly(String polyLine) {
    List poly = new ArrayList();
    int index=0,len=polyLine.length();
    int lat=0,lng=0;
    while(index < len)
    {
      int b,shift=0,result=0;
      do{
        b=polyLine.charAt(index++)-63;
        result |= (b & 0x1f) << shift;
        shift+=5;

      }while(b >= 0x20);
      int dlat = ((result & 1) != 0 ? ~(result >> 1):(result >> 1));
      lat += dlat;

      shift = 0;
      result = 0;
      do{
        b = polyLine.charAt(index++)-63;
        result |= (b & 0x1f) << shift;
        shift +=5;
      }while(b >= 0x20);
      int dlng = ((result & 1)!=0 ? ~(result >> 1): (result >> 1));
      lng +=dlng;

      LatLng p = new LatLng((((double)lat / 1E5)),
          (((double)lng/1E5)));
      poly.add(p);
    }
    return poly;
  }
//Get bearing.
  public static float getBearing(LatLng start, LatLng newPos) {
    double lat = Math.abs(start.latitude - newPos.latitude);
    double lng = Math.abs(start.longitude - newPos.longitude);

    if (start.latitude < newPos.latitude && start.longitude < newPos.longitude)
      return (float) (Math.toDegrees(Math.atan(lng / lat)));
    else if (start.latitude >= newPos.latitude && start.longitude < newPos.longitude)
      return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
    else if (start.latitude >= newPos.latitude && start.longitude >= newPos.longitude)
      return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
    else if (start.latitude < newPos.latitude && start.longitude >= newPos.longitude)
      return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
    return -1;
  }

  public static void setSlidingPanelWelcomeBanner(TextView mWelcomeBanner) {
    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    if (hour >= 1 && hour <= 12)
      mWelcomeBanner.setText(new StringBuilder("Good morning client."));
    else if (hour >= 13 && hour <= 17)
      mWelcomeBanner.setText(new StringBuilder("Good afternoon client."));
    else
      mWelcomeBanner.setText(new StringBuilder("Good evening client."));
  }
}
