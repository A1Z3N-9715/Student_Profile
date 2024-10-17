package com.example.studentdetail;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.studentdetail.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ChangeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private Button btnChange;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private String rollNo; // Variable to hold the roll number

    public ChangeFragment() {
    }

    public static ChangeFragment newInstance(String param1, String param2) {
        ChangeFragment fragment = new ChangeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change, container, false);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference("Photos"); // Reference to the storage
        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        rollNo = sharedPref.getString("roll_no", null);
        btnChange = view.findViewById(R.id.button2);
        btnChange.setOnClickListener(v -> {
            if (rollNo != null) {
                deleteStudentDetails(rollNo);
                deleteStudentPhoto(rollNo); // Call method to delete photo
                Toast.makeText(getContext(), "Student details and photo deleted.", Toast.LENGTH_SHORT).show();
                EditFragment editFragment = new EditFragment();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, editFragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                Toast.makeText(getContext(), "No roll number found.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void deleteStudentDetails(String rollNo) {
        databaseReference.child("students").child(rollNo).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Student details deleted successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to delete student details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteStudentPhoto(String rollNo) {
        StorageReference photoRef = storageReference.child(rollNo + ".jpg");

        photoRef.delete().addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Photo deleted successfully.", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to delete photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
