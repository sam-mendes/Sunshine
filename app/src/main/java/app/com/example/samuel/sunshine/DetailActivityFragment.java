package app.com.example.samuel.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import app.com.example.samuel.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    private Uri detailUri;

    private static final int DETAIL_FORECAST_LOADER_ID = 0;

    private static final String[] DETAIL_FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES
    };

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_HUMIDITY = 5;
    static final int COL_WEATHER_WIND_SPEED = 6;
    static final int COL_WEATHER_PRESSURE = 7;
    static final int COL_WEATHER_DEGREES = 7;

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private ShareActionProvider mShareActionProvider;
    private String mForecastStr;


    public DetailActivityFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_FORECAST_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.detail_fragment, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        setShareIntent(createShareForecastIntent());
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                        mForecastStr + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }
    
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }else{
            Log.e(LOG_TAG, "Share Action Provider is null?");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();

        if (intent == null)
            return null;


        return new CursorLoader(
                getActivity(),
                intent.getData(),
                DETAIL_FORECAST_COLUMNS,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor.moveToFirst()) {

            long date = cursor.getLong(COL_WEATHER_DATE);
            TextView dateView = (TextView) getView().findViewById(R.id.list_item_date_textview);
            dateView.setText(Utility.getFriendlyDayString(getContext(), date));

            String weatherDesc = cursor.getString(COL_WEATHER_DESC);
            TextView forecastView = (TextView) getView().findViewById(R.id.list_item_forecast_textview);
            forecastView.setText(weatherDesc);

            boolean isMetric = Utility.isMetric(getContext());

            String minTemp = Utility.formatTemperature(getContext(),
                    cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

            TextView minTempView = (TextView) getView().findViewById(R.id.list_item_low_textview);
            minTempView.setText(minTemp);

            String maxTemp = Utility.formatTemperature(getContext(),
                    cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric);

            TextView maxTempView = (TextView) getView().findViewById(R.id.list_item_high_textview);
            maxTempView.setText(maxTemp);

            double humidity = cursor.getDouble(COL_WEATHER_HUMIDITY);
            String humidity_format = getContext().getString(R.string.format_humidity, humidity);

            TextView humidityView = (TextView) getView().findViewById(R.id.list_item_humidity_textview);
            humidityView.setText(humidity_format);

            double pressure = cursor.getDouble(COL_WEATHER_PRESSURE);
            String pressure_format = getContext().getString(R.string.format_pressure, pressure);

            TextView pressureView = (TextView) getView().findViewById(R.id.list_item_pressure_textview);
            pressureView.setText(pressure_format);

            Double wind_khm = cursor.getDouble(COL_WEATHER_WIND_SPEED);
            Double degrees = cursor.getDouble(COL_WEATHER_DEGREES);
            String wind_khm_format = Utility.getFormattedWind(getContext(), wind_khm.floatValue(), degrees.floatValue());

            TextView windView = (TextView) getView().findViewById(R.id.list_item_wind_textview);
            windView.setText(wind_khm_format);

            // Use placeholder image for now
            ImageView iconView = (ImageView) getView().findViewById(R.id.list_item_icon);
            iconView.setImageResource(R.mipmap.ic_launcher);

            if (mShareActionProvider != null)
                mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

}
