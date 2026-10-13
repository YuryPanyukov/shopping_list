package code_sys.shopping_list.data

/**
 * Подбирает товары, которые пользователь уже покупал в других списках,
 * чтобы одним касанием добавить их в новый список.
 *
 * Модуль намеренно не зависит от Android — логика покрыта JVM-тестами.
 */
object SuggestionEngine {

    const val DEFAULT_LIMIT = 24

    data class Suggestion(
        val name: String,
        val emoji: String,
        val category: String,
        val subcategory: String,
        /** Сколько списков содержали этот товар. */
        val timesUsed: Int,
        /** Дата самого свежего списка с этим товаром. */
        val lastUsedAt: Long
    )

    /**
     * Возвращает самые «популярные» ранее купленные товары:
     * сначала те, что встречались чаще, при равенстве — использованные позже.
     *
     * @param lists все существующие списки (включая текущий)
     * @param excludeNames названия, которые не надо предлагать (уже есть в списке)
     */
    fun buildSuggestions(
        lists: List<ShoppingList>,
        excludeNames: Collection<String> = emptyList(),
        limit: Int = DEFAULT_LIMIT
    ): List<Suggestion> {
        if (lists.isEmpty() || limit <= 0) return emptyList()

        val excluded = excludeNames.map { it.trim().lowercase() }.toHashSet()

        val stats = LinkedHashMap<String, Stats>()
        for (list in lists) {
            val seenInThisList = HashSet<String>()
            for (item in list.items) {
                val key = item.name.trim().lowercase()
                if (key.isEmpty() || key in excluded) continue
                val stat = stats.getOrPut(key) { Stats(item) }
                if (seenInThisList.add(key)) stat.timesUsed++
                if (list.createdAt > stat.lastUsedAt) stat.lastUsedAt = list.createdAt
                // Товары из «крутилки» приходят с эмодзи категории — предпочитаем их.
                if (stat.emoji == ListItem.DEFAULT_EMOJI && item.emoji != ListItem.DEFAULT_EMOJI) {
                    stat.emoji = item.emoji
                }
                if (stat.category.isEmpty() && item.category.isNotEmpty()) stat.category = item.category
                if (stat.subcategory.isEmpty() && item.subcategory.isNotEmpty()) stat.subcategory = item.subcategory
            }
        }

        if (stats.isEmpty()) return emptyList()

        return stats.values
            .sortedWith(
                compareByDescending<Stats> { it.timesUsed }
                    .thenByDescending { it.lastUsedAt }
                    .thenBy { it.name.lowercase() }
            )
            .take(limit)
            .map { it.toSuggestion() }
    }

    private class Stats(seed: ListItem) {
        val name: String = seed.name.trim()
        var emoji: String = seed.emoji
        var category: String = seed.category
        var subcategory: String = seed.subcategory
        var timesUsed: Int = 0
        var lastUsedAt: Long = 0L

        fun toSuggestion() = Suggestion(
            name = name,
            emoji = emoji.ifEmpty { ListItem.DEFAULT_EMOJI },
            category = category,
            subcategory = subcategory,
            timesUsed = timesUsed,
            lastUsedAt = lastUsedAt
        )
    }
}
