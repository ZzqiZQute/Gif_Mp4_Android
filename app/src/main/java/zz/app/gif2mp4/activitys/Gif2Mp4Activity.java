package zz.app.gif2mp4.activitys;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import zz.app.gif2mp4.R;
import zz.app.gif2mp4.Utils;
import zz.app.gif2mp4.adapters.G2MConfigAdapter;
import zz.app.gif2mp4.views.ScaleImgView;

public class Gif2Mp4Activity extends AppCompatActivity {
    private static final int MSG_READGIFFRAMES = 0;
    private static final int MSG_CONVERTFINISH = 1;
    private static final int MSG_CONVERTPROGRESSCHANGED = 2;
    private static final int MSG_SCANFINISH = 3;
    String gifpath;
    String mp4path;
    RecyclerView listView;
    AlertDialog waitdialog;
    Handler handler;
    GifOptions gifOptions;
    G2MConfigAdapter adapter;
    Button btnOutput;
    Button btnWeixin;
    Thread convertThread;
    Thread readProgressThread;
    AlertDialog convertDialog;
    ProgressBar pbConvert;
    TextView tvConvertProgress;
    int progress;
    int lastprogress;
    ScaleImgView imageView;
    int w, h, x, y, picw, pich, picx, picy, picnx, picny, picnw, picnh, winx, winy, winh, winw;
    View anchor;
    MediaScannerConnection connection;
    private static final String TAG = "Gif2Mp4Activity";
    int scrolly = 0;
    Bitmap b;
    LinearLayout llselectpicwrapper;
    RelativeLayout mainlayout;
    private int lrmargin;
    private int lrnmargin;
    private int tnmargin;
    private int tmargin;
    private ValueAnimator animator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        anchor = View.inflate(this, R.layout.activity_gif_to_mp4, null);
        anchor.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {


            @Override
            public void onGlobalLayout() {
                Point size = new Point();
                getWindowManager().getDefaultDisplay().getSize(size);
                picnw = size.x - 120;
                picnh = size.x * 2 / 3;
                picw = size.x * 3 / 4;
                pich = size.x * 3 / 4;
                lrmargin = (size.x - picw) / 2;
                lrnmargin = 60;
                w = getIntent().getIntExtra("width", 0);
                h = getIntent().getIntExtra("height", 0);
                picy = getIntent().getIntExtra("picy", 0);
                int titlebarheight = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
                Rect rect = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                tmargin = picy - titlebarheight - rect.top;
                tnmargin = 0;
                anchor.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                imageView = new ScaleImgView(Gif2Mp4Activity.this, w, h);
                llselectpicwrapper.addView(imageView);
                animator = new ValueAnimator();
                animator.setDuration(300);
                animator.setFloatValues(0, 1);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float v = (float) animation.getAnimatedValue();
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
                        params.leftMargin = (int) (lrmargin + (lrnmargin - lrmargin) * v);
                        params.rightMargin = (int) (lrmargin + (lrnmargin - lrmargin) * v);
                        params.topMargin = (int) (tmargin + (tnmargin - tmargin) * v);
                        params.width = (int) (picw + (picnw - picw) * v);
                        params.height = (int) (pich + (picnh - pich) * v);
                        imageView.setLayoutParams(params);
                        mainlayout.setAlpha(v);
                        imageView.setDrawable(new BitmapDrawable(getResources(), b));
                        imageView.invalidate();
                    }
                });
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        Utils.getManager().gif2mp4handler.hide();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animator.start();

            }
        });
        setContentView(anchor);

        b = Utils.getManager().gif2mp4handler.getBitmap();

        init();
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrolly += dy;

            }
        });

    }


    private void init() {
        Utils.getManager().gif2mp4handler.setTo(this);
        btnOutput = findViewById(R.id.btnoutput);
        btnWeixin = findViewById(R.id.btnweixin);
        btnOutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(Gif2Mp4Activity.this).setTitle("提示").setMessage("即将转换，是否确认？").setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Calendar calendar=Calendar.getInstance();
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                        String str=format.format(calendar.getTime());
                        mp4path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Gif_Mp4/Mp4/" + str + ".mp4";
                        File f = new File(mp4path);
                        if (f.exists()) {
                            FileUtils.deleteQuietly(f);
                        }
                        convertThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Utils.setAnalyseState(Utils.AnalyseState.Gif2Mp4);
                                Utils.setTotalTime(adapter.getOutputtime());
                                int ret2=Utils.gif2mp42(gifpath, mp4path, adapter.getEncodertypenum(), adapter.getBitrate(), adapter.getOutputtime());
                                //int ret = Utils.gif2mp4(gifpath, mp4path, adapter.getEncodertypenum(), adapter.getBitrate(), adapter.getOutputtime(), adapter.getGifframes());
                                Log.i(TAG, "run: ret=" + ret2);
                                Utils.setProgress2(0);
                                handler.obtainMessage(MSG_CONVERTFINISH).sendToTarget();
                                convertThread = null;
                                if (readProgressThread != null && !readProgressThread.isInterrupted()) {
                                    readProgressThread.interrupt();
                                    readProgressThread = null;
                                }
                            }
                        });
                        readProgressThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (!Thread.interrupted()) {
                                    String line=Utils.getAnalyseLine();
                                    Utils.analyseProgress(line);
                                    progress = Utils.getProgress2();
                                    if (progress != lastprogress) {
                                        lastprogress = progress;
                                        handler.obtainMessage(MSG_CONVERTPROGRESSCHANGED).sendToTarget();
                                    }
                                }
                            }
                        });
                        View v = View.inflate(Gif2Mp4Activity.this, R.layout.convertdialoglayout, null);
                        pbConvert = v.findViewById(R.id.pbconvert);
                        tvConvertProgress = v.findViewById(R.id.tvconvertprogress);
                        tvConvertProgress.setText("0%");
                        convertDialog = new AlertDialog.Builder(Gif2Mp4Activity.this).setView(v).setCancelable(false).show();
                        convertThread.start();
                        readProgressThread.start();
                    }
                }).setNegativeButton("否",null).show();



            }
        });
        btnWeixin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final double time = adapter.getOutputtime();
                if (time < 2 || time > 10) {
                    new AlertDialog.Builder(Gif2Mp4Activity.this).setTitle("提示").setMessage("检测到Gif时间不在微信朋友圈时间范围内,将进行时间变换,是否确定?").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (time < 2) {
                                adapter.setOutputtime(2);
                            } else {
                                adapter.setOutputtime(10);
                            }
                            adapter.setEncodertypenum(0);
                            adapter.notifyItemChanged(2);
                            adapter.notifyItemChanged(4);
                            Toast.makeText(Gif2Mp4Activity.this, "已切换至朋友圈格式", Toast.LENGTH_SHORT).show();

                        }
                    }).setNegativeButton("取消", null).show();
                } else {
                    adapter.setEncodertypenum(0);
                    adapter.notifyItemChanged(2);
                    adapter.notifyItemChanged(4);
                    Toast.makeText(Gif2Mp4Activity.this, "已切换至朋友圈格式", Toast.LENGTH_SHORT).show();
                }

            }
        });
        setTitle("输出配置");
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_READGIFFRAMES:
                        gifOptions = (GifOptions) message.obj;
                        adapter.setGifOptions(gifOptions.frames, gifOptions.rate);
                        adapter.notifyItemChanged(1);
                        adapter.notifyItemChanged(4);
                        try {
                            if (waitdialog != null && waitdialog.isShowing()) {
                                waitdialog.dismiss();
                            }
                        } catch (IllegalArgumentException ignored) {

                        } finally {
                            waitdialog = null;
                        }
                        break;
                    case MSG_CONVERTFINISH:
                        if (convertDialog != null && convertDialog.isShowing()) {
                            convertDialog.dismiss();
                            convertDialog = null;
                        }
                        pbConvert = null;
                        tvConvertProgress = null;
                        connection = new MediaScannerConnection(Gif2Mp4Activity.this,
                                new MediaScannerConnection.MediaScannerConnectionClient() {
                                    @Override
                                    public void onMediaScannerConnected() {
                                        if (connection != null && connection.isConnected())
                                            connection.scanFile(mp4path, "video/mp4");
                                        // MediaScanner service 创建后回调
                                    }

                                    @Override
                                    public void onScanCompleted(String path, Uri uri) {
                                        // 当MediaScanner完成文件扫描后回调
                                        handler.obtainMessage(MSG_SCANFINISH).sendToTarget();

                                    }
                                });
                        connection.connect();

                        break;
                    case MSG_CONVERTPROGRESSCHANGED:
                        if (pbConvert != null) {
                            pbConvert.setProgress(progress);
                        }
                        if (tvConvertProgress != null) {
                            String s = String.valueOf(progress) + "%";
                            tvConvertProgress.setText(s);
                        }
                        break;
                    case MSG_SCANFINISH:
                        final File f = new File(mp4path);
                        String str="文件路径:"+mp4path+"\n文件大小:"+Utils.size2String(f.length())+"\n\n是否保存？";
                        new AlertDialog.Builder(Gif2Mp4Activity.this).setTitle("转换完成").setMessage(str).setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setCancelable(false).setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FileUtils.deleteQuietly(f);
                            }
                        }).show();
                        break;
                }
                return false;
            }
        });
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                llselectpicwrapper.setVisibility(View.INVISIBLE);
                try {
                    if (waitdialog != null) {
                        waitdialog.show();
                        WindowManager.LayoutParams params = Objects.requireNonNull(waitdialog.getWindow()).getAttributes();
                        Point size = new Point();
                        Gif2Mp4Activity.this.getWindow().getWindowManager().getDefaultDisplay().getSize(size);
                        int x = Math.min(size.x, size.y);
                        params.width = x / 2;
                        params.height = x / 2;
                        params.dimAmount = 0.5f;
                        waitdialog.getWindow().setAttributes(params);

                    }
                } catch (Exception ignored) {

                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int frames = Utils.gifframes(gifpath);
                        float rate = Utils.gifavgrate(gifpath);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handler.obtainMessage(MSG_READGIFFRAMES, new GifOptions(frames, rate)).sendToTarget();
                    }
                }).start();
            }
        }, 320);
        waitdialog = new AlertDialog.Builder(this).setView(View.inflate(this, R.layout.waitinglayout, null)).setCancelable(false).create();
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        listView = findViewById(R.id.g2mconfiglistview);
        listView.setLayoutManager(new LinearLayoutManager(this));
