package code_sys.shopping_list.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import code_sys.shopping_list.data.Product
import code_sys.shopping_list.databinding.ItemProductChipBinding

class ProductAdapter(
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private val items = mutableListOf<Product>()
    private val addedNames = mutableSetOf<String>()

    fun submit(products: List<Product>, alreadyInList: Set<String>) {
        items.clear()
        items.addAll(products)
        addedNames.clear()
        addedNames.addAll(alreadyInList)
        notifyDataSetChanged()
    }

    fun markAdded(name: String) {
        val index = items.indexOfFirst { it.name == name }
        if (index < 0 || !addedNames.add(name)) return
        notifyItemChanged(index)
    }

    class ViewHolder(val binding: ItemProductChipBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemProductChipBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = items[position]
        val binding = holder.binding
        val added = addedNames.contains(product.name)

        binding.productEmoji.text = product.emoji
        binding.productName.text = product.name
        binding.productAdded.isVisible = added
        binding.chipCard.alpha = if (added) 0.55f else 1f
        binding.chipCard.setOnClickListener { onProductClick(product) }
    }
}
