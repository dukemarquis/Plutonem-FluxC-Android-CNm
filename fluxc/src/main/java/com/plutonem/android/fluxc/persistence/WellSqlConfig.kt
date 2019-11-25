package com.plutonem.android.fluxc.persistence

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.annotation.StringDef
import com.yarolegovich.wellsql.DefaultWellConfig
import com.yarolegovich.wellsql.WellSql
import com.yarolegovich.wellsql.WellTableManager
import org.wordpress.android.util.AppLog
import org.wordpress.android.util.AppLog.T

open class WellSqlConfig : DefaultWellConfig {
    companion object {
        const val ADDON_WOOCOMMERCE = "WC"
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, @AddOn vararg addOns: String) : super(context, mutableSetOf(*addOns))

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(ADDON_WOOCOMMERCE)
    @Target(AnnotationTarget.VALUE_PARAMETER)
    annotation class AddOn

    override fun getDbVersion(): Int {
        return 1
    }

    override fun getDbName(): String {
        return "pn-fluxc"
    }

    override fun onCreate(db: SQLiteDatabase, helper: WellTableManager) {
        mTables.forEach { table -> helper.createTable(table) }
    }

    @Suppress("CheckStyle")
    override fun onUpgrade(db: SQLiteDatabase, helper: WellTableManager, oldVersion: Int, newVersion: Int) {
        AppLog.d(T.DB, "Upgrading database from version $oldVersion to $newVersion")
        db.beginTransaction()
        for (version in oldVersion..newVersion) {
            when (version) {
                1 -> migrate(version) {
                    db.execSQL("DROP TABLE IF EXISTS AccountModel")
                    db.execSQL(
                        "CREATE TABLE AccountModel (" +
                                "_id INTEGER PRIMARY KEY," +
                                "USER_NAME TEXT," +
                                "USER_ID INTEGER," +
                                "DISPLAY_NAME TEXT," +
                                "PHONE TEXT," +
                                "HAS_UNSEEN_NOTES INTEGER," +
                                "DATE TEXT,"
                    )
                }
            }
        }
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    @Suppress("CheckStyle")
    override fun onConfigure(db: SQLiteDatabase, helper: WellTableManager?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.setForeignKeyConstraintsEnabled(true)
        } else {
            db.execSQL("PRAGMA foreign_keys=ON")
        }
    }

    /**
     * Drop and create all tables
     */
    @Suppress("CheckStyle")
    open fun reset() {
        val db = WellSql.giveMeWritableDb()
        mTables.forEach { clazz ->
            val table = getTable(clazz)
            db.execSQL("DROP TABLE IF EXISTS ${table.tableName}")
            db.execSQL(table.createStatement())
        }
    }

    /**
     * Recreates all the tables in this database - similar to the above but can be used from onDowngrade where we can't
     * call giveMeWritableDb (attempting to do so results in "IllegalStateException: getDatabase called recursively")
     */
    fun reset(helper: WellTableManager) {
        AppLog.d(T.DB, "resetting tables")
        for (table in mTables) {
            AppLog.d(T.DB, "dropping table " + table.simpleName)
            helper.dropTable(table)
            AppLog.d(T.DB, "creating table " + table.simpleName)
            helper.createTable(table)
        }
    }

    private fun migrate(version: Int, script: () -> Unit) {
        AppLog.d(T.DB, "Migrating to version ${version + 1}")
        script()
    }
}