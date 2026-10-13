package code_sys.shopping_list.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import code_sys.shopping_list.R
import code_sys.shopping_list.data.SuggestionEngine
import code_sys.shopping_list.databinding.ItemSuggestRowBinding

class SuggestionsAdapter : RecyclerView.Adapter<SuggestionsAdapter.ViewHolder>() {

    private val items = mutableListOf<SuggestionEngine.Suggestion>()
    private val selected = mutableSetOf<String>()

    var onSelectionChanged: ((Int) -> Unit)? = null

    fun submit(suggestions: List<SuggestionEngine.Suggestion>) {
        items.clear()
        items.addAll(suggestions)
        selected.retainAll(items.map { it.name }.toSet())
        notifyDataSetChanged()
    }

    fun selectedSuggestions(): List<SuggestionEngine.Suggestion> =
        items.filter { selected.contains(it.name) }

    val allSelected: Boolean
        get() = items.isNotEmpty() && selected.size == items.size

    fun selectAll(select: Boolean) {
        selected.clear()
        if (select) selected.addAll(items.map { it.name })
        notifyDataSetChanged()
        onSelectionChanged?.invoke(selected.size)
    }

    class ViewHolder(val binding: ItemSuggestRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemSuggestRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val suggestion = items[position]
        val binding = holder.binding
        val checked = selected.contains(suggestion.name)

        binding.suggestEmoji.text = suggestion.emoji
        binding.suggestName.text = suggestion.name
        binding.suggestCount.text = binding.root.context.getString(R.string.used_times, suggestion.timesUsed)
        binding.suggestCheck.isChecked = checked

        val toggle = {
            if (!selected.add(suggestion.name)) selected.remove(suggestion.name)
            notifyItemChanged(position)
            onSelectionChanged?.invoke(selected.size)
        }
        binding.root.setOnClickListener { toggle() }
        binding.suggestCheck.setOnClickListener { toggle() }
    }
}
