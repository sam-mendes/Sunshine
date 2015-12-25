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
}
