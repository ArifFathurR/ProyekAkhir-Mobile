package com.example.proyekakhir.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CustomCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 38f
    }
    private val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG)

    private val colorBlue = Color.parseColor("#1565C0")
    private val colorBlack = Color.parseColor("#212121")
    private val colorGray = Color.parseColor("#BDBDBD")
    private val colorWhite = Color.WHITE
    private val colorOrange = Color.parseColor("#E27303")
    private val colorTodayBg = Color.parseColor("#E3F2FD")

    val displayCalendar: Calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }

    var eventDateSet: Set<String> = emptySet()
    var selectedDateStr: String? = null
    var onDateClick: ((String) -> Unit)? = null

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr: String = dateFormat.format(Calendar.getInstance().time)

    private data class DayCell(val dateStr: String, val cx: Float, val cy: Float)
    private val dayCells = mutableListOf<DayCell>()

    private var cellWidth = 0f
    private val cellHeight = 80f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        cellWidth = width / 7f
        val rows = getRowCount()
        setMeasuredDimension(width, (rows * cellHeight).toInt())
    }

    private fun getRowCount(): Int {
        val cal = displayCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstCol = getDayColumn(cal.get(Calendar.DAY_OF_WEEK))
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return Math.ceil((firstCol + daysInMonth) / 7.0).toInt()
    }

    private fun getDayColumn(dayOfWeek: Int): Int {
        return when (dayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        dayCells.clear()

        val cal = displayCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val firstCol = getDayColumn(cal.get(Calendar.DAY_OF_WEEK))
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val prevCal = cal.clone() as Calendar
        prevCal.add(Calendar.MONTH, -1)
        val daysInPrevMonth = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        var col = 0
        var row = 0

        // Hari bulan sebelumnya
        for (i in firstCol - 1 downTo 0) {
            val day = daysInPrevMonth - i
            val cx = col * cellWidth + cellWidth / 2
            val cy = row * cellHeight + cellHeight / 2
            drawDay(canvas, day.toString(), cx, cy, isCurrentMonth = false, dateStr = "")
            col++
        }

        // Hari bulan ini
        for (day in 1..daysInMonth) {
            if (col == 7) { col = 0; row++ }
            val cx = col * cellWidth + cellWidth / 2
            val cy = row * cellHeight + cellHeight / 2

            cal.set(Calendar.DAY_OF_MONTH, day)
            val dateStr = dateFormat.format(cal.time)

            drawDay(canvas, day.toString(), cx, cy, isCurrentMonth = true, dateStr = dateStr)
            dayCells.add(DayCell(dateStr, cx, cy))
            col++
        }

        // Hari bulan berikutnya
        var nextDay = 1
        while (col < 7 && col > 0) {
            val cx = col * cellWidth + cellWidth / 2
            val cy = row * cellHeight + cellHeight / 2
            drawDay(canvas, nextDay.toString(), cx, cy, isCurrentMonth = false, dateStr = "")
            col++
            nextDay++
        }
    }

    private fun drawDay(
        canvas: Canvas,
        text: String,
        cx: Float,
        cy: Float,
        isCurrentMonth: Boolean,
        dateStr: String
    ) {
        if (!isCurrentMonth) {
            paintText.color = colorGray
            paintText.typeface = Typeface.DEFAULT
            val textOffset = (paintText.descent() - paintText.ascent()) / 2 - paintText.descent()
            canvas.drawText(text, cx, cy + textOffset, paintText)
            return
        }

        val isSelected = dateStr == selectedDateStr
        val isToday = dateStr == todayStr
        val hasEvent = eventDateSet.contains(dateStr)
        val radius = cellHeight * 0.38f
        val textOffset = (paintText.descent() - paintText.ascent()) / 2 - paintText.descent()

        when {
            // Tanggal dipilih → solid biru
            isSelected -> {
                paintCircle.style = Paint.Style.FILL
                paintCircle.color = colorBlue
                canvas.drawCircle(cx, cy, radius, paintCircle)
                paintText.color = colorWhite
                paintText.typeface = Typeface.DEFAULT_BOLD
            }
            // Hari ini → background biru muda + teks biru bold
            isToday -> {
                paintCircle.style = Paint.Style.FILL
                paintCircle.color = colorBlue
                canvas.drawCircle(cx, cy, radius, paintCircle)
                paintText.color = colorWhite
                paintText.typeface = Typeface.DEFAULT_BOLD
            }
            // Ada event → outline biru + teks biru bold
            hasEvent -> {
                paintCircle.style = Paint.Style.FILL
                paintCircle.color = colorOrange
                canvas.drawCircle(cx, cy, radius, paintCircle)
                paintText.color = colorWhite
                paintText.typeface = Typeface.DEFAULT_BOLD
            }
            // Biasa
            else -> {
                paintText.color = colorBlack
                paintText.typeface = Typeface.DEFAULT
            }
        }

        canvas.drawText(text, cx, cy + textOffset, paintText)
        paintText.typeface = Typeface.DEFAULT
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val x = event.x
            val y = event.y
            for (cell in dayCells) {
                val dx = x - cell.cx
                val dy = y - cell.cy
                if (dx * dx + dy * dy <= (cellHeight * 0.45f) * (cellHeight * 0.45f)) {
                    onDateClick?.invoke(cell.dateStr)
                    return true
                }
            }
        }
        return true
    }

    fun refresh() {
        requestLayout()
        invalidate()
    }
}