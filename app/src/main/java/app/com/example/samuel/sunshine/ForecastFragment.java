package app.com.example.samuel.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    ArrayAdapter<String> mForecastAdapter;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastAdapter =
                new ArrayAdapter<String>(
                    getActivity(),
                    R.layout.list_item_forecast,
                    R.id.list_item_forecast_textview,
                        new ArrayList<String>());

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Object forecastItem = ((ListView)view).getItemAtPosition(position);
                //CharSequence text = ((TextView)view).getText(); // one way, i guess the heavy one.
                CharSequence forecastItem = mForecastAdapter.getItem(position);
//                int duration = Toast.LENGTH_SHORT;
//
//                Toast toast = Toast.makeText(getContext(), forecastItem, duration);
//                toast.show();
                // Executed in an Activity, so 'this' is the Context
                // The fileUrl is a string URL, such as "http://www.example.com/image.png"
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                detailIntent.putExtra(Intent.EXTRA_TEXT, forecastItem);
                startActivity(detailIntent);
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
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        String queryValue =
            sharedPref
                .getString(
                    getString(R.string.pref_location_key),
                    getString(R.string.pref_location_default)
                );

        String unitsValue =
            sharedPref
                .getString(
                    getString(R.string.pref_temperature_units_key),
                    getString(R.string.pref_temperature_units_default)
                );

        new FetchWeatherTask(this.getActivity(), mForecastAdapter).execute(queryValue, unitsValue);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

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

}
