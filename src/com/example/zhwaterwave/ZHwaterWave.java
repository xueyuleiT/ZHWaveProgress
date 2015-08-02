/*
 * ZHwaterWave.java
 * classes : com.example.zhwaterwave.ZHwaterWave
 * @author zenghui
 * V 1.0.0
 * Create at 2015-6-6 下午8:08:42
 */
package com.example.zhwaterwave;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * com.example.zhwaterwave.ZHwaterWave
 * 
 * @author zenghui <br/>
 *         create at 2015-6-6 下午8:08:42
 */
public class ZHwaterWave extends View {
    private Path wavePath;// 水波的路径
    private Paint cyclePaint;// 圆的画笔
    private Paint wavePaint;// 水波的画笔
    private float paintW = 6;// 圆的边界的大小 也就是最外层的那一圈边界
    private float viewH, viewW;// 控件的宽和高
    private float speed = -0.5f;// 水波上升的速度
    private float levelLine;// 水波的水平线
    private float cycleR;// 圆的半径
    private boolean isMeasure = false;// 是否初始化
    private Timer timer;//
    private MyTimerTask mTask;//
    private float moveSpeed = 0;// 水波移动速度
    private Path path;// 裁剪的路径

    private boolean needProgress = true;
    private int WATER_COLOR;// 水的颜色

    public float getCycleR() {
        return this.cycleR;
    }

    public void setCycleR(float cycleR) {
        this.cycleR = cycleR;
    }

    public void start() {
        startDraw();
    }

    /*
     * 水波的转折点
     */
    class Point {
        private float x, y;

        public Point(float viewW, float viewH) {
            this.x = viewW;
            this.y = viewH;
        }

        public float getX() {
            return this.x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public float getY() {
            return this.y;
        }

        public void setY(int y) {
            this.y = y;
        }

    }

    public ZHwaterWave(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.COLOR);
        // 获取自定义属性和默认值
        WATER_COLOR = mTypedArray.getColor(R.styleable.COLOR_waterColor, 0x74e085);
        mTypedArray.recycle();

        initPaint();
    }

