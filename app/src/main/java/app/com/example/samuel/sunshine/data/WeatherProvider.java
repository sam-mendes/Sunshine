package app.com.example.samuel.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import app.com.example.samuel.sunshine.data.WeatherContract.LocationEntry;

/**
 * Created by samuel on 25/12/15.
 */
public class WeatherProvider extends ContentProvider {

    private WeatherDbHelper mOpenHelper;

    static final int WEATHER = 100;
    static final int WEATHER_WITH_LOCATION = 101;
    static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    static final int LOCATION = 300;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

    static {
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        sWeatherByLocationSettingQueryBuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        WeatherContract.LocationEntry.TABLE_NAME +
                        " ON " + WeatherContract.WeatherEntry.TABLE_NAME +
                        "." + WeatherContract.WeatherEntry.COLUMN_LOC_KEY +
                        " = " + WeatherContract.LocationEntry.TABLE_NAME +
                        "." + WeatherContract.LocationEntry._ID);

    }

    //location.location_setting = ?
    private static final String sLocationSettingSelection =
        LocationEntry.TABLE_NAME + "." +
        LocationEntry.COLUMN_LOCATION_SETTING + " = ?";

    //location.location_setting = ? AND data >= ?
    private static final String sLocationSettingWithStartDateSelection =
            LocationEntry.TABLE_NAME + "." +
            LocationEntry.COLUMN_LOCATION_SETTING + " = ?" +
            "AND " + WeatherContract.WeatherEntry.COLUMN_DATE + " >= ?";

    //location.location_setting = ? AND date = ?
    private static final String sLocationSettingAndDaySelection =
            LocationEntry.TABLE_NAME + "." +
            LocationEntry.COLUMN_LOCATION_SETTING + " = ?" +
            "AND " + WeatherContract.WeatherEntry.COLUMN_DATE + " = ?";


    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder){
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        long startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == 0){
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, Long.toString(startDate)};
            selection = sLocationSettingWithStartDateSelection;
        }

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder);
    }

    public boolean onCreate() {
        mOpenHelper = new WeatherDbHelper(getContext());
        return true;
    }

    /*
         Students: Here is where you need to create the UriMatcher. This UriMatcher will
         match each URI to the WEATHER, WEATHER_WITH_LOCATION, WEATHER_WITH_LOCATION_AND_DATE,
         and LOCATION integer constants defined above.  You can test this by uncommenting the
         testUriMatcher test within TestUriMatcher.
      */
    static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(
                WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_WEATHER,
                WEATHER);

        uriMatcher.addURI(
                WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_WEATHER + "/*",
                WEATHER_WITH_LOCATION);

        uriMatcher.addURI(
                WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_WEATHER + "/*/#",
                WEATHER_WITH_LOCATION_AND_DATE);

        uriMatcher.addURI(
                WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_LOCATION,
                LOCATION);

        return uriMatcher;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        final int code = sUriMatcher.match(uri);

        switch (code){
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
            case LOCATION:
                return WeatherContract.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri.toString());
        }

    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
