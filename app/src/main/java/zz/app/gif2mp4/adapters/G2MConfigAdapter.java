package zz.app.gif2mp4.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import zz.app.gif2mp4.R;
import zz.app.gif2mp4.Utils;
import zz.app.gif2mp4.activitys.GifActivity;

import static android.content.Context.MODE_PRIVATE;


public class G2MConfigAdapter extends RecyclerView.Adapter<G2MConfigAdapter.ViewHolder> {
    private final Context context;
    private final String gifpath;
    private final String[] encodertype = {"H.264", "MPEG4"};
    private int encodertypenum = 0;
    private int gifframes = 0;
    private float gifrate = 0;
    private double predicttime = 0;
    private double outputtime = 0;
    private double bitrate;
    private Handler handler;
    private AlertDialog encoderdlg;
    private boolean showpic = true;
    private boolean ready = false;
    private static final String TAG = "G2MConfigAdapter";

    public int getRotateType() {
        return rotateType;
    }

    private int rotateType = 0;

    public double getScale() {
        return scale;
    }

    private double scale = 1f;
    private int width;
    private int height;
    private String[] rotateText;

    public double getOutgifrate() {
        return outgifrate;
    }

    private double outgifrate=30;

    public int getEncodertypenum() {
        return encodertypenum;
    }

    public void setEncodertypenum(int encodertypenum) {
        this.encodertypenum = encodertypenum;
    }

    public int getGifframes() {
        return gifframes;
    }

    public void setGifframes(int gifframes) {
        this.gifframes = gifframes;
    }

    public float getGifrate() {
        return gifrate;
    }

    public void setGifrate(float gifrate) {
        this.gifrate = gifrate;
    }

    public double getPredicttime() {
        return predicttime;
    }

    public void setPredicttime(double predicttime) {
        this.predicttime = predicttime;
    }

    public double getOutputtime() {
        return outputtime;
    }

    public void setOutputtime(double outputtime) {
        this.outputtime = outputtime;
    }


    public double getBitrate() {
        return bitrate;
    }

    public void setBitrate(double bitrate) {
        this.bitrate = bitrate;
    }

