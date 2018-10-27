package zz.app.gif2mp4.activitys;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Matrix;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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

public class Mp42GifActivity extends AppCompatActivity {

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
    Thread convertThread, readProgressThread;
    String gifpath;
    int progress, lastprogress = -1;
    AlertDialog convertDialog;
    ProgressBar pbConvert;
    TextView tvConvertProgress;
    MediaScannerConnection connection;
    boolean bPrepared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_mp4_to_gif);
        Utils.getManager().mp42gifhandler.setTo(this);
        init();

    }

    private void init() {
        mp4path = getIntent().getStringExtra("mp4path");
        player = new MediaPlayer();
        setVolume(false);
        initAnimation();
        initView();
        initScale();
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
                        connection = new MediaScannerConnection(Mp42GifActivity.this,
                                new MediaScannerConnection.MediaScannerConnectionClient() {
                                    @Override
                                    public void onMediaScannerConnected() {
                                        if (connection != null && connection.isConnected())
                                            connection.scanFile(gifpath, "image/*");
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
                        File f = new File(gifpath);
                        String str = "文件路径:" + gifpath + "\n文件大小:" + Utils.size2String(f.length());
//                        new AlertDialog.Builder(Mp42GifActivity.this).setTitle("转换完成").setMessage(str).setPositiveButton("是", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                Intent intent = new Intent();
//                                intent.setAction(Intent.ACTION_VIEW);
//                                intent.setDataAndType(Uri.parse(gifpath), "image/*");
//                                startActivity(intent);
//                            }
//                        }).setNegativeButton("否", null).show();
//                        break;
                        Toast.makeText(Mp42GifActivity.this, str, Toast.LENGTH_SHORT).show();
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
            float twidth = size.x - Utils.dp2px(this, 20);
            float theight = Utils.dp2px(this, 221);
            float rx;
            float ry;
            Matrix matrix = new Matrix();
            if (width * theight > height * twidth) {
                float temp = twidth * height / width;
                ry = (theight - temp) / 2;
                matrix.setScale(1, temp / theight);
                matrix.postTranslate(0, ry);
            } else {
                float temp = theight * width / height;
                rx = (twidth - temp) / 2;
                matrix.setScale(temp / twidth, 1);
                matrix.postTranslate(rx, 0);
            }
            tvvideo.setTransform(matrix);
        }
    }

    private void initAnimation() {
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.explode);
        getWindow().setEnterTransition(transition);
    }

    private void initView() {
        adapter = new M2GConfigAdapter(this);
        recyclerView = findViewById(R.id.rvconfig);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
//        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
//            @Override
//            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
//                super.onDraw(c, parent, state);
//                View item = parent.getChildAt(0);
//                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
//                p.setColor(getColor(R.color.colorPrimary));
//                p.setStyle(Paint.Style.STROKE);
//                Point size = new Point();
//                getWindowManager().getDefaultDisplay().getSize(size);
//                float y = size.x / 59f;
//                float x = 5 * y;
//                p.setPathEffect(new DashPathEffect(new float[]{x, y}, 0));
//                p.setStrokeWidth(10);
//                Path mp4path = new Path();
//                mp4path.moveTo(item.getX(), item.getY()+item.getHeight() +25);
//                mp4path.lineTo(item.getX() + item.getWidth(), item.getY()+item.getHeight() +25);
//                c.drawPath(mp4path, p);
//            }
//        });
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
                            Toast.makeText(Mp42GifActivity.this, "加载视频失败，请选择其他视频！", Toast.LENGTH_SHORT).show();
                            waitdialog=null;
                            finish();
                            return true;
                        }
                    });
                    player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            //TODO:Read info
                            bPrepared = true;
                            player.start();
                            player.pause();
                            waitdialog = new AlertDialog.Builder(Mp42GifActivity.this).setView(R.layout.waitinglayout).setCancelable(false).create();
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
                                                tvRight.setText(Utils.mills2Str(player.getDuration()));
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
                    int temp;
                    int duration = player.getDuration();
                    switch (which) {
                        case LEFT:
                            temp = (int) ((double) value * duration / Mp4ProgressSliderView.SCALE);
                            tvLeft.setText(Utils.mills2Str(temp));
                            compatibleSeekTo(temp);
                            tvCurrent.setText(Utils.mills2Str(temp));
                            player.pause();
                            btnPlaypause.setImageResource(R.drawable.pause);
                            break;
                        case RIGHT:
                            temp = (int) ((double) value * duration / Mp4ProgressSliderView.SCALE);
                            tvRight.setText(Utils.mills2Str(temp));
                            compatibleSeekTo(temp);
                            tvCurrent.setText(Utils.mills2Str(temp));
                            player.pause();
                            btnPlaypause.setImageResource(R.drawable.pause);
                            break;
                    }
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
                new AlertDialog.Builder(Mp42GifActivity.this).setTitle("提示").setMessage("即将转换，是否确认？").setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                        String str = format.format(calendar.getTime());
                        gifpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Gif_Mp4/Gif/" + str + ".gif";
                        File f = new File(gifpath);
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
                                int ret = Utils.mp42gif(mp4path, gifpath, adapter.getOutfps(), adapter.getRotateType(), adapter.getOutwidth(), adapter.getOutheight(),
                                        (double) mp4progress.getLbound() * player.getDuration() / 1000 / Mp4ProgressSliderView.SCALE,
                                        (double) mp4progress.getRbound() * player.getDuration() / 1000 / Mp4ProgressSliderView.SCALE);
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
                                    progress = Utils.getProgress2();
                                    if (progress != lastprogress) {
                                        lastprogress = progress;
                                        handler.obtainMessage(MSG_CONVERTPROGRESSCHANGED).sendToTarget();
                                    }
                                }
                            }
                        });
                        View v1 = View.inflate(Mp42GifActivity.this, R.layout.convertdialoglayout, null);
                        pbConvert = v1.findViewById(R.id.pbconvert);
                        tvConvertProgress = v1.findViewById(R.id.tvconvertprogress);
                        tvConvertProgress.setText("0%");
                        convertDialog = new AlertDialog.Builder(Mp42GifActivity.this).setView(v1).setCancelable(false).show();
                        convertThread.start();
                        readProgressThread.start();
                    }
                }).setNegativeButton("否",null).show();

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

    private void compatibleSeekTo(int millis) {
        if (player != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                player.seekTo(millis, MediaPlayer.SEEK_CLOSEST);
            } else {
                player.seekTo(millis);
            }
        }
    }
}
