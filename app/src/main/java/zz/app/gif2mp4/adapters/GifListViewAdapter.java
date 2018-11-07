package zz.app.gif2mp4.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Handler;
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

import zz.app.gif2mp4.R;
import zz.app.gif2mp4.Utils;
import zz.app.gif2mp4.activitys.GifActivity;
import zz.app.gif2mp4.activitys.ShowGifActivity;
import zz.app.gif2mp4.controllers.ActivityTransitionController;
import zz.app.gif2mp4.interfaces.IGoBack;


public class GifListViewAdapter extends RecyclerView.Adapter<GifListViewAdapter.ViewHolder> {

    private final Handler handler;
    private ArrayList<String> files;
    private Context context;
    private static final String TAG = "GifListViewAdapter";

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    private boolean ready=false;
    public GifListViewAdapter(Context context, Handler handler, ArrayList<String> files) {
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
            viewHolder.cvHintHolder.setVisibility(View.GONE);
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
            GifRequestBuilder builder = Glide.with(context).load(new File(files.get(i-1))).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).listener(new RequestListener<File, GifDrawable>() {
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
            viewHolder.tvImageName.setText(new File(files.get(i-1)).getName());
            gifView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Intent intent = new Intent(context, GifActivity.class);
                    //Intent intent = new Intent(context, TestActivity.class);
                    ShowGifActivity activity = ((ShowGifActivity) context);
                    if (null != gifView.getDrawable() && gifView.getDrawable() instanceof GifDrawable) {
                        if (!Utils.checkgif(new File(files.get(viewHolder.getPosition()-1)).getAbsolutePath())) {
                            Toast.makeText(activity, "无效的Gif文件或为静态Gif", Toast.LENGTH_SHORT).show();
                        } else {
                            Utils.getManager().gif2mp4handler = new ActivityTransitionController((ShowGifActivity) context);
                            Utils.getManager().gif2mp4handler.setShowListener(new ActivityTransitionController.ShowListener() {
                                @Override
                                public void onShow(IGoBack from) {
                                    from.go();
                                }
                            });
                            Utils.getManager().gif2mp4handler.setHideListener(new ActivityTransitionController.HideListener() {
                                @Override
                                public void onHide(IGoBack from) {
                                    from.back();
                                }
                            });
                            intent.putExtra("inputPath", new File(files.get(viewHolder.getPosition()-1)).getAbsolutePath());

                            int w = gifView.getWidth();
                            int h = gifView.getHeight();
                            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(bitmap);
                            gifView.draw(canvas);
                            Utils.getManager().gif2mp4handler.setBitmap(bitmap);
                            int[] pos = new int[2];
                            gifView.getLocationInWindow(pos);
                            intent.putExtra("picy", pos[1]);
                            int width = gifView.getDrawable().getIntrinsicWidth();
                            int height = gifView.getDrawable().getIntrinsicHeight();
                            intent.putExtra("width", width);
                            intent.putExtra("height", height);
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
                            boolean b = FileUtils.deleteQuietly(new File(files.get(viewHolder.getPosition()-1)));
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

            viewHolder.cvHintHolder.setVisibility(View.INVISIBLE);
            viewHolder.cardView.setVisibility(View.GONE);
            if(ready) {
                viewHolder.cvHintHolder.setRadius(30);
                viewHolder.cvHintHolder.setVisibility(View.VISIBLE);
                if (i == 0) {
                    String s = "请选择以下Gif文件";
                    viewHolder.tvHint.setText(s);
                } else {
                    String s = "再怎么往下翻也没有了。。。";
                    viewHolder.tvHint.setText(s);
                }
            }
            ViewGroup.LayoutParams params = viewHolder.cvHintHolder.getLayoutParams();
            Point size = new Point();
            ((ShowGifActivity) context).getWindowManager().getDefaultDisplay().getSize(size);
            params.height = size.y/6;
            viewHolder.cvHintHolder.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return files.size()+2;
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
        FrameLayout frameLayout;
        CardView cardView;
        TextView tvHint;
        CardView cvHintHolder;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            tvLoadingHint = itemView.findViewById(R.id.tvloadinghint);
            tvImageName = itemView.findViewById(R.id.tvimageName);
            cardView = itemView.findViewById(R.id.cardview);
            tvHint=itemView.findViewById(R.id.tvHint);
            frameLayout = (FrameLayout) itemView;
            cvHintHolder=itemView.findViewById(R.id.cvhintholder);
        }
    }
}
