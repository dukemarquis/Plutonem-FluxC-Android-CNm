package com.wellsql.generated;

import com.plutonem.android.fluxc.model.AccountModel;
import com.yarolegovich.wellsql.core.Identifiable;
import com.yarolegovich.wellsql.core.TableClass;

public final class AccountModelTable implements TableClass {
    public static final String ID = "_id";
    public static final String USER_NAME = "USER_NAME";
    public static final String USER_ID = "USER_ID";
    public static final String DISPLAY_NAME = "DISPLAY_NAME";
    public static final String PHONE = "PHONE";
    public static final String HAS_UNSEEN_NOTES = "HAS_UNSEEN_NOTES";
    public static final String DATE = "DATE";

    public AccountModelTable() {
    }

    public String createStatement() {
        return "CREATE TABLE AccountModel (_id INTEGER PRIMARY KEY,USER_NAME TEXT,USER_ID INTEGER,DISPLAY_NAME TEXT,PHONE TEXT,HAS_UNSEEN_NOTES INTEGER,DATE TEXT)";
    }

    public String getTableName() {
        return "AccountModel";
    }

    public Class<? extends Identifiable> getModelClass() {
        return AccountModel.class;
    }

    public boolean shouldAutoincrementId() {
        return false;
    }
}
