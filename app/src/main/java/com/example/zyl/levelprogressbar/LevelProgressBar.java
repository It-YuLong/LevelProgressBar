package com.example.zyl.levelprogressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ProgressBar;

/*
 * Created by ZYL on 2018/1/5.
 */

public class LevelProgressBar extends ProgressBar {
    private final int EMPTY_MESSAGE = 1;
    //xml中自定义属性
    private int levelTextUnChooseColor;
    private int levelTextChooseColor;
    private int progressBgColor;
    private int progressEndColor;
    private int progressStartColor;
    private int levelTextSize;
    private int progressHeight;

    /*代码中需要设置的属性*/
    private int levels;
    private String[] levelTexts;
    private int currentLevel;
    private int animInterval;
    private Paint mPaint;
    private int mTotalWith;
    private int targetProgress;
    private int textHeight;

private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int progress = getProgress();
            // 小于目标值时增加进度，大于目标值时减小进度
            if (progress < targetProgress) {
                setProgress(++progress);
                handler.sendEmptyMessageDelayed(EMPTY_MESSAGE, animInterval);
            } else if (progress > targetProgress){
                setProgress(--progress);
                handler.sendEmptyMessageDelayed(EMPTY_MESSAGE, animInterval);
            } else {
                handler.removeMessages(EMPTY_MESSAGE);
            }
        }
    };
    public LevelProgressBar(Context context) {
        this(context, null);
    }

    public LevelProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LevelProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //通过AttributeSet 获取xml设置的属性值
        obtainStyleAttributeSet(attrs);

        //初始化画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(levelTextSize); //等级文本字体大小
        mPaint.setColor(levelTextUnChooseColor);//等级文本非选中时颜色
    }

    private void obtainStyleAttributeSet(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LevelProgressBar);
        levelTextUnChooseColor = typedArray.getColor(R.styleable.LevelProgressBar_levelTextUnChooseColor, 0x000000);
        levelTextChooseColor = typedArray.getColor(R.styleable.LevelProgressBar_levelTextChooseColor, 0x000000);
        progressBgColor = typedArray.getColor(R.styleable.LevelProgressBar_progressBgColor, 0x000000);
        progressEndColor = typedArray.getColor(R.styleable.LevelProgressBar_progressEndColor, 0x00FF00);
        progressStartColor = typedArray.getColor(R.styleable.LevelProgressBar_progressStartColor, 0xCCFFCC);
        levelTextSize = (int) typedArray.getDimension(R.styleable.LevelProgressBar_levelTextSize, dpTop(15));
        progressHeight = (int) typedArray.getDimension(R.styleable.LevelProgressBar_progressHeight, dpTop(20));


    }

    private int dpTop(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public void setLevels(int levels) {
        this.levels = levels;
    }

    public void setLevelTexts(String[] levelTexts) {
        this.levelTexts = levelTexts;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
        this.targetProgress = (int) (currentLevel * 1f / levels * getMax());
    }

    public void setAnimInterval(int animInterval) {
        this.animInterval = animInterval;
         handler.sendEmptyMessage(EMPTY_MESSAGE);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int with = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (heightMode != MeasureSpec.EXACTLY) {
            textHeight = (int) (mPaint.descent() - mPaint.ascent());
            //等级和进度条之间的间隔
            height = getPaddingTop() + getPaddingBottom() + textHeight + progressHeight + dpTop(10);
        }

        setMeasuredDimension(with, height);
        mTotalWith = getMeasuredWidth() - getPaddingLeft() - getPaddingLeft();

    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        //流出padding的位置
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());

        //绘制等级文字
        for (int i = 0; i < levels; i++) {

            int textwight = (int) mPaint.measureText(levelTexts[i]);
            mPaint.setColor(levelTextUnChooseColor);
            mPaint.setTextSize(levelTextSize);

            //到指定位置时 对应的文字设置为深色
            if (getProgress() == targetProgress && currentLevel >= 1 && currentLevel <= levels && i == currentLevel - 1) {
                mPaint.setColor(levelTextChooseColor);
            }
            canvas.drawText(levelTexts[i], mTotalWith / levels * (i + 1) - textwight, textHeight, mPaint);
        }
        int lineY = textHeight + progressHeight / 2 + dpTop(10);

        // 绘制进度条底部
        mPaint.setColor(progressBgColor);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(progressHeight);
        canvas.drawLine(0 + progressHeight / 2, lineY, mTotalWith - progressHeight / 2, lineY, mPaint);

        //绘制进度条
        int reachedPartEnd = (int)(getProgress()*1.0f/getMax()*mTotalWith);
        if (reachedPartEnd>0){
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            //设置进度条的渐变色
            Shader shader = new LinearGradient(0, lineY, getWidth(), lineY, progressStartColor, progressEndColor
                    , Shader.TileMode.REPEAT);
            mPaint.setShader(shader);
            canvas.drawLine(0+progressHeight/2,lineY,reachedPartEnd-progressHeight/2,lineY,mPaint);
            mPaint.setShader(null);
        }
        canvas.restore();
    }
}
