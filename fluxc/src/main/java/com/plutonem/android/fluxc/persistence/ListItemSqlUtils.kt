package com.plutonem.android.fluxc.persistence

import com.plutonem.android.fluxc.model.list.ListItemModel
import com.wellsql.generated.ListItemModelTable
import com.wellsql.generated.ListModelTable
import com.yarolegovich.wellsql.SelectQuery
import com.yarolegovich.wellsql.WellSql
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListItemSqlUtils @Inject constructor() {
    /**
     * This function inserts the [itemList] in the [ListItemModelTable].
     *
     * Unique constraint in [ListItemModel] will ignore duplicate records which is what we want. That'll ensure that
     * the order of the items will not be altered while the user is browsing the list. The order will fix itself
     * once the list data is refreshed.
     */
    fun insertItemList(itemList: List<ListItemModel>) {
        WellSql.insert(itemList).asSingleTransaction(true).execute()
    }

    /**
     * This function returns a list of [ListItemModel] records for the given [listId].
     */
    fun getListItems(listId: Int): List<ListItemModel> = getListItemsQuery(listId).asModel

    /**
     * This function returns the number of records a list has for the given [listId].
     */
    fun getListItemsCount(listId: Int): Long = getListItemsQuery(listId).count()

    /**
     * A helper function that returns the select query for a list of [ListItemModel] records for the given [listId].
     */
    private fun getListItemsQuery(listId: Int): SelectQuery<ListItemModel> =
        WellSql.select(ListItemModel::class.java)
            .where()
            .equals(ListItemModelTable.LIST_ID, listId)
            .endWhere()
            .orderBy(ListModelTable.ID, SelectQuery.ORDER_ASCENDING)

    /**
     * This function deletes all [ListItemModel]s for a specific [listId].
     */
    fun deleteItems(listId: Int) {
        WellSql.delete(ListItemModel::class.java)
            .where()
            .equals(ListItemModelTable.LIST_ID, listId)
            .endWhere()
            .execute()
    }
}