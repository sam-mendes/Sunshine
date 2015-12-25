package app.com.example.samuel.sunshine.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.test.AndroidTestCase;

import java.util.HashSet;
import java.util.Set;

import app.com.example.samuel.sunshine.data.WeatherContract.WeatherEntry;

import static app.com.example.samuel.sunshine.data.TestUtilities.createWeatherValues;
import static app.com.example.samuel.sunshine.data.TestUtilities.validateCurrentRecord;
import static app.com.example.samuel.sunshine.data.WeatherContract.LocationEntry;
/**
 * Created by samuel on 25/12/15.
 */
public class TestDb extends AndroidTestCase {


    public final String LOG_TAG = TestDb.class.getSimpleName();


    void deleteDatabase(){ mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);}

    public void setUp() {
        deleteDatabase();
    }

    public void testCreateDb() throws Throwable {
        final Set<String> tableNameSet = new HashSet<>();
        tableNameSet.add(WeatherContract.LocationEntry.TABLE_NAME);
        tableNameSet.add(WeatherEntry.TABLE_NAME);

        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: This means that the database has not been created correctly"
                , c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameSet.remove(c.getString(0));
        }   while (c.moveToNext());

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameSet.isEmpty());


        c = db.rawQuery("PRAGMA table_info(" + LocationEntry.TABLE_NAME + ")", null);

        assertTrue("Error: This means that we were unable to query the database for table information."
                , c.moveToFirst());

        // Build a HashSet of all column names we want to look for
        final Set<String> locationColumnSet = new HashSet<>();
        locationColumnSet.add(LocationEntry._ID);
        locationColumnSet.add(LocationEntry.COLUMN_CITY_NAME);
        locationColumnSet.add(LocationEntry.COLUMN_COORD_LAT);
        locationColumnSet.add(LocationEntry.COLUMN_COORD_LONG);
        locationColumnSet.add(LocationEntry.COLUMN_LOCATION_SETTING);

        int columnNameIndex = c.getColumnIndex("name");

        do {
            locationColumnSet.remove(c.getString(columnNameIndex));
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnSet.isEmpty());
        db.close();

    }


    public void testLocationTable(){
        // First step: Get reference to writable database

        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();
        assertTrue("Error: Can not create a writable database.", db.isOpen());
        
        ContentValues values = createNorthPoleLocationValues();

        long rowId = insertLocation(db, values);

        assertTrue("Error: Could not insert location values. ",
                rowId > -1);

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertTrue("Error: This means that we were unable to query the database",
                cursor.moveToFirst());

        TestUtilities.validateCurrentRecord(
                "Error: Location Query Validation Failed", cursor, values);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from location query",
                cursor.moveToNext() );

        cursor.close();
        db.close();

    }

    private long insertLocation(SQLiteDatabase db, ContentValues values) {
        return db.insert(LocationEntry.TABLE_NAME, null, values);
    }

    @NonNull
    private ContentValues createNorthPoleLocationValues() {

        ContentValues values = new ContentValues();
        String testLocationSetting = "99705";
        String testCityName = "North Pole";
        double testLatitude = 64.7488;
        double testLongitude = -147.353;

        values.put(LocationEntry.COLUMN_CITY_NAME, testCityName);
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        values.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
        values.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);

        return values;
    }


    public void testWeatherTable() {
        // First insert the location, and then use the locationRowId to insert
        // the weather. Make sure to cover as many failure cases as you can.

        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();
        long locationId = insertLocation(db, createNorthPoleLocationValues());

        ContentValues weatherValues = createWeatherValues(locationId);

        long weatherId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);

        assertTrue("Error: weather couldn't not be inserted.", weatherId != -1);

        Cursor cursor = db.query(WeatherEntry.TABLE_NAME, null, null, null, null, null, null, null);

        assertTrue("Error: There aren't any row on '" +
                WeatherEntry.TABLE_NAME + "' ",
                cursor.moveToFirst());

        validateCurrentRecord("Error: Weather Query validation failed", cursor, weatherValues);

        assertFalse("Error: Returned more than one row", cursor.moveToNext());

        cursor.close();
        db.close();
    }

    @NonNull
    private ContentValues createNorthPoleWeatherValues(){
        return null;
    }



}
