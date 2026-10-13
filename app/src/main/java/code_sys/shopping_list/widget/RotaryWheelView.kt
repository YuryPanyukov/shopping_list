package code_sys.shopping_list.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.SystemClock
import android.util.AttributeSet
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import code_sys.shopping_list.R
import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Круговая «крутилка»: сектора по кругу, сверху указатель.
 *
 * — потяните за круг и отпустите: колесо «защёлкнется» на ближайшем секторе;
 * — коснитесь сектора: колесо докрутится до него и сектор станет выбранным;
 * — крупная подпись выбранного сектора показывается в центре.
 */
class RotaryWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class Segment(val label: String, val emoji: String, val color: Int)

    /** Вызывается, когда выбор «устоялся» (после докрутки или тапа). */
    var onSelectionChanged: ((index: Int) -> Unit)? = null

    private var segments: List<Segment> = emptyList()
    private var rotationDeg = 0f
    private var lastNotifiedIndex = 0
    private var previewIndex = 0
    private var animator: ValueAnimator? = null
    private var showSectorLabels = true

    private val density = resources.displayMetrics.density
    private fun dp(value: Float) = value * density

    private fun sp(value: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics)

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.wheel_track)
    }
    private val sectorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val hubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.wheel_hub)
    }
    private val hubStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2f)
        color = ContextCompat.getColor(context, R.color.wheel_hub_stroke)
    }
    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.wheel_pointer)
    }
    private val sectorLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = 0xFFFFFFFF.toInt()
    }
    private val sectorEmojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val centerLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        color = ContextCompat.getColor(context, R.color.wheel_text)
    }
    private val centerEmojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }

    private val path = Path()
    private val arcRect = RectF()

    private var centerX = 0f
    private var centerY = 0f
    private var outerRadius = 0f
    private var innerRadius = 0f
    private var bandWidth = 0f

    private var dragging = false
    private var lastTouchAngle = 0f
    private var downX = 0f
    private var downY = 0f
    private var downTime = 0L
    private var moved = false

    private val stepAngle: Float
        get() = if (segments.isEmpty()) 360f else 360f / segments.size

    init {
        isClickable = true
        isHapticFeedbackEnabled = true
    }

    /** Заполняет колесо секторами. Первый сектор встаёт под указатель. */
    fun setSegments(newSegments: List<Segment>) {
        cancelAnimator()
        segments = newSegments
        showSectorLabels = newSegments.size <= 8
        rotationDeg = -stepAngle / 2f
        previewIndex = 0
        lastNotifiedIndex = 0
        invalidate()
    }

    fun currentIndex(): Int = previewIndex

    /** Докрутить до сектора [index]. Уведомление вызывается после докрутки. */
    fun select(index: Int, animate: Boolean = true) {
        if (segments.isEmpty()) return
        val target = alignRotation(index, rotationDeg)
        if (!animate) {
            rotationDeg = target
            settleSelection()
            invalidate()
            return
        }
        animateTo(target, notify = true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // По ширине занимаем всё доступное место, а высота равна диаметру колеса:
        // так круг рисуется по центру экрана, а не прижимается к левому краю.
        val preferred = dp(280f).roundToInt()
        val width = resolveSize(preferred, widthMeasureSpec).coerceAtLeast(dp(160f).roundToInt())
        val height = resolveSize(preferred, heightMeasureSpec).coerceAtMost(width)
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        val available = min(w, h) / 2f - dp(22f)
        outerRadius = available
        innerRadius = available * 0.54f
        bandWidth = outerRadius - innerRadius
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (segments.isEmpty() || outerRadius <= 0f) return

        val count = segments.size
        val step = stepAngle
        val midRadius = (outerRadius + innerRadius) / 2f

        arcRect.set(centerX - midRadius, centerY - midRadius, centerX + midRadius, centerY + midRadius)
        trackPaint.strokeWidth = bandWidth
        canvas.drawArc(arcRect, 0f, 360f, false, trackPaint)

        val gap = min(1.8f, step * 0.07f)
        segments.forEachIndexed { index, segment ->
            val startAngle = -90f + index * step + rotationDeg + gap / 2f
            val sweep = step - gap
            val isPreview = index == previewIndex

            sectorPaint.color = segment.color
            sectorPaint.alpha = if (isPreview) 255 else 110
            sectorPaint.strokeWidth = if (isPreview) bandWidth else bandWidth * 0.76f
            val radius = if (isPreview) midRadius else midRadius + bandWidth * 0.05f
            arcRect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
            canvas.drawArc(arcRect, startAngle, sweep, false, sectorPaint)

            if (!showSectorLabels) return@forEachIndexed

            val midAngle = startAngle + sweep / 2f
            val arcLength = (2f * Math.PI.toFloat() * radius) / count
            canvas.save()
            canvas.rotate(midAngle + 90f, centerX, centerY)

            // Эмодзи — у внешнего края сектора.
            centerEmojiPaint.typeface = android.graphics.Typeface.DEFAULT
            sectorEmojiPaint.textSize = sp(15f)
            val emojiRadius = innerRadius + bandWidth * 0.72f
            canvas.drawText(
                segment.emoji,
                centerX,
                centerY - emojiRadius - verticalCenterOffset(sectorEmojiPaint),
                sectorEmojiPaint
            )

            // Подпись — ближе к центру, сжимаем шрифт под ширину сектора.
            sectorLabelPaint.textSize = sp(12f)
            val textWidth = sectorLabelPaint.measureText(segment.label)
            val maxWidth = arcLength * 0.86f
            if (textWidth > maxWidth && textWidth > 0f) {
                sectorLabelPaint.textSize = (sp(12f) * maxWidth / textWidth).coerceAtLeast(sp(8f))
            }
            val labelRadius = innerRadius + bandWidth * 0.34f
            canvas.drawText(
                segment.label,
                centerX,
                centerY - labelRadius - verticalCenterOffset(sectorLabelPaint),
                sectorLabelPaint
            )
            canvas.restore()
        }

        // Ступица с крупной подписью выбранного сектора.
        canvas.drawCircle(centerX, centerY, innerRadius - dp(3f), hubPaint)
        canvas.drawCircle(centerX, centerY, innerRadius - dp(3f), hubStrokePaint)

        val current = segments[previewIndex.coerceIn(0, segments.size - 1)]
        centerEmojiPaint.textSize = sp(26f)
        centerLabelPaint.textSize = sp(14f)

        val emojiHeight = lineHeight(centerEmojiPaint)
        val labelHeight = lineHeight(centerLabelPaint)
        val blockTop = centerY - (emojiHeight + labelHeight) / 2f
        canvas.drawText(
            current.emoji,
            centerX,
            blockTop - centerEmojiPaint.fontMetrics.ascent,
            centerEmojiPaint
        )
        canvas.drawText(
            current.label,
            centerX,
            blockTop + emojiHeight - centerLabelPaint.fontMetrics.ascent,
            centerLabelPaint
        )

        // Указатель сверху.
        val tipY = centerY - outerRadius + dp(1f)
        path.reset()
        path.moveTo(centerX, tipY)
        path.lineTo(centerX - dp(9f), tipY - dp(15f))
        path.lineTo(centerX + dp(9f), tipY - dp(15f))
        path.close()
        canvas.drawPath(path, pointerPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (segments.isEmpty()) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                cancelAnimator()
                downX = event.x
                downY = event.y
                downTime = SystemClock.uptimeMillis()
                moved = false
                dragging = false
                lastTouchAngle = angleOf(event.x, event.y)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (hypot(event.x - downX, event.y - downY) > touchSlop) {
                    dragging = true
                    moved = true
                }
                if (dragging) {
                    val angle = angleOf(event.x, event.y)
                    val distance = hypot(event.x - centerX, event.y - centerY)
                    if (distance > innerRadius * 0.4f) {
                        rotationDeg += normalizeDelta(angle - lastTouchAngle)
                        val preview = pointerIndex()
                        if (preview != previewIndex) {
                            previewIndex = preview
                            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        }
                        invalidate()
                    }
                    lastTouchAngle = angle
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (dragging) {
                    animateTo(alignRotation(previewIndex, rotationDeg), notify = true)
                } else if (!moved && SystemClock.uptimeMillis() - downTime < 500) {
                    handleTap(event.x, event.y)
                }
                performClick()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                if (dragging) animateTo(alignRotation(previewIndex, rotationDeg), notify = true)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean = super.performClick()

    private fun handleTap(x: Float, y: Float) {
        val distance = hypot(x - centerX, y - centerY)
        if (distance < innerRadius * 0.75f || distance > outerRadius + dp(24f)) return
        val alpha = normalizeDegrees(angleOf(x, y) + 90f - rotationDeg)
        val index = floor(alpha / stepAngle).toInt().coerceIn(0, segments.size - 1)
        animateTo(alignRotation(index, rotationDeg), notify = true)
    }

    /** Ближайший поворот, при котором сектор [index] встаёт под указатель. */
    private fun alignRotation(index: Int, from: Float): Float {
        val target = -(index * stepAngle + stepAngle / 2f)
        var aligned = target
        while (aligned - from > 180f) aligned -= 360f
        while (from - aligned > 180f) aligned += 360f
        return aligned
    }

    private fun animateTo(targetRotation: Float, notify: Boolean) {
        cancelAnimator()
        val startRotation = rotationDeg
        if (kotlin.math.abs(startRotation - targetRotation) < 0.5f) {
            rotationDeg = targetRotation
            previewIndex = pointerIndex()
            invalidate()
            if (notify) notifySelection()
            return
        }
        animator = ValueAnimator.ofFloat(startRotation, targetRotation).apply {
            duration = 340L
            interpolator = DecelerateInterpolator(1.6f)
            addUpdateListener { valueAnimator ->
                rotationDeg = valueAnimator.animatedValue as Float
                previewIndex = pointerIndex()
                invalidate()
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    rotationDeg = targetRotation
                    previewIndex = pointerIndex()
                    invalidate()
                    if (notify) notifySelection()
                }
            })
            start()
        }
    }

    private fun settleSelection() {
        previewIndex = pointerIndex()
        notifySelection()
    }

    private fun notifySelection() {
        lastNotifiedIndex = previewIndex
        onSelectionChanged?.invoke(previewIndex)
    }

    private fun cancelAnimator() {
        animator?.cancel()
        animator = null
    }

    private fun pointerIndex(): Int {
        if (segments.isEmpty()) return 0
        val raw = floor(-rotationDeg / stepAngle).toInt()
        return ((raw % segments.size) + segments.size) % segments.size
    }

    private fun angleOf(x: Float, y: Float): Float =
        Math.toDegrees(atan2((y - centerY).toDouble(), (x - centerX).toDouble())).toFloat()

    private fun normalizeDelta(deg: Float): Float {
        var d = deg
        while (d > 180f) d -= 360f
        while (d < -180f) d += 360f
        return d
    }

    private fun normalizeDegrees(deg: Float): Float {
        var d = deg % 360f
        if (d < 0f) d += 360f
        return d
    }

    private fun lineHeight(paint: Paint): Float = paint.fontMetrics.let { it.descent - it.ascent }

    /** Сдвиг базовой линии, чтобы текст был вертикально по центру. */
    private fun verticalCenterOffset(paint: Paint): Float =
        -(paint.fontMetrics.ascent + paint.fontMetrics.descent) / 2f
}
