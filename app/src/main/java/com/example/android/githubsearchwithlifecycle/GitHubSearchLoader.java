package com.example.android.githubsearchwithlifecycle;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.io.IOException;

/**
 * Created by hessro on 2/20/18.
 */

public class GitHubSearchLoader extends AsyncTaskLoader<String> {
    private final static String TAG = GitHubSearchLoader.class.getSimpleName();

    String mSearchResultsJSON;
    String mGitHubSearchURL;

    GitHubSearchLoader(Context context, String url) {
        super(context);
        mGitHubSearchURL = url;
    }

    @Override
    protected void onStartLoading() {
        if (mGitHubSearchURL != null) {
            if (mSearchResultsJSON != null) {
                Log.d(TAG, "loader returning cached results");
                deliverResult(mSearchResultsJSON);
            } else {
                forceLoad();
            }
        }
    }

    @Override
    public String loadInBackground() {
        if (mGitHubSearchURL != null) {
            Log.d(TAG, "loading results from GitHub with URL: " + mGitHubSearchURL);
            String searchResults = null;
            try {
                searchResults = NetworkUtils.doHTTPGet(mGitHubSearchURL);
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
}
