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
                Log.v("BookFragment", "FetchBookData :" + FetchBookData);
                FetchBooksTask BooksTask = new FetchBooksTask();
                ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    BooksTask.execute(FetchBookData);
                    Log.v("setting up bookadapter", "books");
                    /////adapter = new BookAdapter(getActivity(), books);
                    Log.v("setting up screen", "books");
                    /////ListView listView = (ListView) rootView.findViewById(R.id.list_books);
                    /////listView.setAdapter(adapter);
                    adapter.clear();//
                    adapter.notifyDataSetChanged();//
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "No internet connection availbale", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Log.v("setting up bookadapter", "books");
        ListView listView = (ListView) rootView.findViewById(R.id.list_books);//
        listView.setAdapter(adapter);//
        return rootView;
    }

    public class FetchBooksTask extends AsyncTask<String, Void, ArrayList<Book>> {

        private final String LOG_TAG = FetchBooksTask.class.getSimpleName();

        private ArrayList<Book> getBooksDataFromJson(String booksJsonStr) throws JSONException {
            final String OWM_ITEMS = "items";
            final String OWM_VOLUME_INFO = "volumeInfo";
            final String OWM_TITLE = "title";
            final String OWM_AUTHOR = "authors";
            final String OWM_TOTAL_ITEMS = "totalItems";

            JSONObject booksJson = new JSONObject(booksJsonStr);

            String totalItems = booksJson.optString(OWM_TOTAL_ITEMS);
            Log.v("AyncTask total items", totalItems);
            //String[] resultStrs = new String[booksArray.length()];
            int totalCount = Integer.parseInt(totalItems);
            Log.v("AyncTask total count", String.valueOf(totalCount));
            if (totalCount == 0) {
                Log.v("AsyncTask", "If condition");
                books.clear();
                books.add(totalCount, new Book("No books found", "Please enter valid book name"));
            } else {
                Log.v("AsyncTask", "else condition");
                JSONArray booksArray = booksJson.optJSONArray(OWM_ITEMS);
                for (int i = 0; i < booksArray.length(); i++) {
                    String bookAuthor = "Written By ";
                    String bookName;
                    JSONObject bookDataObject = booksArray.getJSONObject(i);
                    JSONObject volumeInfo = bookDataObject.getJSONObject(OWM_VOLUME_INFO);
                    bookName = volumeInfo.optString(OWM_TITLE);
                    bookAuthor += String.valueOf(volumeInfo.optJSONArray(OWM_AUTHOR));
                    /*JSONArray authorsArray = volumeInfo.optJSONArray(OWM_AUTHOR);
                    int authorsArrayLenghth = authorsArray.length();
                    for (int j = 0; j < authorsArrayLenghth; j++) {
                        ///if (j == (authorsArray.length() - 1)) {
                            //bookAuthor += authorsArray.getJSONObject(j).toString() + ". ";
                            bookAuthor +=  authorsArray.getString(j) + ". ";
                            ///Log.v("AuthorsArray if", bookAuthor);
                        ///} else {
                            //bookAuthor += authorsArray.getJSONObject(j).toString() + ", ";
                            ///bookAuthor += authorsArray.getString(j) + ", ";
                            ///Log.v("AuthorsArray else", bookAuthor);
                        ///}
                    }*/
                    books.add(i, new Book(bookName, bookAuthor));
                    //resultStrs[i] = bookName + "\b" + bookAuthor;
                }
            }
            return books;
        }

        @Override
        protected ArrayList<Book> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }
            Log.v("DoinBackground", params[0]);
            Log.v("DoinBackground", "Siva");
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String booksJsonStr = null;
            int numBooks = 15;

            try {
                final String FETCH_BOOKS_URL = "https://www.googleapis.com/books/v1/volumes?q=intitle:" + params[0];
                //final String QUERY_PARAM = "q";
                //final String TITLE_PARAM = "intitle";
                final String APPID_PARAM = "APPID";
                final String MAX_RESULTS_PARAM = "maxResults";

                Uri builtUri = Uri.parse(FETCH_BOOKS_URL).buildUpon()
                        //.appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(MAX_RESULTS_PARAM, String.valueOf(numBooks))
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_GOOGLE_BOOKS_API_KEY)
                        .build();
                URL url = new URL(builtUri.toString());

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
                Log.v("getBooksDataFromJson2", booksJsonStr);
                return getBooksDataFromJson(booksJsonStr);
            } catch (JSONException e) {
                Log.v("Line 184", "JSONEception");
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Book> books) {
            Log.v("onPostExecute2", books.get(0).toString());
            if (books != null) {
                //adapter.clear();
                for (int p = 0; p < books.size(); p++) {
                    Log.v("postonexecute of books", books.get(p).toString());
                }
                adapter.addAll(books);
            }
        }

    }
}
