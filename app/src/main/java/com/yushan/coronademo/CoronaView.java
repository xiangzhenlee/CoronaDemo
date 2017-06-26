package com.yushan.coronademo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import static com.yushan.coronademo.BuildConfig.DEBUG;

/**
 * Created by yushan_lee on 2017/6/18.
 */

public class CoronaView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    /**
     * 与SurfaceHolder绑定的Canvas
     */
    private Canvas mCanvas;
    /**
     * 用于绘制的线程
     */
    private Thread thread;
    /**
     * 线程的控制字段
     */
    private boolean threadState;
    /**
     * 扇形上的文字
     */
    private String[] mStrs = new String[]{"雨幕青山", "苍山卧雪", "年方18", "I Love You", "程序猿", "花毛一体"};
    /**
     * 与文字对应的图片
     */
    private int[] mImgs = new int[]{R.drawable.qq6, R.drawable.qq4,
            R.drawable.qq2, R.drawable.qq6, R.drawable.qq4,
            R.drawable.qq2};

    /**
     * 与文字对应图片的bitmap数组
     */
    private Bitmap[] mImgsBitmap;
    /**
     * 盘块的个数
     */
    private int mItemCount = mStrs.length;
    /**
     * 绘制盘块的范围
     */
    private RectF mRange = new RectF();
    /**
     * 圆的直径
     */
    private int mRadius;
    /**
     * 通用的画笔
     */
    private Paint mPaint;
    /**
     * 绘制文字的画笔
     */
    private Paint mTextPaint;
    /**
     * 开始的位置
     */
    private volatile float mStartAngle = 0;
    /**
     * 转动的状态
     */
    private boolean turnState;
    /**
     * 控件的中心位置
     */
    private int mCenter;
    /**
     * 控件的默认padding值
     */
    private int mPadding = 40;
    /**
     * 背景盘的padding值
     */
    private int mBackgroundPadding = 50;
    /**
     * 扇形缩放的padding值
     */
    private int mScalePadding = 30;
    /**
     * 背景图的bitmap
     */
    private Bitmap mBgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
    /**
     * 文字的大小
     */
    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics());
    /**
     * 扇形的渲染对象
     */
    private BitmapShader mBitmapShader;
    /**
     * 扇形的缩放矩阵
     */
    private Matrix mMatrix;
    /**
     * 扇形的画笔
     */
    private Paint mBitmapPaint;
    /**
     * 中心遮挡圆的渲染对象
     */
    private BitmapShader mCircleBitmapShader;
    /**
     * 中心遮挡圆的缩放矩阵
     */
    private Matrix mCircleMatrix;
    /**
     * 中心遮挡圆的画笔
     */
    private Paint mCircleBitmapPaint;
    /**
     * 点击位置的坐标
     */
    private float downX;
    private float downY;
    /**
     * 手指移动的距离
     */
    private float distanceX;
    private float distanceY;
    /**
     * 转盘转动的距离
     */
    private float distance;
    /**
     * 点击回调接口
     */
    private ViewOnClickListener mViewOnClickListener;
    /**
     * 上次圆盘移动的距离
     */
    private float disTemp;
    /**
     * 点击位置的角度
     */
    private float finalClickAngle = -1;
    /**
     * 转盘旋转的偏置量
     */
    private float offset;
    /**
     * 偏置量的矫正
     */
    private float setRight;
    /**
     * 点击的区域
     */
    private int clickZone;
    /**
     * 点击误差矫正
     */
    private boolean isClick;
    /**
     * 休眠时间
     */
    private long stopTime;

    /**
     * 构造方法
     *
     * @param context
     */
    public CoronaView(Context context) {
        this(context, null);
    }

    public CoronaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 初始化线程
        mHolder = getHolder();
        mHolder.addCallback(this);
        // 初始化扇形渲染
        mMatrix = new Matrix();
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);
        // 初始化中心遮罩圆的渲染
        mCircleMatrix = new Matrix();
        mCircleBitmapPaint = new Paint();
        mCircleBitmapPaint.setAntiAlias(true);

        // 设置画布 背景透明
