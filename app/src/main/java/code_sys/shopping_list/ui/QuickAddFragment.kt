package code_sys.shopping_list.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import code_sys.shopping_list.R
import code_sys.shopping_list.data.Category
import code_sys.shopping_list.data.ListItem
import code_sys.shopping_list.data.Product
import code_sys.shopping_list.data.ProductCatalog
import code_sys.shopping_list.data.ShoppingRepository
import code_sys.shopping_list.data.Subcategory
import code_sys.shopping_list.databinding.FragmentQuickAddBinding
import code_sys.shopping_list.widget.RotaryWheelView

/**
 * Быстрый набор товара без набора названия.
 *
 * Шаг 1 — крутилка из 4 зон (еда, бытовая химия, канцтовары, прочее).
 * Шаг 2 — крутилка подкатегорий выбранной зоны.
 * Шаг 3 — сетка частых товаров: касание сразу добавляет товар в список.
 */
class QuickAddFragment : Fragment() {

    private var _binding: FragmentQuickAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProductAdapter
    private var listId: String? = null
    private var step = STEP_CATEGORY
    private var category: Category? = null
    private var subcategory: Subcategory? = null
    private var addedCount = 0

    private val backCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            goBack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuickAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listId = requireArguments().getString(NavArgs.LIST_ID)
        if (ShoppingRepository.list(listId) == null) {
            findNavController().popBackStack()
            return
        }

        adapter = ProductAdapter { product -> addProduct(product) }
        binding.productsRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.productsRecycler.adapter = adapter

        binding.wheel.onSelectionChanged = { index -> onWheelSelection(index) }
        binding.buttonBack.setOnClickListener { goBack() }
        binding.buttonDone.setOnClickListener { findNavController().popBackStack() }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
        updateCounter()
        showCategories()
    }

    // region уровни крутилки

    private fun showCategories() {
        step = STEP_CATEGORY
        category = null
        subcategory = null
        backCallback.isEnabled = false

        binding.breadcrumb.text = getString(R.string.title_quick_add)
        binding.hint.setText(R.string.hint_choose_zone)
        binding.buttonBack.isVisible = false
        binding.wheel.isVisible = true
        binding.productsRecycler.isVisible = false

        binding.wheel.setSegments(
            ProductCatalog.categories.map {
                RotaryWheelView.Segment(it.name, it.emoji, it.color)
            }
        )
    }

    private fun openCategory(target: Category) {
        step = STEP_SUBCATEGORY
        category = target
        subcategory = null
        backCallback.isEnabled = true

        binding.breadcrumb.text = target.name
        binding.hint.setText(R.string.hint_choose_subcategory)
        binding.buttonBack.isVisible = true
        binding.wheel.isVisible = true
        binding.productsRecycler.isVisible = false

        val palette = ProductCatalog.dialPalette
        binding.wheel.setSegments(
            target.subcategories.mapIndexed { index, item ->
                RotaryWheelView.Segment(item.name, item.emoji, palette[index % palette.size])
            }
        )
    }

    private fun openSubcategory(target: Subcategory) {
        step = STEP_PRODUCTS
        subcategory = target
        backCallback.isEnabled = true

        val parent = category
        binding.breadcrumb.text = if (parent == null) {
            target.name
        } else {
            getString(R.string.breadcrumb, parent.name, target.name)
        }
        binding.hint.setText(R.string.hint_choose_product)
        binding.buttonBack.isVisible = true
        binding.wheel.isVisible = false
        binding.productsRecycler.isVisible = true

        adapter.submit(target.products, currentListNames())
    }

    private fun onWheelSelection(index: Int) {
        when (step) {
            STEP_CATEGORY -> ProductCatalog.categories.getOrNull(index)?.let { openCategory(it) }
            STEP_SUBCATEGORY -> category?.subcategories?.getOrNull(index)?.let { openSubcategory(it) }
        }
    }

    /** @return false, если возвращаться уже некуда (экран можно закрывать). */
    private fun goBack(): Boolean {
        when (step) {
            STEP_PRODUCTS -> {
                val parent = category ?: return false
                openCategory(parent)
                return true
            }

            STEP_SUBCATEGORY -> {
                showCategories()
                return true
            }
        }
        return false
    }

    // endregion

    private fun addProduct(product: Product) {
        val parent = category
        val item = ListItem(
            name = product.name,
            emoji = product.emoji,
            category = parent?.name.orEmpty(),
            subcategory = subcategory?.name.orEmpty()
        )
        if (ShoppingRepository.addItem(listId.orEmpty(), item)) {
            addedCount++
            adapter.markAdded(product.name)
            updateCounter()
        } else {
            Snackbar.make(binding.root, getString(R.string.already_in_list, product.name), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun updateCounter() {
        binding.addedCounter.text = getString(R.string.added_counter, addedCount)
    }

    private fun currentListNames(): Set<String> =
        ShoppingRepository.list(listId)?.itemNames.orEmpty()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val STEP_CATEGORY = 1
        private const val STEP_SUBCATEGORY = 2
        private const val STEP_PRODUCTS = 3
    }
}
