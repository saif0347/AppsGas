package com.app.ahgas_a3ecc92;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.app.ahgas_a3ecc92.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProductsAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<com.app.ahgas_a3ecc92.ProductModel> mList;
    private LayoutInflater mLayoutInflater = null;

    public ProductsAdapter(Context context, ArrayList<com.app.ahgas_a3ecc92.ProductModel> list) {
        mContext = context;
        mList = list;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int pos) {
        return mList.get(pos);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        CompleteListViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = li.inflate(R.layout.product_item, null);
            viewHolder = new CompleteListViewHolder(v);
            v.setTag(viewHolder);
        } else {
            viewHolder = (CompleteListViewHolder) v.getTag();
        }

        com.app.ahgas_a3ecc92.ProductModel model = mList.get(position);

        // set views data here
        viewHolder.productName.setText(model.getName());

        return v;
    }

    static class CompleteListViewHolder {
        // declare views here
        @BindView(R.id.productName)
        TextView productName;
        public CompleteListViewHolder(View base) {
            //initialize views here
            ButterKnife.bind(this, base);
        }
    }
}
