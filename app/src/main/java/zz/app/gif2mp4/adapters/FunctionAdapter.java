package zz.app.gif2mp4.adapters;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import zz.app.gif2mp4.R;
import zz.app.gif2mp4.activitys.MainActivity;
import zz.app.gif2mp4.activitys.ShowGifActivity;
import zz.app.gif2mp4.activitys.ShowMp4Activity;

public class FunctionAdapter extends RecyclerView.Adapter<FunctionAdapter.ViewHolder> {


    private Context context;
    private String[] funtions;
    private int num;

    public FunctionAdapter(Context context) {
        this.context = context;
        funtions = context.getResources().getStringArray(R.array.functions);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(View.inflate(context, R.layout.functionadapterlayout, null));
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        viewHolder.tvIcon.setText(String.valueOf(funtions[i].charAt(0)));
        viewHolder.tvFunctionName.setText(funtions[i]);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                num = viewHolder.getPosition();
                Intent intent = null;
                if (num == 0) {
                    intent = new Intent(context, ShowGifActivity.class);

                } else if (num == 1) {
                    intent = new Intent(context, ShowMp4Activity.class);

                }
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation((MainActivity) context).toBundle();
                context.startActivity(intent, bundle);

            }
        });
    }

    @Override
    public int getItemCount() {
        return funtions.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvIcon;
        ImageView ivIcon;
        TextView tvFunctionName;

        ViewHolder(View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvFunctionName = itemView.findViewById(R.id.tvFunctionName);
        }
    }
}
