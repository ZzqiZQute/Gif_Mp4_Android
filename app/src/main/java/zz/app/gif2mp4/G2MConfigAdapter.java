package zz.app.gif2mp4;

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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;


public class G2MConfigAdapter extends RecyclerView.Adapter<G2MConfigAdapter.ViewHolder> {
    private final Context context;
    private final String gifpath;
    private final String[] encodertype = {"H.264(朋友圈)", "MPEG4"};
    private int encodertypenum = 0;
    private int gifframes = 0;
    private float gifrate = 0;
    private double predicttime = 0;
    private double outputtime = 0;
    private double bitrate ;
    private Handler handler;
    private AlertDialog encoderdlg;
    private boolean showpic=true;
    private  boolean ready=false;
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
            ((Gif2Mp4Activity) context).getWindowManager().getDefaultDisplay().getSize(size);
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
            layout.addView(v2);
            ViewHolder viewHolder = new ViewHolder(layout);
            viewHolder.ivgifpreview = v;
            viewHolder.tvgifname = v2;
            return viewHolder;
        } else {
            View v = View.inflate(context, R.layout.g2moptionsadapterview, null);
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
        if(i==0){
            if(!showpic)viewHolder.ivgifpreview.setImageAlpha(0);
            else viewHolder.ivgifpreview.setImageAlpha(255);
        }
        else if (i == 1) {
            viewHolder.itemView.setClickable(false);
            viewHolder.tvoptionvalue.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            viewHolder.tvoptionname.setText("详细信息");
            if (gifframes == 0)
                viewHolder.tvoptionvalue.setText("???");
            else {
                String s = String.format(Locale.getDefault(), "帧数:%d\n平均帧率:%.1f/s\n预计时长:%.1fs", gifframes, gifrate, gifframes / gifrate);
                viewHolder.tvoptionvalue.setText(s);
            }

        } else if (i == 2) {
            final ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
            params.setMargins(0, 50, 0, 0);
            viewHolder.itemView.setLayoutParams(params);
            viewHolder.tvoptionname.setText("编码器");
            if (encodertypenum == 0)
                viewHolder.tvoptionvalue.setText(encodertype[encodertypenum]);
            else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Spanned s = Html.fromHtml(encodertype[1] + "(<del>朋友圈</del>)", Html.FROM_HTML_MODE_COMPACT);
                viewHolder.tvoptionvalue.setText(s);
            } else {
                String s = encodertype[1] + "(朋友圈×)";
                viewHolder.tvoptionvalue.setText(s);
            }
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
                    r1.setText(encodertype[0]);
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
            viewHolder.tvoptionname.setText("比特率");
            String s = Utils.bitrate2String(bitrate);
            viewHolder.tvoptionvalue.setText(s);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View view1 = View.inflate(context, R.layout.bitratesettinglayout, null);
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
                viewHolder.tvoptionvalue.setText("???");
            else {
                BigDecimal decimal = new BigDecimal(predicttime / outputtime).setScale(2, RoundingMode.HALF_UP);
                String s = String.valueOf(outputtime) + "s(" + decimal.doubleValue() + "×)";
                viewHolder.tvoptionvalue.setText(s);
            }
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View view1 = View.inflate(context, R.layout.timesettinglayout, null);
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
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    public void setGifOptions(int frames, float rate) {
        gifframes = frames;
        gifrate = rate;
        predicttime = new BigDecimal(gifframes / gifrate).setScale(1, RoundingMode.HALF_UP).doubleValue();
        outputtime = predicttime;
    }


    public void setShowpic(boolean showpic) {
        this.showpic = showpic;
    }

    public boolean getready() {
        return ready;
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
}