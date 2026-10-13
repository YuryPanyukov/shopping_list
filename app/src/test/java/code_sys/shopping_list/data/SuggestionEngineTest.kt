package code_sys.shopping_list.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggestionEngineTest {

    private fun list(id: String, createdAt: Long, vararg items: ListItem) =
        ShoppingList(id = id, name = "Список $id", createdAt = createdAt, items = items.toList())

    @Test
    fun `часто покупаемые товары идут первыми`() {
        val lists = listOf(
            list("a", 1, item("Молоко"), item("Бананы")),
            list("b", 2, item("Бананы")),
            list("c", 3, item("Бананы"), item("Хлеб"))
        )

        val result = SuggestionEngine.buildSuggestions(lists)

        assertEquals("Бананы", result.first().name)
        assertEquals(3, result.first().timesUsed)
        assertEquals(setOf("Бананы", "Молоко", "Хлеб"), result.map { it.name }.toSet())
    }

    @Test
    fun `при равной частоте выше тот, что покупали позже`() {
        val lists = listOf(
            list("a", 1, item("Молоко")),
            list("b", 5, item("Хлеб"))
        )

        val result = SuggestionEngine.buildSuggestions(lists)

        assertEquals(listOf("Хлеб", "Молоко"), result.map { it.name })
    }

    @Test
    fun `товары, уже добавленные в список, не предлагаются`() {
        val lists = listOf(list("a", 1, item("Бананы"), item("Молоко")))

        val result = SuggestionEngine.buildSuggestions(lists, excludeNames = listOf("бананы"))

        assertEquals(listOf("Молоко"), result.map { it.name })
    }

    @Test
    fun `одинаковые названия в разном регистре объединяются`() {
        val lists = listOf(
            list("a", 1, item("бананы")),
            list("b", 2, item("Бананы"))
        )

        val result = SuggestionEngine.buildSuggestions(lists)

        assertEquals(1, result.size)
        assertEquals(2, result.single().timesUsed)
    }

    @Test
    fun `берёт эмодзи и категорию из товара крутилки`() {
        val lists = listOf(
            list("a", 1, item("Бананы")),
            list("b", 2, item("Бананы", emoji = "🍌", category = "Еда", subcategory = "Фрукты"))
        )

        val suggestion = SuggestionEngine.buildSuggestions(lists).single()

        assertEquals("🍌", suggestion.emoji)
        assertEquals("Еда", suggestion.category)
        assertEquals("Фрукты", suggestion.subcategory)
    }

    @Test
    fun `количество подсказок ограничено`() {
        val items = (1..40).map { item("Товар $it") }.toTypedArray()
        val lists = listOf(list("a", 1, *items))

        assertEquals(5, SuggestionEngine.buildSuggestions(lists, limit = 5).size)
        assertTrue(SuggestionEngine.buildSuggestions(lists, limit = 0).isEmpty())
    }

    @Test
    fun `пустая история не даёт подсказок`() {
        assertTrue(SuggestionEngine.buildSuggestions(emptyList()).isEmpty())
        assertTrue(SuggestionEngine.buildSuggestions(listOf(list("a", 1))).isEmpty())
    }

    @Test
    fun `товар не дублируется, если встречается в одном списке несколько раз`() {
        val lists = listOf(
            list("a", 1, item("Банан"), item("Банан"))
        )

        val result = SuggestionEngine.buildSuggestions(lists)

        assertEquals(1, result.size)
        assertEquals(1, result.single().timesUsed)
    }

    private fun item(
        name: String,
        emoji: String = ListItem.DEFAULT_EMOJI,
        category: String = "",
        subcategory: String = ""
    ) = ListItem(name = name, emoji = emoji, category = category, subcategory = subcategory)
}
