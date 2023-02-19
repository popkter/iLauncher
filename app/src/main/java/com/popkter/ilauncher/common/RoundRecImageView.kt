package com.popkter.ilauncher.common

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.popkter.ilauncher.R


class RoundRecImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    AppCompatImageView(context, attrs, defStyle) {
    private val mBorderColor: Int
    private val mBorderWidth: Int
    private val paintImage: Paint
    private val paintBorder: Paint

    /**
     * 圆角的幅度
     */
    private val mRadius: Float

    /**
     * 是否是圆形
     */
    private val mIsCircle: Boolean

    init {
        this.scaleType = ScaleType.FIT_XY
        // 获取自定属性配置
        val ta = context.obtainStyledAttributes(
            attrs,
            R.styleable.CircleImageView, defStyle, 0
        )
        mRadius = ta.getDimensionPixelSize(R.styleable.CircleImageView_radius, 0).toFloat()
        mIsCircle = ta.getBoolean(R.styleable.CircleImageView_circle, false)
        mBorderColor = ta.getColor(R.styleable.CircleImageView_border_color, DEFAULT_BORDER_COLOR)
        mBorderWidth =
            ta.getDimensionPixelSize(R.styleable.CircleImageView_border_width, DEFAULT_BORDER_WIDTH)
        ta.recycle()
        paintImage = Paint()
        paintImage.isAntiAlias = true
        paintBorder = Paint()
        paintBorder.isAntiAlias = true
    }

    public override fun onDraw(canvas: Canvas) {
        // 去点padding值
        val viewWidth = canvas.width - paddingLeft - paddingRight
        val viewHeight = canvas.height - paddingTop - paddingBottom
        // 获取iamgeView 中的drawable 并转成bitmap
        val image = drawableToBitmap(drawable)
        // 对获取到的bitmap 按照当前imageView的宽高进行缩放
        val reSizeImage = reSizeImage(image, viewWidth, viewHeight)
        val imgWidth = reSizeImage.width
        val imgHight = reSizeImage.height
        paintBorder.color = mBorderColor
        paintBorder.strokeWidth = mBorderWidth.toFloat()
        paintBorder.style = Paint.Style.FILL_AND_STROKE

        // 判断当前需要绘制圆角还是圆
        if (mIsCircle) {
            //画边线
            canvas.drawCircle(
                (viewWidth / 2).toFloat(),
                (viewHeight / 2).toFloat(),
                (Math.min(viewWidth, viewHeight) / 2 - mBorderWidth / 2).toFloat(),
                paintBorder
            )
            //设置画笔
            val bitmapShader =
                BitmapShader(reSizeImage, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            paintImage.shader = bitmapShader
            //画布移动(x,y),移动量为边框宽度
            canvas.translate(mBorderWidth.toFloat(), mBorderWidth.toFloat())
            canvas.drawCircle(
                (imgWidth / 2).toFloat(),
                (imgHight / 2).toFloat(),
                (Math.min(imgWidth, imgHight) / 2).toFloat(),
                paintImage
            )
        } else {
            //画边线
            val rectB = RectF(
                (mBorderWidth / 2).toFloat(),
                (mBorderWidth / 2).toFloat(),
                (viewWidth - mBorderWidth / 2).toFloat(),
                (viewHeight - mBorderWidth / 2).toFloat()
            )
            canvas.drawRoundRect(rectB, mRadius, mRadius, paintBorder)
            val bitmapShader =
                BitmapShader(reSizeImage, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            paintImage.shader = bitmapShader
            val rect = RectF(0f, 0f, imgWidth.toFloat(), imgHight.toFloat())
            //弧度半径(有边框无圆角)
            val radius: Float = if (mBorderWidth == 0) mRadius else 0F
            //画布移动(x,y),移动量为边框宽度
            canvas.translate(mBorderWidth.toFloat(), mBorderWidth.toFloat())
            canvas.drawRoundRect(rect, radius, radius, paintImage)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    /**
     * drawable转bitmap
     *
     * @param drawable
     * @return
     */
    private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        } else if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        //        //后加的判断xUtils注解时，需要单独处理
//        else if(drawable instanceof AsyncDrawable){
//            Bitmap b = Bitmap
//                    .createBitmap(
//                            getWidth(),
//                            getHeight(),
//                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
//                                    : Bitmap.Config.RGB_565);
//            Canvas canvas1 = new Canvas(b);
//            // canvas.setBitmap(bitmap);
//            drawable.setBounds(0, 0, getWidth(),
//                    getHeight());
//            drawable.draw(canvas1);
//            return b;
//        }
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicHeight,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * 重设Bitmap的宽高
     *
     * @param bitmap
     * @param newWidth
     * @param newHeight
     * @return
     */
    private fun reSizeImage(bitmap: Bitmap?, newWidth: Int, newHeight: Int): Bitmap {
        var width = bitmap!!.width
        var height = bitmap.height

        //取得正方形边长
        val sideL = Math.min(width, height)
        //取得原点轴坐标
        var x = 0
        var y = 0
        if (width > height) {
            x = (width - height) / 2
        } else {
            y = (height - width) / 2
        }
        val bitmapSquare = Bitmap.createBitmap(bitmap, x, y, sideL, sideL)
        width = bitmapSquare.width
        height = bitmapSquare.height

        // 计算出缩放比
        val scaleHeight = (newHeight - mBorderWidth * 2).toFloat() / height
        val scaleWidth = (newWidth - mBorderWidth * 2).toFloat() / width
        // 矩阵缩放bitmap
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bitmapSquare, 0, 0, width, height, matrix, true)
    }

    companion object {
        private const val DEFAULT_BORDER_WIDTH = 0
        private const val DEFAULT_BORDER_COLOR = Color.WHITE
    }
}