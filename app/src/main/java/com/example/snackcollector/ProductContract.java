package com.example.snackcollector;

import android.provider.BaseColumns;

public class ProductContract {

    private ProductContract() {}

    public static final class ProductEntry implements BaseColumns {
        public static final String TABLE_NAME = "product_list";
        public static final String PRODUCT_ID = "product_id";
        public static final String PRODUCT_NAME = "product_name";
        public static final String PRODUCT_PRICE = "product_price";
        public static final String PRODUCT_ACCESSIBILITY = "product_accessibility";
        public static final String PRODUCT_RATING = "product_rating";
        public static final String PRODUCT_FILE_PATH = "product_file_path";
    }
}
