package zz.app.gif2mp4.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.zip.DataFormatException;

import zz.app.gif2mp4.R;
import zz.app.gif2mp4.Utils;

public class M2GConfigAdapter extends RecyclerView.Adapter<M2GConfigAdapter.ViewHolder> {

    private Context context;
    private String[] mp4convertoptions;
    private String[] mp4rotationselect;
    private int outwidth, outheight;
    private int lastRotateType=4;
    private double fps = -1;
    private double outfps;
    private int width, height;
    private int rotateType = 0;
    private long[] mp4info;
    private long framecnt;

    public int getOutwidth() {
        return outwidth;
    }

    public int getOutheight() {
        return outheight;
    }

    public double getOutfps() {
        return outfps;
    }

    public int getRotateType() {
        return rotateType;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    private long filesize;

    public M2GConfigAdapter(Context context) {
        this.context = context;
        mp4convertoptions = context.getResources().getStringArray(R.array.mp4convertoptions);
        mp4rotationselect = context.getResources().getStringArray(R.array.mp4rotationselect);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = View.inflate(context, R.layout.optionsadapterview, null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        final TextView tvOptionName = viewHolder.itemView.findViewById(R.id.tvOptionName);
        TextView tvOptionValue = viewHolder.itemView.findViewById(R.id.tvOptionValue);
        tvOptionName.setText(mp4convertoptions[i]);
        String str = "";
        switch (i) {
            case 0:
                tvOptionValue.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                if (mp4info == null)
                    str = "";
                else {
                    str = "大小:" + Utils.size2String(filesize);
                    str += " 帧总数:" + framecnt;
                    str += "\n";
                    str += " 尺寸:" + width + "x" + height;
                    str += " 帧率:";
                    BigDecimal bigDecimal = new BigDecimal(fps);
                    str += bigDecimal.setScale(3, RoundingMode.HALF_UP);
                }
                break;
            case 1:
                str = outwidth + "x" + outheight;
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View view=View.inflate(context,R.layout.mp4setscaledialoglayout,null);
                        TextView tvOriWidth=view.findViewById(R.id.tvOriWidth);
                        TextView tvOriHeight=view.findViewById(R.id.tvOriHeight);
                        final EditText etOutWidth=view.findViewById(R.id.etOutWidth);
                        final EditText etOutHeight=view.findViewById(R.id.etOutHeight);
                        final EditText etScale=view.findViewById(R.id.etScale);
                        tvOriWidth.setText(String.valueOf(width));
                        tvOriHeight.setText(String.valueOf(height));
                        AlertDialog dialog=new AlertDialog.Builder(context).setView(view).setTitle("输出尺寸设置").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String str=etScale.getText().toString();
                                if(!str.isEmpty()){
                                    try{
                                        Double d=Double.parseDouble(str);
                                        outheight= (int) (height*d);
                                        outwidth= (int) (width*d);
                                        notifyItemChanged(1);
                                        dialog.dismiss();

                                    }catch (NumberFormatException ignored) {
                                        try {
                                            String s = etOutWidth.getText().toString();
                                            int w = Integer.parseInt(s);
                                            s = etOutHeight.getText().toString();
                                            int h = Integer.parseInt(s);
                                            outwidth = w;
                                            outheight = h;
                                            notifyItemChanged(1);
                                            dialog.dismiss();

                                        } catch (NumberFormatException ignored2) {
                                            Toast.makeText(context, "输入的值无效", Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                }else {
                                    try {
                                        String s = etOutWidth.getText().toString();
                                        int w = Integer.parseInt(s);
                                        s = etOutHeight.getText().toString();
                                        int h = Integer.parseInt(s);
                                        outwidth = w;
                                        outheight = h;
                                        notifyItemChanged(1);
                                        dialog.dismiss();

                                    } catch (NumberFormatException ignored) {
                                        Toast.makeText(context, "输入的值无效", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            }
                        }).setNegativeButton("取消",null).create();
                        dialog.show();

                    }
                });
                break;
            case 2:
                if (outfps == -1)
                    str = "?";
                else
                    str = String.valueOf(outfps);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View view=View.inflate(context,R.layout.mp4setframedialoglayout,null);
                        final EditText etFps=view.findViewById(R.id.etfps);
                        final EditText etOutHeight=view.findViewById(R.id.etOutHeight);
                        AlertDialog dialog=new AlertDialog.Builder(context).setView(view).setTitle("输出帧率设置").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                try{
                                    String s=etFps.getText().toString();
                                    double f= Double.parseDouble(s);
                                    if(f>60){
                                        Toast.makeText(context, "输入的值无效", Toast.LENGTH_SHORT).show();
                                    }else{
                                        outfps=f;
                                        notifyItemChanged(2);
                                    }
                                    dialog.dismiss();
                                }catch (NumberFormatException ignored) {
                                    Toast.makeText(context, "输入的值无效", Toast.LENGTH_SHORT).show();

                                }
                            }
                        }).setNegativeButton("取消", null).create();
                        dialog.show();

                    }
                });
                break;
            case 3:
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListView listView=new ListView(context);
                        RotateTypeAdapter adapter=new RotateTypeAdapter(context);
                        listView.setDividerHeight(0);
                        listView.setAdapter(adapter);
                        final AlertDialog dialog=new AlertDialog.Builder(context).setView(listView).setTitle("输出旋转角度设置").create();
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                rotateType=position;
                                if(lastRotateType!=rotateType) {
                                    if (lastRotateType % 2 == 0) {
                                        if (rotateType == 1 || rotateType == 3) {
                                            int temp = outwidth;
                                            outwidth = outheight;
                                            outheight = temp;
                                            notifyItemChanged(1);
                                        }
                                    } else {
                                        if (rotateType == 0 || rotateType == 2) {
                                            int temp = outwidth;
                                            outwidth = outheight;
                                            outheight = temp;
                                            notifyItemChanged(1);
                                        }
                                    }
                                    lastRotateType = rotateType;
                                }
                                notifyItemChanged(3);
                                dialog.dismiss();
                            }
                        });
                        dialog.show();


                    }
                });
                str = mp4rotationselect[rotateType];
                break;

        }
        tvOptionValue.setText(str);
    }
    class RotateTypeAdapter extends BaseAdapter{

        Context context;

        RotateTypeAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return mp4rotationselect.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            @SuppressLint("ViewHolder") //No Scroll
            View view=View.inflate(context,R.layout.mp4setrotatelistviewadapterlayout,null);
            RadioButton button=view.findViewById(R.id.rbrotatetype);
            button.setText(mp4rotationselect[position]);
            if(rotateType==position){
                button.setChecked(true);
            }
            return view;
        }
    }
    @Override
    public int getItemCount() {
        return mp4convertoptions.length;
    }

    public void setMp4Info(long[] mp4info) {
        this.mp4info = mp4info;
        outwidth = width = (int) mp4info[0];
        outheight = height = (int) mp4info[1];
        framecnt = mp4info[2];
        outfps = fps = (double) mp4info[3] / 1000;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
