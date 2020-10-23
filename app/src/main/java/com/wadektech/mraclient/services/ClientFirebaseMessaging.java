package com.wadektech.mraclient.services;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.wadektech.mraclient.utils.Constants;
import com.wadektech.mraclient.utils.MraClientUtils;
import java.util.Map;
import java.util.Random;

/**
 * Created by WadeQ on 23/10/2020.
 */
public class ClientFirebaseMessaging extends FirebaseMessagingService {
  @Override
  public void onNewToken(@NonNull String s) {
    super.onNewToken(s);
    if (FirebaseAuth.getInstance().getCurrentUser() != null){
      MraClientUtils.updateToken(this, s);
    }

  }

  @Override
  public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);
    Map<String,String> msg = remoteMessage.getData();
    if (msg!=null){
      Constants.showNotifications(
          this,
          new Random().nextInt(),
          msg.get(Constants.NOTIFICATIONS_TITLE),
          msg.get(Constants.NOTIFICATION_BODY),
          null
      );
    }
  }
}
