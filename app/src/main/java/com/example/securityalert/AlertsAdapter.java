package com.example.securityalert;

import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.List;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertViewHolder> {

    private Context context;
    private List<Alert> alerts;

    public AlertsAdapter(Context context, List<Alert> alerts) {
        this.context = context;
        this.alerts = alerts;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        Alert alert = alerts.get(position);

        holder.senderText.setText("From: " + alert.senderEmail);
        holder.messageText.setText(alert.message);
        holder.locationText.setText("Location: " + alert.location);
        holder.timestampText.setText(alert.timestamp);
        holder.groupText.setText("Group: " + alert.groupName);

        if (alert.photoPath != null && !alert.photoPath.isEmpty()
                && !alert.photoPath.contains("No photo")
                && !alert.photoPath.contains("cancelled")) {
            File imgFile = new File(alert.photoPath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                holder.photoView.setImageBitmap(bitmap);
                holder.photoView.setVisibility(View.VISIBLE);


                final String photoPathFinal = alert.photoPath;
                holder.photoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openPhotoFullScreen(photoPathFinal);
                    }
                });
            } else {
                holder.photoView.setVisibility(View.GONE);
            }
        } else {
            holder.photoView.setVisibility(View.GONE);
        }

        final String locationFinal = alert.location;
        holder.openMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLocationInMaps(locationFinal);
            }
        });
    }

    private void openLocationInMaps(String location) {
        // Check if location exists
        if (location == null || location.isEmpty()) {
            Toast.makeText(context, " Location not available for this alert", Toast.LENGTH_LONG).show();
            return;
        }

        // Check if location capture failed
        if (location.contains("unavailable") || location.contains("denied")
                || location.contains("not found") || location.contains("permission")
                || location.contains("error")) {
            Toast.makeText(context, " " + location, Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // Clean location string
            location = location.replace("Location:", "").trim();

        //split cordinates
            String[] coords = location.split(",");

            if (coords.length != 2) {
                Toast.makeText(context, " Invalid location format", Toast.LENGTH_SHORT).show();
                return;
            }

            String lat = coords[0].trim();
            String lng = coords[1].trim();

            // Validate they are numbers
            try {
                Double.parseDouble(lat);
                Double.parseDouble(lng);
            } catch (NumberFormatException e) {
                Toast.makeText(context, " Invalid coordinates: " + lat + ", " + lng, Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(context, " Opening location in maps...", Toast.LENGTH_SHORT).show();

            // Method 1: Try Google Maps app
            try {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + lat + "," + lng + "(Emergency Alert Location)");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                context.startActivity(mapIntent);
                return;
            } catch (Exception e) {
                // Google Maps not installed, try next method
            }

            // Method 2: Try any maps app
            try {
                Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                context.startActivity(mapIntent);
                return;
            } catch (Exception e) {
                // No maps app, try browser
            }

            // Method 3: Open in browser (ALWAYS works)
            String url = "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng;
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(browserIntent);

        } catch (Exception e) {
            Toast.makeText(context, " Error opening maps: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // UPDATED: Better photo viewing with FileProvider
    private void openPhotoFullScreen(String photoPath) {
        try {
            File photoFile = new File(photoPath);

            if (!photoFile.exists()) {
                Toast.makeText(context, " Photo file not found at: " + photoPath, Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(context, " Opening photo...", Toast.LENGTH_SHORT).show();

            // Try with FileProvider first (more secure)
            try {
                Uri photoUri = FileProvider.getUriForFile(context,
                        context.getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(photoUri, "image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
                return;
            } catch (Exception e) {
                // FileProvider failed, try direct Uri
            }

            // Fallback: Direct file Uri
            Uri photoUri = Uri.fromFile(photoFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(photoUri, "image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(context, " Could not open photo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView senderText, messageText, locationText, timestampText, groupText;
        ImageView photoView;
        Button openMapButton;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            senderText = itemView.findViewById(R.id.senderText);
            messageText = itemView.findViewById(R.id.messageText);
            locationText = itemView.findViewById(R.id.locationText);
            timestampText = itemView.findViewById(R.id.timestampText);
            groupText = itemView.findViewById(R.id.groupText);
            photoView = itemView.findViewById(R.id.photoView);
            openMapButton = itemView.findViewById(R.id.openMapButton);
        }
    }
}