package com.armjld.eb3tly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.armjld.eb3tly.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Timer;
import java.util.TimerTask;

public class StartUp extends AppCompatActivity {

    private FirebaseAuth mAuth;

    public void onBackPressed() { }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_startup);

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            mAuth=FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            DatabaseReference uDatabase = FirebaseDatabase.getInstance().getReference().child("Pickly").child("users");
            uDatabase.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String isComplete = snapshot.child("completed").getValue().toString();
                    if(isComplete.equals("true")) {
                        String isActive = snapshot.child("active").getValue().toString();
                        if(isActive.equals("true")) {
                            String uType = snapshot.child("accountType").getValue().toString();
                            switch (uType) {
                                case "Supplier":
                                    startActivity(new Intent(StartUp.this, profile.class));
                                    break;
                                case "Delivery Worker":
                                    startActivity(new Intent(StartUp.this, HomeActivity.class));
                                    break;
                                case "Admin":
                                    startActivity(new Intent(StartUp.this, Admin.class));
                                    break;
                            }
                        } else {
                            Toast.makeText(StartUp.this, "تم تعطيل حسابك بسبب مشاكل مع المستخدمين", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        }
                    } else if (isComplete.equals("false")) {
                        Toast.makeText(StartUp.this, "الرجاء اكمال بيانات التسجيل", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(StartUp.this, Signup.class));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    startActivity(new Intent(StartUp.this, MainActivity.class));
                }
            }, 2500);
        }
    }
}
