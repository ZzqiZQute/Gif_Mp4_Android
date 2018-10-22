package zz.app.gif2mp4;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class ShowGifActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    RecyclerView giflistview;
    ArrayList<File> files;
    SwipeRefreshLayout swipeRefreshLayout;
    GifListViewAdapter adapter;
    AlertDialog sortdialog;
    final String Magic = "gif";
    private static final int MSG_GIFOK = 0;
    private static final int MSG_CLOSE_GIFSORTVIEW = 1;
    int sorttype;
    boolean ascending;
    boolean totop = false;
    YellowTopFrameLayout yellowTopFrameLayout;
    Handler handler;
    String[] sortmethod = new String[]{
            "按时间降序",
            "按时间升序",
            "按大小降序",
            "按大小升序",
            "按名称降序",
            "按名称升序",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_gif);
        init();
    }

    private void init() {
        yellowTopFrameLayout =findViewById(R.id.framelayout);

        initCreate();
        setTitle("选择Gif图片");
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_GIFOK:
                        adapter.setFiles(files);
                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                        if (totop) {
                            totop = false;
                            giflistview.smoothScrollToPosition(0);
                        }
                        break;
                    case MSG_CLOSE_GIFSORTVIEW:
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
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        swipeRefreshLayout = findViewById(R.id.gifsrl);
        swipeRefreshLayout.setColorSchemeColors(getColor(R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(this);
        giflistview = findViewById(R.id.giflistview);
        giflistview.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new GifListViewAdapter(this,handler, new ArrayList<File>());
        giflistview.setAdapter(adapter);
        freshgif();
    }

    @Override
    public void onRefresh() {
        freshgif();
    }

    public void freshgif() {
        swipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                files = Utils.getFiles(Magic, sorttype, ascending, true);
                handler.obtainMessage(MSG_GIFOK).sendToTarget();
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.showgifmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menusort:
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View v = View.inflate(ShowGifActivity.this, R.layout.gifsortlistview, null);
                ListView lv = v.findViewById(R.id.gifsort_listview);
                lv.setAdapter(new ArrayAdapter<>(ShowGifActivity.this, R.layout.gifsortlistadapterview, sortmethod));
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
                        handler.obtainMessage(MSG_CLOSE_GIFSORTVIEW).sendToTarget();
                        swipeRefreshLayout.setRefreshing(true);
                        totop = true;
                        freshgif();
                    }

                });
                builder.setView(lv);
                sortdialog = builder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initCreate() {
        Utils.welcome();
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (!sdCardExist) {
            Toast.makeText(this, "请检查SD卡?", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!Utils.createDir("Gif_Mp4")) //主目录
        {
            Toast.makeText(this, "创建Gif_Mp4目录失败?", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!Utils.createDir("Gif_Mp4/Gif")) //主目录
        {
            Toast.makeText(this, "创建Gif_Mp4/Gif目录失败?", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!Utils.createDir("Gif_Mp4/Mp4")) //主目录
        {
            Toast.makeText(this, "创建Gif_Mp4/Mp4目录失败?", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void hide() {
        swipeRefreshLayout.setVisibility(View.INVISIBLE);
    }

    public void show() {
        swipeRefreshLayout.setVisibility(View.VISIBLE);
    }
}
