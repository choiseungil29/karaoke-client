package com.vpang.clicker.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vpang.clicker.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BackgroundSelectActivity extends Activity {

    @Bind(R.id.grid_left)
    GridView gridLeft;
    @Bind(R.id.grid_right)
    GridView gridRight;
    @Bind(R.id.image_left)
    ImageView imageLeft;
    @Bind(R.id.image_right)
    ImageView imageRight;
    @Bind(R.id.btn_send)
    ImageView btnSend;

    @Bind(R.id.tv_singer)
    TextView tv_singer;

    @Bind(R.id.tv_leftBackground)
    TextView tv_leftBackground;
    @Bind(R.id.tv_rightBackground)
    TextView tv_rightBackground;

    private String song;
    private String singer;


    private int[] imageResourceLeft = {R.drawable.front1, R.drawable.front2, R.drawable.front3, R.drawable.front4, R.drawable.front5, R.drawable.front6};
    private int[] imageResourceRight = {R.drawable.back1, R.drawable.back2, R.drawable.back3
            , R.drawable.back4, R.drawable.back5, R.drawable.back6, R.drawable.back7
            , R.drawable.back8, R.drawable.back9, R.drawable.back10, R.drawable.back11
            , R.drawable.back12, R.drawable.back13, R.drawable.back14, R.drawable.back15};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_select);
        ButterKnife.bind(this);

        singer = getIntent().getStringExtra("singer");
        song = getIntent().getStringExtra("song");

        if(singer == null || song == null) {
            tv_singer.setText("노래를 선택해주세요");
        } else {
            tv_singer.setText("노래 : " + singer + " - " + song);
        }

        tv_leftBackground.setText("전경을 선택해주세요");
        tv_rightBackground.setText("배경을 선택해주세요");

        initListView();
        initButton();
    }

    private void initListView() {
        gridLeft.setAdapter(new CustomAdapter(imageResourceLeft));
        gridRight.setAdapter(new CustomAdapter(imageResourceRight));

        gridLeft.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Picasso.with(parent.getContext()).load(imageResourceLeft[position]).into(imageLeft);
                tv_leftBackground.setText("전경 : " + position + "번");
            }
        });
        gridRight.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Picasso.with(parent.getContext()).load(imageResourceRight[position]).into(imageRight);
                tv_rightBackground.setText("배경 : " + position + "번");
            }
        });

    }

    private void initButton() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @OnClick(R.id.btn_back)
    public void backButtonClick() {
        finish();
    }


    class CustomAdapter extends BaseAdapter {
        int[] imageResource;

        public CustomAdapter(int[] imageResource) {
            this.imageResource = imageResource;
        }

        @Override
        public int getCount() {
            return imageResource.length;
        }

        @Override
        public Object getItem(int position) {
            return imageResource[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            convertView = layoutInflater.inflate(R.layout.item_background_select, parent, false);

            int resource = imageResource[position];

            if (resource != 0) {
                Picasso.with(parent.getContext()).load(resource).into((ImageView) convertView.findViewById(R.id.image));
            }
            return convertView;
        }
    }
}
