package code_sys.shopping_list.data

import java.util.UUID

/** Товар внутри списка покупок. */
data class ListItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String = DEFAULT_EMOJI,
    val category: String = "",
    val subcategory: String = "",
    val done: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val DEFAULT_EMOJI = "\uD83D\uDED2"
    }
}

/** Список покупок. */
data class ShoppingList(
    val id: String,
    val name: String,
    val createdAt: Long,
    val items: List<ListItem> = emptyList()
) {
    val totalItems: Int get() = items.size
    val boughtItems: Int get() = items.count { it.done }

    /** Ищет товар по названию без учёта регистра и лишних пробелов вокруг. */
    fun containsItem(name: String): Boolean {
        val key = name.trim()
        return items.any { it.name.trim().equals(key, ignoreCase = true) }
    }

    val itemNames: Set<String> get() = items.map { it.name }.toSet()
}
