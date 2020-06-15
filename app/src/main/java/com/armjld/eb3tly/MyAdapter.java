package com.armjld.eb3tly;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import Model.Data;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

     Context context , context1;
     long count;
     Data [] filtersData;
     private FirebaseAuth mAuth = FirebaseAuth.getInstance();
     SwipeRefreshLayout mSwipeRefreshLayout;
     private ArrayList datalist,filterList;
     private DatabaseReference mDatabase,rDatabase,uDatabase,vDatabase;
     private ArrayList<String> mArraylistSectionLessons = new ArrayList<String>();
     private String TAG = "My Adapter";

     SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
     String datee = sdf.format(new Date());

    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
    String datee2 = sdf.format(new Date());

    public MyAdapter(SwipeRefreshLayout mSwipeRefreshLayout) {
        this.mSwipeRefreshLayout = mSwipeRefreshLayout;
    }

    public MyAdapter(Context context, Data[] filtersData, Context context1, long count, SwipeRefreshLayout mSwipeRefreshLayout) {
        this.count = count;
        this.context = context;
        this.filtersData = filtersData;
        this.context1 = context1;
        this.mSwipeRefreshLayout = mSwipeRefreshLayout;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Pickly").child("orders");
        uDatabase = FirebaseDatabase.getInstance().getReference().child("Pickly").child("users");
        rDatabase = FirebaseDatabase.getInstance().getReference().child("Pickly").child("comments");
        vDatabase = FirebaseDatabase.getInstance().getReference().child("Pickly").child("values");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view  = inflater.inflate(R.layout.item_data,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        // Get Post Date
        String startDate = filtersData[position].getDate();
        String stopDate = datee;
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date d1 = null;
        Date d2 = null;
        try {
            d1 = format.parse(startDate);
            d2 = format.parse(stopDate);
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        long diff = d2.getTime() - d1.getTime();
        long diffSeconds = diff / 1000;
        long diffMinutes = diff / (60 * 1000);
        long diffHours = diff / (60 * 60 * 1000);
        long diffDays = diff / (24 * 60 * 60 * 1000);

        int idiffSeconds = (int) diffSeconds;
        int idiffMinutes = (int) diffMinutes;
        int idiffHours = (int) diffHours;
        int idiffDays = (int) diffDays;

        holder.setDate(filtersData[position].getDDate());
        holder.setUsername(filtersData[position].getuId());
        holder.setOrdercash(filtersData[position].getGMoney());
        holder.setOrderFrom(filtersData[position].reStateP());
        holder.setOrderto(filtersData[position].reStateD());
        holder.setFee(filtersData[position].getGGet().toString());
        holder.setPostDate(idiffSeconds, idiffMinutes, idiffHours, idiffDays);
        holder.setType(filtersData[position].getIsCar(), filtersData[position].getIsMotor(), filtersData[position].getIsMetro(), filtersData[position].getIsTrans());

        //Hide this order Button
        holder.btnHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Still working on this", Toast.LENGTH_SHORT).show();
            }
        });

        final String PAddress =filtersData[position].getmPAddress();
        final String DAddress = filtersData[position].getDAddress();
        final String rateUID = filtersData[position].getuId();
        final String notes = filtersData[position].getNotes();

        //More Info Button
        holder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder myDialogMore = new AlertDialog.Builder(context);
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogMore = inflater.inflate(R.layout.dialogsupinfo, null);
                myDialogMore.setView(dialogMore);
                final AlertDialog dialog = myDialogMore.create();
                dialog.show();

                TextView tbTitle = dialogMore.findViewById(R.id.toolbar_title);
                tbTitle.setText("بيانات الاوردر");

                ImageView btnClose = dialogMore.findViewById(R.id.btnClose);

                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                final TextView dsUsername = dialogMore.findViewById(R.id.ddUsername);
                TextView dsPAddress = dialogMore.findViewById(R.id.ddPhone);
                TextView dsDAddress = dialogMore.findViewById(R.id.dsDAddress);
                TextView dsOrderNotes = dialogMore.findViewById(R.id.dsOrderNotes);
                final ImageView supPP = dialogMore.findViewById(R.id.supPP);
                final RatingBar rbUser = dialogMore.findViewById(R.id.ddRate);
                final TextView ddCount = dialogMore.findViewById(R.id.ddCount);

                // Get posted orders count
                int pos = position;
                mDatabase.orderByChild("uId").equalTo(filtersData[pos].getuId()).addListenerForSingleValueEvent (new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            int count = (int) dataSnapshot.getChildrenCount();
                            String strCount = String.valueOf(count);
                            ddCount.setText( "اضاف "+ strCount + " اوردر");
                        } else {
                            ddCount.setText("لم يقم بأضافه اي اوردرات");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                //Get the user name & Pic
                uDatabase.child(rateUID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String dsUser = snapshot.child("name").getValue().toString();
                        String dsPP = snapshot.child("ppURL").getValue().toString();

                        Log.i(TAG, "Photo " + dsPP);
                        if (!dsPP.equals("none") && dsPP != null) {
                            Picasso.get().load(Uri.parse(dsPP)).into(supPP);
                        }
                        dsUsername.setText(dsUser);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                if (PAddress.trim().equals("")) {
                    dsPAddress.setVisibility(View.GONE);
                } else {
                    dsPAddress.setVisibility(View.VISIBLE);
                    dsPAddress.setText("عنوان الاستلام : " + PAddress);
                }
                if (DAddress.trim().equals("")) {
                    dsDAddress.setVisibility(View.GONE);
                } else {
                    dsDAddress.setText("عنوان التسليم : " + DAddress);
                    dsDAddress.setVisibility(View.VISIBLE);
                }
                if(notes.trim().equals("")) {
                    dsOrderNotes.setVisibility(View.GONE);
                } else {
                    dsOrderNotes.setText(notes);
                    dsOrderNotes.setVisibility(View.VISIBLE);
                }

                //Get the Rate Stars
                rDatabase.child(rateUID).orderByChild("sId").equalTo(rateUID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long total = 0;
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            long rating = (long) Double.parseDouble(ds.child("rate").getValue().toString());
                            total = total + rating;
                        }
                        double average = (double) total / dataSnapshot.getChildrenCount();
                        rbUser.setRating((int) average);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                // Get that user Comments
                ListView listComment = dialogMore.findViewById(R.id.dsComment);
                final ArrayAdapter<String> arrayAdapterLessons = new ArrayAdapter<String>(context, R.layout.list_white_text, R.id.txtItem, mArraylistSectionLessons);
                listComment.setAdapter(arrayAdapterLessons);
                mArraylistSectionLessons.clear(); // To not dublicate comments
                rDatabase.child(rateUID).orderByChild("sId").equalTo(rateUID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            String tempComment = data.child("comment").getValue().toString();
                            if(!tempComment.equals("")) { // make sure that there is a comment
                                mArraylistSectionLessons.add(tempComment);
                            }
                            arrayAdapterLessons.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });

        //Accept Order Button
        final String orderID = filtersData[position].getId();
        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("accepting").getValue().toString().equals("false")) {
                            Toast.makeText(context, "لا يمكن قبول اي اوردرات الان حاول بعد قليل", Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            uDatabase.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    int cancelledCount =  Integer.parseInt(dataSnapshot.child("canceled").getValue().toString());
                                    if(cancelledCount >= 3) { // Number of allowed canceled orders
                                        Toast.makeText(context, "لقد الغيت 3 اوردرات هذا الشهر, لا يمكنك قبول اي اوردرات اخري حتي الاسبوع القادم", Toast.LENGTH_SHORT).show();
                                    } else {
                                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which){
                                                    case DialogInterface.BUTTON_POSITIVE:
                                                        mDatabase.child(orderID).child("statue").setValue("accepted");
                                                        mDatabase.child(orderID).child("uAccepted").setValue(mAuth.getCurrentUser().getUid());
                                                        mDatabase.child(orderID).child("acceptedTime").setValue(datee);
                                                        Toast.makeText(context, "تم قبول الاوردر", Toast.LENGTH_SHORT).show();
                                                        context.startActivity(new Intent(context, profile.class));
                                                        break;
                                                    case DialogInterface.BUTTON_NEGATIVE:
                                                        break;
                                                }
                                            }
                                        };
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        builder.setMessage("هل انت متاكد من انك تريد استلام الاوردر ؟").setPositiveButton("نعم", dialogClickListener).setNegativeButton("لا", dialogClickListener).show();
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });


    }

    @Override
    public int getItemCount() {
        int Count = (int) count;
        return Count;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        View myview;
        Button btnAccept, btnHide, btnMore ;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myview=itemView;
            btnAccept = myview.findViewById(R.id.btnAccept);
            btnHide = myview.findViewById(R.id.btnHide);
            btnMore = myview.findViewById(R.id.btnMore);
        }

        void setUsername(String userID){
            uDatabase.child(userID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String mName = snapshot.child("name").getValue().toString();
                    TextView mtitle = myview.findViewById(R.id.txtUsername);
                    mtitle.setText(mName);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        public void setOrderFrom(String orderFrom){
            TextView mtitle=myview.findViewById(R.id.OrderFrom);
            mtitle.setText(orderFrom);
        }
        public void setOrderto(String orderto){
            TextView mtitle=myview.findViewById(R.id.orderto);
            mtitle.setText(orderto);
        }

        public void setDate (String date){
            TextView mdate= myview.findViewById(R.id.date);
            mdate.setText(date);
        }
        public void setOrdercash(String ordercash){
            TextView mtitle=myview.findViewById(R.id.ordercash);
            mtitle.setText(ordercash + " ج");
        }
        public void setFee(String fees) {
            TextView mtitle=myview.findViewById(R.id.fees);
            mtitle.setText(fees + " ج");
        }

        public void setType(String car, String motor, String metro, String trans) {
            ImageView icnCar = myview.findViewById(R.id.icnCar);
            ImageView icnMotor = myview.findViewById(R.id.icnMotor);
            ImageView icnMetro = myview.findViewById(R.id.icnMetro);
            ImageView icnTrans = myview.findViewById(R.id.icnTrans);
            if (car.equals("سياره")) {
                icnCar.setVisibility(View.VISIBLE);
            } else {
                icnCar.setVisibility(View.INVISIBLE);
            }

            if(motor.equals("موتسكل")) {
                icnMotor.setVisibility(View.VISIBLE);
            } else {
                icnMotor.setVisibility(View.INVISIBLE);
            }

            if(metro.equals("مترو")) {
                icnMetro.setVisibility(View.VISIBLE);
            } else {
                icnMetro.setVisibility(View.INVISIBLE);
            }

            if (trans.equals("مواصلات")) {
                icnTrans.setVisibility(View.VISIBLE);
            } else {
                icnTrans.setVisibility(View.INVISIBLE);
            }
        }

        public void setPostDate(int dS, int dM, int dH, int dD) {
            String finalDate = "";
            TextView mtitle = myview.findViewById(R.id.txtPostDate);
            if (dS < 60) {
                finalDate = "منذ " + String.valueOf(dS) + " ثوان";
            } else if (dS > 60 && dS < 3600) {
                finalDate = "منذ " + String.valueOf(dM) + " دقيقة";
            } else if (dS > 3600 && dS < 86400) {
                finalDate = "منذ " + String.valueOf(dH) + " ساعات";
            } else if (dS > 86400) {
                finalDate = "منذ " + String.valueOf(dD) + " ايام";
            }
            mtitle.setText(finalDate);
        }
    }

    private void refresh() {
        new Handler() {
            public void postDelayed(Runnable runnable, int i) {
            }

            @Override
            public void publish(LogRecord record) {

            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }.postDelayed(new Runnable() {

            @Override
            public void run() {
                MyAdapter.this.notifyDataSetChanged();
                Log.i(TAG, "Data Refreshed");

            }
        },3000);
        MyAdapter.this.notifyDataSetChanged();
        Log.i(TAG, "Data Refreshed");
        mSwipeRefreshLayout.setRefreshing(false);
    }
}