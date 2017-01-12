package com.marco.analogbridgecomponent;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class OrderSuccessFragment extends Fragment {

    View rootView;
    Context mContext;
    ListView orderProductList;

    public class OrderSuccessAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;
        JSONArray orderProductArray;
        JSONObject order;

        public OrderSuccessAdapter(Context context, JSONObject ord) {
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            order = ord;
            try {
                orderProductArray = order.getJSONArray("products");
            } catch (JSONException e) {
                e.printStackTrace();
                orderProductArray = null;
            }
        }

        @Override
        public int getCount() {
            if (orderProductArray == null) {
                return 3;
            }

            return orderProductArray.length() + 3;
        }

        @Override
        public Object getItem(int position) {
            if (orderProductArray == null) {
                return null;
            }
            else {
                int count = getCount();
                if (position == 0 || position == count - 2 || position == count - 1) {
                    return null;
                }

                try {
                    JSONObject prod = orderProductArray.getJSONObject(position - 1);
                    return prod;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            int count = getCount();
            View rowView;

            if (position == 0) {
                rowView = mInflater.inflate(R.layout.order_success_detail_layout, parent, false);
                TextView order_number = (TextView) rowView.findViewById(R.id.success_detail_order_number);
                TextView order_date = (TextView) rowView.findViewById(R.id.success_detail_date);
                TextView order_paid = (TextView) rowView.findViewById(R.id.success_detail_total_paid);

                try {
                    int number = order.getInt("order_id");
                    String date = order.getString("order_date");
                    double paid = order.getDouble("paymentTotal");

                    order_number.setText(String.format("%d", number));
                    order_date.setText(date);
                    order_paid.setText(String.format("$%,.2f", paid));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else if (position == count - 1) {
                rowView = mInflater.inflate(R.layout.order_next_step_layout, parent, false);
            }
            else if (position == count - 2) {
                rowView = mInflater.inflate(R.layout.order_customer_detail_layout, parent, false);
                TextView subtotal = (TextView) rowView.findViewById(R.id.order_subtotal);
                TextView shipping = (TextView) rowView.findViewById(R.id.order_shipping);
                TextView total = (TextView) rowView.findViewById(R.id.order_total);
                TextView email = (TextView) rowView.findViewById(R.id.order_customer_email);
                TextView telephone = (TextView) rowView.findViewById(R.id.order_customer_phone);
                TextView name = (TextView) rowView.findViewById(R.id.order_customer_name);
                TextView company = (TextView) rowView.findViewById(R.id.order_customer_company);
                TextView address1 = (TextView) rowView.findViewById(R.id.order_customer_address1);
                TextView address2 = (TextView) rowView.findViewById(R.id.order_customer_address2);
                TextView city = (TextView) rowView.findViewById(R.id.order_customer_city);

                ArrayList<String> validStr = new ArrayList<String>();

                JSONObject ship = new JSONObject();
                String em = null, phone = null, customer_name = null, add1 = null, ct = null, cmp = null, add2 = null;
                try {
                    String sub_total = APIService.sharedService().getCurrencyString(order.getString("total_no_shipping"));
                    String shipping_amount = APIService.sharedService().getCurrencyString(order.getString("shipping_amount"));
                    String order_total = APIService.sharedService().getCurrencyString(order.getString("total_amount"));

                    ship = order.getJSONObject("ship");
                    em = ship.getString("ship_email");
                    phone = ship.getString("ship_phone");
                    customer_name = ship.getString("ship_first_name") + " " + ship.getString("ship_last_name");
                    add1 = ship.getString("ship_address1");
                    ct = ship.getString("ship_city") + ", " + ship.getString("ship_state") + " " + ship.getString("ship_zip");

                    subtotal.setText(sub_total);
                    shipping.setText(shipping_amount);
                    total.setText(order_total);
                    email.setText(em);
                    telephone.setText(phone);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    cmp = ship.getString("ship_company");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    add2 = ship.getString("ship_address2");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                ArrayList<TextView> textViews = new ArrayList<>();
                textViews.add(name);
                textViews.add(company);
                textViews.add(address1);
                textViews.add(address2);
                textViews.add(city);

                validStr.add(customer_name); validStr.add(cmp); validStr.add(add1); validStr.add(add2); validStr.add(ct);

                int index = 0;
                for (int i = 0; i < validStr.size(); i++) {
                    String str = validStr.get(i);
                    if (str != null && str.compareTo("null") != 0) {
                        TextView view = textViews.get(index);
                        view.setText(str);
                        index++;
                    }
                }

                for (int i = index; i < validStr.size(); i++) {
                    TextView view = textViews.get(i);
                    view.setVisibility(View.INVISIBLE);
                }
            }
            else {
                rowView = mInflater.inflate(R.layout.order_product_layout, parent, false);
                TextView item = (TextView) rowView.findViewById(R.id.order_item);
                TextView quantity = (TextView) rowView.findViewById(R.id.order_quantity);
                TextView price = (TextView) rowView.findViewById(R.id.order_price);
                TextView total = (TextView) rowView.findViewById(R.id.order_total);

                try {
                    JSONObject prod = orderProductArray.getJSONObject(position - 1);

                    String description = prod.getString("description");
                    int qty = prod.getInt("quantity");
                    String bridge_price = "$" + prod.getString("bridge_price") + " per " + prod.getString("unit_name");
                    String order_total = APIService.sharedService().getCurrencyString(prod.getString("total"));

                    item.setText(description);
                    quantity.setText(String.format("%d", qty));
                    price.setText(bridge_price);
                    total.setText(order_total);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return rowView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.order_success_layout, container, false);

        orderProductList = (ListView) rootView.findViewById(R.id.order_product_list);

        mContext = AnalogBridgeActivity.currentActivity;
        OrderSuccessAdapter adapter = new OrderSuccessAdapter(mContext, APIService.sharedService().order);

        orderProductList.setAdapter(adapter);

        for (int i = 0; i < APIService.sharedService().products.length(); i++) {
            try {
                JSONObject product = APIService.sharedService().products.getJSONObject(i);
                product.put("qty", 0);
                product.put("current_qty", 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (APIService.sharedService().estimateBox != null) {
            try {
                APIService.sharedService().estimateBox.put("qty", 0);
                APIService.sharedService().estimateBox.put("current_qty", 0);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        AnalogBridgeActivity.currentActivity.invalidateOptionsMenu();

        return rootView;
    }

}
