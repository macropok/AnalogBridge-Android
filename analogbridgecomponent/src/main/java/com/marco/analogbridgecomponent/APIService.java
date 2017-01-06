package com.marco.analogbridgecomponent;

import android.content.Context;

import com.loopj.android.http.*;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;


public class APIService {

    private Context context;

    private static APIService sharedService = null;
    private AsyncHttpClient client = new AsyncHttpClient();
    private static final int TIME_OUT = 70000;

    String apiURL = "https://api.analogbridge.io";
    String publicKey = null;
    String customerToken = null;

    public JSONArray products = null;
    public JSONObject estimateBox = null;
    public JSONArray faqs = null;

    public APIService() {
        client.setTimeout(TIME_OUT);
        client.setConnectTimeout(TIME_OUT);
        client.setResponseTimeout(TIME_OUT);
    }

    public static APIService sharedService() {
        if (sharedService == null) {
            sharedService = new APIService();
        }
        return sharedService;
    }

    public void setAuth(final String key, final String token, final CompletionHandler handler) {
        JSONObject auth = new JSONObject();
        String url = "https://api.analogbridge.io/v1/customer/auth";
        try {
            auth.put("publicKey", key);
            auth.put("customerToken", token);

            postRequest(url, auth, new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    if (statusCode == 200) {
                        publicKey = key;
                        customerToken = token;
                        handler.completion(true, responseString);
                    }
                    else {
                        handler.completion(false, responseString);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    handler.completion(false, errorResponse.toString());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            handler.completion(false, e.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            handler.completion(false, e.toString());
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private String getApiURL(String url) {
        String retURL = apiURL + "/v1/" + url;
        return retURL;
    }

    public void getProducts(final CompletionHandler handler) {

        if (products != null) {
            handler.completion(true, null);
            return;
        }

        String url = getApiURL("products/in");
        getRequest(url, new RequestParams(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray prods = response.getJSONArray("products");
                    products = new JSONArray();
                    for (int i = 0; i < prods.length(); i++) {
                        JSONObject item = prods.getJSONObject(i);
                        item.put("qty", 0);
                        item.put("current_qty", 0);
                        if (item.getString("unit_name").compareTo("box") == 0) {
                            estimateBox = item;
                        }
                        else {
                            products.put(item);
                        }
                    }

                    handler.completion(true, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                    products = null;
                    handler.completion(false, "Response Data is Null");
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                products = response;
                handler.completion(true, null);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                products = null;
                handler.completion(false, responseString);
            }
        });
    }

    public void increaseEstimateBox() {
        if (estimateBox != null) {
            int qty = 0;
            try {
                qty = estimateBox.getInt("qty");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            qty++;
            try {
                estimateBox.put("qty", qty);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void getFaqs(final CompletionHandler handler) {
        if (faqs != null) {
            handler.completion(true, null);
            return;
        }

        String url = getApiURL("faqs");
        getRequest(url, new RequestParams(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                faqs = response;
                handler.completion(true, null);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                faqs = null;
                handler.completion(false, responseString);
            }
        });
    }

    public int getEstimateCount() {

        if (products == null) {
            return 0;
        }

        int count = 0;
        for (int i = 0; i < products.length(); i++) {
            try {
                JSONObject product = products.getJSONObject(i);
                count += product.getInt("qty");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (estimateBox != null) {
            try {
                count += estimateBox.getInt("qty");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return count;
    }

    public double getEstimate() {
        double estimate = 0;

        if (products != null) {
            for (int i = 0; i < products.length(); i++) {
                try {
                    JSONObject product = products.getJSONObject(i);
                    estimate += product.getDouble("price") * product.getInt("qty");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        if (estimateBox != null) {
            try {
                double boxEstimate = estimateBox.getDouble("price") * estimateBox.getDouble("qty");
                if (estimate < boxEstimate) {
                    return boxEstimate;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return estimate;
    }

    public int getApprovalCount() {
        return 0;
    }

    private void postRequest(String url, RequestParams params, JsonHttpResponseHandler handler) {
        client.post(url, params, handler);
    }

    private void getRequest(String url, RequestParams params, JsonHttpResponseHandler handler) {
        client.get(url, params, handler);
    }

    private void putRequest(String url, RequestParams params, JsonHttpResponseHandler handler) {
        client.put(url, params, handler);
    }

    private void postRequest(String url, JSONObject object, JsonHttpResponseHandler handler) throws UnsupportedEncodingException {
        StringEntity entity = new StringEntity(object.toString(), ContentType.APPLICATION_JSON);
        client.post(this.context, url, entity, ContentType.APPLICATION_JSON.getMimeType(), handler);
    }

    private void getRequest(String url, JSONObject object, JsonHttpResponseHandler handler) throws UnsupportedEncodingException {
        StringEntity entity = new StringEntity(object.toString(), ContentType.APPLICATION_JSON);
        client.get(this.context, url, entity, ContentType.APPLICATION_JSON.getMimeType(), handler);
    }

    private void putRequest(String url, JSONObject object, JsonHttpResponseHandler handler) throws UnsupportedEncodingException {
        StringEntity entity = new StringEntity(object.toString(), ContentType.APPLICATION_JSON);
        client.put(this.context, url, entity, ContentType.APPLICATION_JSON.getMimeType(), handler);
    }

    private void putRequest(String url, JSONArray array, JsonHttpResponseHandler handler) {
        StringEntity entity = new StringEntity(array.toString(), ContentType.APPLICATION_JSON);
        client.put(this.context, url, entity, ContentType.APPLICATION_JSON.getMimeType(), handler);
    }

    private void postRequest(String url, JSONArray array, JsonHttpResponseHandler handler) {
        StringEntity entity = new StringEntity(array.toString(), ContentType.APPLICATION_JSON);
        client.post(this.context, url, entity, ContentType.APPLICATION_JSON.getMimeType(), handler);
    }
}
