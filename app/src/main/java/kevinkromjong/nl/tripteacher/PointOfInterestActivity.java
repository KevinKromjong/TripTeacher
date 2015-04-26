package kevinkromjong.nl.tripteacher;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.app.ActionBar;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.metaio.sdk.jni.LLACoordinate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;


public class PointOfInterestActivity extends ActionBarActivity implements TabListener, View.OnClickListener {

    private String naam;
    private TabHost tabHost;
    private Button modelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_of_interest);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle("POI");
//        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        naam = getIntent().getExtras().getString("NAME_OF_MONUMENT");
        Log.i("Naam van monument", "Naam van monument: " + naam);

        new GetPOI().execute();

        createTabs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_point_of_interest, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void createTabs(){
        TabHost tabHost = (TabHost) this.findViewById(R.id.tabHost);
        tabHost.setup(); // dont forget to call this line.

        TabSpec spec3 = tabHost.newTabSpec("Model");
        spec3.setContent(R.id.tab3);
        spec3.setIndicator("Model");

        modelButton = (Button) findViewById(R.id.model_button);
        modelButton.setOnClickListener(this);


//        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
//            @Override
//            public void onTabChanged(String tabId) {
//                if(tabId.equals("Model")) {
//                    Intent i = new Intent(PointOfInterestActivity.this, ModelActivity.class);
//                    startActivity(i);
//                }
//            }
//        });

        TabSpec spec1 = tabHost.newTabSpec("Informatie");
        spec1.setContent(R.id.tab1);
        spec1.setIndicator("Informatie");


        TabSpec spec2 = tabHost.newTabSpec("Opdracht");
        spec2.setContent(R.id.tab2);
        spec2.setIndicator("Opdracht");




        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);
    }

    @Override
    public void onTabSelected(android.support.v7.app.ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabUnselected(android.support.v7.app.ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(android.support.v7.app.ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.model_button :
                Intent i = new Intent(PointOfInterestActivity.this, ModelActivity.class);
                startActivity(i);
            break;
        }
    }


    class GetPOI extends AsyncTask<String, JSONObject, JSONObject> {

        private String url = "http://kevinkromjong.nl/pressurecooker/poi-information/id/" + naam;

        private static final String TAG_HEADER_TITLE = "header_title";
        private static final String TAG_HEADER_SUBTITLE = "header_subtitle";
        private static final String TAG_HEADER_IMAGE = "header_image";
        private static final String TAG_ARTICLE_TITLE = "article_title";
        private static final String TAG_ARTICLE_HEADING = "article_heading";
        private static final String TAG_ARTICLE_TEXT = "article_text";


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected JSONObject doInBackground(String... arg0) {
            try {
                JSONParser jParser = new JSONParser();

                JSONObject json = jParser.getJSONFromUrl(url);

                Log.i("Jason", "Jason: " + json + " - " + json.length());

                return json;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;

        }

        protected void onPostExecute(JSONObject result) {
            try {
                String url = result.getString(TAG_HEADER_IMAGE);
                new DownloadImageTask((ImageView) findViewById(R.id.header_image)).execute(url);

                String header_title = result.getString(TAG_HEADER_TITLE);
                String header_subtitle = result.getString(TAG_HEADER_SUBTITLE);
                String article_title = result.getString(TAG_ARTICLE_TITLE);
                String article_heading = result.getString(TAG_ARTICLE_HEADING);
                String article_text = result.getString(TAG_ARTICLE_TEXT);


                TextView a = (TextView) findViewById(R.id.header_title);
                a.setText(header_title);
                TextView b = (TextView) findViewById(R.id.header_subtitle);
                b.setText(header_subtitle);
                TextView c = (TextView) findViewById(R.id.article_title);
                c.setText(article_title);
                TextView d = (TextView) findViewById(R.id.article_heading);
                d.setText(article_heading);
                TextView e = (TextView) findViewById(R.id.article_text);
                e.setText(article_text);



            } catch (JSONException j) {
                Log.e("JException", "JException" + j.getMessage());
            }
        }



        private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
            ImageView bmImage;

            public DownloadImageTask(ImageView bmImage) {
                this.bmImage = bmImage;
            }

            protected Bitmap doInBackground(String... urls) {
                String urldisplay = urls[0];
                Bitmap mIcon11 = null;
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
                return mIcon11;
            }

            protected void onPostExecute(Bitmap result) {
                bmImage.setImageBitmap(result);
            }
        }

    }
}