package code_sys.shopping_list.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import code_sys.shopping_list.R
import code_sys.shopping_list.data.ListItem
import code_sys.shopping_list.data.ShoppingRepository
import code_sys.shopping_list.data.SuggestionEngine
import code_sys.shopping_list.databinding.FragmentSuggestItemsBinding

/**
 * После создания списка предлагает товары, которые уже покупались раньше.
 * Отмеченные позиции одним нажатием попадают в новый список.
 */
class SuggestItemsFragment : Fragment() {

    private var _binding: FragmentSuggestItemsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SuggestionsAdapter
    private var listId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuggestItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listId = requireArguments().getString(NavArgs.LIST_ID)
        val list = ShoppingRepository.list(listId)
        if (list == null) {
            findNavController().popBackStack()
            return
        }

        val suggestions: List<SuggestionEngine.Suggestion> = ShoppingRepository.suggestionsFor(listId, 30)
        if (suggestions.isEmpty()) {
            goToDetail()
            return
        }

        binding.subtitle.text = getString(R.string.suggest_subtitle, list.name)

        adapter = SuggestionsAdapter().apply { onSelectionChanged = { updateAddButton() } }
        binding.suggestionsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.suggestionsRecycler.adapter = adapter
        adapter.submit(suggestions)

        binding.buttonAddSelected.setOnClickListener { addSelected() }
        binding.buttonSkip.setOnClickListener { goToDetail() }
        binding.buttonSelectAll.setOnClickListener {
            val selectAll = !adapter.allSelected
            adapter.selectAll(selectAll)
            binding.buttonSelectAll.setText(if (selectAll) R.string.deselect_all else R.string.select_all)
        }

        updateAddButton()
    }

    private fun updateAddButton() {
        val count = adapter.selectedSuggestions().size
        binding.buttonAddSelected.text = getString(R.string.suggest_add, count)
        binding.buttonAddSelected.isEnabled = count > 0
    }

    private fun addSelected() {
        val chosen = adapter.selectedSuggestions()
        ShoppingRepository.addItems(
            listId.orEmpty(),
            chosen.map {
                ListItem(
                    name = it.name,
                    emoji = it.emoji,
                    category = it.category,
                    subcategory = it.subcategory
                )
            }
        )
        goToDetail()
    }

    private fun goToDetail() {
        findNavController().navigate(
            R.id.action_suggest_to_detail,
            NavArgs.ofList(listId)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
