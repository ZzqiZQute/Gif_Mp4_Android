package zz.app.gif2mp4.activitys;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.TransitionInflater;
import android.view.Window;
import android.view.WindowManager;

import java.util.Objects;

import zz.app.gif2mp4.R;
import zz.app.gif2mp4.Utils;
import zz.app.gif2mp4.adapters.M2GConfigAdapter;

public class Mp42GifActivity extends AppCompatActivity {

    M2GConfigAdapter adapter;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_mp4_to_gif);
        Utils.getManager().mp42gifhandler.setTo(this);
        init();

    }

    private void init() {
        initAnimation();
        initView();
    }

    private void initAnimation() {
        Fade fade= (Fade) TransitionInflater.from(this).inflateTransition(R.transition.fade);
        getWindow().setEnterTransition(fade);
    }


    private void initView() {
        adapter=new M2GConfigAdapter(this);
        recyclerView=findViewById(R.id.rvconfig);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        Utils.getManager().mp42gifhandler.show();
        super.onBackPressed();
    }
}
