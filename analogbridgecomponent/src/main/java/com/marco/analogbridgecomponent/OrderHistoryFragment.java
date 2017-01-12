package com.marco.analogbridgecomponent;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OrderHistoryFragment extends Fragment {

    View rootView;
    Context mContext;
    ListView orderHistoryListView;

    public class OrderHistoryAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;
        JSONArray orderArray;

        public OrderHistoryAdapter(Context context, JSONArray array) {
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            orderArray = array;
        }

        @Override
        public int getCount() {
            if (orderArray == null) {
                return 0;
            }

            return orderArray.length();
        }

        @Override
        public Object getItem(int position) {
            try {
                JSONObject order = orderArray.getJSONObject(position);
                return order;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View rowView = mInflater.inflate(R.layout.order_history_cell_layout, parent, false);

            TextView orderID = (TextView) rowView.findViewById(R.id.order_history_number);
            TextView orderDate = (TextView) rowView.findViewById(R.id.order_history_date);
            TextView orderStatus = (TextView) rowView.findViewById(R.id.order_history_status);
            TextView orderTotal = (TextView) rowView.findViewById(R.id.order_history_total);
            Button viewButton = (Button) rowView.findViewById(R.id.order_history_button);

            try {
                JSONObject order = orderArray.getJSONObject(position);
                String order_id = "#" + order.getString("order_id");
                String order_date = order.getString("order_date");
                String order_status = order.getString("status_name");
                String order_total = APIService.sharedService().getCurrencyString(order.getString("total_amount"));

                orderID.setText(order_id);
                orderDate.setText(order_date);
                orderStatus.setText(order_status);
                orderTotal.setText(order_total);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                JSONObject order = orderArray.getJSONObject(position);
                int pending = order.getInt("pending");
                if (pending != 0) {
                    viewButton.setText("APPROVE/REJECT QUOTE");
                    viewButton.setBackgroundColor(0xFFFFCF05);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            viewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        JSONObject order = orderArray.getJSONObject(position);
                        AnalogBridgeActivity.currentActivity.showOrderDetailScreen(order, position);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            return rowView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.order_history_layout, container, false);

        orderHistoryListView = (ListView) rootView.findViewById(R.id.order_history_list);
        try {
            JSONArray orderArray = APIService.sharedService().customer.getJSONArray("orders");
            OrderHistoryAdapter adapter = new OrderHistoryAdapter(AnalogBridgeActivity.currentActivity, orderArray);
            orderHistoryListView.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rootView;
    }
}
