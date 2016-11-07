package milestone.ewalk.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import milestone.ewalk.util.Util;

/***
 * 自定义柱状图进度条
 * Created by ltf on 2016/6/14.
 */
public class SpringProgressView extends View {

    /**分段颜色*/
    private static final int[] SECTION_COLORS = {Color.RED,Color.YELLOW,Color.GREEN};
    /**进度条最大值*/
    private float maxCount=100;
    /**进度条当前值*/
    private float currentCount;
    /**画笔*/
    private Paint mPaint;
    private int mWidth,mHeight;

    public SpringProgressView(Context context, AttributeSet attrs,
                              int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public SpringProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public SpringProgressView(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint = new Paint();

        mPaint.setAntiAlias(true);
        int round = mWidth/2;
        float section = currentCount/maxCount;
        RectF rectBg = new RectF(0, 0, mWidth, mHeight);
        LinearGradient shader = new LinearGradient(0, 0, mWidth,mHeight, SECTION_COLORS,null, Shader.TileMode.MIRROR);
        mPaint.setShader(shader);
//        canvas.drawRect(rectBg,mPaint);

//        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        mPaint.setStyle(Paint.Style.FILL);//设置填满
        if(mHeight*section<=dipToPx(15)){
            RectF rectProgressBg = new RectF(0,mHeight*(1-section), mWidth,mHeight );
            canvas.drawRect(rectProgressBg, mPaint);
        }else{
            RectF oval2 = new RectF(0, mHeight*(1-section), mWidth, mHeight*(1-section)+dipToPx(15));// 设置个新的长方形，扫描测量
//            // 画弧，第一个参数是RectF：该类是第二个参数是角度的开始，第三个参数是多少度，第四个参数是真的时候画扇形，是假的时候画弧线
            canvas.drawArc(oval2, 0, -180, true, mPaint);
            RectF rectProgressBg = new RectF(0,mHeight*(1-section)+dipToPx(15)/2, mWidth,mHeight );
            canvas.drawRect(rectProgressBg, mPaint);
        }

    }

    private int dipToPx(int dip) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }

    /***
     * 设置最大的进度值
     * @param maxCount
     */
    public void setMaxCount(float maxCount) {
        this.maxCount = maxCount;
    }

    /***
     * 设置当前的进度值
     * @param currentCount
     */
    public void setCurrentCount(float currentCount) {
        this.currentCount = currentCount > maxCount ? maxCount : currentCount;
        invalidate();
    }

    public float getMaxCount() {
        return maxCount;
    }

    public float getCurrentCount() {
        return currentCount;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
//        if (widthSpecMode == MeasureSpec.EXACTLY || widthSpecMode == MeasureSpec.AT_MOST) {
//            mWidth = widthSpecSize;
//        } else {
//            mWidth = 0;
//        }
//        if (heightSpecMode == MeasureSpec.AT_MOST || heightSpecMode == MeasureSpec.UNSPECIFIED) {
//            mHeight = dipToPx(15);
//        } else {
//            mHeight = heightSpecSize;
//        }
        if (widthSpecMode == MeasureSpec.EXACTLY || widthSpecMode == MeasureSpec.AT_MOST) {
            mWidth = dipToPx(15);
//            mWidth = widthSpecSize;
        } else {
            mWidth = widthSpecSize;
        }
        if (heightSpecMode == MeasureSpec.AT_MOST || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            mHeight = heightSpecSize;
        } else {
            mHeight = 0;
        }
        setMeasuredDimension(mWidth, mHeight);
    }



}