    public ZHwaterWave(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZHwaterWave(Context context) {
        this(context, null);
    }

    private void initPaint() {
        timer = new Timer();

        wavePath = new Path();
        cyclePaint = new Paint();
        cyclePaint.setStyle(Paint.Style.STROKE);// 空心画笔
        cyclePaint.setColor(Color.GRAY);
        cyclePaint.setAntiAlias(true);
        cyclePaint.setStrokeWidth(paintW);

        wavePaint = new Paint();
        wavePaint.setStyle(Paint.Style.FILL);
        wavePaint.setColor(WATER_COLOR);
        wavePaint.setAlpha(80);
        wavePaint.setAntiAlias(true);

        progressPaint = new Paint();
        progressPaint.setColor(Color.RED);
        progressPaint.setTypeface(Typeface.DEFAULT);
        progressPaint.setTextSize(progressSize);
    }

    float x1, x2, y;// 水波与圆相交的亮点坐标 y都是一样的（因为是一个完整的正玄函数）

    private void startDraw() {

        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
        mTask = new MyTimerTask(updateHandler);
        timer.schedule(mTask, 0, 10);

    }

    private int sX = 0;// 水波的x坐标值
    private ArrayList<Point> pList = new ArrayList<Point>();// 水波的转折点集合
    int b;// 自己去理解 y ＝ levelLine - sin(x - b) 其实就是计算水波偏离x2的值
    private float waveH = 0;
    float pecent = 1;

    private void setPoint() {
        if (y >= viewH / 2) {
            pecent = ((float) (viewH / 2 + cycleR - y + paintW)) / cycleR;
        } else {
            pecent = ((float) (y - (viewH / 2 - cycleR - paintW))) / cycleR;
        }
        float margin = (int) Math.sin(sX);
        b = (int) (sX - Math.toDegrees(margin));
        margin = ((waveH * pecent) * margin);
        int temp = (int) Math.sqrt((cycleR) * (cycleR) - (levelLine + margin - viewH / 2)
                * (levelLine + margin - viewH / 2));
        x2 = viewW / 2 - temp;
        x1 = viewW / 2 + temp;

        y = levelLine + margin;
        float scale = (float) temp / 180;// 计算一个周期与(x2-x1)的比值(x2-x1)/360
                                         // (x2-x1) = 2*temp
        for (int i = 0; i < pList.size(); i++) {
            pList.get(i).x = (int) (b * scale + (i - length / 2) * temp / 2 + x2);

            if (i == 1 || i == 3 || i == 5 || i == 7 || i == 9 || i == 11 || i == 13) {
                pList.get(i).y = levelLine;
            } else if (i == 2 || i == 6 || i == 10) {
                pList.get(i).y = (int) (waveH * pecent) + levelLine;
            } else {
                pList.get(i).y = -(int) (waveH * pecent) + levelLine;
            }
        }

    }

    Handler updateHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            levelLine += speed;
            sX += moveSpeed;

            if (sX % 360 == 0) {
                sX = sX % 180;
            }
            if (y < viewH / 2 - cycleR) {
                y = viewH / 2 + cycleR + paintW;
                levelLine = y;
                sX = 0;
            }

            setPoint();
            invalidate();
        }
    };

    class MyTimerTask extends TimerTask {
        Handler handler;

        public MyTimerTask(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            handler.sendMessage(handler.obtainMessage());
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        wavePath.reset();
        wavePath.moveTo(viewW / 2 + cycleR, viewH / 2 + cycleR);
        wavePath.lineTo(viewW / 2 - cycleR, viewH / 2 + cycleR);
        wavePath.lineTo(viewW / 2 - cycleR, y);
        int i = 0;
        for (; i < pList.size() - 2; i = i + 2) {
            wavePath.quadTo(pList.get(i).getX(), pList.get(i).getY(), pList.get(i + 1).getX(), pList.get(i + 1).getY());
        }
        wavePath.quadTo(pList.get(i).getX(), pList.get(i).getY(), viewW, y);
        wavePath.lineTo(viewW, viewH / 2 + cycleR);
        canvas.clipPath(path);
        canvas.drawPath(wavePath, wavePaint);
        wavePath.close();
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, cycleR, cyclePaint);

        if (needProgress)
            drawProgress(canvas);
        drawBubble(canvas);
    }

    public boolean isNeedProgress() {
        return this.needProgress;
    }

    public void setNeedProgress(boolean needProgress) {
        this.needProgress = needProgress;
    }

    public float getProgressSize() {
        return this.progressSize;
    }

    public void setProgressSize(float progressSize) {
        this.progressSize = progressSize;
        progressPaint.setTextSize(progressSize);
    }

    Rect rect = new Rect();
    Paint progressPaint;
    private float progressSize = 40;
    DecimalFormat df = new DecimalFormat("#####0.00");

    private float textX, textY;

    private void drawProgress(Canvas canvas) {
        float height = (viewH / 2 + cycleR - y);
        double p = height * 100 / (cycleR * 2);
        String str = df.format(p) + "%";
        textX = (float) ((viewW - rect.width() * str.length()) * 0.5);
        canvas.drawText(str, textX, textY, progressPaint);

    }

    private int length;// 1/4波形的个数

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!isMeasure) {
            isMeasure = true;
            cycleR = ((getHeight() > getWidth() ? getWidth() / 2 : getHeight() / 2) - paintW) - paintW;
            viewH = getHeight();
            viewW = getWidth();
            if (waveH == 0)
                waveH = cycleR / 3;
            if (moveSpeed == 0) {
                moveSpeed = 1.2f;
            }
            levelLine = (getHeight() / 2 + cycleR);
            y = levelLine;
            speed = -1;
            path = new Path();
            path.addCircle(viewW / 2, viewH / 2, cycleR, Path.Direction.CCW);

            scaleSpeedY = cycleR / 100;

            if (length == 0) {
                length = 14;
            }

            for (int i = 0; i < length; i++) {
                Point p1 = new Point(viewW, viewH);
                pList.add(p1);
            }

            String str = "0";
            progressPaint.getTextBounds(str, 0, str.length(), rect);
            textY = (float) ((viewH + rect.height()) * 0.5);

            randomR = (int) (viewW / 40);
        }
    }

    public float getSpeed() {
        return this.speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getMoveSpeed() {
        return this.moveSpeed;
    }

    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public int getWATER_COLOR() {
        return this.WATER_COLOR;
    }

    public void setWATER_COLOR(int wATER_COLOR) {
        this.WATER_COLOR = wATER_COLOR;
        wavePaint.setColor(WATER_COLOR);
    }

    public int getLength() {
        return this.length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public float getScaleSpeedY() {
        return this.scaleSpeedY;
    }

    public void setScaleSpeedY(float scaleSpeedY) {
        this.scaleSpeedY = scaleSpeedY;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private Random random = new Random();// 生成随机数
    private boolean starting = false;
    private List<Bubble> bubbles = new ArrayList<Bubble>();
    private float scaleSpeedY;
    private int randomR;

    private void drawBubble(Canvas canvas) {
        if (!starting) {
            starting = true;
            new Thread() {
                public void run() {
                    while (starting) {
                        try {
                            Thread.sleep(random.nextInt(3) * 300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Bubble bubble = new Bubble();
                        int radius = random.nextInt((int) (randomR));
                        Log.d("bubble", "radius");
                        System.out.println("radius ======>" + radius);
                        while (radius == 0) {
                            radius = random.nextInt((int) (randomR));
                        }
                        float speedY = random.nextFloat() * scaleSpeedY;
                        while (speedY < 1 || speedY > 10) { 
                            speedY = random.nextFloat() * scaleSpeedY;
                        }
                        bubble.setRadius(radius);
                        bubble.setSpeedY(speedY);
                        bubble.setX(viewW / 2);
                        bubble.setY(viewH / 2 + cycleR);
                        float speedX = random.nextFloat() - 0.5f;
                        while (speedX == 0) {
                            speedX = random.nextFloat() - 0.5f;
                        }
                        bubble.setSpeedX(speedX * 2);
                        bubbles.add(bubble);
                    }
                };
            }.start();
        }
        Paint paint = new Paint();
        paint.reset();
        paint.setColor(0X669999);// 灰白色
        paint.setAlpha(45);// 设置不透明度：透明为0，完全不透明为255
        List<Bubble> list = new ArrayList<Bubble>(bubbles);
        // 依次绘制气泡
        for (Bubble bubble : list) {
            // 碰到上边界从数组中移除
            if (bubble.getY() - bubble.getSpeedY() <= levelLine + waveH * pecent) {
                bubbles.remove(bubble);
            }

            // 碰到左边界从数组中移除
            else if (bubble.getX() - bubble.getRadius() <= x2) {
                bubbles.remove(bubble);
            }
            // 碰到右边界从数组中移除
            else if (bubble.getX() + bubble.getRadius() >= x1) {
                bubbles.remove(bubble);
            } else {
                int i = bubbles.indexOf(bubble);
                if (bubble.getX() + bubble.getSpeedX() <= bubble.getRadius()) {
                    bubble.setX(bubble.getRadius());
                } else if (bubble.getX() + bubble.getSpeedX() >= x1 - bubble.getRadius()) {
                    bubble.setX(x1 - bubble.getRadius());
                } else {
                    bubble.setX(bubble.getX() + bubble.getSpeedX());
                }
                bubble.setY(bubble.getY() - bubble.getSpeedY());

                // 海底溢出的甲烷上升过程越来越大（气压减小）
                // 鱼类和潜水员吐出的气体却会越变越小（被海水和藻类吸收）
                // 如果考虑太多现实情景的话，代码量就会变得很大，也容易出现bug
                // 感兴趣的读者可以自行添加
                // bubble.setRadius(bubble.getRadius());

                bubbles.set(i, bubble);

                canvas.drawCircle(bubble.getX(), bubble.getY(), bubble.getRadius(), paint);
            }
        }
    }

    private class Bubble {
        // 气泡半径
        private float radius;
        // 上升速度
        private float speedY;
        // 平移速度
        private float speedX;
        // 气泡x坐标
        private float x;
        // 气泡y坐标
        private float y;

        public float getRadius() {
            return radius;
        }

        public void setRadius(float radius) {
            this.radius = radius;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getSpeedY() {
            return speedY;
        }

        public void setSpeedY(float speedY) {
            this.speedY = speedY;
        }

        public float getSpeedX() {
            return speedX;
        }

        public void setSpeedX(float speedX) {
            this.speedX = speedX;
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        starting = false;
        super.onDetachedFromWindow();
    }

}
