package zz.app.gif2mp4.activitys;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import zz.app.gif2mp4.R;
import zz.app.gif2mp4.Utils;

public class MainActivity extends AppCompatActivity {
    Button btngif2mp4;
    Button btnmp42gif;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        title( "一个神奇的软件？");
        init();

    }

    private void title(String s) {
        setTitle(s);
    }

    private void init() {
        Utils.welcome();
        createFolder();
        initView();
    }

    private void initView() {
        btngif2mp4=findViewById(R.id.btngif2mp4);
        btngif2mp4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,ShowGifActivity.class);
                startActivity(intent);
            }
        });
        btnmp42gif=findViewById(R.id.btnmp42gif);
        btnmp42gif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,ShowMp4Activity.class);
                startActivity(intent);
            }
        });
    }


    private void createFolder() {
        Utils.makeFolders(this);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
