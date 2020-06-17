package com.example.snackcollector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class RecyclerViewActivity extends AppCompatActivity {

    private SQLiteDatabase sqLiteDatabase;
    private ProductAdapter productAdapter;
    private static final int GET_PRODUCT_DATA = 111;
    public static final String PRODUCT_ID_KEY = "product_id";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        sqLiteDatabase = dataBaseHelper.getWritableDatabase();

        buildRecyclerView();

        Button buttonGoToAddProduct = findViewById(R.id.buttonGoToAddProduct);

        buttonGoToAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(RecyclerViewActivity.this, AddProductActivity.class), GET_PRODUCT_DATA);
            }
        });
    }

    private void buildRecyclerView() {
        final RecyclerView recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(this, getAllItems());
        recyclerView.setAdapter(productAdapter);
        productAdapter.setOnProductClickListener(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(int position) {
                long id = (long) recyclerView.findViewHolderForAdapterPosition(position).itemView.getTag();
                Intent intent = new Intent(RecyclerViewActivity.this, ProductDetailsActivity.class);
                intent.putExtra(PRODUCT_ID_KEY, id);
                startActivity(intent);
            }
        });
    }

    private Cursor getAllItems() {
        return sqLiteDatabase.query(
                ProductContract.ProductEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                ProductContract.ProductEntry.PRODUCT_ID + " DESC"
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == GET_PRODUCT_DATA && data != null) {
            Bundle bundle = data.getBundleExtra(AddProductActivity.PRODUCT_DATA);

            ContentValues cv = new ContentValues();

            cv.put(ProductContract.ProductEntry.PRODUCT_NAME, bundle.getString(AddProductActivity.NAME));
            cv.put(ProductContract.ProductEntry.PRODUCT_TYPE, bundle.getString(AddProductActivity.TYPE));
            cv.put(ProductContract.ProductEntry.PRODUCT_PRICE, bundle.getString(AddProductActivity.PRICE));
            cv.put(ProductContract.ProductEntry.PRODUCT_ACCESSIBILITY, bundle.getString(AddProductActivity.ACCESSIBILITY));
            cv.put(ProductContract.ProductEntry.PRODUCT_RATING, bundle.getFloat(AddProductActivity.RATING));
            cv.put(ProductContract.ProductEntry.PRODUCT_FILE_PATH, bundle.getString(AddProductActivity.PATH));

            sqLiteDatabase.insert(ProductContract.ProductEntry.TABLE_NAME, null, cv);
            productAdapter.swapCursor(getAllItems());

        }
    }
}