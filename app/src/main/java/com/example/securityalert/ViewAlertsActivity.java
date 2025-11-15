package com.example.securityalert;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ViewAlertsActivity extends AppCompatActivity {

    private RecyclerView alertsRecyclerView;
    private Button backButton;
    private AlertsAdapter adapter;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_alerts);

        alertsRecyclerView = findViewById(R.id.alertsRecyclerView);
        backButton = findViewById(R.id.backButton);

        db = new DatabaseHelper(this);

        alertsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadAlerts();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadAlerts() {
        List<Alert> alerts = db.getAllAlerts();
        adapter = new AlertsAdapter(this, alerts);
        alertsRecyclerView.setAdapter(adapter);
    }
}