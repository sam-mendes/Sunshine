package app.com.example.samuel.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        String message = intent.getStringExtra(Intent.EXTRA_TEXT);

//        int duration = Toast.LENGTH_SHORT;
//
//        Toast toast = Toast.makeText(getContext(), message, duration);
//        toast.show();

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        TextView textView = (TextView)rootView.findViewById(R.id.list_item_forecast_textview);
        textView.setText(message);

        return rootView;
    }



}
