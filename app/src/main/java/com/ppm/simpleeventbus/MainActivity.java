package com.ppm.simpleeventbus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.ppm.library.EventBus;
import com.ppm.library.Subscribe;
import com.ppm.library.ThreadMode;

public class MainActivity extends AppCompatActivity {

    private TextView txtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        txtView = findViewById(R.id.txt_login);
        txtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Event event) {
        txtView.setText(event.getMsg());
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unRegister(this);
        super.onDestroy();
    }
}
