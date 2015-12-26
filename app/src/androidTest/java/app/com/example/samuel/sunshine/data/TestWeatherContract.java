package app.com.example.samuel.sunshine.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by samuel on 25/12/15.
 */
public class TestWeatherContract extends AndroidTestCase {
    private static final String LOG_TAG = TestWeatherContract.class.getSimpleName();


    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final String TEST_WEATHER_LOCATION = "/North Pole";
    private static final long TEST_WEATHER_DATE = 1419033600L;  // December 20th, 2014

    public void testBuildWeatherLocation(){
        Uri locationUri = WeatherContract.WeatherEntry.buildWeatherLocation(TEST_WEATHER_LOCATION);

        assertNotNull("Error: Null Uri returned.  You must fill-in buildWeatherLocation in " +
                        "WeatherContract.",
                locationUri);

        assertEquals(
                "Error: uri doesn't appended /North Pole correctly",
                locationUri.getLastPathSegment(), TEST_WEATHER_LOCATION);

        assertEquals("Error: Weather location Uri doesn't match our expected result",
                locationUri.toString(),
                "content://com.example.samuel.sunshine/weather/%2FNorth%20Pole");
    }

}
