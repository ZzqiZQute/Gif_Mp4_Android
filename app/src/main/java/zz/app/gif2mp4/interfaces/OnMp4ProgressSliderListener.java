package zz.app.gif2mp4.interfaces;


public interface OnMp4ProgressSliderListener {
    enum Selector{
        LEFT,
        RIGHT
    }
    void onSliderDown();
    void onSliderMoving(int value);
    void onSliderUp(int value);
    void onSelectorDown(Selector which);
    void onSelectorMoving(Selector which, int value);
    void onSelectorUp(Selector which, int value);
}
