package com.example.snackcollector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;

public class ProductDetailsActivity extends AppCompatActivity {

    private SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        initView();

    }

    private void initView() {

        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        sqLiteDatabase = dataBaseHelper.getReadableDatabase();
        Intent intent = getIntent();
        long id = intent.getLongExtra(RecyclerViewActivity.PRODUCT_ID_KEY, -1);
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
        String productName = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.PRODUCT_NAME)),
                productType = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.PRODUCT_TYPE)),
                productPrice = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.PRODUCT_PRICE)),
                productAccessibility = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.PRODUCT_ACCESSIBILITY)),
                productImageFilePath = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.PRODUCT_FILE_PATH));
        float productRating = cursor.getFloat(cursor.getColumnIndex(ProductContract.ProductEntry.PRODUCT_RATING));

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
                showImage(imageViewProductDisplay, bitmap, setRotationVariables(Uri.parse(productImageFilePath)));
            }
            catch (Exception e) {
                Toast.makeText(this, "Nie ma danych.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            try {
                Uri imageUri = Uri.fromFile(new File(productImageFilePath));
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                showImage(imageViewProductDisplay, bitmap, setRotationVariables(Uri.parse(productImageFilePath)));
            }
            catch (Exception e) {
                Toast.makeText(this, "Nie ma danych.", Toast.LENGTH_SHORT).show();
            }
            imageViewProductDisplay.setImageURI(Uri.fromFile(new File(productImageFilePath)));
        }
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

}