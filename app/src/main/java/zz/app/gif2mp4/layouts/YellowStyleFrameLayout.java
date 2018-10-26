package zz.app.gif2mp4.layouts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import zz.app.gif2mp4.R;

public class YellowStyleFrameLayout extends FrameLayout {
    int a = 80;
    Paint paint;
    private static final String TAG = "FrameLayout1";

    public YellowStyleFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getContext().getColor(R.color.colorPrimary));
        canvas.drawCircle(width / 2, a - width * width / 4 / a, a + width * width / 4 / a, paint);
    }

}
