package code_sys.shopping_list.ui

import code_sys.shopping_list.data.ListItem
import code_sys.shopping_list.data.ShoppingList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Проверяет текст, который отправляется при «Поделиться списком». */
class ShareBuilderTest {

    @Test
    fun `empty list renders header and empty suffix`() {
        val text = ShareBuilder.build(list(name = "Пустой", items = emptyList()))
        assertTrue(text.contains("🛒 Список «Пустой»"))
        assertTrue(text.contains("Список пуст"))
    }

    @Test
    fun `items are grouped with checkmarks`() {
        val text = ShareBuilder.build(
            list(
                name = "Продукты",
                items = listOf(
                    ListItem(name = "Молоко", done = true),
                    ListItem(name = "Хлеб", done = false)
                )
            )
        )
        assertTrue(text.contains("✅ Молоко"))
        assertTrue(text.contains("⬜ Хлеб"))
        assertTrue(text.contains("Куплено 1 из 2"))
        assertFalse(text.contains("Список пуст"))
    }

    @Test
    fun `header contains list name`() {
        val text = ShareBuilder.build(list(name = "На дачу", items = listOf(ListItem(name = "Вода"))))
        assertTrue(text.contains("«На дачу»"))
    }

    private fun list(name: String, items: List<ListItem>) = ShoppingList(
        id = "id",
        name = name,
        createdAt = 0L,
        items = items
    )
}
