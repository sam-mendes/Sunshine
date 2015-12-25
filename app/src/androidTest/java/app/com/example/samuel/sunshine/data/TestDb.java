package app.com.example.samuel.sunshine.data;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;
import java.util.Set;

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

    }

}
