package zz.app.gif2mp4.activitys;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bumptech.glide.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Objects;

import zz.app.gif2mp4.R;
import zz.app.gif2mp4.Utils;
import zz.app.gif2mp4.adapters.Mp4ListViewAdapter;
import zz.app.gif2mp4.interfaces.IShowHide;

public class ShowMp4Activity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, IShowHide {
    ArrayList<File> files;
    ArrayList<Pair<String, Bitmap>> thumbnailMap;
    SwipeRefreshLayout swipeRefreshLayout;
    Mp4ListViewAdapter adapter;
    boolean totop = false;
    private static final int MSG_IMGOK = 0;
    private static final int MSG_CLOSE_SORTVIEW = 1;
    AlertDialog sortdialog;
    Handler handler;
    String[] sortmethod;
    RecyclerView mp4ListView;
    final String Magic = "mp4";
    int sorttype;
    boolean ascending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_mp4);
        init();

    }

    private void init() {
        sortmethod = getResources().getStringArray(R.array.sort_type);
        setTitle("选择Mp4视频");
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_IMGOK:
                        adapter.setFiles(files);
                        adapter.setThumbnailMap(thumbnailMap);
                        adapter.setReady(true);
                        adapter.notifyDataSetChanged();

                        swipeRefreshLayout.setRefreshing(false);
                        if (totop) {
                            totop = false;
                            mp4ListView.smoothScrollToPosition(0);
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

    private void initView() {

        swipeRefreshLayout = findViewById(R.id.mp4srl);
        swipeRefreshLayout.setColorSchemeColors(getColor(R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(this);
        mp4ListView = findViewById(R.id.mp4listview);
        mp4ListView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Mp4ListViewAdapter(this, handler, new ArrayList<File>(), new ArrayList<Pair<String, Bitmap>>());
        mp4ListView.setAdapter(adapter);
        mp4ListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_SETTLING:
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        break;
                    case RecyclerView.SCROLL_STATE_IDLE:
                        final LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                int pos = manager.findFirstVisibleItemPosition();
                                adapter.setPlayindex(pos);
                                if(adapter.getLastplayindex()!=adapter.getPlayindex()){
                                    adapter.setLastplayindex(adapter.getPlayindex());
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        },100);




                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        freshimg();

    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

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
                thumbnailMap = Utils.getThumbnailMap(ShowMp4Activity.this, files);
                handler.obtainMessage(MSG_IMGOK).sendToTarget();
            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menusort:
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View v = View.inflate(ShowMp4Activity.this, R.layout.gifsortlistview, null);
                ListView lv = v.findViewById(R.id.gifsort_listview);
                lv.setAdapter(new ArrayAdapter<>(ShowMp4Activity.this, R.layout.sortlistview, sortmethod));
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
                        SharedPreferences preferences = getSharedPreferences("mp42gif", MODE_PRIVATE);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.showimgmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void readSettings() {
        SharedPreferences preferences = getSharedPreferences("mp42gif", MODE_PRIVATE);
        sorttype = preferences.getInt("sorttype", 0);
        ascending = preferences.getBoolean("ascending", false);

    }

}
