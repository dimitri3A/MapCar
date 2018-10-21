package com.community.hanrynzale.mapcar;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;
import static com.community.hanrynzale.mapcar.LoginActivity.URL_PROFIL_PATH;

public class EditActivity extends AppCompatActivity {

    public static final String USER_PROFIL_PATH = "user_profil_path";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final String TAG_LOG = "log" ;
    private CircleImageView profilUserImg;
    private ProgressBar progressBar;
    private SharedPreferences preferences;
    private static final String PREFS = "PREFS";
    private static final String PREFS_NOM_USER = "NOM_USER";
    private static final String PREFS_PRENOM_USER = "PRENOM_USER";
    private static final String PREFS_CONTACT = "CONTACT_USER";
    String path = null;
    Uri uri = null;
    EditText input_nom,input_prenom,input_contact;
    AppCompatButton maj;
    FirebaseAuth auth;
    String userID;
    DatabaseReference mClientDatabase;
    StorageReference storageReference;
    Uri resultUri;
    Bitmap bitmap_new_photo = null;
    String newPath = null;
    boolean uploadFinish;
    String nom,prenom,contact;
    StorageReference mClientStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Modifier le profile");

        preferences= getBaseContext().getSharedPreferences(PREFS, MODE_PRIVATE);
        path = preferences.getString(USER_PROFIL_PATH, null);
        checkTempFile(path);

        profilUserImg = findViewById(R.id.img_profil_edit);
        progressBar = findViewById(R.id.progressBar);
        uploadFinish = false;
        input_nom = findViewById(R.id.input_name);
        input_prenom = findViewById(R.id.input_prenom);
        input_contact = findViewById(R.id.input_contact);

