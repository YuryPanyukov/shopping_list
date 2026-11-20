package code_sys.shopping_list.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import code_sys.shopping_list.MainActivity
import code_sys.shopping_list.R
import code_sys.shopping_list.data.ShoppingList
import code_sys.shopping_list.data.ShoppingRepository
import code_sys.shopping_list.databinding.DialogListNameBinding
import code_sys.shopping_list.databinding.FragmentListsBinding

/** Список всех списков покупок. */
class ListsFragment : Fragment() {

    private var _binding: FragmentListsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ShoppingListsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ShoppingListsAdapter(
            onClick = { list -> openList(list) },
            onLongClick = { list -> showActions(list) },
            onDelete = { list -> confirmDelete(list) }
        )
        binding.listsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.listsRecycler.adapter = adapter
        binding.fabNewList.setOnClickListener { askListName(null) }
    }

    override fun onResume() {
        super.onResume()
        // Заголовок живёт в шапке экрана, а не в панели действий.
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.title_lists)
        (activity as? MainActivity)?.setToolbarVisible(false)
        refresh()
    }

    private fun refresh() {
        val lists = ShoppingRepository.allLists()
        adapter.submit(lists)
        binding.emptyView.isVisible = lists.isEmpty()
    }

    private fun openList(list: ShoppingList) {
        findNavController().navigate(
            R.id.action_lists_to_detail,
            NavArgs.ofList(list.id)
        )
    }

    /** Новый список: спрашиваем название, а затем предлагаем товары из прошлых покупок. */
    private fun askListName(list: ShoppingList?) {
        val input = DialogListNameBinding.inflate(layoutInflater)
        input.nameInput.setText(list?.name.orEmpty())
        input.nameInput.setSelection(input.nameInput.text?.length ?: 0)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (list == null) R.string.create_list_title else R.string.rename)
            .setView(input.root)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(if (list == null) R.string.create else R.string.rename) { _, _ ->
                val name = input.nameInput.text?.toString()?.trim().orEmpty()
                if (list == null) createList(name) else renameList(list, name)
            }
            .create()
            .also { dialog ->
                dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                dialog.show()
                input.nameInput.requestFocus()
            }
    }

    private fun createList(name: String) {
        val list = ShoppingRepository.createList(name)
        val hasHistory = ShoppingRepository.suggestionsFor(list.id, limit = 1).isNotEmpty()
        if (hasHistory) {
            findNavController().navigate(
                R.id.action_lists_to_suggest,
                NavArgs.ofList(list.id)
            )
        } else {
            openList(list)
        }
    }

    private fun renameList(list: ShoppingList, name: String) {
        if (name.isEmpty()) return
        ShoppingRepository.renameList(list.id, name)
        refresh()
    }

    private fun showActions(list: ShoppingList) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.list_actions) + ": " + list.name)
            .setItems(
                arrayOf(
                    getString(R.string.share),
                    getString(R.string.rename),
                    getString(R.string.delete)
                )
            ) { _, which ->
                when (which) {
                    0 -> shareList(list)
                    1 -> askListName(list)
                    2 -> confirmDelete(list)
                }
            }
            .show()
    }

    /** Открывает системный диалог «Поделиться» с текстом списка. */
    private fun shareList(list: ShoppingList) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, ShareBuilder.build(list))
        }
        startActivity(Intent.createChooser(send, getString(R.string.share_list_chooser)))
    }

    private fun confirmDelete(list: ShoppingList) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_list_title)
            .setMessage(getString(R.string.delete_list_message, list.name))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                ShoppingRepository.deleteList(list.id)
                refresh()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
