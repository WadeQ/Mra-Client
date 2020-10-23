package com.wadektech.mraclient.ui.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wadektech.mraclient.R;
import com.wadektech.mraclient.auth.SplashActivity;
import com.wadektech.mraclient.utils.Constants;
import com.wadektech.mraclient.utils.MraClientUtils;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.HashMap;
import java.util.Map;

public class ClientHomeActivity extends AppCompatActivity {
  private AppBarConfiguration mAppBarConfiguration;
  private static final int IMAGE_PICKER_REQUEST_CODE = 1988;
  DrawerLayout drawer;
  NavigationView navigationView;
  NavController navController;
  AlertDialog alertDialog;
  StorageReference storageReference ;
  Uri imageUri;
  ImageView userProfileImage ;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_client_home);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    drawer = findViewById(R.id.drawer_layout);
    navigationView = findViewById(R.id.nav_view);
    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.
    mAppBarConfiguration = new AppBarConfiguration.Builder(
        R.id.nav_home)
        .setDrawerLayout(drawer)
        .build();
    navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
    NavigationUI.setupWithNavController(navigationView, navController);

    initParameters();
  }

  private void initParameters() {
    alertDialog = new AlertDialog.Builder(this)
        .setCancelable(false)
        .setMessage("Uploading...")
        .create();

    storageReference = FirebaseStorage.getInstance().getReference();
    navigationView.setNavigationItemSelectedListener(item -> {
      if (item.getItemId() == R.id.nav_sign_out) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClientHomeActivity.this);
        builder.setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss())
            .setPositiveButton("SIGN OUT", (dialog, which) -> ClientHomeActivity.this.signOutUser())
            .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
          dialog.getButton(AlertDialog.BUTTON_POSITIVE)
              .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
          dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
              .setTextColor(getResources().getColor(R.color.colorAccent));
        });
        dialog.show();
      }
      return true;
    });

    View view = navigationView.getHeaderView(0);
    TextView username = view.findViewById(R.id.tv_user_name);
    TextView userPhone = view.findViewById(R.id.tv_user_phone_number);
    TextView rating = view.findViewById(R.id.tv_user_rating);
    userProfileImage = view.findViewById(R.id.user_image_profile);

    userProfileImage.setOnClickListener(v -> {
      Intent intent = new Intent();
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE);
    });

    username.setText(Constants.userWelcomeBanner());
    userPhone.setText(Constants.currentUser != null ? Constants.currentUser.getPhoneNumber(): "");
    rating.setText(Constants.currentUser != null ? String.valueOf(Constants.currentUser.getRating()): "0.0");
    if (Constants.currentUser != null && Constants.currentUser.getImageUrl() != null &&
        !TextUtils.isEmpty(Constants.currentUser.getImageUrl())){
      Glide.with(this)
          .load(Constants.currentUser.getImageUrl())
          .into(userProfileImage);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK){
      if (data != null && data.getData() != null){
        imageUri = data.getData();
        userProfileImage.setImageURI(imageUri);
        showUploadProgress();
      }
    }
  }

  private void showUploadProgress() {
    AlertDialog.Builder builder = new AlertDialog.Builder(ClientHomeActivity.this);
    builder.setTitle("Change Profile Image")
        .setMessage("Are you sure you want to change image?")
        .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss())
        .setPositiveButton("UPLOAD", (dialog, which) -> {
          if (imageUri != null){
            alertDialog.setMessage("Upload started...");
            alertDialog.show();
            String key = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference rootRef = storageReference.child("profileImages/"+key);
            rootRef.putFile(imageUri)
                .addOnFailureListener(e -> {
                  alertDialog.dismiss();
                  Snackbar.make(drawer, "Upload error "
                      + e.getMessage(), Snackbar.LENGTH_LONG).show();
                })
                .addOnCompleteListener(task -> {
                  if (task.isSuccessful()) {
                    rootRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                      @Override
                      public void onSuccess(Uri uri) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("profileImages", uri.toString());
                        MraClientUtils.updateMraClientCredentials(drawer, map);
                      }
                    });
                  }
                  alertDialog.dismiss();
                }).addOnProgressListener(snapshot -> {
              double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
              alertDialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
            });
          }
        })
        .setCancelable(false);
    AlertDialog dialog = builder.create();
    dialog.setOnShowListener(dialog1 -> {
      dialog.getButton(AlertDialog.BUTTON_POSITIVE)
          .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
      dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
          .setTextColor(getResources().getColor(R.color.colorAccent));
    });
    dialog.show();
  }

  private void signOutUser() {
    FirebaseAuth.getInstance().signOut();
    Intent intent = new Intent(ClientHomeActivity.this, SplashActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.client_home, menu);
    return true;
  }

  @Override
  public boolean onSupportNavigateUp() {
    navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    return NavigationUI.navigateUp(navController, mAppBarConfiguration)
        || super.onSupportNavigateUp();
  }
}