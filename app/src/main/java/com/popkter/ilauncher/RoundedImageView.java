package com.popkter.ilauncher;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class RoundedImageView extends AppCompatImageView {


    private static int DEFAULT_SIZE_VALUE = 0;
    private static int DEFAULT_BORDER_COLOR = Color.WHITE;

    private int _border_color;
    private int _border_width;
    private Paint _paint_image;
    private Paint _paint_border;

    private float _radius;

    private boolean _is_circle;

    public RoundedImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public RoundedImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        setScaleType(ScaleType.FIT_XY);
        TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyleAttr, 0);

        _radius = t.getDimensionPixelSize(R.styleable.CircleImageView_radius, DEFAULT_SIZE_VALUE);
        _is_circle = t.getBoolean(R.styleable.CircleImageView_circle, false);
        _border_color = t.getColor(R.styleable.CircleImageView_border_color, DEFAULT_BORDER_COLOR);
        _border_width = t.getDimensionPixelSize(R.styleable.CircleImageView_border_width, DEFAULT_SIZE_VALUE);
        t.recycle();

        _paint_image = new Paint();
        _paint_image.setAntiAlias(true);
        _paint_border = new Paint();
        _paint_image.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        Bitmap bitmap = drawableToBitmap(getDrawable());
        Bitmap realBitmap = resizeBitMap(bitmap, viewWidth, viewHeight);
        int bitmapWidth = realBitmap.getWidth();
        int bitmapHeight = realBitmap.getHeight();
        _paint_border.setColor(_border_color);
        _paint_border.setStrokeWidth(_border_width);
        _paint_border.setStyle(Paint.Style.FILL_AND_STROKE);

        if (_is_circle) {
            canvas.drawCircle(
                    (viewWidth / 2F),
                    (viewHeight / 2F),
                    (Math.min(viewWidth, viewHeight) / 2F - _border_width / 2F),
                    _paint_border
            );
            _paint_image.setShader(new BitmapShader(realBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            canvas.translate(_border_width, _border_width);
            canvas.drawCircle(
                    bitmapWidth / 2F,
                    bitmapHeight / 2F,
                    Math.min(bitmapWidth, bitmapHeight / 2F),
                    _paint_image
            );

        } else {
            RectF rectF = new RectF(
                    (_border_width / 2F),
                    (_border_width / 2),
                    (viewWidth - _border_width / 2),
                    (viewHeight - _border_width / 2));

            canvas.drawRoundRect(rectF, _radius, _radius, _paint_border);
            _paint_image.setShader(new BitmapShader(realBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            RectF rectF1 = new RectF(DEFAULT_SIZE_VALUE / 1F, DEFAULT_SIZE_VALUE / 1F, bitmapWidth / 1F, bitmapHeight / 1F);
            float radius = _border_width == 0 ? _radius : 0F;
            canvas.translate(bitmapWidth, bitmapWidth);
            canvas.drawRoundRect(rectF1, radius, radius, _paint_image);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private Bitmap resizeBitMap(Bitmap bitmap, int viewWidth, int viewHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int recLength = Math.min(width, height);
        int x = 0;
        int y = 0;
        if (width > height) {
            x = (width - height) / 2;
        } else {
            y = (height - width) / 2;
        }
        Bitmap bitmapSquare = Bitmap.createBitmap(bitmap, x, y, recLength, recLength);
        width = bitmapSquare.getWidth();
        height = bitmapSquare.getHeight();
        float scaleHeight = (viewHeight - _border_width * 2F) / height;
        float scaleWidth = (viewWidth - _border_width * 2F) / width;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmapSquare, DEFAULT_SIZE_VALUE, DEFAULT_SIZE_VALUE, width, height, matrix, true);
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (null == drawable) {
            return null;
        } else if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            Bitmap bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicHeight(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888
            );
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(DEFAULT_SIZE_VALUE, DEFAULT_SIZE_VALUE, canvas.getWidth(), canvas.getHeight());
            return bitmap;
        }
    }
}
