package code_sys.shopping_list.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import code_sys.shopping_list.R
import code_sys.shopping_list.data.ShoppingList
import code_sys.shopping_list.databinding.ItemShoppingListBinding

class ShoppingListsAdapter(
    private val onClick: (ShoppingList) -> Unit,
    private val onLongClick: (ShoppingList) -> Unit,
    private val onDelete: (ShoppingList) -> Unit
) : RecyclerView.Adapter<ShoppingListsAdapter.ViewHolder>() {

    private val items = mutableListOf<ShoppingList>()

    fun submit(lists: List<ShoppingList>) {
        items.clear()
        items.addAll(lists)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemShoppingListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemShoppingListBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = items[position]
        val binding = holder.binding
        binding.listName.text = list.name
        binding.listSubtitle.text = subtitle(binding.root.context, list)
        binding.root.setOnClickListener { onClick(list) }
        binding.root.setOnLongClickListener {
            onLongClick(list)
            true
        }
        binding.buttonDelete.setOnClickListener { onDelete(list) }
    }

    private fun subtitle(context: Context, list: ShoppingList): String {
        val date = formatDate(list.createdAt)
        if (list.totalItems == 0) return context.getString(R.string.list_subtitle_empty, date)
        val count = context.resources
            .getQuantityString(R.plurals.items_count, list.totalItems, list.totalItems)
        return context.getString(R.string.list_subtitle, count, list.boughtItems, date)
    }
}
