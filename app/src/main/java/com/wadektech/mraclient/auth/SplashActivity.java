package com.wadektech.mraclient.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.wadektech.mraclient.R;
import com.wadektech.mraclient.models.MraClient;
import com.wadektech.mraclient.ui.views.ClientHomeActivity;
import com.wadektech.mraclient.utils.Constants;
import com.wadektech.mraclient.utils.MraClientUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class SplashActivity extends AppCompatActivity {
  private final static int LOGIN_REQUEST_CODE = 1988;
  private List<AuthUI.IdpConfig> authProviders ;
  private FirebaseAuth firebaseAuth ;
  private FirebaseAuth.AuthStateListener authStateListener ;
  @BindView(R.id.auth_progress_bar)
  ProgressBar mProgress ;
  FirebaseDatabase db ;
  DatabaseReference dbRef ;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);
    init();
  }

  private void init() {
    ButterKnife.bind(this);
    db = FirebaseDatabase.getInstance();
    dbRef = db.getReference(Constants.MRA_CLIENT_INFO_REFERENCE);
    authProviders = Arrays.asList(
        new AuthUI.IdpConfig.PhoneBuilder().build(),
        new AuthUI.IdpConfig.GoogleBuilder().build()) ;

    firebaseAuth = FirebaseAuth.getInstance();
    authStateListener = myFirebaseAuth -> {
      FirebaseUser user = myFirebaseAuth.getCurrentUser();
      if (user != null){
        //update token
        FirebaseInstanceId
            .getInstance()
            .getInstanceId()
            .addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                Timber.e("Error saving token %s", e.getMessage());
                Toast.makeText(SplashActivity.this, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
              }
            })
            .addOnSuccessListener(instanceIdResult -> {
                  Timber.e("Saved token %s", instanceIdResult.getToken());
                  MraClientUtils.updateToken(SplashActivity.this, instanceIdResult.getToken());
                });
        signInReturningUser();

      } else {
        displaySignInLayout();

      }
    };
  }

  private void signInReturningUser() {
    dbRef.child(Objects.requireNonNull(
        FirebaseAuth
            .getInstance()
            .getCurrentUser())
        .getUid())
        .addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists()){
              MraClient mraClient = snapshot.getValue(MraClient.class);
              navigateToHome(mraClient);
            }else {
              displayUserRegistrationLayout();
            }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(SplashActivity.this, "Database error "+error.getMessage(),
                Toast.LENGTH_SHORT).show();
          }
        });
  }

  private void navigateToHome(MraClient mraClient) {
    Constants.currentUser = mraClient ;
    startActivity(new Intent(SplashActivity.this, ClientHomeActivity.class));
    finish();
  }

  private void displayUserRegistrationLayout() {
    AlertDialog.Builder mDialog = new AlertDialog.Builder(this, R.style.DialogTheme);
    @SuppressLint("InflateParams") View view = LayoutInflater.from(this)
        .inflate(R.layout.layout_sign_up,null);

    TextInputEditText et_first_name = view.findViewById(R.id.user_first_name);
    TextInputEditText et_last_name = view.findViewById(R.id.user_last_name);
    TextInputEditText et_phone = view.findViewById(R.id.et_reg_phone_number);
    Button btn_continue = view.findViewById(R.id.btn_register_user);

    //set data
    if (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()
        != null && !TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()));
    et_phone.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

    //set view
    mDialog.setView(view);
    AlertDialog dialog = mDialog.create();
    dialog.show();

    btn_continue.setOnClickListener(v -> {
      if (TextUtils.isEmpty(Objects.requireNonNull(et_first_name.getText()).toString())){
        et_first_name.setError("First name cannot be blank!");
      } else if (TextUtils.isEmpty(Objects.requireNonNull(et_last_name.getText()).toString())){
        et_last_name.setError("Last name cannot be blank!");
      } else if (TextUtils.isEmpty(Objects.requireNonNull(et_phone.getText()).toString())){
        et_phone.setError("Phone number cannot be blank!");
        dialog.dismiss();
      } else {
        MraClient mraClient = new MraClient();
        mraClient.setFirstName(et_first_name.getText().toString().trim());
        mraClient.setLastName(et_last_name.getText().toString().trim());
        mraClient.setPhoneNumber(et_phone.getText().toString().trim());
        mraClient.setRating(0.0);

        dbRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
            .setValue(mraClient)
            .addOnFailureListener(e -> {
              dialog.dismiss();
              Toast.makeText(this, "Error while saving user "+e.getMessage(),
                  Toast.LENGTH_SHORT).show();
            }).addOnSuccessListener(aVoid -> {
          dialog.dismiss();
          navigateToHome(mraClient);
        });
      }
    });
  }

  private void displaySignInLayout() {
    AuthMethodPickerLayout authMethodPickerLayout = new AuthMethodPickerLayout
        .Builder(R.layout.layout_sign_in)
        .setPhoneButtonId(R.id.btn_phone_sign_in)
        .setGoogleButtonId(R.id.btn_google_sign_in)
        .build() ;

    startActivityForResult(AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAuthMethodPickerLayout(authMethodPickerLayout)
        .setIsSmartLockEnabled(false)
        .setTheme(R.style.LoginTheme)
        .setAvailableProviders(authProviders)
        .build(), LOGIN_REQUEST_CODE);

  }

  @SuppressLint("CheckResult")
  private void initAuth() {
    mProgress.setVisibility(View.VISIBLE);
    Completable.timer(2, TimeUnit.SECONDS,
        AndroidSchedulers.mainThread())
        .subscribe(() ->
            firebaseAuth.addAuthStateListener(authStateListener)
        );
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == LOGIN_REQUEST_CODE){
      IdpResponse idpResponse = IdpResponse.fromResultIntent(data);
      if (resultCode == RESULT_OK){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
      } else {
        assert idpResponse != null;
        Toast.makeText(getApplicationContext(), "[Error]: "+
            Objects.requireNonNull(idpResponse.getError()).getMessage(), Toast.LENGTH_SHORT).show();
      }
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    initAuth();
  }

  @Override
  protected void onStop() {
    if (firebaseAuth != null && authStateListener != null){
      firebaseAuth.removeAuthStateListener(authStateListener);
    }
    super.onStop();
  }
}