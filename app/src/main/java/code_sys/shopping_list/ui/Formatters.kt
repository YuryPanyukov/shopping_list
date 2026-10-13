package code_sys.shopping_list.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("d MMM", Locale.forLanguageTag("ru"))

/** Дата вида «13 окт». */
fun formatDate(time: Long): String = dateFormat.format(Date(time))
