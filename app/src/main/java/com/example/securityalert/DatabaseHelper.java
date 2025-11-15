package com.example.securityalert;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SecurityAlert.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "Users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USER_NAME = "name";
    private static final String COL_USER_EMAIL = "email";
    private static final String COL_USER_PHONE = "phone";
    private static final String COL_USER_PASSWORD = "password";

    private static final String TABLE_GROUPS = "Groups";
    private static final String COL_GROUP_ID = "id";
    private static final String COL_GROUP_NAME = "groupName";
    private static final String COL_GROUP_OWNER = "ownerEmail";
    private static final String COL_GROUP_MEMBER = "memberEmail";

    private static final String TABLE_ALERTS = "Alerts";
    private static final String COL_ALERT_ID = "id";
    private static final String COL_ALERT_SENDER = "senderEmail";
    private static final String COL_ALERT_MESSAGE = "message";
    private static final String COL_ALERT_LOCATION = "location";
    private static final String COL_ALERT_TIMESTAMP = "timestamp";
    private static final String COL_ALERT_PHOTO = "photoPath";
    private static final String COL_ALERT_GROUP = "groupName";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_NAME + " TEXT, " +
                COL_USER_EMAIL + " TEXT UNIQUE, " +
                COL_USER_PHONE + " TEXT, " +
                COL_USER_PASSWORD + " TEXT)";

        String createGroupsTable = "CREATE TABLE " + TABLE_GROUPS + " (" +
                COL_GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_GROUP_NAME + " TEXT, " +
                COL_GROUP_OWNER + " TEXT, " +
                COL_GROUP_MEMBER + " TEXT)";

        String createAlertsTable = "CREATE TABLE " + TABLE_ALERTS + " (" +
                COL_ALERT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ALERT_SENDER + " TEXT, " +
                COL_ALERT_MESSAGE + " TEXT, " +
                COL_ALERT_LOCATION + " TEXT, " +
                COL_ALERT_TIMESTAMP + " TEXT, " +
                COL_ALERT_PHOTO + " TEXT, " +
                COL_ALERT_GROUP + " TEXT)";

        db.execSQL(createUsersTable);
        db.execSQL(createGroupsTable);
        db.execSQL(createAlertsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALERTS);
        onCreate(db);
    }

    public boolean registerUser(String name, String email, String phone, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, name);
        values.put(COL_USER_EMAIL, email);
        values.put(COL_USER_PHONE, phone);
        values.put(COL_USER_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USER_EMAIL + "=? AND " + COL_USER_PASSWORD + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public User getUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USER_EMAIL + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4)
            );
        }
        cursor.close();
        return user;
    }

    public boolean addGroup(String groupName, String ownerEmail, String memberEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_GROUP_NAME, groupName);
        values.put(COL_GROUP_OWNER, ownerEmail);
        values.put(COL_GROUP_MEMBER, memberEmail);
        long result = db.insert(TABLE_GROUPS, null, values);
        return result != -1;
    }

    public List<String> getUserGroups(String ownerEmail) {
        List<String> groups = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT DISTINCT " + COL_GROUP_NAME + " FROM " + TABLE_GROUPS + " WHERE " + COL_GROUP_OWNER + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{ownerEmail});
        if (cursor.moveToFirst()) {
            do {
                groups.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return groups;
    }

    public List<String> getGroupMembers(String groupName, String ownerEmail) {
        List<String> members = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COL_GROUP_MEMBER + " FROM " + TABLE_GROUPS + " WHERE " + COL_GROUP_NAME + "=? AND " + COL_GROUP_OWNER + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{groupName, ownerEmail});
        if (cursor.moveToFirst()) {
            do {
                members.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return members;
    }
    public boolean removeMember(String groupName, String ownerEmail, String memberEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedRows = db.delete(TABLE_GROUPS,
                COL_GROUP_NAME + "=? AND " + COL_GROUP_OWNER + "=? AND " + COL_GROUP_MEMBER + "=?",
                new String[]{groupName, ownerEmail, memberEmail});
        return deletedRows > 0;
    }
    public boolean saveAlert(String senderEmail, String message, String location, String timestamp, String photoPath, String groupName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ALERT_SENDER, senderEmail);
        values.put(COL_ALERT_MESSAGE, message);
        values.put(COL_ALERT_LOCATION, location);
        values.put(COL_ALERT_TIMESTAMP, timestamp);
        values.put(COL_ALERT_PHOTO, photoPath);
        values.put(COL_ALERT_GROUP, groupName);
        long result = db.insert(TABLE_ALERTS, null, values);
        return result != -1;
    }

    public List<Alert> getAllAlerts() {
        List<Alert> alerts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_ALERTS + " ORDER BY " + COL_ALERT_ID + " DESC";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Alert alert = new Alert(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6)
                );
                alerts.add(alert);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return alerts;
    }
}

class User {
    int id;
    String name, email, phone, password;

    public User(int id, String name, String email, String phone, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }
}

class Alert {
    int id;
    String senderEmail, message, location, timestamp, photoPath, groupName;

    public Alert(int id, String senderEmail, String message, String location, String timestamp, String photoPath, String groupName) {
        this.id = id;
        this.senderEmail = senderEmail;
        this.message = message;
        this.location = location;
        this.timestamp = timestamp;
        this.photoPath = photoPath;
        this.groupName = groupName;
    }
}