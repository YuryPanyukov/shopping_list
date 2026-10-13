package code_sys.shopping_list.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductCatalogTest {

    @Test
    fun `первая ступень крутилки состоит из четырёх зон`() {
        val names = ProductCatalog.categories.map { it.name }
        assertEquals(
            listOf(
                ProductCatalog.FOOD,
                ProductCatalog.HOUSEHOLD,
                ProductCatalog.STATIONERY,
                ProductCatalog.OTHER
            ),
            names
        )
    }

    @Test
    fun `подкатегории еды соответствуют оговорённому списку`() {
        val food = ProductCatalog.category(ProductCatalog.FOOD)
        assertNotNull(food)
        assertEquals(
            listOf("Мясо", "Рыба", "Овощи", "Фрукты", "Консервы", "Перекус", "Хлеб"),
            food!!.subcategories.map { it.name }
        )
    }

    @Test
    fun `у каждой подкатегории есть товары, но не слишком много`() {
        ProductCatalog.categories.forEach { category ->
            assertTrue("${category.name}: мало подкатегорий", category.subcategories.size >= 4)
            category.subcategories.forEach { subcategory ->
                val count = subcategory.products.size
                assertTrue(
                    "${category.name}/${subcategory.name}: товаров $count",
                    count in 4..12
                )
            }
        }
    }

    @Test
    fun `подкатегории внутри зоны не повторяются`() {
        ProductCatalog.categories.forEach { category ->
            val names = category.subcategories.map { it.name }
            assertEquals("${category.name}: дубли подкатегорий", names.size, names.toSet().size)
        }
    }

    @Test
    fun `товары внутри зоны не повторяются`() {
        ProductCatalog.categories.forEach { category ->
            val names = category.subcategories.flatMap { it.products.map { product -> product.name } }
            assertEquals("${category.name}: дубли товаров", names.size, names.toSet().size)
        }
    }

    @Test
    fun `у каждого товара и сектора есть подпись и эмодзи`() {
        ProductCatalog.categories.forEach { category ->
            assertTrue(category.name.isNotBlank())
            assertTrue("${category.name}: нет эмодзи", category.emoji.isNotBlank())
            category.subcategories.forEach { subcategory ->
                assertTrue(subcategory.name.isNotBlank())
                assertTrue(subcategory.emoji.isNotBlank())
                subcategory.products.forEach { product ->
                    assertTrue("пустое название товара в ${subcategory.name}", product.name.isNotBlank())
                    assertEquals(product.name, product.name.trim())
                    assertTrue("${product.name}: нет эмодзи", product.emoji.isNotBlank())
                }
            }
        }
    }

    @Test
    fun `палитры хватает на любое количество секторов второй ступени`() {
        val maxSubcategories = ProductCatalog.categories.maxOf { it.subcategories.size }
        assertTrue(maxSubcategories <= ProductCatalog.dialPalette.size)
    }

    @Test
    fun `поиск товара по названию возвращает его место в справочнике`() {
        val found = ProductCatalog.findByName("  бананы ")
        assertNotNull(found)
        assertEquals("Бананы", found!!.product.name)
        assertEquals(ProductCatalog.FOOD, found.category)
        assertEquals("Фрукты", found.subcategory)

        assertNull(ProductCatalog.findByName("чего-то такого нет"))
        assertNull(ProductCatalog.findByName("   "))
    }
}
