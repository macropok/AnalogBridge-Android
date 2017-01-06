package com.marco.analogbridgecomponent;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class CartFragment extends Fragment {
    Context mContext;
    View rootView;
    ListView cartListView;
    JSONArray cartProductArray;

    public class CartAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;
        JSONArray productArray;

        public CartAdapter(Context context) {
            mContext = context;
            productArray = APIService.sharedService().products;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {

            int count = 1;

            if (productArray == null) {
                return 1;
            }

            for (int i = 0; i < productArray.length(); i++) {
                try {
                    JSONObject product = productArray.getJSONObject(i);
                    int qty = product.getInt("qty");
                    if (qty != 0) count++;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (APIService.sharedService().estimateBox != null) {
                try {
                    int boxQty = APIService.sharedService().estimateBox.getInt("qty");
                    if (boxQty > 0) count++;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return count;
        }

        @Override
        public Object getItem(int position) {
            if (position == 0 && APIService.sharedService().estimateBox != null){
                try {
                    int qty = APIService.sharedService().estimateBox.getInt("qty");
                    if (qty > 0)
                        return APIService.sharedService().estimateBox;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (productArray == null) {
                return null;
            }

            int index = 0;

            if (APIService.sharedService().estimateBox != null) {
                try {
                    int qty = APIService.sharedService().estimateBox.getInt("qty");
                    if (qty > 0)
                        index = 1;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            for (int realIndex = 0; realIndex < productArray.length(); realIndex++) {
                try {
                    JSONObject product = productArray.getJSONObject(realIndex);
                    int qty = product.getInt("qty");
                    if (qty > 0) {
                        if (index == position)
                            return product;
                        else
                            index++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final JSONObject product = (JSONObject) getItem(position);
            View rowView;
            if (product == null) {
                rowView = mInflater.inflate(R.layout.cart_checkout_layout, parent, false);

                TextView totalEstimate = (TextView) rowView.findViewById(R.id.cart_total_price);
                Button checkOut = (Button) rowView.findViewById(R.id.cart_checkout_button);

                double estimate = APIService.sharedService().getEstimate();
                totalEstimate.setText(String.format("$ %.2f", estimate));

                checkOut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

            }
            else {
                rowView = mInflater.inflate(R.layout.cart_cell_layout, parent, false);
                ImageView cartImage = (ImageView) rowView.findViewById(R.id.cart_image);
                TextView cartName = (TextView) rowView.findViewById(R.id.cart_product_title);
                TextView cartPrice = (TextView) rowView.findViewById(R.id.cart_product_price);
                //Button plus = (Button) rowView.findViewById(R.id.cart_plus_button);
                //Button minus = (Button) rowView.findViewById(R.id.cart_minus_button);
                final EditText cartQty = (EditText) rowView.findViewById(R.id.cart_qty);
                ImageButton cartRemove = (ImageButton) rowView.findViewById(R.id.cart_remove_button);
                cartRemove.setTag(position);

                try {
                    String name = product.getString("name");
                    cartName.setText(name);

                    double price = product.getDouble("price");
                    String unitName = product.getString("unit_name");
                    cartPrice.setText(String.format("$%.2f per %s", price, unitName));

                    int qty = product.getInt("qty");
                    cartQty.setText(String.format("%d", qty));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    String imageURL = product.getString("thumb");
                    DownloadImagesTask task = new DownloadImagesTask();
                    task.imageView = cartImage;
                    task.url = imageURL;
                    task.execute(cartImage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
/*
                plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            int qty = product.getInt("qty");
                            qty++;
                            product.put("qty", qty);
                            cartQty.setText(String.format("%d", qty));
                            notifyDataSetChanged();
                            AnalogBridgeActivity.currentActivity.invalidateOptionsMenu();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            int qty = product.getInt("qty");
                            qty = (qty > 0) ? qty - 1 : 0;
                            product.put("qty", qty);
                            cartQty.setText(String.format("%d", qty));
                            notifyDataSetChanged();
                            AnalogBridgeActivity.currentActivity.invalidateOptionsMenu();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
*/
                cartRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            product.put("qty", 0);
                            AnalogBridgeActivity.currentActivity.invalidateOptionsMenu();
                            notifyDataSetChanged();
                            CartFragment.this.updateCartCount();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                int cart_qty = 0;
                try {
                    cart_qty = product.getInt("qty");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final int finalQuantity = cart_qty;
                cartQty.setTag(1001);
                cartQty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(final View v, boolean hasFocus) {
                        if (hasFocus == false) return;

                        int tag = (int) v.getTag();
                        if (tag != 1001) return;
                        v.setTag(1002);

                        final EditText inputView = new EditText(AnalogBridgeActivity.currentActivity);
                        inputView.setInputType(InputType.TYPE_CLASS_NUMBER);
                        final int qty = finalQuantity;
                        if (qty != 0 && qty != -1) {
                            inputView.setText((new Integer(qty)).toString());
                            inputView.setSelection(inputView.getText().length());
                        }
                        inputView.requestFocus();
                        InputMethodManager imm = (InputMethodManager) AnalogBridgeActivity.currentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(inputView, InputMethodManager.SHOW_IMPLICIT);

                        new AlertDialog.Builder(AnalogBridgeActivity.currentActivity).setTitle("Estimate Quantity").setMessage(null).setView(inputView).setPositiveButton("Add To Estimate", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                v.setTag(1001);
                                if (inputView.getText().toString().length() > 0) {
                                    String qtyStr = inputView.getText().toString();
                                    try {
                                        int qt = Integer.parseInt(qtyStr);
                                        if (qt != 0) {
                                            product.put("qty", qt);
                                            cartQty.setText(qtyStr);
                                            AnalogBridgeActivity.currentActivity.invalidateOptionsMenu();
                                            CartFragment.this.updateCartCount();
                                        }
                                        dialog.dismiss();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        dialog.cancel();
                                    }
                                }
                                else {
                                    dialog.cancel();
                                }
                                notifyDataSetChanged();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                v.setTag(1001);
                                dialog.cancel();
                                notifyDataSetChanged();
                                CartFragment.this.updateCartCount();
                            }
                        }).show();
                    }
                });
            }

            return rowView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.cart_layout, container, false);
        mContext = AnalogBridgeActivity.currentActivity;

        cartListView = (ListView) rootView.findViewById(R.id.cart_list);
        CartAdapter adapter = new CartAdapter(mContext);
        cartListView.setAdapter(adapter);

        updateCartCount();

        return rootView;
    }

    public void updateCartCount() {
        TextView cartCount = (TextView) rootView.findViewById(R.id.cart_count);
        int estimateCount = APIService.sharedService().getEstimateCount();
        String countStr = "Currently " + estimateCount + " items in cart";
        cartCount.setText(countStr);
    }

}
