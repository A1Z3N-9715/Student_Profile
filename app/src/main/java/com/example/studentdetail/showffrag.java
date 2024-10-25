package com.example.studentdetail;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class showffrag extends Fragment {

    private static final String TAG = "showffrag";
    private static final long MAX_DOWNLOAD_SIZE = 5 * 1024 * 1024; // 5MB

    private TextView name, roll, dob, dept, stay, year, stay_no, phone, address, cgpa, label, notice;
    private ImageView ivDisplayPhoto, Id;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    public showffrag() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_showffrag, container, false);

        // Initialize Firebase references
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference("Photos");

        // Initialize UI elements
        name = view.findViewById(R.id.textViewName);
        roll = view.findViewById(R.id.textViewRollNo);
        dob = view.findViewById(R.id.textViewDOB);
        dept = view.findViewById(R.id.textViewDepartment);
        stay = view.findViewById(R.id.textViewStay);
        year = view.findViewById(R.id.textViewYearOfStudy);
        stay_no = view.findViewById(R.id.textViewBusRoomNo);
        phone = view.findViewById(R.id.textViewPhoneNumber);
        address = view.findViewById(R.id.textViewAddress);
        cgpa = view.findViewById(R.id.textViewCGPA);
        label = view.findViewById(R.id.textViewBusRoomNoLabel);

        // Retrieve roll number from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        String rollNo = sharedPreferences.getString("roll no", null);

        // Load student data if roll number is found
        if (rollNo != null) {
            fetchStudentDetails(rollNo);
        } else {
            Toast.makeText(getContext(), "Roll number not found", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void fetchStudentDetails(String rollNo) {
        databaseReference.child("students").child(rollNo).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    String nameVal = snapshot.child("Name").getValue(String.class);
                    String department = snapshot.child("Department").getValue(String.class);
                    String stayType = snapshot.child("Stay").getValue(String.class);
                    String rollVal = snapshot.child("Roll No").getValue(String.class);
                    String dobVal = snapshot.child("DOB").getValue(String.class);
                    String yearVal = snapshot.child("Year").getValue(String.class);
                    String stayNoVal = snapshot.child("Stay_no").getValue(String.class);
                    String phoneVal = snapshot.child("Phone").getValue(String.class);
                    String addressVal = snapshot.child("Address").getValue(String.class);
                    String cgpaVal = snapshot.child("CGPA").getValue(String.class);

                    // Set text for TextViews
                    name.setText(nameVal != null ? nameVal : "");
                    roll.setText(rollVal != null ? rollVal : "");
                    dob.setText(dobVal != null ? dobVal : "");
                    dept.setText(department != null ? department : "");
                    stay.setText(stayType != null ? stayType : "");
                    year.setText(yearVal != null ? yearVal : "");
                    stay_no.setText(stayNoVal != null ? stayNoVal : "");
                    phone.setText(phoneVal != null ? phoneVal : "");
                    address.setText(addressVal != null ? addressVal : "");
                    cgpa.setText(cgpaVal != null ? cgpaVal : "");

                    if ("Dayscholar".equals(stayType)) {
                        label.setText("Bus No");
                    } else if ("Hostel".equals(stayType)) {
                        label.setText("Room No");
                    } else {
                        label.setText("Outpass");
                    }

                } else {
                    Toast.makeText(getContext(), "No details found for roll number", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Error fetching student details", task.getException());
            }
        });
    }




}
