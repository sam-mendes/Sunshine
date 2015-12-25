package app.com.example.samuel.sunshine.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

/**
 * Created by samuel on 25/12/15.
 */
public class TestUtilities extends AndroidTestCase {

    private static final String LOG_TAG = TestUtilities.class.getSimpleName();

    static final String TEST_LOCATION = "99705";
    static final long TEST_DATE = 1419033600L;  // December 20th, 2014

    static void validateCurrentRecord(String error, Cursor c, ContentValues expectedValues){
        Set<Map.Entry<String, Object>> entries = expectedValues.valueSet();

        for (Map.Entry<String, Object> entry : entries ) {
            String columnName = entry.getKey();

            int columnIndex = c.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, columnIndex == -1);

            String expectedValue = entry.getValue().toString();
            String value = c.getString(columnIndex);

            assertEquals("Value '" + value + "' " +
                        "did not match the expected value '" +
                            expectedValue + "'. " + error, value, expectedValue);
        }

    }

    static ContentValues createWeatherValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, TEST_DATE);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, 321);

        return weatherValues;
    }
}
