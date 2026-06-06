package com.example.trailnote.core.selection

import android.view.GestureDetector
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class RecyclerDragSelectionHelper(
    private val recyclerView: RecyclerView,
    private val selectionController: SelectionController,
    private val getItemId: (Int) -> String?,
    private val getItemScope: (Int) -> String?,
    private val isItemSelected: (Int) -> Boolean,
    private val onDragStarted: (Int) -> Unit = {},
    private val onSelectionChanged: () -> Unit = {}
) : RecyclerView.SimpleOnItemTouchListener() {
    private val processedIds = mutableSetOf<String>()
    private var isDragging = false
    private var dragSelectMode = true
    private var dragScope: String? = null
    private var lastX = 0f
    private var lastY = 0f

    private val gestureDetector = GestureDetector(
        recyclerView.context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onLongPress(e: MotionEvent) {
                val position = findPosition(e.x, e.y)
                if (position == RecyclerView.NO_POSITION) return
                val itemId = getItemId(position) ?: return
                val itemScope = getItemScope(position)

                isDragging = true
                dragSelectMode = !isItemSelected(position)
                dragScope = itemScope
                processedIds.clear()
                onDragStarted(position)
                applySelection(itemId, itemScope)
                recyclerView.parent?.requestDisallowInterceptTouchEvent(true)
            }
        }
    )

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        lastX = e.x
        lastY = e.y
        gestureDetector.onTouchEvent(e)
        if (isDragging) {
            handleTouch(e)
            return true
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        lastX = e.x
        lastY = e.y
        handleTouch(e)
    }

    private fun handleTouch(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                autoScrollIfNeeded(event.y)
                val position = findPosition(event.x, event.y)
                if (position != RecyclerView.NO_POSITION) {
                    val itemId = getItemId(position)
                    val itemScope = getItemScope(position)
                    if (itemId != null) applySelection(itemId, itemScope)
                }
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> stopDragging()
        }
    }

    private fun applySelection(itemId: String, itemScope: String?) {
        if (itemScope != dragScope || !processedIds.add(itemId)) return
        selectionController.setSelected(itemId, itemScope, dragSelectMode)
        onSelectionChanged()
    }

    private fun autoScrollIfNeeded(y: Float) {
        val height = recyclerView.height
        val threshold = AUTO_SCROLL_THRESHOLD_PX
        val delta = when {
            y < threshold -> -AUTO_SCROLL_STEP_PX
            y > height - threshold -> AUTO_SCROLL_STEP_PX
            else -> 0
        }
        if (delta == 0) return
        recyclerView.scrollBy(0, delta)
        recyclerView.postDelayed({
            if (isDragging) {
                val position = findPosition(lastX, lastY)
                if (position != RecyclerView.NO_POSITION) {
                    val itemId = getItemId(position)
                    val itemScope = getItemScope(position)
                    if (itemId != null) applySelection(itemId, itemScope)
                }
                autoScrollIfNeeded(lastY)
            }
        }, AUTO_SCROLL_DELAY_MS)
    }

    private fun findPosition(x: Float, y: Float): Int {
        val child = recyclerView.findChildViewUnder(x, y) ?: return RecyclerView.NO_POSITION
        return recyclerView.getChildAdapterPosition(child)
    }

    private fun stopDragging() {
        isDragging = false
        dragScope = null
        processedIds.clear()
        recyclerView.parent?.requestDisallowInterceptTouchEvent(false)
    }

    private companion object {
        const val AUTO_SCROLL_THRESHOLD_PX = 96
        const val AUTO_SCROLL_STEP_PX = 18
        const val AUTO_SCROLL_DELAY_MS = 32L
    }
}
