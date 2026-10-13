package code_sys.shopping_list.data

import android.content.Context
import android.content.SharedPreferences
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Сохранение и чтение списков: данные должны переживать перезапуск приложения,
 * а испорченное хранилище — не ронять его.
 *
 * Хранилище настоящие (SharedPreferences + JSON), Android-платформу подменяет Robolectric,
 * поэтому тесты остаются обычными JVM-тестами.
 */
@RunWith(RobolectricTestRunner::class)
class ShoppingRepositoryTest {

    private val context: Context get() = RuntimeEnvironment.getApplication()

    private val storage: SharedPreferences
        get() = context.getSharedPreferences(ShoppingRepository.PREFS_NAME, Context.MODE_PRIVATE)

    @Before
    fun setUp() {
        storage.edit().clear().commit()
        ShoppingRepository.reset()
        ShoppingRepository.init(context)
    }

    @After
    fun tearDown() {
        ShoppingRepository.reset()
        storage.edit().clear().commit()
    }

    /** Перезапуск приложения: кеш забыт, данные читаются из хранилища заново. */
    private fun restartApp() {
        ShoppingRepository.reset()
        ShoppingRepository.init(context)
    }

    @Test
    fun `пустое хранилище не даёт ни одного списка`() {
        assertTrue(ShoppingRepository.allLists().isEmpty())
    }

    @Test
    fun `список переживает перезапуск приложения`() {
        val created = ShoppingRepository.createList("Продукты")

        restartApp()

        val restored = ShoppingRepository.list(created.id)
        assertNotNull(restored)
        assertEquals("Продукты", restored!!.name)
        assertEquals(created.createdAt, restored.createdAt)
    }

    @Test
    fun `товар сохраняется и восстанавливается целиком`() {
        val list = ShoppingRepository.createList("Продукты")
        val item = ListItem(
            name = "Бананы",
            emoji = "\uD83C\uDF4C",
            category = "Еда",
            subcategory = "Фрукты"
        )
        assertTrue(ShoppingRepository.addItem(list.id, item))

        restartApp()

        assertEquals(item, ShoppingRepository.list(list.id)!!.items.single())
    }

    @Test
    fun `отметка о покупке сохраняется`() {
        val list = ShoppingRepository.createList("Продукты")
        val milk = ListItem(name = "Молоко")
        val bread = ListItem(name = "Хлеб")
        ShoppingRepository.addItems(list.id, listOf(milk, bread))
        ShoppingRepository.setDone(list.id, milk.id, true)

        restartApp()

        val restored = ShoppingRepository.list(list.id)!!
        assertTrue(restored.items.single { it.id == milk.id }.done)
        assertFalse(restored.items.single { it.id == bread.id }.done)
        assertEquals(1, restored.boughtItems)
    }

    @Test
    fun `удаление товара и «убрать купленные» сохраняются`() {
        val list = ShoppingRepository.createList("Продукты")
        val milk = ListItem(name = "Молоко")
        val bread = ListItem(name = "Хлеб")
        val cheese = ListItem(name = "Сыр")
        ShoppingRepository.addItems(list.id, listOf(milk, bread, cheese))
        ShoppingRepository.removeItem(list.id, bread.id)
        ShoppingRepository.setDone(list.id, milk.id, true)
        assertEquals(1, ShoppingRepository.removeBought(list.id))

        restartApp()

        val restored = ShoppingRepository.list(list.id)!!
        assertEquals(listOf("Сыр"), restored.items.map { it.name })
    }

    @Test
    fun `удаление списка сохраняется`() {
        val first = ShoppingRepository.createList("Первый")
        val second = ShoppingRepository.createList("Второй")

        ShoppingRepository.deleteList(first.id)
        restartApp()

        assertEquals(listOf(second.id), ShoppingRepository.allLists().map { it.id })
    }

    @Test
    fun `переименование списка сохраняется`() {
        val list = ShoppingRepository.createList("Покупки")

        ShoppingRepository.renameList(list.id, "Продукты на неделю")
        restartApp()

        assertEquals("Продукты на неделю", ShoppingRepository.list(list.id)!!.name)
    }

