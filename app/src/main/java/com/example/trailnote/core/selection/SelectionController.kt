package com.example.trailnote.core.selection

class SelectionController {
    private val selectedIds = linkedSetOf<String>()
    private val listeners = linkedSetOf<(SelectionState) -> Unit>()
    var selectionScope: String? = null
        private set
    var isInSelectionMode: Boolean = false
        private set

    val selectedItemIds: Set<String>
        get() = selectedIds.toSet()

    fun addStateListener(listener: (SelectionState) -> Unit) {
        listeners.add(listener)
        listener(currentState())
    }

    fun removeStateListener(listener: (SelectionState) -> Unit) {
        listeners.remove(listener)
    }

    fun enter(selectionScope: String?, initialSelectedIds: Collection<String> = emptyList()) {
        this.selectionScope = selectionScope
        selectedIds.clear()
        selectedIds.addAll(initialSelectedIds)
        isInSelectionMode = true
        notifyChanged()
    }

    fun toggle(itemId: String, itemScope: String? = selectionScope) {
        if (!isInSelectionMode) {
            enter(itemScope, listOf(itemId))
            return
        }
        if (itemScope != selectionScope) return
        if (!selectedIds.add(itemId)) {
            selectedIds.remove(itemId)
        }
        if (selectedIds.isEmpty()) {
            exit()
            return
        }
        notifyChanged()
    }

    fun setSelected(itemId: String, itemScope: String?, selected: Boolean) {
        if (!isInSelectionMode) {
            enter(itemScope, emptyList())
        }
        if (itemScope != selectionScope) return
        if (selected) {
            selectedIds.add(itemId)
        } else {
            selectedIds.remove(itemId)
        }
        if (selectedIds.isEmpty()) {
            exit()
            return
        }
        notifyChanged()
    }

    fun clearSelection() {
        selectedIds.clear()
        notifyChanged()
    }

    fun exit() {
        selectedIds.clear()
        selectionScope = null
        isInSelectionMode = false
        notifyChanged()
    }

    private fun notifyChanged() {
        val state = currentState()
        listeners.forEach { it(state) }
    }

    private fun currentState(): SelectionState {
        return SelectionState(
            isInSelectionMode = isInSelectionMode,
            selectedItemIds = selectedItemIds,
            selectionScope = selectionScope
        )
    }
}

data class SelectionState(
    val isInSelectionMode: Boolean,
    val selectedItemIds: Set<String>,
    val selectionScope: String?
)
