package zz.app.gif2mp4.activitys;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import zz.app.gif2mp4.R;
import zz.app.gif2mp4.Utils;
import zz.app.gif2mp4.adapters.M2GConfigAdapter;
import zz.app.gif2mp4.interfaces.OnMp4ProgressSliderListener;
import zz.app.gif2mp4.views.Mp4ProgressSliderView;
import zz.app.gif2mp4.views.Mp4RegionSelectorView;

public class Mp4Activity extends AppCompatActivity {

    private static final int MSG_CONVERTFINISH = 1;
    private static final int MSG_CONVERTPROGRESSCHANGED = 2;
    private static final int MSG_SCANFINISH = 3;
    M2GConfigAdapter adapter;
    RecyclerView recyclerView;
    String mp4path;
    MediaPlayer player;
    TextureView tvvideo;
    TextView tvCurrent;
    TextView tvLeft;
    TextView tvRight;
    ImageButton btnPlaypause;
    Mp4ProgressSliderView mp4progress;
    Thread progressThread;
    boolean baudio;
    ImageButton btnAudio;
    Handler handler;
    boolean bStop = false;
    AlertDialog waitdialog;
    Thread readMp4InfoThread;
    long[] mp4info;
    Button btnOutput;
    Button btnOutputMp4;
    Thread convertThread, readProgressThread;
    String outputPath;
    int progress, lastprogress = -1;
    AlertDialog convertDialog;
    ProgressBar pbConvert;
    TextView tvConvertProgress;
    MediaScannerConnection connection;
    boolean bPrepared;
    Mp4RegionSelectorView regionSelector;
    FrameLayout videoHolder;
    FrameLayout videoOutHolder;
    RelativeLayout videoOutHolderHolder;
    float left, top, right, bottom;
    private long currentLeft;
    private long currentRight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_mp4_to_gif);
        Utils.getManager().mp42gifhandler.setTo(this);
        init();

    }

    private void init() {
        mp4path = getIntent().getStringExtra("inputPath");
        player = new MediaPlayer();
        setVolume(false);
        initAnimation();
        initView();

        handler = new Handler(new Handler.Callback() {


            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_CONVERTFINISH:
                        if (convertDialog != null && convertDialog.isShowing()) {
                            convertDialog.dismiss();
                            convertDialog = null;
                        }
                        pbConvert = null;
                        tvConvertProgress = null;
                        connection = new MediaScannerConnection(Mp4Activity.this,
                                new MediaScannerConnection.MediaScannerConnectionClient() {
                                    @Override
                                    public void onMediaScannerConnected() {
                                        if (connection != null && connection.isConnected())
                                            connection.scanFile(outputPath, null);
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
                        final File f = new File(outputPath);
                        String str = "文件路径:" + outputPath + "\n文件大小:" + Utils.size2String(f.length()) + "\n\n是否保存？";
                        new AlertDialog.Builder(Mp4Activity.this).setTitle("转换完成").setMessage(str).setPositiveButton("是", new DialogInterface.OnClickListener() {
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
        progressThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean b = false;
                while (!b && !Thread.interrupted()) {
                    timerproc();
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        b = true;
                        e.printStackTrace();
                    }
                }

            }
        });
        getWindow().findViewById(Window.ID_ANDROID_CONTENT).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getWindow().findViewById(Window.ID_ANDROID_CONTENT).getViewTreeObserver().removeOnGlobalLayoutListener(this);
                initScale();
            }
        });
    }

    private void timerproc() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mp4progress != null && player != null) {
                    int duration = player.getDuration();
                    int position = player.getCurrentPosition();
                    tvCurrent.setText(Utils.mills2Str(position));
                    int temp = (int) ((double) position * Mp4ProgressSliderView.SCALE / duration);
                    int temp2 = mp4progress.getRbound();
                    int temp3 = (int) ((double) temp2 * duration / Mp4ProgressSliderView.SCALE);
                    if (temp > temp2) {
                        compatibleSeekTo(temp3);
                        btnPlaypause.setImageResource(R.drawable.play);
                        player.pause();
                        bStop = true;
                    } else {
                        mp4progress.setCurrent(temp);
                    }
                }
            }
        });
    }

    private void initScale() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        mp4info = Utils.getMp4Info(mp4path);
        if (mp4info != null) {
            int width = (int) mp4info[0];
            int height = (int) mp4info[1];
            float twidth = videoOutHolder.getWidth();
            float theight = videoOutHolder.getHeight();
            float temp;
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) videoHolder.getLayoutParams();

            if (width * theight > height * twidth) {
                temp = (theight - height * twidth / width) / 2;
                params.topMargin = params.bottomMargin = (int) temp;
                videoHolder.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        videoHolder.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        regionSelector.reset();
                    }
                });
                videoHolder.setLayoutParams(params);

            } else {
                ViewGroup.LayoutParams params1 = videoOutHolderHolder.getLayoutParams();
                params1.height += Utils.dp2px(Mp4Activity.this, 100);
                videoOutHolderHolder.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        videoOutHolderHolder.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) videoHolder.getLayoutParams();
                        int width = (int) mp4info[0];
                        int height = (int) mp4info[1];
                        float twidth = videoOutHolder.getWidth();
                        float theight = videoOutHolder.getHeight();
                        float temp;
                        if (width * theight > height * twidth) {
                            temp = (theight - height * twidth / width) / 2;
                            params.topMargin = params.bottomMargin = (int) temp;
                        } else {
                            temp = (twidth - width * theight / height) / 2;
                            params.leftMargin = params.rightMargin = (int) temp;
                        }
                        videoHolder.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                videoHolder.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                regionSelector.reset();
                            }
                        });
                        videoHolder.setLayoutParams(params);

                    }
                });
                videoOutHolderHolder.setLayoutParams(params1);

            }


        }
    }

    private void initAnimation() {
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.explode);
        getWindow().setEnterTransition(transition);
    }

    private void initView() {
        videoHolder = findViewById(R.id.videoholder);
        videoOutHolder = findViewById(R.id.videooutholder);
        videoOutHolderHolder = findViewById(R.id.videooutholderholder);
        regionSelector = findViewById(R.id.regionSelector);
        regionSelector.setListener(new Mp4RegionSelectorView.OnValueChangedListener() {
            @Override
            public void onValueChanged() {
                left = regionSelector.getRealLeft();
                top = regionSelector.getRealTop();
                right = regionSelector.getRealRight();
                bottom = regionSelector.getRealBottom();
                if (mp4info != null) {
                    if (adapter.getRotateType() == 0 || adapter.getRotateType() == 2) {
                        adapter.setOutwidth((int) ((right - left) * mp4info[0]));
                        adapter.setOutheight((int) ((bottom - top) * mp4info[1]));
                    } else {
                        adapter.setOutwidth((int) ((bottom - top) * mp4info[1]));
                        adapter.setOutheight((int) ((right - left) * mp4info[0]));
                    }
                    adapter.notifyItemChanged(1);
                }
            }
        });
        adapter = new M2GConfigAdapter(this);
        SharedPreferences preferences = getSharedPreferences("mp42gif", MODE_PRIVATE);
        adapter.setBitrate(preferences.getInt("defaultbitrate", 500 * 1000));
        recyclerView = findViewById(R.id.rvconfig);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        tvvideo = findViewById(R.id.tvvideo);
        tvvideo.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                player.setSurface(new Surface(surface));
                try {
                    FileInputStream inputStream = new FileInputStream(new File(mp4path));
                    player.setDataSource(inputStream.getFD());
                    player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            Toast.makeText(Mp4Activity.this, "加载视频失败，请选择其他视频！", Toast.LENGTH_SHORT).show();
                            waitdialog = null;
                            finish();
                            return true;
                        }
                    });
                    player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            bPrepared = true;
                            player.start();
                            player.pause();
                            waitdialog = new AlertDialog.Builder(Mp4Activity.this).setView(R.layout.waitinglayout).setCancelable(false).create();
                            waitdialog.setCanceledOnTouchOutside(false);
                            waitdialog.show();
                            WindowManager.LayoutParams params = Objects.requireNonNull(waitdialog.getWindow()).getAttributes();
                            Point size = new Point();
                            getWindow().getWindowManager().getDefaultDisplay().getSize(size);
                            int x = Math.min(size.x, size.y);
                            params.width = x / 2;
                            params.height = x / 2;
                            params.dimAmount = 0.5f;
                            waitdialog.getWindow().setAttributes(params);
                            readMp4InfoThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (waitdialog != null && waitdialog.isShowing()) {
                                                waitdialog.dismiss();
                                                waitdialog = null;
                                                adapter.setFilesize(FileUtils.sizeOf(new File(mp4path)));
                                                adapter.setMp4Info(mp4info);
                                                player.start();
                                                tvLeft.setText(Utils.mills2Str(0));
                                                currentLeft=0;
                                                tvRight.setText(Utils.mills2Str(player.getDuration()));
                                                currentRight=player.getDuration();
                                                adapter.setRealTime((currentRight-currentLeft)/1000.0);
                                                adapter.notifyItemChanged(5);
                                                progressThread.start();

                                            }
                                        }
                                    });
                                }
                            });
                            readMp4InfoThread.start();

                        }
                    });
                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            compatibleSeekTo((int) ((double) mp4progress.getLbound() * mp.getDuration() / Mp4ProgressSliderView.SCALE));
                            btnPlaypause.setImageResource(R.drawable.play);

                        }
                    });
                    player.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

        btnAudio = findViewById(R.id.btnaudio);
        btnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!baudio) {
                    btnAudio.setImageResource(R.drawable.audioon);
                    setVolume(true);
                } else {
                    btnAudio.setImageResource(R.drawable.audiooff);
                    setVolume(false);
                }
            }
        });
        btnPlaypause = findViewById(R.id.btnPlaypause);
        btnPlaypause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player != null) {
                    if (player.isPlaying()) {
                        player.pause();
                        btnPlaypause.setImageResource(R.drawable.play);
                    } else {
                        if (bStop) {
                            bStop = false;
                            compatibleSeekTo((int) ((double) mp4progress.getLbound() * player.getDuration() / Mp4ProgressSliderView.SCALE));
                        }
                        player.start();
                        btnPlaypause.setImageResource(R.drawable.pause);
                    }
                }
            }
        });
        mp4progress = findViewById(R.id.mp4progress);
        mp4progress.setDEBUG(false);
        mp4progress.setListener(new OnMp4ProgressSliderListener() {
            @Override
            public void onSliderDown() {
                if (progressThread != null) {
                    progressThread.interrupt();
                    progressThread = null;
                }
            }

            @Override
            public void onSliderMoving(int value) {
                int duration = player.getDuration();
                int temp = (int) ((double) value * duration / Mp4ProgressSliderView.SCALE);
                compatibleSeekTo(temp);
                player.pause();
                tvCurrent.setText(Utils.mills2Str(temp));
                btnPlaypause.setImageResource(R.drawable.pause);
            }

            @Override
            public void onSliderUp(int value) {
                int duration = player.getDuration();
                int temp = (int) ((double) value * duration / Mp4ProgressSliderView.SCALE);
                compatibleSeekTo(temp);
                btnPlaypause.setImageResource(R.drawable.pause);
                player.start();
                progressThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean b = false;
                        while (!b && !Thread.interrupted()) {
                            timerproc();
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                b = true;
                                e.printStackTrace();
                            }
                        }
                    }
                });
                progressThread.start();

            }

            @Override
            public void onSelectorDown(Selector which) {

            }

            @Override
            public void onSelectorMoving(Selector which, int value) {
                if (player != null) {
                    long temp;
                    long duration = player.getDuration();
                    switch (which) {
                        case LEFT:
                            temp = (long) ((double) value * duration / Mp4ProgressSliderView.SCALE);
                            currentLeft=temp;
                            tvLeft.setText(Utils.mills2Str(temp));
                            compatibleSeekTo(temp);
                            tvCurrent.setText(Utils.mills2Str(temp));
                            player.pause();
                            btnPlaypause.setImageResource(R.drawable.pause);
                            break;
                        case RIGHT:
                            temp = (long) ((double) value * duration / Mp4ProgressSliderView.SCALE);
                            currentRight=temp;
                            tvRight.setText(Utils.mills2Str(temp));
                            compatibleSeekTo(temp);
                            tvCurrent.setText(Utils.mills2Str(temp));
                            player.pause();
                            btnPlaypause.setImageResource(R.drawable.pause);
                            break;
                    }
                    adapter.setRealTime((currentRight-currentLeft)/1000.0);
                    adapter.notifyItemChanged(5);

                }
            }

            @Override
            public void onSelectorUp(Selector which, int value) {
                int temp;
                int duration = player.getDuration();
                switch (which) {
                    case LEFT:
                        temp = (int) ((double) value * duration / Mp4ProgressSliderView.SCALE);
                        compatibleSeekTo(temp);
                        player.start();
                        break;
                    case RIGHT:
                        temp = (int) ((double) mp4progress.getLbound() * duration / Mp4ProgressSliderView.SCALE);
                        compatibleSeekTo(temp);
                        player.start();
                        break;
                }
            }
        });
        tvCurrent = findViewById(R.id.tvcurrent);
        tvLeft = findViewById(R.id.tvleft);
        tvRight = findViewById(R.id.tvright);
        tvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = player.getCurrentPosition();
                int duration = player.getDuration();
                int temp = (int) ((double) Mp4ProgressSliderView.SCALE * position / duration);
                mp4progress.setLbound(temp);
                tvLeft.setText(Utils.mills2Str(position));
            }
        });
        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = player.getCurrentPosition();
                int duration = player.getDuration();
                int temp = (int) ((double) Mp4ProgressSliderView.SCALE * position / duration);
                mp4progress.setRbound(temp);
                tvRight.setText(Utils.mills2Str(position));
            }
        });
        tvCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int duration = player.getDuration();
                mp4progress.setLbound(0);
                mp4progress.setRbound(Mp4ProgressSliderView.SCALE);
                tvLeft.setText(Utils.mills2Str(0));
                tvRight.setText(Utils.mills2Str(duration));
            }
        });
        btnOutput = findViewById(R.id.btnoutput);
        btnOutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Mp4Activity.this).setTitle("提示").setMessage("即将转换，是否确认？\n(将丢失音频数据)").setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                        String str = format.format(calendar.getTime());
                        outputPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Gif_Mp4/Gif/" + str + ".gif";
                        File f = new File(outputPath);
                        if (f.exists()) {
                            FileUtils.deleteQuietly(f);
                        }
                        final double start = (double) mp4progress.getLbound() * player.getDuration() / 1000 / Mp4ProgressSliderView.SCALE;
                        final double end = (double) mp4progress.getRbound() * player.getDuration() / 1000 / Mp4ProgressSliderView.SCALE;

                        convertThread = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Utils.setAnalyseState(Utils.AnalyseState.Mp42Gif);
                                double totalTime=(end - start)*adapter.getOutputTimeScale();
                                Utils.setTotalTime(totalTime);
                                int ret = Utils.mp42gif2(mp4path, outputPath, adapter.getOutfps(), adapter.getRotateType(),
                                        (int) (adapter.getOutwidth() * adapter.getScale()), (int) (adapter.getOutheight() * adapter.getScale()),
                                        (int) (left * mp4info[0]), (int) (top * mp4info[1]), (int) ((right - left) * mp4info[0]), (int) ((bottom - top) * mp4info[1]),
                                        start, end, totalTime);
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
                                    String line = Utils.getAnalyseLine();
                                    Utils.analyseProgress(line);
                                    progress = Utils.getProgress2();
                                    if (progress != lastprogress) {
                                        lastprogress = progress;
                                        handler.obtainMessage(MSG_CONVERTPROGRESSCHANGED).sendToTarget();
                                    }
                                }
                            }
                        });
                        View v1 = View.inflate(Mp4Activity.this, R.layout.convertdialoglayout, null);
                        pbConvert = v1.findViewById(R.id.pbconvert);
                        tvConvertProgress = v1.findViewById(R.id.tvconvertprogress);
                        tvConvertProgress.setText("0%");
                        convertDialog = new AlertDialog.Builder(Mp4Activity.this).setView(v1).setCancelable(false).show();
                        convertThread.start();
                        readProgressThread.start();
                    }
                }).setNegativeButton("否", null).show();

            }
        });
        btnOutputMp4 = findViewById(R.id.btnoutputmp4);
        btnOutputMp4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Mp4Activity.this).setTitle("提示").setMessage("即将转换，是否确认？").setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                        String str = format.format(calendar.getTime());
                        outputPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Gif_Mp4/Mp4/" + str + ".mp4";
                        File f = new File(outputPath);
                        if (f.exists()) {
                            FileUtils.deleteQuietly(f);
                        }
                        final double start = (double) mp4progress.getLbound() * player.getDuration() / 1000 / Mp4ProgressSliderView.SCALE;
                        final double end = (double) mp4progress.getRbound() * player.getDuration() / 1000 / Mp4ProgressSliderView.SCALE;

                        convertThread = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Utils.setAnalyseState(Utils.AnalyseState.Mp42Mp4);
                                double totalTime=(end - start)*adapter.getOutputTimeScale();
                                Utils.setTotalTime(totalTime);
                                int ret = Utils.mp42mp4(mp4path, outputPath, adapter.getOutfps(), adapter.getRotateType(),
                                        (int) (adapter.getOutwidth() * adapter.getScale()), (int) (adapter.getOutheight() * adapter.getScale()),
                                        (int) (left * mp4info[0]), (int) (top * mp4info[1]), (int) ((right - left) * mp4info[0]), (int) ((bottom - top) * mp4info[1]),
                                        start, end, totalTime,adapter.getBitrate(),Utils.isMp4HasAudio(mp4path));
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
                                    String line = Utils.getAnalyseLine();
                                    Utils.analyseProgress(line);
                                    progress = Utils.getProgress2();
                                    if (progress != lastprogress) {
                                        lastprogress = progress;
                                        handler.obtainMessage(MSG_CONVERTPROGRESSCHANGED).sendToTarget();
                                    }
                                }
                            }
                        });
                        View v1 = View.inflate(Mp4Activity.this, R.layout.convertdialoglayout, null);
                        pbConvert = v1.findViewById(R.id.pbconvert);
                        tvConvertProgress = v1.findViewById(R.id.tvconvertprogress);
                        tvConvertProgress.setText("0%");
                        convertDialog = new AlertDialog.Builder(Mp4Activity.this).setView(v1).setCancelable(false).show();
                        convertThread.start();
                        readProgressThread.start();
                    }
                }).setNegativeButton("否", null).show();

            }
        });

    }

    private void setVolume(boolean b) {
        if (player != null) {
            this.baudio = b;
            if (b)
                player.setVolume(1, 1);
            else
                player.setVolume(0, 0);
        }
    }

    @Override
    public void onBackPressed() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        Utils.getManager().mp42gifhandler.show();
        super.onBackPressed();
    }

    private void compatibleSeekTo(long millis) {
        if (player != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                player.seekTo(millis, MediaPlayer.SEEK_CLOSEST);
            } else {
                player.seekTo((int) millis);
            }
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