    @Test
    fun `порядок списков и товаров не меняется`() {
        val first = ShoppingRepository.createList("Первый")
        val second = ShoppingRepository.createList("Второй")
        ShoppingRepository.addItems(
            first.id,
            listOf(ListItem(name = "Апельсины"), ListItem(name = "Молоко"), ListItem(name = "Хлеб"))
        )

        restartApp()

        // Новые списки кладутся в начало, товары — в порядке добавления.
        assertEquals(listOf(second.id, first.id), ShoppingRepository.allLists().map { it.id })
        assertEquals(
            listOf("Апельсины", "Молоко", "Хлеб"),
            ShoppingRepository.list(first.id)!!.items.map { it.name }
        )
    }

    @Test
    fun `повторный запуск приложения не сбрасывает уже загруженные данные`() {
        val list = ShoppingRepository.createList("Продукты")
        ShoppingRepository.addItem(list.id, ListItem(name = "Молоко"))

        ShoppingRepository.init(context)

        assertEquals(1, ShoppingRepository.allLists().size)
        assertEquals(1, ShoppingRepository.list(list.id)!!.totalItems)
    }

    @Test
    fun `дубликат не сохраняется в хранилище`() {
        val list = ShoppingRepository.createList("Продукты")
        ShoppingRepository.addItem(list.id, ListItem(name = "Молоко"))

        assertFalse(ShoppingRepository.addItem(list.id, ListItem(name = "молоко")))
        assertFalse(ShoppingRepository.addItem(list.id, ListItem(name = "  Молоко  ")))

        restartApp()

        assertEquals(listOf("Молоко"), ShoppingRepository.list(list.id)!!.items.map { it.name })
    }

    @Test
    fun `пакетное добавление тоже отсекает дубликаты`() {
        val list = ShoppingRepository.createList("Продукты")
        ShoppingRepository.addItems(list.id, listOf(ListItem(name = "Молоко")))

        ShoppingRepository.addItems(
            list.id,
            listOf(ListItem(name = "  молоко  "), ListItem(name = "Хлеб"))
        )

        restartApp()

        assertEquals(listOf("Молоко", "Хлеб"), ShoppingRepository.list(list.id)!!.items.map { it.name })
    }

    @Test
    fun `кавычки и переносы строк в названиях не ломают JSON`() {
        val trickyName = "Соус \"Терияки\"\\острый"
        val list = ShoppingRepository.createList(trickyName)
        ShoppingRepository.addItem(list.id, ListItem(name = trickyName))

        restartApp()

        val restored = ShoppingRepository.list(list.id)!!
        assertEquals(trickyName, restored.name)
        assertEquals(trickyName, restored.items.single().name)
    }

    @Test
    fun `товары без необязательных полей восстанавливаются с умолчаниями`() {
        writeRaw(
            """
            [{"id":"list-1","name":"Старый список","createdAt":123,"items":[{"id":"item-1","name":"Молоко"}]}]
            """.trimIndent()
        )

        restartApp()

        val list = ShoppingRepository.allLists().single()
        assertEquals("list-1", list.id)
        assertEquals(123L, list.createdAt)
        val item = list.items.single()
        assertEquals("item-1", item.id)
        assertEquals("Молоко", item.name)
        assertEquals(ListItem.DEFAULT_EMOJI, item.emoji)
        assertEquals("", item.category)
        assertEquals("", item.subcategory)
        assertFalse(item.done)
    }

    @Test
    fun `товар без идентификатора получает новый, а не теряется`() {
        writeRaw("""[{"id":"list-1","name":"Список","createdAt":1,"items":[{"name":"Молоко"}]}]""")

        restartApp()

        val item = ShoppingRepository.allLists().single().items.single()
        assertTrue(item.id.isNotBlank())
        assertEquals("Молоко", item.name)
    }

    @Test
    fun `повреждённое хранилище не роняет приложение`() {
        writeRaw("{ это точно не JSON")

        restartApp()

        assertEquals(emptyList<ShoppingList>(), ShoppingRepository.allLists())
    }

    @Test
    fun `хранилище с чужим JSON не роняет приложение`() {
        writeRaw("""{"lists":[],"version":2}""")

        restartApp()

        assertEquals(emptyList<ShoppingList>(), ShoppingRepository.allLists())
    }

    @Test
    fun `список без идентификатора не даёт себя найти, но не роняет приложение`() {
        writeRaw("""[{"name":"Без идентификатора","createdAt":7}]""")

        restartApp()

        assertEquals(1, ShoppingRepository.allLists().size)
        assertNull(ShoppingRepository.list("нет-такого"))
    }

    private fun writeRaw(raw: String) {
        storage.edit().putString(ShoppingRepository.KEY_LISTS, raw).commit()
    }
}
