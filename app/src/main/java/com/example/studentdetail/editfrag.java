package com.example.studentdetail;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 */
public class editfrag extends Fragment {
    CheckBox Hostel, Dayscholar;
    String[] dept = {"CSE", "IT", "ECE", "CIVIL", "MECH", "AI&DS", "Other"};
    String[] year = {"I", "II", "III", "IV"};
    AutoCompleteTextView deptview, yearview;
    ArrayAdapter<String> adapter_dept, adapter_year;
    ImageView imageView;
    StorageReference ImageReference;
    Button button,overall;
    EditText Name, Roll, DOB;
    TextView t3;
    String stay;
    DatabaseReference databaseReference;

    private static final int PICK_IMAGE_REQUEST = 1;

    public editfrag() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editfrag, container, false);

        deptview = view.findViewById(R.id.deptview);
        yearview = view.findViewById(R.id.yearview);
        Hostel = view.findViewById(R.id.Hostel);
        Dayscholar = view.findViewById(R.id.Dayscholar);
        imageView = view.findViewById(R.id.Photo);
        button = view.findViewById(R.id.button);
        Name = view.findViewById(R.id.Name);
        Roll = view.findViewById(R.id.Roll);
        DOB = view.findViewById(R.id.DOB);
        t3 = view.findViewById(R.id.photo);
        overall = view.findViewById(R.id.overall);
        databaseReference = FirebaseDatabase.getInstance().getReference();



        ImageReference = FirebaseStorage.getInstance().getReference("Profile_Image");
        visibility();
        Name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                visibility();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                visibility();

            }
        });

        if (!Name.getText().toString().isEmpty()) {
            displayImageFromFirebase();


        }

        adapter_dept = new ArrayAdapter<>(getContext(), R.layout.dept_list, dept); // department list
        deptview.setAdapter(adapter_dept);
        deptview.setOnItemClickListener((adapterView, view1, i, l) -> {
            String selectedDept = adapterView.getItemAtPosition(i).toString();
            Toast.makeText(getContext(), selectedDept, Toast.LENGTH_SHORT).show();
        });


        adapter_year = new ArrayAdapter<>(getContext(), R.layout.dept_list, year);// year list
        yearview.setAdapter(adapter_year);
        yearview.setOnItemClickListener((adapterView, view2, j, m) -> {
            String selectedYear = adapterView.getItemAtPosition(j).toString();
            Toast.makeText(getContext(), selectedYear, Toast.LENGTH_SHORT).show();
        });

        Hostel.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {Dayscholar.setChecked(false);
                stay = "Hostel";
        }});// check boxes

        Dayscholar.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) Hostel.setChecked(false);
            stay = "Dayscholar";
        });

        ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadImageToFirebase(imageUri);
                        }
                    }
                });
// buttons
        button.setOnClickListener(view13 -> chooseImage(imagePickerLauncher));
        overall.setOnClickListener(view1 -> {
            if(Name.getText().toString().isEmpty() || Roll.getText().toString().isEmpty() || DOB.getText().toString().isEmpty()|| deptview.getText().toString().isEmpty() || yearview.getText().toString().isEmpty()){
                Toast.makeText(getContext(), "Please fill all the details", Toast.LENGTH_SHORT).show();

            } else if (!Hostel.isChecked() && !Dayscholar.isChecked()) {
                Toast.makeText(getContext(), "Please select Hostel or Dayscholar", Toast.LENGTH_SHORT).show();
                
            } else if (!imageupload==true) {
                Toast.makeText(getContext(), "Please upload your image", Toast.LENGTH_SHORT).show();

            } else {
                uploaddata();
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            } ;
        });

        return view;
    }


    private void chooseImage(ActivityResultLauncher<Intent> imagePickerLauncher) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(intent);
    }
    private boolean imageupload = false;

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference fileReference = ImageReference.child(Name.getText().toString()+".jpg");
        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageupload = true;
                    Toast.makeText(getContext(), "Upload successful", Toast.LENGTH_SHORT).show();
                    displayImageFromFirebase();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayImageFromFirebase() {
        StorageReference imageref = ImageReference.child(Name.getText().toString()+".jpg");

        final File localFile = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "download_image.jpg");
        imageref.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
        });
    }
    private void uploaddata() {

        String name = Name.getText().toString();
        String roll = Roll.getText().toString();
        String dob = DOB.getText().toString();
        String dept = deptview.getText().toString();
        String year = yearview.getText().toString();
        String stay = this.stay;

        details details = new details(name,roll,dob,dept,year,stay);
        databaseReference.child(name).setValue(details);
        Toast.makeText(getContext(), "Data uploaded successfully", Toast.LENGTH_SHORT).show();
    }
    private void visibility(){
        if(!Name.getText().toString().isEmpty()){
           t3.setVisibility(View.VISIBLE);
           imageView.setVisibility(View.VISIBLE);
           button.setVisibility(View.VISIBLE);
        }

        else{
            t3.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);
            button.setVisibility(View.INVISIBLE);
        }

    }

}
