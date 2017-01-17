package com.marco.analogbridgecomponent;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class HowItWorksPageFragment extends Fragment {

    Context mContext;
    View rootView;
    ViewPager viewPager;

    ArrayList<ImageView> pageIndicators;

    public class HowItWorksPageAdapter extends PagerAdapter {

        private Context context;

        public HowItWorksPageAdapter(Context context) {
            this.context = context;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(context);
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.how_it_works_page_layout, container, false);
            ImageView image = (ImageView) layout.findViewById(R.id.page_image);
            TextView text = (TextView) layout.findViewById(R.id.page_text);

            if (position == 0) {
                image.setImageResource(R.drawable.a);
                text.setText("Receive analog transfer box 2-5 business days after purchase");
            }
            else if (position == 1) {
                image.setImageResource(R.drawable.b);
                text.setText("Add your analog media to the box");
            }
            else if (position == 2) {
                image.setImageResource(R.drawable.c);
                text.setText("Ready package with included materials and pre-paid FedEx label");
            }
            else if (position == 3) {
                image.setImageResource(R.drawable.d);
                text.setText("Call FedEx for free pick-up or drop-off at a local FedEx Location");
            }
            else if (position == 4) {
                image.setImageResource(R.drawable.e);
                text.setText("In 2-3 weeks all media is digitized and available in this app");
            }
            else if (position == 5) {
                image.setImageResource(R.drawable.a);
                text.setText("Receive your precious originals back just as they were");
            }

            container.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return  view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.how_it_works_scroll_layout, container, false);

        viewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        HowItWorksPageAdapter adapter = new HowItWorksPageAdapter(AnalogBridgeActivity.currentActivity);
        viewPager.setAdapter(adapter);

        pageIndicators = new ArrayList<ImageView>();

        ImageView first = (ImageView) rootView.findViewById(R.id.first_dot);
        ImageView second = (ImageView) rootView.findViewById(R.id.second_dot);
        ImageView third = (ImageView) rootView.findViewById(R.id.third_dot);
        ImageView forth = (ImageView) rootView.findViewById(R.id.forth_dot);
        ImageView fifth = (ImageView) rootView.findViewById(R.id.fifth_dot);
        ImageView sixth = (ImageView) rootView.findViewById(R.id.sixth_dot);

        pageIndicators.add(first);
        pageIndicators.add(second);
        pageIndicators.add(third);
        pageIndicators.add(forth);
        pageIndicators.add(fifth);
        pageIndicators.add(sixth);

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < 6; i++) {
                    ImageView dot = pageIndicators.get(i);
                    dot.setImageResource(R.drawable.unselected_dot);
                }

                ImageView selectedDot = pageIndicators.get(position);
                selectedDot.setImageResource(R.drawable.selected_dot);
            }
        });

        return rootView;
    }


}
