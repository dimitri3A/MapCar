package com.community.hanrynzale.mapcar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.community.hanrynzale.mapcar.LoginActivity.PREFS_EMAIL_USER;

public class UpdateEmailPassword extends AppCompatActivity {
    TextView oldEmail,oldPassword,newPassword;
    String email;
    FirebaseUser user = null;
    ProgressBar progressBar = null;
    private SharedPreferences preferences;
    private static final String PREFS = "PREFS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email_password);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Modifier le profile");
        setSupportActionBar(toolbar);

        preferences= getBaseContext().getSharedPreferences(PREFS, MODE_PRIVATE);

        oldEmail = findViewById(R.id.input_email);

        oldPassword = findViewById(R.id.input_password);
        newPassword = findViewById(R.id.input_passwordNew);

        progressBar = findViewById(R.id.progressBar);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            email = user.getEmail();
            oldEmail.setText(email);
        }

        toolbar.setNavigationIcon(R.drawable.ic_check);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                updateUserInformation();
            }
        });
    }

    private void updateUserInformation() {

        if (!validate()) {
            return;
        }
        int updateType;
        if(email.equals(oldEmail.getText().toString())&&!newPassword.getText().toString().isEmpty()) {
            updateType = 1;
            reAuthenticateUser(updateType);
        }
        if(newPassword.getText().toString().isEmpty()&& !email.equals(oldEmail.getText().toString())){
            updateType = 2;
            reAuthenticateUser(updateType);
        }
        if(!newPassword.getText().toString().isEmpty()&& !email.equals(oldEmail.getText().toString())){
            updateType = 3;
            reAuthenticateUser(updateType);
        }

    }

    private void reAuthenticateUser(final int t){
        AuthCredential credential = EmailAuthProvider.getCredential(oldEmail.getText().toString(),oldPassword.getText().toString());
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                switch (t){
                    case 1:
                        updatePasswordOnly(newPassword.getText().toString());
                        break;
                    case 2:
                        updateEmailOnly(newPassword.getText().toString());
                        break;
                    case 3:
                        updateEmailAndPassword(oldEmail.getText().toString(),newPassword.getText().toString());
                        break;
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UpdateEmailPassword.this, "Une erreur s'est produite", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void onSuccessUpdate(){
        progressBar.setVisibility(View.GONE);
        Intent intent = new Intent(UpdateEmailPassword.this, MapsActivity.class);
        startActivity(intent);
    }

    private void updateEmailOnly(final String e){
        user.updateEmail(e).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(UpdateEmailPassword.this, "Email mis à jour", Toast.LENGTH_LONG).show();
                preferences.edit()
                        .putString(PREFS_EMAIL_USER, e).apply();
                onSuccessUpdate();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UpdateEmailPassword.this, "Une erreur s'est produite", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    private void updatePasswordOnly(String p){
        user.updatePassword(p).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(UpdateEmailPassword.this, "Mot de passe mis à jour", Toast.LENGTH_LONG).show();
                onSuccessUpdate();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UpdateEmailPassword.this, "Une erreur s'est produite", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    private void updateEmailAndPassword(final String e, final String p){

        user.updateEmail(e).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                preferences.edit().putString(PREFS_EMAIL_USER, e).apply();
                Toast.makeText(UpdateEmailPassword.this, "Email mis à jour", Toast.LENGTH_LONG).show();
                updatePasswordOnly(p);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UpdateEmailPassword.this, "Une erreur s'est produite", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    public boolean validate() {
        boolean valid = true;

        String emailOld = oldEmail.getText().toString();
        String passwordOld = oldPassword.getText().toString();
        String passwordNew = newPassword.getText().toString();

        if (emailOld.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailOld).matches()) {
            oldEmail.setError("Entrez une adresse email valide");
            valid = false;
        } else {
            oldEmail.setError(null);
        }

        if (passwordNew.isEmpty() ) {
            newPassword.setError(null);
        } else if(passwordNew.length() < 4) {
            newPassword.setError("Entrez plus de 3 caractères");
            valid = false;
        }

        if(passwordOld.isEmpty()){
            oldPassword.setError("Entrez le mot de passe actuel pour confirmer la modification");
            valid = false;
        }else{
            oldPassword.setError(null);
        }

        return valid;
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
    }

}
