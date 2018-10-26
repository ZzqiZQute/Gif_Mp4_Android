package zz.app.gif2mp4.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import zz.app.gif2mp4.R;
import zz.app.gif2mp4.interfaces.OnMp4ProgressSliderListener;

public class Mp4ProgressSliderView extends View {
    boolean DEBUG=false;
    final Paint paint;
    int progressColor;
    int boundColor;
    static final int SCALE = 1000000;
    int width;

    public int getLbound() {
        return lbound;
    }

    public int getRbound() {
        return rbound;
    }

    int lbound = 0;
    int rbound = SCALE;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
        invalidate();
    }

    int current=0;
    final int totalheight = 210;
    int boundWidth = 40;
    final int height = 50;
    final int roundx = 25;
    final int roundy = 25;
    final int pinwidth = 5;
    final int boundpadding=25;
    Path leftBoundPath, rightBoundPath,barPath;
    Region leftBoundRegion, rightBoundRegion,barRegion;
    private int offsetX;
    private final int offsetY;

    public void setListener(OnMp4ProgressSliderListener listener) {
        this.listener = listener;
    }

    OnMp4ProgressSliderListener listener;
    enum State {
        IDLE,
        LBOUND,
        RBOUND,
        PROGRESS
    }

    State state = State.IDLE;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Mp4ProgressSliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Mp4ProgressSliderView);
        progressColor = array.getColor(R.styleable.Mp4ProgressSliderView_progressColor, Color.BLUE);
        boundColor = array.getColor(R.styleable.Mp4ProgressSliderView_boundColor, Color.BLUE);
        offsetY=array.getInteger(R.styleable.Mp4ProgressSliderView_topoffset,0);
        array.recycle();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        leftBoundPath = new Path();
        rightBoundPath = new Path();
        barPath=new Path();
        leftBoundRegion=new Region();
        rightBoundRegion=new Region();
        barRegion=new Region();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int hmode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (hmode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(width, totalheight);
        } else {
            setMeasuredDimension(width, height);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = getWidth();
        barPath.reset();
        barPath.addRoundRect(boundWidth,offsetY+(totalheight - height) / 2, width - boundWidth, offsetY+height + (totalheight - height) / 2, roundx, roundy,Path.Direction.CW);
        barRegion.set(boundWidth-boundpadding,offsetY+(totalheight - height) / 2-boundpadding, width - boundWidth+boundpadding, offsetY+height + (totalheight - height) / 2+boundpadding);
        drawBar(canvas);
        drawProgress(canvas);
        drawBound(canvas);



    }

    private void drawProgress(Canvas canvas) {
        canvas.save();
        canvas.clipPath(barPath);
        paint.setColor(progressColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(boundWidth,
                offsetY+(totalheight - height) / 2,
               boundWidth+current*(width-2*boundWidth)/SCALE,
                offsetY+height + (totalheight - height) / 2,
                paint);
        canvas.restore();

    }

    private void drawBound(Canvas canvas) {
        paint.reset();
        paint.setStrokeWidth(3);
        leftBoundPath.reset();
        leftBoundPath.moveTo(boundWidth + lbound * (width - 2 * boundWidth) / SCALE, offsetY+(totalheight - height) / 2);
        leftBoundPath.rLineTo(0, 2 * height);
        leftBoundPath.rLineTo(-boundWidth, 0);
        leftBoundPath.rLineTo(boundWidth - pinwidth, -boundWidth + pinwidth);
        leftBoundPath.rLineTo(0, -2 * height + boundWidth - pinwidth);
        leftBoundPath.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(boundColor);
        canvas.drawPath(leftBoundPath, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(changeColor(boundColor));
        canvas.drawPath(leftBoundPath, paint);
        leftBoundRegion.set(lbound * (width - 2 * boundWidth) / SCALE-boundpadding,offsetY+(totalheight - height) / 2-boundpadding,
                lbound * (width - 2 * boundWidth) / SCALE+ boundWidth,offsetY+2 * height+(totalheight - height) / 2+boundpadding);
        if(DEBUG) {
            paint.setColor(Color.GREEN);
            canvas.drawRect(leftBoundRegion.getBounds(), paint);
        }
        rightBoundPath.reset();
        rightBoundPath.moveTo(boundWidth + rbound * (width - 2 * boundWidth) / SCALE, offsetY+(totalheight - height) / 2);
        rightBoundPath.rLineTo(0, 2 * height);
        rightBoundPath.rLineTo(boundWidth, 0);
        rightBoundPath.rLineTo(-boundWidth + pinwidth, -boundWidth + pinwidth);
        rightBoundPath.rLineTo(0, -2 * height + boundWidth - pinwidth);
        rightBoundPath.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(boundColor);
        canvas.drawPath(rightBoundPath, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(changeColor(boundColor));
        canvas.drawPath(rightBoundPath, paint);
        rightBoundRegion.set(rbound * (width - 2 * boundWidth) / SCALE+boundWidth,offsetY+(totalheight - height) / 2-boundpadding,
                rbound * (width - 2 * boundWidth) / SCALE+2*boundWidth+boundpadding,offsetY+2 * height+(totalheight - height) / 2+boundpadding);
        if(DEBUG) {
            paint.setColor(Color.GREEN);
            canvas.drawRect(rightBoundRegion.getBounds(), paint);
        }
    }

    private int changeColor(int color) {
        float[] f = new float[3];
        Color.colorToHSV(color, f);
        f[1] *= 0.3;
        return Color.HSVToColor(f);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void drawBar(Canvas canvas) {
        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        canvas.drawPath(barPath,paint);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(barPath,paint);
        if(DEBUG){
            paint.setColor(Color.YELLOW);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(barRegion.getBounds(),paint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x= (int) event.getX();
        int y= (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if(leftBoundRegion.contains(x,y)){
                    offsetX=leftBoundRegion.getBounds().right-boundpadding-x;
                    state= State.LBOUND;
                    if(listener!=null){
                        listener.onSelectorDown(OnMp4ProgressSliderListener.Selector.LEFT);
                    }
                }else if(rightBoundRegion.contains(x,y)){
                    offsetX=x-rightBoundRegion.getBounds().left-boundpadding;
                    state= State.RBOUND;
                    if(listener!=null){
                        listener.onSelectorDown(OnMp4ProgressSliderListener.Selector.RIGHT);
                    }
                }else if(barRegion.contains(x,y)){
                    offsetX=boundpadding;
                    state= State.PROGRESS;
                    if(listener!=null){
                        listener.onSliderDown();
                    }
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                switch (state){
                    case LBOUND:
                        lbound=(x+offsetX)*SCALE/width;
                        if(lbound>rbound)
                            lbound=rbound-1;
                        else if(lbound<0)
                            lbound=0;
                        if(listener!=null){
                            listener.onSelectorMoving(OnMp4ProgressSliderListener.Selector.LEFT,lbound);
                        }
                        break;
                    case RBOUND:
                        rbound=(x-offsetX)*SCALE/width;
                        if(rbound<lbound)
                            rbound=lbound+1;
                        else if(rbound>SCALE)
                            rbound=SCALE;
                        if(listener!=null){
                            listener.onSelectorMoving(OnMp4ProgressSliderListener.Selector.RIGHT,rbound);
                        }
                        break;
                    case PROGRESS:
                        current=(x-offsetX)*SCALE/width;
                        if(current<0)
                            current=0;
                        else if(current>SCALE)
                            current=SCALE;
                        if(listener!=null){
                            listener.onSliderMoving(current);
                        }
                        break;

                }
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                switch (state){
                    case LBOUND:
                        if(listener!=null){
                            listener.onSelectorUp(OnMp4ProgressSliderListener.Selector.LEFT,lbound);
                        }
                        break;
                    case RBOUND:
                        if(listener!=null){
                            listener.onSelectorUp(OnMp4ProgressSliderListener.Selector.RIGHT,rbound);
                        }
                        break;
                    case PROGRESS:
                        if(listener!=null){
                            listener.onSliderUp(current);
                        }
                        break;
                }
                state = State.IDLE;
                return true;
        }
        return false;
    }

}
