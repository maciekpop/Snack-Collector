package com.example.snackcollector;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context mContext;
    private Cursor mCursor;

    public ProductAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {

        public TextView
                textViewListProductName,
                textViewListRating,
                textViewListProductAccessibility,
                textViewListProductPrice;

        public ImageView imageViewListProduct;


        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewListProductName = itemView.findViewById(R.id.textViewListProductName);
            textViewListRating = itemView.findViewById(R.id.textViewListRating);
            textViewListProductAccessibility = itemView.findViewById(R.id.textViewListProductAccessibility);
            textViewListProductPrice = itemView.findViewById(R.id.textViewListProductPrice);
            imageViewListProduct = itemView.findViewById(R.id.imageViewListProduct);
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        if(!mCursor.moveToPosition(position)) {
            return;
        }

        String
                productName = mCursor.getString(mCursor.getColumnIndex(ProductContract.ProductEntry.PRODUCT_NAME)),
                productPrice = mCursor.getString(mCursor.getColumnIndex(ProductContract.ProductEntry.PRODUCT_PRICE)),
                productAccessibility = mCursor.getString(mCursor.getColumnIndex(ProductContract.ProductEntry.PRODUCT_ACCESSIBILITY)),
                productImageFilePath = mCursor.getString(mCursor.getColumnIndex(ProductContract.ProductEntry.PRODUCT_FILE_PATH));

        float productRating = mCursor.getFloat(mCursor.getColumnIndex(ProductContract.ProductEntry.PRODUCT_RATING));

        holder.textViewListProductName.setText(productName);
        holder.textViewListRating.setText(productRating + "/5");
        holder.textViewListProductAccessibility.setText(productAccessibility);
        if(productImageFilePath.contains("external"))
            holder.imageViewListProduct.setImageURI(Uri.parse(productImageFilePath));
        else
            holder.imageViewListProduct.setImageURI(Uri.fromFile(new File(productImageFilePath)));

        if(Float.parseFloat(productPrice) < 1f) {
            holder.textViewListProductPrice.setText(productPrice + " gr");
        }
        else
            holder.textViewListProductPrice.setText(productPrice + " zł");
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if(mCursor != null)
            mCursor.close();
        mCursor = newCursor;
        if(newCursor != null)
            notifyDataSetChanged();
    }
}
