package com.example.dell.repreciardemo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {


    private EditText signUpName, signUpEmail, signUpPassword,signUpContact,signUpRoll;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    RadioGroup section,food;
    RadioButton sectionRb,foodRb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        signUpName = findViewById(R.id.signUpName);
        signUpEmail = findViewById(R.id.signUpEmail);
        signUpPassword = findViewById(R.id.signUpPassword);
        signUpContact=findViewById(R.id.signUpContact);
        signUpRoll=findViewById(R.id.signUpRoll);
        section=findViewById(R.id.section);
        food=findViewById(R.id.food);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    public void onRegisterBtnClick(View view) {

        final String name = signUpName.getText().toString().trim();
        final String email = signUpEmail.getText().toString().trim();
        final String password = signUpPassword.getText().toString().trim();
        final String roll = signUpRoll.getText().toString().trim();
        final String contact = signUpContact.getText().toString().trim();


        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(roll) && !TextUtils.isEmpty(contact) && section.getCheckedRadioButtonId() != -1 && food.getCheckedRadioButtonId() != -1) {

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this,"Successful",Toast.LENGTH_SHORT).show();
                        String user_id = firebaseAuth.getCurrentUser().getUid();
                        DatabaseReference currentUserDb = databaseReference.child(user_id);
                        sectionRb=findViewById(section.getCheckedRadioButtonId());
                        foodRb=findViewById(food.getCheckedRadioButtonId());
                        currentUserDb.child("Name").setValue(name);
                        currentUserDb.child("Roll").setValue(roll);
                        currentUserDb.child("Contact").setValue(contact);
                        currentUserDb.child("Section").setValue(sectionRb.getText().toString().trim());
                        currentUserDb.child("Food").setValue(foodRb.getText().toString().trim());
                        currentUserDb.child("QRCodes").setValue("");
                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    }
                    else{
                        Toast.makeText(RegisterActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        signUpName.setText(task.getException().getMessage());
                    }
                }
            });
        }
    }



}
