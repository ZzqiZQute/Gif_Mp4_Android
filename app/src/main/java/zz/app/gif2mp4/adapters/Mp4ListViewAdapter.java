package zz.app.gif2mp4.adapters;

import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import zz.app.gif2mp4.R;
import zz.app.gif2mp4.Utils;
import zz.app.gif2mp4.activitys.Mp4Activity;
import zz.app.gif2mp4.activitys.ShowMp4Activity;
import zz.app.gif2mp4.beans.Mp4Info;
import zz.app.gif2mp4.controllers.ActivityTransitionController;
import zz.app.gif2mp4.interfaces.IGoBack;


public class Mp4ListViewAdapter extends RecyclerView.Adapter<Mp4ListViewAdapter.ViewHolder> {

    private final Handler handler;
    private ArrayList<String> files;
    private ArrayList<Mp4Info> thumbnailMap;
    private ArrayList<String> showfiles;
    private Context context;

    public void setReadyToOpen(boolean readyToOpen) {
        this.readyToOpen = readyToOpen;
    }

    private boolean readyToOpen = true;

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    private MediaPlayer mediaPlayer;
    private static final String TAG = "Mp4ListViewAdapter";

    public int getPlayindex() {
        return playindex;
    }

    public void setPlayindex(int playindex) {
        this.playindex = playindex;
    }

    private int playindex = 0;

    public int getLastplayindex() {
        return lastplayindex;
    }

    public void setLastplayindex(int lastplayindex) {
        this.lastplayindex = lastplayindex;
    }

    private int lastplayindex = 0;

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    private boolean ready = false;

