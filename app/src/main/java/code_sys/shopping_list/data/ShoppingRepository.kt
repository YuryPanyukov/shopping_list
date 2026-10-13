package code_sys.shopping_list.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Простое хранилище списков покупок: SharedPreferences + JSON.
 * Данных мало (десятки записей), поэтому сохраняем синхронно.
 */
object ShoppingRepository {

    internal const val PREFS_NAME = "shopping_lists_store"
    internal const val KEY_LISTS = "lists_v1"

    private var prefs: SharedPreferences? = null
    private val cache = mutableListOf<ShoppingList>()

    fun init(context: Context) {
        if (prefs != null) return
        val store = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = store
        cache.clear()
        cache.addAll(readLists(store.getString(KEY_LISTS, null)))
    }

    /**
     * Забывает кеш и привязку к хранилищу: следующий [init] перечитает данные заново.
     * Так тесты проверяют, что данные действительно дошли до диска, а не остались в памяти.
     */
    internal fun reset() {
        prefs = null
        cache.clear()
    }

    fun allLists(): List<ShoppingList> = cache.toList()

    fun list(listId: String?): ShoppingList? = cache.firstOrNull { it.id == listId }

    fun createList(name: String): ShoppingList {
        val list = ShoppingList(
            id = java.util.UUID.randomUUID().toString(),
            name = name.trim().ifEmpty { "Список покупок" },
            createdAt = System.currentTimeMillis()
        )
        cache.add(0, list)
        persist()
        return list
    }

    fun renameList(listId: String, newName: String) {
        updateList(listId) { it.copy(name = newName.trim().ifEmpty { it.name }) }
    }

    fun deleteList(listId: String) {
        cache.removeAll { it.id == listId }
        persist()
    }

    /** @return false, если такой товар уже есть в списке. */
    fun addItem(listId: String, item: ListItem): Boolean {
        val list = list(listId) ?: return false
        if (list.containsItem(item.name)) return false
        updateList(listId) { it.copy(items = it.items + item) }
        return true
    }

    fun addItems(listId: String, items: List<ListItem>) {
        if (items.isEmpty()) return
        val list = list(listId) ?: return
        val existing = list.items.map { it.name.trim().lowercase() }.toHashSet()
        val fresh = items.filter { existing.add(it.name.trim().lowercase()) }
        if (fresh.isEmpty()) return
        updateList(listId) { it.copy(items = it.items + fresh) }
    }

    fun setDone(listId: String, itemId: String, done: Boolean) {
        updateList(listId) { l ->
            l.copy(items = l.items.map { if (it.id == itemId) it.copy(done = done) else it })
        }
    }

    fun removeItem(listId: String, itemId: String) {
        updateList(listId) { l -> l.copy(items = l.items.filterNot { it.id == itemId }) }
    }

    /** Убирает купленные товары, возвращает их количество. */
    fun removeBought(listId: String): Int {
        val list = list(listId) ?: return 0
        val bought = list.boughtItems
        if (bought > 0) updateList(listId) { l -> l.copy(items = l.items.filterNot { it.done }) }
        return bought
    }

    /** Товары из прошлых покупок, которых ещё нет в указанном списке. */
    fun suggestionsFor(listId: String?, limit: Int = SuggestionEngine.DEFAULT_LIMIT) =
        SuggestionEngine.buildSuggestions(
            lists = cache.toList(),
            excludeNames = list(listId)?.itemNames.orEmpty(),
            limit = limit
        )

    private fun updateList(listId: String, transform: (ShoppingList) -> ShoppingList) {
        val index = cache.indexOfFirst { it.id == listId }
        if (index < 0) return
        cache[index] = transform(cache[index])
        persist()
    }

    private fun persist() {
        prefs?.edit()?.putString(KEY_LISTS, toJson(cache).toString())?.apply()
    }

    // region JSON

    private fun toJson(lists: List<ShoppingList>) = JSONArray().apply {
        lists.forEach { put(it.toJson()) }
    }

    private fun ShoppingList.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("createdAt", createdAt)
        put("items", JSONArray().apply { items.forEach { put(it.toJson()) } })
    }

    private fun ListItem.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("emoji", emoji)
        put("category", category)
        put("subcategory", subcategory)
        put("done", done)
        put("addedAt", addedAt)
    }

    private fun readLists(raw: String?): List<ShoppingList> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { array.getJSONObject(it).toShoppingList() }
        }.getOrDefault(emptyList())
    }

    private fun JSONObject.toShoppingList(): ShoppingList {
        val itemsJson = optJSONArray("items") ?: JSONArray()
        val items = (0 until itemsJson.length()).map { itemsJson.getJSONObject(it).toListItem() }
        return ShoppingList(
            id = optString("id"),
            name = optString("name"),
            createdAt = optLong("createdAt", System.currentTimeMillis()),
            items = items
        )
    }

    private fun JSONObject.toListItem() = ListItem(
        id = optString("id", java.util.UUID.randomUUID().toString()),
        name = optString("name"),
        emoji = optString("emoji", ListItem.DEFAULT_EMOJI),
        category = optString("category"),
        subcategory = optString("subcategory"),
        done = optBoolean("done", false),
        addedAt = optLong("addedAt", 0L)
    )

    // endregion
}
