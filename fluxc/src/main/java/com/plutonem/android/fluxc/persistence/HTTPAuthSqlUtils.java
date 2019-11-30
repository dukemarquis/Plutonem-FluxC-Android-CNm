package com.plutonem.android.fluxc.persistence;

import android.content.ContentValues;

import com.plutonem.android.fluxc.network.HTTPAuthModel;
import com.wellsql.generated.HTTPAuthModelTable;
import com.yarolegovich.wellsql.WellSql;
import com.yarolegovich.wellsql.mapper.InsertMapper;

import java.util.List;

public class HTTPAuthSqlUtils {
    public static void insertOrUpdateModel(HTTPAuthModel model) {
        List<HTTPAuthModel> modelResult = WellSql.select(HTTPAuthModel.class)
                .where().equals(HTTPAuthModelTable.ROOT_URL, model.getRootUrl()).endWhere()
                .getAsModel();
        if (modelResult.isEmpty()) {
            // insert
            WellSql.insert(model).asSingleTransaction(true).execute();
        } else {
            // update
            int oldId = modelResult.get(0).getId();
            WellSql.update(HTTPAuthModel.class).whereId(oldId)
                    .put(model, new InsertMapper<HTTPAuthModel>() {
                        @Override
                        public ContentValues toCv(HTTPAuthModel item) {
                            ContentValues cv = new ContentValues();
                            cv.put(HTTPAuthModelTable.USERNAME, item.getUsername());
                            cv.put(HTTPAuthModelTable.PASSWORD, item.getPassword());
                            cv.put(HTTPAuthModelTable.REALM, item.getRealm());
                            return cv;
                        }
                    }).execute();
        }
    }
}