        auth = FirebaseAuth.getInstance();
        userID = auth.getCurrentUser().getUid();
        mClientDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Clients").child(userID);
        storageReference = FirebaseStorage.getInstance().getReference();
        mClientStorage = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);


        setupProfilUser();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_check);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });
        profilUserImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(EditActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(Intent.ACTION_PICK);
                    i.setType("image/*");
                    startActivityForResult(i,0);
                }else{
                    ActivityCompat.requestPermissions(EditActivity.this,
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            }
        });

        if(preferences.contains(PREFS_NOM_USER) && preferences.contains(PREFS_PRENOM_USER) ) {
            String nom = preferences.getString(PREFS_NOM_USER, "");
            String prenom = preferences.getString(PREFS_PRENOM_USER, "");
            String contact = preferences.getString(PREFS_CONTACT, "");

            input_nom.setText(nom);
            input_prenom.setText(prenom);
            input_contact.setText(contact);

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        Intent i = new Intent(Intent.ACTION_PICK);
                        i.setType("image/*");
                        startActivityForResult(i,0);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void setupProfilUser() {
        if(newPath!=null){
            Bitmap bitmap = decodeFile(newPath);
            profilUserImg.setImageBitmap(bitmap);
        }else{
            path = preferences.getString(USER_PROFIL_PATH, null);
            if (path!=null){
                Glide.with(getApplication()).load(path).into(profilUserImg);
            }
        }
    }

    private void saveUserInformation() {

        progressBar.setVisibility(View.VISIBLE);
        nom = input_nom.getText().toString().trim();
        prenom = input_prenom.getText().toString().trim();
        contact = input_contact.getText().toString().trim();

        HashMap<String,Object> userInfo = new HashMap<String,Object>();
        userInfo.put("nom", nom);
        userInfo.put("prenom", prenom);
        userInfo.put("contact", contact);
        mClientDatabase.updateChildren(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()){
                    Toast.makeText(getBaseContext(), "Echec de l'enregistrement", Toast.LENGTH_LONG).show();
                }else{
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(nom+prenom)
                            .build();
                    if(user!=null){
                        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()){
                                    Toast.makeText(getBaseContext(), "Une erreur s'est produite", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                    preferences.edit()
                            .putString(PREFS_NOM_USER, nom)
                            .putString(PREFS_PRENOM_USER, prenom)
                            .putString(PREFS_CONTACT, contact)
                            .apply();

                    if(resultUri != null) {

                        StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);

                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(bitmap!=null){
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                            byte[] data = baos.toByteArray();
                            UploadTask uploadTask = filePath.putBytes(data);

                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getBaseContext(), "Echec de sauvegarde de la photo", Toast.LENGTH_LONG).show();
                                    onSaveFailed();
                                }
                            });
                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    HashMap<String,Object> newImage = new HashMap<String, Object>();
                                    if(downloadUrl!=null){
                                        newImage.put("profileImageUrl", downloadUrl.toString());
                                        mClientDatabase.updateChildren(newImage);
                                        Toast.makeText(getBaseContext(), "Photo saved", Toast.LENGTH_LONG).show();
                                        uploadFinish = true;
                                    }
                                    onSaveSuccess();
                                }
                            });
                        }else{

                        filePath.putFile(resultUri)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getBaseContext(), "Echec de sauvegarde de la photo", Toast.LENGTH_LONG).show();
                                        onSaveFailed();
                                    }
                                })
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                        HashMap<String,Object> newImage = new HashMap<String, Object>();
                                        if (downloadUrl!=null){
                                            newImage.put("profileImageUrl", downloadUrl.toString());
                                            mClientDatabase.updateChildren(newImage);
                                            Toast.makeText(getBaseContext(), "Photo saved", Toast.LENGTH_LONG).show();
                                            uploadFinish = true;
                                        }
                                        onSaveSuccess();
                                    }
                                });
                        }
                    }else{
                        Toast.makeText(getBaseContext(), "Aucune photo sélectionnée", Toast.LENGTH_LONG).show();
                        uploadFinish = true;
                    }
                }
            }
        });


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
                        Toast.makeText(EditActivity.this, "Problème lors de la récupération de la photo de profil", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode== Activity.RESULT_OK){
            uri = data.getData();
            resultUri = uri;
            Cursor cursor= getContentResolver().query(uri,new String[]{MediaStore.Images.ImageColumns.DATA},null,null,null);
            if (cursor!= null && cursor.getCount()>0){
                cursor.moveToFirst();
                newPath=cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                if(newPath!=null){
                    bitmap_new_photo= decodeFile(newPath);
                    if (bitmap_new_photo!=null)profilUserImg.setImageBitmap(bitmap_new_photo);
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edittoolbar, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.edittoolbar) {
            onEditAction();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void onEditAction(){
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    @Override
    protected void onResume() {
        super.onResume();
        setupProfilUser();
    }
    public void onSaveSuccess() {
        getUserInfos();
    }
    public void onSaveFailed() {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(getBaseContext(), "Echec de connexion", Toast.LENGTH_LONG).show();
    }

    private void getUserInfos() {
        mClientDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> hashMap = (Map<String, Object>) dataSnapshot.getValue();
                    if (hashMap!=null){
                        if(hashMap.get("profileImageUrl")!=null){
                            preferences.edit().putString(URL_PROFIL_PATH, hashMap.get("profileImageUrl").toString()).apply();
                            if(hashMap.get("nom")!=null){
                                preferences.edit().putString(PREFS_NOM_USER, hashMap.get("nom").toString()).apply();
                            }
                            if(hashMap.get("prenom")!=null){
                                preferences.edit().putString(PREFS_PRENOM_USER, hashMap.get("prenom").toString()).apply();
                            }
                            if(hashMap.get("contact")!=null){
                                preferences.edit().putString(PREFS_CONTACT, hashMap.get("contact").toString()).apply();
                            }

                            try {
                                final File localFile = File.createTempFile("images", "jpg");

                                mClientStorage.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        preferences.edit().putString(USER_PROFIL_PATH,localFile.getAbsolutePath()).apply();
                                        progressBar.setVisibility(View.GONE);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

