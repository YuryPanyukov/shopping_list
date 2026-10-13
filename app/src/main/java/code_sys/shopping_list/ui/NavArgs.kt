package code_sys.shopping_list.ui

import android.os.Bundle

/** Ключи аргументов, передаваемых между экранами. */
object NavArgs {
    const val LIST_ID = "listId"

    fun ofList(listId: String?): Bundle = Bundle().apply { putString(LIST_ID, listId) }
}