//        listView.addItemDecoration(new RecyclerView.ItemDecoration() {
//            @Override
//            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
//                super.onDraw(c, parent, state);
//                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
//                p.setColor(getColor(R.color.colorPrimary));
//                int cnt = parent.getChildCount();
//                for (int i = 2; i < cnt; i++) {
//                    View item = parent.getChildAt(i);
//                    c.drawLine(item.getX(), item.getY() + item.getHeight(), item.getX() + item.getWidth(), item.getY() + item.getHeight(), p);
//                    if (i == 2) {
//                        int d = ((ViewGroup.MarginLayoutParams) item.getLayoutParams()).topMargin;
//                        Paint p2 = new Paint(Paint.ANTI_ALIAS_FLAG);
//                        p2.setStyle(Paint.Style.STROKE);
//                        Point size = new Point();
//                        getWindowManager().getDefaultDisplay().getSize(size);
//                        float y = size.x / 59f;
//                        float x = 5 * y;
//                        p2.setPathEffect(new DashPathEffect(new float[]{x, y}, 0));
//                        p2.setColor(getColor(R.color.colorPrimary));
//                        p2.setStrokeWidth(d / 4);
//                        Path mp4path = new Path();
//                        mp4path.moveTo(item.getX(), item.getY() - d * 5 / 8);
//                        mp4path.lineTo(item.getX() + item.getWidth(), item.getY() - d * 5 / 8);
//                        c.drawPath(mp4path, p2);
//                    }
//                }
//            }
//        });
        gifpath = getIntent().getStringExtra("gifpath");
        adapter = new G2MConfigAdapter(this, gifpath);
        SharedPreferences preferences = getSharedPreferences("gif2mp4", MODE_PRIVATE);
        adapter.setBitrate(preferences.getInt("defaultbitrate", 500 * 1000));
        listView.setAdapter(adapter);
        llselectpicwrapper = findViewById(R.id.llselectpicwrapper);
        mainlayout = findViewById(R.id.mainlayout);
        mainlayout.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onPause() {
        overridePendingTransition(0, 0);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        waitdialog = null;
        if (this.animator != null) {
            this.animator.cancel();
            this.animator = null;
        }
        Utils.getManager().gif2mp4handler.show();
        llselectpicwrapper.setVisibility(View.VISIBLE);
        mainlayout.setVisibility(View.INVISIBLE);
        ValueAnimator animator = new ValueAnimator();
        animator.setDuration(300);
        animator.setFloatValues(1, 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
                params.leftMargin = (int) (lrmargin + (lrnmargin - lrmargin) * v);
                params.rightMargin = (int) (lrmargin + (lrnmargin - lrmargin) * v);
                params.topMargin = (int) (tmargin + (tnmargin - tmargin) * v);
                params.width = (int) (picw + (picnw - picw) * v);
                params.height = (int) (pich + (picnh - pich) * v);
                imageView.setLayoutParams(params);
                mainlayout.setAlpha(v);
                imageView.invalidate();
                WindowManager.LayoutParams attr = getWindow().getAttributes();
                attr.alpha = v;
                getWindow().setAttributes(attr);

            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();


    }


    class GifOptions {
        int frames;
        float rate;

        public GifOptions(int frames, float rate) {
            this.frames = frames;
            this.rate = rate;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(connection!=null){
            unbindService(connection);
            connection=null;
        }

    }
}
