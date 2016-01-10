package app.com.example.samuel.sunshine;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import app.com.example.samuel.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ForecastAdapter mForecastAdapter;

    private static final int FORECAST_LOADER_ID = 0;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    public ForecastFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Object forecastItem = ((ListView)view).getItemAtPosition(position);
                //CharSequence text = ((TextView)view).getText(); // one way, i guess the heavy one.
//                CharSequence forecastItem = mForecastAdapter.getItem(position);
//                int duration = Toast.LENGTH_SHORT;
//
//                Toast toast = Toast.makeText(getContext(), forecastItem, duration);
//                toast.show();
                // Executed in an Activity, so 'this' is the Context
                // The fileUrl is a string URL, such as "http://www.example.com/image.png"

//                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
//                detailIntent.putExtra(Intent.EXTRA_TEXT, forecastItem);
//                startActivity(detailIntent);
                Cursor cursor = (Cursor) mForecastAdapter.getItem(position);

                if (cursor != null) {
                    String location = Utility.getPreferredLocation(getContext());
                    Uri weatherWithLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, cursor.getLong(COL_WEATHER_DATE));

                    Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                    detailIntent.setData(weatherWithLocationUri);
                    startActivity(detailIntent);
                }


            }
        });
        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_refresh){
            updateWeather();

            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    private void updateWeather() {
        String location = Utility.getPreferredLocation(getContext());

        new FetchWeatherTask(this.getContext()).execute(location);
    }

    //
    //  Commented to prevent download data from Open Weather Map every time the user rotates the device.
    //  In order to update data from OWM click on the button Refresh on the main activity.


//    @Override
//    public void onStart() {
//        super.onStart();
//        updateWeather();
//    }

    public void myClickHandler(View view) {

        ConnectivityManager connMgr = (ConnectivityManager)
                this.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.i("Internet Connection","Connected!!!! =)");
        } else {
            Log.i("Internet Connection", "Not Connected! ;(");
        }

    }

    void onLocationChanged(){
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == FORECAST_LOADER_ID) {

            String locationSetting = Utility.getPreferredLocation(getActivity());

            // Sort order:  Ascending, by date.
            String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
            Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                    locationSetting, System.currentTimeMillis());

            return new CursorLoader(getContext(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mForecastAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }
}
