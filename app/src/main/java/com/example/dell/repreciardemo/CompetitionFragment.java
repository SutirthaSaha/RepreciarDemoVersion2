package com.example.dell.repreciardemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

import static android.app.Activity.RESULT_OK;


public class CompetitionFragment extends Fragment {

    TextView score;
    private int valueChange;
    CardView scanButton;

    public static final int REQUEST_CODE = 100;
    public static final int PERMISSION_REQUEST = 200;

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseAuth.AuthStateListener authStateListener;

    String[] QRCodes=new String[]{};
    private String val;

    public CompetitionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_competition, container, false);
        score=v.findViewById(R.id.score);
        scanButton=v.findViewById(R.id.scanButton);
        firebaseDatabase= FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference().child("Users");
        firebaseAuth= FirebaseAuth.getInstance();
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onScanBtnClick(view);
            }
        });
        return v;
    }
    @Override
    public void onStart() {
        super.onStart();
        String user_id=firebaseAuth.getCurrentUser().getUid();
        final DatabaseReference userdb=databaseReference.child(user_id);
        userdb.child("Score").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    int value =dataSnapshot.getValue(Integer.class);
                    score.setText("Your Score : "+String.valueOf(value));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        userdb.child("QRCodes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    val=dataSnapshot.getValue(String.class);
                    QRCodes=val.split("/");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            if(data != null){
                final Barcode barcode = data.getParcelableExtra("barcode");
                if(Arrays.asList(QRCodes).contains(barcode.displayValue)){
                    Toast.makeText(getContext(),"You have already scanned this QR Code",Toast.LENGTH_SHORT).show();
                }
                else{

                    final DatabaseReference qrdb= FirebaseDatabase.getInstance().getReference().child("QRCodes").child(barcode.displayValue);
                    qrdb.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            valueChange=dataSnapshot.getValue(Integer.class);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                    qrdb.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            if (mutableData.getValue() == null) {
                                mutableData.setValue(1);
                            } else {
                                if(valueChange<10)
                                    mutableData.setValue((Long) mutableData.getValue() + 1);
                                else
                                    Toast.makeText(getContext(),"QR Code has Expired",Toast.LENGTH_SHORT).show();
                            }
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        }
                    });
                    String user_id = firebaseAuth.getCurrentUser().getUid();
                    final DatabaseReference userdb = databaseReference.child(user_id);
                    userdb.child("Score").runTransaction(new Transaction.Handler() {

                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            if (mutableData.getValue() == null) {
                                mutableData.setValue(1);
                            } else {
                                if(valueChange<10)
                                    mutableData.setValue((Long) mutableData.getValue() + 1);
                            }
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        }
                    });

                    userdb.child("QRCodes").setValue(val+ barcode.displayValue+"/");

                }
            }
        }
    }

    public void onScanBtnClick(View view) {
        Intent intent = new Intent(getContext(), ScanActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }
}
