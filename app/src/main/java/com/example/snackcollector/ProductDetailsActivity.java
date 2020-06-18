package com.example.snackcollector;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class ProductDetailsActivity extends AppCompatActivity {

    private String productName,
            productType,
            productPrice,
            productAccessibility,
            productImageFilePath;
    float productRating;

    int position;

    private SQLiteDatabase sqLiteDatabase;
    private ProductAdapter productAdapter;
    private long id;

    private static Dialog dialogDelete;

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String PRICE = "price";
    public static final String ACCESSIBILITY = "accessibility";
    public static final String RATING = "rating";
    public static final String PATH = "path";
    public static final String REQUEST_EDIT = "request_edit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        initView();

        Button buttonDelete = findViewById(R.id.buttonDelete);

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog(ProductDetailsActivity.this);
            }
        });

    }

    public void initView() {

        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        sqLiteDatabase = dataBaseHelper.getReadableDatabase();
        Intent intent = getIntent();
        id = intent.getLongExtra(RecyclerViewActivity.PRODUCT_ID_KEY, -1);
        position = intent.getIntExtra(RecyclerViewActivity.POSITION, -1);
        Cursor cursor = sqLiteDatabase.query(
                ProductContract.ProductEntry.TABLE_NAME,
                null,
                ProductContract.ProductEntry.PRODUCT_ID + " = " + id,
                null,
                null,
                null,
                null
        );
        cursor.moveToPosition(0);
        productName = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.PRODUCT_NAME));
                productType = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.PRODUCT_TYPE));
                productPrice = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.PRODUCT_PRICE));
                productAccessibility = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.PRODUCT_ACCESSIBILITY));
                productImageFilePath = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.PRODUCT_FILE_PATH));
        productRating = cursor.getFloat(cursor.getColumnIndex(ProductContract.ProductEntry.PRODUCT_RATING));
        cursor.close();
        TextView textViewProductNameDisplay = findViewById(R.id.textViewProductNameDisplay),
                textViewProductTypeDisplay = findViewById(R.id.textViewProductTypeDisplay),
                textViewProductPriceDisplay = findViewById(R.id.textViewProductPriceDisplay),
                textViewProductAccessibilityDisplay = findViewById(R.id.textViewProductAccessibilityDisplay);
        RatingBar ratingBarDisplay = findViewById(R.id.ratingBarDisplay);
        ratingBarDisplay.setEnabled(false);
        ImageView imageViewProductDisplay = findViewById(R.id.imageViewProductDisplay);

        textViewProductNameDisplay.setText(productName);
        textViewProductTypeDisplay.setText(productType);
        if(Float.parseFloat(productPrice) < 1f)
            textViewProductPriceDisplay.setText(Float.parseFloat(productPrice)*100 + " gr");
        else
            textViewProductPriceDisplay.setText(productPrice + " zł");
        textViewProductAccessibilityDisplay.setText(productAccessibility);
        ratingBarDisplay.setRating(productRating);
        if(productImageFilePath.contains("external")) {
            try {
                Uri imageUri = Uri.parse(productImageFilePath);
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                showImage(imageViewProductDisplay, bitmap, setRotationVariables(imageUri));
            }
            catch (Exception e) {
                Toast.makeText(this, "Nie ma danych.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            try {
                Uri imageUri = Uri.fromFile(new File(productImageFilePath));
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                showImage(imageViewProductDisplay, bitmap, setRotationVariables(imageUri));
            }
            catch (Exception e) {
                Toast.makeText(this, "Nie ma danych.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void editProduct(View view) {

        Bundle bundle = new Bundle();
        Intent intent = new Intent(ProductDetailsActivity.this, EditProductActivity.class);

        bundle.putInt(RecyclerViewActivity.POSITION, position);
        bundle.putLong(ID, id);
        bundle.putString(NAME, productName);
        bundle.putString(PRICE, productPrice);
        bundle.putString(ACCESSIBILITY, productAccessibility);
        bundle.putString(PATH, productImageFilePath);
        bundle.putFloat(RATING, productRating);
        intent.putExtra(REQUEST_EDIT, bundle);
        finish();
        startActivity(intent);
    }

    private void deleteProduct() {

        sqLiteDatabase.delete(ProductContract.ProductEntry.TABLE_NAME, ProductContract.ProductEntry.PRODUCT_ID + " = " + id, null);
        RecyclerViewActivity.productAdapter.swapCursor(sqLiteDatabase.query(
                ProductContract.ProductEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                ProductContract.ProductEntry.PRODUCT_ID + " DESC"
        ));
        Toast.makeText(this, "Usunięto produkt.", Toast.LENGTH_SHORT).show();
        dialogDelete.dismiss();
        finish();
    }

    private int setRotationVariables(Uri uri)
    {
        return ImageOrientationUtil.getExifRotation(ImageOrientationUtil
                .getFromMediaUri(
                        this,
                        getContentResolver(),
                        uri));
    }

    private static void fitImageViewToImage(ImageView i, Bitmap b) {
        i.setAdjustViewBounds(true);
        i.setMaxHeight(b.getHeight());
        i.setMaxWidth(b.getWidth());
    }

    private static void showImage(ImageView i, Bitmap b, int a) {
        Matrix matrix = new Matrix();
        matrix.postRotate(a);
        b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
        fitImageViewToImage(i, b);
        i.setImageBitmap(b);
    }

    public void showDeleteDialog (Activity activity) {
        dialogDelete = new Dialog(activity);
        dialogDelete.setCancelable(false);
        dialogDelete.setContentView(R.layout.delete_product);

        Button buttonConfirmDelete = dialogDelete.findViewById(R.id.buttonConfirmDelete),
                buttonCancel = dialogDelete.findViewById(R.id.buttonCancel);

        buttonConfirmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteProduct();

            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDelete.dismiss();
            }
        });

        dialogDelete.show();
    }

}