package app.com.example.samuel.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import app.com.example.samuel.sunshine.data.WeatherContract.LocationEntry;
import app.com.example.samuel.sunshine.data.WeatherContract.WeatherEntry;

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
                WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        LocationEntry.TABLE_NAME +
                        " ON " + WeatherEntry.TABLE_NAME +
                        "." + WeatherEntry.COLUMN_LOC_KEY +
                        " = " + LocationEntry.TABLE_NAME +
                        "." + LocationEntry._ID);

    }

    //location.location_setting = ?
    private static final String sLocationSettingSelection =
        LocationEntry.TABLE_NAME + "." +
        LocationEntry.COLUMN_LOCATION_SETTING + " = ?";

    //location.location_setting = ? AND data >= ?
    private static final String sLocationSettingWithStartDateSelection =
            LocationEntry.TABLE_NAME + "." +
            LocationEntry.COLUMN_LOCATION_SETTING + " = ?" +
            " AND " + WeatherEntry.COLUMN_DATE + " >= ?";

    //location.location_setting = ? AND date = ?
    private static final String sLocationSettingAndDaySelection =
            LocationEntry.TABLE_NAME + "." +
            LocationEntry.COLUMN_LOCATION_SETTING + " = ?" +
            " AND " + WeatherEntry.COLUMN_DATE + " = ?";


    private Cursor getLocation(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){
        return mOpenHelper.getReadableDatabase().query(
                LocationEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getWeather(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){
        return mOpenHelper.getReadableDatabase().query(
                WeatherEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder){
        String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);
        long startDate = WeatherEntry.getStartDateFromUri(uri);

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

    private Cursor getWeatherByLocationSettingAndDate(
            Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);
        long date = WeatherEntry.getDateFromUri(uri);

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingAndDaySelection,
                new String[]{locationSetting, Long.toString(date)},
                null,
                null,
                sortOrder
        );
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

        final int match = sUriMatcher.match(uri);
        Cursor cursor;
        switch (match){
            case WEATHER:
                cursor = getWeather(uri, projection, selection, selectionArgs, sortOrder);
                break;

            case WEATHER_WITH_LOCATION:
                cursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                break;

            case WEATHER_WITH_LOCATION_AND_DATE:
                cursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
                break;

            case LOCATION:
                cursor = getLocation(uri, projection, selection, selectionArgs, sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        final int code = sUriMatcher.match(uri);

        switch (code){
            case WEATHER:
            case WEATHER_WITH_LOCATION:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherEntry.CONTENT_ITEM_TYPE;
            case LOCATION:
                return LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri.toString());
        }

    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);

        Uri returnUri;

        switch (match){
            case WEATHER:
                normalizeDate(values);
                long weatherId = mOpenHelper.getWritableDatabase().insert(
                        WeatherEntry.TABLE_NAME,
                        null,
                        values);

                returnUri = ContentUris.appendId(WeatherEntry.CONTENT_URI.buildUpon(), weatherId).build();

                break;


            case LOCATION:

                long locationId = mOpenHelper.getWritableDatabase().insert(
                        LocationEntry.TABLE_NAME,
                        null,
                        values);

                returnUri = ContentUris.appendId(LocationEntry.CONTENT_URI.buildUpon(), locationId).build();
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(WeatherContract.WeatherEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
            values.put(WeatherContract.WeatherEntry.COLUMN_DATE, WeatherContract.normalizeDate(dateValue));
        }
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        int rowsAffected = 0;
        if ( null == selection ) selection = "1";
        switch (match){
            case WEATHER:
                rowsAffected = mOpenHelper.getWritableDatabase().delete(WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case LOCATION:
                rowsAffected = mOpenHelper.getWritableDatabase().delete(LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsAffected != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {

        final int match = sUriMatcher.match(uri);
        int rowsAffected = 0;

        switch (match){
            case WEATHER:
                normalizeDate(values);
                rowsAffected = mOpenHelper.getWritableDatabase().update(
                        WeatherEntry.TABLE_NAME,
                        values,
                        where,
                        whereArgs);

                break;

            case LOCATION:
                rowsAffected = mOpenHelper.getWritableDatabase().update(
                        LocationEntry.TABLE_NAME,
                        values,
                        where,
                        whereArgs);

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
    }

    @Override
    public void shutdown() {
        super.shutdown();

        mOpenHelper.close();

    }
}
