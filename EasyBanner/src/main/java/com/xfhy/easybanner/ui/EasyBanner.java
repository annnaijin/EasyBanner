package com.xfhy.easybanner.ui;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.easybanner.R;
import com.xfhy.easybanner.util.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * author xfhy
 * create at 17-10-3 下午1:19
 * description：自定义的Banner   广告轮播图
 * <p>
 * 需要实现的东西：
 * 自动轮播
 * 无限左划右划
 * 传入广告条目
 * 加载网络图片（Glide），加载文字
 * 底部小白点可切换，大小可换，数量可动态增加
 * 监听点击事件
 * 设置滑动事件
 * PagerAdapter
 * 触摸时不能自动滑动
 * 实现ViewPager点击事件
 * <p>
 * <p>
 * 遇到的坑：ViewPager把触摸事件消费了，外层重写onTouchEvent没用的，已经被子View消费的事件，是没用回传回来的，所以我直接在dispatchTouchEvent()
 * 分发事件的时候就获取到该事件，并停止ViewPager的滚动   当用户抬起手指时，继续滚动。
 */

public class EasyBanner extends FrameLayout implements ViewPager.OnPageChangeListener {
    private static final String TAG = "EasyBanner";
    /**
     * 每个广告条目的图片地址
     */
    private List<String> imageUrlList;
    /**
     * 每个广告条目的文字内容
     */
    private List<String> contentList;
    /**
     * 用来盛放广告条目的
     */
    private ViewPager mViewPager;
    /**
     * 当前广告条目的文字内容
     */
    private TextView mContent;
    /**
     * 底部小圆点整个布局
     */
    private LinearLayout mPointLayout;
    /**
     * 用来加载banner图片的
     */
    private List<ImageView> imageViewList;
    /**
     * 小圆点上一次的位置
     */
    private int lastPosition;
    /**
     * 底部小圆点默认大小
     */
    private final static float POINT_DEFAULT_SIZE = 10f;
    /**
     * 切换广告的时长  单位：ms
     */
    private final static int BANNER_SWITCH_DELAY_MILLIS = 3000;
    /**
     * 用户是否正在触摸banner
     */
    private boolean isTouched = false;
    private Handler mHandler = new Handler();
    /**
     * banner点击事件监听器
     */
    private OnItemClickListener listener;

    public EasyBanner(@NonNull Context context) {
        super(context);
        initView();
    }

    public EasyBanner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public EasyBanner(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int
            defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    /**
     * 初始化View
     */
    private void initView() {
        //加载布局   子View个数==0  则还没有加载布局
        if (getChildCount() == 0) {
            View.inflate(getContext(), R.layout.layout_banner, this);
            mViewPager = (ViewPager) findViewById(R.id.vp_banner);
            mContent = (TextView) findViewById(R.id.tv_banner_content);
            mPointLayout = (LinearLayout) findViewById(R.id.ll_banner_point);
        }
    }

    /**
     * 初始化banner
     * 图片地址数必须和文字内容条目数相同
     *
     * @param imageUrlList 每个广告条目的图片地址
     * @param contentList  每个广告条目的文字内容
     */
    public void initBanner(@NonNull List<String> imageUrlList, @NonNull List<String> contentList) {
        this.imageUrlList = imageUrlList;
        this.contentList = contentList;
        //TODO 这里需要限制是否为空  还有图片地址个数必须和文字内容条目数相等
        if (imageUrlList == null || contentList == null || imageUrlList.size() == 0 || contentList
                .size() == 0) {
            throw new IllegalArgumentException("传入图片地址或广告内容不能为空");
        }

        if (imageUrlList.size() != contentList.size()) {
            throw new IllegalArgumentException("传入图片地址或广告内容数量必须一致");
        }

        initView();
        initData();
    }
    //int touchFlags = 0;
    /**
     * 初始化数据
     */
    private void initData() {
        imageViewList = new ArrayList<>();
        View pointView;

        int bannerSize = imageUrlList.size();
        for (int i = 0; i < bannerSize; i++) {
            //加载图片
            ImageView imageView = new ImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(getContext()).load(imageUrlList.get(i)).into(imageView);
            imageViewList.add(imageView);

            //底部的小白点
            pointView = new View(getContext());
            //设置背景
            pointView.setBackgroundResource(R.drawable.selector_banner_point);
            //设置小圆点的大小
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(DensityUtil
                    .dip2px(getContext(), POINT_DEFAULT_SIZE), DensityUtil.dip2px(getContext(),
                    POINT_DEFAULT_SIZE));

            //除第一个以外，其他小白点都需要设置左边距
            if (i != 0) {
                layoutParams.leftMargin = DensityUtil.dip2px(getContext(), POINT_DEFAULT_SIZE / 2);
                pointView.setEnabled(false); //默认小白点是不可用的
            }

            pointView.setLayoutParams(layoutParams);
            mPointLayout.addView(pointView);  //添加到linearLayout中
        }

        //延时
        mHandler.postDelayed(delayRunnable, BANNER_SWITCH_DELAY_MILLIS);

        BannerAdapter bannerAdapter = new BannerAdapter();
        mViewPager.setAdapter(bannerAdapter);
        //页面切换监听器
        mViewPager.addOnPageChangeListener(this);

        //将ViewPager的起始位置放在  一个很大的数处，那么一开始就可以往左划动了   那个数必须是imageUrlList.size()的倍数
        int remainder = (Integer.MAX_VALUE / 2) % imageUrlList.size();
        mViewPager.setCurrentItem(Integer.MAX_VALUE / 2 - remainder);
        //文本默认为第一项
        mContent.setText(contentList.get(0));
        mPointLayout.getChildAt(0).setEnabled(true);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        int newPosition = position % imageUrlList.size();

        //当页面切换时，将底部白点的背景颜色换掉
        mPointLayout.getChildAt(newPosition).setEnabled(true);
        mPointLayout.getChildAt(lastPosition).setEnabled(false);
        //文字内容替换掉
        mContent.setText(contentList.get(newPosition));
        lastPosition = newPosition;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isTouched = true;   //正在触摸  按下
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                isTouched = false;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 延时的任务
     */
    Runnable delayRunnable = new Runnable() {
        @Override
        public void run() {
            //用户在触摸时不能进行自动滑动
            if (!isTouched) {
                //ViewPager设置为下一项
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                Log.e(TAG, "run: 自动滑动  isTouched:" + isTouched);
            }
            //继续延迟切换广告
            mHandler.postDelayed(delayRunnable, BANNER_SWITCH_DELAY_MILLIS);
        }
    };

    /**
     * banner中ViewPager的adapter
     */
    private class BannerAdapter extends PagerAdapter {

        /**
         * 返回资源一共有的条目数
         */
        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        /**
         * 复用判断逻辑
         */
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final int newPosition = position % imageUrlList.size();
            ImageView imageView = imageViewList.get(newPosition);

            //设置点击事件
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //回调
                    if (listener != null) {
                        listener.onItemClick(newPosition, contentList.get(newPosition));
                    }
                }
            });
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    /**
     * 设置banner的item点击事件
     *
     * @param listener OnItemClickListener
     */
    public void setOnItemClickListener(@NonNull OnItemClickListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Item监听器不能为空！");
        }
        this.listener = listener;
    }

    /**
     * Item点击的”监听器“
     */
    public interface OnItemClickListener {
        /**
         * 点击item时的回调函数
         *
         * @param position 当前点击item的索引
         * @param title    当前点击item的标题
         */
        void onItemClick(int position, String title);
    }

}
