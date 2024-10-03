package com.example.studentdetail;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class editfrag extends Fragment {
    CheckBox Hostel, Dayscholar, outpass;
    String[] dept = {"CSE", "IT", "ECE", "CIVIL", "MECH", "AI&DS", "Other"};
    String[] year = {"I", "II", "III", "IV"};
    AutoCompleteTextView deptview, yearview;
    ArrayAdapter<String> adapter_dept, adapter_year;
    ImageView imageView;
    StorageReference ImageReference;
    Button button, overall;
    EditText Name, Roll, DOB, stay_no, Phone, Address, cgpa;
    TextView t3, t1, bus;
    String stay;
    DatabaseReference databaseReference;
    ProgressBar progressBar2;

    private boolean imageupload = false;

    public editfrag() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editfrag, container, false);
        initializeUIElements(view);
        setupAdapters();
        setupCheckboxListeners();
        setupImagePicker();
        setupOverallButton();
        return view;
    }

    private void initializeUIElements(View view) {
        progressBar2 = view.findViewById(R.id.progressBar2);
        deptview = view.findViewById(R.id.deptview);
        yearview = view.findViewById(R.id.yearview);
        Hostel = view.findViewById(R.id.Hostel);
        Dayscholar = view.findViewById(R.id.Dayscholar);
        imageView = view.findViewById(R.id.Photo);
        button = view.findViewById(R.id.button);
        Name = view.findViewById(R.id.Name);
        Roll = view.findViewById(R.id.Roll);
        DOB = view.findViewById(R.id.DOB);
        t1 = view.findViewById(R.id.stay_no);
        t3 = view.findViewById(R.id.photo);
        Phone = view.findViewById(R.id.editTextPhone);
        Address = view.findViewById(R.id.editTextTextPostalAddress);
        bus = view.findViewById(R.id.bus);
        cgpa = view.findViewById(R.id.cgpa);
        stay_no = view.findViewById(R.id.stay_et);
        outpass = view.findViewById(R.id.outpass);
        overall = view.findViewById(R.id.overall);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        ImageReference = FirebaseStorage.getInstance().getReference("Photos");

        Name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

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
    }

    private void setupAdapters() {
        adapter_dept = new ArrayAdapter<>(requireContext(), R.layout.dept_list, dept);
        deptview.setAdapter(adapter_dept);
        adapter_year = new ArrayAdapter<>(requireContext(), R.layout.dept_list, year);
        yearview.setAdapter(adapter_year);
    }

    @SuppressLint("SetTextI18n")
    private void setupCheckboxListeners() {
        Hostel.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                Dayscholar.setChecked(false);
                outpass.setChecked(false);
                stay = "Hostel";
                t1.setText("Room no:");
                stay_no.setHint("Enter room number");
                bus.setVisibility(View.GONE);
                stay_no.setVisibility(View.VISIBLE);
            }
        });

        Dayscholar.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                Hostel.setChecked(false);
                outpass.setChecked(false);
                stay = "Dayscholar";
                t1.setText("Bus no:");
                stay_no.setHint("Enter Bus no");
                bus.setVisibility(View.GONE);
                stay_no.setVisibility(View.VISIBLE);
            }
        });

        outpass.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                Hostel.setChecked(false);
                Dayscholar.setChecked(true);
                stay = "Outpass";
                bus.setText("OUTPASS");
                bus.setVisibility(View.VISIBLE);
                t1.setText("Bus no:");
                stay_no.setVisibility(View.INVISIBLE);
            } else {
                bus.setVisibility(View.GONE);
                stay_no.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupImagePicker() {
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

        button.setOnClickListener(view13 -> chooseImage(imagePickerLauncher));
    }

    private void setupOverallButton() {
        overall.setOnClickListener(view1 -> {
            if (Name.getText().toString().isEmpty() || Roll.getText().toString().isEmpty() || DOB.getText().toString().isEmpty()
                    || deptview.getText().toString().isEmpty() || yearview.getText().toString().isEmpty() || stay_no.getText().toString().isEmpty() || cgpa.getText().toString().isEmpty()) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Please fill all the details", Toast.LENGTH_SHORT).show();
                }
            } else if (!Hostel.isChecked() && !Dayscholar.isChecked() && !outpass.isChecked()) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Please select Hostel, Dayscholar, or Outpass", Toast.LENGTH_SHORT).show();
                }
            } else if ((Hostel.isChecked() || Dayscholar.isChecked()) && stay_no.getText().toString().isEmpty()) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Please enter " + (Hostel.isChecked() ? "Room no" : "Bus no"), Toast.LENGTH_SHORT).show();
                }
            } else if (!imageupload) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Please upload your image", Toast.LENGTH_SHORT).show();
                }
            } else {
                uploaddata();
                Bundle bundle = new Bundle();
                bundle.putString("name", Name.getText().toString());
                bundle.putString("roll", Roll.getText().toString());
                bundle.putString("dept", deptview.getText().toString());
                bundle.putString("stay", this.stay);

                File localFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "download_image.jpg");
                bundle.putString("image_path", localFile.getAbsolutePath());

                getParentFragmentManager().setFragmentResult("requestKey", bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new homefrag())
                        .commit();

            }
        });
    }

    private void chooseImage(ActivityResultLauncher<Intent> imagePickerLauncher) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(intent);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        String roll = Roll.getText().toString();
        if (roll.isEmpty()) {
            if (isAdded()) {
                Toast.makeText(getContext(), "Please enter Roll number before uploading image", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        pbar(true);

        StorageReference fileReference = ImageReference.child(roll + ".jpg");
        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageupload = true;
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Upload successful", Toast.LENGTH_SHORT).show();
                    }
                    displayImageFromFirebase();
                    pbar(false);
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    pbar(false);
                });
    }

    private void displayImageFromFirebase() {
        String roll = Roll.getText().toString();
        StorageReference fileReference = ImageReference.child(roll + ".jpg");
        File localFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "download_image.jpg");

        fileReference.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    imageView.setImageBitmap(bitmap);
                });
    }

    private void uploaddata() {
        String name = Name.getText().toString();
        String roll = Roll.getText().toString();
        String dob = DOB.getText().toString();
        String department = deptview.getText().toString();
        String year = yearview.getText().toString();
        String phoneno = Phone.getText().toString();
        String address = Address.getText().toString();
        String cgpaStr = cgpa.getText().toString();

        DatabaseReference studentsRef = databaseReference.child("students").child(roll);
        studentsRef.child("Name").setValue(name);
        studentsRef.child("Roll").setValue(roll);
        studentsRef.child("DOB").setValue(dob);
        studentsRef.child("Department").setValue(department);
        studentsRef.child("Year").setValue(year);
        studentsRef.child("Phone").setValue(phoneno);
        studentsRef.child("Address").setValue(address);
        studentsRef.child("Stay").setValue(stay);
        studentsRef.child("Stay_no").setValue(stay_no.getText().toString());
        studentsRef.child("CGPA").setValue(cgpaStr);
    }

    private void visibility() {
        if (!Name.getText().toString().isEmpty()) {
            t3.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
        }
    }

    void pbar(boolean inprogress) {
        if (progressBar2 != null && button != null) {
            if (inprogress) {
                progressBar2.setVisibility(View.VISIBLE);
                button.setVisibility(View.INVISIBLE);
            } else {
                progressBar2.setVisibility(View.GONE);
                button.setVisibility(View.VISIBLE);
            }
        }
    }
}
