package com.fabiosanto.currencyconverter;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
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
    private final String stringUrl = "http://api.fixer.io/latest?base=AUD&symbols=CAD,EUR,JPY,USD,GBP";
    private String[] currencies = new String[]{null,"CAD", "EUR", "GBP", "JPY", "USD",null};
    private Locale[] locales = new Locale[]{null, Locale.CANADA, Locale.ITALY, Locale.UK, Locale.JAPAN, Locale.US,null};

    JSONObject wsResult = null;
    NumberFormat formatInput = NumberFormat.getCurrencyInstance();
    NumberFormat formatResult = NumberFormat.getCurrencyInstance(Locale.UK);
    NumberFormat formatNumber = NumberFormat.getNumberInstance();
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

        //start rerieving data from ws
        new GetData().execute();
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
//            Number number = formatNumber.parse();
            double value = formatInput.parse(valueTx.getText().toString()).doubleValue();
            double converted = rate * value;

            resultTx.setText(formatResult.format(converted));

        } catch (Exception e) {
            tryRecoverData();
            e.printStackTrace();
        }
    }

    private void tryRecoverData() {
        try {
            Number number = formatNumber.parse(valueTx.getText().toString());
            valueTx.setText(formatInput.format(number.doubleValue()));

        }catch (Exception e){
            valueTx.setText("0.00");
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

            if(currencies[currentItem]!=null){
                formatResult = NumberFormat.getCurrencyInstance(locales[currentItem]);

                refreshColors(currentItem);
                calculate();
            }

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



    //asynctask to retrieve data from ws
    private class GetData extends AsyncTask<Void,Void,JSONObject>{
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this,null,"Checking rates...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
                try{
                    String response = getData();
                    JSONObject wsResult = new JSONObject(response);

                    return wsResult;
                }catch (Exception e){
                    return null;
                }
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            progressDialog.dismiss();
            if (result!=null){

                wsResult = result;
                valueTx.setText(formatInput.format(100.00));
                mViewPager.setCurrentItem(currentItem);

            }else {
                //show error
                Toast.makeText(MainActivity.this,"Ops an error occured",Toast.LENGTH_SHORT).show();
            }
        }

        private String getData() throws IOException {
            InputStream inputStream = null;

            try {
                URL url = new URL(stringUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                // Starts the query
                conn.connect();

                int response = conn.getResponseCode();
                inputStream = conn.getInputStream();

                // Convert the inputStream into a string
                String contentAsString = getResponse(inputStream);
                return contentAsString;

                //Close the inputStream at the end
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }

        private String getResponse(InputStream inputStream) throws IOException {
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }

            return total.toString();
        }
    }
}
