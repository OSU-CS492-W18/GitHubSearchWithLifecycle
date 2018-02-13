package com.example.android.githubsearchwithlifecycle;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by hessro on 1/30/18.
 */

public class GitHubUtils {
    public static final String EXTRA_SEARCH_RESULT = "GitHubUtils.SearchResult";

    final static String GITHUB_SEARCH_BASE_URL = "https://api.github.com/search/repositories";
    final static String GITHUB_SEARCH_QUERY_PARAM = "q";
    final static String GITHUB_SEARCH_SORT_PARAM = "sort";
    final static String GITHUB_SEARCH_SORT_VALUE = "stars";

    public static class SearchResult implements Serializable {
        public String fullName;
        public String description;
        public String htmlURL;
        public int stars;
    }

    public static String buildGitHubSearchURL(String searchQuery) {
        return Uri.parse(GITHUB_SEARCH_BASE_URL).buildUpon()
                .appendQueryParameter(GITHUB_SEARCH_QUERY_PARAM, searchQuery)
                .appendQueryParameter(GITHUB_SEARCH_SORT_PARAM, GITHUB_SEARCH_SORT_VALUE)
                .build()
                .toString();
    }

    public static ArrayList<SearchResult> parseSearchResultsJSON(String searchResultsJSON) {
        try {
            JSONObject searchResultsObj = new JSONObject(searchResultsJSON);
            JSONArray searchResultsItems = searchResultsObj.getJSONArray("items");

            ArrayList<SearchResult> searchResultsList = new ArrayList<SearchResult>();
            for (int i = 0; i < searchResultsItems.length(); i++) {
                SearchResult result = new SearchResult();
                JSONObject resultItem = searchResultsItems.getJSONObject(i);
                result.fullName = resultItem.getString("full_name");
                result.description = resultItem.getString("description");
                result.htmlURL = resultItem.getString("html_url");
                result.stars = resultItem.getInt("stargazers_count");
                searchResultsList.add(result);
            }
            return searchResultsList;
        } catch (JSONException e) {
            return null;
        }
    }
}
