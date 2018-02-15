package com.example.android.githubsearchwithlifecycle;

import android.content.Intent;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements GitHubSearchAdapter.OnSearchItemClickListener, LoaderManager.LoaderCallbacks<String> {

    private final static String TAG = MainActivity.class.getSimpleName();

    private final static String SEARCH_URL_KEY = "githubSearchURL";

    private final static int GITHUB_SEARCH_LOADER_ID = 0;

    private RecyclerView mSearchResultsRV;
    private EditText mSearchBoxET;
    private GitHubSearchAdapter mGitHubSearchAdapter;
    private ProgressBar mLoadingProgressBar;
    private TextView mLoadingErrorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadingProgressBar = (ProgressBar)findViewById(R.id.pb_loading_indicator);
        mLoadingErrorMessage = (TextView)findViewById(R.id.tv_loading_error);

        mSearchBoxET = (EditText)findViewById(R.id.et_search_box);
        mSearchResultsRV = (RecyclerView)findViewById(R.id.rv_search_results);

        mSearchResultsRV.setLayoutManager(new LinearLayoutManager(this));
        mSearchResultsRV.setHasFixedSize(true);

        mGitHubSearchAdapter = new GitHubSearchAdapter(this);
        mSearchResultsRV.setAdapter(mGitHubSearchAdapter);

        Button searchButton = (Button)findViewById(R.id.btn_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = mSearchBoxET.getText().toString();
                if (!TextUtils.isEmpty(searchQuery)) {
                    doGitHubSearch(searchQuery);
                }
            }
        });

        getSupportLoaderManager().initLoader(GITHUB_SEARCH_LOADER_ID, null, this);
    }

    private void doGitHubSearch(String searchQuery) {
        String githubSearchURL = GitHubUtils.buildGitHubSearchURL(searchQuery);
        Bundle args = new Bundle();
        args.putString(SEARCH_URL_KEY, githubSearchURL);
        getSupportLoaderManager().restartLoader(GITHUB_SEARCH_LOADER_ID, args, this);
    }

    @Override
    public void onSearchItemClick(GitHubUtils.SearchResult searchResult) {
        Intent detailedSearchResultIntent = new Intent(this, SearchResultDetailActivity.class);
        detailedSearchResultIntent.putExtra(GitHubUtils.EXTRA_SEARCH_RESULT, searchResult);
        startActivity(detailedSearchResultIntent);
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            String mSearchResultsJSON;

            @Override
            protected void onStartLoading() {
                if (args != null) {
                    if (mSearchResultsJSON != null) {
                        Log.d(TAG, "loader returning cached results");
                        deliverResult(mSearchResultsJSON);
                    } else {
                        mLoadingProgressBar.setVisibility(View.VISIBLE);
                        forceLoad();
                    }
                }
            }

            @Override
            public String loadInBackground() {
                if (args != null) {
                    String githubSearchURL = args.getString(SEARCH_URL_KEY);

                    Log.d(TAG, "loading results from GitHub with URL: " + githubSearchURL);
                    String searchResults = null;
                    try {
                        searchResults = NetworkUtils.doHTTPGet(githubSearchURL);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return searchResults;
                } else {
                    return null;
                }
            }

            @Override
            public void deliverResult(String data) {
                mSearchResultsJSON = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        Log.d(TAG, "loader finished loading");
        if (data != null) {
            ArrayList<GitHubUtils.SearchResult> searchResults = GitHubUtils.parseSearchResultsJSON(data);
            mGitHubSearchAdapter.updateSearchResults(searchResults);
            mLoadingErrorMessage.setVisibility(View.INVISIBLE);
            mSearchResultsRV.setVisibility(View.VISIBLE);
        } else {
            mSearchResultsRV.setVisibility(View.INVISIBLE);
            mLoadingErrorMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        // Nothing to do...
    }
}
