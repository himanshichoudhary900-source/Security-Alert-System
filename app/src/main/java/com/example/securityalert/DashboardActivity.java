package com.example.securityalert;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.io.File;
import android.os.Handler;
import com.google.android.gms.tasks.OnFailureListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;

    private Button emergencyButton, manageGroupsButton, viewAlertsButton, logoutButton;
    private TextView welcomeText;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private FusedLocationProviderClient fusedLocationClient;

    private String currentPhotoPath;
    private String currentLocation = "Location unavailable";
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        emergencyButton = findViewById(R.id.emergencyButton);
        manageGroupsButton = findViewById(R.id.manageGroupsButton);
        viewAlertsButton = findViewById(R.id.viewAlertsButton);
        logoutButton = findViewById(R.id.logoutButton);
        welcomeText = findViewById(R.id.welcomeText);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        userEmail = prefs.getString("userEmail", "");
        User user = db.getUser(userEmail);
        if (user != null) {
            welcomeText.setText("Welcome, " + user.name + "!");
        }

        checkAndRequestPermissions();

        emergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEmergencyConfirmation();
            }
        });

        manageGroupsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, ManageGroupsActivity.class));
            }
        });

        viewAlertsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, ViewAlertsActivity.class));
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA
        };

        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
        }
    }

    private void showEmergencyConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Emergency Alert");
        builder.setMessage("Are you sure you want to send an emergency alert to your groups?");
        builder.setPositiveButton("SEND ALERT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                triggerEmergency();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void triggerEmergency() {
        // Step 1: Show alert that we're getting location
        Toast.makeText(this, " Getting your location...", Toast.LENGTH_SHORT).show();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            currentLocation = "Location permission denied";
            Toast.makeText(this, " Location permission denied. Please enable in settings.", Toast.LENGTH_LONG).show();
            // Continue with camera anyway
            Toast.makeText(this, " Opening camera...", Toast.LENGTH_SHORT).show();
            dispatchTakePictureIntent();
            return;
        }

        // Get location with callback
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLocation = location.getLatitude() + ", " + location.getLongitude();
                            Toast.makeText(DashboardActivity.this,
                                    "Location captured: " + currentLocation,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            currentLocation = "Location not available";
                            Toast.makeText(DashboardActivity.this,
                                    " GPS signal weak. Turn on GPS and wait a moment.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // Step 2: Now open camera with alert
                        Toast.makeText(DashboardActivity.this,
                                " Opening camera for evidence photo...",
                                Toast.LENGTH_LONG).show();

                        // Small delay so user can see the message
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dispatchTakePictureIntent();
                            }
                        }, 1000); // 1 second delay
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        currentLocation = "Location error";
                        Toast.makeText(DashboardActivity.this,
                                " Location error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();

                        // Continue with camera
                        Toast.makeText(DashboardActivity.this,
                                " Opening camera...",
                                Toast.LENGTH_SHORT).show();
                        dispatchTakePictureIntent();
                    }
                });
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            currentLocation = "Location permission denied";
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location.getLatitude() + ", " + location.getLongitude();
                } else {
                    currentLocation = "Location not found";
                }
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating photo file", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.securityalert.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            sendAlertToGroups("No photo captured");
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "ALERT_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                // Photo captured successfully
                Toast.makeText(this, "✅ Photo saved!", Toast.LENGTH_SHORT).show();
                sendAlertToGroups(currentPhotoPath); // Make sure currentPhotoPath is set
            } else {
                // User cancelled
                Toast.makeText(this, "⚠️ Photo cancelled", Toast.LENGTH_SHORT).show();
                sendAlertToGroups("Photo capture cancelled");
            }
        }
    }

    private void sendAlertToGroups(String photoPath) {
        List<String> groups = db.getUserGroups(userEmail);

        if (groups.isEmpty()) {
            Toast.makeText(this, "No groups found. Please create groups first.", Toast.LENGTH_LONG).show();
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String message = "EMERGENCY! I need help immediately!";

        int alertsSent = 0;
        for (String group : groups) {
            boolean saved = db.saveAlert(userEmail, message, currentLocation, timestamp, photoPath, group);
            if (saved) {
                alertsSent++;
            }
        }

        if (alertsSent > 0) {
            Toast.makeText(this, "Emergency alert sent to " + alertsSent + " group(s)!", Toast.LENGTH_LONG).show();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Alert Sent Successfully");
            builder.setMessage("Your emergency alert has been sent to:\n\nGroups: " + alertsSent + "\nLocation: " + currentLocation + "\nTime: " + timestamp);
            builder.setPositiveButton("OK", null);
            builder.show();
        } else {
            Toast.makeText(this, "Failed to send alerts", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                Toast.makeText(this, "Permissions are required for emergency alerts", Toast.LENGTH_LONG).show();
            }
        }
    }
}