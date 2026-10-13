package code_sys.shopping_list.ui

import code_sys.shopping_list.data.ListItem
import code_sys.shopping_list.data.ShoppingList

/**
 * Формирует текст списка покупок для отправки через системную кнопку «Поделиться».
 */
object ShareBuilder {

    fun build(list: ShoppingList): String = buildString {
        append(header(list))
        if (list.items.isEmpty()) {
            append("\n\n")
            append(EMPTY_SUFFIX)
        } else {
            append("\n\n")
            appendLine(list.items.joinToString("\n") { it.row() })
            append(progress(list))
        }
    }.trim()

    private fun header(list: ShoppingList) =
        "🛒 Список «${list.name}» (${formatDate(list.createdAt)})"

    private fun progress(list: ShoppingList) =
        "Куплено ${list.boughtItems} из ${list.totalItems}"

    private fun ListItem.row(): String = if (done) "✅ $name" else "⬜ $name"

    private const val EMPTY_SUFFIX = "Список пуст"
}
