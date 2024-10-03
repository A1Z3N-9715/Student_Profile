package com.example.studentdetail;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link homefrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class homefrag extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private TextView name, roll, dept, stay;
    private ImageView imageView;
    private String imagePath;

    public homefrag() {
        // Required empty public constructor
    }

    public static homefrag newInstance(String param1, String param2) {
        homefrag fragment = new homefrag();
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
        View view = inflater.inflate(R.layout.fragment_homefrag, container, false);

        name = view.findViewById(R.id.diplay_name);
        roll = view.findViewById(R.id.display_roll);
        dept = view.findViewById(R.id.display_dept);
        stay = view.findViewById(R.id.diplay_stay);
        imageView = view.findViewById(R.id.display_photo);

        // Set up the FragmentResultListener
        getParentFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, bundle) -> {
            String Namevalue = bundle.getString("name");
            String Rollvalue = bundle.getString("roll");
            String Deptvalue = bundle.getString("dept");
            String Stayvalue = bundle.getString("stay");
            imagePath = bundle.getString("image_path"); // Get the image path from the bundle


            name.setText(Namevalue);
            roll.setText(Rollvalue);
            dept.setText(Deptvalue);
            if (Stayvalue.equals("Hostel")) {
                stay.setText("H");} else if (Stayvalue.equals("Dayscholar")) {
                stay.setText("D");} else  {
                stay.setText("OP");

            }


            // Load image
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(bitmap);
        });

        return view;
    }
}
