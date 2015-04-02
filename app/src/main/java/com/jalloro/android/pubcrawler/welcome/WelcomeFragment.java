package com.jalloro.android.pubcrawler.welcome;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jalloro.android.pubcrawler.R;

public class WelcomeFragment extends Fragment {

    private static final String CHECKED_IN = "CHECKED_IN";
    private boolean checkedIn;

    public WelcomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_welcome_checkin, container, false);

        checkedIn = savedInstanceState != null && Boolean.parseBoolean(savedInstanceState.get(CHECKED_IN).toString());
        updateChecked(rootView, checkedIn);

        final ImageButton checkInButton = (ImageButton)rootView.findViewById(R.id.checkinButton);

        checkInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkedIn){
                    Toast toast = Toast.makeText(rootView.getContext(), "congrats, your first checkin!", Toast.LENGTH_SHORT);
                    toast.show();

                    checkedIn = true;
                    updateChecked(rootView, true);
                }
            }
        });

        return rootView;
    }

    private void updateChecked(@NonNull final View rootView, final boolean checkedIn) {
        TextView checkInText = (TextView)rootView.findViewById(R.id.checkInText);
        final ImageButton checkInButton = (ImageButton)rootView.findViewById(R.id.checkinButton);
        if(checkedIn){
            checkInText.setText(R.string.checked_in);
            checkInButton.setBackground(rootView.getResources().getDrawable(R.drawable.checked_in_button));
        }
        else {
            checkInText.setText(R.string.checkIn);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(CHECKED_IN,Boolean.toString(checkedIn));
        super.onSaveInstanceState(outState);
    }


}
