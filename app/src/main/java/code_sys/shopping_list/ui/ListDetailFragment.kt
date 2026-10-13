package code_sys.shopping_list.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import code_sys.shopping_list.R
import code_sys.shopping_list.data.ListItem
import code_sys.shopping_list.data.ProductCatalog
import code_sys.shopping_list.data.ShoppingRepository
import code_sys.shopping_list.databinding.FragmentListDetailBinding

/** Товары одного списка: отметки «куплено», ручной ввод, переход к крутилке. */
class ListDetailFragment : Fragment() {

    private var _binding: FragmentListDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ItemsAdapter
    private var listId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listId = requireArguments().getString(NavArgs.LIST_ID)
        if (ShoppingRepository.list(listId) == null) {
            findNavController().popBackStack()
            return
        }

        adapter = ItemsAdapter(
            onToggle = { item, done -> toggle(item, done) },
            onRemove = { item -> remove(item) }
        )
        binding.itemsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.itemsRecycler.adapter = adapter

        binding.buttonAddItem.setOnClickListener { addTypedItem() }
        binding.itemInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addTypedItem()
                true
            } else {
                false
            }
        }
        binding.buttonClearBought.setOnClickListener { clearBought() }
        binding.fabQuickAdd.setOnClickListener {
            findNavController().navigate(
                R.id.action_detail_to_quick_add,
                NavArgs.ofList(listId)
            )
        }
        binding.buttonShare.setOnClickListener { shareList() }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val list = ShoppingRepository.list(listId) ?: return
        (activity as? AppCompatActivity)?.supportActionBar?.title = list.name
        adapter.submit(list.items)
        binding.emptyView.isVisible = list.items.isEmpty()
        binding.progressText.text = getString(R.string.progress_bought, list.boughtItems, list.totalItems)
        binding.buttonClearBought.isVisible = list.boughtItems > 0
    }

    private fun addTypedItem() {
        val typed = binding.itemInput.text?.toString()?.trim().orEmpty()
        if (typed.isEmpty()) {
            binding.itemInputLayout.error = getString(R.string.item_hint)
            return
        }
        binding.itemInputLayout.error = null

        val found = ProductCatalog.findByName(typed)
        val item = ListItem(
            name = found?.product?.name ?: typed,
            emoji = found?.product?.emoji ?: ListItem.DEFAULT_EMOJI,
            category = found?.category.orEmpty(),
            subcategory = found?.subcategory.orEmpty()
        )

        if (ShoppingRepository.addItem(listId.orEmpty(), item)) {
            binding.itemInput.setText("")
            refresh()
        } else {
            Snackbar.make(binding.root, getString(R.string.already_in_list, item.name), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun toggle(item: ListItem, done: Boolean) {
        ShoppingRepository.setDone(listId.orEmpty(), item.id, done)
        adapter.setDone(item.id, done)
        val list = ShoppingRepository.list(listId) ?: return
        binding.progressText.text = getString(R.string.progress_bought, list.boughtItems, list.totalItems)
        binding.buttonClearBought.isVisible = list.boughtItems > 0
    }

    private fun remove(item: ListItem) {
        ShoppingRepository.removeItem(listId.orEmpty(), item.id)
        adapter.removeById(item.id)
        val list = ShoppingRepository.list(listId)
        binding.emptyView.isVisible = list == null || list.items.isEmpty()
        if (list != null) {
            binding.progressText.text = getString(R.string.progress_bought, list.boughtItems, list.totalItems)
            binding.buttonClearBought.isVisible = list.boughtItems > 0
        }
        Snackbar.make(binding.root, R.string.removed, Snackbar.LENGTH_SHORT)
            .setAction(R.string.undo) {
                ShoppingRepository.addItem(listId.orEmpty(), item)
                refresh()
            }
            .show()
    }

    /** Открывает системный диалог «Поделиться» с текстом списка. */
    private fun shareList() {
        val list = ShoppingRepository.list(listId) ?: return
        val text = ShareBuilder.build(list)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(send, getString(R.string.share_list_chooser)))
    }

    private fun clearBought() {
        val count = ShoppingRepository.removeBought(listId.orEmpty())
        refresh()
        if (count > 0) {
            Snackbar.make(binding.root, getString(R.string.clear_bought_done, count), Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
