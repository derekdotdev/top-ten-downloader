package com.example.toptendownloaded;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String RSS_FEED = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml";
    private ListView listApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listApps = (ListView) findViewById(R.id.xmlListView);

        Log.d(TAG, "onCreate: starting AsyncTask");
        DownloadData downloadData = new DownloadData();
        downloadData.execute(RSS_FEED);
        Log.d(TAG, "onCreate: done");
    }


    // Create inner class (AsyncTask is deprecated)
    // <String=URL to RSS feed, Void=Info for ProgressBar, String=ResultReturnType
    private class DownloadData extends AsyncTask<String, Void, String> {
        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: parameter is " + s);

            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);

            // Context, Where in layout to put data, Where in app to find data
//            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<>(
//                    MainActivity.this, R.layout.list_item, parseApplications.getApplications());
//            listApps.setAdapter(arrayAdapter);

            // Custom FeedAdapter
            FeedAdapter feedAdapter = new FeedAdapter(MainActivity.this, R.layout.list_record, parseApplications.getApplications());
            listApps.setAdapter(feedAdapter);
        }


        /// ... = variable-length argument list (introduced in Java 5)
        // But instead of passing multiple URLs to one background thread,
        // it would be better to spin up multiple async tasks simultaneously
        // (one for each URL)
        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: starts with " + strings[0]);
            String rssFeed = downloadXML(strings[0]);
            if(rssFeed == null) {
                // Error log generated with loge because logd (debug logs)
                // don't appear in logcat in production apps
                Log.e(TAG, "doInBackground: Error downloading");
            }
            return rssFeed;
        }


        private String downloadXML(String urlPath) {
            StringBuilder xmlResult = new StringBuilder();

            try {
                URL url = new URL(urlPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int response = connection.getResponseCode();
                Log.d(TAG, "downloadXML: The response code was "  + response);
//                InputStream inputStream = connection.getInputStream();
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//                BufferedReader reader = new BufferedReader(inputStreamReader);

                // Replace previous three lines with chained methods below
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));

                // Create character array to hold data from BufferedReader
                // 500 chars is arbitrary and can be adjusted to improve performance
                // Android internal BufferedReader = 8192 bytes = 8kB
                int charsRead;
                char[] inputBuffer = new char[500];
                while(true) {
                    charsRead = reader.read(inputBuffer);
                    if(charsRead < 0) {
                        break;
                    }
                    if(charsRead > 0) {
                        xmlResult.append(String.copyValueOf(inputBuffer, 0, charsRead));
                    }
                }
                // Closing the BR takes care of closing ISR and IS.
                reader.close();
                return xmlResult.toString();
            } catch (MalformedURLException e) {
                Log.e(TAG, "downloadXML: Invalid URL" + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "downloadXML: IOException " + e.getMessage());
                e.printStackTrace();
            } catch (SecurityException e) {
                Log.e(TAG, "downloadXML: Security Exception. Needs permission? " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }
}