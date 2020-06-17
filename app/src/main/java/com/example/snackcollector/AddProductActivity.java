package com.example.snackcollector;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

public class AddProductActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final int REQUEST_CAMERA_PERMISSION_CODE = 99;
    private static final int REQUEST_READ_EXTERNAL_STORAGE_CODE = 98;
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_CAMERA = 2;
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String PRICE = "price";
    public static final String ACCESSIBILITY = "accessibility";
    public static final String RATING = "rating";
    public static final String PATH = "path";
    public static final String PRODUCT_DATA = "product_data";

    private static Dialog dialogCameraOrGallery;

    private Button buttonAddImage;
    private EditText editTextProductName, editTextProductPrice, editTextAccessibility;
    private RatingBar ratingBar;
    private String productType;
    private ImageView imageViewProduct;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initActivityItems();

        buttonAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageChooseDialog(AddProductActivity.this);
            }
        });
    }

    private void initActivityItems() {

        Spinner spinnerProductType = findViewById(R.id.spinnerProductType);
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextProductPrice = findViewById(R.id.editTextProductPrice);
        editTextAccessibility = findViewById(R.id.editTextAccessibility);
        ratingBar = findViewById(R.id.ratingBar);
        imageViewProduct = findViewById(R.id.imageViewProduct);
        buttonAddImage = findViewById(R.id.buttonAddImage);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(AddProductActivity.this, R.array.product_types, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProductType.setAdapter(arrayAdapter);
        spinnerProductType.setOnItemSelectedListener(this);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        productType = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }

    public void onClickAddProduct(View view) {

        if(TextUtils.isEmpty(editTextProductName.getText().toString().trim()) ||
                TextUtils.isEmpty(productType.trim()) ||
                TextUtils.isEmpty(editTextAccessibility.getText().toString().trim()) ||
                TextUtils.isEmpty(editTextProductPrice.getText().toString().trim()) ||
                ratingBar.getRating() == 0 ||
                TextUtils.isEmpty(currentPhotoPath)) {
            Toast.makeText(this, "Uzupełnij niezbędne dane.", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Dodano produkt.", Toast.LENGTH_SHORT).show();
        setBundle();
        clearAll();
        finish();
    }

    private void clearAll() {
        editTextProductName.getText().clear();
        editTextProductPrice.getText().clear();
        editTextAccessibility.getText().clear();
        ratingBar.setRating(0);
        imageViewProduct.setImageDrawable(null);
        buttonAddImage.setText("Dodaj zdjęcie");
    }

    private void setBundle() {
        Intent intent = new Intent(AddProductActivity.this, RecyclerViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(NAME, editTextProductName.getText().toString().trim());
        bundle.putString(TYPE, productType);
        bundle.putString(PRICE, editTextProductPrice.getText().toString().trim());
        bundle.putString(ACCESSIBILITY, editTextAccessibility.getText().toString().trim());
        bundle.putFloat(RATING, ratingBar.getRating());
        bundle.putString(PATH, currentPhotoPath);
        intent.putExtra(PRODUCT_DATA, bundle);
        setResult(RESULT_OK, intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CAMERA_PERMISSION_CODE) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                dispatchTakePictureIntent();
            if(grantResults[0] == PackageManager.PERMISSION_DENIED)
                Toast.makeText(this, R.string.camPermission, Toast.LENGTH_LONG).show();
            if(grantResults[1] == PackageManager.PERMISSION_DENIED)
                Toast.makeText(this, R.string.filesPermission, Toast.LENGTH_LONG).show();
        }

        if(requestCode == REQUEST_READ_EXTERNAL_STORAGE_CODE) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                dispatchPickImageIntent();
            else
                Toast.makeText(this, R.string.filesPermission, Toast.LENGTH_LONG).show();
        }
    }

    private void dispatchPickImageIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
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
                currentPhotoPath = photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
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
        //currentPhotoPath = image.getAbsolutePath();
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

            if (requestCode == REQUEST_IMAGE_PICK) {
                try {
                    Uri imageUri = data.getData();
                    currentPhotoPath = imageUri.toString();
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    showImage(imageViewProduct, bitmap, setRotationVariables(imageUri));
                    buttonAddImage.setText("Zmień zdjęcie");
                }
                catch (Exception e) {
                    Toast.makeText(this, "Nie ma danych.", Toast.LENGTH_SHORT).show();
                    dialogCameraOrGallery.show();
                }
            }
            if (requestCode == REQUEST_CAMERA) {
                try {
                    Uri imageUri = Uri.fromFile(new File(currentPhotoPath));
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    showImage(imageViewProduct, bitmap, setRotationVariables(imageUri));
                    buttonAddImage.setText("Zmień zdjęcie");
                }
                catch (Exception e) {
                    Toast.makeText(this, "Nie ma danych.", Toast.LENGTH_SHORT).show();
                    dialogCameraOrGallery.show();
                }
            }
        }
    }

    public void showImageChooseDialog (Activity activity) {
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
                    requestPermissions(permissionRequest, REQUEST_READ_EXTERNAL_STORAGE_CODE);
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
                    requestPermissions(permissionRequest, REQUEST_CAMERA_PERMISSION_CODE);
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