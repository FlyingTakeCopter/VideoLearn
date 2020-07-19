package com.lqk.videolearn;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.ffmpeg_4_2_2.FFmpegMainActivity;
import com.lqk.videolearn.camera.CameraActivity;
import com.lqk.videolearn.image.SGLViewActivity;
import com.lqk.videolearn.render.FGLViewActivity;
import com.lqk.videolearn.solarsystem.SSViewActivity;
import com.lqk.videolearn.vr.VrContextActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView mList;
    private ArrayList<MainBean> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mList= (RecyclerView)findViewById(R.id.mList);
        mList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        data=new ArrayList<>();
        add("绘制形体", FGLViewActivity.class);
        add("绘制纹理", SGLViewActivity.class);
        add("太阳系", SSViewActivity.class);
        add("相机", CameraActivity.class);
        add("vr", VrContextActivity.class);
        add("ffmpeg-learn", FFmpegMainActivity.class);


        mList.setAdapter(new MenuAdapter());
    }

    private void add(String name,Class<?> clazz){
        MainBean bean=new MainBean();
        bean.name=name;
        bean.clazz=clazz;
        data.add(bean);
    }

    @Override
    public void onClick(View view) {
        int position= (int)view.getTag();
        MainBean bean=data.get(position);
        startActivity(new Intent(this,bean.clazz));
    }

    private class MainBean{
        String name;
        Class<?> clazz;
    }

    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuHolder>{


        @Override
        public MenuHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MenuHolder(getLayoutInflater().inflate(R.layout.item_button,parent,false));
        }

        @Override
        public void onBindViewHolder(MenuHolder holder, int position) {
            holder.setPosition(position);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class MenuHolder extends RecyclerView.ViewHolder{

            private Button mBtn;

            MenuHolder(View itemView) {
                super(itemView);
                mBtn= (Button)itemView.findViewById(R.id.mBtn);
                mBtn.setOnClickListener(MainActivity.this);
            }

            public void setPosition(int position){
                MainBean bean=data.get(position);
                mBtn.setText(bean.name);
                mBtn.setTag(position);
            }
        }

    }
}
