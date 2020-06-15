package com.armjld.eb3tly;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import com.armjld.eb3tly.R;
import com.armjld.eb3tly.Signup;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.google.firebase.database.FirebaseDatabase.getInstance;

public class code22 extends AppCompatActivity {

    private String mVerificationId;
    private FirebaseAuth mAuth;
    private EditText editTextMobile, editTextCode;
    private Button btnConfirmCode, btnConfirmPhone;
    private TextView txtViewPhone, btnReType;
    private ConstraintLayout linerVerf, linerPhone;
    private String TAG = "Phone Auth";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code22);

        mAuth = FirebaseAuth.getInstance();
        editTextCode = findViewById(R.id.txtVerfCode);
        editTextMobile = findViewById(R.id.txtPhoneNumb);
        btnConfirmCode = findViewById(R.id.btnConfirmCode);
        btnConfirmPhone = findViewById(R.id.btnConfirmPhone);
        txtViewPhone = findViewById(R.id.txtViewPhone);
        btnReType = findViewById(R.id.btnReType);

        linerVerf = findViewById(R.id.linerVerf);
        linerPhone = findViewById(R.id.linerPhone);

        linerVerf.setVisibility(View.GONE);

        txtViewPhone.setText("ادخل رقم الهاتف");

        btnConfirmPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobile = editTextMobile.getText().toString().trim();
                if(mobile.length() != 11){
                    editTextMobile.setError("ادخل رقم هاتف صحيح");
                    editTextMobile.requestFocus();
                    return;
                } else {
                    final String getMobile = mobile;
                    FirebaseDatabase.getInstance().getReference().child("Pickly").child("users")
                            .orderByChild("phone").equalTo(mobile).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                if (snapshot.getValue() != null) {
                                    Toast.makeText(code22.this, "رقم الهاتف بالفعل مسجل", Toast.LENGTH_SHORT).show();
                                } else {
                                    txtViewPhone.setText("ضع الرمز المرسل اليك");
                                    linerPhone.setVisibility(View.GONE);
                                    linerVerf.setVisibility(View.VISIBLE);
                                    Toast.makeText(code22.this, "تم ارسال الكود", Toast.LENGTH_SHORT).show();
                                    sendVerificationCode(getMobile);
                                }
                            } else {
                                txtViewPhone.setText("ضع الرمز المرسل اليك");
                                linerPhone.setVisibility(View.GONE);
                                linerVerf.setVisibility(View.VISIBLE);
                                Toast.makeText(code22.this, "تم ارسال الكود", Toast.LENGTH_SHORT).show();
                                sendVerificationCode(getMobile);
                            }

                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }});
                }
            }});

        btnConfirmCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editTextCode.getText().toString().trim();
                if (code.length() != 6) {
                    editTextCode.setError("ادخل كود صحيح");
                    editTextCode.requestFocus();
                    return;
                } else {
                    verifyVerificationCode(code);
                }
            }
        });

        btnReType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextCode.setText("");
                linerVerf.setVisibility(View.GONE);
                linerPhone.setVisibility(View.VISIBLE);
                txtViewPhone.setText("ادخل رقم الهاتف");
            }
        });
    }

    //the method is sending verification code
    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+2" + mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
        Log.i(TAG, "Send Verfication Code fun to : +2" + mobile);
    }


    //the callback to detect the verification status
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            Log.i(TAG, "Message Detected " + code);
            if (code != null) {
                editTextCode.setText(code);
                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(code22.this, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.i(TAG, "Failed to verfiy");
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            Log.i(TAG, "onCodeSent : " + s);
            mVerificationId = s;
        }
    };


    private void verifyVerificationCode(String code) {
        //-----------------------------------------------
        Log.i(TAG, "Verfied");
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Log.i(TAG, "Signed Up via phone");
        mAuth.signInWithCredential(credential).addOnCompleteListener(code22.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    DatabaseReference uDatabase = getInstance().getReference().child("Pickly").child("users");
                    uDatabase.child(mAuth.getCurrentUser().getUid()).child("completed").setValue("false");
                    uDatabase.child(mAuth.getCurrentUser().getUid()).child("id").setValue(mAuth.getCurrentUser().getUid());
                    uDatabase.child(mAuth.getCurrentUser().getUid()).child("ppURL").setValue("none");
                    Toast.makeText(code22.this, "تم تاكيد رقم الهاتف الرجاء استكمال البيانات", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(code22.this, Signup.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    String message = "Somthing is wrong, we will fix it soon...";
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        message = "Invalid code entered...";
                    }
                    Toast.makeText(code22.this, message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}