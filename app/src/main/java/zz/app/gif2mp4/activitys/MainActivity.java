package zz.app.gif2mp4.activitys;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.Objects;

import zz.app.gif2mp4.R;
import zz.app.gif2mp4.Utils;
import zz.app.gif2mp4.adapters.FunctionAdapter;

public class MainActivity extends AppCompatActivity {
    FunctionAdapter adapter;
    RecyclerView rvFunctions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils.initManager();
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
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        rvFunctions = findViewById(R.id.rvFunctions);
        rvFunctions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FunctionAdapter(this);
        rvFunctions.setAdapter(adapter);
    }


    private void createFolder() {
        Utils.makeFolders(this);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