    public G2MConfigAdapter(Context context, String gifpath) {
        this.context = context;
        this.gifpath = gifpath;
        rotateText = context.getResources().getStringArray(R.array.mp4rotationselect);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case 0:
                        if (encoderdlg != null) encoderdlg.dismiss();
                        encoderdlg = null;
                        notifyItemChanged(2);
                        break;

                }
                return false;
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == 1) {
            LinearLayout layout = new LinearLayout(context);
            layout.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            layout.setOrientation(LinearLayout.VERTICAL);
            ImageView v = new ImageView(context);
            Point size = new Point();
            ((GifActivity) context).getWindowManager().getDefaultDisplay().getSize(size);
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, size.x * 2 / 3);
            params.setMargins(60, 0, 60, 0);
            v.setLayoutParams(params);
            Glide.with(context).load(gifpath).asGif().listener(new RequestListener<String, GifDrawable>() {
                @Override
                public boolean onException(Exception e, String s, Target<GifDrawable> target, boolean b) {
                    return false;
                }

                @Override
                public boolean onResourceReady(GifDrawable gifDrawable, String s, Target<GifDrawable> target, boolean b, boolean b1) {
                    return false;
                }
            }).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(v);
            v.setImageAlpha(0);
            layout.addView(v);
            TextView v2 = new TextView(context);
            v2.setTextColor(Color.BLACK);
            v2.setTextSize(16);
            v2.setPadding(0, 15, 0, 25);
            v2.setText(new File(gifpath).getName());
            v2.setGravity(Gravity.CENTER);
            v2.setVisibility(View.INVISIBLE);
            layout.addView(v2);
            ViewHolder viewHolder = new ViewHolder(layout);
            viewHolder.ivgifpreview = v;
            viewHolder.tvgifname = v2;
            return viewHolder;
        } else {
            View v = View.inflate(context, R.layout.optionsadapterview, null);
            ViewHolder viewHolder = new ViewHolder(v);
            viewHolder.tvoptionname = v.findViewById(R.id.tvOptionName);
            viewHolder.tvoptionvalue = v.findViewById(R.id.tvOptionValue);
            return viewHolder;
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return 1;
        else return 2;

    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        try {
            if (i == 0) {
                if (!showpic) viewHolder.ivgifpreview.setImageAlpha(0);
                else viewHolder.ivgifpreview.setImageAlpha(255);
            } else if (i == 1) {
                viewHolder.itemView.setClickable(false);
                viewHolder.tvoptionvalue.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                viewHolder.tvoptionname.setText("详细信息");
                if (gifframes == 0)
                    viewHolder.tvoptionvalue.setText("?");
                else {
                    String s = String.format(Locale.getDefault(), "帧数:%d\n时长:%.2fs\n大小:%s", gifframes, predicttime, Utils.size2String(FileUtils.sizeOf(new File(gifpath))));
                    viewHolder.tvoptionvalue.setText(s);
                }

            } else if (i == 2) {
                final ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                params.setMargins(0, 50, 0, 0);
                viewHolder.itemView.setLayoutParams(params);
                viewHolder.tvoptionname.setText("编码器(Mp4）");
                viewHolder.tvoptionvalue.setText(encodertype[encodertypenum]);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LinearLayout group = new LinearLayout(context);
                        group.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.MarginLayoutParams params1 = new LinearLayout.MarginLayoutParams(LinearLayout.MarginLayoutParams.MATCH_PARENT, LinearLayout.MarginLayoutParams.WRAP_CONTENT);
                        TypedValue value = new TypedValue();
                        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, value, true);
                        LinearLayout l1 = new LinearLayout(context);
                        l1.setBackgroundResource(value.resourceId);
                        RadioButton r1 = new RadioButton(context);
                        r1.setBackground(null);
                        r1.setClickable(false);
                        l1.setPadding(30, 30, 30, 30);
                        l1.addView(r1);
                        if (encodertypenum == 0) {
                            r1.setChecked(true);
                        } else
                            r1.setChecked(false);
                        String s1 = encodertype[0] + "(朋友圈)";
                        r1.setText(s1);
                        l1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                encodertypenum = 0;
                                handler.obtainMessage(0).sendToTarget();
                            }
                        });
                        LinearLayout l2 = new LinearLayout(context);
                        l2.setBackgroundResource(value.resourceId);
                        RadioButton r2 = new RadioButton(context);
                        r2.setBackground(null);
                        r2.setClickable(false);
                        l2.setPadding(30, 30, 30, 30);
                        l2.addView(r2);
                        if (encodertypenum == 1) {
                            r2.setChecked(true);
                        } else
                            r2.setChecked(false);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            Spanned s = Html.fromHtml(encodertype[1] + "(<del>朋友圈</del>)", Html.FROM_HTML_MODE_COMPACT);
                            r2.setText(s);
                        } else {
                            String s = encodertype[1] + "(朋友圈×)";
                            r2.setText(s);
                        }

                        l2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                encodertypenum = 1;
                                handler.obtainMessage(0).sendToTarget();
                            }
                        });
                        group.addView(l1);
                        group.addView(l2);
                        encoderdlg = new AlertDialog.Builder(context).setTitle("编码方式").setView(group).show();
                    }
                });


            } else if (i == 3) {
                viewHolder.tvoptionname.setText("比特率(Mp4）");
                String s = Utils.bitrate2String(bitrate);
                viewHolder.tvoptionvalue.setText(s);
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
                                    SharedPreferences preferences = context.getSharedPreferences("gif2mp4", MODE_PRIVATE);
                                    preferences.edit().putInt("defaultbitrate", (int) bitrate).apply();
                                    notifyItemChanged(3);
                                }
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
                    }
                });
            } else if (i == 4) {
                viewHolder.tvoptionname.setText("持续时间");
                if (outputtime == 0)
                    viewHolder.tvoptionvalue.setText("?");
                else {
                    BigDecimal decimal = new BigDecimal(predicttime / outputtime).setScale(2, RoundingMode.HALF_UP);
                    String s = String.valueOf(outputtime) + "s(" + decimal.doubleValue() + "×)";
                    viewHolder.tvoptionvalue.setText(s);
                }
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View view1 = View.inflate(context, R.layout.gifsettimelayout, null);
                        final EditText editText = view1.findViewById(R.id.ettime);
                        new AlertDialog.Builder(context).setTitle("持续时间").setView(view1).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    double b = Double.parseDouble(editText.getText().toString());
                                    if (b > 0) {
                                        outputtime = b;
                                        notifyItemChanged(4);

                                    } else {
                                        Toast.makeText(context, "无效的时间", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception ignored) {
                                    Toast.makeText(context, "无效的时间", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
                    }
                });
            } else if (i == 5) {
                viewHolder.tvoptionname.setText("输出尺寸");
                String s = "";
                if (rotateType == 0 || rotateType == 2) {
                    s = (int) (width * scale) + "x" + (int) (height * scale);

                } else {
                    s = (int) (height * scale) + "x" + (int) (width * scale);

                }
                s += "(" + new BigDecimal(scale).setScale(2, RoundingMode.HALF_UP).toString() + "x)";
                viewHolder.tvoptionvalue.setText(s);
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
                                        scale = Double.parseDouble(str);
                                        notifyItemChanged(5);
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
            } else if (i == 6) {
                viewHolder.tvoptionname.setText("输出帧率(Mp4)");
                String s = new BigDecimal(outgifrate).setScale(2, RoundingMode.HALF_UP).toString();
                viewHolder.tvoptionvalue.setText(s);
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
                                        outgifrate = f;
                                        notifyItemChanged(6);
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
            } else if (i == 7) {
                viewHolder.tvoptionname.setText("旋转角度");
                viewHolder.tvoptionvalue.setText(rotateText[rotateType]);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListView listView = new ListView(context);
                        G2MConfigAdapter.RotateTypeAdapter adapter = new G2MConfigAdapter.RotateTypeAdapter(context);
                        listView.setDividerHeight(0);
                        listView.setAdapter(adapter);
                        final AlertDialog dialog = new AlertDialog.Builder(context).setView(listView).setTitle("输出旋转角度设置").create();
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                rotateType = position;
                                notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        });
                        dialog.show();


                    }
                });
            }
        }catch (Exception ignored){
            Toast.makeText(context, "发生异常", Toast.LENGTH_SHORT).show();
            ((GifActivity)context).finish();
        }
    }

    @Override
    public int getItemCount() {
        return 8;
    }

    public void setGifOptions(int frames, long endpts) {
        gifframes = frames;
        Log.e(TAG, "setGifOptions: endpts="+endpts );
        predicttime= (double)endpts/100;
        gifrate = (frames*100/endpts);
        outputtime = predicttime;
        notifyDataSetChanged();
    }


    public void setShowpic(boolean showpic) {
        this.showpic = showpic;
    }

    public boolean getready() {
        return ready;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivgifpreview;
        TextView tvgifname;
        TextView tvoptionname;
        TextView tvoptionvalue;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    class RotateTypeAdapter extends BaseAdapter {

        Context context;

        RotateTypeAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return rotateText.length;
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
            button.setText(rotateText[position]);
            if (rotateType == position) {
                button.setChecked(true);
            }
            return view;
        }
    }
}
