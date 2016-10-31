package com.example.aurel.bdd;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

public class TripContentProvider extends ContentProvider{

    public static final Uri CONTENT_URI_TRIP = Uri.parse("content://com.example.aurel.carnet_de_voyage/trip");
    public static final Uri CONTENT_URI_CARD = Uri.parse("content://com.example.aurel.carnet_de_voyage/card");
    private static final int TRIP_ALLROWS = 1;
    private static final int TRIP_SINGLE_ROW = 2;
    private static final int CARD_ALLROWS = 3;
    private static final int CARD_SINGLE_ROW = 4;
    private static final UriMatcher uriMatcher;

    //Table trip
    public static final String KEY_ID = "_id";
    public static final String KEY_TITRE = "key_titre";
    //Table card
    public static final String KEY_CARD_ID = "card_id";
    public static final String KEY_TRIP_ID = "key_trip_id";
    public static final String KEY_IMG = "key_img";
    public static final String KEY_TEXT = "key_text";

    private TripDBHelper dbHelper;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.example.aurel.carnet_de_voyage","trip",TRIP_ALLROWS);
        uriMatcher.addURI("com.example.aurel.carnet_de_voyage","trip/#",TRIP_SINGLE_ROW);
        uriMatcher.addURI("com.example.aurel.carnet_de_voyage","card",CARD_ALLROWS);
        uriMatcher.addURI("com.example.aurel.carnet_de_voyage","card/#",CARD_SINGLE_ROW);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new TripDBHelper(getContext(), TripDBHelper.DATABASE_NAME, null, TripDBHelper.DATABASE_VERSION);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        String groupBy = null;
        String having = null;

        switch (uriMatcher.match(uri)) {
            case TRIP_ALLROWS :
                queryBuilder.setTables(TripDBHelper.DATABASE_TABLE_TRIP);
             break;
            case CARD_ALLROWS :
                queryBuilder.setTables(TripDBHelper.DATABASE_TABLE_CARD);
            break;
            case TRIP_SINGLE_ROW :
                queryBuilder.setTables(TripDBHelper.DATABASE_TABLE_TRIP);
                String rowId = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(KEY_ID + "=" + rowId);
            break;
            case CARD_SINGLE_ROW :
                queryBuilder.setTables(TripDBHelper.DATABASE_TABLE_CARD);
                String rowId2 = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(KEY_CARD_ID + "=" + rowId2);
            break;
            default: break;
        }

        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, groupBy, having, sortOrder);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case TRIP_ALLROWS : return "vnd.android.cursor.dir/vnd.paad.elemental";
            case  TRIP_SINGLE_ROW : return "vnd.android.cursor.item/vnd.paad.elemental";
            case CARD_ALLROWS : return "vnd.android.cursor.dir/vnd.paad.elemental";
            case  CARD_SINGLE_ROW : return "vnd.android.cursor.item/vnd.paad.elemental";
            default: throw new IllegalArgumentException("URI non reconnue");
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String nullColumnHack = null;
        switch (uriMatcher.match(uri)){
            case TRIP_ALLROWS :
                long id = db.insert(TripDBHelper.DATABASE_TABLE_TRIP, nullColumnHack, values);

                if(id > -1) {
                    Uri insertedId = ContentUris.withAppendedId(uri, id);
                    getContext().getContentResolver().notifyChange(insertedId, null);

                    return insertedId;
                }
            case CARD_ALLROWS :
                long id2 = db.insert(TripDBHelper.DATABASE_TABLE_CARD, nullColumnHack, values);

                if(id2 > -1) {
                    Uri insertedId = ContentUris.withAppendedId(uri, id2);
                    getContext().getContentResolver().notifyChange(insertedId, null);

                    return insertedId;
                }
            default: break;
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case TRIP_ALLROWS :
                if(selection == null)
                    selection = "1";
                int deleteCount = db.delete(TripDBHelper.DATABASE_TABLE_TRIP, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return deleteCount;
            case CARD_ALLROWS :
                if(selection == null)
                    selection = "1";
                int deleteCount2 = db.delete(TripDBHelper.DATABASE_TABLE_CARD, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return deleteCount2;
            case TRIP_SINGLE_ROW :
                String rowId = uri.getPathSegments().get(1);
                selection = KEY_ID + "=" + rowId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
                if(selection == null)
                    selection = "1";
                int deleteCount3 = db.delete(TripDBHelper.DATABASE_TABLE_TRIP, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return deleteCount3;
            case CARD_SINGLE_ROW :
                String rowId2 = uri.getPathSegments().get(1);
                selection = KEY_ID + "=" + rowId2 + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
                if(selection == null)
                    selection = "1";
                int deleteCount4 = db.delete(TripDBHelper.DATABASE_TABLE_CARD, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return deleteCount4;
            default: break;
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case TRIP_ALLROWS :
                if(selection == null)
                    selection = "1";
                int deleteCount = db.update(TripDBHelper.DATABASE_TABLE_TRIP, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return deleteCount;
            case CARD_ALLROWS :
                if(selection == null)
                    selection = "1";
                int deleteCount2 = db.update(TripDBHelper.DATABASE_TABLE_CARD, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return deleteCount2;
            case TRIP_SINGLE_ROW :
                String rowId = uri.getPathSegments().get(1);
                selection = KEY_ID + "=" + rowId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
                if(selection == null)
                    selection = "1";
                int deleteCount3 = db.update(TripDBHelper.DATABASE_TABLE_TRIP, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return deleteCount3;
            case CARD_SINGLE_ROW :
                String rowId2 = uri.getPathSegments().get(1);
                selection = KEY_ID + "=" + rowId2 + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
                if(selection == null)
                    selection = "1";
                int deleteCount4 = db.update(TripDBHelper.DATABASE_TABLE_CARD, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return deleteCount4;
            default: break;
        }
        return 0;
    }

    private static class TripDBHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "voyage";
        private static final int DATABASE_VERSION = 2;

        private static final String DATABASE_TABLE_TRIP = "trip";
        private static final String DATABASE_CREATE_TRIP = "create table " + DATABASE_TABLE_TRIP + " (" + KEY_ID +
                " integer primary key autoincrement, " + KEY_TITRE + " text not null);";

        private static final String DATABASE_TABLE_CARD = "card";
        private static final String DATABASE_CREATE_CARD = "create table " + DATABASE_TABLE_CARD + " (" + KEY_CARD_ID +
                " integer primary key autoincrement, " + KEY_TRIP_ID + " integer secondary key not null, " + KEY_IMG +
                " text not null, " + KEY_TEXT + " text not null);";


        public TripDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_TRIP);
            db.execSQL(DATABASE_CREATE_CARD);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("DATABASE", "Mis à jour de la version " + oldVersion + " vers la version" + newVersion +
            " : toutes les données seront persues.");
            db.execSQL("drop table if exists " + DATABASE_TABLE_TRIP);
            db.execSQL("drop table if exists " + DATABASE_TABLE_CARD);
            onCreate(db);
        }
    }
}
