package com.gplabs.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gplabs.inventoryapp.data.StockContract;
import com.gplabs.inventoryapp.data.StockContract.StockEntry;

import static com.gplabs.inventoryapp.R.id.price;
import static com.gplabs.inventoryapp.R.id.quantity;

/**
 * Created by pedronice on 23/01/17.
 */

public class StockAdapter extends CursorAdapter{

    private Context mContext;

    public StockAdapter(MainActivity context, Cursor c){
        super(context, c ,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        mContext = context;
        TextView nameTextView = view.findViewById(R.id.product_name);
        TextView priceTextView = view.findViewById(price);
        TextView quantityView = view.findViewById(quantity);
        ImageView imageView = view.findViewById(R.id.image_view);



        String nameColumnIndex = cursor.getString(cursor.getColumnIndex(StockContract.StockEntry.COLUMN_NAME));
        String priceColummIndex = cursor.getString(cursor.getColumnIndex(StockContract.StockEntry.COLUMN_PRICE));
        final int quantityColumnIndex = cursor.getInt(cursor.getColumnIndex(StockContract.StockEntry.COLUMN_QUANTITY));

        nameTextView.setText(nameColumnIndex);
        priceTextView.setText(priceColummIndex);
        quantityView.setText(String.valueOf(quantityColumnIndex));


        imageView.setImageURI(Uri.parse(cursor.getString(cursor.getColumnIndex(StockContract.StockEntry.COLUMN_IMAGE))));


        ImageView sellButton = view.findViewById(R.id.sale);
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view != null) {
                    ContentValues values = new ContentValues();
                    values.put(StockEntry.COLUMN_QUANTITY, (  quantityColumnIndex >= 1 ? quantityColumnIndex - 1 : 0)  );

                    String tag = view.getTag().toString();
                    Uri currentUri = ContentUris.withAppendedId(StockEntry.CONTENT_URI, Integer.parseInt(tag));

                    int rowsAffected = mContext.getContentResolver().update(currentUri, values, null, null);
                    if (rowsAffected == 0 || quantity == 0) {
                        Snackbar.make(view, mContext.getString(R.string.sell_product_failed), Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });
        sellButton.setTag(cursor.getInt(cursor.getColumnIndex(StockEntry._ID)));
    }

}

