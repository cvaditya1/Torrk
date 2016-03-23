package com.example.ave.torrk;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by AVE on 3/21/2016.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyHolder>{

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private int visibleThreshold = 2;
    private int totalItemCount, lastVisibleItem;
    private boolean loaded = false;

    private ArrayList<String> dataSet;
    private MyClickListener mMyClickListener;
    private onLoadMoreListener monLoadMoreListener;

    public MyAdapter(RecyclerView recyclerView, ArrayList<String> data){
        mRecyclerView = recyclerView;
        dataSet = data;
        if(mRecyclerView.getLayoutManager() instanceof LinearLayoutManager){
            mLinearLayoutManager = (LinearLayoutManager)mRecyclerView.getLayoutManager();
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = mLinearLayoutManager.getItemCount();
                    lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();
                    if(!loaded && (totalItemCount <= lastVisibleItem + visibleThreshold)){
                        loaded = true;
                        if(monLoadMoreListener != null){
                            monLoadMoreListener.onLoadMore();
                        }

                    }
                }
            });
        }
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_view, parent, false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        //holder.image_View.setImageResource(R.drawable.logo);
        String[] link_Content = dataSet.get(position).split(";");
        if(link_Content.length > 0) {
            holder.name_View.setText(link_Content[0]);
            if(link_Content[1] != null){
                holder.name_View.setTag(link_Content[1]);
            }
        }
        holder.details_View.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void setLoaded(boolean status){
        loaded = status;
    }

    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        //private ImageView image_View;
        private TextView name_View, details_View;

        public MyHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            //image_View = (ImageView)itemView.findViewById(R.id.imageView);
            name_View = (TextView)itemView.findViewById(R.id.name);
            details_View = (TextView)itemView.findViewById(R.id.details);
        }

        @Override
        public void onClick(View view) {
            if(mMyClickListener != null)mMyClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public void setOnClickListener(MyClickListener listener){
        mMyClickListener = listener;
    }

    public interface MyClickListener{
        public void onItemClick(View v, int position);
    }

    public void setOnLoadMoreDataListener(onLoadMoreListener listener){
        monLoadMoreListener = listener;
    }

    public interface onLoadMoreListener{
        public void onLoadMore();
    }

    public static class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {
        private Drawable mDivider;

        public SimpleDividerItemDecoration(Context con){
            mDivider = con.getResources().getDrawable(R.drawable.line_divider);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDrawOver(c, parent, state);
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }
}
