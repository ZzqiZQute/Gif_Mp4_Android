package zz.app.gif2mp4.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

import zz.app.gif2mp4.R;
import zz.app.gif2mp4.Utils;

import static android.content.Context.MODE_PRIVATE;

public class M2GConfigAdapter extends RecyclerView.Adapter<M2GConfigAdapter.ViewHolder> {

    private Context context;
    private String[] mp4convertoptions;
    private String[] mp4rotationselect;
    private double realTime;

    public void setOutwidth(int outwidth) {
        this.outwidth = outwidth;
    }

    public void setOutheight(int outheight) {
        this.outheight = outheight;
    }

    private int outwidth, outheight;
    private int lastRotateType = 4;
    private double fps = -1;
    private double outfps;
    private int width, height;
    private double bitrate;
    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    private double scale=1f;

    private int rotateType = 0;
    private long[] mp4info;
    private long framecnt;

    public double getOutputTimeScale() {
        return outputTimeScale;
    }

    private double outputTimeScale=1f;

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
                    str += bigDecimal.setScale(2, RoundingMode.HALF_UP);
                }
                break;
            case 1:
                str = (int)(outwidth*scale) + "x" + (int)(outheight*scale)+"("+new BigDecimal(scale).setScale(2,RoundingMode.HALF_UP).toString()+"x)";
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View view = View.inflate(context, R.layout.mp4setscaledialoglayout, null);
                        final EditText etScale = view.findViewById(R.id.etScale);
                        AlertDialog dialog = new AlertDialog.Builder(context).setView(view).setTitle("大小缩放设置").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String str = etScale.getText().toString();
                                if (!str.isEmpty()) {
                                    try {
                                        scale= Double.parseDouble(str);
                                        notifyItemChanged(1);
                                        dialog.dismiss();
                                    } catch (NumberFormatException ignored) {
                                            Toast.makeText(context, "输入的值无效", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(context, "输入的值无效", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).setNegativeButton("取消", null).create();
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
                        View view = View.inflate(context, R.layout.setframedialoglayout, null);
                        final EditText etFps = view.findViewById(R.id.etfps);
                        AlertDialog dialog = new AlertDialog.Builder(context).setView(view).setTitle("输出帧率设置").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                try {
                                    String s = etFps.getText().toString();
                                    double f = Double.parseDouble(s);
                                    if (f > 30) {
                                        Toast.makeText(context, "输入的值无效", Toast.LENGTH_SHORT).show();
                                    } else {
                                        outfps = f;
                                        notifyItemChanged(2);
                                    }
                                    dialog.dismiss();
                                } catch (NumberFormatException ignored) {
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
                        ListView listView = new ListView(context);
                        RotateTypeAdapter adapter = new RotateTypeAdapter(context);
                        listView.setDividerHeight(0);
                        listView.setAdapter(adapter);
                        final AlertDialog dialog = new AlertDialog.Builder(context).setView(listView).setTitle("输出旋转角度设置").create();
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                rotateType = position;
                                if (lastRotateType != rotateType) {
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
            case 4:
                str= Utils.bitrate2String(bitrate);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View view1 = View.inflate(context, R.layout.setbitratedialoglayout, null);
                        final EditText editText = view1.findViewById(R.id.etbitrate);
                        new AlertDialog.Builder(context).setTitle("比特率").setView(view1).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                double b = Utils.string2Bitrate(editText.getText().toString());
                                if (b == -1) {
                                    Toast.makeText(context, "无效的比特率", Toast.LENGTH_SHORT).show();
                                } else {
                                    bitrate = b;
                                    SharedPreferences preferences = context.getSharedPreferences("mp42gif", MODE_PRIVATE);
                                    preferences.edit().putInt("defaultbitrate", (int) bitrate).apply();
                                    notifyItemChanged(4);
                                }
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
                    }
                });
                break;
            case 5:
                str= Utils.mills2Str((long) (realTime*1000*outputTimeScale))+"("+new BigDecimal(outputTimeScale).setScale(2,RoundingMode.HALF_UP).toString()+"x)";
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View view1 = View.inflate(context, R.layout.mp4settimeorscaledialoglayout, null);
                        final EditText etScale = view1.findViewById(R.id.etScale);
                        final EditText etTime = view1.findViewById(R.id.ettime);
                        AlertDialog dialog = new AlertDialog.Builder(context).setView(view1).setTitle("时间设置").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String str = etTime.getText().toString();
                                if (!str.isEmpty()) {
                                    try {
                                        double temp=Double.parseDouble(str)/realTime;
                                        if(temp>2||temp<0.5) {
                                            Toast.makeText(context, "输入的范围无效", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                            return;
                                        }
                                        outputTimeScale= temp;
                                        notifyItemChanged(5);
                                        dialog.dismiss();
                                    } catch (NumberFormatException ignored) {
                                        Toast.makeText(context, "输入的值无效", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    String str2=etScale.getText().toString();
                                    if (!str2.isEmpty()) {
                                        try {
                                            double temp=Double.parseDouble(str2);
                                            if(temp>2||temp<0.5) {
                                                Toast.makeText(context, "输入的范围无效", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                                return;
                                            }
                                            outputTimeScale= temp;
                                            notifyItemChanged(5);
                                            dialog.dismiss();
                                        } catch (NumberFormatException ignored) {
                                            Toast.makeText(context, "输入的值无效", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(context, "输入的值无效", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }).setNegativeButton("取消", null).create();
                        dialog.show();

                    }
                });
                break;

        }
        tvOptionValue.setText(str);
    }

    public double getBitrate() {
        return bitrate;
    }

    public void setBitrate(double bitrate) {
        this.bitrate = bitrate;
    }

    public void setRealTime(double realTime) {
        this.realTime = realTime;
    }

    public double getRealTime() {
        return realTime;
    }

    class RotateTypeAdapter extends BaseAdapter {

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
                    View view = View.inflate(context, R.layout.setrotatelistviewadapterlayout, null);
            RadioButton button = view.findViewById(R.id.rbrotatetype);
            button.setText(mp4rotationselect[position]);
            if (rotateType == position) {
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
