package com.wadektech.mraclient.utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.wadektech.mraclient.models.Token;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;

/**
 * Created by WadeQ on 22/10/2020.
 */
public class MraClientUtils {
  public static void updateMraClientCredentials(View view, Map<String, Object> map){
    FirebaseDatabase
        .getInstance()
        .getReference(Constants.MRA_CLIENT_INFO_REFERENCE)
        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
        .updateChildren(map)
        .addOnFailureListener(e -> Snackbar.make(view, "Error updating profile!" + e.getMessage(),
            Snackbar.LENGTH_LONG).show())
        .addOnSuccessListener(aVoid -> Snackbar.make(view, "Profile updated successfully..",
            Snackbar.LENGTH_LONG).show());
  }

  public static void updateToken(Context context, String token) {
    Token userToken = new Token(token);
    FirebaseDatabase
        .getInstance()
        .getReference(Constants.TOKEN_REFERENCE)
        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
        .setValue(userToken)
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            Timber.e("Error saving token %s", e.getMessage());
            Toast.makeText(context, "Error saving token " + e.getMessage(), Toast.LENGTH_SHORT)
                .show();
          }
        }).addOnSuccessListener(aVoid -> {

                });
  }
}
