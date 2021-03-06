package com.example.xfhy.bannerdemo;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.xfhy.easybanner.ui.EasyBanner;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EasyBanner mBanner;
    private LinearLayout mRootView;
    private Context mContext;
    private Button btnNew;
    private Button btnStart;
    private Button btnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        initView();
    }

    private void initView() {
        mRootView = (LinearLayout) findViewById(R.id.ll_main);
        btnNew = (Button) findViewById(R.id.btn_new);
        btnStart = (Button) findViewById(R.id.btn_start);
        btnStop = (Button) findViewById(R.id.btn_stop);

        btnNew.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);

        //可以在布局里面写
        mBanner = (EasyBanner) findViewById(R.id.eb_banner);
        //设置图片url和图片标题
        mBanner.initBanner(getImageUrlData(), getContentData());
        //设置图片加载器
        mBanner.setImageLoader(new EasyBanner.ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Glide.with(mContext).load(url).into(imageView);
            }
        });
        //监听banner的item点击事件
        mBanner.setOnItemClickListener(new EasyBanner.OnItemClickListener() {
            @Override
            public void onItemClick(int position, String title) {
                Toast.makeText(MainActivity.this, "position:" + position + "   title:" + title,
                        Toast
                                .LENGTH_SHORT).show();
            }
        });



        /*

        //也可以直接动态生成
        EasyBanner easyBanner = new EasyBanner(this);
        easyBanner.initBanner(getImageUrlData(), getContentData());
        mRootView.addView(easyBanner,new LinearLayout.LayoutParams(LinearLayout
                .LayoutParams.MATCH_PARENT, DensityUtil.dip2px(this,200)));
        //设置图片加载器
        easyBanner.setImageLoader(new EasyBanner.ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Glide.with(mContext).load(url).into(imageView);
            }
        });

        */
    }

    private List<String> getImageUrlData() {
        List<String> imageList = new ArrayList<>();
        imageList.add("https://pic3.zhimg.com/v2-e885c8acf8ca274cda11dd8ce7760d26.jpg");
        imageList.add("https://pic3.zhimg.com/v2-ecfc08ebca9f42e29144d09050fb619e.jpg");
        imageList.add("https://pic1.zhimg.com/v2-0d3e0c7d778083003e6f8029cf7cf570.jpg");
        imageList.add("https://pic1.zhimg.com/v2-5d0f06845b6cdd5e32aed0a960aede98.jpg");
        imageList.add("https://pic2.zhimg.com/v2-4d873d82642d347aa0e709b2e2f5be81.jpg");
        return imageList;
    }

    private List<String> getContentData() {
        List<String> contentList = new ArrayList<>();
        contentList.add("黄河变清了要发生大洪水」，这个标题党过分了");
        contentList.add("地铁司机为什么每次都能把车停得那么准？");
        contentList.add("赵薇老师，你还能不能有点明星样儿了？");
        contentList.add("听说同时吃它俩，像是在吃「蘸了肥皂水的苍蝇」我亲自试了试");
        contentList.add("「不主动追求你，也不明确拒绝你」，这就是现代人的爱情");
        return contentList;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_new:
                mBanner.resetData(getImageUrlData(), getContentData());
                break;
            case R.id.btn_start:
                mBanner.start();
                break;
            case R.id.btn_stop:
                mBanner.stop();
                break;

            default:
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("xfhy", "onStop");
        if (mBanner != null) {
            mBanner.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBanner != null) {
            mBanner.start();
        }
    }

}
