package com.example.ave.torrk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static MainActivity THIS;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private MyAdapter adapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean has_Swipe_Refresh = false;

    private ProgressBar mProgressBar;

    private static final String SITE = "http://www.torrentz.com";
    private static final String CATEGORY_TV = "/verified?f=tv";
    private static String new_Page_Address;
    private static int currentSchemaTotalPageCount = 0;
    private static HashMap<String, Integer> schemaPageCountOfCategory = null;
    private static HashMap<String, Integer> currentPageDisplayedOfCategory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        THIS = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

            }
        });

        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.refresh_recycler_view);
        if(mRecyclerView != null){
            if(mProgressBar != null){
                if(getData() != null && !(getData().size() > 0)){
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }
            mRecyclerView.setHasFixedSize(true);
            //mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.addItemDecoration(new MyAdapter.SimpleDividerItemDecoration(this));
            adapter = new MyAdapter(mRecyclerView, getData());
            mRecyclerView.setAdapter(adapter);
            adapter.setOnClickListener(new MyAdapter.MyClickListener() {
                @Override
                public void onItemClick(View v, int position) {
                    TextView view = (TextView) v.findViewById(R.id.name);
                    if (view != null) {
                        String linkHref = view.getTag().toString();
                        if (linkHref != null) {
                            if (!TextUtils.isEmpty(linkHref)) {
                                THIS.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(SITE + linkHref)));
                            }
                        }

                        if (fab != null) {
                            Snackbar.make(fab, "position " + position + " is clicked, Tag " + view.getTag().toString(), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }
                }
            });
            if(mSwipeRefreshLayout != null){
                mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if(!has_Swipe_Refresh) {
                            has_Swipe_Refresh = true;
                            refreshRecyclerView();
                        }
                    }
                });
            }
            adapter.setOnLoadMoreDataListener(new MyAdapter.onLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    if (!addContentToRecyclerView()) {
                        if (adapter != null) {
                            adapter.setLoaded(false);
                        }
                    }
                }
            });
        }
        new NetworkTask().execute();
    }

    private void refreshRecyclerView() {
        new NetworkTask().execute();
    }

    public boolean addContentToRecyclerView() {
        if(schemaPageCountOfCategory != null) {
            if(schemaPageCountOfCategory.get(CATEGORY_TV) != null) {
                if (schemaPageCountOfCategory.get(CATEGORY_TV) > 0) {
                    if (currentPageDisplayedOfCategory != null) {
                        if (currentPageDisplayedOfCategory.get(CATEGORY_TV) != null) {
                            if (new_Page_Address != null) {
                                if (!TextUtils.isEmpty(new_Page_Address)) {
                                    String address = new_Page_Address.substring(0, new_Page_Address.lastIndexOf("=") + 1);
                                    Log.i("MAINActivity", "addContentToRecyclerView(): address: " + address);
                                    int pageNoToLoad = currentPageDisplayedOfCategory.get(CATEGORY_TV) + 1;
                                    if (pageNoToLoad <= (schemaPageCountOfCategory.get(CATEGORY_TV) - 1)) {
                                        new GetContentTask().execute(SITE + address + String.valueOf(pageNoToLoad));
                                        return true;
                                    } else {
                                        return false;
                                    }
                                } else {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private ArrayList<String> listData;
    private ArrayList<String> getData() {
        if(listData == null)
            listData = new ArrayList<>();
        return listData;
    }

    private ArrayList<String> updateData(ArrayList<String> data, boolean clearOldData) {
        if(data != null){
            if(listData == null) {
                listData = new ArrayList<>();
            } else {
                if(clearOldData)
                    clearData();
            }

            listData.addAll(data);
            if(adapter != null){
                adapter.notifyDataSetChanged();
            }
        }
        return listData;
    }

    private boolean clearData() {
        if(listData != null){
            listData.clear();
            return true;
        } else {
            return false;
        }
    }

    private static String generateRandomString(){
        try {
            char[] alphabets = "abcdefghijklmnopqrstuvwxyz".toCharArray();
            StringBuilder sb = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 5; i++) {
                char c = alphabets[random.nextInt(alphabets.length)];
                sb.append(c);
            }
            return sb.toString();
        } catch (Exception e){
            return  null;
        }
    }

    public class NetworkTask extends AsyncTask<Void, Void, Boolean>{

        private ProgressDialog mProgressDialog;
        private String Url;

        private ArrayList<String> content_Names;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i("MAINActivity", "NetworkTask(): onPreExecute() ");
            /*if(THIS != null) {
                mProgressDialog = new ProgressDialog(THIS, R.style.MyProgressDialogTheme);
                mProgressDialog.setMessage("Wait");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }*/
            content_Names = new ArrayList<>();
            Url = SITE + CATEGORY_TV;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                Log.i("MAINActivity", "NetworkTask(): doInBackground() ");
                Document webPage_Content = HttpCalls.jsoupGetContent(HttpCalls.jsoupConnect(Url));
                if(webPage_Content != null){
                    Element content = webPage_Content.body();
                    Element results = content.select("div.results").first();
                    Elements focusContent = results.getElementsByTag("dl");

                    for (Element lines:
                            focusContent) {
                        Elements links = lines.getElementsByTag("a");
                        for (Element link:
                                links) {
                            String linkHref = link.attr("href");
                            String linkText = link.text();
                            content_Names.add(linkText + ";" + linkHref);
                        }
                    }
                    Element pagesContent = results.select("p").first();
                    Elements pages = pagesContent.getElementsByTag("a");
                    if(pages != null){
                        if(pages.size() > 0){
                            currentSchemaTotalPageCount = 0;
                            for (Element pageItem:
                                    pages) {
                                if(StringUtil.isNumeric(pageItem.text())){
                                    int pageNumber = Integer.valueOf(pageItem.text());
                                    if(pageNumber > currentSchemaTotalPageCount){
                                        currentSchemaTotalPageCount = pageNumber;
                                        new_Page_Address = pageItem.attr("href");
                                    }
                                }
                            }
                            Log.i("MAINActivity", "NetworkTask(): doInBackground() " + currentSchemaTotalPageCount + " results available for " + SITE + CATEGORY_TV);
                        }

                    }
                }
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
            if(content_Names != null && content_Names.size() > 0){
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(mProgressDialog != null){
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            if(mProgressBar != null){
                mProgressBar.setVisibility(View.GONE);
            }
            if(mSwipeRefreshLayout != null){
                if(mSwipeRefreshLayout.isRefreshing())
                    mSwipeRefreshLayout.setRefreshing(false);
            }
            if(aBoolean != null){
                if(aBoolean.booleanValue()){
                    if(has_Swipe_Refresh){
                        updateData(content_Names, true);
                        has_Swipe_Refresh = false;
                    } else {
                        updateData(content_Names, false);
                    }
                    if(currentPageDisplayedOfCategory == null){
                        currentPageDisplayedOfCategory = new HashMap<>();
                    }

                    if(schemaPageCountOfCategory == null){
                        schemaPageCountOfCategory = new HashMap<>();
                    }
                    if(Url.equals(SITE + CATEGORY_TV)){
                        currentPageDisplayedOfCategory.put(CATEGORY_TV, 0);
                        schemaPageCountOfCategory.put(CATEGORY_TV, currentSchemaTotalPageCount);
                    }
                } else {

                }
            }
            Log.i("MAINActivity", "NetworkTask(): onPostExecute() ");
        }
    }

    public class GetContentTask extends AsyncTask<String, Void, Boolean>{

        private ProgressDialog mProgressDialog;
        private String Url;

        private ArrayList<String> content_Names;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i("MAINActivity", "GetContentTask(): onPreExecute() ");
            content_Names = new ArrayList<>();
        }

        @Override
        protected Boolean doInBackground(String... siteURLs) {
            try{
                Log.i("MAINActivity", "GetContentTask(): doInBackground() ");

                String site_Address = siteURLs[0];
                if(site_Address != null){
                    Document webPage_Content = HttpCalls.jsoupGetContent(HttpCalls.jsoupConnect(site_Address));
                    if(webPage_Content != null){
                        Element content = webPage_Content.body();
                        Element results = content.select("div.results").first();
                        Elements focusContent = results.getElementsByTag("dl");

                        for (Element lines:
                                focusContent) {
                            Elements links = lines.getElementsByTag("a");
                            for (Element link:
                                    links) {
                                String linkHref = link.attr("href");
                                String linkText = link.text();
                                content_Names.add(linkText + ";" + linkHref);
                            }
                        }
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
            if(content_Names != null && content_Names.size() > 0){
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(mProgressDialog != null){
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            /*if(mSwipeRefreshLayout != null){
                if(mSwipeRefreshLayout.isRefreshing())
                    mSwipeRefreshLayout.setRefreshing(false);
            }*/
            if(aBoolean != null){
                if(aBoolean.booleanValue()){
                    updateData(content_Names, false);
                    int pageNo = currentPageDisplayedOfCategory.get(CATEGORY_TV);
                    currentPageDisplayedOfCategory.put(CATEGORY_TV, pageNo + 1);
                }
            }
            if(adapter != null){
                adapter.setLoaded(false);
            }
            Log.i("MAINActivity", "GetContentTask(): onPostExecute() ");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        THIS = null;
    }
}
