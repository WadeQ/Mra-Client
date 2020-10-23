package com.wadektech.mraclient.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.wadektech.mraclient.R;
import com.wadektech.mraclient.utils.Constants;

import java.util.Objects;

import timber.log.Timber;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
  HomeViewModel homeViewModel;
  GoogleMap mMap;
  SupportMapFragment mapFragment;
  FusedLocationProviderClient fusedLocationProviderClient;
  LocationCallback locationCallback;
  LocationRequest locationRequest;
  DatabaseReference onlineStatusRef, userRef, jobSeekerLocationRef ;
  GeoFire geoFire;

  ValueEventListener onlineStatusValueEventListener = new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
      if (snapshot.exists())
        userRef.onDisconnect().removeValue();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
      Snackbar.make(mapFragment.requireView(), error.getMessage(), Snackbar.LENGTH_LONG).show();
    }
  };

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
    homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    View root = inflater.inflate(R.layout.fragment_home, container, false);

    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    mapFragment = (SupportMapFragment) getChildFragmentManager()
        .findFragmentById(R.id.map);
    assert mapFragment != null;
    mapFragment.getMapAsync(this);
    initLocation();
    return root;

  }

  @SuppressLint("MissingPermission")
  private void initLocation() {
    onlineStatusRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
    jobSeekerLocationRef = FirebaseDatabase.getInstance().getReference(Constants.MRA_CLIENT_LOCATION_REFERENCE);
    userRef = FirebaseDatabase.getInstance().getReference(Constants.MRA_CLIENT_LOCATION_REFERENCE)
        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
    geoFire = new GeoFire(jobSeekerLocationRef);

    updateSeekerOnlineStatus();

    locationRequest = new LocationRequest();
    locationRequest.setSmallestDisplacement(10f);
    locationRequest.setInterval(5000);
    locationRequest.setFastestInterval(3000);
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    locationCallback = new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);

        LatLng latLng = new LatLng(locationResult.getLastLocation().getLatitude(),
            locationResult.getLastLocation().getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f));

        geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
            new GeoLocation(locationResult.getLastLocation().getLatitude(),
                locationResult.getLastLocation().getLongitude()), (key, error) -> {
              if (error != null){
                Snackbar.make(mapFragment.requireView(), error.getMessage(), Snackbar.LENGTH_LONG).show();
              } else {
                Snackbar.make(mapFragment.requireView(), "You are online...", Snackbar.LENGTH_LONG).show();
              }
            });
      }
    };

    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
    fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    Dexter.withContext(getContext())
        .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        .withListener(new PermissionListener() {
          @SuppressLint("MissingPermission")
          @Override
          public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setOnMyLocationButtonClickListener(() -> {
              fusedLocationProviderClient.getLastLocation()
                  .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(),
                      Toast.LENGTH_SHORT).show())
                  .addOnSuccessListener(location -> {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,18f));
                  });
              return false;
            });
            View view = ((View) mapFragment.requireView().findViewById(Integer.parseInt("1")).getParent())
                .findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0,0,0,50);
          }

          @Override
          public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
            Toast.makeText(getContext(), "Permission "+permissionDeniedResponse.getPermissionName()+" was denied!",
                Toast.LENGTH_SHORT).show();
          }

          @Override
          public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

          }
        }).check();

    try {
      boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.mra_maps_style));
      if (!success)
        Timber.e("Error parsing map");
    } catch (Resources.NotFoundException notFoundException){
      Timber.e("Error %s", notFoundException.getMessage());
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    updateSeekerOnlineStatus();
  }

  private void updateSeekerOnlineStatus() {
    onlineStatusRef.addValueEventListener(onlineStatusValueEventListener);
  }

  @Override
  public void onDestroy() {
    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    geoFire.removeLocation(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
    onlineStatusRef.removeEventListener(onlineStatusValueEventListener);
    super.onDestroy();
  }
}