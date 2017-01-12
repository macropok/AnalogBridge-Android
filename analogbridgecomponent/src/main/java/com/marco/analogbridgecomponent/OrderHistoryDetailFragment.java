package com.marco.analogbridgecomponent;

import android.app.ProgressDialog;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class OrderHistoryDetailFragment extends Fragment {

    View rootView;
    Context mContext;
    ListView detailListView;
    JSONObject order = null;
    int index = -1;
    private ProgressDialog mProgressDialog = null;

    public class HistoryDetailAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;
        JSONObject order;
        JSONArray productArray;
        int index;

        public HistoryDetailAdapter(Context context, JSONObject order, int index) {
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.order = order;
            this.index = index;
            try {
                productArray = this.order.getJSONArray("products");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getCount() {
            if (productArray == null) {
                return 2;
            }
            else {
                return productArray.length() + 2;
            }
        }

        @Override
        public Object getItem(int position) {
            int count = getCount();
            if (position == 0 || position == count - 1) {
                return null;
            }

            if (productArray == null) {
                return null;
            }

            try {
                JSONObject product = productArray.getJSONObject(position);
                return product;
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
            View rowView = null;
            int count = getCount();

            if (position == 0) {
                String date = null, status = null;
                double total = 0;
                try {
                    date = order.getString("order_date");
                    status = order.getString("status_name");
                    total = order.getDouble("paymentTotal");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    int pending = order.getInt("pending");
                    if (pending != 0) {
                        rowView = mInflater.inflate(R.layout.order_detail_pending_layout, parent, false);

                        TextView detail_date = (TextView) rowView.findViewById(R.id.order_detail_date);
                        TextView detail_status = (TextView) rowView.findViewById(R.id.order_detail_status);
                        TextView detail_total = (TextView) rowView.findViewById(R.id.order_detail_total_paid);
                        final TextView detail_quote_status = (TextView) rowView.findViewById(R.id.order_detail_quote_status);
                        TextView detail_estimate_amount = (TextView) rowView.findViewById(R.id.order_detail_estimate_amount);
                        Button detail_approve_button = (Button) rowView.findViewById(R.id.order_detail_approve_button);
                        Button detail_reject_button = (Button) rowView.findViewById(R.id.order_detail_reject_button);

                        String estimate_title = order.getString("estimate_title");
                        String amount = order.getString("total_amount");

                        detail_date.setText(date);
                        detail_status.setText(status);
                        detail_total.setText(String.format("$%,.2f", total));

                        detail_quote_status.setText(estimate_title);
                        detail_estimate_amount.setText(APIService.sharedService().getCurrencyString(amount));

                        detail_approve_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    int order_id = order.getInt("order_id");
                                    showProgressDialog();
                                    APIService.sharedService().approveOrder(order_id, new CompletionHandler() {
                                        @Override
                                        public void completion(boolean bSuccess, String message) {
                                            if (bSuccess == true) {
                                                APIService.sharedService().getCustomer(new CompletionHandler() {
                                                    @Override
                                                    public void completion(boolean bSuccess, String message) {
                                                        if (bSuccess == true) {
                                                            AnalogBridgeActivity.currentActivity.invalidateOptionsMenu();
                                                            dismissProgressDialog();
                                                            try {
                                                                JSONArray orderArray = APIService.sharedService().customer.getJSONArray("orders");
                                                                order = orderArray.getJSONObject(index);
                                                                notifyDataSetChanged();
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                        else {
                                                            dismissProgressDialog();
                                                            Toast.makeText(AnalogBridgeActivity.currentActivity, message, Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            }
                                            else {
                                                dismissProgressDialog();
                                                Toast.makeText(AnalogBridgeActivity.currentActivity, message, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        detail_reject_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    int order_id = order.getInt("order_id");
                                    showProgressDialog();
                                    APIService.sharedService().rejectOrder(order_id, new CompletionHandler() {
                                        @Override
                                        public void completion(boolean bSuccess, String message) {
                                            if (bSuccess == true) {
                                                APIService.sharedService().getCustomer(new CompletionHandler() {
                                                    @Override
                                                    public void completion(boolean bSuccess, String message) {
                                                        if (bSuccess == true) {
                                                            AnalogBridgeActivity.currentActivity.invalidateOptionsMenu();
                                                            dismissProgressDialog();
                                                            try {
                                                                JSONArray orderArray = APIService.sharedService().customer.getJSONArray("orders");
                                                                order = orderArray.getJSONObject(index);
                                                                notifyDataSetChanged();
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                        else {
                                                            dismissProgressDialog();
                                                            Toast.makeText(AnalogBridgeActivity.currentActivity, message, Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            }
                                            else {
                                                dismissProgressDialog();
                                                Toast.makeText(AnalogBridgeActivity.currentActivity, message, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    else {

                        int approved = order.getInt("approved");
                        int rejected = order.getInt("rejected");

                        if (approved != 0 || rejected != 0) {
                            rowView = mInflater.inflate(R.layout.order_detail_approved_layout, parent, false);

                            TextView detail_date = (TextView) rowView.findViewById(R.id.order_detail_date);
                            TextView detail_status = (TextView) rowView.findViewById(R.id.order_detail_status);
                            TextView quote_status = (TextView) rowView.findViewById(R.id.order_detail_quote_status);
                            TextView detail_total = (TextView) rowView.findViewById(R.id.order_detail_total);

                            detail_date.setText(date);
                            detail_status.setText(status);
                            detail_total.setText(String.format("$%,.2f", total));
                            quote_status.setText(order.getString("estimate_title"));
                        }
                        else {
                            rowView = mInflater.inflate(R.layout.order_detail_overview_layout, parent, false);

                            TextView detail_date = (TextView) rowView.findViewById(R.id.order_detail_date);
                            TextView detail_status = (TextView) rowView.findViewById(R.id.order_detail_status);
                            TextView detail_total = (TextView) rowView.findViewById(R.id.order_detail_total);

                            detail_date.setText(date);
                            detail_status.setText(status);
                            detail_total.setText(String.format("$%,.2f", total));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    rowView = mInflater.inflate(R.layout.order_detail_overview_layout, parent, false);

                    TextView detail_date = (TextView) rowView.findViewById(R.id.order_detail_date);
                    TextView detail_status = (TextView) rowView.findViewById(R.id.order_detail_status);
                    TextView detail_total = (TextView) rowView.findViewById(R.id.order_detail_total);

                    detail_date.setText(date);
                    detail_status.setText(status);
                    detail_total.setText(String.format("$%,.2f", total));
                }
            }
            else if (position == count - 1) {
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

                try {
                    subtotal.setText(APIService.sharedService().getCurrencyString(order.getString("total_no_shipping")));
                    shipping.setText(APIService.sharedService().getCurrencyString(order.getString("shipping_amount")));
                    total.setText(APIService.sharedService().getCurrencyString(order.getString("total_amount")));
                    email.setText(order.getString("ship_email"));
                    telephone.setText(order.getString("ship_phone"));

                    ArrayList<String> strArray = new ArrayList<String>();
                    ArrayList<TextView> textViews = new ArrayList<TextView>();

                    strArray.add(order.getString("ship_first_name") + " " + order.getString("ship_last_name"));
                    strArray.add(order.getString("ship_company"));
                    strArray.add(order.getString("ship_address1"));
                    strArray.add(order.getString("ship_address2"));
                    strArray.add(order.getString("ship_city") + ", " + order.getString("ship_state") + " " + order.getString("ship_zip"));

                    textViews.add(name); textViews.add(company); textViews.add(address1); textViews.add(address2); textViews.add(city);

                    int index = 0;
                    for (int i = 0; i < strArray.size(); i++) {
                        String str = strArray.get(i);
                        if (str != null && str.compareTo("null") != 0) {
                            TextView view = textViews.get(index);
                            view.setText(str);
                            index++;
                        }
                    }

                    for (int i = index; i < textViews.size(); i++) {
                        TextView view = textViews.get(i);
                        view.setVisibility(View.INVISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                rowView = mInflater.inflate(R.layout.order_product_layout, parent, false);
                TextView item = (TextView) rowView.findViewById(R.id.order_item);
                TextView quantity = (TextView) rowView.findViewById(R.id.order_quantity);
                TextView price = (TextView) rowView.findViewById(R.id.order_price);
                TextView total = (TextView) rowView.findViewById(R.id.order_total);

                try {
                    JSONObject product = productArray.getJSONObject(position - 1);

                    item.setText(product.getString("description"));
                    quantity.setText(product.getString("quantity"));
                    price.setText("$" + product.getString("price_per_unit") + " per " + product.get("unit_name"));
                    total.setText(APIService.sharedService().getCurrencyString(order.getString("total")));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return rowView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.order_detail_layout, container, false);

        TextView title = (TextView) rootView.findViewById(R.id.order_detail_title);

        if (order != null) {
            try {
                int order_id = order.getInt("order_id");
                title.setText(String.format("Order Detail #%d", order_id));

                detailListView = (ListView) rootView.findViewById(R.id.order_detail_list);
                HistoryDetailAdapter adapter = new HistoryDetailAdapter(AnalogBridgeActivity.currentActivity, order, index);
                detailListView.setAdapter(adapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return rootView;
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this.getContext());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

}
