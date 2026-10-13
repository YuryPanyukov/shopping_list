package code_sys.shopping_list.data

/** Товар из справочника быстрого набора. */
data class Product(val name: String, val emoji: String)

/** Подкатегория — второй уровень крутилки. */
data class Subcategory(val name: String, val emoji: String, val products: List<Product>)

/** Категория — первый уровень крутилки (одна из 4 зон). */
data class Category(val name: String, val emoji: String, val color: Int, val subcategories: List<Subcategory>)

/**
 * Справочник частых товаров для быстрого набора.
 * Задача — не охватить всё, а дать 8–10 ходовых позиций на подкатегорию.
 * Чистый Kotlin (без Android) — покрыт JVM-тестами.
 */
object ProductCatalog {

    const val FOOD = "Еда"
    const val HOUSEHOLD = "Бытовая химия"
    const val STATIONERY = "Канцтовары"
    const val OTHER = "Прочее"

    /** Палитра для второго уровня крутилки (подкатегории). */
    val dialPalette = intArrayOf(
        0xFFE5523B.toInt(),
        0xFFEF8B2C.toInt(),
        0xFFD9A514.toInt(),
        0xFF43A047.toInt(),
        0xFF1E9E8F.toInt(),
        0xFF2E7FD9.toInt(),
        0xFF5C6BC0.toInt(),
        0xFF7E57C2.toInt(),
        0xFFC2185B.toInt()
    )

