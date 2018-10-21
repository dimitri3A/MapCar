package com.community.hanrynzale.mapcar;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    protected static final String PREFS_EMAIL_USER = "EMAIL_USER";
    private EditText emailText = null;
    private EditText passwordText = null;
    private Button loginButton = null;
    private Button facebook_Button = null;
    private Button google_Button = null;
    //private ProgressBar progressBar;
    private ProgressDialog progressDialog = null;
    SharedPreferences sharedPreferences;
    private static final String PREFS = "PREFS";
    protected static final String PREFS_USER_STAT = "PREFS_USER_STAT";
    private static final String USER_STAT = "CONNECTED";
    public static final String USER_PROFIL_PATH = "user_profil_path";
    public static final String URL_PROFIL_PATH = "url_profil";
    private static final String PREFS_NOM_USER = "NOM_USER";
    private static final String PREFS_PRENOM_USER = "PRENOM_USER";
    private static final String PREFS_CONTACT_USER = "CONTACT_USER";
    private FirebaseAuth auth;
    DatabaseReference mClientDatabase;
    private String userID;
    StorageReference mClientStorage;
    private String email;
    private String password;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login2);
        emailText = findViewById(R.id.input_email);
        passwordText = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.btn_login);
        facebook_Button = findViewById(R.id.facebook_btn);
        google_Button = findViewById(R.id.google_btn);
        TextView signupLink = findViewById(R.id.link_signup);
        TextView forgotPassWord = findViewById(R.id.link_forgotpassword);
        //progressBar = findViewById(R.id.progressBar);

        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppCompatAlertDialogStyle);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Connexion");


        auth = FirebaseAuth.getInstance();

        sharedPreferences = getBaseContext().getSharedPreferences(PREFS, MODE_PRIVATE);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        forgotPassWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void resetPassword() {
        progressDialog.show();
        final String email = emailText.getText().toString();
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            emailText.setError("Entrez une adresse Email valide");
                        }
                    }, 2000);
        } else {
            emailText.setError(null);
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "Un Email de récupération vous a été envoyé.", Toast.LENGTH_LONG).show();
                                    }
                                }, 2000);
                    }else{
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "Echec d'envoi à l'adresse "+email, Toast.LENGTH_LONG).show();
                                    }
                                }, 2000);
                    }
                }
            });
        }
    }

    public void login() {
        Log.d(TAG, "Login");
        if (!validate()) {
            onLoginFailed();
            return;
        }
        loginButton.setEnabled(false);

        email = emailText.getText().toString();
        password = passwordText.getText().toString();

        progressDialog.show();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            new android.os.Handler().postDelayed(
                                    new Runnable() {
                                        public void run() {
                                            progressDialog.dismiss();
                                            loginButton.setEnabled(true);
                                            Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                        }
                                    }, 2000);
                        } else {
                            userID = auth.getCurrentUser().getUid();
                            mClientDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Clients").child(userID);
                            mClientStorage = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
                            sharedPreferences.edit().putString(PREFS_USER_STAT,USER_STAT).apply();
                            getUserInfos();
                        }
                    }
                });

    }

    private void getUserInfos() {
        mClientDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                            Map<String, Object> hashMap = (Map<String, Object>) dataSnapshot.getValue();
                            if (hashMap!=null){

                                if(hashMap.get("nom")!=null){
                                    sharedPreferences.edit().putString(PREFS_NOM_USER, hashMap.get("nom").toString()).apply();
                                }
                                if(hashMap.get("prenom")!=null){
                                    sharedPreferences.edit().putString(PREFS_PRENOM_USER, hashMap.get("prenom").toString()).apply();
                                }
                                if(hashMap.get("contact")!=null){
                                    sharedPreferences.edit().putString(PREFS_CONTACT_USER, hashMap.get("contact").toString()).apply();
                                    sharedPreferences.edit().putString(PREFS_EMAIL_USER, email).apply();
                                }
                                if(hashMap.get("profileImageUrl")!=null){
                                    sharedPreferences.edit().putString(URL_PROFIL_PATH, hashMap.get("profileImageUrl").toString()).apply();

                                    try {
                                        final File localFile = File.createTempFile("images", "jpg");

                                        mClientStorage.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                sharedPreferences.edit().putString(USER_PROFIL_PATH,localFile.getAbsolutePath()).apply();
                                                onLoginSuccess();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(LoginActivity.this, "Problème lors de la récupération de la photo de profil", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                }else{
                                    onLoginSuccess();
                                }

                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mClientStorage!=null){
            outState.putString("reference",mClientStorage.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        final String stringRef = savedInstanceState.getString("reference");
        if(stringRef==null){
            return;
        }
        mClientStorage = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef);
        List<FileDownloadTask> tasks = mClientStorage.getActiveDownloadTasks();
        if(tasks.size()>0){
            FileDownloadTask  task = tasks.get(0);
            task.addOnSuccessListener(this, new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                        }
                    }, 2000);
    }

    public void onLoginSuccess() {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(getBaseContext(), "Connexion réussite", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, 2000);
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Echec de connexion", Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Entrez une adresse mail valide");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 ) {
            passwordText.setError("Entrez plus de 3 caractères");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }

}