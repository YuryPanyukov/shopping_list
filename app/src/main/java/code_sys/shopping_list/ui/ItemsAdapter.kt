package code_sys.shopping_list.ui

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import code_sys.shopping_list.data.ListItem
import code_sys.shopping_list.databinding.ItemProductRowBinding

class ItemsAdapter(
    private val onToggle: (ListItem, Boolean) -> Unit,
    private val onRemove: (ListItem) -> Unit
) : RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {

    private val items = mutableListOf<ListItem>()

    fun submit(newItems: List<ListItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun setDone(itemId: String, done: Boolean) {
        val index = items.indexOfFirst { it.id == itemId }
        if (index < 0) return
        items[index] = items[index].copy(done = done)
        notifyItemChanged(index)
    }

    fun removeById(itemId: String) {
        val index = items.indexOfFirst { it.id == itemId }
        if (index < 0) return
        items.removeAt(index)
        notifyItemRemoved(index)
    }

    class ViewHolder(val binding: ItemProductRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemProductRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val binding = holder.binding

        binding.itemCheck.setOnCheckedChangeListener(null)
        binding.itemCheck.isChecked = item.done
        binding.itemCheck.setOnCheckedChangeListener { _, checked -> onToggle(item, checked) }

        binding.itemEmoji.text = item.emoji
        binding.itemName.text = item.name

        val paintFlags = binding.itemName.paintFlags
        binding.itemName.paintFlags = if (item.done) {
            paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        binding.itemName.alpha = if (item.done) 0.55f else 1f
        binding.itemEmoji.alpha = if (item.done) 0.55f else 1f

        val path = listOf(item.subcategory, item.category)
            .filter { it.isNotBlank() }
            .joinToString(" · ")
        binding.itemCategory.text = path
        binding.itemCategory.isVisible = path.isNotEmpty()

        binding.buttonRemove.setOnClickListener { onRemove(item) }
    }
}
