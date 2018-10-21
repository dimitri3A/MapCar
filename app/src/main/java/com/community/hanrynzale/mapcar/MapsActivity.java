package com.community.hanrynzale.mapcar;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Looper;
import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import zemin.notification.NotificationDelegater;
import zemin.notification.NotificationLocal;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener,
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks{

    private GoogleMap mMap = null;
    public static final String USER_PROFIL_PATH = "user_profil_path";
    private static final String TAG_LOG = "log" ;
    private Marker myLocation = null;
    Location mLastLocation;
    private Boolean firstLocation;
    private Marker destination = null;
    String mapDestination;
    private LatLng destinationLatLng;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private LocationManager locationManager;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String PREFS = "PREFS";
    SharedPreferences sharedPreferences;
    private static final String PREFS_USER_STAT = "PREFS_USER_STAT";
    protected static final String USER_DISCONNECT = "DECONNECTED";
    private static final String PREFS_NOM_USER = "NOM_USER";
    private static final String PREFS_PRENOM_USER = "PRENOM_USER";
    protected static final String PREFS_CONTACT = "CONTACT_USER";
    private ImageView profil_image = null;
    private LocationRequest mLocationRequest;
    private Boolean destinationOK = false;
    private static final String LOG_TAG = "MapsActivity";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    LatLng latLngDestination = null;
    ImageButton setMyDestination = null;
    ImageButton setMyLocation = null;
    AppCompatButton commander = null;
    private static final LatLngBounds BOUNDS_GABON = new LatLngBounds(
            new LatLng(-3.9,8.7), new LatLng(2.283333,14.483333));
    private Marker pickupMarker;
    private LatLng pickupLocation;
    private Boolean requestBol = false;
    private LinearLayout mDriverInfo;
    private ImageView mDriverProfileImage;
    private TextView mDriverName, mDriverPhone, mDriverCar;
    private String responseDriver = "";
    AutoCompleteTextView mAutocompleteTextView = null;

    private NotificationDelegater mDelegater;
    private NotificationLocal mLocal;
    String CHANNEL_ID = "notification_taxi";

    File localFile = null;
    String modaleNom = "",modalePrenom = "",modaleContact = "",modaleImmatriculation = "21629 G1-U";

    private de.hdodenhof.circleimageview.CircleImageView driverButton;
    Bitmap bitmapPhotoDriver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        GoogleMapOptions options = new GoogleMapOptions();
        options.tiltGesturesEnabled(true);
        SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);
        getSupportFragmentManager().beginTransaction().replace(R.id.contentFragment, mapFragment).commit();
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        firstLocation = true;
        destinationLatLng = new LatLng(0.0,0.0);

        startService(new Intent(MapsActivity.this, onAppKilled.class));
        createNotificationChannel();

        mAutocompleteTextView =findViewById(R.id.autoCompleteTextView);
        mAutocompleteTextView.setSingleLine();
        mAutocompleteTextView.setEllipsize(TextUtils.TruncateAt.END);
        mAutocompleteTextView.setThreshold(3);
        mAutocompleteTextView.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,BOUNDS_GABON, null);
        mAutocompleteTextView.setAdapter(mPlaceArrayAdapter);

        sharedPreferences = getBaseContext().getSharedPreferences(PREFS, MODE_PRIVATE);
        String path = sharedPreferences.getString(USER_PROFIL_PATH, "");
        checkTempFile(path);

        setContentToolbar();

        mDriverInfo = findViewById(R.id.driverInfo);
        mDriverProfileImage = findViewById(R.id.driverProfileImage);
        mDriverName = findViewById(R.id.driverName);
        mDriverPhone = findViewById(R.id.driverPhone);
        mDriverCar = findViewById(R.id.driverCar);


        commander = findViewById(R.id.btn_commander);
        commander.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestBol){
                    endRide();
                }else{
                    commander.setEnabled(false);
                    requestBol = true;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("clientRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                    pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    commander.setText(R.string.textSearchTaxi);
                    getClosestDriver();
                }

            }
        });

        driverButton = findViewById(R.id.driver_button);
        driverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverButtonClicked(v);
            }
        });

    }

    public void driverButtonClicked(View view){
        LayoutInflater inflater = getLayoutInflater();
        View modale = inflater.inflate(R.layout.driver_profile, null);

        final TextView noms = modale.findViewById(R.id.name);
        String cNoms = modaleNom+" "+modalePrenom;
        noms.setText(cNoms);

        final TextView contact = modale.findViewById(R.id.contact);
        contact.setText(modaleContact);

        final TextView immatriculation = modale.findViewById(R.id.matricule);
        immatriculation.setText(modaleImmatriculation);

        final CircleImageView photo = modale.findViewById(R.id.profile_picture);

        if(bitmapPhotoDriver!=null){
            photo.setImageBitmap(bitmapPhotoDriver);
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        //alert.setTitle("Chauffeur");
        View v = getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);
        alert.setView(modale);

        AlertDialog dialog = alert.create();
        dialog.show();
    }


    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundID;

    GeoQuery geoQuery;
    private void getClosestDriver(){
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");

        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestBol){
                    DatabaseReference mClientDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Chauffeurs").child(key);
                    mClientDb.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                                if (driverFound){
                                    commander.setEnabled(true);
                                    return;
                                }

                                    driverFound = true;
                                    driverFoundID = dataSnapshot.getKey();

                                    final DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Chauffeurs").child(driverFoundID).child("clientRequest");
                                    String clientId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap<String, Object> map = new HashMap<String,Object>();
                                    map.put("IdClientCommande", clientId);
                                    map.put("destination", mapDestination);
                                    map.put("destinationLat", destinationLatLng.latitude);
                                    map.put("destinationLng", destinationLatLng.longitude);
                                    map.put("responseDriver", responseDriver);
                                    driverRef.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            driverRef.child("responseDriver").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.exists() && dataSnapshot.getValue().toString().equals("Ok")){
                                                        getDriverLocation();
                                                        getDriverInfo();
                                                        getHasRideEnded();
                                                        commander.setText(R.string.localisationOfTaxi);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    });

                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound)
                {
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private void getDriverLocation(){
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverFoundID).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestBol){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat,locationLng);
                    if(mDriverMarker != null){
                        mDriverMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance<100){
                        commander.setText("Le taxi est là");
                    }else{
                        commander.setText("Taxi trouvé: " + String.valueOf(distance));
                    }

                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("votre taxi").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void getDriverInfo(){
        final DatabaseReference mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Chauffeurs").child(driverFoundID);
        final StorageReference mDriverStorage = FirebaseStorage.getInstance().getReference().child("profile_images").child(driverFoundID);
        mDriverDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    if(dataSnapshot.child("nom").child("nom")!=null){
                        mDriverName.setText(dataSnapshot.child("nom").child("nom").getValue().toString());
                        modaleNom = dataSnapshot.child("nom").child("nom").getValue().toString();
                    }
                    if(dataSnapshot.child("nom").child("prenom")!=null){
                        modalePrenom = dataSnapshot.child("nom").child("prenom").getValue().toString();
                    }
                    if(dataSnapshot.child("contact")!=null){
                        mDriverPhone.setText(dataSnapshot.child("contact").getValue().toString());
                        modaleContact = dataSnapshot.child("contact").getValue().toString();
                    }
                    if(dataSnapshot.child("nom").child("profileImageUrl").getValue()!=null){
                        Glide.with(getApplication()).load(dataSnapshot.child("nom").child("profileImageUrl").getValue().toString()).into(mDriverProfileImage);
                        try {
                            localFile = File.createTempFile("images", "jpg");

                            mDriverStorage.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        bitmapPhotoDriver= decodeFile(localFile.getAbsolutePath());
                                        driverButton.setImageBitmap(bitmapPhotoDriver);
                                    sendLocalNotif();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MapsActivity.this, "Problème lors de la récupération de la photo de profil", Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else{
                        sendLocalNotif();
                    }
                    //mDriverInfo.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;
    private void getHasRideEnded(){
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Chauffeurs").child(driverFoundID).child("clientRequest").child("IdClientCommande");
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                }else{
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void endRide(){
        requestBol = false;
        geoQuery.removeAllListeners();
        driverLocationRef.removeEventListener(driverLocationRefListener);
        driveHasEndedRef.removeEventListener(driveHasEndedRefListener);

        if (driverFoundID != null){
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Chauffeurs").child(driverFoundID).child("clientRequest");
            driverRef.removeValue();
            driverFoundID = null;

        }
        driverFound = false;
        radius = 1;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("clientRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

        if(pickupMarker != null){
            pickupMarker.remove();
        }
        if (mDriverMarker != null){
            mDriverMarker.remove();
        }
        commander.setText("Commander une course");
        commander.setEnabled(true);
        destination.remove();
        commander.setVisibility(View.GONE);
        setMyDestination.setVisibility(View.GONE);
        driverButton.setVisibility(View.GONE);
        driverButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_person));
        getMyLocation();

        mDriverInfo.setVisibility(View.GONE);
        mDriverName.setText("");
        mDriverPhone.setText("");
        mDriverCar.setText("Destination: --");
        mDriverProfileImage.setImageResource(R.drawable.ic_person);
        if(localFile!=null){
            boolean r = localFile.delete();
        }
    }

    private void cancelRide(){
        requestBol = false;
        geoQuery.removeAllListeners();
        driverLocationRef.removeEventListener(driverLocationRefListener);
        driveHasEndedRef.removeEventListener(driveHasEndedRefListener);

        if (driverFoundID != null){
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Chauffeurs").child(driverFoundID).child("clientRequest");
            driverRef.removeValue();
            driverFoundID = null;

        }
        driverFound = false;
        radius = 1;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("clientRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

        if(pickupMarker != null){
            pickupMarker.remove();
        }
        if (mDriverMarker != null){
            mDriverMarker.remove();
        }
        commander.setText("Commander une course");
        commander.setEnabled(true);

        mDriverInfo.setVisibility(View.GONE);
        mDriverName.setText("");
        mDriverPhone.setText("");
        mDriverCar.setText("Destination: --");
        mDriverProfileImage.setImageResource(R.drawable.ic_person);
    }

    private void cancelSearchRide(){

        radius = 1;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("clientRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

        if(pickupMarker != null){
            pickupMarker.remove();
        }
        commander.setText("Commander une course");
        commander.setEnabled(true);
        commander.setVisibility(View.GONE);

    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(LOG_TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i(LOG_TAG, "Fetching details for ID: " + item.placeId);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            CharSequence attributions = places.getAttributions();

            /*mNameTextView.setText(Html.fromHtml(place.getName() + ""));
            */
            
            if (destinationOK)destination.remove();
            mapDestination = place.getName().toString();
            latLngDestination = place.getLatLng();
            destinationLatLng = place.getLatLng();

            destination = mMap.addMarker(new MarkerOptions()
                    .position(latLngDestination)
                    .title("Destination")
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .anchor(0.5f,1));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLngDestination)
                    .tilt(45)
                    .zoom(15)
                    .bearing(0)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            destinationOK = true;
            if(setMyDestination!=null){
                setMyDestination.setVisibility(View.VISIBLE);
                commander.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(LOG_TAG, "Google Places API connected.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    class PlaceArrayAdapter
            extends ArrayAdapter<PlaceArrayAdapter.PlaceAutocomplete> implements Filterable {
        private static final String TAG = "PlaceArrayAdapter";
        private GoogleApiClient mGoogleApiClient;
        private AutocompleteFilter mPlaceFilter;
        private LatLngBounds mBounds;
        private ArrayList<PlaceAutocomplete> mResultList;

        /**
         * Constructor
         *
         * @param context  Context
         * @param resource Layout resource
         * @param bounds   Used to specify the search bounds
         * @param filter   Used to specify place types
         */
        public PlaceArrayAdapter(Context context, int resource, LatLngBounds bounds,
                                 AutocompleteFilter filter) {
            super(context, resource);
            mBounds = bounds;
            mPlaceFilter = filter;
        }

        public void setGoogleApiClient(GoogleApiClient googleApiClient) {
            if (googleApiClient == null || !googleApiClient.isConnected()) {
                mGoogleApiClient = null;
            } else {
                mGoogleApiClient = googleApiClient;
            }
        }

        @Override
        public int getCount() {
            return mResultList.size();
        }

        @Override
        public PlaceAutocomplete getItem(int position) {
            return mResultList.get(position);
        }

        private ArrayList<PlaceAutocomplete> getPredictions(CharSequence constraint) {
            if (mGoogleApiClient != null) {
                Log.i(TAG, "Executing autocomplete query for: " + constraint);
                PendingResult<AutocompletePredictionBuffer> results =
                        Places.GeoDataApi
                                .getAutocompletePredictions(mGoogleApiClient, constraint.toString(),
                                        mBounds, mPlaceFilter);
                // Wait for predictions, set the timeout.
                AutocompletePredictionBuffer autocompletePredictions = results
                        .await(60, TimeUnit.SECONDS);
                final Status status = autocompletePredictions.getStatus();
                if (!status.isSuccess()) {
                    Toast.makeText(getContext(), "Error: " + status.toString(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error getting place predictions: " + status
                            .toString());
                    autocompletePredictions.release();
                    return null;
                }

                Log.i(TAG, "Query completed. Received " + autocompletePredictions.getCount()
                        + " predictions.");
                Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
                ArrayList resultList = new ArrayList<>(autocompletePredictions.getCount());
                while (iterator.hasNext()) {
                    AutocompletePrediction prediction = iterator.next();
                    resultList.add(new PlaceAutocomplete(prediction.getPlaceId(),
                            prediction.getFullText(null)));
                }
                // Buffer release
                autocompletePredictions.release();
                return resultList;
            }
            Log.e(TAG, "Google API client is not connected.");
            return null;
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    if (constraint != null) {
                        // Query the autocomplete API for the entered constraint
                        mResultList = getPredictions(constraint);
                        if (mResultList != null) {
                            // Results
                            results.values = mResultList;
                            results.count = mResultList.size();
                        }
                    }
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        // The API returned at least one result, update the data.
                        notifyDataSetChanged();
                    } else {
                        // The API did not return any results, invalidate the data set.
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }

        class PlaceAutocomplete {

            public CharSequence placeId;
            public CharSequence description;

            PlaceAutocomplete(CharSequence placeId, CharSequence description) {
                this.placeId = placeId;
                this.description = description;
            }

            @Override
            public String toString() {
                return description.toString();
            }
        }
    }

    private void setupProfilUser() {
        String path= sharedPreferences.getString(USER_PROFIL_PATH,null);
        if (path!=null){
            Bitmap bitmap= decodeFile(path);
            profil_image = findViewById(R.id.profileToolbar_user);
            profil_image.setImageBitmap(bitmap);
            if(myLocation!=null){
                Bitmap user_i = null;
                user_i = getCircularBitmap(bitmap);
                user_i = addBorderToCircularBitmap(user_i,5,Color.WHITE);
                user_i = addShadowToCircularBitmap(user_i,4,Color.LTGRAY);
                user_i = Bitmap.createScaledBitmap(user_i,72,72,false);
                myLocation.setIcon(BitmapDescriptorFactory.fromBitmap(user_i));
            }
        }
    }

    public Bitmap getCircularBitmap(Bitmap bitmap){
        int squareBitmapWidth = Math.min(bitmap.getWidth(),bitmap.getHeight());
        Bitmap output =Bitmap.createBitmap(squareBitmapWidth,squareBitmapWidth, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        Rect rect = new Rect(0,0,squareBitmapWidth,squareBitmapWidth);
        RectF rectF = new RectF(rect);

        canvas.drawOval(rectF,paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        float left = (squareBitmapWidth-bitmap.getWidth())/2;
        float top = (squareBitmapWidth-bitmap.getHeight())/2;

        canvas.drawBitmap(bitmap,left,top,paint);
        bitmap.recycle();

        return output;
    }

    protected Bitmap addBorderToCircularBitmap(Bitmap srcBitmap, int borderWidth, int borderColor){
        int dstBitmapWidth = srcBitmap.getWidth()+borderWidth*2;

        Bitmap dstBitmap = Bitmap.createBitmap(dstBitmapWidth,dstBitmapWidth,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(dstBitmap);
        canvas.drawBitmap(srcBitmap,borderWidth,borderWidth,null);

        Paint paint = new Paint();
        paint.setColor(borderColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);
        paint.setAntiAlias(true);

        canvas.drawCircle(
                canvas.getWidth()/2,
                canvas.getWidth()/2,
                canvas.getWidth()/2 - borderWidth/2,
                paint
        );

        srcBitmap.recycle();

        return  dstBitmap;
    }

    protected Bitmap addShadowToCircularBitmap(Bitmap srcBitmap,int shadowWidth,int shadowColor){
        int dstBitmapWidth = srcBitmap.getWidth()+shadowWidth*2;
        Bitmap dstBitmap = Bitmap.createBitmap(dstBitmapWidth,dstBitmapWidth,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(dstBitmap);
        canvas.drawBitmap(srcBitmap,shadowWidth,shadowWidth,null);

        Paint paint = new Paint();
        paint.setColor(shadowColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(shadowWidth);
        paint.setAntiAlias(true);

        canvas.drawCircle(
                dstBitmapWidth/2,
                dstBitmapWidth/2,
                dstBitmapWidth/2 - shadowWidth/2,
                paint
        );

        srcBitmap.recycle();

        return  dstBitmap;
    }

    public void setContentToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        String path= sharedPreferences.getString(USER_PROFIL_PATH,null);
        if (path!=null){
            Bitmap bitmap= decodeFile(path);
            profil_image = findViewById(R.id.profileToolbar_user);
            profil_image.setImageBitmap(bitmap);
        }

        profil_image = findViewById(R.id.profileToolbar_user);
        setupProfilUser();
        profil_image.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                Intent intent = new Intent(MapsActivity.this, ScrollingActivity.class);
                startActivity(intent);
            }
        });


    }

    @Override
    public void onBackPressed() {
        if(destination!=null && destinationOK){
            destination.remove();
            destinationOK = false;
            setMyDestination.setVisibility(View.GONE);
            commander.setVisibility(View.GONE);
            if(requestBol){
                buildAlertMessageEndRide(driverFound);
            }
            commander.setText("Commander une course");
            driverButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_person));
            if(localFile!=null){
                localFile.delete();
            }
            driverButton.setVisibility(View.GONE);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            userSettings();
            return true;
        }
        if (id == R.id.action_logout){
            userLogout();
            return true;
        }
        if (id == R.id.action_paiement){
            return true;
        }
        if (id == R.id.action_service){
            return true;
        }
        if (id == R.id.action_about){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void userSettings() {
        Intent intent = new Intent(MapsActivity.this, UpdateEmailPassword.class);
        startActivity(intent);
    }

    private void userLogout(){

        new AlertDialog.Builder(this)
                .setTitle("Déconnexion")
                .setMessage("Vous allez etre déconnecter")
                .setNegativeButton(R.string.non, null)
                .setPositiveButton(R.string.oui, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        sharedPreferences.edit()
                                .putString(PREFS_USER_STAT, USER_DISCONNECT)
                                .putString(PREFS_NOM_USER,null)
                                .putString(PREFS_PRENOM_USER,null)
                                .putString(PREFS_CONTACT,null)
                                .putString(USER_PROFIL_PATH,null)
                                .apply();
                        Toast.makeText(getBaseContext(), "Déconnexion", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }).create().show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try{
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.bright_bubbly));
            if(!success){
                Log.e(TAG, "Style parsing failed");
            }
        }catch (Resources.NotFoundException e){
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        mMap = googleMap;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            buildAlertMessageNoGps();
        }

        Bitmap userIcon;
        String path= sharedPreferences.getString(USER_PROFIL_PATH,null);
        if (path!=null){
            Bitmap bitmap= decodeFile(path);

            userIcon = getCircularBitmap(bitmap);
            userIcon = addBorderToCircularBitmap(userIcon,15,Color.WHITE);
            userIcon = addShadowToCircularBitmap(userIcon,4,Color.LTGRAY);
            userIcon = Bitmap.createScaledBitmap(userIcon,72,72,false);
        } else {
            Bitmap icon = BitmapFactory.decodeResource(MapsActivity.this.getResources(),R.drawable.ic_person );
            userIcon = getCircularBitmap(icon);
            userIcon = addBorderToCircularBitmap(userIcon,15,Color.WHITE);
            userIcon = addShadowToCircularBitmap(userIcon,4,Color.LTGRAY);
            userIcon = Bitmap.createScaledBitmap(userIcon,90,90,false);
        }

        setMyLocation = (ImageButton) findViewById(R.id.setMyLocation);
        LatLng p = new LatLng(0.0,0.0);
        myLocation = mMap.addMarker(new MarkerOptions()
                .position(p)
                .title("Moi")
                .icon(BitmapDescriptorFactory.fromBitmap(userIcon))
                .anchor(0.5f,1));

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            setMyLocation.setVisibility(View.VISIBLE);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);

        } else {
            checkLocationPermission();
        }
       }

        setMyDestination = (ImageButton) findViewById(R.id.setMyDestination);
        setMyDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(destinationOK && latLngDestination!=null){
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLngDestination)
                            .tilt(45)
                            .zoom(15)
                            .bearing(0)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });

        setMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    buildAlertMessageNoGps();
                }else{
                    getMyLocation();
                }
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(destination!=null && destinationOK){ destination.remove();}
                latLngDestination = latLng;
                destinationLatLng = latLng;
                destination = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Destination")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                destinationOK = true;
                setMyDestination.setVisibility(View.VISIBLE);
                commander.setVisibility(View.VISIBLE);

            }
        });
    }

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                    myLocation.setPosition(latLng);

                    if(firstLocation) {
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(latLng)
                                .tilt(45)
                                .zoom(15)
                                .bearing(0)
                                .build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        firstLocation = false;
                    }

                }
            }
        }
    };

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Activez la localisation pour continuer")
                .setCancelable(false)
                .setPositiveButton("Oui", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Non", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void buildAlertMessageEndRide(final Boolean s) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Annuler la commande")
                .setCancelable(false)
                .setPositiveButton("Oui", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!s){
                            cancelSearchRide();
                            requestBol = false;
                        }else{
                            cancelRide();
                            requestBol = false;
                        }
                    }
                })
                .setNegativeButton("Non", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void getMyLocation() {
        if(ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }else{
            LatLng latlngPosition = null;
            latlngPosition = myLocation.getPosition();
                if(latlngPosition != null){
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latlngPosition)
                            .tilt(45)
                            .zoom(15)
                            .bearing(0)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    public static Bitmap decodeFile(String pathName){
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        for (options.inSampleSize = 1; options.inSampleSize <= 32; options.inSampleSize++){
            try {
                bitmap = BitmapFactory.decodeFile(pathName, options);
                Log.d(TAG_LOG, "Decoded successfully for sampleSize "+options.inSampleSize);
                break;
            }catch (OutOfMemoryError outOfMemoryError){
                Log.e(TAG_LOG, "outOfMemoryError while reading file for sampleSize "+options.inSampleSize+" retrying with higher value");
            }
        }
        return bitmap;
    }

    public void abonnementGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public void desabonnementGPS() {
        locationManager.removeUpdates(this);
    }

    public void sendLocalNotif(){

        Intent intent = new Intent(this, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        //Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("Course acceptée.")
                .setContentText("Votre taxi sera bientot là.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(new long[] {1000, 1000})
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        int notificationId = 1;
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId, mBuilder.build());

        driverButton.setVisibility(View.VISIBLE);
    }
    public void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,name,importance);
            channel.setDescription(description);
            NotificationManager notificationManager =getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        //Obtention de la référence du service
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        //Si le GPS est disponible, on s'y abonne
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            abonnementGPS();
        }
        setupProfilUser();


    }


    @Override
    public void onPause() {
        super.onPause();
        //On appelle la méthode pour se désabonner
        desabonnementGPS();
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(MapsActivity.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(MapsActivity.this)
                .addOnConnectionFailedListener(MapsActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        //Si le GPS est désactivé on se désabonne
        if("gps".equals(provider)) {
            desabonnementGPS();
        }
    }

    @Override
    public void onProviderEnabled(final String provider) {
        //Si le GPS est activé on s'abonne
        if("gps".equals(provider)) {
            abonnementGPS();
        }
    }

    private void checkTempFile(String sPath){
        boolean fileExist = new File(sPath).isFile();
        if(!fileExist){
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String iduser = auth.getCurrentUser().getUid();
            StorageReference mClientStorage = FirebaseStorage.getInstance().getReference().child("profile_images").child(iduser);

            try {
                final File localFile = File.createTempFile("images", "jpg");

                mClientStorage.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        sharedPreferences.edit().putString(USER_PROFIL_PATH,localFile.getAbsolutePath()).apply();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MapsActivity.this, "Problème lors de la récupération de la photo de profil", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) { }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

}
