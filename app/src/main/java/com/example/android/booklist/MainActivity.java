package com.example.android.booklist;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private NewsAdapter adapter;
    private ArrayList<News> news;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        news = new ArrayList<>();
        adapter = new NewsAdapter(this, news);//
        adapter.clear();
        FetchNewsTask NewsTask = new FetchNewsTask();
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            NewsTask.execute();
            adapter.clear();
        } else {
            Toast.makeText(this,
                    getResources().getString(R.string.no_internet), Toast.LENGTH_LONG).show();
        }
        ListView listView = (ListView) findViewById(R.id.list_news);//
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                News currentNewsItem = adapter.getItem(position);
                String currentNewsUrl = currentNewsItem.getNewsUrl();
                //Toast.makeText(getActivity(),forecast,Toast.LENGTH_SHORT).show();
                //Calling DetailActivity via explicit intent
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse(currentNewsUrl));
                Intent browserChooserIntent = Intent.createChooser(browserIntent,
                        getResources().getString(R.string.browserIntentChoice));
                startActivity(browserChooserIntent);
            }
        });
    }

    public class FetchNewsTask extends AsyncTask<Void, Void, ArrayList<News>> {

        private final String WEB_URL = FetchNewsTask.class.getSimpleName();

        private ArrayList<News> getNewsDataFromJson(String newsJsonStr) throws JSONException {
            final String OWM_RESPONSE = getResources().getString(R.string.response);
            final String OWM_RESULTS = getResources().getString(R.string.results);
            final String OWM_TYPE = getResources().getString(R.string.type);
            final String OWM_WEB_TITLE = getResources().getString(R.string.webTitle);
            final String OWM_PUBLISH_DATE = getResources().getString(R.string.news_date);
            final String OWM_WEB_URL = getResources().getString(R.string.webUrl);
            news = new ArrayList<>();
            JSONObject newsJson = new JSONObject(newsJsonStr);

            JSONObject newsObject = newsJson.getJSONObject(OWM_RESPONSE);
            JSONArray newsArray = newsObject.optJSONArray(OWM_RESULTS);
            for (int i = 0; i < newsArray.length(); i++) {
                String webUrl;
                String webTitle;
                String type;
                String datePublished;
                String publishedOn = getResources().getString(R.string.publishedOn);
                JSONObject newsDataObject = newsArray.getJSONObject(i);
                webTitle = newsDataObject.optString(OWM_WEB_TITLE);
                webUrl = newsDataObject.optString(OWM_WEB_URL);
                type = newsDataObject.optString(OWM_TYPE);
                datePublished = publishedOn + newsDataObject.optString(OWM_PUBLISH_DATE).substring(0, 10);
                news.add(i, new News(type, webTitle, datePublished, webUrl));
            }
            return news;
        }

        @Override
        protected ArrayList<News> doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String newsJsonStr = null;


            try {
                final String fetchNewsUrl = getResources().getString(R.string.url);
                final String appIdParam = getResources().getString(R.string.appId);

                Uri builtUri = Uri.parse(fetchNewsUrl).buildUpon()
                        .appendQueryParameter(appIdParam, "test")
                        .build();
                URL url = new URL(builtUri.toString().replace(" ", "%20"));

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                newsJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.v(WEB_URL, "ERROR", e);
                Log.e(WEB_URL, e.getMessage(), e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(WEB_URL, "Error closing stream", e);
                    }
                }
            }
            try {
                return getNewsDataFromJson(newsJsonStr);
            } catch (JSONException e) {
                Log.e(WEB_URL, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(ArrayList<News> news) {
            if (news != null) {
                adapter.addAll(news);
            }
        }

    }
}
