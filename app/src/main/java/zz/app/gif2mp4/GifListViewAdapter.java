package zz.app.gif2mp4;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


public class GifListViewAdapter extends RecyclerView.Adapter<GifListViewAdapter.ViewHolder> {

    private final Handler handler;
    private ArrayList<File> files;
    private Context context;
    private static final String TAG = "GifListViewAdapter";

    GifListViewAdapter(Context context, Handler handler,ArrayList<File> files) {
        this.files = files;
        this.context = context;
        this.handler=handler;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_gif_file, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        final ImageView gifView = viewHolder.imageView;
        GifRequestBuilder builder = Glide.with(context).load(files.get(i)).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).listener(new RequestListener<File, GifDrawable>() {
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
        viewHolder.tvGifName.setText(files.get(i).getName());
        gifView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
               Intent intent = new Intent(context, Gif2Mp4Activity.class);
                //Intent intent = new Intent(context, TestActivity.class);
                ShowGifActivity activity = ((ShowGifActivity) context);
                if (null != gifView.getDrawable() && gifView.getDrawable() instanceof GifDrawable) {
                    if (!Utils.checkgif(files.get(viewHolder.getPosition()).getAbsolutePath())) {
                        Toast.makeText(activity, "无效的Gif文件或为静态Gif", Toast.LENGTH_SHORT).show();
                    } else {
                        intent.putExtra("gifpath", files.get(viewHolder.getPosition()).getAbsolutePath());

                        int w=gifView.getWidth();
                        int h=gifView.getHeight();
                        Bitmap bitmap=Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
                        Canvas canvas=new Canvas(bitmap);
                        gifView.draw(canvas);
                        intent.putExtra("bitmap",bitmap);
                        int[] location=new int[2];
                        gifView.getLocationOnScreen(location);
                        intent.putExtra("picx",location[0]);
                        intent.putExtra("picy",location[1]);
                        intent.putExtra("picw",w);
                        intent.putExtra("pich",h);
                         context.startActivity(intent);
                        ((ShowGifActivity)context).overridePendingTransition(0,0);

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
                        activity.freshgif();
                    }
                }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
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
        TextView tvGifName;


        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.gifview);
            tvLoadingHint = itemView.findViewById(R.id.tvloadinghint);
            tvGifName = itemView.findViewById(R.id.tvgifname);
        }
    }
}
