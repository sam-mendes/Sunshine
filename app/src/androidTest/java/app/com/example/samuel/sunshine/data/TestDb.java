package app.com.example.samuel.sunshine.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;
import java.util.Set;

import app.com.example.samuel.sunshine.data.WeatherContract.WeatherEntry;

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
        insertLocation();
    }

    public long insertLocation() {
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        long locationRowId;
        locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                WeatherContract.LocationEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from location query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Location Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from location query",
                cursor.moveToNext() );

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
        return locationRowId;
    }


    public void testWeatherTable() {
        // First insert the location, and then use the locationRowId to insert
        // the weather. Make sure to cover as many failure cases as you can.

        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();
        long locationId = insertLocation();

        ContentValues weatherValues = TestUtilities.createWeatherValues(locationId);

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

}
