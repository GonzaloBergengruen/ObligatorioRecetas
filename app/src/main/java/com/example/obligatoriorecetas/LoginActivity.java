package com.example.obligatoriorecetas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    // Referencias a los campos del formulario y al servicio de autenticación Firebase.
    private EditText emailInput;
    private EditText passwordInput;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //interfaz se dibuja hasta los bordes de la pantalla.
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Ajusta el contenido para que no quede debajo de las barras del sistema.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // Inicializa Firebase Authentication y conecta las vistas del XML.
        firebaseAuth = FirebaseAuth.getInstance();
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        // Firebase conserva la sesion. si ya hay un usuario, no hace falta mostrar el login.
        if (firebaseAuth.getCurrentUser() != null) {
            openMainActivity();
        }
    }

    //login se ejecuta al tocar el boton con android:onClick="login" en el XML.
    public void login(View view) {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        // comprueba si falta completar algun campo.
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa el correo y la contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        // si se ingresa exitosamente se abre mainactivity, sino se muestra un mensaje de error.
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show();
                        openMainActivity();
                    } else {
                        Toast.makeText(this, "No se pudo iniciar sesion. Revisa tus datos.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // registrar se ejecuta al tocar el boton con android:onClick="registrar" en el XML.
    public void registrar(View view) {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        //comprueba si falta completar algun campo.
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa el correo y la contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        // si la cuenta se crea correctamente se abre mainactivity, sino se muestra un mensaje de error.
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Cuenta creada. Bienvenido!", Toast.LENGTH_SHORT).show();
                        openMainActivity();
                    } else {
                        Toast.makeText(this, "No se pudo crear la cuenta. Revisa tus datos.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);

        // Limpia la pila de pantallas para que Atras no vuelva al login.
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent); // Abre MainActivity.
        finish(); // Cierra el login.
    }
}
