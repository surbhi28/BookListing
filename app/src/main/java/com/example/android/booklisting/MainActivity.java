package com.example.android.booklisting;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = MainActivity.class.getName();

    private static final String BASE_URL = "https://www.googleapis.com/books/v1/volumes?maxResults=7&q=";

    /**
     * Adapter for the list of Books
     */
    private BookAdapter mAdapter;

    /**
     * Empty Text View
     */
    private TextView mEmptyStateTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        // Find a reference to the {@link ListView} in the layout
        ListView bookListView = (ListView) findViewById(R.id.list);

        // Create a new adapter that takes an empty list of books as input
        mAdapter = new BookAdapter(this, new ArrayList<Book>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        bookListView.setAdapter(mAdapter);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        bookListView.setEmptyView(mEmptyStateTextView);

        final EditText searchText = (EditText) findViewById(R.id.search_view);
        Button search = (Button) findViewById(R.id.search_button);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting()) {
                    String query = searchText.getText().toString().replaceAll(" ", "+");
                    String BOOK_URL = BASE_URL + query;

                    // Kick off an {@link AsyncTask} to perform the network request
                    BookAsyncTask task = new BookAsyncTask();
                    task.execute(BOOK_URL);
                } else {
                    mEmptyStateTextView.setText("No Internet Connection");

                }

            }
        });

    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the query books in the response.
     */
    private class BookAsyncTask extends AsyncTask<String, Void, List<Book>> {

        @Override
        protected List<Book> doInBackground(String... urls) {
            // Create URL object
            URL url = createUrl(urls[0]);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                // TODO Handle the IOException
            }

            // Extract relevant fields from the JSON response and create an {@link Book} object
            List<Book> books = extractBookFromJson(jsonResponse);

            // Return the {@link Book} object as the result for the {@link BookAsyncTask}
            return books;
        }

        /**
         * Update the screen with the given book (which was the result of the
         * {@link }).
         */
        @Override
        protected void onPostExecute(List<Book> data) {
            // Clear the adapter of previous book data
            mAdapter.clear();

            // If there is a valid list of {@link Book}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (data != null && !data.isEmpty()) {
                mAdapter.addAll(data);
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linear_layout);
                linearLayout.setVisibility(View.GONE);
            } else {
                mEmptyStateTextView.setText("No Books Available");
            }
        }


        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            if (url == null) {
                return jsonResponse;
            }
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(30000 /* milliseconds */);
                urlConnection.setConnectTimeout(45000 /* milliseconds */);
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e(LOG_TAG, "Error Response Code :" + urlConnection.getResponseCode());
                }

            } catch (IOException e) {
                // TODO: Handle the exception
                Log.e(LOG_TAG, "Problem Retrieving JSON results", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }


        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        /**
         * Return an {@link Book} object by parsing out information
         * about the query book from the input bookJSON string.
         */
        private List<Book> extractBookFromJson(String bookJSON) {
            if (bookJSON.isEmpty()) {
                return null;
            }

            // Create an empty ArrayList that we can start adding books to
            List<Book> books = new ArrayList<>();
            try {
                JSONObject rootObject = new JSONObject(bookJSON);
                JSONArray items = rootObject.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject object = items.getJSONObject(i);
                    JSONObject details = object.getJSONObject("volumeInfo");
                    String title = details.getString("title");
                    String authors = "";
                    JSONArray authorsArray;
                    if (details.has("authors")) {
                        authorsArray = details.getJSONArray("authors");
                        for (int j = 0; j < authorsArray.length(); j++) {
                            authors += "," + authorsArray.getString(j);
                        }
                    } else {
                        authors = "No author";
                    }
                    // Create a new {@link Book} object
                    Book book = new Book(title, authors);
                    books.add(book);
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the earthquake JSON results", e);
            }
            return books;
        }
    }
}

