package zz.app.gif2mp4.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.GifRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import android.os.Handler;

import zz.app.gif2mp4.activitys.ShowMp4Activity;
import zz.app.gif2mp4.controllers.ActivityTransitionController;
import zz.app.gif2mp4.interfaces.IShowHide;
import zz.app.gif2mp4.R;
import zz.app.gif2mp4.Utils;
import zz.app.gif2mp4.activitys.Gif2Mp4Activity;
import zz.app.gif2mp4.activitys.ShowGifActivity;


public class GifListViewAdapter extends RecyclerView.Adapter<GifListViewAdapter.ViewHolder> {

    private final Handler handler;
    private ArrayList<File> files;
    private Context context;
    private static final String TAG = "GifListViewAdapter";

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    private boolean ready=false;
    public GifListViewAdapter(Context context, Handler handler, ArrayList<File> files) {
        this.files = files;
        this.context = context;
        this.handler=handler;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_img_file, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        if (i > 0 && i < files.size() + 1) {
            viewHolder.tvHint.setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams params = viewHolder.cardView.getLayoutParams();
            Point size = new Point();
            ((ShowGifActivity) context).getWindowManager().getDefaultDisplay().getSize(size);
            params.height = size.x * 3 / 4;
            viewHolder.cardView.setLayoutParams(params);
            viewHolder.cardView.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params1=viewHolder.imageView.getLayoutParams();
            params1.width=size.x*3/4;
            viewHolder.imageView.setLayoutParams(params1);
            final ImageView imgView = viewHolder.imageView;
            viewHolder.tvImageName.setVisibility(View.GONE);
            viewHolder.tvImageName.setSelected(true);
            final ImageView gifView = viewHolder.imageView;
            GifRequestBuilder builder = Glide.with(context).load(files.get(i-1)).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).listener(new RequestListener<File, GifDrawable>() {
                @Override
                public boolean onException(Exception e, File model, Target<GifDrawable> target, boolean isFirstResource) {
                    viewHolder.tvLoadingHint.setVisibility(View.INVISIBLE);
                    return false;
                }

                @Override
                public boolean onResourceReady(GifDrawable resource, File model, Target<GifDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    viewHolder.tvLoadingHint.setVisibility(View.INVISIBLE);
                    return false;
                }
            }).error(R.drawable.resourceerr);
            builder.into(gifView);
            viewHolder.tvImageName.setText(files.get(i-1).getName());
            gifView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Intent intent = new Intent(context, Gif2Mp4Activity.class);
                    //Intent intent = new Intent(context, TestActivity.class);
                    ShowGifActivity activity = ((ShowGifActivity) context);
                    if (null != gifView.getDrawable() && gifView.getDrawable() instanceof GifDrawable) {
                        if (!Utils.checkgif(files.get(viewHolder.getPosition()-1).getAbsolutePath())) {
                            Toast.makeText(activity, "无效的Gif文件或为静态Gif", Toast.LENGTH_SHORT).show();
                        } else {
                            Utils.gif2mp4handler = new ActivityTransitionController((ShowGifActivity) context);
                            Utils.gif2mp4handler.setShowListener(new ActivityTransitionController.ShowListener() {
                                @Override
                                public void onShow(IShowHide from) {
                                    from.show();
                                }
                            });
                            Utils.gif2mp4handler.setHideListener(new ActivityTransitionController.HideListener() {
                                @Override
                                public void onHide(IShowHide from) {
                                    from.hide();
                                }
                            });
                            intent.putExtra("gifpath", files.get(viewHolder.getPosition()-1).getAbsolutePath());

                            int w = gifView.getWidth();
                            int h = gifView.getHeight();
                            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(bitmap);
                            gifView.draw(canvas);
                            Utils.gif2mp4handler.setBitmap(bitmap);
                            int[] location = new int[2];
                            gifView.getLocationOnScreen(location);
                            intent.putExtra("picx", location[0]);
                            intent.putExtra("picy", location[1]);
                            intent.putExtra("picw", w);
                            intent.putExtra("pich", h);
                            context.startActivity(intent);
                            ((ShowGifActivity) context).overridePendingTransition(0, 0);
                        }
                    } else {
                        Toast.makeText(activity, "加载Gif失败", Toast.LENGTH_SHORT).show();
                    }

                }
            });
            gifView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                    builder1.setMessage("是否删除Gif？").setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            boolean b = FileUtils.deleteQuietly(files.get(viewHolder.getPosition()));
                            ShowGifActivity activity = ((ShowGifActivity) context);
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
        }else{
            if(ready) {
                viewHolder.tvHint.setVisibility(View.VISIBLE);
                if (i == 0) {
                    String s = "选择下面的一个Gif文件吧！！！";
                    viewHolder.tvHint.setText(s);
                } else {
                    String s = "再怎么往下翻也没有了。。。";
                    viewHolder.tvHint.setText(s);
                }
            }
            ViewGroup.LayoutParams params =  viewHolder.cardView.getLayoutParams();
            Point size = new Point();
            ((ShowGifActivity) context).getWindowManager().getDefaultDisplay().getSize(size);
            params.height = size.y / 4;
            viewHolder.cardView.setLayoutParams(params);
            viewHolder.cardView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return files.size()+2;
    }

    public ArrayList<File> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<File> files) {
        this.files = files;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvLoadingHint;
        TextView tvImageName;
        FrameLayout frameLayout;
        CardView cardView;
        TextView tvHint;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            tvLoadingHint = itemView.findViewById(R.id.tvloadinghint);
            tvImageName = itemView.findViewById(R.id.tvimageName);
            cardView = itemView.findViewById(R.id.cardview);
            tvHint=itemView.findViewById(R.id.tvHint);
            frameLayout = (FrameLayout) itemView;
        }
    }
}
