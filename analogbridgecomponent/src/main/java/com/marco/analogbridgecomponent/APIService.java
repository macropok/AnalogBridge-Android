package com.marco.analogbridgecomponent;

import android.content.Context;
import android.widget.Toast;

import com.loopj.android.http.*;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.exception.AuthenticationException;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

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

    public JSONObject customer = null;
    public JSONObject order = null;

    public Stripe stripe = null;

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
                        getCustomer(new CompletionHandler(){
                            @Override
                            public void completion(boolean bSuccess, String message) {
                                if (bSuccess == true) {

                                    try {
                                        stripe = new Stripe(publicKey);
                                        handler.completion(true, null);
                                    } catch (AuthenticationException e) {
                                        e.printStackTrace();
                                        handler.completion(false, e.toString());
                                    }
                                }
                                else {
                                    handler.completion(false, message);
                                }
                            }
                        });
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

    public void getCustomer(final CompletionHandler handler) {
        if (customer != null) {
            handler.completion(true, null);
            return;
        }

        String url = getApiURL("customer");
        RequestParams params = new RequestParams();
        params.add("publicKey", publicKey);
        params.add("customerToken", customerToken);

        getRequest(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                customer = response;
                handler.completion(true, null);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                customer = null;
                handler.completion(false, errorResponse.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                customer = null;
                handler.completion(false, responseString);
            }
        });
    }

    public void submitOrder(final Card card, final CompletionHandler handler) {
        if (card == null || stripe == null) {
            handler.completion(false, "Payment not defined yet.");
            return;
        }

        stripe.createToken(card, new TokenCallback() {
            @Override
            public void onError(Exception error) {
                handler.completion(false, error.toString());
            }

            @Override
            public void onSuccess(final Token token) {
                JSONObject auth = new JSONObject();
                try {
                    auth.put("publicKey", publicKey);
                    JSONObject tokenObj = getTokenJson(token);
                    auth.put("card", tokenObj);
                    auth.put("customer", customer);
                    JSONObject estimate = getEstimateJson();
                    if (estimate == null) {
                        handler.completion(false, "Products Information Error.");
                        return;
                    }

                    auth.put("estimate", estimate);

                    String url = getApiURL("customer/orders");
                    postRequest(url, auth, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {

                                String status = response.getString("status");
                                String message = response.getString("message");

                                if (status.compareTo("success") == 0) {
                                    order = response.getJSONObject("data");
                                    handler.completion(true, null);
                                }
                                else {
                                    handler.completion(false, message);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                order = null;
                                handler.completion(false, "Get Order Information Failed.");
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
                }
                catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    JSONObject getTokenJson(Token token) {
        JSONObject res = new JSONObject();

        try {
            res.put("brand", token.getCard().getBrand());
            res.put("id", token.getId());
            res.put("expMonth", token.getCard().getExpMonth());
            res.put("expYear", token.getCard().getExpYear());
            res.put("expString", String.format("%d/%d", token.getCard().getExpMonth(), token.getCard().getExpYear()));
            res.put("isLive", token.getLivemode());
            res.put("last4", token.getCard().getLast4());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return res;
    }

    JSONObject getEstimateJson() {
        JSONObject res = new JSONObject();

        try {
            JSONObject prods = new JSONObject();

            double total = 0.0;
            int basketCount = 0;

            for (int i = 0; i < products.length(); i++) {
                JSONObject product = products.getJSONObject(i);
                JSONObject prodCapsule = new JSONObject();

                int qty = product.getInt("qty");
                double price = product.getDouble("price");

                total += qty * price;
                basketCount += qty;

                prodCapsule.put("qty", qty);
                prodCapsule.put("total", price * qty);
                prodCapsule.put("formatTotal", String.format("%.2f", price*qty));
                prodCapsule.put("data", product);

                int productID = product.getInt("bridge_product_id");
                String key = String.format("%d", productID);
                prods.put(key, prodCapsule);
            }

            if (estimateBox != null && estimateBox.getInt("qty") != 0) {
                int qty = estimateBox.getInt("qty");
                double price = estimateBox.getDouble("price");

                basketCount += qty;
                if (total < qty * price) {
                    total = qty * price;
                }

                JSONObject estProd = new JSONObject();
                estProd.put("qty", qty);
                estProd.put("total", qty * price);
                estProd.put("data", estimateBox);
                int productID = estimateBox.getInt("bridge_product_id");
                String key = String.format("%d", productID);
                prods.put(key, estProd);
            }

            res.put("basketCount", basketCount);
            res.put("formatTotal", String.format("%.2f", total));
            res.put("total", total);
            res.put("products", prods);
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return res;
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
        if (customer != null) {
            try {
                int approval = customer.getInt("approvals");
                return approval;
            } catch (JSONException e) {
                e.printStackTrace();
                return 0;
            }
        }

        return 0;
    }

    JSONObject getOrder(int orderID) {
        if (customer == null) return null;
        try {
            JSONArray orderArray = customer.getJSONArray("orders");
            for (int i = 0; i < orderArray.length(); i++) {
                JSONObject ord = orderArray.getJSONObject(i);
                if (ord.getInt("order_id") == orderID) {
                    return ord;
                }
            }

            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    void updateOrder(int orderID, boolean approve) {
        if (customer == null) return;
        JSONObject ord = getOrder(orderID);

        if (ord == null) return;

        try {
            ord.put("rejected", false);
            ord.put("approved", false);
            ord.put("pending", false);
            ord.put("estimate_status", false);

            if (approve == true) {
                ord.put("estimate_title", "Approved");
            }
            else {
                ord.put("estimate_title", "Rejected");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void approveOrder(final int orderID, final CompletionHandler handler) {
        String url = getApiURL("customer/orders/") + String.format("%d", orderID) + "/approve-estimate";
        JSONObject auth = new JSONObject();
        try {
            auth.put("publicKey", publicKey);
            postRequest(url, auth, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    updateOrder(orderID, true);
                    handler.completion(true, responseString);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    handler.completion(false, responseString);
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

    void rejectOrder(final int orderID, final CompletionHandler handler) {
        String url = getApiURL("customer/orders/") + String.format("%d", orderID) + "/reject-estimate";
        JSONObject auth = new JSONObject();
        try {
            auth.put("publicKey", publicKey);
            postRequest(url, auth, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    updateOrder(orderID, false);
                    handler.completion(true, responseString);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    handler.completion(false, responseString);
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
