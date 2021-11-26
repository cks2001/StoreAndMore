package com.exampe.storeandmore.helper;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "ekart.db";
    public static final String TABLE_FAVORITE_NAME = "tblfavourite";
    public static final String TABLE_SAVE_FOR_LATER_NAME = "tblsaveforlater";
    public static final String KEY_ID = "pid";

    final String TABLE_CART_NAME = "tblcart";
    final String PID = "pid";
    final String VID = "vid";
    final String QTY = "qty";
    final String FavoriteTableInfo = TABLE_FAVORITE_NAME + "(" + KEY_ID + " TEXT" + ")";
    final String SaveForLaterTableInfo = TABLE_SAVE_FOR_LATER_NAME + "(" + VID + " TEXT ," + PID + " TEXT ," + QTY + " TEXT)";
    final String CartTableInfo = TABLE_CART_NAME + "(" + VID + " TEXT ," + PID + " TEXT ," + QTY + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + FavoriteTableInfo);
        db.execSQL("CREATE TABLE " + CartTableInfo);
        db.execSQL("CREATE TABLE " + SaveForLaterTableInfo);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        replaceDataToNewTable(db, TABLE_FAVORITE_NAME, FavoriteTableInfo);
        replaceDataToNewTable(db, TABLE_CART_NAME, CartTableInfo);
        replaceDataToNewTable(db, TABLE_SAVE_FOR_LATER_NAME, SaveForLaterTableInfo);
        onCreate(db);
    }

    void replaceDataToNewTable(SQLiteDatabase db, String tableName, String tableString) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + tableString);

        List<String> columns = getColumns(db, tableName);
        db.execSQL("ALTER TABLE " + tableName + " RENAME TO temp_" + tableName);
        db.execSQL("CREATE TABLE " + tableString);

        columns.retainAll(getColumns(db, tableName));
        String cols = join(columns);
        db.execSQL(String.format("INSERT INTO %s (%s) SELECT %s from temp_%s",
                tableName, cols, cols, tableName));
        db.execSQL("DROP TABLE temp_" + tableName);
    }

    List<String> getColumns(SQLiteDatabase db, String tableName) {
        List<String> ar = null;
        try (Cursor c = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 1", null)) {
            if (c != null) {
                ar = new ArrayList<>(Arrays.asList(c.getColumnNames()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ar;
    }

    String join(List<String> list) {
        StringBuilder buf = new StringBuilder();
        int num = list.size();
        for (int i = 0; i < num; i++) {
            if (i != 0)
                buf.append(",");
            buf.append(list.get(i));
        }
        return buf.toString();
    }


    /*      FAVORITE TABLE OPERATION      */
    public boolean getFavoriteById(String pid) {
        boolean count = false;
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = new String[]{pid};
        Cursor cursor = db.rawQuery("SELECT " + KEY_ID + " FROM " + TABLE_FAVORITE_NAME + " WHERE " + KEY_ID + "=? ", args);
        if (cursor.moveToFirst()) {
            count = true;
        }
        cursor.close();
        db.close();
        return count;
    }

    public void AddOrRemoveFavorite(String id, boolean isAdd) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (isAdd) {
            addFavorite(id);
        } else {
            db.execSQL("DELETE FROM  " + TABLE_FAVORITE_NAME + " WHERE " + KEY_ID + " = " + id);
        }
        db.close();
    }

    public void addFavorite(String id) {
        ContentValues fav = new ContentValues();
        fav.put(DatabaseHelper.KEY_ID, id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_FAVORITE_NAME, null, fav);
    }

    public ArrayList<String> getFavorite() {
        final ArrayList<String> ids = new ArrayList<>();
        String selectQuery = "SELECT *  FROM " + TABLE_FAVORITE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                ids.add(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)));
            } while (cursor.moveToNext());

        }
        cursor.close();
        db.close();
        return ids;
    }

    public void DeleteAllFavoriteData() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + TABLE_FAVORITE_NAME);
        database.close();

    }


    /*      CART TABLE OPERATION      */
    public ArrayList<String> getCartList() {
        final ArrayList<String> ids = new ArrayList<>();
        String selectQuery = "SELECT *  FROM " + TABLE_CART_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String count = cursor.getString(cursor.getColumnIndex(QTY));
                if (count.equals("0")) {
                    db.execSQL("DELETE FROM " + TABLE_CART_NAME + " WHERE " + VID + " = ? AND " + PID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(VID)), cursor.getString(cursor.getColumnIndexOrThrow(PID))});

                } else
                    ids.add(cursor.getString(cursor.getColumnIndexOrThrow(VID)));
            } while (cursor.moveToNext());

        }
        cursor.close();
        db.close();
        return ids;
    }

    public HashMap<String, String> getCartData() {
        final HashMap<String, String> ids = new HashMap<>();
        String selectQuery = "SELECT *  FROM " + TABLE_CART_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String count = cursor.getString(cursor.getColumnIndex(QTY));
                if (count.equals("0")) {
                    db.execSQL("DELETE FROM " + TABLE_CART_NAME + " WHERE " + VID + " = ? AND " + PID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(VID)), cursor.getString(cursor.getColumnIndexOrThrow(PID))});
                } else
                    ids.put(cursor.getString(cursor.getColumnIndexOrThrow(VID)), cursor.getString(cursor.getColumnIndexOrThrow(QTY)));
            } while (cursor.moveToNext());

        }
        cursor.close();
        db.close();
        return ids;
    }

    public int getTotalItemOfCart(Activity activity) {
        String countQuery = "SELECT  * FROM " + TABLE_CART_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        Constant.TOTAL_CART_ITEM = count;
        activity.invalidateOptionsMenu();
        return count;
    }

    public void AddToCart(String vid, String pid, String qty) {
        try {
            if (!CheckCartItemExist(vid, pid).equalsIgnoreCase("0")) {
                UpdateCart(vid, pid, qty);
            } else {
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(VID, vid);
                values.put(PID, pid);
                values.put(QTY, qty);
                db.insert(TABLE_CART_NAME, null, values);
                db.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void UpdateCart(String vid, String pid, String qty) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (qty.equals("0")) {
            RemoveFromCart(vid, pid);
        } else {
            ContentValues values = new ContentValues();
            values.put(QTY, qty);
            db.update(TABLE_CART_NAME, values, VID + " = ? AND " + PID + " = ?", new String[]{vid, pid});
        }
        db.close();
    }

    public void RemoveFromCart(String vid, String pid) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + TABLE_CART_NAME + " WHERE " + VID + " = ? AND " + PID + " = ?", new String[]{vid, pid});
        database.close();
    }

    public String CheckCartItemExist(String vid, String pid) {
        String count = "0";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CART_NAME + " WHERE " + VID + " = ? AND " + PID + " = ?", new String[]{vid, pid});
        if (cursor.moveToFirst()) {
            count = cursor.getString(cursor.getColumnIndex(QTY));
            if (count.equals("0")) {
                db.execSQL("DELETE FROM " + TABLE_CART_NAME + " WHERE " + VID + " = ? AND " + PID + " = ?", new String[]{vid, pid});
            }
        }
        cursor.close();
        db.close();
        return count;
    }

    public void ClearCart() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + TABLE_CART_NAME);
        database.close();

    }

    /*      SAVE FOR LATER TABLE OPERATION      */
    public ArrayList<String> getSaveForLaterList() {
        final ArrayList<String> ids = new ArrayList<>();
        String selectQuery = "SELECT *  FROM " + TABLE_SAVE_FOR_LATER_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String count = cursor.getString(cursor.getColumnIndex(QTY));
                if (count.equals("0")) {
                    db.execSQL("DELETE FROM " + TABLE_SAVE_FOR_LATER_NAME + " WHERE " + VID + " = ? AND " + PID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(VID)), cursor.getString(cursor.getColumnIndexOrThrow(PID))});

                } else
                    ids.add(cursor.getString(cursor.getColumnIndexOrThrow(VID)));
            } while (cursor.moveToNext());

        }
        cursor.close();
        db.close();
        return ids;
    }

    public void AddToSaveForLater(String vid, String pid, String qty) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(VID, vid);
            values.put(PID, pid);
            values.put(QTY, qty);
            db.insert(TABLE_SAVE_FOR_LATER_NAME, null, values);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

