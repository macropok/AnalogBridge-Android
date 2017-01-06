package com.marco.analogbridgecomponent;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class FormatFragment extends Fragment {
    Context mContext;
    View rootView;
    ListView productListView;
    JSONArray productArray;
    private ProgressDialog mProgressDialog = null;

    public AnalogBridgeActivity.SCREEN category = AnalogBridgeActivity.SCREEN.FORMAT_PHOTO;

    public class ProductAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater mInflater;
        JSONArray productArray;

        public ProductAdapter(Context context, JSONArray array) {
            mContext = context;
            productArray = array;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if (productArray == null)
                return 0;
            return productArray.length();
        }

        @Override
        public Object getItem(int position) {
            if (productArray == null)
                return null;
            try {
                return productArray.getJSONObject(position);
            } catch (JSONException e) {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View rowView = mInflater.inflate(R.layout.format_cell_layout, parent, false);

            TextView name = (TextView) rowView.findViewById(R.id.product_name);
            TextView price = (TextView) rowView.findViewById(R.id.product_price);
            ImageView image = (ImageView) rowView.findViewById(R.id.product_image);
            final EditText estimate = (EditText) rowView.findViewById(R.id.product_esitmate);

            final JSONObject product = (JSONObject) getItem(position);

            try {
                name.setText(product.getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                double prc = product.getDouble("price");
                String unitName = product.getString("unit_name");
                price.setText(String.format("$%.2f per %s", prc, unitName));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            int quantity = -1;
            try {
                quantity = product.getInt("current_qty");
                if (quantity != 0) {
                    estimate.setText(Integer.toString(quantity));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            final int finalQuantity = quantity;
            estimate.setTag(0);
            estimate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus == false) return;

                    int tag = (int) v.getTag();
                    if (tag != 0) return;
                    v.setTag(1);

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
                            if (inputView.getText().toString().length() > 0) {
                                String qtyStr = inputView.getText().toString();
                                try {
                                    int qt = Integer.parseInt(qtyStr);
                                    if (qt != 0) {
                                        int oldQt = product.getInt("qty");
                                        product.put("current_qty", qt);
                                        product.put("qty", oldQt + qt);
                                        estimate.setText(qtyStr);
                                        AnalogBridgeActivity.currentActivity.invalidateOptionsMenu();
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
                            estimate.setTag(0);
                            notifyDataSetChanged();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            estimate.setTag(0);
                            notifyDataSetChanged();
                        }
                    }).show();
                }
            });

            try {
                String imageURL = product.getString("thumb");
                DownloadImagesTask task = new DownloadImagesTask();
                task.imageView = image;
                task.url = imageURL;
                task.execute(image);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return rowView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.format_layout, container, false);
        mContext = AnalogBridgeActivity.currentActivity;

        productListView = (ListView) rootView.findViewById(R.id.product_list);

        showProgressDialog();
        APIService.sharedService().getProducts(new CompletionHandler() {
            @Override
            public void completion(boolean bSuccess, String message) {
                dismissProgressDialog();
                if (bSuccess == true) {
                    productArray = APIService.sharedService().products;
                    ProductAdapter adapter = new ProductAdapter(mContext, productArray);
                    productListView.setAdapter(adapter);
                    AnalogBridgeActivity.currentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int startIndex = getStartIndex();

                            productListView.setSelection(startIndex);
                        }
                    });
                }
                else {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                }
            }
        });

        return rootView;
    }

    public int getStartIndex() {

        if (productArray == null)
            return 0;

        String typename;
        switch (category) {
            case FORMAT_PHOTO:
                typename = "image";
                break;
            case FORMAT_IMAGE:
                typename = "film";
                break;
            default:
                typename = "video";
                break;
        }

        for (int i = 0; i < productArray.length(); i++) {
            JSONObject item = null;
            try {
                item = productArray.getJSONObject(i);
                if (item.getString("type_name").compareTo(typename) == 0) {
                    return i;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return 0;
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
