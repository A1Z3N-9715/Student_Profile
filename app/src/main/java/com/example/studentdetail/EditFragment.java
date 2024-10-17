package com.example.studentdetail;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.util.Log;
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

public class EditFragment extends Fragment {

    private CheckBox hostelCheckBox, dayScholarCheckBox, outpassCheckBox;
    private AutoCompleteTextView deptView, yearView;
    private ArrayAdapter<String> deptAdapter, yearAdapter;
    private ImageView photoImageView;
    private Button uploadButton, submitButton;
    private EditText nameEditText, rollEditText, dobEditText, stayNoEditText, phoneEditText, addressEditText, cgpaEditText;
    private TextView stayLabelTextView, busTextView;
    private ProgressBar progressBar;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private static final String TAG = "EditFragment";
    private static final long MAX_DOWNLOAD_SIZE = 5 * 1024 * 1024;
    private String stayType;
    private boolean isImageUploaded = false;
    private static final String PREFS_NAME = "UserPref";
    private static final String KEY_ROLL_NO = "roll_no";

    public EditFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_editfrag, container, false);
        initializeUIElements(view);
        setupAdapters();
        setupCheckboxListeners();
        setupImagePicker();
        setupSubmitButton();

        return view;
    }

    /**
     * Initialize all UI components and Firebase references.
     *
     * @param view The root view of the fragment.
     */
    private void initializeUIElements(View view) {
        // Initialize Firebase Database and Storage
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference("Photos");

        // Initialize UI components
        progressBar = view.findViewById(R.id.progressBar2);
        deptView = view.findViewById(R.id.deptview);
        yearView = view.findViewById(R.id.yearview);
        hostelCheckBox = view.findViewById(R.id.Hostel);
        dayScholarCheckBox = view.findViewById(R.id.Dayscholar);
        outpassCheckBox = view.findViewById(R.id.outpass);
        photoImageView = view.findViewById(R.id.Photo);
        uploadButton = view.findViewById(R.id.button);
        submitButton = view.findViewById(R.id.overall);
        nameEditText = view.findViewById(R.id.Name);
        rollEditText = view.findViewById(R.id.Roll);
        dobEditText = view.findViewById(R.id.DOB);
        stayNoEditText = view.findViewById(R.id.stay_et);
        phoneEditText = view.findViewById(R.id.editTextPhone);
        addressEditText = view.findViewById(R.id.editTextTextPostalAddress);
        cgpaEditText = view.findViewById(R.id.cgpa);
        stayLabelTextView = view.findViewById(R.id.stay_no);
        busTextView = view.findViewById(R.id.bus);
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                toggleImageUploadVisibility();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                toggleImageUploadVisibility();
            }
        });
        if (!nameEditText.getText().toString().isEmpty()) {
            displayImageFromFirebase();
        }
    }

    /**
     * Setup adapters for department and year AutoCompleteTextViews.
     */
    private void setupAdapters() {
        String[] departments = {"CSE", "IT", "ECE", "CIVIL", "MECH", "AI&DS", "Other"};
        String[] years = {"I", "II", "III", "IV"};

        deptAdapter = new ArrayAdapter<>(requireContext(), R.layout.dept_list, departments);
        deptView.setAdapter(deptAdapter);

        yearAdapter = new ArrayAdapter<>(requireContext(), R.layout.dept_list, years);
        yearView.setAdapter(yearAdapter);
    }

    /**
     * Setup listeners for CheckBoxes to handle stay type selection.
     */
    @SuppressLint("SetTextI18n")
    private void setupCheckboxListeners() {
        hostelCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                dayScholarCheckBox.setChecked(false);
                outpassCheckBox.setChecked(false);
                stayType = "Hostel";
                stayLabelTextView.setText("Room no:");
                stayNoEditText.setHint("Enter room number");
                busTextView.setVisibility(View.GONE);
                stayNoEditText.setVisibility(View.VISIBLE);
            }
        });

        dayScholarCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                hostelCheckBox.setChecked(false);
                outpassCheckBox.setChecked(false);
                stayType = "Dayscholar";
                stayLabelTextView.setText("Bus no:");
                stayNoEditText.setHint("Enter Bus no");
                busTextView.setVisibility(View.GONE);
                stayNoEditText.setVisibility(View.VISIBLE);
            }
        });

        outpassCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                hostelCheckBox.setChecked(false);
                dayScholarCheckBox.setChecked(false);
                stayType = "Outpass";
                busTextView.setText("OUTPASS");
                busTextView.setVisibility(View.VISIBLE);
                stayLabelTextView.setText("Bus no:");
                stayNoEditText.setVisibility(View.INVISIBLE);
            } else {
                busTextView.setVisibility(View.GONE);
                stayNoEditText.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Setup the Image Picker using ActivityResultLauncher.
     */
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

        uploadButton.setOnClickListener(v -> chooseImage(imagePickerLauncher));
    }

    /**
     * Launch the image chooser intent.
     *
     * @param imagePickerLauncher The ActivityResultLauncher to handle the result.
     */
    private void chooseImage(ActivityResultLauncher<Intent> imagePickerLauncher) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    /**
     * Upload the selected image to Firebase Storage.
     *
     * @param imageUri The URI of the selected image.
     */
    private void uploadImageToFirebase(Uri imageUri) {
        String roll = rollEditText.getText().toString().trim();
        if (roll.isEmpty()) {
            Toast.makeText(getContext(), "Please enter Roll number before uploading image.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        uploadButton.setEnabled(false);

        StorageReference fileReference = storageReference.child(roll + ".jpg");
        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    isImageUploaded = true;
                    Toast.makeText(getContext(), "Image upload successful.", Toast.LENGTH_SHORT).show();
                    displayImageFromFirebase();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Image upload failed", e);
                })
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    uploadButton.setEnabled(true);
                });
    }

    /**
     * Display the uploaded image from Firebase Storage.
     */
    private void displayImageFromFirebase() {
        String roll = rollEditText.getText().toString().trim();
        if (roll.isEmpty()) {
            return;
        }

        StorageReference fileReference = storageReference.child(roll + ".jpg");
        File localFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "download_image.jpg");

        fileReference.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    if (bitmap != null) {
                        photoImageView.setImageBitmap(bitmap);
                    } else {
                        photoImageView.setImageResource(R.drawable.profile); // Default image
                        Toast.makeText(getContext(), "Failed to decode image.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    photoImageView.setImageResource(R.drawable.profile); // Default image
                    Toast.makeText(getContext(), "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to load image", e);
                });
    }

    /**
     * Setup the Submit Button to validate inputs and upload data to Firebase.
     */
    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                uploadData();
                saveRollNumberToSharedPreferences();
                navigateToHomeFragment();
            }
        });
    }

    /**
     * Validate all input fields before uploading data.
     *
     * @return True if all inputs are valid, false otherwise.
     */
    private boolean validateInputs() {
        String name = nameEditText.getText().toString().trim();
        String roll = rollEditText.getText().toString().trim();
        String dob = dobEditText.getText().toString().trim();
        String department = deptView.getText().toString().trim();
        String year = yearView.getText().toString().trim();
        String stayNo = stayNoEditText.getText().toString().trim();
        String cgpa = cgpaEditText.getText().toString().trim();

        if (name.isEmpty() || roll.isEmpty() || dob.isEmpty()
                || department.isEmpty() || year.isEmpty() || cgpa.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all the details.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!hostelCheckBox.isChecked() && !dayScholarCheckBox.isChecked() && !outpassCheckBox.isChecked()) {
            Toast.makeText(getContext(), "Please select Hostel, Dayscholar, or Outpass.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if ((hostelCheckBox.isChecked() || dayScholarCheckBox.isChecked()) && stayNo.isEmpty()) {
            String label = hostelCheckBox.isChecked() ? "Room no" : "Bus no";
            Toast.makeText(getContext(), "Please enter " + label + ".", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (outpassCheckBox.isChecked() && busTextView.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please enter Bus no.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isImageUploaded) {
            Toast.makeText(getContext(), "Please upload your image.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Upload user data to Firebase Realtime Database.
     */
    private void uploadData() {
        String roll = rollEditText.getText().toString().trim();

        if (roll.isEmpty()) {
            Toast.makeText(getContext(), "Roll number is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference studentRef = databaseReference.child("students").child(roll);

        studentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    Toast.makeText(getContext(), "Name was already inserted.", Toast.LENGTH_SHORT).show();
                } else {
                    String name = nameEditText.getText().toString().trim();
                    String dob = dobEditText.getText().toString().trim();
                    String department = deptView.getText().toString().trim();
                    String year = yearView.getText().toString().trim();
                    String phone = phoneEditText.getText().toString().trim();
                    String address = addressEditText.getText().toString().trim();
                    String cgpa = cgpaEditText.getText().toString().trim();
                    String stayNo = stayNoEditText.getText().toString().trim();

                    studentRef.child("Name").setValue(name);
                    studentRef.child("Roll").setValue(roll);
                    studentRef.child("DOB").setValue(dob);
                    studentRef.child("Department").setValue(department);
                    studentRef.child("Year").setValue(year);
                    studentRef.child("Phone").setValue(phone);
                    studentRef.child("Address").setValue(address);
                    studentRef.child("Stay").setValue(stayType);

                    if (hostelCheckBox.isChecked() || dayScholarCheckBox.isChecked()) {
                        studentRef.child("Stay_no").setValue(stayNo);
                    }

                    studentRef.child("CGPA").setValue(cgpa);

                    Toast.makeText(getContext(), "Data uploaded successfully.", Toast.LENGTH_SHORT).show();
                    saveRollNumberToSharedPreferences();
                    navigateToHomeFragment();
                }
            } else {
                Toast.makeText(getContext(), "Failed to check roll number existence.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Save the roll number to SharedPreferences for retrieval in HomeFragment.
     */
    private void saveRollNumberToSharedPreferences() {
        String roll = rollEditText.getText().toString().trim();
        if (!roll.isEmpty()) {
            SharedPreferences sharedPref = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(KEY_ROLL_NO, roll);
            editor.apply();
            Log.d(TAG, "Roll number saved to SharedPreferences: " + roll);
        }
    }

    /**
     * Navigate back to HomeFragment after successful data upload.
     */
    private void navigateToHomeFragment() {
        // Optionally, you can pass data back using FragmentResult if needed
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    /**
     * Toggle the visibility of the image upload button based on the Name field.
     */
    private void toggleImageUploadVisibility() {
        if (!nameEditText.getText().toString().trim().isEmpty()) {
            uploadButton.setVisibility(View.VISIBLE);
            busTextView.setVisibility(View.GONE);
        } else {
            uploadButton.setVisibility(View.GONE);
        }
    }

    /**
     * Display or hide the progress bar and upload button based on upload status.
     *
     * @param inProgress True to show progress bar, false to hide it.
     */
    private void toggleProgressBar(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            uploadButton.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            uploadButton.setEnabled(true);
        }
    }
}