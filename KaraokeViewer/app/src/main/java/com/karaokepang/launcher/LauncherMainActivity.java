package com.karaokepang.launcher;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.karaokepang.Activity.MainActivity;
import com.karaokepang.R;

public class LauncherMainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnVpang;
    private Button btnFriend;
    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher_main);

        btnVpang = (Button) findViewById(R.id.btn_launcher_vpang);
        btnFriend = (Button) findViewById(R.id.btn_launcher_friend);
        btnStart = (Button) findViewById(R.id.btn_launcher_star);

        btnVpang.setOnClickListener(this);
        btnFriend.setOnClickListener(this);
        btnStart.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_launcher_vpang:
                startActivity(new Intent(LauncherMainActivity.this, MainActivity.class));
                break;
            case R.id.btn_launcher_friend:
                Toast.makeText(getApplicationContext(), "연결전...(수정중)", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_launcher_star:
                Toast.makeText(getApplicationContext(), "연결전...(수정중)", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
