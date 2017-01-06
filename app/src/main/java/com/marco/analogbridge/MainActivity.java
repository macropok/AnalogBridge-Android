package com.marco.analogbridge;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.marco.analogbridgecomponent.APIService;
import com.marco.analogbridgecomponent.AnalogBridgeActivity;
import com.marco.analogbridgecomponent.CompletionHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    String tokenURL = "https://analogbridge.io/analog/customer/token";
    String publicKey = "pk_test_eY7kL6QyHG3tNUHbmLdDYWWN";

    AsyncHttpClient client = new AsyncHttpClient();
    private ProgressDialog mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button importButton = (Button) this.findViewById(R.id.import_analog);
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressDialog();
                getRequest(tokenURL, new RequestParams(), new JsonHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        if (statusCode == 200) {
                            APIService.sharedService().setAuth(publicKey, responseString, new CompletionHandler() {
                                @Override
                                public void completion(boolean bSuccess, String message) {
                                    dismissProgressDialog();
                                    if (bSuccess == true) {
                                        Intent i = new Intent(MainActivity.this, AnalogBridgeActivity.class);
                                        startActivityForResult(i, 1);
                                    }
                                    else {
                                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                        else {
                            dismissProgressDialog();
                            Toast.makeText(MainActivity.this, responseString, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    private void getRequest(String url, RequestParams params, JsonHttpResponseHandler handler) {
        client.get(url, params, handler);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
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
