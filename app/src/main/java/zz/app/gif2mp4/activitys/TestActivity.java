package zz.app.gif2mp4.activitys;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;

import java.util.Objects;

import zz.app.gif2mp4.R;

public class TestActivity extends AppCompatActivity {

    ImageView imageView;
    int w, h, x, y, picw, pich, picx, picy, picnx, picny, picnw, picnh,winx,winy,winh,winw;
    View anchor;
    PopupWindow pw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        anchor = View.inflate(this, R.layout.activity_test, null);
        anchor.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect frame = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
                int statusBarHeight = frame.top;
                int titleBarHeight = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
                winx=0;
                winy=titleBarHeight+statusBarHeight;
                winh=frame.bottom-winy;
                winw=frame.right-frame.left;
                anchor.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                pw = new PopupWindow(winw, winh);
                FrameLayout framelayout=new FrameLayout(TestActivity.this);
                framelayout.addView(imageView);
                pw.setContentView(framelayout);
                FrameLayout.MarginLayoutParams params= (FrameLayout.MarginLayoutParams) imageView.getLayoutParams();
                params.width=picw;
                params.height=pich;
                params.leftMargin=picx;
                params.topMargin=picy-winy;
                imageView.setLayoutParams(params);

                pw.showAtLocation(anchor, Gravity.NO_GRAVITY, winx, winy);
                ValueAnimator animator=new ValueAnimator();
                animator.setDuration(500);
                animator.setFloatValues(0,1);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float v= (float) animation.getAnimatedValue();
                        FrameLayout.MarginLayoutParams params= (FrameLayout.MarginLayoutParams) imageView.getLayoutParams();
                        params.width= (int) ((picnw-picw)*v+picw);
                        params.height= (int) ((picnh-pich)*v+pich);
                        params.leftMargin= (int) ((picnx-picx)*v+picx);
                        params.topMargin= (int) ((picny-picy+winy)*v+picy-winy);
                        imageView.setLayoutParams(params);
                        WindowManager.LayoutParams p=getWindow().getAttributes();
                        p.alpha=v;
                        getWindow().setAttributes(p);

                    }
                });
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ((FrameLayout)pw.getContentView()).removeAllViews();
                        pw.dismiss();
                        pw=null;

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animator.start();
            }
        });
        setContentView(anchor);
        WindowManager.LayoutParams p=getWindow().getAttributes();
        p.alpha=0;
        p.dimAmount=0;
        getWindow().setAttributes(p);

        setTitle("测试页");
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        Bitmap b = getIntent().getParcelableExtra("bitmap");
        imageView = new ImageView(this);
        imageView.setImageDrawable(new BitmapDrawable(getResources(), b));
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        w = size.x - 100;
        h = size.x * 2 / 3;
        picw = getIntent().getIntExtra("picw", 0);
        pich = getIntent().getIntExtra("pich", 0);
        picx = getIntent().getIntExtra("picx", 0);
        picy = getIntent().getIntExtra("picy", 0);
        x = 50;
        y = 0;
        if (pich * w > picw * h) {
            picnh = h;
            picnw = picnh * picw / pich;
            picnx = (w - picnw) / 2 + 50;
            picny = 0;
        } else {
            picnw = w;
            picnh = picnw * pich / picw;
            picnx = 0;
            picny = (h - picnh) / 2;
        }

        // imageView.layout(picnx,picny,picnw,picnh);
    }

    @Override
    protected void onPause() {
        overridePendingTransition(0, 0);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        pw = new PopupWindow(winw, winh);
        FrameLayout framelayout=new FrameLayout(TestActivity.this);
        framelayout.addView(imageView);
        pw.setContentView(framelayout);
        FrameLayout.MarginLayoutParams params= (FrameLayout.MarginLayoutParams) imageView.getLayoutParams();
        params.width=picw;
        params.height=pich;
        params.leftMargin=picx;
        params.topMargin=picy-winy;
        imageView.setLayoutParams(params);

        pw.showAtLocation(anchor, Gravity.NO_GRAVITY, winx, winy);
        ValueAnimator animator=new ValueAnimator();
        animator.setDuration(500);
        animator.setFloatValues(1,0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v= (float) animation.getAnimatedValue();
                FrameLayout.MarginLayoutParams params= (FrameLayout.MarginLayoutParams) imageView.getLayoutParams();
                params.width= (int) ((picnw-picw)*v+picw);
                params.height= (int) ((picnh-pich)*v+pich);
                params.leftMargin= (int) ((picnx-picx)*v+picx);
                params.topMargin= (int) ((picny-picy+winy)*v+picy-winy);
                imageView.setLayoutParams(params);
                WindowManager.LayoutParams p=getWindow().getAttributes();
                p.alpha=v;
                getWindow().setAttributes(p);

            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ((FrameLayout)pw.getContentView()).removeAllViews();
                pw.dismiss();
                pw=null;
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    @Override
    protected void onStart() {
        WindowManager.LayoutParams p=getWindow().getAttributes();
        p.alpha=0;
        p.dimAmount=0;
        getWindow().setAttributes(p);
        super.onStart();
    }
}
