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

public class HomeFragment extends Fragment {

    private TextView tvDisplayName, tvDisplayRoll, tvDisplayDept, tvDisplayStay, slash, notice;
    private ImageView ivDisplayPhoto, Id;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private static final String TAG = "HomeFragment";
    private static final long MAX_DOWNLOAD_SIZE = 5 * 1024 * 1024; // 5MB

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_homefrag, container, false);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference("Photos");
        ivDisplayPhoto = view.findViewById(R.id.display_photo);
        tvDisplayName = view.findViewById(R.id.diplay_name);
        tvDisplayRoll = view.findViewById(R.id.display_roll);
        tvDisplayDept = view.findViewById(R.id.display_dept);
        tvDisplayStay = view.findViewById(R.id.diplay_stay);
        Id = view.findViewById(R.id.imageView2);
        slash = view.findViewById(R.id.textView13);
        notice = view.findViewById(R.id.notice);
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        String rollNo = sharedPref.getString("roll no", null);
        if (rollNo != null) {
            fetchStudentDetails(rollNo);
        } else {
            clearTextViews();
            Toast.makeText(getContext(), "Roll number not found", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void fetchStudentDetails(String rollNo) {
        databaseReference.child("students").child(rollNo).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    String name = snapshot.child("Name").getValue(String.class);
                    String department = snapshot.child("Department").getValue(String.class);
                    String stay = snapshot.child("Stay").getValue(String.class);
                    String roll = snapshot.child("Roll No").getValue(String.class);
                    tvDisplayName.setText(name != null ? name : "");
                    tvDisplayRoll.setText(roll != null ? roll : "");
                    tvDisplayDept.setText(department != null ? department : "");
                    if (stay == null) {
                        tvDisplayStay.setText("");
                    } else if (stay.equals("Dayscholar")) {
                        tvDisplayStay.setText("D");
                    } else if (stay.equals("Outpass")) {
                        tvDisplayStay.setText("OP");
                    } else {
                        tvDisplayStay.setText("H");
                    }
                    loadStudentPhoto(rollNo);
                    if(tvDisplayName.getText().toString().isEmpty()){
                        clearTextViews();
                    }
                } else {
                    clearTextViews();
                    Toast.makeText(getContext(), "No details found for roll number", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Error fetching student details", task.getException());
                clearTextViews();
            }
        });
    }

    private void loadStudentPhoto(String rollNo) {
        StorageReference photoRef = storageReference.child(rollNo + ".jpg");
        photoRef.getBytes(MAX_DOWNLOAD_SIZE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap != null) {
                ivDisplayPhoto.setImageBitmap(bitmap);
            } else {
                ivDisplayPhoto.setVisibility(View.GONE); // Default image if download fails
            }
        }).addOnFailureListener(e -> {
            ivDisplayPhoto.setVisibility(View.GONE);
            Log.e(TAG, "Error loading student photo", e);
        });
    }

    private void clearTextViews() {
        tvDisplayName.setVisibility(View.GONE);
        tvDisplayRoll.setVisibility(View.GONE);
        tvDisplayDept.setVisibility(View.GONE);
        tvDisplayStay.setVisibility(View.GONE);
        Id.setVisibility(View.GONE);
        slash.setVisibility(View.GONE);
        ivDisplayPhoto.setVisibility(View.GONE);
        notice.setVisibility(View.VISIBLE);
    }
}
