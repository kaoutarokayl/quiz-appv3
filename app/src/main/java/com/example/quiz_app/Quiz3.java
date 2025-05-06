package com.example.quiz_app;

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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Quiz3 extends AppCompatActivity {
    private RadioGroup rg;
    private RadioButton rb;
    private Button bNext;
    private int score;
    private TextView timerTextView;
    private CountDownTimer countDownTimer;
    private static final long TOTAL_TIME = 30000; // 30 secondes
    private boolean isTimerRunning = false;
    private String RepCorrect = "Non";

    private boolean userLeftApp = false;
    private boolean isFirstStart = true; // ✅ Ajout

    // ➕ CAMERA
    private ExecutorService cameraExecutor;
    private PreviewView previewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz3);

        rg = findViewById(R.id.rg);
        bNext = findViewById(R.id.bNext);
        timerTextView = findViewById(R.id.timerTextView);

        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0);

        previewView = findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();
        startCamera();

        startTimer(); // ✅ Timer lancé qu’une seule fois

        bNext.setOnClickListener(v -> {
            cancelTimer();
            if (rg.getCheckedRadioButtonId() == -1) {
                Toast.makeText(getApplicationContext(), "Merci de choisir une réponse S.V.P !", Toast.LENGTH_SHORT).show();
            } else {
                rb = findViewById(rg.getCheckedRadioButtonId());
                if (rb.getText().toString().equals(RepCorrect)) {
                    score += 1;
                }
                goToNextQuiz();
            }
        });
    }

    private void startTimer() {
        if (isTimerRunning) return; // ✅ Evite relance multiple
        isTimerRunning = true;

        countDownTimer = new CountDownTimer(TOTAL_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerTextView.setText("Temps restant: " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                timerTextView.setText("Temps écoulé !");
                Toast.makeText(Quiz3.this, "Temps écoulé !", Toast.LENGTH_SHORT).show();
                goToNextQuiz();
            }
        }.start();
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            isTimerRunning = false;
        }
    }

    private void goToNextQuiz() {
        cancelTimer();
        Intent intent = new Intent(Quiz3.this, Quiz4.class);
        intent.putExtra("score", score);
        startActivity(intent);
        overridePendingTransition(R.anim.exit, R.anim.entry);
        finish();
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

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImage);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("Quiz3", "Erreur configuration caméra", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analyzeImage(ImageProxy imageProxy) {
        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .enableTracking()
                    .build();

            FaceDetection.getClient(options).process(image)
                    .addOnSuccessListener(faces -> {
                        if (faces.size() > 1) {
                            Toast.makeText(Quiz3.this, "Triche détectée ! Plus d’un visage présent.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Quiz3.this, DetectionActivity.class);
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

    @Override
    protected void onPause() {
        super.onPause();
        if (!isFirstStart) { // ✅ Ignore la pause initiale
            userLeftApp = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isFirstStart) { // ✅ Ignore le stop initial
            userLeftApp = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isFirstStart) {
            isFirstStart = false; // ✅ Ignore la 1ʳᵉ fois
        } else if (userLeftApp) {
            Toast.makeText(this, "Triche détectée : vous avez quitté le quiz.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Quiz3.this, DetectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
