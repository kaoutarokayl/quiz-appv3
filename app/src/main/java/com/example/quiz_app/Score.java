package com.example.quiz_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Score extends AppCompatActivity {
    Button bLogout, bTry;
    ProgressBar progressBar;
    TextView tvScore;
    int score;
    final int TOTAL_QUESTIONS = 5; // Définir comme constante

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        tvScore = findViewById(R.id.tvScore);
        progressBar = findViewById(R.id.progressBar);
        bLogout = findViewById(R.id.bLogout);
        bTry = findViewById(R.id.bTry);

        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0);

        int percentage = 0;
        if (TOTAL_QUESTIONS > 0) { // Éviter la division par zéro
            percentage = (100 * score) / TOTAL_QUESTIONS;
        }

        progressBar.setProgress(percentage);
        tvScore.setText(percentage + " %");

        bLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Merci de votre Participation !", Toast.LENGTH_SHORT).show();
                // L'utilisateur sera déconnecté via UserProfileActivity s'il le souhaite.
                // Ici, on retourne à l'écran de login principal.
                Intent intent = new Intent(Score.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Termine l'activité Score
            }
        });

        bTry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // NOUVEAU: Rediriger vers UserProfileActivity pour recommencer le flux
                Intent intent = new Intent(Score.this, UserProfileActivity.class);
                // Optionnel: Effacer la pile jusqu'à UserProfileActivity s'il existe déjà.
                // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish(); // Termine l'activité Score
            }
        });
    }
}