public HashMap<String, String> getSaveForLaterData() {
    final HashMap<String, String> ids = new HashMap<>();
    String selectQuery = "SELECT *  FROM " + TABLE_SAVE_FOR_LATER_NAME;
    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);

    if (cursor.moveToFirst()) {
        do {
            String count = cursor.getString(cursor.getColumnIndex(QTY));
            if (count.equals("0")) {
                db.execSQL("DELETE FROM " + TABLE_SAVE_FOR_LATER_NAME + " WHERE " + VID + " = ? AND " + PID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(VID)), cursor.getString(cursor.getColumnIndexOrThrow(PID))});
            } else
                ids.put(cursor.getString(cursor.getColumnIndexOrThrow(VID)), cursor.getString(cursor.getColumnIndexOrThrow(QTY)));
        } while (cursor.moveToNext());

    }
    cursor.close();
    db.close();
    return ids;
}

    public int getTotalItemOfSaveForLater() {
        String countQuery = "SELECT  * FROM " + TABLE_SAVE_FOR_LATER_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void MoveToCartOrSaveForLater(String vid, String pid, String from,Activity activity) {
        if (from.equals("cart")) {
            AddToSaveForLater(vid, pid, CheckCartItemExist(vid, pid));
            RemoveFromCart(vid, pid);
        } else {
            AddToCart(vid, pid, CheckSaveForLaterItemExist(vid, pid));
            RemoveFromSaveForLater(vid, pid);
        }
        getTotalItemOfCart(activity);
    }

    public void RemoveFromSaveForLater(String vid, String pid) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + TABLE_SAVE_FOR_LATER_NAME + " WHERE " + VID + " = ? AND " + PID + " = ?", new String[]{vid, pid});
        database.close();
    }

    public String CheckSaveForLaterItemExist(String vid, String pid) {
        String count = "0";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SAVE_FOR_LATER_NAME + " WHERE " + VID + " = ? AND " + PID + " = ?", new String[]{vid, pid});
        if (cursor.moveToFirst()) {
            count = cursor.getString(cursor.getColumnIndex(QTY));
            if (count.equals("0")) {
                db.execSQL("DELETE FROM " + TABLE_SAVE_FOR_LATER_NAME + " WHERE " + VID + " = ? AND " + PID + " = ?", new String[]{vid, pid});

            }
        }
        cursor.close();
        db.close();
        return count;
    }

    public void ClearSaveForLater() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + TABLE_SAVE_FOR_LATER_NAME);
        database.close();

    }

}