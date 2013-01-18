package com.connyay.domainr;

import org.holoeverywhere.app.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import org.holoeverywhere.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class MoreInfoFragment extends Fragment implements OnClickListener {
    Button wiki, iana, viewSite, whois, share;

    public static Fragment newInstance() {
	MoreInfoFragment myFragment = new MoreInfoFragment();
	return myFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
	View v = inflater.inflate(R.layout.more_info, container, false);
	wiki = (Button) v.findViewById(R.id.wiki);
	wiki.setOnClickListener(this);
	iana = (Button) v.findViewById(R.id.iana);
	iana.setOnClickListener(this);
	whois = (Button) v.findViewById(R.id.whois);
	whois.setOnClickListener(this);
	viewSite = (Button) v.findViewById(R.id.viewSite);
	viewSite.setOnClickListener(this);
	return v;
    }

    @Override
    public void onClick(View v) {
	String uri = "";
	if (v == wiki) {
	    uri = getArguments().getString("wiki");
	} else if (v == iana) {
	    uri = getArguments().getString("iana");
	} else if (v == viewSite) {
	    uri = getArguments().getString("www");
	} else if (v == whois) {
	    uri = getArguments().getString("whois");
	}
	if (uri != null) {
	    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
		    Uri.parse(uri));
	    startActivity(browserIntent);
	}

    }
}
