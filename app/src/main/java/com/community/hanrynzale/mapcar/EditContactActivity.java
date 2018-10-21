package com.community.hanrynzale.mapcar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class EditContactActivity extends AppCompatActivity {
    private AppCompatButton confirmePhoneButton = null;
    SharedPreferences sharedPreferences = null;
    DatabaseReference mClientDatabase;
    FirebaseAuth auth;
    EditText numeroText = null;
    private static final String PREFS_CONTACT_USER = "CONTACT_USER";
    private static final String PREFS = "PREFS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_contact);

        auth = FirebaseAuth.getInstance();
        String userID = auth.getCurrentUser().getUid();
        mClientDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Clients").child(userID);

        sharedPreferences = getBaseContext().getSharedPreferences(PREFS, MODE_PRIVATE);

        numeroText= (EditText) findViewById(R.id.input_phoneNumber);
        confirmePhoneButton = (AppCompatButton) findViewById(R.id.phone_continuer);
        confirmePhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPhone();
            }
        });
    }


    public void onPhoneSuccess() {
        confirmePhoneButton.setEnabled(true);
        sharedPreferences.edit().putString(PREFS_CONTACT_USER, numeroText.getText().toString()).apply();
        setResult(RESULT_OK);
        Intent intent = new Intent(EditContactActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    public void onPhoneFailed() {
        Toast.makeText(getBaseContext(), "Echec de connexion", Toast.LENGTH_LONG).show();
        confirmePhoneButton.setEnabled(true);
        numeroText.setEnabled(true);
    }

    public void setPhone(){
        confirmePhoneButton.setEnabled(false);
        numeroText.setEnabled(false);
        String numero = numeroText.getText().toString();

        final ProgressDialog progressDialog = new ProgressDialog(EditContactActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Enregistrement...");
        progressDialog.show();

        // TODO: Enregistrer le contact sur Firebase
        HashMap<String,Object> userContact = new HashMap<String, Object>();
        userContact.put("contact", numero);
        mClientDatabase.updateChildren(userContact).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                onPhoneSuccess();
                            }
                        }, 3000);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                onPhoneFailed();
                            }
                        }, 3000);
            }
        });


    }

}
