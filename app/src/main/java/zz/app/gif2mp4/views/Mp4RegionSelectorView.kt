package zz.app.gif2mp4.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import zz.app.gif2mp4.R
import kotlin.math.abs
import zz.app.gif2mp4.views.Mp4RegionSelectorView.State.*

class Mp4RegionSelectorView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val arr: TypedArray = context!!.obtainStyledAttributes(attrs, R.styleable.Mp4RegionSelectorView)
    private val color: Int
    private val opacity: Float
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var top = 0.0f
    private var left = 0.0f
    private var right = 0.0f
    private var bottom = 0.0f
    private var lastTop = 0.0f
    private var lastLeft = 0.0f
    private var lastRight = 0.0f
    private var lastBottom = 0.0f
    private var lastWidth = 0.0f
    private var lastHeight = 0.0f
    private val selectorRadius = 20f
    private val borderWidth = 2f
    private val borderPath = Path()
    private val trgn = Region()
    private val lrgn = Region()
    private val rrgn = Region()
    private val brgn = Region()
    private val ltrgn = Region()
    private val rtrgn = Region()
    private val lbrgn = Region()
    private val rbrgn = Region()
    var realLeft = 0.0f
        get() = left / width
        private set
    var realRight = 0.0f
        get() = right / width
        private set
    var realTop = 0.0f
        get() = top / height
        private set
    var realBottom = 0.0f
        get() = bottom / height
        private set
    private var lastX = 0.0f
    private var lastY = 0.0f
    private var lastValue = 0.0f;
    private var state = None
    private val selectorPadding = 30f;
    private val minMargin = 4 * selectorPadding
    private val dashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
    private val lastPoint = PointF()
    private var src = 0
    private var bitmap: Bitmap? = null
    var listener:OnValueChangedListener?=null
    var firstShow=true


    enum class State {
        None, Left, Top, Right, Bottom, LeftTop, RightTop, LeftBotton, RightBottom, Move, Scale, Other
    }

    init {
        color = arr.getColor(R.styleable.Mp4RegionSelectorView_color, Color.GRAY)
        opacity = arr.getFloat(R.styleable.Mp4RegionSelectorView_opacity, 0.6f)
        src = arr.getResourceId(R.styleable.Mp4RegionSelectorView_src, 0)
        arr.recycle()
        if (src != 0)
            bitmap = (context?.getDrawable(src) as BitmapDrawable).bitmap

    }
    fun reset(){
        left=0f
        top=0f
        right= width.toFloat()
        bottom= height.toFloat()
        setSelectorRegion()
        invalidate()
        listener?.onValueChanged()
    }

    private fun setSelectorRegion() {
        trgn.set(((left + right) / 2 - selectorRadius - selectorPadding).toInt(), (top - selectorRadius - selectorPadding).toInt(),
                ((left + right) / 2 + selectorRadius + selectorPadding).toInt(), (top + selectorRadius + selectorPadding).toInt())
        brgn.set(((left + right - selectorPadding) / 2 - selectorRadius).toInt(), (bottom - selectorRadius).toInt(),
                ((left + right) / 2 + selectorRadius + selectorPadding).toInt(), (bottom + selectorRadius + selectorPadding).toInt())
        lrgn.set((left - selectorRadius - selectorPadding).toInt(), ((top + bottom) / 2 - selectorRadius - selectorPadding).toInt(),
                (left + selectorRadius + selectorPadding).toInt(), ((top + bottom) / 2 + selectorRadius + selectorPadding).toInt())
        rrgn.set((right - selectorRadius - selectorPadding).toInt(), ((top + bottom) / 2 - selectorRadius - selectorPadding).toInt(),
                (right + selectorRadius + selectorPadding).toInt(), ((top + bottom) / 2 + selectorRadius + selectorPadding).toInt())
        ltrgn.set((left - selectorRadius - selectorPadding).toInt(), (top - selectorRadius - selectorPadding).toInt(),
                (left + selectorRadius + selectorPadding).toInt(), (top + selectorRadius + selectorPadding).toInt())
        rtrgn.set((right - selectorRadius - selectorPadding).toInt(), (top - selectorRadius - selectorPadding).toInt(),
                (right + selectorRadius + selectorPadding).toInt(), (top + selectorRadius + selectorPadding).toInt())
        lbrgn.set((left - selectorRadius - selectorPadding).toInt(), (bottom - selectorRadius - selectorPadding).toInt(),
                (left + selectorRadius + selectorPadding).toInt(), (bottom + selectorRadius + selectorPadding).toInt())
        rbrgn.set((right - selectorRadius - selectorPadding).toInt(), (bottom - selectorRadius - selectorPadding).toInt(),
                (right + selectorRadius + selectorPadding).toInt(), (bottom + selectorRadius + selectorPadding).toInt())
    }

    override fun onDraw(canvas: Canvas?) {
        canvas as Canvas
        drawBkgnd(canvas)
        drawBoreder(canvas)
        drawSrc(canvas)
        drawSelector(canvas)

    }

    private fun drawSrc(canvas: Canvas) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap!!, Rect(0, 0, bitmap!!.width, bitmap!!.height), RectF(left, top, right, bottom), null)
        }
    }

    private fun drawSelector(canvas: Canvas) {
        paint.reset()
        paint.flags = Paint.ANTI_ALIAS_FLAG
        paint.color = Color.WHITE
        paint.alpha = 200
        paint.style = Paint.Style.FILL
        canvas.drawCircle(left, (top + bottom) / 2, selectorRadius, paint)
        canvas.drawCircle(right, (top + bottom) / 2, selectorRadius, paint)
        canvas.drawCircle((left + right) / 2, top, selectorRadius, paint)
        canvas.drawCircle((left + right) / 2, bottom, selectorRadius, paint)
        canvas.drawCircle(left, top, selectorRadius, paint)
        canvas.drawCircle(right, top, selectorRadius, paint)
        canvas.drawCircle(left, bottom, selectorRadius, paint)
        canvas.drawCircle(right, bottom, selectorRadius, paint)
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        canvas.drawCircle(left, (top + bottom) / 2, selectorRadius, paint)
        canvas.drawCircle(right, (top + bottom) / 2, selectorRadius, paint)
        canvas.drawCircle((left + right) / 2, top, selectorRadius, paint)
        canvas.drawCircle((left + right) / 2, bottom, selectorRadius, paint)
        canvas.drawCircle(left, top, selectorRadius, paint)
        canvas.drawCircle(right, top, selectorRadius, paint)
        canvas.drawCircle(left, bottom, selectorRadius, paint)
        canvas.drawCircle(right, bottom, selectorRadius, paint)
    }

    private fun drawBoreder(canvas: Canvas) {
        paint.reset()
        paint.flags = Paint.ANTI_ALIAS_FLAG
        paint.pathEffect = dashEffect
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        paint.strokeWidth = borderWidth
        canvas.drawPath(borderPath, paint)
    }

    private fun drawBkgnd(canvas: Canvas) {
        paint.reset()
        paint.flags = Paint.ANTI_ALIAS_FLAG
        paint.color = color
        paint.alpha = (opacity * 255).toInt()
        canvas.save()
        borderPath.reset()
        borderPath.addRect(left, top, right, bottom, Path.Direction.CW)
        borderPath.close()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canvas.clipOutPath(borderPath)
        } else {
            canvas.clipPath(borderPath, Region.Op.DIFFERENCE);
        }
        canvas.drawRect(getLeft().toFloat(), getTop().toFloat(), getRight().toFloat(), getBottom().toFloat(), paint)
        canvas.restore()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event as MotionEvent
        val x = event.x
        val y = event.y
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                lastX = x
                lastY = y
                lastWidth = right - left
                lastHeight = bottom - top
                if (lrgn.contains(x.toInt(), y.toInt())) {
                    state = Left
                    lastValue = left
                } else if (trgn.contains(x.toInt(), y.toInt())) {
                    state = Top
                    lastValue = top
                } else if (rrgn.contains(x.toInt(), y.toInt())) {
                    state = Right
                    lastValue = right
                } else if (brgn.contains(x.toInt(), y.toInt())) {
                    state = Bottom
                    lastValue = bottom
                } else if (ltrgn.contains(x.toInt(), y.toInt())) {
                    state = LeftTop
                    lastPoint.set(x, y)
                } else if (rtrgn.contains(x.toInt(), y.toInt())) {
                    state = RightTop
                    lastPoint.set(x, y)
                } else if (lbrgn.contains(x.toInt(), y.toInt())) {
                    state = LeftBotton
                    lastPoint.set(x, y)
                } else if (rbrgn.contains(x.toInt(), y.toInt())) {
                    state = RightBottom
                    lastPoint.set(x, y)
                } else {
                    state = Move
                    lastLeft = left
                    lastRight = right
                    lastTop = top
                    lastBottom = bottom
                    lastPoint.set(x, y)

                }
                return true
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    state = Scale
                    lastLeft = left
                    lastRight = right
                    lastTop = top
                    lastBottom = bottom
                    lastPoint.set(abs(event.getX(0) - event.getX(1)),
                            abs(event.getY(0) - event.getY(1)))
                } else {
                    state = Other
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                when (state) {
                    Left -> {
                        left = lastValue + x - lastX
                        if (left >= right - minMargin)
                            left = right - minMargin
                        if (left < 0) left = 0f
                    }
                    Right -> {
                        right = lastValue + x - lastX
                        if (right <= left + minMargin)
                            right = left + minMargin
                        if (right > width) right = width.toFloat()
                    }
                    Top -> {
                        top = lastValue + y - lastY
                        if (top >= bottom - minMargin)
                            top = bottom - minMargin
                        if (top < 0) top = 0f
                    }

                    Bottom -> {
                        bottom = lastValue + y - lastY
                        if (bottom <= top + minMargin)
                            bottom = top + minMargin
                        if (bottom > height) bottom = height.toFloat()
                    }
                    LeftBotton -> {
                        left = lastPoint.x + x - lastX
                        bottom = lastPoint.y + y - lastY
                        if (left >= right - minMargin)
                            left = right - minMargin
                        if (left < 0) left = 0f
                        if (bottom <= top + minMargin)
                            bottom = top + minMargin
                        if (bottom > height) bottom = height.toFloat()
                    }
                    RightBottom -> {
                        right = lastPoint.x + x - lastX
                        bottom = lastPoint.y + y - lastY
                        if (right <= left + minMargin)
                            right = left + minMargin
                        if (right > width) right = width.toFloat()
                        if (bottom <= top + minMargin)
                            bottom = top + minMargin
                        if (bottom > height) bottom = height.toFloat()
                    }
                    LeftTop -> {
                        left = lastPoint.x + x - lastX
                        top = lastPoint.y + y - lastY
                        if (left >= right - minMargin)
                            left = right - minMargin
                        if (left < 0) left = 0f
                        if (top >= bottom - minMargin)
                            top = bottom - minMargin
                        if (top < 0) top = 0f
                    }
                    RightTop -> {
                        right = lastPoint.x + x - lastX
                        top = lastPoint.y + y - lastY
                        if (right <= left + minMargin)
                            right = left + minMargin
                        if (right > width) right = width.toFloat()
                        if (top >= bottom - minMargin)
                            top = bottom - minMargin
                        if (top < 0) top = 0f
                    }
                    Move -> {
                        val ttop = lastTop + y - lastPoint.y
                        val tleft = lastLeft + x - lastPoint.x
                        val tright = tleft + lastWidth
                        val tbottom = ttop + lastHeight
                        if (tleft < 0) {
                            left = 0f
                            lastLeft = left
                            right = lastWidth
                            lastPoint.x = x
                        } else if (tright > width) {
                            left = width.toFloat() - lastWidth
                            right = width.toFloat()
                            lastLeft = left
                            lastPoint.x = x
                        } else {
                            left = lastLeft + x - lastPoint.x
                            right = left + lastWidth
                        }
                        if (ttop < 0) {
                            top = 0f
                            lastTop = top
                            bottom = lastHeight
                            lastPoint.y = y
                        } else if (tbottom > height) {
                            top = height.toFloat() - lastHeight
                            bottom = height.toFloat()
                            lastTop = top
                            lastPoint.y = y
                        } else {
                            top = lastTop + y - lastPoint.y
                            bottom = top + lastHeight
                        }

                    }
                    Scale -> {
                        val x11 = event.x
                        val x21 = event.getX(1)
                        val y11 = event.y
                        val y21 = event.getY(1)
                        left = lastLeft - (abs(x21 - x11) - lastPoint.x) / 2
                        right = lastRight + (abs(x21 - x11) - lastPoint.x) / 2
                        top = lastTop - (abs(y21 - y11) - lastPoint.y) / 2
                        bottom = lastBottom + (abs(y21 - y11) - lastPoint.y) / 2

                        if (left >= right - minMargin)
                            left = right - minMargin
                        if (left < 0) left = 0f

                        if (top >= bottom - minMargin)
                            top = bottom - minMargin
                        if (top < 0) top = 0f

                        if (right <= left + minMargin)
                            right = left + minMargin
                        if (right > width) right = width.toFloat()

                        if (bottom <= top + minMargin)
                            bottom = top + minMargin
                        if (bottom > height) bottom = height.toFloat()
                        lastWidth = right - left
                        lastHeight = bottom - top
                    }
                    else -> {
                    }
                }
                invalidate()
                setSelectorRegion()
                listener?.onValueChanged()
                return true
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (event.pointerCount == 3) {
                    state = Scale
                    lastTop = top
                    lastBottom = bottom
                    lastLeft = left
                    lastRight = right
                    lastPoint.set(abs(event.getX(0) - event.getX(1)),
                            abs(event.getY(0) - event.getY(1)))
                } else if (event.pointerCount == 2) {
                    state = Move
                    lastTop = top
                    lastBottom = bottom
                    lastLeft = left
                    lastRight = right
                    when (event.actionIndex) {
                        0 -> lastPoint.set(event.getX(1), event.getY(1))
                        1 -> lastPoint.set(event.getX(0), event.getY(0))
                    }
                } else state = Other
                return true
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                state = None
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    interface OnValueChangedListener{
        fun onValueChanged()
    }
}