package com.example.quiz_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserProfileActivity extends AppCompatActivity {

    private TextView tvWelcomeUser, tvUserEmail;
    private Button btnStartQuiz, btnLogoutUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();

        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnStartQuiz = findViewById(R.id.btnStartQuiz);
        btnLogoutUser = findViewById(R.id.btnLogoutUser);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userName = currentUser.getDisplayName(); // Peut être null si non défini
            String userEmail = currentUser.getEmail();

            if (userName != null && !userName.isEmpty()) {
                tvWelcomeUser.setText("Bienvenue, " + userName + "!");
            } else if (userEmail != null && !userEmail.isEmpty()) {
                // Si le nom n'est pas dispo, on peut utiliser la partie locale de l'email
                String localPart = userEmail.split("@")[0];
                tvWelcomeUser.setText("Bienvenue, " + localPart + "!");
            } else {
                tvWelcomeUser.setText("Bienvenue !");
            }
            tvUserEmail.setText(userEmail != null ? userEmail : "Email non disponible");

        } else {
            // Normalement, on ne devrait pas arriver ici si la logique de redirection est correcte.
            // Si c'est le cas, rediriger vers l'écran de connexion.
            Toast.makeText(this, "Utilisateur non connecté. Redirection...", Toast.LENGTH_SHORT).show();
            navigateToLoginScreen();
            return; // Stopper l'exécution de onCreate
        }

        btnStartQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, DetectionActivity.class);
                startActivity(intent);
                // Optionnel: finish(); si vous ne voulez pas que l'utilisateur puisse revenir ici
                // avec le bouton "back" une fois dans le flux du quiz.
            }
        });

        btnLogoutUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Toast.makeText(UserProfileActivity.this, "Déconnexion réussie.", Toast.LENGTH_SHORT).show();
                navigateToLoginScreen();
            }
        });
    }

    private void navigateToLoginScreen() {
        Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Comportement par défaut du bouton retour.
        // Si l'utilisateur arrive ici depuis l'écran de login, et que l'écran de login a été fini,
        // appuyer sur retour quittera l'application.
        // Si vous voulez un dialogue de confirmation "Voulez-vous quitter ?", vous pouvez l'implémenter ici.
        super.onBackPressed();
    }
}