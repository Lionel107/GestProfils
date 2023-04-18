package com.example.gestprofils;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_GAME_ACTIVITY = 42;
    private static final String SHARED_PREF_USER_INFO = "SHARED_PREF_USER_INFO";
    private static final String SHARED_PREF_USER_INFO_NAME = "SHARED_PREF_USER_INFO_NAME";
    private static final String SHARED_PREF_USER_INFO_SCORE = "SHARED_PREF_USER_INFO_SCORE";
    private TextView mGreetingTextView;
    private EditText mNameEditText;
    private Button mPlayButton;
    private Button mScoreButton;
    private TextView textView;
    String simpleFileName ="note.txt";
    private ImageView imageView;
    private VideoView videoView;
    private Button imageButton;
    private Button videoButton;

    private static final int REQUEST_ID_READ_WRITE_PERMISSION = 99;
    private static final int REQUEST_ID_IMAGE_CAPTURE = 100;
    private static final int REQUEST_ID_VIDEO_CAPTURE = 101;


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Dans votre activité MainActivity, dans la méthode onCreate()
        Button buttonImage = findViewById(R.id.button_image);
        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Créer le fichier de sortie pour l'image capturée
                File outputDirectory = getOutputDirectory();
                File outputFile = new File(outputDirectory, "image.jpg");

                // Configurer le gestionnaire de capture d'image
                ImageCapture.OutputFileOptions outputOptions =
                        new ImageCapture.OutputFileOptions.Builder(outputFile).build();
                ImageCapture imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .build();

                // Lancer la capture d'image
                imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(MainActivity.this), new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // L'image a été capturée et enregistrée dans le fichier outputFile
                        // Vous pouvez maintenant enregistrer le chemin du fichier dans votre base de données ou tout autre endroit approprié
                        String imagePath = outputFile.getAbsolutePath();
                        // TODO: enregistrer l'image dans la base de données
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        // La capture d'image a échoué
                        Toast.makeText(MainActivity.this, "Erreur de capture d'image : " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            private File getOutputDirectory() {
                File mediaDir = getExternalMediaDirs()[0];
                File outputDir = new File(mediaDir, "myapp");
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }
                return outputDir;
            }
        });


        setContentView(R.layout.activity_main);

        mGreetingTextView = findViewById(R.id.main_textview_greeting);
        mNameEditText = findViewById(R.id.main_edittext_name);
        mNameEditText = findViewById(R.id.main_edittext_adresse);
        mPlayButton = findViewById(R.id.main_button_play);
        videoView = findViewById(R.id.videoView);
        imageButton = findViewById(R.id.button_image);
        videoButton = findViewById(R.id.button_video);

        mPlayButton.setEnabled(false);

        mNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                mPlayButton.setEnabled(!s.toString().isEmpty());
            }
        });



        mScoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readData();
            }
        });

        this.imageButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });

        this.videoButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                askPermissionAndCaptureVideo();
            }
        });

    }



    private void readData() {
        try {

            FileInputStream in = this.openFileInput(simpleFileName);

            BufferedReader br= new BufferedReader(new InputStreamReader(in));

            StringBuilder sb= new StringBuilder();
            String s= null;
            while((s= br.readLine())!= null)  {
                sb.append(s).append("\n");
            }
            this.textView.setText(sb.toString());

        } catch (Exception e) {
            Toast.makeText(this,"Error : score file does not exist.",Toast.LENGTH_SHORT).show();
        }
    }

    private void captureImage() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        this.startActivityForResult(intent, REQUEST_ID_IMAGE_CAPTURE);
    }

    private void askPermissionAndCaptureVideo() {

        if (android.os.Build.VERSION.SDK_INT >= 23) {

            int readPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            int writePermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (writePermission != PackageManager.PERMISSION_GRANTED ||
                    readPermission != PackageManager.PERMISSION_GRANTED) {

                this.requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_ID_READ_WRITE_PERMISSION
                );
                return;
            }
        }
        this.captureVideo();
    }

    private void captureVideo() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

            File dir = Environment.getExternalStorageDirectory();
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String savePath = dir.getAbsolutePath() + "/myvideo.mp4";
            File videoFile = new File(savePath);
            Uri videoUri = Uri.fromFile(videoFile);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            this.startActivityForResult(intent, REQUEST_ID_VIDEO_CAPTURE);

        } catch(Exception e)  {
            Toast.makeText(this, "Error capture video: " +e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_ID_READ_WRITE_PERMISSION: {

                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_LONG).show();

                    this.captureVideo();

                }
                else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }

    }
    public String getnom(){
        EditText editText = findViewById(R.id.main_edittext_name); // Récupérer la référence du champ de texte EditText
        return editText.getText().toString(); // Récupérer le texte saisi par l'utilisateur
    }
    public String getadresse(){
        EditText editText = findViewById(R.id.main_edittext_adresse); // Récupérer la référence du champ de texte EditText
        return editText.getText().toString(); // Récupérer le texte saisi par l'utilisateur
    }

}