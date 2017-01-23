package com.marco.analogbridgecomponent;

import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;


public class FaqExpandableFragment extends Fragment {
    Context mContext;
    View rootView;
    private ProgressDialog mProgressDialog = null;
    ExpandableListView faqListView;

    public class FaqExpandableAdapter extends BaseExpandableListAdapter {
        private Context context;
        private ArrayList<String> expandableListTitle;
        private ArrayList<String> expandableListDetail;

        public  FaqExpandableAdapter(Context context, ArrayList<String> listTitle, ArrayList<String> listDetail) {
            this.context = context;
            expandableListTitle = listTitle;
            expandableListDetail = listDetail;
        }

        @Override
        public Object getChild(int listPosition, int expandedListPosition) {
            return expandableListDetail.get(listPosition);
        }

        @Override
        public long getChildId(int listPosition, int expandedListPosition) {
            return expandedListPosition;
        }

        @Override
        public View getChildView(int listPosition, final int expandedListPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            final String expandedListText = (String) getChild(listPosition, expandedListPosition);

            LayoutInflater layoutInflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.faq_answer_layout, null);
            TextView expandedListTextView = (TextView) convertView
                    .findViewById(R.id.answer);
            expandedListTextView.setText(expandedListText);
            return convertView;
        }

        @Override
        public int getChildrenCount(int listPosition) {
            return 1;
        }

        @Override
        public Object getGroup(int listPosition) {
            return this.expandableListTitle.get(listPosition);
        }

        @Override
        public int getGroupCount() {
            return this.expandableListTitle.size();
        }

        @Override
        public long getGroupId(int listPosition) {
            return listPosition;
        }

        @Override
        public View getGroupView(int listPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String listTitle = (String) getGroup(listPosition);

            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                        getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.faq_question_layout, null);

            TextView listTitleTextView = (TextView) convertView
                    .findViewById(R.id.question);
            listTitleTextView.setText(listTitle);

            ImageView indicator = (ImageView) convertView.findViewById(R.id.indicator);
            if (isExpanded == false) {
                //indicator.setImageResource(android.R.drawable.arrow_down_float);
                indicator.setImageResource(R.drawable.down);
            }
            else {
                //indicator.setImageResource(android.R.drawable.arrow_up_float);
                indicator.setImageResource(R.drawable.up);
            }

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int listPosition, int expandedListPosition) {
            return true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.faq_expandable_layout, container, false);
        mContext = AnalogBridgeActivity.currentActivity;
        faqListView = (ExpandableListView) rootView.findViewById(R.id.faq_list);

        Display display = AnalogBridgeActivity.currentActivity.getWindowManager().getDefaultDisplay();
        //int width = display.getWidth();
        //faqListView.setIndicatorBounds(width - 50, width);

        showProgressDialog();

        APIService.sharedService().getFaqs(new CompletionHandler() {
            @Override
            public void completion(boolean bSuccess, String message) {
                dismissProgressDialog();
                if (bSuccess == true) {
                    AnalogBridgeActivity.currentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONArray faqArray = APIService.sharedService().faqs;
                            ArrayList<String> questionArray = new ArrayList<String>();
                            ArrayList<String> answerArray = new ArrayList<String>();
                            for (int i = 0; i < faqArray.length(); i++) {
                                try {
                                    JSONObject faq = faqArray.getJSONObject(i);
                                    String question = faq.getString("question");
                                    String answer = faq.getString("answer");
                                    questionArray.add(question);
                                    answerArray.add(answer);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            FaqExpandableAdapter adapter = new FaqExpandableAdapter(AnalogBridgeActivity.currentActivity, questionArray, answerArray);
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
