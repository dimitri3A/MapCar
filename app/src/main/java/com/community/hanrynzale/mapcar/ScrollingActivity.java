package com.community.hanrynzale.mapcar;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.community.hanrynzale.mapcar.LoginActivity.PREFS_EMAIL_USER;
import static com.community.hanrynzale.mapcar.LoginActivity.PREFS_USER_STAT;
import static com.community.hanrynzale.mapcar.MapsActivity.PREFS_CONTACT;
import static com.community.hanrynzale.mapcar.MapsActivity.USER_DISCONNECT;


public class ScrollingActivity extends AppCompatActivity {
    public static final String USER_PROFIL_PATH = "user_profil_path";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final String TAG_LOG = "log" ;
    private static final String TAG = "log";
    private static final String PREFS = "PREFS";
    private static final String PREFS_NOM_USER = "NOM_USER";
    private static final String PREFS_PRENOM_USER = "PRENOM_USER";
    private static final String PREFS_CONTACT_USER = "CONTACT_USER";
    GoogleMap mMap;
    SharedPreferences preferences;
    String path;
    CircleImageView profilUserImg = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
        profilUserImg = findViewById(R.id.img_profil);

        preferences = getBaseContext().getSharedPreferences(PREFS, MODE_PRIVATE);
        path = preferences.getString(USER_PROFIL_PATH, null);
        checkTempFile(path);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView username;
        username = findViewById(R.id.username);
        TextView usercontact = findViewById(R.id.contactUser);
        TextView useremail;
        useremail = findViewById(R.id.email);

        TextView edit;
        edit = findViewById(R.id.link_edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                startActivity(intent);
            }
        });

        String prenom = preferences.getString(PREFS_NOM_USER,"");
        String nom = preferences.getString(PREFS_PRENOM_USER,"");
        String contact = preferences.getString(PREFS_CONTACT_USER,"");
        String email = preferences.getString(PREFS_EMAIL_USER,"");
        String name =  prenom+" "+nom;
        username.setText(name);
        usercontact.setText(contact);
        useremail.setText(email);

        if(preferences.contains(USER_PROFIL_PATH)) {
            String path= preferences.getString(USER_PROFIL_PATH,null);
            if (path!=null){
                Bitmap bitmap= decodeFile(path);
                profilUserImg.setImageBitmap(bitmap);
            }
        }

        Button btn_logout = findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogout();
            }
        });

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:{
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Intent i = new Intent(Intent.ACTION_PICK);
                    i.setType("image/*");
                    startActivityForResult(i,0);
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(preferences.contains(USER_PROFIL_PATH)) {
            String path= preferences.getString(USER_PROFIL_PATH,null);
            if (path!=null){
                Bitmap bitmap= decodeFile(path);
                profilUserImg.setImageBitmap(bitmap);
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
                        preferences.edit().putString(USER_PROFIL_PATH,localFile.getAbsolutePath()).apply();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ScrollingActivity.this, "Problème lors de la récupération de la photo de profil", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
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
                        preferences.edit()
                                .putString(PREFS_USER_STAT, USER_DISCONNECT)
                                .putString(PREFS_NOM_USER,null)
                                .putString(PREFS_PRENOM_USER,null)
                                .putString(PREFS_CONTACT,null)
                                .putString(PREFS_EMAIL_USER,null)
                                .putString(USER_PROFIL_PATH,null)
                                .apply();
                        Toast.makeText(getBaseContext(), "Déconnexion", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ScrollingActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }).create().show();
    }

}

