package com.app.copagaz;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.app.copagaz.util.LogUtil;
import com.app.copagaz.util.TimeUtil;
import com.app.copagaz.volley.CustomRequest;
import com.app.copagaz.volley.VolleyLibrary;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VendorsAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<VendorModel> mList;
    private HashMap<Integer, RatingModel> ratingMap = new HashMap<>();
    private HashMap<String, String> statusMap = new HashMap<>();
    private LayoutInflater mLayoutInflater = null;

    public VendorsAdapter(Context context, ArrayList<VendorModel> list) {
        mContext = context;
        mList = list;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public ArrayList<VendorModel> getData() {
        return mList;
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
            v = li.inflate(R.layout.vendor_item, null);
            viewHolder = new CompleteListViewHolder(v);
            v.setTag(viewHolder);
        } else {
            viewHolder = (CompleteListViewHolder) v.getTag();
        }

        VendorModel model = mList.get(position);
        viewHolder.vendorName.setText(model.getPartner_name());
        viewHolder.price.setText("R$"+model.getProduct_special_price());
        Glide.with(mContext).load(model.getProduct_image_url()).into(viewHolder.image);

        getVendorRating(model.getPartner_hash_id(), position);
        final RatingModel ratingModel = ratingMap.get(position);
        if(ratingModel != null) {
            viewHolder.rating.setRating((float) model.getRating());
        }
        else{
            viewHolder.rating.setRating(0);
        }

        getTimes(
                TimeUtil.getSdf("EEEE").format(System.currentTimeMillis()).toLowerCase(),
                model.getPartner_hash_id()
        );
        String status = statusMap.get(model.getPartner_hash_id());
        if(status == null){
            status = "0";
        }

        if(status.equals("0")){
            viewHolder.open.setVisibility(View.GONE);
            viewHolder.close.setVisibility(View.GONE);
        }
        else if(status.equals("1")){
            viewHolder.open.setVisibility(View.VISIBLE);
            viewHolder.close.setVisibility(View.GONE);
        }
        else if(status.equals("2")){
            viewHolder.open.setVisibility(View.GONE);
            viewHolder.close.setVisibility(View.VISIBLE);
        }

        return v;
    }

    static class CompleteListViewHolder {
        // declare views here
        @BindView(R.id.image)
        ImageView image;
        @BindView(R.id.vendorName)
        TextView vendorName;
        @BindView(R.id.rating)
        RatingBar rating;
        @BindView(R.id.price)
        TextView price;
        @BindView(R.id.open)
        TextView open;
        @BindView(R.id.close)
        TextView close;
        public CompleteListViewHolder(View base) {
            //initialize views here
            ButterKnife.bind(this, base);
        }
    }

    public void getVendorRating(final String hashId, final int position) {
        if(ratingMap.get(position) != null)
            return;
        HashMap<String, String> map = new HashMap<>();
        String url = MainActivity.baseUrl+"/partner/"+hashId+"/review";
        LogUtil.loge("url: "+url);
        CustomRequest customRequest = new CustomRequest(Request.Method.GET, url, map, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(ratingMap.get(position) != null)
                        return;
                    int code = response.getInt("status_code");
                    if(code > 0){
                        double rating = response.getJSONObject("data").getDouble("average");
                        RatingModel model = new RatingModel();
                        model.setRating(rating);
                        ratingMap.put(position, model);
                        notifyDataSetChanged();
                        if(ratingMap.keySet().size() == mList.size()){
                            sortList();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("getVendorRating", error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                return headers;
            }
        };
        VolleyLibrary.getInstance(mContext).addToRequestQueue(customRequest, "", false);
    }

    private void getTimes(final String tempDay, final String hashId) {
        if(statusMap.get(hashId) != null)
            return;
        HashMap<String, String> hashMap = new HashMap<>();
        String url = MainActivity.baseUrl + "/partner/" + hashId + "/time";
        CustomRequest customRequest = new CustomRequest(Request.Method.GET, url, hashMap, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("getTimes", hashId+" : "+response.toString());
                try {
                    if(statusMap.get(hashId) != null)
                        return;
                    JSONArray vendorTimeLimits = response.getJSONArray("data");
                    for (int i = 0; i < vendorTimeLimits.length(); i++) {
                        String weekday = vendorTimeLimits.getJSONObject(i).getString("weekday").toLowerCase();
                        if (tempDay.equalsIgnoreCase(weekday)) {
                            LogUtil.loge("weekday: " + weekday);
                            // get times
                            String openTime = vendorTimeLimits.getJSONObject(i).getString("opening_time");
                            String closeTime = vendorTimeLimits.getJSONObject(i).getString("closing_time");
                            int openHour = Integer.parseInt(openTime.split(":")[0]);
                            int openMin = Integer.parseInt(openTime.split(":")[1]);
                            int closeHour = Integer.parseInt(closeTime.split(":")[0]);
                            int closeMin = Integer.parseInt(closeTime.split(":")[1]);

                            Calendar currentCal = Calendar.getInstance();

                            Calendar openCal = Calendar.getInstance();
                            openCal.set(Calendar.HOUR_OF_DAY, openHour);
                            openCal.set(Calendar.MINUTE, openMin);

                            Calendar closeCal = Calendar.getInstance();
                            closeCal.set(Calendar.HOUR_OF_DAY, closeHour);
                            closeCal.set(Calendar.MINUTE, closeMin);

                            if(currentCal.getTimeInMillis() > openCal.getTimeInMillis()
                                    && currentCal.getTimeInMillis() < closeCal.getTimeInMillis()){
                                statusMap.put(hashId, "1");
                            }
                            else{
                                statusMap.put(hashId, "2");
                            }

                            notifyDataSetChanged();
                            break;
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error", error.toString());
            }
        });
        VolleyLibrary.getInstance(mContext).addToRequestQueue(customRequest, "", false);
    }

    private void sortList() {
        SelectVendor selectVendor = (SelectVendor)mContext;
        selectVendor.hideLoader();
        for (int i = 0; i < mList.size(); i++) {
            if(ratingMap.get(i) == null)
                continue;
            mList.get(i).setRating((float) ratingMap.get(i).getRating());
        }
        sortByRatings();
    }

    public void sortByRatings() {
        int n = mList.size();
        VendorModel temp = null;
        for (int i = 0; i < n; i++) {
            for (int j = i+1; j < n; j++) {
                if (mList.get(i).getRating() < mList.get(j).getRating()) {
                    //swap elements
                    temp = mList.get(i);
                    mList.set(i, mList.get(j));
                    mList.set(j, temp);
                }
            }
        }

        notifyDataSetChanged();
    }

    public void sortByPrices() {
        Log.e("tag", "sortByPrices");
        int n = mList.size();
        VendorModel temp = null;
        for (int i = 0; i < n; i++) {
            for (int j = 1; j < (n - i); j++) {
                if (Double.parseDouble(mList.get(j - 1).getProduct_special_price())
                        > Double.parseDouble(mList.get(j).getProduct_special_price())) {
                    //swap elements
                    temp = mList.get(j - 1);
                    mList.set(j - 1, mList.get(j));
                    mList.set(j, temp);
                }
            }
        }
    }
}
