package com.example.foroandroid.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foroandroid.MainActivity;
import com.example.foroandroid.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView mStatusTextView;
    private TextInputEditText mEmailField;
    private TextInputEditText mPasswordField;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);


        mAuth = FirebaseAuth.getInstance();

        mStatusTextView = findViewById(R.id.status_text);
        mEmailField = findViewById(R.id.input_email);
        mPasswordField = findViewById(R.id.input_password);


        Button mLoginButton = findViewById(R.id.btn_login);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin(mEmailField.getText().toString(), mPasswordField.getText().toString());
            }
        });


        Button mRegisterButton = findViewById(R.id.btn_register);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
                Intent i = new Intent(Login.this, Register.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onStart() {

        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startMain();
        }
    }

    private boolean validateForm(String email, String password) {
        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("No puede estar vacio");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("No puede estar vacio");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }


    private void doLogin(String email, String password) {
        if (!validateForm(email, password)) {
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                updateTextView();
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        if (user.isEmailVerified()){
                            startMain();
                        } else {
                            user.sendEmailVerification();
                            Toast toast = Toast.makeText(Login.this, "verifica email", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }

                } else {
                    Toast.makeText(Login.this, "Debes de registrarte y confirmar la direccion del correo", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void createAccount(String email, String password) {
        if (!validateForm(email, password)) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                updateTextView();

                if (task.isSuccessful()) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName("GrupoDAP").build();

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null)
                        user.updateProfile(profileUpdates);

                    sendEmail();
                } else {
                    Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    Toast.makeText(Login.this, "Fallo al registrar al usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(Login.this,
                                "Email de verificación enviado a " + user.getEmail(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Login.this,
                                "Fallo al enviar el email de verificación", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void startMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateTextView() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mStatusTextView.setText("Has iniciado sesión");
        } else {
            mStatusTextView.setText("User logged out");
        }
    }

}