//        setZOrderOnTop(true);
//        mHolder.setFormat(PixelFormat.TRANSLUCENT);

        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);

    }

    /**
     * 设置控件为正方形
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = Math.min(getMeasuredWidth(), getMeasuredHeight());
        // 获取圆形的直径
        mRadius = width - mPadding - mBackgroundPadding;
        // 中心点
        mCenter = width / 2;
        setMeasuredDimension(width, width);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 初始化通用的画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        // 初始化绘制文字的画笔
        mTextPaint = new Paint();
        mTextPaint.setColor(0xFF000000);
        mTextPaint.setTextSize(mTextSize);
        // 默认扇形的绘制区域
        setDefaultZone();

        // 初始化图片
        mImgsBitmap = new Bitmap[mItemCount];
        for (int i = 0; i < mItemCount; i++) {
            mImgsBitmap[i] = BitmapFactory.decodeResource(getResources(), mImgs[i]);
        }

        // 开启线程绘制View
        start();
    }

    /**
     * 开启线程
     */
    public void start() {
        threadState = true;
        stopTime = 0;
        if (thread != null && thread.isAlive()) {
            if (DEBUG) {
                Log.e("yushan", "start: thread is alive");
            }
        } else {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // 不断的进行绘制
                    while (threadState) {
                        long start = System.currentTimeMillis();
                        draw();
                        long end = System.currentTimeMillis();
                        long pieTime = end - start;
                        stopTime += pieTime;
                        try {
                            if (pieTime < 50) {
                                Thread.sleep(50 - pieTime);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // 3秒不操作就休眠
                        if (stopTime >= 3000) {
                            stop();
                        }
                    }

                    if (DEBUG) {
                        Log.i("yushan", "run: thread stopping");
                    }
                }
            });
            thread.start();
        }
    }

    /**
     * 关闭线程
     */
    public void stop() {
        if (threadState) {
            threadState = false;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 关闭线程
        stop();
    }

    /**
     * 绘制控件
     */
    private void draw() {
        try {
            // 获得canvas
            mCanvas = mHolder.lockCanvas();
            if (mCanvas != null) {
                float tmpAngle = mStartAngle;
                float sweepAngle = (float) (360 / mItemCount);

                // 绘制背景以及圆盘背景
                drawBackground();

                // 绘制扇形以及扇形上的文字、背景
                for (int i = 0; i < mItemCount; i++) {
                    // 绘制扇形
                    drawFanShaped(tmpAngle, sweepAngle, i);
                    // 绘制扇形上的文本
//                    drawFanText(tmpAngle, sweepAngle, mStrs[i]);
                    drawFanText(tmpAngle, sweepAngle, "i:" + i + "::" + (i * 60 + offset));

                    // 移动到下一区域
                    tmpAngle += sweepAngle;
                }

                // 绘制中心圆的背景
                drawCenterBg();
                // 绘制中心遮挡圆的文字
                drawCenterText("aabbcc");

                // 滚动转盘
                if (turnState == false) {
                    mStartAngle += (distance / 2);
                    turnState = true;
                } else {
                    distance = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null)
                mHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    /**
     * 绘制扇形以及扇形的点击事件
     *
     * @param tmpAngle
     * @param sweepAngle
     * @param i
     */
    private void drawFanShaped(float tmpAngle, float sweepAngle, int i) {

        // 计算偏置量
        float turnAngle = tmpAngle % 360;
        if (i == 0) {
            offset = turnAngle;
        }
        // 矫正偏置量
        setRight = i * sweepAngle + offset;
        if (i * sweepAngle + offset > 360) {
            setRight = i * sweepAngle + offset - 360;
        } else if (i * sweepAngle + offset < 0) {
            setRight = i * sweepAngle + offset + 360;
        }
        // 设置扇形区域边界
        if (setRight > 300) {
            if ((finalClickAngle >= setRight && finalClickAngle < 360) || (finalClickAngle >= 0 && finalClickAngle < setRight - 300)) {

                setClickZone();
                clickZone = i;
                drawSingle(setRight, sweepAngle);
            } else {
                setDefaultZone();
            }
        } else {
            if (finalClickAngle >= setRight && finalClickAngle < setRight + sweepAngle) {

                setClickZone();
                clickZone = i;
                drawSingle(setRight, sweepAngle);
            } else {
                setDefaultZone();
            }
        }

        setFanPaintShader(i);
        mCanvas.drawArc(mRange, tmpAngle, sweepAngle, true, mBitmapPaint);
    }

    /**
     * 默认扇形区域边界
     */
    private void setDefaultZone() {
        mRange = new RectF(mPadding + mBackgroundPadding, mPadding + mBackgroundPadding, mRadius, mRadius);
    }

    /**
     * 设置点击扇形区域边界
     */
    private void setClickZone() {
        mRange = new RectF(mPadding + mScalePadding, mPadding + mScalePadding,
                mRadius - mScalePadding + mBackgroundPadding, mRadius - mScalePadding + mBackgroundPadding);
    }

    /**
     * 回调点击区域
     */
    private void clickZone() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (distance == 0) {
                    mViewOnClickListener.onClicked(clickZone);
                }
            }
        }, 100);
    }

    /**
     * 设置扇形渲染对象
     *
     * @param i
     */
    private void setFanPaintShader(int i) {

        // 创建Bitmap渲染对象
        mBitmapShader = new BitmapShader(mImgsBitmap[i], Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        float scale = 1.0f;
        // 比较bitmap宽和高，获得较小值
        int bSize = Math.min(mImgsBitmap[i].getWidth(), mImgsBitmap[i].getHeight());
        scale = mRadius * 1.0f / bSize;

        // shader的变换矩阵，用于放大或者缩小
        mMatrix.setScale(scale, scale);
        // 设置变换矩阵
        mBitmapShader.setLocalMatrix(mMatrix);
        // 设置shader
        mBitmapPaint.setShader(mBitmapShader);
    }

    /**
     * 设置中心遮挡圆渲染对象
     */
    private void setCirclePaintShader() {

        // 创建Bitmap渲染对象
        mCircleBitmapShader = new BitmapShader(mBgBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        float scale = 1.0f;
        // 比较bitmap宽和高，获得较小值
        int bSize = Math.min(mBgBitmap.getWidth(), mBgBitmap.getHeight());
        scale = mRadius * 1.0f / bSize;

        // shader的变换矩阵，用于放大或者缩小
        mCircleMatrix.setScale(scale, scale);
        // 设置变换矩阵
        mCircleBitmapShader.setLocalMatrix(mCircleMatrix);
        // 设置shader
        mCircleBitmapPaint.setShader(mCircleBitmapShader);
    }

    /**
     * 绘制控件背景以及圆盘背景
     */
    private void drawBackground() {

        mCanvas.drawBitmap(mBgBitmap, null, new Rect(0, 0, getMeasuredWidth(), getMeasuredWidth()), null);
        // 圆盘背景颜色设置
        mPaint.setColor(0x50000000);
        mCanvas.drawCircle(mCenter, mCenter, mCenter - mPadding, mPaint);
    }

    /**
     * 绘制中心遮挡圆背景
     */
    private void drawCenterBg() {
        setCirclePaintShader();
        mCanvas.drawCircle(mCenter, mCenter, mCenter / 2, mCircleBitmapPaint);
    }

    /**
     * 绘制中心遮挡的文字
     *
     * @param str
     */
    private void drawCenterText(String str) {

        float textWidth = mTextPaint.measureText(str);
        // 利用偏移让文字居中
        float hOffset = textWidth / 2;// 水平偏移
        float vOffset = mTextSize / 4;// 垂直偏移
        mCanvas.drawText(str, mCenter - hOffset, mCenter + vOffset, mTextPaint);
    }

    /**
     * 绘制点击区域的标志
     *
     * @param angle
     * @param sweepAngle
     */
    private void drawSingle(float angle, float sweepAngle) {
        mPaint.setColor(Color.BLUE);
        // 计算标志的坐标
        // positionX = Math.sin(Math.PI*角度/180) * R       positionY = Math.cos(Math.PI*角度/180) * R
        float positionX = (float) (Math.cos(Math.PI * (angle + sweepAngle / 2) / 180) * mCenter);
        float positionY = (float) (Math.sin(Math.PI * (angle + sweepAngle / 2) / 180) * mCenter);

        mCanvas.drawCircle(mCenter + positionX, mCenter + positionY, 20, mPaint);
    }

    /**
     * 绘制扇形上的文本
     *
     * @param startAngle
     * @param sweepAngle
     * @param string
     */
    private void drawFanText(float startAngle, float sweepAngle, String string) {
        Path path = new Path();
        path.addArc(mRange, startAngle, sweepAngle);
        float textWidth = mTextPaint.measureText(string);
        // 利用水平偏移让文字居中
        float hOffset = (float) (mRadius * Math.PI / mItemCount / 2 - textWidth / 2);// 水平偏移
        float vOffset = mRadius / 2 / 6;// 垂直偏移
        mCanvas.drawTextOnPath(string, path, hOffset, vOffset, mTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        start();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                // 每次转动圆盘都要去掉点中区域
                finalClickAngle = -1;
                isClick = false;
                break;
            case MotionEvent.ACTION_MOVE:

                // 圆心的下方
                if (downY - mCenter >= 0) {
                    distanceX = -(event.getX() - downX);
                } else {// 圆心的上方
                    distanceX = event.getX() - downX;
                }

                // 圆心的右方
                if (downX - mCenter >= 0) {
                    distanceY = event.getY() - downY;
                } else {// 圆心的左方
                    distanceY = -(event.getY() - downY);
                }

                // 圆盘转动的距离
                if (Math.abs(distanceY) - Math.abs(distanceX) >= 0) {
                    distance = distanceY;
                } else {
                    distance = distanceX;
                }

                // 每隔30px采集一次定位点
                if (Math.abs(distance) >= 30) {
                    downX = event.getX();
                    downY = event.getY();
                }

                // 圆盘移动误差矫正
                float moveDistance = disTemp - Math.abs(distance);
                if (moveDistance < 5 && moveDistance >= 0) {
                    distance = 0;
                } else {
                    disTemp = Math.abs(distance);
                }

                // 圆盘转动状态设置
                if (Math.abs(distance) < 5) {
                    distance = 0;
                    turnState = true;
                } else {
                    turnState = false;
                }

                // 点击误差矫正
                if (Math.abs(distance) > 5) {
                    isClick = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                // 每项角度大小
                float angle = (float) (360 / mItemCount);

                // 角度 = Math.atan((dpPoint.y-dpCenter.y) / (dpPoint.x-dpCenter.x)) / π（3.14） * 180
                double clickAngle = Math.atan((downY - mCenter) / (downX - mCenter)) / Math.PI * 180;

                // 点击区域
                int zone = (int) (clickAngle / angle);
                float overflow = (float) (clickAngle % angle);
                // 点击角度的矫正
                // 圆心的下方
                if (downY - mCenter >= 0) {
                    if (overflow >= 0) {
                        finalClickAngle = (float) clickAngle;
                    } else {
                        finalClickAngle = (float) clickAngle + 180;
                    }
                } else {// 圆心的上方
                    if (overflow >= 0) {
                        finalClickAngle = (float) clickAngle + 180;
                    } else {
                        finalClickAngle = (float) clickAngle + 360;
                    }
                }

                if (isClick == false) {
                    // 调用回调接口
                    clickZone();
                } else {
                    // 每次转动圆盘都要去掉点中区域
                    finalClickAngle = -1;
                }

                turnState = true;
                break;
        }
        return true;
    }

    public void setViewOnClickListener(ViewOnClickListener viewOnClickListener) {
        this.mViewOnClickListener = viewOnClickListener;
    }

    /**
     * 点击扇形的监听器
     */
    public interface ViewOnClickListener {
        /**
         * 点击转盘，回调方法
         */
        void onClicked(int clickZone);
    }

}

