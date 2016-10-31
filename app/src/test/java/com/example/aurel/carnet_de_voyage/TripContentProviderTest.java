package com.example.aurel.carnet_de_voyage;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.util.Log;

import com.example.aurel.bdd.TripContentProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TripContentProviderTest extends ProviderTestCase2<TripContentProvider> {

    private ContentResolver resolver;

    public TripContentProviderTest() {
        super(TripContentProvider.class, "com.example.aurel.carnet_de_voyage");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        resolver = getMockContentResolver();
    }

    @Override
    protected void tearDown() throws Exception{
        super.tearDown();
        cleanDatabase();
    }

    private void cleanDatabase() {
        resolver.delete(TripContentProvider.CONTENT_URI, null, null);
    }

    @Test
    public void testQuery() {
        resolver = getMockContentResolver();
        Cursor cursor = resolver.query(TripContentProvider.CONTENT_URI, null, null, null, null);
        assertNotNull(cursor);

        cleanDatabase();
    }

    @Test
    public void testInser() {
        resolver = getMockContentResolver();
        Uri myRowUri = resolver.insert(TripContentProvider.CONTENT_URI, getDataValues());
        Log.w("Test", myRowUri.toString());
        assertNotNull(myRowUri);

        Cursor cursor = resolver.query(TripContentProvider.CONTENT_URI, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());

        Cursor cursor2 = resolver.query(myRowUri, null, null, null, null);
        assertNotNull(cursor2);
        assertEquals(1, cursor2.getCount());

        cleanDatabase();
    }

    private ContentValues getDataValues() {
        ContentValues values = new ContentValues();
        values.put(TripContentProvider.KEY_TITRE, "Mexique");

        return values;
    }
}
