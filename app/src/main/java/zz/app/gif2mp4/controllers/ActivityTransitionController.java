package zz.app.gif2mp4.controllers;


import android.support.v7.app.AppCompatActivity;

import zz.app.gif2mp4.interfaces.IShowHide;

public class ActivityTransitionController {
    private final IShowHide from;
    private AppCompatActivity to;
    private ShowListener showListener;
    private HideListener hideListener;
    public ActivityTransitionController(IShowHide from) {
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
    public interface ShowListener{
        void onShow(IShowHide from);
    }
    public interface HideListener{
        void onHide(IShowHide from);
    }
}
