package com.example.obligatoriorecetas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class LoginActivity extends AppCompatActivity {
    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button registerButton;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.login),
                (view, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );
                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );
                    return insets;
                }
        );

        firebaseAuth = FirebaseAuth.getInstance();
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(view -> signIn());
        registerButton.setOnClickListener(view -> register());

        if (firebaseAuth.getCurrentUser() != null) {
            openMainActivity();
        }
    }

    private void signIn() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (!validateInput(email, password)) {
            return;
        }

        setLoading(true);
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);

                    if (task.isSuccessful()) {
                        openMainActivity();
                    } else {
                        showAuthError(task.getException(), false);
                    }
                });
    }

    private void register() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (!validateInput(email, password)) {
            return;
        }

        setLoading(true);
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);

                    if (task.isSuccessful()) {
                        openMainActivity();
                    } else {
                        showAuthError(task.getException(), true);
                    }
                });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            emailInput.setError("Ingresá tu correo electrónico");
            emailInput.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Ingresá un correo válido");
            emailInput.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Ingresá tu contraseña");
            passwordInput.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordInput.setError(
                    "La contraseña debe tener al menos 6 caracteres"
            );
            passwordInput.requestFocus();
            return false;
        }

        return true;
    }

    private void setLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        registerButton.setEnabled(!loading);
        loginButton.setText(loading ? "Conectando…" : "Ingresar");
        registerButton.setText(loading ? "Conectando…" : "Crear una cuenta");
    }

    private void showAuthError(Exception exception, boolean registering) {
        String message;

        if (exception instanceof FirebaseAuthUserCollisionException) {
            message = "Ya existe una cuenta con ese correo. Ingresá a tu cuenta.";
        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            message = "La contraseña es demasiado débil. Usá al menos 6 caracteres.";
        } else if (exception instanceof FirebaseAuthInvalidUserException) {
            message = "No encontramos una cuenta con ese correo. Podés registrarte.";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            message = registering
                    ? "No se pudo crear la cuenta. Revisá el correo y la contraseña."
                    : "El correo o la contraseña no son correctos.";
        } else {
            message = registering
                    ? "No se pudo crear la cuenta. Revisá tu conexión e intentá de nuevo."
                    : "No se pudo iniciar sesión. Revisá tu conexión e intentá de nuevo.";
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
