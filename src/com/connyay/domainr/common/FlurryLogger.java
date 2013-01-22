package com.connyay.domainr.common;

import java.util.HashMap;
import java.util.Map;

import com.flurry.android.FlurryAgent;

public class FlurryLogger {

    public static void logSearch(String query) {

	Map<String, String> params = new HashMap<String, String>();
	params.put("SearchQuery", query);
	FlurryAgent.logEvent("Search", params);

    }

    public static void logSearchTap(String domain) {

	Map<String, String> params = new HashMap<String, String>();
	params.put("SearchTap", domain);
	FlurryAgent.logEvent("SearchTap", params);

    }

    public static void logDomainrShare() {

	FlurryAgent.logEvent("DomainrShare");

    }

    public static void logDomainShare(String domain) {
	Map<String, String> params = new HashMap<String, String>();
	params.put("SearchTap", domain);
	FlurryAgent.logEvent("DomainShare", params);

    }

    public static void logUncaught(String ex) {
	Map<String, String> params = new HashMap<String, String>();
	params.put("Throwable", ex);
	FlurryAgent.logEvent("Uncaught", params);

    }
}