    val categories: List<Category> = listOf(
        Category(
            name = FOOD,
            emoji = "🍽",
            color = 0xFFE5523B.toInt(),
            subcategories = listOf(
                Subcategory(
                    "Мясо", "🥩", listOf(
                        Product("Куриная грудка", "🍗"),
                        Product("Куриные бёдра", "🍗"),
                        Product("Фарш", "🥩"),
                        Product("Свинина", "🥓"),
                        Product("Говядина", "🥩"),
                        Product("Сосиски", "🌭"),
                        Product("Колбаса", "🥓"),
                        Product("Бекон", "🥓"),
                        Product("Пельмени", "🥟")
                    )
                ),
                Subcategory(
                    "Рыба", "🐟", listOf(
                        Product("Филе рыбы", "🐟"),
                        Product("Сёмга", "🐠"),
                        Product("Треска", "🐟"),
                        Product("Минтай", "🐟"),
                        Product("Сельдь", "🐟"),
                        Product("Скумбрия", "🐟"),
                        Product("Креветки", "🦐"),
                        Product("Кальмары", "🦑"),
                        Product("Икра", "🍣")
                    )
                ),
                Subcategory(
                    "Овощи", "🥕", listOf(
                        Product("Картофель", "🥔"),
                        Product("Морковь", "🥕"),
                        Product("Лук", "🧅"),
                        Product("Помидоры", "🍅"),
                        Product("Огурцы", "🥒"),
                        Product("Капуста", "🥬"),
                        Product("Болгарский перец", "🫑"),
                        Product("Чеснок", "🧄"),
                        Product("Свекла", "🥗"),
                        Product("Кабачок", "🥒"),
                        Product("Зелень", "🌿")
                    )
                ),
                Subcategory(
                    "Фрукты", "🍎", listOf(
                        Product("Яблоки", "🍎"),
                        Product("Бананы", "🍌"),
                        Product("Апельсины", "🍊"),
                        Product("Мандарины", "🍊"),
                        Product("Виноград", "🍇"),
                        Product("Груши", "🍐"),
                        Product("Лимоны", "🍋"),
                        Product("Киви", "🥝"),
                        Product("Персики", "🍑")
                    )
                ),
                Subcategory(
                    "Консервы", "🥫", listOf(
                        Product("Горошек", "🫛"),
                        Product("Кукуруза", "🌽"),
                        Product("Томаты в собственном соку", "🥫"),
                        Product("Оливки", "🫒"),
                        Product("Маслины", "🫒"),
                        Product("Огурцы солёные", "🥒"),
                        Product("Тунец в масле", "🐟"),
                        Product("Сардины", "🐟"),
                        Product("Паштет", "🥫"),
                        Product("Сгущёнка", "🥛")
                    )
                ),
                Subcategory(
                    "Перекус", "🍫", listOf(
                        Product("Чипсы", "🥨"),
                        Product("Сухарики", "🍞"),
                        Product("Орехи", "🥜"),
                        Product("Семечки", "🌻"),
                        Product("Шоколад", "🍫"),
                        Product("Печенье", "🍪"),
                        Product("Вафли", "🧇"),
                        Product("Конфеты", "🍬"),
                        Product("Мармелад", "🍡"),
                        Product("Попкорн", "🍿")
                    )
                ),
                Subcategory(
                    "Хлеб", "🍞", listOf(
                        Product("Хлеб белый", "🍞"),
                        Product("Хлеб чёрный", "🍞"),
                        Product("Батон", "🥖"),
                        Product("Багет", "🥖"),
                        Product("Лаваш", "🫓"),
                        Product("Булочки", "🥯"),
                        Product("Пита", "🫓"),
                        Product("Сухари", "🍞")
                    )
                )
            )
        ),
        Category(
            name = HOUSEHOLD,
            emoji = "🧴",
            color = 0xFF2E7FD9.toInt(),
            subcategories = listOf(
                Subcategory(
                    "Стирка", "🧺", listOf(
                        Product("Стиральный порошок", "🧴"),
                        Product("Гель для стирки", "🧴"),
                        Product("Кондиционер для белья", "🌸"),
                        Product("Капсулы для стирки", "💊"),
                        Product("Пятновыводитель", "🧴"),
                        Product("Отбеливатель", "🧴"),
                        Product("Средство для деликатных тканей", "🧴")
                    )
                ),
                Subcategory(
                    "Посуда", "🍽", listOf(
                        Product("Средство для посуды", "🧴"),
                        Product("Губки для посуды", "🧽"),
                        Product("Таблетки для посудомойки", "💊"),
                        Product("Ополаскиватель для посудомойки", "🧴"),
                        Product("Сода кальцинированная", "🥄"),
                        Product("Салфетки для посуды", "🧻")
                    )
                ),
                Subcategory(
                    "Чистящие", "🧽", listOf(
                        Product("Средство для кухни", "🧴"),
                        Product("Средство для плиты", "🧴"),
                        Product("Чистящий порошок", "🧽"),
                        Product("Универсальный спрей", "🧴"),
                        Product("Средство от накипи", "🫖"),
                        Product("Средство для духовки", "♨️")
                    )
                ),
                Subcategory(
                    "Сантехника", "🚿", listOf(
                        Product("Средство для унитаза", "🚽"),
                        Product("Гель для сантехники", "🧴"),
                        Product("Чистящий крем", "🧽"),
                        Product("Средство от плесени", "🦠"),
                        Product("Средство для труб", "🛠"),
                        Product("Освежитель для санузла", "🌸")
                    )
                ),
                Subcategory(
                    "Полы и окна", "🪟", listOf(
                        Product("Средство для мытья полов", "🧼"),
                        Product("Средство для стёкол", "🪟"),
                        Product("Спрей для окон", "🧴"),
                        Product("Салфетки для уборки", "🧻"),
                        Product("Тряпки из микрофибры", "🧽"),
                        Product("Швабра-насадка", "🧹")
                    )
                ),
                Subcategory(
                    "Ароматы", "🌸", listOf(
                        Product("Освежитель воздуха", "🌸"),
                        Product("Ароматизатор для дома", "🕯"),
                        Product("Гель-ароматизатор", "🫧"),
                        Product("Салфетки в машину", "🚗")
                    )
                ),
                Subcategory(
                    "Бумага", "🧻", listOf(
                        Product("Туалетная бумага", "🧻"),
                        Product("Бумажные полотенца", "🧻"),
                        Product("Влажные салфетки", "🧴"),
                        Product("Пакеты для мусора", "🗑"),
                        Product("Перчатки латексные", "🧤"),
                        Product("Мешки для мусора большие", "🗑")
                    )
                )
            )
        ),
        Category(
            name = STATIONERY,
            emoji = "✏️",
            color = 0xFF2FA36B.toInt(),
            subcategories = listOf(
                Subcategory(
                    "Ручки", "✏️", listOf(
                        Product("Шариковая ручка", "🖊"),
                        Product("Гелевая ручка", "🖊"),
                        Product("Простой карандаш", "✏️"),
                        Product("Цветные карандаши", "🖍"),
                        Product("Маркер", "🖍"),
                        Product("Корректор", "🧴"),
                        Product("Ластик", "🧽"),
                        Product("Точилка", "🔪")
                    )
                ),
                Subcategory(
                    "Блокноты", "📄", listOf(
                        Product("Тетрадь", "📒"),
                        Product("Общая тетрадь", "📓"),
                        Product("Блокнот", "📔"),
                        Product("Бумага А4", "📄"),
                        Product("Стикеры", "🗒"),
                        Product("Бумага для заметок", "🗒")
                    )
                ),
                Subcategory(
                    "Папки", "📁", listOf(
                        Product("Папка", "📁"),
                        Product("Файл-вкладыш", "📂"),
                        Product("Регистратор", "🗂"),
                        Product("Обложка для тетради", "📘"),
                        Product("Скоросшиватель", "📁")
                    )
                ),
                Subcategory(
                    "Клей и скотч", "🩹", listOf(
                        Product("Клей-карандаш", "🖍"),
                        Product("Клей ПВА", "🧴"),
                        Product("Суперклей", "🧴"),
                        Product("Скотч прозрачный", "🎗"),
                        Product("Скотч малярный", "🎗"),
                        Product("Изолента", "⚡")
                    )
                ),
                Subcategory(
                    "Мелочи", "📎", listOf(
                        Product("Скрепки", "📎"),
                        Product("Кнопки", "📍"),
                        Product("Степлер", "📌"),
                        Product("Скрепки для степлера", "📌"),
                        Product("Дырокол", "🕳"),
                        Product("Линейка", "📏"),
                        Product("Канцелярский нож", "🔪")
                    )
                ),
                Subcategory(
                    "Творчество", "🎨", listOf(
                        Product("Краски", "🎨"),
                        Product("Кисти", "🖌"),
                        Product("Фломастеры", "🖍"),
                        Product("Пластилин", "🟢"),
                        Product("Альбом для рисования", "📕"),
                        Product("Цветная бумага", "🌈"),
                        Product("Картон", "🟫"),
                        Product("Гуашь", "🎨")
                    )
                )
            )
        ),
        Category(
            name = OTHER,
            emoji = "📦",
            color = 0xFF7E57C2.toInt(),
            subcategories = listOf(
                Subcategory(
                    "Гигиена", "🧼", listOf(
                        Product("Зубная паста", "🪥"),
                        Product("Зубная щётка", "🪥"),
                        Product("Мыло", "🧼"),
                        Product("Гель для душа", "🚿"),
                        Product("Шампунь", "🧴"),
                        Product("Дезодорант", "💨"),
                        Product("Бритва", "🪒"),
                        Product("Ватные палочки", "🧵"),
                        Product("Ватные диски", "⚪"),
                        Product("Прокладки", "🧻")
                    )
                ),
                Subcategory(
                    "Аптека", "💊", listOf(
                        Product("Пластырь", "🩹"),
                        Product("Бинт", "🩹"),
                        Product("Вата", "⚪"),
                        Product("Активированный уголь", "⚫"),
                        Product("Обезболивающее", "💊"),
                        Product("Витамины", "🍊"),
                        Product("Антисептик", "🧴"),
                        Product("Перекись водорода", "🧴")
                    )
                ),
                Subcategory(
                    "Электрика", "🔌", listOf(
                        Product("Батарейки AA", "🔋"),
                        Product("Батарейки AAA", "🔋"),
                        Product("Лампочка", "💡"),
                        Product("Удлинитель", "🔌"),
                        Product("Сетевой фильтр", "🔌"),
                        Product("Зарядка для телефона", "🔌"),
                        Product("Кабель USB", "🔌")
                    )
                ),
                Subcategory(
                    "Кухня", "🍳", listOf(
                        Product("Спички", "🔥"),
                        Product("Свеча", "🕯"),
                        Product("Фольга", "🌫"),
                        Product("Бумага для выпечки", "📜"),
                        Product("Пищевая плёнка", "🎞"),
                        Product("Пакеты для хранения", "🛍"),
                        Product("Зубочистки", "🍢"),
                        Product("Одноразовая посуда", "🍽")
                    )
                ),
                Subcategory(
                    "Зоотовары", "🐾", listOf(
                        Product("Корм для кошки", "🐱"),
                        Product("Корм для собаки", "🐶"),
                        Product("Наполнитель для лотка", "🐾"),
                        Product("Лакомство для питомца", "🦴"),
                        Product("Игрушка для питомца", "🧸")
                    )
                ),
                Subcategory(
                    "Разное", "🎁", listOf(
                        Product("Подарочный пакет", "🛍"),
                        Product("Открытка", "💌"),
                        Product("Воздушные шарики", "🎈"),
                        Product("Подарочная лента", "🎀"),
                        Product("Уголь для гриля", "🔥"),
                        Product("Жидкость для розжига", "🔥")
                    )
                )
            )
        )
    )

    /** Найденный в справочнике товар вместе с местом в каталоге. */
    data class Found(val product: Product, val category: String, val subcategory: String)

    /** Поиск товара по названию — чтобы у товара, введённого руками, был правильный эмодзи. */
    fun findByName(name: String): Found? {
        val key = name.trim().lowercase()
        if (key.isEmpty()) return null
        categories.forEach { category ->
            category.subcategories.forEach { subcategory ->
                subcategory.products.firstOrNull { it.name.lowercase() == key }?.let {
                    return Found(it, category.name, subcategory.name)
                }
            }
        }
        return null
    }

    fun category(name: String?): Category? = categories.firstOrNull { it.name == name }

    fun subcategory(categoryName: String?, subcategoryName: String?): Subcategory? =
        category(categoryName)?.subcategories?.firstOrNull { it.name == subcategoryName }

    /** Все товары справочника «плоским» списком. */
    fun allProductNames(): List<String> =
        categories.flatMap { c -> c.subcategories.flatMap { s -> s.products.map { it.name } } }
}
