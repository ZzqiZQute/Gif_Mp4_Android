package zz.app.gif2mp4.controllers;


import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;

import zz.app.gif2mp4.interfaces.IGoBack;

public class ActivityTransitionController {
    private final IGoBack from;
    private AppCompatActivity to;
    private ShowListener showListener;
    private HideListener hideListener;
    private Bitmap bitmap;
    public ActivityTransitionController(IGoBack from) {
        this.from=from;
    }

    public void setShowListener(ShowListener showListener) {
        this.showListener = showListener;
    }

    public void setHideListener(HideListener hideListener) {
        this.hideListener = hideListener;
    }

    public void setTo(AppCompatActivity to) {
        this.to = to;
    }
    public void show(){
        if(showListener!=null){
            showListener.onShow(from);
        }
    }
    public void hide(){
        if(hideListener!=null){
            hideListener.onHide(from);
        }
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public interface ShowListener{
        void onShow(IGoBack from);
    }
    public interface HideListener{
        void onHide(IGoBack from);
    }
}
