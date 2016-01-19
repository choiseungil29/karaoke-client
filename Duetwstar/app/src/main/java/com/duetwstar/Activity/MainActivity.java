package com.duetwstar.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.duetwstar.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private ListView listView;
    private ArrayList<Data> datas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        datas.add(new Data("신승훈", R.drawable.singer_001a));
        datas.add(new Data("김건모", R.drawable.singer_002a));
        datas.add(new Data("이선희", R.drawable.singer_003a));
        datas.add(new Data("백지영", R.drawable.singer_004a));
        datas.add(new Data("엑 소", R.drawable.singer_005a));
        datas.add(new Data("걸스데이", R.drawable.singer_006a));
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new CustomAdapter(datas));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(MainActivity.this, Main2Activity.class));
                finish();
                Toast.makeText(getApplicationContext(), datas.get(position).getName() + " 선택됨", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class CustomAdapter extends BaseAdapter {

        ArrayList<Data> datas;

        public CustomAdapter(ArrayList<Data> datas) {
            this.datas = datas;
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return datas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            convertView = layoutInflater.inflate(R.layout.item_star, parent, false);

            Data data = datas.get(position);
            if (data != null) {
                TextView textName = (TextView) convertView.findViewById(R.id.text_star);
                textName.setText(data.getName());
                Picasso.with(parent.getContext()).load(data.getResource()).resize(500,500).into((ImageView) convertView.findViewById(R.id.image_star));
            }

            return convertView;
        }
    }

    class Data {
        String name;
        int resource;

        public Data(String name, int resource) {
            this.name = name;
            this.resource = resource;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getResource() {
            return resource;
        }

        public void setResource(int resource) {
            this.resource = resource;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "name='" + name + '\'' +
                    ", resource=" + resource +
                    '}';
        }
    }

}
