package com.wadektech.mraclient.ui.home;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.security.keystore.UserNotAuthenticatedException;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteFragment;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.wadektech.mraclient.R;
import com.wadektech.mraclient.callbacks.IFirebaseFailedListener;
import com.wadektech.mraclient.callbacks.IFirebaseJobSeekerInfoListener;
import com.wadektech.mraclient.models.GeoQueryClass;
import com.wadektech.mraclient.models.JobSeekerGeolocation;
import com.wadektech.mraclient.models.JobSeekerInfo;
import com.wadektech.mraclient.models.LocationAnimation;
import com.wadektech.mraclient.remote.IGoogleAPI;
import com.wadektech.mraclient.remote.RetrofitClient;
import com.wadektech.mraclient.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class HomeFragment extends Fragment implements OnMapReadyCallback, IFirebaseFailedListener,
    IFirebaseJobSeekerInfoListener {
  HomeViewModel homeViewModel;
  GoogleMap mMap;
  SupportMapFragment mapFragment;
  FusedLocationProviderClient fusedLocationProviderClient;
  LocationCallback locationCallback;
  LocationRequest locationRequest;
  DatabaseReference onlineStatusRef, userRef, jobSeekerLocationRef;
  GeoFire geoFire;
  private double distance = 1.0;
  private static final double LIMIT_RANGE = 10.0;
  private Location previousLocation, currentLocation;
  IFirebaseJobSeekerInfoListener iFirebaseJobSeekerInfoListener;
  IFirebaseFailedListener iFirebaseFailedListener;
  boolean firstTime = true;
  CompositeDisposable compositeDisposable = new CompositeDisposable();
  private IGoogleAPI iGoogleAPI ;

  @BindView(R.id.activity_main)
  SlidingUpPanelLayout slidingUpPanelLayout;
  @BindView(R.id.tv_welcome_banner)
  TextView mWelcomeBanner ;
  private AutocompleteSupportFragment autocompleteSupportFragment;


  @Override
  public void onStop() {
    compositeDisposable.clear();
    super.onStop();
  }

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
  private String cityName;

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
    initSlidingPanelView(root);
    return root;

  }

  private void initSlidingPanelView(View root) {
    ButterKnife.bind(this,root);
    Constants.setSlidingPanelWelcomeBanner(mWelcomeBanner);
  }

  @SuppressLint("MissingPermission")
  private void initLocation() {
    Places.initialize(getContext(), getString(R.string.google_maps_key));
    autocompleteSupportFragment = (AutocompleteSupportFragment) getChildFragmentManager()
        .findFragmentById(R.id.autocomplete_fragment);
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID,
            Place.Field.ADDRESS, Place.Field.NAME,Place.Field.LAT_LNG));
        autocompleteSupportFragment.setHint(getString(R.string.where_to));
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
          @Override
          public void onPlaceSelected(@NonNull Place place) {
            Snackbar.make(mapFragment.requireView(), ""+place.getLatLng(),Snackbar.LENGTH_LONG).show();
          }

          @Override
          public void onError(@NonNull Status status) {
            Snackbar.make(mapFragment.requireView(), ""+status.getStatusMessage()
                ,Snackbar.LENGTH_LONG).show();
          }
        });

    iGoogleAPI = RetrofitClient.getInstance().create(IGoogleAPI.class);

    iFirebaseFailedListener = this;
    iFirebaseJobSeekerInfoListener = this;
    onlineStatusRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
    jobSeekerLocationRef = FirebaseDatabase.getInstance()
        .getReference(Constants.MRA_CLIENT_LOCATION_REFERENCE);
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

        //if user changes direction, we calculate distance and load job seeker again
        if (firstTime) {
          previousLocation = currentLocation = locationResult.getLastLocation();
          firstTime = false;

          setRestrictedPlacesInCountry(locationResult.getLastLocation());
        } else {
          previousLocation = currentLocation;
          currentLocation = locationResult.getLastLocation();
        }
        if (previousLocation.distanceTo(currentLocation) / 100 <= LIMIT_RANGE) {
          loadAvailableJobSeekers();
        } else {
          //TO-DO
        }

        geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
            new GeoLocation(locationResult.getLastLocation().getLatitude(),
                locationResult.getLastLocation().getLongitude()), (key, error) -> {
              if (error != null) {
                Snackbar.make(mapFragment.requireView(), error.getMessage(), Snackbar.LENGTH_LONG).show();
              } else {
                Snackbar.make(mapFragment.requireView(), "You are online...", Snackbar.LENGTH_LONG).show();
              }
            });
      }
    };

    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

    loadAvailableJobSeekers();
  }

  private void setRestrictedPlacesInCountry(Location lastLocation) {
    try {
      Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
      List<Address> addressList = geocoder.getFromLocation(lastLocation.getLatitude(),
          lastLocation.getLongitude(), 1);
      if (addressList.size() > 0)
          autocompleteSupportFragment.setCountry(addressList.get(0).getCountryCode());
    }  catch (IOException e) {
         e.printStackTrace();
    }
  }

  private void loadAvailableJobSeekers() {
    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(),
        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      Snackbar.make(requireView(), getString(R.string.permission_required), Snackbar.LENGTH_SHORT).show();
      return;
    }
    fusedLocationProviderClient
        .getLastLocation()
        .addOnFailureListener(e ->
            Snackbar.make(requireView(), "Error "+e.getMessage(), Snackbar.LENGTH_SHORT).show())
        .addOnSuccessListener(location -> {
          Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
          List<Address> addressList ;
          try {
            addressList = geocoder.getFromLocation(location.getLatitude(),
                location.getLongitude(),1);
            if (addressList.size() > 0)
                cityName = addressList.get(0).getLocality();
            if (!TextUtils.isEmpty(cityName)) {
              DatabaseReference dbRef = FirebaseDatabase
                  .getInstance()
                  .getReference(Constants.JOB_SEEKER_LOCATION_REFERENCE)
                  .child(cityName);
              GeoFire geoFire = new GeoFire(dbRef);
              GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(),
                  location.getLongitude()), distance);
              geoQuery.removeAllListeners();
              geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                  Constants.jobSeekerFound.add(new JobSeekerGeolocation(key, location));
                }

                @Override
                public void onKeyExited(String key) {

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {
                  if (distance <= LIMIT_RANGE) {
                    distance++;
                    //no job seekers around, search further
                    loadAvailableJobSeekers();
                  } else {
                    //reset the distance value to default
                    distance = 1.0;
                    addJobSeekerMarker();
                  }
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                  Snackbar.make(requireView(), "Error " + error.getMessage(), Snackbar.LENGTH_SHORT).show();
                }
              });

              dbRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                  GeoQueryClass geoQueryClass = snapshot.getValue(GeoQueryClass.class);
                  assert geoQueryClass != null;
                  GeoLocation geoLocation = new GeoLocation(geoQueryClass.getArrayList().get(0),
                      geoQueryClass.getArrayList().get(1));
                  JobSeekerGeolocation jobSeekerGeolocation = new JobSeekerGeolocation(snapshot.getKey(),
                      geoLocation);
                  Location loc = new Location("");
                  loc.setLatitude(geoLocation.latitude);
                  loc.setLongitude(geoLocation.longitude);
                  float currDistance = location.distanceTo(loc) / 1000;
                  if (currDistance <= LIMIT_RANGE)
                    findJobSeekerByKey(jobSeekerGeolocation);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
              });
            } else
              Snackbar.make(mapFragment.requireView(), getString(R.string.city_name_empty_exception),
                  Snackbar.LENGTH_LONG).show();
          } catch (IOException e) {
            Snackbar.make(requireView(), "Error "+e.getMessage(), Snackbar.LENGTH_SHORT).show();
          }
        });
  }

  @SuppressLint("CheckResult")
  private void addJobSeekerMarker() {
    if (Constants.jobSeekerFound.size() > 0){
      Observable
          .fromIterable(Constants.jobSeekerFound)
          .subscribeOn(Schedulers.newThread())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(this::findJobSeekerByKey, throwable -> {
            Snackbar.make(requireView(), "Error "+throwable.getMessage(), Snackbar.LENGTH_SHORT).show();
          }, () -> {});
    } else {
      Snackbar.make(requireView(), getString(R.string.job_seeker_not_found), Snackbar.LENGTH_SHORT).show();
    }
  }

  private void findJobSeekerByKey(JobSeekerGeolocation jobSeekerGeolocation) {
    FirebaseDatabase
        .getInstance()
        .getReference(Constants.JOB_SEEKER_INFO_REFERENCE)
        .child(jobSeekerGeolocation.getKey())
        .addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists()){
              jobSeekerGeolocation.setJobSeekerInfo(snapshot.getValue(JobSeekerInfo.class));
              iFirebaseJobSeekerInfoListener.onJobSeekerInfoLoadSuccess(jobSeekerGeolocation);
            } else {
              iFirebaseFailedListener.onFirebaseLoadFailed(getString(R.string.no_job_seeker_with_key)
              +jobSeekerGeolocation.getKey());
            }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {
            iFirebaseFailedListener.onFirebaseLoadFailed(error.getMessage());
          }
        });
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
            layoutParams.setMargins(0,0,0,250);
          }

          @Override
          public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
            Toast.makeText(getContext(), "Permission "+permissionDeniedResponse.getPermissionName()
                    +" was denied!", Toast.LENGTH_SHORT).show();
          }

          @Override
          public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest,
                                                         PermissionToken permissionToken) {

          }
        }).check();

    mMap.getUiSettings().setZoomControlsEnabled(true);

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

  @Override
  public void onFirebaseLoadFailed(String message) {
    Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show();
  }

  @Override
  public void onJobSeekerInfoLoadSuccess(JobSeekerGeolocation jobSeekerGeolocation) {
    if (!Constants.markerList.containsKey(jobSeekerGeolocation.getKey()))
    Constants.markerList.put(jobSeekerGeolocation.getKey(),
        mMap.addMarker(new MarkerOptions()
        .position(new LatLng(jobSeekerGeolocation.getGeoLocation().latitude,
            jobSeekerGeolocation.getGeoLocation().longitude))
        .flat(true)
            .title(Constants.buildName(jobSeekerGeolocation.getJobSeekerInfo()
            .getFirstName(),
                jobSeekerGeolocation.getJobSeekerInfo().getLastName()))
            .snippet(jobSeekerGeolocation.getJobSeekerInfo().getPhoneNumber())
            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.job_seeker_icon))));

    if (!TextUtils.isEmpty(cityName)){
      DatabaseReference databaseReference = FirebaseDatabase
          .getInstance()
          .getReference(Constants.JOB_SEEKER_LOCATION_REFERENCE)
          .child(cityName)
          .child(jobSeekerGeolocation.getKey());
      databaseReference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
          if (!snapshot.hasChildren()){
            if (Constants.markerList.get(jobSeekerGeolocation.getKey())!=null){
              //Remove marker
              Objects.requireNonNull(Constants.markerList.get(jobSeekerGeolocation.getKey())).remove();
              //Remove marker info from hash map
              Constants.markerList.remove(jobSeekerGeolocation.getKey());
              //Remove job seeker info too
              Constants.jobSeekerLocationSubscribe.remove(jobSeekerGeolocation.getKey());
              databaseReference.removeEventListener(this);

            }else {
              if (Constants.markerList.get(jobSeekerGeolocation.getKey())!=null){
                GeoQueryClass geoQuery = snapshot.getValue(GeoQueryClass.class);
                LocationAnimation locationAnimation = new LocationAnimation(false,geoQuery);
                if (Constants.jobSeekerLocationSubscribe.get(jobSeekerGeolocation.getKey())!= null){
                  Marker marker = Constants.markerList.get(jobSeekerGeolocation.getKey());
                  LocationAnimation oldPosition = Constants
                      .jobSeekerLocationSubscribe
                      .get(jobSeekerGeolocation.getKey());
                  assert oldPosition != null;
                  String from = oldPosition.getGeoQueryClass().getArrayList().get(0) +
                      "," +
                      oldPosition.getGeoQueryClass().getArrayList().get(1);

                  String to = locationAnimation.getGeoQueryClass().getArrayList().get(0) +
                      "," +
                      locationAnimation.getGeoQueryClass().getArrayList().get(1);

                  moveMarkerAnimation(jobSeekerGeolocation.getKey(),locationAnimation,marker,from,to);
                } else {
                  Constants.jobSeekerLocationSubscribe.put(jobSeekerGeolocation.getKey(),locationAnimation);
                }
              }
            }
          }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
          Snackbar.make(requireView(), error.getMessage(), Snackbar.LENGTH_SHORT).show();
        }
      });
    }
  }

  private void moveMarkerAnimation(String key, LocationAnimation locationAnimation, Marker marker,
                                   String from, String to) {
    if (!locationAnimation.isRun()){
      //Request api
      compositeDisposable.add(iGoogleAPI.getDirections(
          "Moving",
          "less_moving",
          from, to,
          getString(R.string.google_api_key))
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(returnResult ->{
            Timber.d("API Return%s", returnResult);
            try {
              //parse JSON
              JSONObject jsonObject = new JSONObject(returnResult);
              JSONArray jsonArray = jsonObject.getJSONArray("routes");
              for (int i = 0; i < jsonArray.length(); i++){
                JSONObject route = jsonArray.getJSONObject(i);
                JSONObject poly = route.getJSONObject("overview_polyline");
                String polyLine = poly.getString("points");
                locationAnimation.setPolyLineList(Constants.decodePoly(polyLine));
              }
              //moving
              locationAnimation.setIndex(-1);
              locationAnimation.setNext(1);

              Runnable runnable = () -> {
                if (locationAnimation.getPolyLineList() != null && locationAnimation
                    .getPolyLineList().size() > 1) {
                  if (locationAnimation.getIndex() < locationAnimation.getPolyLineList().size() - 2) {
//                    index++;
                    locationAnimation.setIndex(locationAnimation.getIndex() + 1);
                    locationAnimation.setNext(locationAnimation.getNext() + 1);
//                    start = locationAnimation.getPolyLineList().get(index);
                    locationAnimation.setStart(locationAnimation.getPolyLineList()
                        .get(locationAnimation.getIndex()));
//                    end = locationAnimation.getPolyLineList().get(next);
                    locationAnimation.setEnd(locationAnimation.getPolyLineList()
                        .get(locationAnimation.getNext()));
                  }

                  ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 1);
                  valueAnimator.setDuration(3000);
                  valueAnimator.setInterpolator(new LinearInterpolator());
                  valueAnimator.addUpdateListener(animation -> {
//                    v = animation.getAnimatedFraction();
                    locationAnimation.setV(valueAnimator.getAnimatedFraction());
                    locationAnimation.setLat(locationAnimation.getV() * locationAnimation.getEnd()
                        .latitude + (1 - locationAnimation.getV()) * locationAnimation.getStart().latitude);
//                    lat = v*end.latitude + (1-v) * start.latitude;
                    locationAnimation.setLng(locationAnimation.getV() * locationAnimation.getEnd()
                        .longitude + (1 - locationAnimation.getV()) * locationAnimation.getStart().longitude);
//                    lng = v*end.longitude + (1-v) * start.longitude;
                    LatLng newPos = new LatLng(locationAnimation.getLat(), locationAnimation.getLng());
                    marker.setPosition(newPos);
                    marker.setAnchor(0.5f, 0.5f);
                    marker.setRotation(Constants.getBearing(locationAnimation.getStart(), newPos));
                  });

                  valueAnimator.start();
                  //reach destination
                  if (locationAnimation.getIndex() < locationAnimation.getPolyLineList().size() - 2) {
                    locationAnimation.getHandler().postDelayed(HomeFragment.this::addJobSeekerMarker, 1500);
                    //Done
                  } else if (locationAnimation.getIndex() < locationAnimation.getPolyLineList().size() - 1) {
                    locationAnimation.setRun(false);
                    //update data
                    Constants.jobSeekerLocationSubscribe.put(key, locationAnimation);
                  }
                }
              };

              //Run handler
              locationAnimation.getHandler().postDelayed(runnable,1500);

            } catch (Exception e){
              Snackbar.make(mapFragment.requireView(), Objects.requireNonNull(e.getMessage()),
                  Snackbar.LENGTH_SHORT).show();
            }
          })
      );
    }
  }
}