package com.example.quiz_app;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.media.Image;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Quiz2 extends AppCompatActivity {

    RadioGroup rg;
    RadioButton rb;
    Button bNext;
    TextView timerTextView;
    int score;
    String RepCorrect = "À droite";

    private boolean userLeftApp = false;
    private boolean isActivityFinished = false; // ✅ nouvelle variable
    private CountDownTimer countDownTimer;
    private static final long TOTAL_TIME = 30000; // 30 secondes

    private ExecutorService cameraExecutor;
    private PreviewView previewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz2);

        rg = findViewById(R.id.rg);
        bNext = findViewById(R.id.bNext);
        timerTextView = findViewById(R.id.timerTextView);
        previewView = findViewById(R.id.previewView);

        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0);

        cameraExecutor = Executors.newSingleThreadExecutor();
        startCamera();

        startTimer();

        bNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countDownTimer != null) {
                    countDownTimer.cancel(); // ✅ on arrête le timer
                }

                if (rg.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(getApplicationContext(), "Merci de choisir une réponse S.V.P !", Toast.LENGTH_SHORT).show();
                } else {
                    rb = findViewById(rg.getCheckedRadioButtonId());
                    if (rb.getText().toString().equals(RepCorrect)) {
                        score += 1;
                    }

                    isActivityFinished = true; // ✅ marquer comme terminé

                    Intent intent = new Intent(Quiz2.this, Quiz3.class);
                    intent.putExtra("score", score);
                    startActivity(intent);
                    overridePendingTransition(R.anim.exit, R.anim.entry);
                    finish();
                }
            }
        });
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(TOTAL_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                timerTextView.setText("Temps restant: " + secondsRemaining + "s");
            }

            @Override
            public void onFinish() {
                if (!isActivityFinished) { // ✅ on vérifie avant d’exécuter
                    showAlert("Temps écoulé", "Le temps est terminé, veuillez passer à l'étape suivante.");
                }
            }
        }.start();
    }

    private void showAlert(String title, String message) {
        if (isActivityFinished) return; // ✅ éviter alerte si déjà fini

        new AlertDialog.Builder(Quiz2.this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    isActivityFinished = true; // ✅ marquer comme fini
                    Intent intent = new Intent(Quiz2.this, Quiz3.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("score", score);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel(); // ✅ arrêt timer si on quitte l’activité
        }
        userLeftApp = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        userLeftApp = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (userLeftApp && !isActivityFinished) { //  éviter si déjà fini
            Toast.makeText(this, "Triche détectée : vous avez quitté le quiz.", Toast.LENGTH_LONG).show();
            isActivityFinished = true; // marquer comme terminé
            Intent intent = new Intent(Quiz2.this, DetectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> processImageProxy(imageProxy));

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("Quiz2", "Erreur configuration caméra", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void processImageProxy(ImageProxy imageProxy) {
        @SuppressLint("UnsafeOptInUsageError")
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .enableTracking()
                    .build();

            FaceDetection.getClient(options).process(image)
                    .addOnSuccessListener(faces -> {
                        if (faces.size() > 1 && !isActivityFinished) { // ✅ éviter si déjà fini
                            Toast.makeText(Quiz2.this, "Triche détectée ! Plus d’un visage présent.", Toast.LENGTH_SHORT).show();
                            isActivityFinished = true; // ✅ marquer comme terminé
                            Intent intent = new Intent(Quiz2.this, DetectionActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("MLKIT", "Échec détection visage", e))
                    .addOnCompleteListener(task -> imageProxy.close());
        } else {
            imageProxy.close();
        }
    }
}
