package com.pechydev.iotproject01.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pechydev.iotproject01.Components;
import com.pechydev.iotproject01.MainActivity;
import com.pechydev.iotproject01.R;

import java.util.ArrayList;


public class Adapter_Device extends RecyclerView.Adapter<Adapter_Device.VersionViewHolder> {
    Context context;
    OnItemClickListener clickListener;
    ArrayList<String> device;
    String TAG = "AdapterDevice";

    public Adapter_Device(Context applicationContext, ArrayList<String> device) {
        this.context = applicationContext;
        this.device = device;
    }

    @Override
    public VersionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_device, viewGroup, false);
        VersionViewHolder viewHolder = new VersionViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final VersionViewHolder versionViewHolder, final int i) {
        versionViewHolder.cardlist_item.setVisibility(View.INVISIBLE);
        new Components().set_Chart(versionViewHolder.mChart);

        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference myDevice = mFirebaseDatabase.getReference().child(device.get(i));
        DatabaseReference myValue = mFirebaseDatabase.getReference().child(device.get(i)).child("value");

        myValue.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                versionViewHolder.txtHum.setText("Humidity : " + dataSnapshot.child("humidity").getValue().toString() + " %");
                versionViewHolder.txtTemp.setText("Temperature: " + dataSnapshot.child("temperature").getValue().toString() + " °");
                feedMultiple(versionViewHolder.mChart, dataSnapshot.child("humidity").getValue().toString(), dataSnapshot.child("temperature").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        myDevice.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                versionViewHolder.txtName.setText(dataSnapshot.child("name").getValue().toString());
                versionViewHolder.cardlist_item.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return device == null ? 0 : device.size();
    }


    class VersionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txtName, txtHum, txtTemp;
        CardView cardlist_item;
        LineChart mChart;
        public VersionViewHolder(View itemView) {
            super(itemView);
            txtName = (TextView) itemView.findViewById(R.id.txtName);
            txtHum = (TextView) itemView.findViewById(R.id.txtHum);
            txtTemp = (TextView) itemView.findViewById(R.id.txtTemp);
            cardlist_item = (CardView) itemView.findViewById(R.id.cardlist_item);
            mChart = (LineChart) itemView.findViewById(R.id.chart);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(v, getPosition());
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    private Thread thread;

    private void feedMultiple(final LineChart mChart, final String humidity, final String temperature) {

        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                addEntry2(mChart,humidity,temperature);
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                ((Activity)context).runOnUiThread(runnable);
            }
        });

        thread.start();
    }

    private void addEntry2(LineChart mChart,String mHumidity, String mTemperature) {
        LineData data = mChart.getData();
        float humidit = Float.parseFloat(mHumidity);
        float temperature = Float.parseFloat(mTemperature);
        int R1 = ((int) (Math.random() * 100) + 1);
        int R2 = ((int) (Math.random() * 100) + 1);


        if (data != null) {

            ILineDataSet set_h = data.getDataSetByIndex(0);
            ILineDataSet set_t = data.getDataSetByIndex(1);

            if (set_h == null) {
                set_h = new Components().createSet();
                set_t = new Components().createSet2();
                data.addDataSet(set_h);
                data.addDataSet(set_t);
            }


            data.addEntry(new Entry(set_h.getEntryCount(), humidit), 0);
            data.addEntry(new Entry(set_t.getEntryCount(), temperature), 1);
            data.notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(20);
            mChart.moveViewToX(data.getEntryCount());

        }
    }

}
