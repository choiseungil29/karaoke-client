package karaokeclicker.kkk.com.karaokeclicker.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import butterknife.Bind;
import butterknife.ButterKnife;
import karaokeclicker.kkk.com.karaokeclicker.R;

public class BackgroundSelectActivity extends Activity {

    @Bind(R.id.list_left)
    ListView listLeft;
    @Bind(R.id.list_right)
    ListView listRight;
    @Bind(R.id.image_left)
    ImageView imageLeft;
    @Bind(R.id.image_right)
    ImageView imageRight;
    @Bind(R.id.btn_ok)
    Button btnOk;

    private int[] imageResourceLeft = {R.drawable.back_1, R.drawable.back_2, R.drawable.back_3};
    private int[] imageResourceRight = {R.drawable.front_1, R.drawable.front_2, R.drawable.front_3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_select);
        ButterKnife.bind(this);

        initListView();
        initButton();
    }

    private void initButton() {
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BackgroundSelectActivity.this, FinalSendActivity.class));
                finish();
            }
        });
    }

    private void initListView() {
        listLeft.setAdapter(new CustomAdapter(imageResourceLeft));
        listRight.setAdapter(new CustomAdapter(imageResourceRight));

        listLeft.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                imageLeft.setImageResource(imageResourceLeft[position]);
            }
        });
        listRight.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                imageRight.setImageResource(imageResourceRight[position]);
            }
        });

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
                ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
                imageView.setImageResource(resource);
            }
            return convertView;
        }
    }
}
