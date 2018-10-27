package zz.app.gif2mp4.activitys;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import zz.app.gif2mp4.R;
import zz.app.gif2mp4.Utils;
import zz.app.gif2mp4.adapters.GifListViewAdapter;
import zz.app.gif2mp4.interfaces.IGoBack;

public class ShowGifActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,IGoBack {
    RecyclerView giflistview;
    ArrayList<File> files;
    SwipeRefreshLayout swipeRefreshLayout;
    GifListViewAdapter adapter;
    AlertDialog sortdialog;
    final String Magic = "gif";
    private static final int MSG_IMGOK = 0;
    private static final int MSG_CLOSE_SORTVIEW = 1;
    int sorttype;
    boolean ascending;
    boolean totop = false;
    Handler handler;
    String[] sortmethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_gif);
        setTitle("选择Gif文件");
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        init();
    }

    private void init() {
        Transition transition=TransitionInflater.from(this).inflateTransition(R.transition.fade);
        getWindow().setEnterTransition(transition);
        sortmethod=getResources().getStringArray(R.array.sort_type);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_IMGOK:
                        adapter.setFiles(files);
                        adapter.setReady(true);
                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                        if (totop) {
                            totop = false;
                            giflistview.smoothScrollToPosition(0);
                        }
                        break;
                    case MSG_CLOSE_SORTVIEW:
                        if (sortdialog != null)
                            sortdialog.dismiss();
                        break;
                }
                return false;
            }
        });
        readSettings();
        initView();
    }

    private void readSettings() {
        SharedPreferences preferences = getSharedPreferences("gif2mp4", MODE_PRIVATE);
        sorttype = preferences.getInt("sorttype", 0);
        ascending = preferences.getBoolean("ascending", false);

    }

    private void initView() {

        swipeRefreshLayout = findViewById(R.id.gifsrl);
        swipeRefreshLayout.setColorSchemeColors(getColor(R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(this);
        giflistview = findViewById(R.id.giflistview);
        giflistview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GifListViewAdapter(this,handler, new ArrayList<File>());
        giflistview.setAdapter(adapter);
        freshimg();
    }

    @Override
    public void onRefresh() {
        freshimg();
    }

    public void freshimg() {
        swipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                files = Utils.getFiles(Magic, sorttype, ascending, true);
                handler.obtainMessage(MSG_IMGOK).sendToTarget();
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.showimgmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menusort:
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View v = View.inflate(ShowGifActivity.this, R.layout.gifsortlistview, null);
                ListView lv = v.findViewById(R.id.gifsort_listview);
                lv.setAdapter(new ArrayAdapter<>(ShowGifActivity.this, R.layout.sortlistview, sortmethod));
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case 0:
                                sorttype = Utils.SORTTYPE_TIME;
                                ascending = false;
                                break;
                            case 1:
                                sorttype = Utils.SORTTYPE_TIME;
                                ascending = true;
                                break;
                            case 2:
                                sorttype = Utils.SORTTYPE_SIZE;
                                ascending = false;
                                break;
                            case 3:
                                sorttype = Utils.SORTTYPE_SIZE;
                                ascending = true;
                                break;
                            case 4:
                                sorttype = Utils.SORTTYPE_NAME;
                                ascending = false;
                                break;
                            case 5:
                                sorttype = Utils.SORTTYPE_NAME;
                                ascending = true;
                                break;
                        }
                        SharedPreferences preferences = getSharedPreferences("gif2mp4", MODE_PRIVATE);
                        preferences.edit().putInt("sorttype", sorttype).putBoolean("ascending", ascending).apply();
                        handler.obtainMessage(MSG_CLOSE_SORTVIEW).sendToTarget();
                        swipeRefreshLayout.setRefreshing(true);
                        totop = true;
                        freshimg();
                    }

                });
                builder.setView(lv);
                sortdialog = builder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void back() {
        swipeRefreshLayout.setVisibility(View.INVISIBLE);
    }

    public void go() {
        swipeRefreshLayout.setVisibility(View.VISIBLE);
    }
}
