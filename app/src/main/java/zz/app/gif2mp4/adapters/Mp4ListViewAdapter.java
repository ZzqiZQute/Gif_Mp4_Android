package zz.app.gif2mp4.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import zz.app.gif2mp4.R;
import zz.app.gif2mp4.Utils;
import zz.app.gif2mp4.activitys.ShowMp4Activity;
import zz.app.gif2mp4.controllers.ActivityTransitionController;
import zz.app.gif2mp4.interfaces.IShowHide;


public class Mp4ListViewAdapter extends RecyclerView.Adapter<Mp4ListViewAdapter.ViewHolder> {

    private final Handler handler;
    private ArrayList<File> files;
    private ArrayList<Pair<String, Bitmap>> thumbnailMap;
    private ArrayList<String> showfiles;
    private Context context;
    private static final String TAG = "Mp4ListViewAdapter";

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    private boolean ready=false;
    public Mp4ListViewAdapter(Context context, Handler handler, ArrayList<File> files, ArrayList<Pair<String, Bitmap>> thumbnailMap) {
        this.files = files;
        this.context = context;
        this.handler = handler;
        this.thumbnailMap = thumbnailMap;
        showfiles = new ArrayList<>();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(context).inflate(R.layout.layout_video_file, viewGroup, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        if (i > 0 && i < showfiles.size() + 1) {
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
            String path = showfiles.get(i - 1);
            File file = new File(path);
            String name = file.getName();
            viewHolder.tvImageName.setText(name);
            Bitmap bitmap = findThumbnailMap(path).second;
            if (bitmap != null) {
                imgView.setImageDrawable(new BitmapDrawable(null, bitmap));
            } else {
                imgView.setImageDrawable(context.getDrawable(R.drawable.resourceerr));
            }
            imgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    Utils.gif2mp4handler = new ActivityTransitionController((ShowMp4Activity) context);
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
                }
            });
        } else {
            if(ready) {
                viewHolder.tvHint.setVisibility(View.VISIBLE);
                if (i == 0) {
                    String s = "选择下面的一个Mp4文件吧！！！";
                    viewHolder.tvHint.setText(s);
                } else {
                    String s = "再怎么往下翻也没有了。。。";
                    viewHolder.tvHint.setText(s);
                }
            }
            ViewGroup.LayoutParams params =  viewHolder.cardView.getLayoutParams();
            Point size = new Point();
            ((ShowMp4Activity) context).getWindowManager().getDefaultDisplay().getSize(size);
            params.height = size.y / 4;
            viewHolder.cardView.setLayoutParams(params);
            viewHolder.cardView.setVisibility(View.INVISIBLE);
        }
    }

    private Pair<String, Bitmap> findThumbnailMap(String path) {
        for (Pair<String, Bitmap> p : thumbnailMap) {
            if (p.first.equals(path))
                return p;
        }
        return new Pair<>(null, null);
    }

    @Override
    public int getItemCount() {
        return showfiles.size() + 2;
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

    public void setThumbnailMap(ArrayList<Pair<String, Bitmap>> thumbnailMap) {
        this.thumbnailMap = thumbnailMap;
        showfiles.clear();
        for (Pair<String, Bitmap> p : thumbnailMap) {
            if (p.second != null)
                showfiles.add(p.first);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == showfiles.size() + 1) return 0;
        else return 1;
    }
}
