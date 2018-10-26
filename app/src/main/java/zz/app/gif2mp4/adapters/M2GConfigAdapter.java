package zz.app.gif2mp4.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import zz.app.gif2mp4.R;

public class M2GConfigAdapter extends RecyclerView.Adapter<M2GConfigAdapter.ViewHolder> {

    private Context context;

    public M2GConfigAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v=View.inflate(context, R.layout.optionsadapterview,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        TextView tvOptionName=viewHolder.itemView.findViewById(R.id.tvOptionName);
        TextView tvOptionValue=viewHolder.itemView.findViewById(R.id.tvOptionValue);
        tvOptionName.setText(String.valueOf(i));
        tvOptionValue.setText(String.valueOf(i));
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
