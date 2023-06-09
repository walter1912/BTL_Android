package com.main.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.main.activities.DisplayWordActivity;
import com.main.activities.FavoriteActivity;
import com.main.database.DatabaseHelper;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.main.R;
import com.main.adapters.WordAdapter;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class SearchAllFragment  extends Fragment{

    private AutoCompleteTextView mAutoTxtSearchAll;
    private DatabaseHelper mDatabaseHelper;
    private ImageButton mButtonVoice;
    private ImageButton mBtnCamera;
    Button mDialogBtnCamera;
    Button mDialogBtnGallery;
    ImageView mPreviewIv;
//    private WordAdapter dataAdapter;

    private static final int REQUEST_CODE_CHANGED_STATUS = 10;
    private static final int REQUEST_CODE_SPEECH_INPUT = 2;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    String cameraPermission[];
    String storagePermission[];

    Uri image_uri;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_all,container,false);
        mAutoTxtSearchAll  = view.findViewById(R.id.searchAll);
        mDatabaseHelper = new DatabaseHelper(getActivity());

        // tìm kiếm toàn bộ từ vựng
        mAutoTxtSearchAll.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    mAutoTxtSearchAll.setAdapter(new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_list_item_1, mDatabaseHelper.getEngWord(s.toString())));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mAutoTxtSearchAll.setOnClickListener(v -> {
            // Xử lý khi AutoCompleteTextView được click
            CharSequence searchText = mAutoTxtSearchAll.getText(); // Lấy dữ liệu từ AutoCompleteTextView

            if (searchText.length() >= 1) {
                mAutoTxtSearchAll.setAdapter(new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_list_item_1, mDatabaseHelper.getEngWord(searchText.toString())));

                // Hiển thị danh sách gợi ý
                mAutoTxtSearchAll.showDropDown();
            }
        });

        mAutoTxtSearchAll.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Lấy danh sách gợi ý
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) mAutoTxtSearchAll.getAdapter();
                if (adapter != null && adapter.getCount() > 0) {
                    // Chọn item đầu tiên
                    String firstItem = adapter.getItem(0);
                    getDescription(firstItem);
                }
                return true;
            }
            return false;
        });


        // xử lý item trong list auto text
        mAutoTxtSearchAll.setOnItemClickListener((parent, view1, position, id) -> {
            String mWord = (String) parent.getItemAtPosition(position);
            getDescription(mWord);
        });

        // tìm bằng giọng nói
        mButtonVoice = view.findViewById(R.id.button_voice);
        mButtonVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }

        });

        // định nghĩa image view để set image khi chụp ảnh
        mPreviewIv = view.findViewById(R.id.imageIv);
        // nút camera
        mBtnCamera = view.findViewById(R.id.button_camera);
        mBtnCamera.setOnClickListener(v -> showImageImportDialog());

        //camera permission
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //storage permission
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        return view;
    }

    // xử lý voice to text
    private void speak() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.UK);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi speak something");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        }catch (Exception e){
            Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    // Hiện dialog để chọn camera hay gallery
    private void showImageImportDialog() {
        Dialog dialogCamera = new Dialog(getContext());
        dialogCamera.setContentView(R.layout.dialog_choose_camera);
        dialogCamera.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg_choose_camera);
        dialogCamera.show();

        mDialogBtnCamera = dialogCamera.findViewById(R.id.btn_camera);
        mDialogBtnCamera.setOnClickListener(v -> {
            //camera option click
            if(!checkCameraPermission()){
                // yêu cầu quyền truy cập máy ảnh nếu không được phép
                requestCameraPermission();
            }else{
                // chụp ảnh khi được truy cập
                pickCamera();
            }
            dialogCamera.dismiss();
        });

        mDialogBtnGallery = dialogCamera.findViewById(R.id.btn_gallery);
        mDialogBtnGallery.setOnClickListener(v -> {
            //gallery option click
            if(!checkStoragePermission()){
                // yêu cầu quyền truy cập gallery nếu không được phép
                requestStoragePermission();
            }else{
                // lấy ảnh khi được truy cập
                pickGallery();
            }
            dialogCamera.dismiss();
        });
    }

    // các hàm để xử lý lấy text từ hình ảnh
    private void pickGallery() {
        //intent để lấy image từ gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        //intent để lấy ảnh camera, nó sẽ được lưu lại để có ảnh chất lượng cao
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NewPic"); //title of picture
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image To Text"); //description
        image_uri = requireActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }



    // các hàm để xử lý lấy text từ hình ảnh
    //end

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(getActivity(), storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(getActivity(), cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        //Check camera permission and return the result
        //In order to get high quality picture we have to save image to external storage first
        //before interesting to image view that's why storage permission will also be required
        boolean result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    //call displayWordActivity class
    private void getDescription(String word) {
        Intent intent = new Intent(getActivity(), DisplayWordActivity.class);
        intent.putExtra("word", word);
        startActivity(intent);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //got image from gallery now crop it
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON) //enable image guidelines
                        .start(getContext(), SearchAllFragment.this);
            }

            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //got image from camera now crop it
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON) //enable image guidelines
                        .start(getContext(), SearchAllFragment.this);
            }
        }

        switch (requestCode) {
            //trường hợp voice to text
            case REQUEST_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && data != null) {
                    //lấy text từ intent voice
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //set text to view
                    getDescription(result.get(0));
                }
                break;
            }

            //lấy ảnh đã cắt
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE: {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri(); //get image uri
                    // set image to image view
                    mPreviewIv.setImageURI(resultUri);

                    //get drawable bitmap for text recognition
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) mPreviewIv.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();

                    TextRecognizer textRecognizer = new TextRecognizer.Builder(getActivity().getApplicationContext()).build();
                    if (!textRecognizer.isOperational()) {
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    } else {
                        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                        SparseArray<TextBlock> items = textRecognizer.detect(frame);
                        StringBuilder sb = new StringBuilder();
                        //get text từ sb cho đến khi không còn text
                        for (int i = 0; i < items.size(); i++) {
                            TextBlock myItem = items.valueAt(i);
                            sb.append(myItem.getValue());
                        }
                        //gọi displayWord bằng sb
                        if (sb != null && !sb.toString().isEmpty()) {
                            //call displayWord by text
                            getDescription(sb.toString());
                        }
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    //nếu có lỗi thì sẽ hiển thị ra lỗi
                    Exception error = result.getError();
                    Toast.makeText(getContext(), "" + error, Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case REQUEST_CODE_CHANGED_STATUS:{
                Intent i = new Intent(getActivity(), FavoriteActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        }
    }
}