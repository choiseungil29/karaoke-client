package com.vpang.clicker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vpang.clicker.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BackgroundSelectActivity extends Activity {

    @Bind(R.id.list_left)
    ListView listLeft;
    @Bind(R.id.list_right)
    ListView listRight;
    @Bind(R.id.image_left)
    ImageView imageLeft;
    @Bind(R.id.image_right)
    ImageView imageRight;
    @Bind(R.id.edit_phone)
    EditText editPhone;
    @Bind(R.id.checkbox_agree)
    CheckBox checkBox;
    @Bind(R.id.btn_send)
    Button btnSend;


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

        initListView();
        initButton();
    }

    private void initListView() {
        listLeft.setAdapter(new CustomAdapter(imageResourceLeft));
        listRight.setAdapter(new CustomAdapter(imageResourceRight));

        listLeft.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Picasso.with(parent.getContext()).load(imageResourceLeft[position]).into(imageLeft);
            }
        });
        listRight.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Picasso.with(parent.getContext()).load(imageResourceRight[position]).into(imageRight);
            }
        });

    }

    private void initButton() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCellphone(editPhone.getText().toString()) && editPhone.getText().toString().length() != 0) {
                    if (checkBox.isChecked()) {
                        Toast.makeText(BackgroundSelectActivity.this, "서버가 미구현되여 초기화면으로 이동합니다", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(BackgroundSelectActivity.this, "약관에 동의해주세요", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BackgroundSelectActivity.this, "전화번호를 확인해주세요", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public boolean isCellphone(String str) {
        return str.matches("(01[016789])(\\d{3,4})(\\d{4})");
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
