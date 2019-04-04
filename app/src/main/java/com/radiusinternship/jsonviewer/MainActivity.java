package com.radiusinternship.jsonviewer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    protected JSONFetch jsonFetch; //Object that stores the JSON fetched from the given URL.
    protected ListView listView; //ListView to show Person image, name and age.
    protected HashMap<String,Bitmap> urlImageHashMap = new HashMap<String, Bitmap>(); //To prevent second time download of thumbnails.

    //Method to convert the first character of string to Uppercase.
    public String stringCapitalize(String string){
        return string.substring(0,1).toUpperCase()+string.substring(1);
    }
    //Method to check internet connection
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String jsonUrl = "https://raw.githubusercontent.com/iranjith4/radius-intern-mobile/master/users.json";
        listView = (ListView) findViewById(R.id.listview);

        if(isNetworkAvailable()) {
            //Block to create instance of JSONFetch which fetches JSON file from the internet asynchronously.
            try {
                jsonFetch = new JSONFetch(jsonUrl);
                jsonFetch.execute();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            //Making sure of working internet connection.
            Toast.makeText(getApplicationContext(),"Turn on Internet connection and try again",Toast.LENGTH_LONG).show();
            finish();
        }

    }

    //Class to create custom listview adapter to hold an image, a name and an age.
    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return jsonFetch.jsonImageUrl.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View tview = getLayoutInflater().inflate(R.layout.customlayout,null);
            ImageView thumbNailImageView = (ImageView) tview.findViewById(R.id.thumbNailImageView); //Thumbnail of Person
            TextView textName = (TextView) tview.findViewById(R.id.textName); //Name of Person
            TextView textAge = (TextView) tview.findViewById(R.id.textAge);// Age of Person
            new ImageDownloader(jsonFetch.jsonImageUrl[i],thumbNailImageView).execute(); //Downloads image from the url and set it to the imageView.
            textName.setText(jsonFetch.jsonPersonName[i]); //Sets the persons name.
            textAge.setText("Age : "+Integer.toString(jsonFetch.jsonPersonAge[i])); //Sets the person's age.
            return tview;
        }
    }

    //Class to download image from given url asynchronously and setting it to the given imageView.
    class ImageDownloader extends AsyncTask<Void,Void,Bitmap>{
        private String url;
        private ImageView imageView;

        //Parameterised constructor that gets the imageUrl and imageview.
        public ImageDownloader(String url, ImageView imageView){
            this.url = new String(url);
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            //Block that is responsible for making connections and downloading the image.
            if (!urlImageHashMap.containsKey(url)){
                try {
                    URL urlConnection = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    urlImageHashMap.put(url,myBitmap); //storing the url and downloaded image in hashmap to prevent redownload during recycling of view.
                    return myBitmap;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            else{
                return urlImageHashMap.get(url); //Return the image if already downloaded.
            }
        }

        @Override
        protected void onPostExecute(Bitmap result){
            super.onPostExecute(result);
            imageView.setImageBitmap(result); //sets the imageview with the downloaded image.
        }
    }
    class JSONFetch extends AsyncTask<Void, Void, String> {
        //Arrays to store the values fetched from the json file.
        String jsonImageUrl[];
        String jsonPersonName[];
        int jsonPersonAge[];

        //ProgressDialog to show while fetching the JSON from the internet.
        ProgressDialog progress;
        private String jsonUrl;
        Context context;
        public JSONFetch(String jsonUrl){
            this.jsonUrl = jsonUrl;
        }
        @Override
        protected String doInBackground(Void... params) {
            //Block that fetches the JSONString the given URL.
            try {
                URL url = new URL(this.jsonUrl);
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                String jsonString="";
                String tempString="";
                while (null != (tempString = br.readLine())) {
                    jsonString+=tempString;
                }
                Log.e("test","fetch json successful");
                return jsonString;
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e("test","cannot fetch json");
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            Log.e("test","inside onPreExecute");
            //Setting up the ProgressDialog.
            progress = new ProgressDialog(MainActivity.this);
            progress.setTitle("Please Wait...");
            progress.setMessage("Fetching JSON file from the internet");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.show();
        }

        @Override
        protected void onPostExecute(String result) {
            JSONObject jsonObject = null;
            try {
                //Parsing the JSONString into JSONObject
                jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("results");

                //Allocation of array size based on the JSON array size.
                jsonImageUrl = new String[jsonArray.length()];
                jsonPersonAge = new int[jsonArray.length()];
                jsonPersonName = new String[jsonArray.length()];

                //Extracting ImageUrl,Name and Age from the JSONObject.
                for(int i=0;i<jsonArray.length();i++) {
                    JSONObject tempObject = jsonArray.getJSONObject(i);
                    JSONObject tempNameObject = tempObject.getJSONObject("name");
                    jsonPersonName[i] = stringCapitalize(tempNameObject.getString("title"))
                            + ". " + stringCapitalize(tempNameObject.getString("first"))
                            + " " + stringCapitalize(tempNameObject.getString("last"));
                    jsonPersonAge[i] =  tempObject.getJSONObject("dob").getInt("age");
                    jsonImageUrl[i] = tempObject.getJSONObject("picture").getString("thumbnail");
                }
                //Dismissing the dialog once everything is ready.
                progress.dismiss();

                //Creating an instance of CustomAdapter and setting it to the listView.
                CustomAdapter customAdapter = new CustomAdapter();
                listView.setAdapter(customAdapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
