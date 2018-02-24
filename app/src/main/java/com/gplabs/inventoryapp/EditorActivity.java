package com.gplabs.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.gplabs.inventoryapp.data.StockContract.StockEntry;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by pedronice on 25/01/17.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_STOCK_LOADER = 0;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private Uri mCurrentStockUri;
    private Uri mActualUri;



    private EditText mNameEditText;
    private EditText mPriceEditeText;
    private EditText mQuantityEditText;
    private ImageView mImageView;
    private ImageButton mDecreaseQuantity;
    private ImageButton mIncreaseQuantity;
    private Button mImageBtn;

    private Boolean mInfoItemHasChanged = false;
    private static final int PICK_IMAGE_REQUEST = 0;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mInfoItemHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);

        Intent intent = getIntent();
        mCurrentStockUri = intent.getData();

        if (mCurrentStockUri == null) {
            setTitle(R.string.editor_activity_title_new);
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.editor_activity_title_edit_item);
            getLoaderManager().initLoader(EXISTING_STOCK_LOADER, null, this);
        }


        mNameEditText = findViewById(R.id.product_name_edit);
        mPriceEditeText = findViewById(R.id.price_edit);
        mQuantityEditText = findViewById(R.id.quantity_edit);
        mImageView = findViewById(R.id.image_view);
        mImageBtn = findViewById(R.id.select_image);
        mDecreaseQuantity = findViewById(R.id.decrease_quantity);
        mIncreaseQuantity = findViewById(R.id.increase_quantity);


        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditeText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);


        mDecreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subtractOneToQuantity();
                mInfoItemHasChanged = true;
            }
        });

        mIncreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sumOneToQuantity();
                mInfoItemHasChanged = true;
            }
        });

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryToOpenImageSelector();
                mInfoItemHasChanged = true;
            }
        });
    }


    private boolean saveStock() {
        boolean isAllOk = true;

        if (!checkIfValueSet(mNameEditText, "Nome")) {
            isAllOk = false;
        }
        if (!checkIfValueSet(mPriceEditeText, "Preço")) {
            isAllOk = false;
        }
        if (!checkIfValueSet(mQuantityEditText, "Quantidade")) {
            isAllOk = false;
        }
        if (mActualUri == null && mCurrentStockUri == null) {
            isAllOk = false;
            mImageBtn.setError("Imagem Ausente");
        }
        if (!isAllOk) {
            return false;
        }

        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditeText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();


        ContentValues values = new ContentValues();
        values.put(StockEntry.COLUMN_NAME, nameString);
        values.put(StockEntry.COLUMN_PRICE, priceString);


        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {

            quantity = Integer.parseInt(quantityString);
        }
        values.put(StockEntry.COLUMN_QUANTITY, quantity);

        Bitmap icLanucher = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        mImageView = (ImageView) findViewById(R.id.image_view);
        Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        if (!isEquals(icLanucher, bitmap) && mActualUri != null) {
            values.put(StockEntry.COLUMN_IMAGE, mActualUri.toString());
        }


        if (mCurrentStockUri == null) {
            Uri newUri = getContentResolver().insert(StockEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_stock), Toast.LENGTH_SHORT).show();

                }


            } else {

                int rowsAffected = getContentResolver().update(mCurrentStockUri, values, null, null);

                if (rowsAffected == 0) {

                    Toast.makeText(this, getString(R.string.editor_update_stock), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.editor_update_saved), Toast.LENGTH_SHORT).show();

                }

            }
        return true;
    }

    private boolean checkIfValueSet(EditText text, String description) {
        if (TextUtils.isEmpty(text.getText())) {
            text.setError("Produto ausente " + description);
            return false;
        } else {
            text.setError(null);
            return true;
        }
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mCurrentStockUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete_item);
            menuItem.setVisible(false);
            MenuItem menuOrder = menu.findItem(R.id.action_order);
            menuOrder.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if(!saveStock()) {
                    return true;
                }

                finish();
                return true;
            case R.id.action_delete_item:

                showDeleteConfirmationDialog();
                return true;

            case R.id.action_order:
                showOrderConfirmationDialog();
                return true;

            case android.R.id.home:

                if (!mInfoItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                DialogInterface.OnClickListener discarButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discarButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        String[] projection = {
                StockEntry._ID,
                StockEntry.COLUMN_NAME,
                StockEntry.COLUMN_PRICE,
                StockEntry.COLUMN_QUANTITY,
                StockEntry.COLUMN_IMAGE
        };

        return new CursorLoader(this,
                mCurrentStockUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {

            int nameColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_NAME);
            int priceColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            mNameEditText.setText(name);
            mPriceEditeText.setText(price);
            mQuantityEditText.setText(Integer.toString(quantity));
            mImageView.setImageURI(Uri.parse(image));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditeText.setText(Float.toString(0));
        mQuantityEditText.setText(Integer.toString(0));
        mImageView.setImageDrawable(null);

    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mInfoItemHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                deleteStock();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteStock() {

        if (mCurrentStockUri != null) {

            int rowsDeleted = getContentResolver().delete(mCurrentStockUri, null, null);

            if (rowsDeleted == 0) {

                Toast.makeText(this, getString(R.string.editor_delete_stock_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_stock_successful), Toast.LENGTH_SHORT).show();
            }

            finish();
        }
    }

    private void showOrderConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.order_message);
        builder.setNegativeButton(R.string.email, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // intent to email
                Intent intent = new Intent(android.content.Intent.ACTION_SENDTO);
                intent.setType("text/plain");
                intent.setData(Uri.parse("mailto:" + getString(R.string.email_order)));
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.new_order));
                String bodyMessage = getString(R.string.msg_order) +
                        mNameEditText.getText().toString().trim() +
                        "!";
                intent.putExtra(android.content.Intent.EXTRA_TEXT, bodyMessage);
                startActivity(intent);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void subtractOneToQuantity() {
        String previousValueString = mQuantityEditText.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            return;
        } else if (previousValueString.equals("0")) {
            return;
        } else {
            previousValue = Integer.parseInt(previousValueString);
            mQuantityEditText.setText(String.valueOf(previousValue - 1));
        }
    }

    private void sumOneToQuantity() {
        String previousValueString = mQuantityEditText.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            previousValue = 0;
        } else {
            previousValue = Integer.parseInt(previousValueString);
        }
        mQuantityEditText.setText(String.valueOf(previousValue + 1));
    }

    public void tryToOpenImageSelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            return;
        }
        openImageSelector();
    }

    private void openImageSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImageSelector();
                    // permission was granted
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mActualUri = resultData.getData();
                mImageView.setImageURI(mActualUri);
                mImageView.invalidate();
            }
        }
    }

    public boolean isEquals(Bitmap bitmapOne, Bitmap bitmapTwo) {
        ByteBuffer bufferOne = ByteBuffer.allocate(bitmapOne.getHeight() * bitmapOne.getRowBytes());
        bitmapOne.copyPixelsToBuffer(bufferOne);
        ByteBuffer bufferTwo = ByteBuffer.allocate(bitmapTwo.getHeight() * bitmapTwo.getRowBytes());
        bitmapTwo.copyPixelsToBuffer(bufferTwo);
        return Arrays.equals(bufferOne.array(), bufferTwo.array());
    }

}


