package com.connyay.domainr;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.ListActivity;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.ListView;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.connyay.domainr.common.DelayedTextWatcher;
import com.connyay.domainr.gson.GsonTransformer;
import com.connyay.domainr.gson.Results;
import com.connyay.domainr.gson.ResultsData;
import com.connyay.domainr.support.ResultsAdapter;



public class Main extends ListActivity {
    ListView mainListView;
    EditText queryBox;
    Button clear;
    ResultsAdapter adapter;
    private AQuery aq;

    // cache for an hour
    long expire = 60 * 60 * 1000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

	mainListView = getListView();

	queryBox = (EditText) findViewById(R.id.queryBox);
	adapter = new ResultsAdapter(this);
	mainListView.setAdapter(adapter);

	clear = (Button) findViewById(R.id.clear);

	queryBox.setOnEditorActionListener(new OnEditorActionListener() {

	    @Override
	    public boolean onEditorAction(android.widget.TextView v,
		    int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE) {
		    clearList();
		    buildResults(queryBox.getText().toString());		    
		}
		return false;
	    }
	});

	clear.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		queryBox.setText("");
		clearList();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(queryBox, 0);
	    }
	});

	queryBox.addTextChangedListener(new DelayedTextWatcher(800) {
	    @Override
	    public void afterTextChangedDelayed(Editable s) {
		if (s.toString().length() > 1) {		    
		    buildResults(s.toString());
		}
	    }
	});

	mainListView.setOnScrollListener(new OnScrollListener() {
	    @Override
	    public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == SCROLL_STATE_FLING) {
		    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		    imm.hideSoftInputFromWindow(mainListView.getWindowToken(),
			    0);
		}
	    }

	    @Override
	    public void onScroll(AbsListView view, int firstVisibleItem,
		    int visibleItemCount, int totalItemCount) {
	    }
	});

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getSupportMenuInflater();
	inflater.inflate(R.menu.main, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.menu_license:
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);

	    // 2. Chain together various setter methods to set the dialog
	    // characteristics
	    builder.setMessage(R.string.Licenses);

	    // 3. Get the AlertDialog from create()
	    AlertDialog dialog = builder.create();
	    dialog.show();
	    return true;
	}

	return super.onOptionsItemSelected(item);
    }

    public void buildResults(String query) {
	aq = new AQuery(this);
	// strip all whitespaces
	query = query.replaceAll("\\s", "");
	String url = "http://domai.nr/api/json/search?client_id=domainr-android&q="
		+ query;
	GsonTransformer t = new GsonTransformer();

	aq.progress(R.id.progress).transformer(t).ajax(url, Results.class, expire,
		new AjaxCallback<Results>() {
		    public void callback(String url, Results results,
			    AjaxStatus status) {
			if (status.getCode() == 200) {
			    // good response
			    updateList(results);
			}
			if (status.getCode() == -101) {
			    // bad response
			    clearList();
			}

		    }
		});
    }

    public void updateList(Results results) {
	// clear list first
	clearList();

	ResultsData[] resultsData = results.getResults();
	for (int i = 0; i < resultsData.length; i++) {
	    adapter.add(resultsData[i]);
	}

    }

    public void clearList() {
	adapter.clear();
	adapter.notifyDataSetChanged();
    }

}
