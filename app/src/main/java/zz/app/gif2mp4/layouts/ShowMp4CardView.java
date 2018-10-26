package zz.app.gif2mp4.layouts;

import android.content.Context;
import android.graphics.Region;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class ShowMp4CardView extends CardView {
    public ShowMp4CardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        RelativeLayout relativeLayout= (RelativeLayout) getChildAt(0);
        if(relativeLayout!=null){
            ImageButton button= (ImageButton) relativeLayout.getChildAt(2);
            if(button!=null){
                Region region=new Region(button.getLeft(),button.getTop(),button.getRight(),button.getRight());
                return !region.contains((int) ev.getX(), (int) ev.getY());
            }
        }
        return true;
    }
}
