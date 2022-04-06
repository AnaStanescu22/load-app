package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.content_main.view.*
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var text: String
    private var buttonBackgroundColor = R.attr.button_background_color
    private var progress: Float = 0f

    private var valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (new) {
            ButtonState.Loading -> {
                text = "We are Downloading"
                invalidate()
                requestLayout()

                buttonBackgroundColor = Color.parseColor("#004349")
                invalidate()
                requestLayout()

                valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                    addUpdateListener {
                        progress = animatedValue as Float
                        invalidate()
                    }
                    repeatMode = ValueAnimator.REVERSE
                    repeatCount = ValueAnimator.INFINITE
                    duration = 3000
                    start()
                }
                custom_button.isEnabled = false
            }

            ButtonState.Completed -> {
                text = "Downloaded"
                invalidate()
                requestLayout()

                buttonBackgroundColor = Color.parseColor("#07C2AA")
                invalidate()
                requestLayout()

                valueAnimator.cancel()

                progress = 0f
                custom_button.isEnabled = true
            }

            ButtonState.Clicked -> {
            }
        }
        invalidate()
    }


    init {
        context.theme.obtainStyledAttributes( attrs, R.styleable.LoadingButton,0,0).apply {
            try{
                text = getString(R.styleable.LoadingButton_text).toString()
                buttonBackgroundColor = ContextCompat.getColor(context,R.color.colorPrimary)
            } finally {
                recycle()
            }
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    fun setLoadingButtonState(state: ButtonState) {
        buttonState = state
    }
}