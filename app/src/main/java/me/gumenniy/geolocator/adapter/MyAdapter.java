package me.gumenniy.geolocator.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import me.gumenniy.geolocator.R;
import me.gumenniy.geolocator.pojo.DaySet;

/**
 * Created by Arkadiy on 16.01.2016.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<DaySet> mDataset;

    public void setListener(ItemClickListener listener) {
        this.listener = listener;
    }

    private ItemClickListener listener;

    public MyAdapter(List<DaySet> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mDateView.setText(mDataset.get(position).getDate());
        holder.mDistanceView.setText(String.format("%.2f m", mDataset.get(position).getDistance()));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClick(position);
                }
            }
        });
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        holder.itemView.setOnClickListener(null);
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void setData(List<DaySet> data) {
        this.mDataset = data;
    }

    public interface ItemClickListener {
        void onClick(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public TextView mDateView;
        public TextView mDistanceView;

        public ViewHolder(View v) {
            super(v);
            mView = v;
            mDateView = (TextView) mView.findViewById(R.id.date);
            mDistanceView = (TextView) mView.findViewById(R.id.distance);
        }
    }
}
