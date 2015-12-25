package app.com.example.samuel.sunshine.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;
import java.util.Set;

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
        tableNameSet.add(WeatherContract.WeatherEntry.TABLE_NAME);

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

}
