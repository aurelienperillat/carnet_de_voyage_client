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

public class CardContentProvider extends ContentProvider {
    public static final Uri CONTENT_URI = Uri.parse("content://com.example.aurel.carnet_de_voyage");
    private static final int ALLROWS = 1;
    private static final int SINGLE_ROW = 2;
    private static final UriMatcher uriMatcher;

    //Table card
    public static final String KEY_ID = "_id";
    public static final String KEY_TRIP_ID = "key_trip_id";
    public static final String KEY_IMG = "key_img";
    public static final String KEY_TEXT = "key_text";

    private CardDBHelper dbHelper;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.example.aurel.carnet_de_voyage","card",ALLROWS);
        uriMatcher.addURI("com.example.aurel.carnet_de_voyage","card/#",SINGLE_ROW);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new CardContentProvider.CardDBHelper(getContext(), CardContentProvider.CardDBHelper.DATABASE_NAME, null,
                CardContentProvider.CardDBHelper.DATABASE_VERSION);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        String groupBy = null;
        String having = null;

        queryBuilder.setTables(CardContentProvider.CardDBHelper.DATABASE_TABLE);

        switch (uriMatcher.match(uri)) {
            case SINGLE_ROW :
                String rowId = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(KEY_ID + "=" + rowId);
            default: break;
        }

        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, groupBy, having, sortOrder);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ALLROWS : return "vnd.android.cursor.dir/vnd.paad.elemental";
            case  SINGLE_ROW : return "vnd.android.cursor.item/vnd.paad.elemental";
            default: throw new IllegalArgumentException("URI non reconnue");
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String nullColumnHack = null;

        long id = db.insert(CardContentProvider.CardDBHelper.DATABASE_TABLE, nullColumnHack, values);

        if(id > -1) {
            Uri insertedId = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(insertedId, null);

            return insertedId;
        }

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case SINGLE_ROW :
                String rowId = uri.getPathSegments().get(1);
                selection = KEY_ID + "=" + rowId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
            default: break;
        }
        if(selection == null)
            selection = "1";

        int deleteCount = db.delete(CardContentProvider.CardDBHelper.DATABASE_TABLE, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case SINGLE_ROW :
                String rowId = uri.getPathSegments().get(1);
                selection = KEY_ID + "=" + rowId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
            default: break;
        }
        if(selection == null)
            selection = "1";

        int updateCount = db.update(CardContentProvider.CardDBHelper.DATABASE_TABLE, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return updateCount;
    }

    private static class CardDBHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "voyage";
        private static final String DATABASE_TABLE = "card";
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + KEY_ID +
                " integer primary key autoincrement, " + KEY_TRIP_ID + " integer secondary key not null, " + KEY_IMG +
                " text not null, " + KEY_TEXT + " text not null);";


        public CardDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("DATABASE", "Mis à jour de la version " + oldVersion + " vers la version" + newVersion +
                    " : toutes les données seront persues.");
            db.execSQL("drop table if exists " + DATABASE_TABLE);
            onCreate(db);
        }
    }
}
