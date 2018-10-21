package com.community.hanrynzale.mapcar;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ConfirmePhoneActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences = null;
    AppCompatButton continuerPhoneButton = null;
    private static final String PREFS = "PREFS";
    private EditText codeText = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_confirme_phone);

        sharedPreferences = getBaseContext().getSharedPreferences(PREFS, MODE_PRIVATE);

        continuerPhoneButton = (AppCompatButton) findViewById(R.id.phone_button_continuer);
        continuerPhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirme();
            }
        });
   }

    public void onConfirmeSuccess() {
        continuerPhoneButton.setEnabled(true);
        setResult(RESULT_OK);
    }

    public void onConfirmeFailed() {
        Toast.makeText(getBaseContext(), "Echec de connexion", Toast.LENGTH_LONG).show();
        continuerPhoneButton.setEnabled(true);
    }

    public void confirme(){
        continuerPhoneButton.setEnabled(false);

        codeText = (EditText) findViewById(R.id.input_codeConfirmEmail);
        String code = codeText.getText().toString();

        final ProgressDialog progressDialog = new ProgressDialog(ConfirmePhoneActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Vérification...");
        progressDialog.show();

        // TODO: Implement your own confirm logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        onConfirmeSuccess();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }

}
