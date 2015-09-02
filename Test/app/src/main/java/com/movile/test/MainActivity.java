package com.movile.test;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.movile.test.model.Episode;
import com.nineoldandroids.view.ViewHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ObservableScrollViewCallbacks {
    View mImageView;
    View mToolbarView;
    View mListBackgroundView;
    View mHeaderLayout;
    ObservableListView mListView;
    int mParallaxImageHeight;
    TextView mRating_textView;
    Context mContext;

    public  List<Episode> episodesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;


        initUIControls();

        mParallaxImageHeight = getResources().getDimensionPixelSize(R.dimen.parallax_image_height);
        mListView.setScrollViewCallbacks(this);
        View paddingView = new View(this);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, mParallaxImageHeight);
        paddingView.setLayoutParams(lp);
        paddingView.setClickable(true);
        mListView.addHeaderView(paddingView);
        mListBackgroundView = findViewById(R.id.list_background);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //window.setStatusBarColor(Color.CYAN);
        }

        attemptConnection();


    }

    private void attemptConnection () {
        if (!Helper.hasInternetAccess(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.internet_message)
                    .setTitle(R.string.internet_title)
                    .setPositiveButton(R.string.internet_settings_button, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    WifiManager wifi;
                                    wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
                                    wifi.setWifiEnabled(true);

                                    final ProgressDialog progressDialog;
                                    progressDialog = new ProgressDialog(MainActivity.this);
                                    progressDialog.setMessage(getString(R.string.internet_turning_wifiOn));
                                    progressDialog.setIndeterminate(false);
                                    progressDialog.setCancelable(false);
                                    progressDialog.getWindow().setGravity(Gravity.CENTER);
                                    progressDialog.show();

                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            attemptConnection();
                                            if(progressDialog.isShowing())
                                                progressDialog.dismiss();
                                        }
                                    }, 4000);
                                }
                            }
                    )
                    .setNegativeButton(R.string.internet_cancel_button,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    MainActivity.this.finish();
                                }
                            }
                    );
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            new GetRatingsAsync().execute();
        }
    }

    private void initUIControls() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(R.string.season8);
        mHeaderLayout = findViewById(R.id.header_layout);
        mImageView = findViewById(R.id.image);
        mToolbarView = findViewById(R.id.toolbar);
        mToolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(0, getResources().getColor(R.color.primary)));
        mListView = (ObservableListView) findViewById(R.id.list);
        mRating_textView = (TextView) findViewById(R.id.header_rating_textView);
    }

    public void setAdapterList() {
        EpisodeAdapter listAdapter = new EpisodeAdapter(this, episodesList);
        mListView.setAdapter(listAdapter);
    }

    //region listView methods implementations
    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        int baseColor = getResources().getColor(R.color.primary);
        float alpha = Math.min(1, (float) scrollY / mParallaxImageHeight);
        mToolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(alpha, baseColor));
        ViewHelper.setTranslationY(mHeaderLayout, -scrollY / 2);
        // Translate list background
        ViewHelper.setTranslationY(mListBackgroundView, Math.max(0, -scrollY + mParallaxImageHeight));
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        ActionBar ab = getSupportActionBar();
        if (scrollState == ScrollState.UP) {
            if (ab.isShowing()) {
                ab.hide();
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (!ab.isShowing()) {
                ab.show();
            }
        }
    }
    //endregion


    public class GetSeason8EpisodesAsync extends AsyncTask<String, String, JSONArray> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage(getString(R.string.getting_episodes_list));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setGravity(Gravity.CENTER);
            progressDialog.show();
        }

        @Override
        protected JSONArray doInBackground(String... params) {
            return ApiClient.getEpisodes();
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            super.onPostExecute(jsonArray);
            if(progressDialog.isShowing())
                progressDialog.dismiss();

            if (jsonArray.length() != 0 && jsonArray != null) {
                try {
                    jsonToList(jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setAdapterList();
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                alertDialog.setMessage(getResources().getString(R.string.error_getting_data));
                alertDialog.setPositiveButton(R.string.internet_settings_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        attemptConnection();
                    }
                    });
                alertDialog.show();
            }
        }
    }

    public class GetRatingsAsync extends AsyncTask<String, String, String> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage(getString(R.string.getting_episodes_list));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setGravity(Gravity.CENTER);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            return ApiClient.getRating();
        }

        @Override
        protected void onPostExecute(String rating) {
            super.onPostExecute(rating);
            if(progressDialog.isShowing())
                progressDialog.dismiss();

            if (rating != null)
                mRating_textView.setText(rating);
            else
                mRating_textView.setText("N/A");

            new GetSeason8EpisodesAsync().execute();
            //jsonToList(jsonObject);
            //setAdapterList();

        }
    }
    private void jsonToList(JSONArray jsonArray) throws JSONException {
        if (jsonArray != null) {
            for(int position = 0; position < jsonArray.length(); position++) {
                Episode episode = new Episode();
                episode.setNumber((Integer) jsonArray.getJSONObject(position).get("number"));
                episode.setTitle(jsonArray.getJSONObject(position).get("title").toString());
                episode.setSeason((Integer) jsonArray.getJSONObject(position).get("season"));

                episodesList.add(episode);
            }
        }
    }
}
