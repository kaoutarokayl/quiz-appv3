package com.example.quiz_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// Retire EdgeToEdge si tu ne l'utilises pas ou si ça cause des soucis
// import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // Ajout de l'import

public class MainActivity extends AppCompatActivity {

    EditText etlogin, etPassword;
    Button bLogin;
    TextView tvRegister;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Retire si non nécessaire
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // Vérifier si un utilisateur est déjà connecté
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Si oui, rediriger directement vers UserProfileActivity
            startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
            finish(); // Terminer MainActivity pour empêcher le retour
            return;   // Arrêter l'exécution de onCreate ici
        }

        etlogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        bLogin = findViewById(R.id.blogin);
        tvRegister = findViewById(R.id.tvRegister);

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etlogin.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Connexion réussie!", Toast.LENGTH_SHORT).show();
                                // NOUVEAU: Lancer l'activité de profil utilisateur
                                startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
                                finish();  // Ferme l'activité de connexion
                            } else {
                                Toast.makeText(getApplicationContext(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Register.class));
                // Ne pas 'finish()' ici pour permettre le retour à l'écran de login depuis Register.
            }
        });
    }
}