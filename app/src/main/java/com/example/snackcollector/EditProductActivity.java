package com.example.snackcollector;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditProductActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private long id;
    private int position;

    private EditText editTextChangeName,
            editTextChangePrice,
            editTextChangeAccessibility;

    private RatingBar ratingBarChange;

    private ImageView imageViewChangeImage;

    private String productType;
    private String imagePath;

    private Button buttonChangeImage;

    private static Dialog dialogCameraOrGallery;

    private SQLiteDatabase sqLiteDatabase;
   // private ProductAdapter productAdapter;

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String PRICE = "price";
    public static final String ACCESSIBILITY = "accessibility";
    public static final String RATING = "rating";
    public static final String PATH = "path";
    public static final String EDITED_PRODUCT_DATA = "edited_product_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_product);
        initView();
    }

    private void initView() {

        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        sqLiteDatabase = dataBaseHelper.getReadableDatabase();

        editTextChangeName = findViewById(R.id.editTextChangeName);
        editTextChangePrice = findViewById(R.id.editTextChangePrice);
        editTextChangeAccessibility = findViewById(R.id.editTextChangeAccessibility);
        ratingBarChange = findViewById(R.id.ratingBarChange);

        imageViewChangeImage = findViewById(R.id.imageViewChangeImage);

        Spinner spinnerChangeType = findViewById(R.id.spinnerChangeType);

        buttonChangeImage = findViewById(R.id.buttonChangeImage);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(EditProductActivity.this, R.array.product_types, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChangeType.setAdapter(arrayAdapter);
        spinnerChangeType.setOnItemSelectedListener(this);


        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(ProductDetailsActivity.REQUEST_EDIT);
        position = bundle.getInt(RecyclerViewActivity.POSITION);
        id = bundle.getLong(ProductDetailsActivity.ID);
        editTextChangeName.setText(bundle.getString(ProductDetailsActivity.NAME));
        editTextChangePrice.setText(bundle.getString(ProductDetailsActivity.PRICE));
        editTextChangeAccessibility.setText(bundle.getString(ProductDetailsActivity.ACCESSIBILITY));
        ratingBarChange.setRating(bundle.getFloat(ProductDetailsActivity.RATING, 0));
        imagePath = bundle.getString(ProductDetailsActivity.PATH);

        if(imagePath.contains("external")) {
            try {
                Uri imageUri = Uri.parse(imagePath);
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                showImage(imageViewChangeImage, bitmap, setRotationVariables(imageUri));
            }
            catch (Exception e) {
                Toast.makeText(this, "Nie ma danych.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            try {
                Uri imageUri = Uri.fromFile(new File(imagePath));
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                showImage(imageViewChangeImage, bitmap, setRotationVariables(imageUri));
            }
            catch (Exception e) {
                Toast.makeText(this, "Nie ma danych.", Toast.LENGTH_SHORT).show();
            }
        }

        buttonChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageChooseDialog(EditProductActivity.this);
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        productType = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }

    public void confirmChanges(View view) {

        if(TextUtils.isEmpty(editTextChangeName.getText().toString().trim()) ||
                TextUtils.isEmpty(productType.trim()) ||
                TextUtils.isEmpty(editTextChangeAccessibility.getText().toString().trim()) ||
                TextUtils.isEmpty(editTextChangePrice.getText().toString().trim()) ||
                ratingBarChange.getRating() == 0 ||
                TextUtils.isEmpty(imagePath)) {
            Toast.makeText(this, "Uzupełnij niezbędne dane.", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Zmieniono produkt.", Toast.LENGTH_SHORT).show();
        finalizeEdit();
        clearAll();
        finish();
    }

    private void clearAll() {
        editTextChangeName.getText().clear();
        editTextChangePrice.getText().clear();
        editTextChangeAccessibility.getText().clear();
        ratingBarChange.setRating(0);
        imageViewChangeImage.setImageDrawable(null);
    }

    private void finalizeEdit() {
        ContentValues cv = new ContentValues();
        cv.put(ProductContract.ProductEntry.PRODUCT_NAME, editTextChangeName.getText().toString().trim());
        cv.put(ProductContract.ProductEntry.PRODUCT_TYPE, productType);
        cv.put(ProductContract.ProductEntry.PRODUCT_PRICE, editTextChangePrice.getText().toString().trim());
        cv.put(ProductContract.ProductEntry.PRODUCT_ACCESSIBILITY, editTextChangeAccessibility.getText().toString().trim());
        cv.put(ProductContract.ProductEntry.PRODUCT_RATING, ratingBarChange.getRating());
        cv.put(ProductContract.ProductEntry.PRODUCT_FILE_PATH, imagePath);

        sqLiteDatabase.update(
                ProductContract.ProductEntry.TABLE_NAME,
                cv,
                ProductContract.ProductEntry.PRODUCT_ID + " = " + id,
                null);

        RecyclerViewActivity.productAdapter.swapCursor(sqLiteDatabase.query(
                ProductContract.ProductEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                ProductContract.ProductEntry.PRODUCT_ID + " DESC"
        ));
    }

    public void cancelEditing(View view) {
        clearAll();
        finish();
    }


    private void dispatchPickImageIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, AddProductActivity.REQUEST_IMAGE_PICK);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Nie udało się utworzyć pliku zdjęcia.", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri imageUri = FileProvider.getUriForFile(this, "com.example.snackcollector.fileProvider", photoFile);
                imagePath = photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, AddProductActivity.REQUEST_CAMERA);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "snackcollector" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            dialogCameraOrGallery.dismiss();

            if (requestCode == AddProductActivity.REQUEST_IMAGE_PICK) {
                try {
                    Uri imageUri = data.getData();
                    imagePath = imageUri.toString();
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    showImage(imageViewChangeImage, bitmap, setRotationVariables(imageUri));
                }
                catch (Exception e) {
                    Toast.makeText(this, "Nie ma danych.", Toast.LENGTH_SHORT).show();
                    dialogCameraOrGallery.show();
                }
            }
            if (requestCode == AddProductActivity.REQUEST_CAMERA) {
                try {
                    Uri imageUri = Uri.fromFile(new File(imagePath));
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    showImage(imageViewChangeImage, bitmap, setRotationVariables(imageUri));
                }
                catch (Exception e) {
                    Toast.makeText(this, "Nie ma danych.", Toast.LENGTH_SHORT).show();
                    dialogCameraOrGallery.show();
                }
            }
        }
    }

    private void showImageChooseDialog (Activity activity) {
        dialogCameraOrGallery = new Dialog(activity);
        dialogCameraOrGallery.setCancelable(false);
        dialogCameraOrGallery.setContentView(R.layout.camera_or_gallery);

        Button buttonGallery = dialogCameraOrGallery.findViewById(R.id.buttonGallery);
        Button buttonCamera = dialogCameraOrGallery.findViewById(R.id.buttonCamera);
        Button buttonExitDialog = dialogCameraOrGallery.findViewById(R.id.buttonExitDialog);

        buttonGallery.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    dispatchPickImageIntent();
                else {
                    String[] permissionRequest = {Manifest.permission.READ_EXTERNAL_STORAGE};
                    requestPermissions(permissionRequest, AddProductActivity.REQUEST_READ_EXTERNAL_STORAGE_CODE);
                }
            }
        });

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    dispatchTakePictureIntent();
                else {
                    String[] permissionRequest = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permissionRequest, AddProductActivity.REQUEST_CAMERA_PERMISSION_CODE);
                }
            }
        });

        buttonExitDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogCameraOrGallery.dismiss();
            }
        });

        dialogCameraOrGallery.show();
    }
}