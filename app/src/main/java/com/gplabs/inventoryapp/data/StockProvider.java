package com.gplabs.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.gplabs.inventoryapp.data.StockContract.StockEntry;

/**
 * Created by pedronice on 23/01/17.
 */

public class StockProvider extends ContentProvider {

    public static final String LOG_TAG = StockProvider.class.getSimpleName();

    private InventoryDbHelper mDbHelper;

    private static final int STOCK = 100;
    private static final int STOCK_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(StockContract.CONTENT_AUTHORITY, StockContract.PATH_STOCK, STOCK);
        sUriMatcher.addURI(StockContract.CONTENT_AUTHORITY, StockContract.PATH_STOCK + "/#", STOCK_ID);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:

                cursor = database.query(StockEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case STOCK_ID:

                selection = StockEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };


                cursor = database.query(StockEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }


    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                return StockEntry.CONTENT_LIST_TYPE;
            case STOCK_ID:
                return StockEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match );
        }

    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                return insertStock(uri,values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertStock(Uri uri, ContentValues values) {


        String name = values.getAsString(StockEntry.COLUMN_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Name is required");
        }
        String price = values.getAsString(StockEntry.COLUMN_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Price is required");
        }
        Integer quantity = values.getAsInteger(StockEntry.COLUMN_QUANTITY);
        if (quantity == null && quantity < 0) {
            throw new IllegalArgumentException("Quantity is required");
        }
        String image = values.getAsString(StockEntry.COLUMN_IMAGE);
        if (image == null) {
            throw new IllegalArgumentException("Image is required");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.insert(StockEntry.TABLE_NAME,null,values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to isnert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);


    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                rowsDeleted = db.delete(StockEntry.TABLE_NAME,selection,selectionArgs);
                break;
            case STOCK_ID:
                selection = StockEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(StockEntry.TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted!= 0){
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                return updateStock(uri,values, selection,selectionArgs);
            case STOCK_ID:
                selection = StockEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateStock(uri,values,selection,selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateStock(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(StockEntry.COLUMN_NAME)) {
            String name = values.getAsString(StockEntry.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Name is required");
            }
        }

        if (values.containsKey(StockEntry.COLUMN_PRICE)) {
            String price = values.getAsString(StockEntry.COLUMN_PRICE);
            if (price == null) {
                throw new IllegalArgumentException("Price is required");
            }
        }

        if (values.containsKey(StockEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(StockEntry.COLUMN_QUANTITY);
            if (quantity == null) {
                throw new IllegalArgumentException("Quantity is required");
            }
        }

        if (values.containsKey(StockEntry.COLUMN_IMAGE)) {
            String image = values.getAsString(StockEntry.COLUMN_IMAGE);
            if (image == null) {
                throw new IllegalArgumentException("Image is required");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsUpdated = db.update(StockEntry.TABLE_NAME,values,selection,selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return rowsUpdated;

    }

}