    public Mp4ListViewAdapter(Context context, Handler handler) {
        this.files = new ArrayList<>();
        this.context = context;
        this.handler = handler;
        this.thumbnailMap = new ArrayList<>();
        showfiles = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(context).inflate(R.layout.layout_video_file, viewGroup, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        try {
            if (i > 0 && i < showfiles.size() + 1) {
                viewHolder.cvHintHolder.setVisibility(View.GONE);
                viewHolder.baudio = false;
                viewHolder.btnAudio.setImageDrawable(new BitmapDrawable(context.getResources(), BitmapFactory.decodeResource(context.getResources(), R.drawable.audiooff)));
                final float start = viewHolder.cardView.getCardElevation();
                viewHolder.tvHint.setVisibility(View.INVISIBLE);
                ViewGroup.LayoutParams params = viewHolder.cardView.getLayoutParams();
                Point size = new Point();
                ((ShowMp4Activity) context).getWindowManager().getDefaultDisplay().getSize(size);
                params.height = size.x * 3 / 4;
                viewHolder.cardView.setLayoutParams(params);
                viewHolder.cardView.setVisibility(View.VISIBLE);
                final ImageView imgView = viewHolder.imageView;
                viewHolder.tvImageName.setVisibility(View.GONE);
                viewHolder.tvImageName.setSelected(true);
                ViewGroup.LayoutParams params1 = viewHolder.imageView.getLayoutParams();
                params1.width = size.x * 3 / 4;
                viewHolder.imageView.setLayoutParams(params1);
                final String path = showfiles.get(i - 1);
                File file = new File(path);
                String name = file.getName();
                viewHolder.tvImageName.setText(name);
                Bitmap bitmap = Objects.requireNonNull(findThumbnailMap(path)).getThumbnail();
                if (bitmap != null) {
                    imgView.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
                } else {
                    imgView.setImageDrawable(context.getDrawable(R.drawable.resourceerr));
                }

                viewHolder.imageView.setVisibility(View.VISIBLE);
                if (viewHolder.flvideowrapper.getChildCount() == 3) {
                    LinearLayout layout = (LinearLayout) viewHolder.flvideowrapper.getChildAt(1);
                    TextureView textureView = (TextureView) layout.getChildAt(0);
                    if (mediaPlayer != null)
                        mediaPlayer.release();
                    mediaPlayer = null;
                    layout.removeAllViews();
                    viewHolder.flvideowrapper.removeViewAt(1);

                }
                viewHolder.btnAudio.setVisibility(View.INVISIBLE);
                viewHolder.btnAudio.setOnClickListener(null);
                if (playindex == i - 1) {
                    viewHolder.btnAudio.setVisibility(View.VISIBLE);
                    viewHolder.btnAudio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mediaPlayer != null) {
                                try {
                                    if (!viewHolder.baudio) {
                                        viewHolder.baudio = true;
                                        mediaPlayer.setVolume(1, 1);
                                        viewHolder.btnAudio.setImageDrawable(new BitmapDrawable(context.getResources(), BitmapFactory.decodeResource(context.getResources(), R.drawable.audioon)));
                                    } else {
                                        viewHolder.baudio = false;
                                        mediaPlayer.setVolume(0, 0);
                                        viewHolder.btnAudio.setImageDrawable(new BitmapDrawable(context.getResources(), BitmapFactory.decodeResource(context.getResources(), R.drawable.audiooff)));

                                    }
                                } catch (Exception ignored) {

                                }
                            }
                        }
                    });
                    final LinearLayout layout = new LinearLayout(context);
                    layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    layout.setGravity(Gravity.CENTER);
                    final TextureView textureView = new TextureView(context);
                    textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                        @Override
                        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setSurface(new Surface(surface));
                            try {
                                FileInputStream inputStream = new FileInputStream(new File(path));
                                mediaPlayer.setDataSource(inputStream.getFD());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mp.setVolume(0, 0);
                                    mp.setLooping(true);
                                    mp.start();
                                }
                            });
                            mediaPlayer.prepareAsync();
                        }

                        @Override
                        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                        }

                        @Override
                        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                            return true;
                        }

                        @Override
                        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                            viewHolder.imageView.setVisibility(View.INVISIBLE);
                        }
                    });

                    ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    int margin = (int) Utils.dp2px(context, 5);
                    layoutParams.setMargins(margin, margin, margin, margin);
                    layoutParams.width = size.x * 3 / 4;
                    textureView.setLayoutParams(layoutParams);
                    int[] mp4Size = Utils.getMp4Size(path);
                    if (mp4Size != null) {
                        int width = mp4Size[0];
                        int height = mp4Size[1];
                        float twidth = size.x * 3 / 4;
                        float theight = (size.x) * 3 / 4 - 2 * Utils.dp2px(context, 5);
                        float rx;
                        float ry;
                        float rw;
                        float rh;
                        float rxs, rys, rxo, ryo;
                        if (width * theight > height * twidth) {
                            rw = twidth;
                            float temp = twidth * height / width;
                            ry = (theight - temp) / 2;
                            Matrix matrix = new Matrix();

                            matrix.setScale(1, temp / theight);
                            matrix.postTranslate(0, ry);

                            textureView.setTransform(matrix);
                        } else {
                            rh = theight;
                            float temp = theight * width / height;
                            rx = (twidth - temp) / 2;
                            Matrix matrix = new Matrix();

                            matrix.setScale(temp / twidth, 1);
                            matrix.postTranslate(rx, 0);

                            textureView.setTransform(matrix);
                        }
                        layout.addView(textureView);
                        viewHolder.tvLoadingHint.setVisibility(View.INVISIBLE);
                        viewHolder.flvideowrapper.addView(layout, 1);
                    } else {
                        viewHolder.tvLoadingHint.setVisibility(View.INVISIBLE);
                        viewHolder.imageView.setImageResource(R.drawable.resourceerr);
                    }
                }
                ValueAnimator animator = new ValueAnimator();
                animator.setFloatValues(0, 1);
                animator.setDuration(300);
                animator.start();
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float end;
                        if (playindex == viewHolder.getPosition() - 1) {
                            end = Utils.dp2px(context, 35);
                        } else {
                            end = Utils.dp2px(context, 5);
                        }

                        float v = (float) animation.getAnimatedValue();
                        viewHolder.cardView.setCardElevation(start + (end - start) * v);

                    }
                });

                viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        if (readyToOpen) {
                            try {
                                if (mediaPlayer != null)
                                    mediaPlayer.pause();
                            } catch (IllegalArgumentException ignored) {

                            }
                            Intent intent = new Intent(context, Mp4Activity.class);
                            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation((ShowMp4Activity) context).toBundle();
                            intent.putExtra("inputPath", path);
                            context.startActivity(intent, bundle);
                            Utils.getManager().mp42gifhandler = new ActivityTransitionController((ShowMp4Activity) context);
                            Utils.getManager().mp42gifhandler.setShowListener(new ActivityTransitionController.ShowListener() {
                                @Override
                                public void onShow(IGoBack from) {
                                    from.go();
                                }
                            });
                            Utils.getManager().mp42gifhandler.setHideListener(new ActivityTransitionController.HideListener() {
                                @Override
                                public void onHide(IGoBack from) {
                                    from.back();
                                }
                            });
                        }
                    }
                });
                viewHolder.cardView.setLongClickable(true);
                viewHolder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                        builder1.setMessage("是否删除Mp4？").setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                boolean b = FileUtils.deleteQuietly(new File(files.get(viewHolder.getPosition() - 1)));
                                ShowMp4Activity activity = ((ShowMp4Activity) context);
                                activity.freshimg();
                            }
                        }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
                        return false;
                    }

                });

            } else {
                viewHolder.cvHintHolder.setVisibility(View.INVISIBLE);
                viewHolder.cardView.setVisibility(View.INVISIBLE);
                if (ready) {
                    viewHolder.cvHintHolder.setRadius(30);
                    viewHolder.cvHintHolder.setVisibility(View.VISIBLE);
                    if (i == 0) {
                        String s = "请选择以下Mp4文件";
                        viewHolder.tvHint.setText(s);
                    } else {
                        String s = "再怎么往下翻也没有了。。。";
                        viewHolder.tvHint.setText(s);
                    }
                }
                ViewGroup.LayoutParams params = viewHolder.cvHintHolder.getLayoutParams();
                Point size = new Point();
                ((ShowMp4Activity) context).getWindowManager().getDefaultDisplay().getSize(size);
                params.height = size.y / 6;
                viewHolder.cvHintHolder.setLayoutParams(params);
            }
        } catch (Exception ignored) {
            Toast.makeText(context, "发生异常", Toast.LENGTH_SHORT).show();
            ((Mp4Activity) context).finish();
        }
    }

    private Mp4Info findThumbnailMap(String path) {
        for (Mp4Info p : thumbnailMap) {
            if (p.getPath().equals(path))
                return p;
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return showfiles.size() + 2;
    }

    public ArrayList<String> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<String> files) {
        this.files = files;
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvLoadingHint;
        TextView tvImageName;
        FrameLayout flvideowrapper;
        CardView cardView;
        TextView tvHint;
        ImageButton btnAudio;
        Boolean baudio;
        CardView cvHintHolder;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            tvLoadingHint = itemView.findViewById(R.id.tvloadinghint);
            tvImageName = itemView.findViewById(R.id.tvimageName);
            cardView = itemView.findViewById(R.id.cardview);
            tvHint = itemView.findViewById(R.id.tvHint);
            flvideowrapper = itemView.findViewById(R.id.flvideowrapper);
            btnAudio = itemView.findViewById(R.id.btnaudio);
            baudio = false;
            cvHintHolder = itemView.findViewById(R.id.cvhintholder);
        }

    }

    public void setThumbnailMap(ArrayList<Mp4Info> thumbnailMap) {
        this.thumbnailMap = thumbnailMap;
        showfiles.clear();
        for (Mp4Info p : thumbnailMap) {
            if (p.getThumbnail() != null)
                showfiles.add(p.getPath());
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == showfiles.size() + 1) return 0;
        else return 1;
    }
}
