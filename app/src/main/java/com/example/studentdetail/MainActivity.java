package com.example.studentdetail;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    boolean ispressed = false;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setupOnBackPressedCallback();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        databaseReference = FirebaseDatabase.getInstance().getReference("students");
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.home); // Default selection
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                break;

            case R.id.Show:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new showffrag()).commit();
                break;

            case R.id.edit:
                // Check roll number in Firebase and navigate accordingly
                checkNameForEdit();
                break;

            case R.id.about:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new aboutfrag()).commit();
                break;

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, log_in.class));
                finish();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupOnBackPressedCallback() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (ispressed) {
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Press back again to exit", Toast.LENGTH_SHORT).show();
                    ispressed = true;
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void checkNameForEdit() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        String rollNo = sharedPreferences.getString("roll no", "");
        if (rollNo != null) {
            // Query the database for the name associated with the roll number
            databaseReference.orderByChild("Roll No").equalTo(rollNo)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot rollSnapshot) {
                            if (rollSnapshot.exists()) {
                                for (DataSnapshot snapshot : rollSnapshot.getChildren()) {

                                    String name = snapshot.child("Name").getValue(String.class);
                                    if (name != null) {
                                        // If a name exists for the roll number, navigate to ChangeFragment
                                        getSupportFragmentManager().beginTransaction()
                                                .replace(R.id.fragment_container, new ChangeFragment())
                                                .commit();
                                        return; // Exit the loop after the transaction
                                    }
                                }
                            }
                            // If no name exists for the roll number, navigate to EditFragment
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new EditFragment())
                                    .commit();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(MainActivity.this, "Error checking roll number", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }




}
