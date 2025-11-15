package com.example.securityalert;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class ManageGroupsActivity extends AppCompatActivity {

    private Button createGroupButton, backButton;
    private LinearLayout groupsContainer;
    private DatabaseHelper db;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_groups);

        createGroupButton = findViewById(R.id.createGroupButton);
        backButton = findViewById(R.id.backButton);
        groupsContainer = findViewById(R.id.groupsContainer);

        db = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userEmail = prefs.getString("userEmail", "");

        loadGroups();

        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateGroupDialog();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadGroups() {
        groupsContainer.removeAllViews();
        List<String> groups = db.getUserGroups(userEmail);

        if (groups.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No groups created yet. Create your first group!");
            emptyText.setPadding(20, 20, 20, 20);
            emptyText.setTextSize(16);
            groupsContainer.addView(emptyText);
        } else {
            for (String groupName : groups) {
                addGroupView(groupName);
            }
        }
    }

    private void addGroupView(String groupName) {
        View groupView = LayoutInflater.from(this).inflate(R.layout.item_group, groupsContainer, false);

        TextView groupNameText = groupView.findViewById(R.id.groupNameText);
        TextView membersCountText = groupView.findViewById(R.id.membersCountText);
        Button viewMembersButton = groupView.findViewById(R.id.viewMembersButton);
        Button addMemberButton = groupView.findViewById(R.id.addMemberButton);

        groupNameText.setText(groupName);

        List<String> members = db.getGroupMembers(groupName, userEmail);
        membersCountText.setText(members.size() + " members");

        viewMembersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMembersDialog(groupName, members);
            }
        });

        addMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddMemberDialog(groupName);
            }
        });

        groupsContainer.addView(groupView);
    }

    private void showCreateGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Group");

        final EditText input = new EditText(this);
        input.setHint("Enter group name (e.g., Family, Campus Friends)");
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String groupName = input.getText().toString().trim();
            if (!groupName.isEmpty()) {
                boolean created = db.addGroup(groupName, userEmail, userEmail);
                if (created) {
                    Toast.makeText(this, "Group created: " + groupName, Toast.LENGTH_SHORT).show();
                    loadGroups();
                } else {
                    Toast.makeText(this, "Failed to create group", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showAddMemberDialog(String groupName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Member to " + groupName);

        final EditText input = new EditText(this);
        input.setHint("Enter member's email or phone");
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String memberContact = input.getText().toString().trim();
            if (!memberContact.isEmpty()) {
                boolean added = db.addGroup(groupName, userEmail, memberContact);
                if (added) {
                    Toast.makeText(this, "Member added successfully", Toast.LENGTH_SHORT).show();
                    loadGroups();
                } else {
                    Toast.makeText(this, "Failed to add member", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter a valid contact", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showMembersDialog(String groupName, List<String> members) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Members of " + groupName);

        if (members.isEmpty()) {
            builder.setMessage("No members yet");
            builder.setPositiveButton("OK", null);
        } else {
            // Create array of member names for display
            String[] memberArray = members.toArray(new String[0]);

            builder.setItems(memberArray, (dialog, which) -> {
                // When user clicks a member, show remove confirmation
                String selectedMember = memberArray[which];
                showRemoveMemberConfirmation(groupName, selectedMember);
            });

            builder.setNeutralButton("Close", null);
        }

        builder.show();
    }

    private void showRemoveMemberConfirmation(String groupName, String memberContact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Member");
        builder.setMessage("Remove " + memberContact + " from " + groupName + "?");

        builder.setPositiveButton("Remove", (dialog, which) -> {
            boolean removed = db.removeMember(groupName, userEmail, memberContact);
            if (removed) {
                Toast.makeText(this, "Member removed successfully", Toast.LENGTH_SHORT).show();
                loadGroups();
            } else {
                Toast.makeText(this, "Failed to remove member", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}