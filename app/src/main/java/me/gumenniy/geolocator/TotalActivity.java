package me.gumenniy.geolocator;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import me.gumenniy.geolocator.adapter.MyAdapter;
import me.gumenniy.geolocator.db.SQLiteHelper;
import me.gumenniy.geolocator.pojo.DaySet;
import me.gumenniy.geolocator.pojo.TagImage;

public class TotalActivity extends AppCompatActivity implements MyAdapter.ItemClickListener {

    private static final int REQUEST_CODE = 1010;
    public static final String DATE = "date";
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private List<DaySet> mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TotalActivity.this, TagActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mData = new ArrayList<>();
        mAdapter = new MyAdapter(mData);
        mAdapter.setListener(this);

        mRecyclerView.setAdapter(mAdapter);

        updateView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            updateView();
        }
    }

    @Override
    public void onClick(int position) {
        Log.e("TotalActivity", "onClick()");
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(DATE, mData.get(position).getDate());
        startActivity(intent);
    }

    private void updateView() {
        Log.e("TotalActivity", "updateView()");
        new DaySetLoadTask().execute(this);
    }

    class DaySetLoadTask extends AsyncTask<Context, Void, List<DaySet>> {

        @Override
        protected List<DaySet> doInBackground(Context... params) {
            SQLiteHelper helper = SQLiteHelper.getInstance(params[0]);

            List<TagImage> images = helper.getImages(params[0]);
            return DaySet.imagesToSet(images);
        }

        @Override
        protected void onPostExecute(List<DaySet> daySets) {
            mData = daySets;
            Log.e("TotalActivity", "onPostExecute() " + daySets.size());
            mAdapter.setData(mData);
            mAdapter.notifyDataSetChanged();
        }
    }
}
