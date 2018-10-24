package zz.app.gif2mp4.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

public class ScaleImgView extends View {
    private final int orih;
    private final int oriw;

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    private Drawable drawable;
    Paint paint;
    Rect in;
    Rect out;

    private static final String TAG = "ScaleImgView";

    public ScaleImgView(Context context, int w, int h) {
        super(context);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        in = new Rect();
        out = new Rect();
        oriw = w;
        orih = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawable != null) {
            int dw = drawable.getIntrinsicWidth();
            int dh = drawable.getIntrinsicHeight();
            int w = getWidth();
            int h = getHeight();
            int offsetx, offsety, width, height;

            if (dw * orih > dh * oriw) {
                in.set((dw - oriw * dh / orih) / 2, 0, oriw * dh / orih + (dw - oriw * dh / orih) / 2, dh);
            } else {
                in.set(0, (dh - orih * dw / oriw) / 2, dw, (dh - orih * dw / oriw) / 2 + orih * dw / oriw);
            }
            if (oriw * h > orih * w) {
                offsetx = 0;
                offsety = (h - orih * w / oriw) / 2;
                width = w;
                height = orih * w / oriw;
            } else {
                offsetx = (w - oriw * h / orih) / 2;
                offsety = 0;
                width = oriw * h / orih;
                height = h;
            }
            out.set(offsetx, offsety, width + offsetx, height + offsety);
            canvas.drawBitmap(((BitmapDrawable) drawable).getBitmap(), in, out, paint);


        }

    }
}
