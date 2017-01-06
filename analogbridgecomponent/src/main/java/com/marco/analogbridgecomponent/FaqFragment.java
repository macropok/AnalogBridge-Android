package com.marco.analogbridgecomponent;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class FaqFragment extends Fragment {


    Context mContext;
    View rootView;
    ListView faqListView;
    private ProgressDialog mProgressDialog = null;

    public class FaqAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;
        JSONArray faqArray;

        public FaqAdapter(Context context, JSONArray array) {
            mContext = context;
            faqArray = array;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if (faqArray == null)
                return 0;
            return faqArray.length();
        }

        @Override
        public Object getItem(int position) {
            if (faqArray == null)
                return null;
            try {
                return faqArray.getJSONObject(position);
            } catch (JSONException e) {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = mInflater.inflate(R.layout.faq_cell_layout, parent, false);

            TextView question = (TextView) rowView.findViewById(R.id.question);
            TextView answer = (TextView) rowView.findViewById(R.id.answer);

            JSONObject faq = (JSONObject) getItem(position);

            try {
                question.setText(faq.getString("question"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                answer.setText(faq.getString("answer"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return rowView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.faq_layout, container, false);
        mContext = AnalogBridgeActivity.currentActivity;

        faqListView = (ListView) rootView.findViewById(R.id.faq_list);
        showProgressDialog();

        APIService.sharedService().getFaqs(new CompletionHandler() {
            @Override
            public void completion(boolean bSuccess, String message) {
                dismissProgressDialog();
                if (bSuccess == true) {
                    AnalogBridgeActivity.currentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            FaqAdapter adapter = new FaqAdapter(mContext, APIService.sharedService().faqs);
                            faqListView.setAdapter(adapter);
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
