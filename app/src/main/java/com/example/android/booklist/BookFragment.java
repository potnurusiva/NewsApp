package com.example.android.booklist;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 **/
public class BookFragment extends Fragment {

    private BookAdapter adapter;
    private ArrayList<Book> books;

    public BookFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        books = new ArrayList<>();
        adapter = new BookAdapter(getActivity(), books);//
        adapter.clear();
        final View rootView = inflater.inflate(R.layout.fragment_book, container, false);
        // Inflate the layout for this fragment
        Button button = (Button) rootView.findViewById(R.id.seacrh_books);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText bookNameText = (EditText) getActivity().findViewById(R.id.enter_book_name);
                String FetchBookData = bookNameText.getText().toString();
                FetchBooksTask BooksTask = new FetchBooksTask();
                ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    BooksTask.execute(FetchBookData);
                    adapter.clear();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });
        ListView listView = (ListView) rootView.findViewById(R.id.list_books);//
        listView.setAdapter(adapter);//
        return rootView;
    }

    public class FetchBooksTask extends AsyncTask<String, Void, ArrayList<Book>> {

        private final String LOG_TAG = FetchBooksTask.class.getSimpleName();

        private ArrayList<Book> getBooksDataFromJson(String booksJsonStr) throws JSONException {
            final String OWM_ITEMS = getResources().getString(R.string.items);
            final String OWM_VOLUME_INFO = getResources().getString(R.string.volumeInfo);
            final String OWM_TITLE = getResources().getString(R.string.title);
            final String OWM_AUTHOR = getResources().getString(R.string.authors);
            final String OWM_TOTAL_ITEMS = getResources().getString(R.string.totalItems);
            books = new ArrayList<>();
            JSONObject booksJson = new JSONObject(booksJsonStr);

            String totalItems = booksJson.optString(OWM_TOTAL_ITEMS);
            int totalCount = Integer.parseInt(totalItems);
            if (totalCount == 0) {
                books.add(totalCount, new Book(getResources().getString(R.string.no_books),
                                               getResources().getString(R.string.message)));
            } else {
                JSONArray booksArray = booksJson.optJSONArray(OWM_ITEMS);
                for (int i = 0; i < booksArray.length(); i++) {
                    String bookAuthor = getResources().getString(R.string.written_by);
                    String bookName;
                    JSONObject bookDataObject = booksArray.getJSONObject(i);
                    JSONObject volumeInfo = bookDataObject.getJSONObject(OWM_VOLUME_INFO);
                    bookName = volumeInfo.optString(OWM_TITLE);
                    bookAuthor += String.valueOf(volumeInfo.optJSONArray(OWM_AUTHOR));
                    books.add(i, new Book(bookName, bookAuthor));
                }
            }
            return books;
        }

        @Override
        protected ArrayList<Book> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String booksJsonStr = null;
            int numBooks = 15;

            try {
                final String FETCH_BOOKS_URL = getResources().getString(R.string.url) + params[0];
                final String APPID_PARAM = getResources().getString(R.string.appId);
                final String MAX_RESULTS_PARAM = getResources().getString(R.string.maxResults);

                Uri builtUri = Uri.parse(FETCH_BOOKS_URL).buildUpon()
                        .appendQueryParameter(MAX_RESULTS_PARAM, String.valueOf(numBooks))
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_GOOGLE_BOOKS_API_KEY)
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
                booksJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.v(LOG_TAG, "ERROR", e);
                Log.e(LOG_TAG, e.getMessage(), e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return getBooksDataFromJson(booksJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Book> books) {
            if (books != null) {
                adapter.addAll(books);
            }
        }

    }
}
