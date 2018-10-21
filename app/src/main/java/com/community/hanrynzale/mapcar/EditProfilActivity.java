package com.community.hanrynzale.mapcar;

import android.*;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.graphics.BitmapFactory.decodeFile;

public class EditProfilActivity extends AppCompatActivity {

    public static final String USER_PROFIL_PATH = "user_profil_path";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final String TAG_LOG = "log" ;
    private Toolbar toolbar = null;
    private CircleImageView profilUserImg;
    private AppCompatButton buttonEditContact1 = null;
    private SharedPreferences preferences;
    private static final String PREFS = "PREFS";
    private static final String PREFS_NOM_USER = "NOM_USER";
    private static final String PREFS_PRENOM_USER = "PRENOM_USER";
    private static final String PREFS_EMAIL_USER = "EMAIL_USER";
    private static final String PREFS_PASSWORD_USER = "PASSWORD_USER";
    private static final String PREFS_CONTACT = "CONTACT_USER";
    String path = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profil);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        profilUserImg = (CircleImageView) findViewById(R.id.img_profil_edit);
        preferences= getBaseContext().getSharedPreferences(PREFS, MODE_PRIVATE);

        setupProfilUser();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //FloatingActionButton change_photo = (FloatingActionButton) findViewById(R.id.fab);
        profilUserImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(EditProfilActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(Intent.ACTION_PICK);
                    i.setType("image/*");
                    startActivityForResult(i,0);
                }else{
                    ActivityCompat.requestPermissions(EditProfilActivity.this,
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                }
            }
        });

        if(preferences.contains(PREFS_NOM_USER) && preferences.contains(PREFS_PRENOM_USER) && preferences.contains(PREFS_EMAIL_USER) ) {
            String nom = preferences.getString(PREFS_NOM_USER, "");
            String prenom = preferences.getString(PREFS_PRENOM_USER, "");
            String contact = preferences.getString(PREFS_CONTACT, "");
            String email = preferences.getString(PREFS_EMAIL_USER, "");


        }
    }

    private void setupProfilUser() {
        path = preferences.getString(USER_PROFIL_PATH, null);
        if (path!=null){
            Bitmap bitmap = decodeFile(path);
            profilUserImg.setImageBitmap(bitmap);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode== Activity.RESULT_OK){
            Uri uri = data.getData();
            Cursor cursor= getContentResolver().query(uri,new String[]{MediaStore.Images.ImageColumns.DATA},null,null,null);
            if (cursor!= null && cursor.getCount()>0){
                cursor.moveToFirst();
                String path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                if(path!=null){
                    Bitmap bitmap= decodeFile(path);
                    if (bitmap!=null)profilUserImg.setImageBitmap(bitmap);
                    preferences.edit().putString(USER_PROFIL_PATH,path).apply();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        setupProfilUser();
    }
}
