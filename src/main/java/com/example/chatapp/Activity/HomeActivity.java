package com.example.chatapp.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Adapter.UserAdapter;
import com.example.chatapp.ModelClass.Users;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    FirebaseAuth auth;
    RecyclerView mainUserRecyclerView;
    UserAdapter adapter;
    FirebaseDatabase database;
    ArrayList<Users> usersArrayList;
    ImageView imgLogout;
    ImageView imgSetting;
    private boolean doubleBackToExitPressedOnce = false;

    private FirebaseRemoteConfig remoteConfig;
    private android.content.pm.PackageInfo PackageInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        usersArrayList = new ArrayList<>();

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(HomeActivity.this, RegistrationActivity.class));
        }

        Toast.makeText(this, "Data is Fetching...", Toast.LENGTH_SHORT).show();

        DatabaseReference reference = database.getReference().child("user");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    usersArrayList.add(users);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        imgLogout = findViewById(R.id.img_logOut);
        imgSetting = findViewById(R.id.img_Setting);

        mainUserRecyclerView = findViewById(R.id.mainUserRecyclerView);
        mainUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(HomeActivity.this, usersArrayList);
        mainUserRecyclerView.setAdapter(adapter);

        imgLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(HomeActivity.this);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                dialog.setContentView(R.layout.dialog_layout);
                TextView yesBtn, noBtn;

                yesBtn = dialog.findViewById(R.id.yesBtn);
                noBtn = dialog.findViewById(R.id.noBtn);

                yesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(HomeActivity.this, RegistrationActivity.class));
                    }
                });

                noBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

            }
        });

        imgSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, SettingActivity.class));
            }
        });

        TextView currentVersion = findViewById(R.id.update_box);
        currentVersion.setText("Current Version:" + getVersionCode());
        HashMap<String, Object> defaultsRate = new HashMap<>();
        defaultsRate.put("New Version", String.valueOf(getVersionCode()));

        remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setFetchTimeoutInSeconds(10)
                .build();

        remoteConfig.setConfigSettingsAsync(configSettings);
        remoteConfig.setDefaultsAsync(defaultsRate);

        remoteConfig.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful()) {
                    final String new_version_code = remoteConfig.getString("new_version_code");
                    if (Integer.parseInt(new_version_code) > getVersionCode())
                        showDialog(new_version_code);
                }
            }
        });

    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        doubleBackToExitPressedOnce = true;

    }

    private void showDialog(String VersionFromRemoteConfig) {
        final AlertDialog dialog=new AlertDialog.Builder(this)
                .setTitle("New Update Available")
                .setMessage("Previous Version:1\n Latest Version:" + VersionFromRemoteConfig)
                .setPositiveButton("UPDATE",null)
                .show();
        dialog.setCancelable(false);

        Button positiveButton=dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://drive.google.com/drive/folders/1ND6eCM3kYJGOd-Le2BCIXpG9NL3LNwPH?usp=sharing\"+\"com.example.chatapp")));
                }catch (android.content.ActivityNotFoundException anfe){
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("\"https://drive.google.com/drive/folders/1ND6eCM3kYJGOd-Le2BCIXpG9NL3LNwPH?usp=sharing\\\"+\\\"com.example.chatapp\"")));
                }
            }
        });
    }





    private int getVersionCode() {
        PackageInfo packageInfo=null;
        try {
            PackageInfo=getPackageManager().getPackageInfo(getPackageName(),0);
        }catch (PackageManager.NameNotFoundException e){
            Log.i("MyLog","NameNotFoundException: "+e.getMessage());
        }
        return Objects.requireNonNull(packageInfo).versionCode;
    }
}