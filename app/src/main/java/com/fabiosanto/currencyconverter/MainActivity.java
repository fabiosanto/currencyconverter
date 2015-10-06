package com.fabiosanto.currencyconverter;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private String[] currencies = new String[]{null,"CAD", "EUR", "GBP", "JPY", "USD",null};

    JSONObject wsResult = null;

    private ViewPager mViewPager;

    int currentItem = 2;
    private TextView resultTx;
    private EditText valueTx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //prepare view
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new VpAdapter());
        mViewPager.addOnPageChangeListener(onPageChangeListener);

        valueTx = (EditText) findViewById(R.id.value);
        valueTx.addTextChangedListener(txw);
        resultTx = (TextView) findViewById(R.id.result);


    }

    //listener for input editext changes
    TextWatcher txw = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            calculate();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    //method to convert input in result value
    private void calculate() {
        try {
            String currency = currencies[currentItem];
            JSONObject joRates = wsResult.getJSONObject("rates");

            double rate = joRates.getDouble(currency);
            double value = Double.parseDouble(valueTx.getText().toString());
            double converted = rate * value;

            resultTx.setText(String.valueOf(converted));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //viewpager change page listener
        ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int i, float v, int i1) {
        }

        @Override
        public void onPageSelected(int position) {
            currentItem = position+1;
            refreshColors(currentItem);
            calculate();
        }

        @Override
        public void onPageScrollStateChanged(int i) {
        }
    };

    //need to refresh color for the three currencies showed
    //before, center and next
    //i leave the others as are beacuse they won't be changed
    private void refreshColors(int selected){

        try{
            changeColor(mViewPager.findViewWithTag(selected-1),false);
            changeColor(mViewPager.findViewWithTag(selected),true);
            changeColor(mViewPager.findViewWithTag(selected + 1),false);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    //change color for textview if selected
    private void changeColor(View v,boolean isSelected){
        TextView tx = (TextView) v.findViewById(R.id.currency);
        if (isSelected)
            tx.setTextColor(getResources().getColor(R.color.white));
        else
            tx.setTextColor(getResources().getColor(R.color.second_green));
    }

    //viewpager adapter
    private class VpAdapter extends PagerAdapter{

        @Override
        public View instantiateItem(ViewGroup container, int position) {

            View v = getLayoutInflater().inflate(R.layout.currency_item,container,false);

            TextView tx = (TextView) v.findViewById(R.id.currency);
            tx.setText(currencies[position]);

            //set up color on instantiation time
            changeColor(v, (currentItem+1) == position);

            //set position as tag
            //i will use this as reference to retrieve the view after
            //i use this way cause the Viewpager.getCurrentItem is not always giving the correct selected item
            //viewpager doesn't help totally on 3 page-a-time set up (view1 | view1 | view2)
            v.setTag(position);
            container.addView(v);

            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {
            ((ViewPager) container).removeView((View) view);
        }

        @Override
        public int getCount() {
            return currencies.length;
        }

        @Override
        public float getPageWidth(int position) {
            return 0.34f;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == (View)o;
        }
    }


}
