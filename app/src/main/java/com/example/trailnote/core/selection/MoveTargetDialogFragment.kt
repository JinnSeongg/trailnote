package com.example.trailnote.core.selection

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailnote.core.util.InlineQuickAdd
import com.example.trailnote.databinding.DialogMoveTargetBinding
import com.example.trailnote.databinding.ItemMoveTargetBinding

class MoveTargetDialogFragment(
    private val title: String,
    private val addHint: String,
    private val loadTargets: () -> List<MoveTarget>,
    private val onAddTarget: (String) -> Unit,
    private val onTargetSelected: (MoveTarget) -> Unit
) : DialogFragment() {
    private var binding: DialogMoveTargetBinding? = null
    private val adapter = MoveTargetAdapter { target ->
        onTargetSelected(target)
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            setCanceledOnTouchOutside(true)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = DialogMoveTargetBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val current = binding ?: return
        current.titleText.text = title
        current.targetList.layoutManager = LinearLayoutManager(requireContext())
        current.targetList.adapter = adapter
        renderTargets()

        InlineQuickAdd.bind(current.targetQuickAdd.root) { text ->
            onAddTarget(text)
            renderTargets()
        }
        current.addTargetButton.setOnClickListener {
            InlineQuickAdd.show(current.targetQuickAdd.root, addHint)
        }
    }

    private fun renderTargets() {
        adapter.submitList(loadTargets())
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * DIALOG_WIDTH_RATIO).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private companion object {
        const val DIALOG_WIDTH_RATIO = 0.9f
    }
}

data class MoveTarget(
    val id: String,
    val title: String
)

private class MoveTargetAdapter(
    private val onClick: (MoveTarget) -> Unit
) : RecyclerView.Adapter<MoveTargetAdapter.ViewHolder>() {
    private val items = mutableListOf<MoveTarget>()

    fun submitList(newItems: List<MoveTarget>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMoveTargetBinding.inflate(LayoutInflater.from(parent.context), parent, false), onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(
        private val binding: ItemMoveTargetBinding,
        private val onClick: (MoveTarget) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MoveTarget) {
            binding.titleText.text = item.title
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
