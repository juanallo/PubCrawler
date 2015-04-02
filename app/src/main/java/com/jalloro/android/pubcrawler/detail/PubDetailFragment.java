package com.jalloro.android.pubcrawler.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jalloro.android.pubcrawler.R;
import com.jalloro.android.pubcrawler.welcome.WelcomeFragment;

public class PubDetailFragment extends Fragment {

    public PubDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pub_detail, container, false);

        TextView pubName = (TextView) rootView.findViewById(R.id.pub_name);
        Intent intent = getActivity().getIntent();

        final String stringExtra = intent.getStringExtra(WelcomeFragment.PUB_ADDRESS);

        pubName.setText(stringExtra);

        return rootView;
    }
}
