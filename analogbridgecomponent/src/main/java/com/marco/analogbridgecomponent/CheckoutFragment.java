package com.marco.analogbridgecomponent;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.stripe.android.Stripe;
import com.stripe.android.model.Card;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

public class CheckoutFragment extends Fragment {

    View rootView;
    private ProgressDialog mProgressDialog = null;

    JSONObject states;
    private static final int MAX_CARD_NUMBER_LENGTH = 16;
    private static final int MAX_CARD_DATE_LENGTH = 5;
    private static final int MAX_CARD_CVV_LENGTH = 3;
    String mLastInput ="";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.checkout_layout, container, false);

        final EditText first_name = (EditText) rootView.findViewById(R.id.first_name);
        final EditText last_name = (EditText) rootView.findViewById(R.id.last_name);
        final EditText email = (EditText) rootView.findViewById(R.id.email);
        final EditText phone_number = (EditText) rootView.findViewById(R.id.phone_number);
        final EditText address1 = (EditText) rootView.findViewById(R.id.address1);
        final EditText address2 = (EditText) rootView.findViewById(R.id.address2);
        final EditText company = (EditText) rootView.findViewById(R.id.company);
        final EditText city = (EditText) rootView.findViewById(R.id.city);
        final EditText zipcode = (EditText) rootView.findViewById(R.id.zip_code);
        final EditText custom_instruction = (EditText) rootView.findViewById(R.id.custom_instruction);

        TextView partial_payment = (TextView) rootView.findViewById(R.id.partial_payment);

        final EditText card_number = (EditText) rootView.findViewById(R.id.card_number);
        card_number.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(MAX_CARD_NUMBER_LENGTH);
        card_number.setFilters(filterArray);

        final EditText billing_zip = (EditText) rootView.findViewById(R.id.billing_zip);
        final EditText expiration_date = (EditText) rootView.findViewById(R.id.expiration_date);
        expiration_date.setInputType(InputType.TYPE_CLASS_NUMBER);
        filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(MAX_CARD_DATE_LENGTH);
        expiration_date.setFilters(filterArray);
        expiration_date.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                SimpleDateFormat formatter = new SimpleDateFormat("MM/yy", Locale.GERMANY);
                Calendar expiryDateDate = Calendar.getInstance();
                if (s.length() == 2 && !mLastInput.endsWith("/")) {
                    int month = Integer.parseInt(input);
                    if (month <= 12) {
                        expiration_date.setText(expiration_date.getText().toString() + "/");
                        expiration_date.setSelection(expiration_date.getText().toString().length());
                    }
                }else if (s.length() == 2 && mLastInput.endsWith("/")) {
                    int month = Integer.parseInt(input);
                    if (month <= 12) {
                        expiration_date.setText(expiration_date.getText().toString().substring(0,1));
                        expiration_date.setSelection(expiration_date.getText().toString().length());
                    } else {
                        expiration_date.setText("");
                        expiration_date.setSelection(expiration_date.getText().toString().length());
                        Toast.makeText(AnalogBridgeActivity.currentActivity, "Enter a valid month", Toast.LENGTH_LONG).show();
                    }
                } else if (s.length() == 1){
                    int month = Integer.parseInt(input);
                    if (month > 1) {
                        expiration_date.setText("0" + expiration_date.getText().toString() + "/");
                        expiration_date.setSelection(expiration_date.getText().toString().length());
                    }
                }
                else {

                }
                mLastInput = expiration_date.getText().toString();
                return;
            }
        });

        final EditText cvc = (EditText) rootView.findViewById(R.id.cvc);
        cvc.setInputType(InputType.TYPE_CLASS_NUMBER);
        filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(MAX_CARD_CVV_LENGTH);
        cvc.setFilters(filterArray);

        final Spinner state = (Spinner) rootView.findViewById(R.id.state);

        String state_str = "{\"1\":{\"short\":\"AL\",\"name\":\"Alabama\",\"country_code\":\"US\"},\"2\":{\"short\":\"AK\",\"name\":\"Alaska\",\"country_code\":\"US\"},\"3\":{\"short\":\"AS\",\"name\":\"AmericanSamoa\",\"country_code\":\"US\"},\"4\":{\"short\":\"AZ\",\"name\":\"Arizona\",\"country_code\":\"US\"},\"5\":{\"short\":\"AR\",\"name\":\"Arkansas\",\"country_code\":\"US\"},\"6\":{\"short\":\"CA\",\"name\":\"California\",\"country_code\":\"US\"},\"7\":{\"short\":\"CO\",\"name\":\"Colorado\",\"country_code\":\"US\"},\"8\":{\"short\":\"CT\",\"name\":\"Connecticut\",\"country_code\":\"US\"},\"9\":{\"short\":\"DE\",\"name\":\"Delaware\",\"country_code\":\"US\"},\"10\":{\"short\":\"DC\",\"name\":\"DistrictofColumbia\",\"country_code\":\"US\"},\"11\":{\"short\":\"FM\",\"name\":\"FederatedStatesofMicronesia\",\"country_code\":\"US\"},\"12\":{\"short\":\"FL\",\"name\":\"Florida\",\"country_code\":\"US\"},\"13\":{\"short\":\"GA\",\"name\":\"Georgia\",\"country_code\":\"US\"},\"14\":{\"short\":\"GU\",\"name\":\"Guam\",\"country_code\":\"US\"},\"15\":{\"short\":\"HI\",\"name\":\"Hawaii\",\"country_code\":\"US\"},\"16\":{\"short\":\"ID\",\"name\":\"Idaho\",\"country_code\":\"US\"},\"17\":{\"short\":\"IL\",\"name\":\"Illinois\",\"country_code\":\"US\"},\"18\":{\"short\":\"IN\",\"name\":\"Indiana\",\"country_code\":\"US\"},\"19\":{\"short\":\"IA\",\"name\":\"Iowa\",\"country_code\":\"US\"},\"20\":{\"short\":\"KS\",\"name\":\"Kansas\",\"country_code\":\"US\"},\"21\":{\"short\":\"KY\",\"name\":\"Kentucky\",\"country_code\":\"US\"},\"22\":{\"short\":\"LA\",\"name\":\"Louisiana\",\"country_code\":\"US\"},\"23\":{\"short\":\"ME\",\"name\":\"Maine\",\"country_code\":\"US\"},\"24\":{\"short\":\"MH\",\"name\":\"MarshallIslands\",\"country_code\":\"US\"},\"25\":{\"short\":\"MD\",\"name\":\"Maryland\",\"country_code\":\"US\"},\"26\":{\"short\":\"MA\",\"name\":\"Massachusetts\",\"country_code\":\"US\"},\"27\":{\"short\":\"MI\",\"name\":\"Michigan\",\"country_code\":\"US\"},\"28\":{\"short\":\"MN\",\"name\":\"Minnesota\",\"country_code\":\"US\"},\"29\":{\"short\":\"MS\",\"name\":\"Mississippi\",\"country_code\":\"US\"},\"30\":{\"short\":\"MO\",\"name\":\"Missouri\",\"country_code\":\"US\"},\"31\":{\"short\":\"MT\",\"name\":\"Montana\",\"country_code\":\"US\"},\"32\":{\"short\":\"NE\",\"name\":\"Nebraska\",\"country_code\":\"US\"},\"33\":{\"short\":\"NV\",\"name\":\"Nevada\",\"country_code\":\"US\"},\"34\":{\"short\":\"NH\",\"name\":\"NewHampshire\",\"country_code\":\"US\"},\"35\":{\"short\":\"NJ\",\"name\":\"NewJersey\",\"country_code\":\"US\"},\"36\":{\"short\":\"NM\",\"name\":\"NewMexico\",\"country_code\":\"US\"},\"37\":{\"short\":\"NY\",\"name\":\"NewYork\",\"country_code\":\"US\"},\"38\":{\"short\":\"NC\",\"name\":\"NorthCarolina\",\"country_code\":\"US\"},\"39\":{\"short\":\"ND\",\"name\":\"NorthDakota\",\"country_code\":\"US\"},\"40\":{\"short\":\"MP\",\"name\":\"NorthernMarianaIslands\",\"country_code\":\"US\"},\"41\":{\"short\":\"OH\",\"name\":\"Ohio\",\"country_code\":\"US\"},\"42\":{\"short\":\"OK\",\"name\":\"Oklahoma\",\"country_code\":\"US\"},\"43\":{\"short\":\"OR\",\"name\":\"Oregon\",\"country_code\":\"US\"},\"44\":{\"short\":\"PW\",\"name\":\"Palau\",\"country_code\":\"US\"},\"45\":{\"short\":\"PA\",\"name\":\"Pennsylvania\",\"country_code\":\"US\"},\"46\":{\"short\":\"PR\",\"name\":\"PuertoRico\",\"country_code\":\"US\"},\"47\":{\"short\":\"RI\",\"name\":\"RhodeIsland\",\"country_code\":\"US\"},\"48\":{\"short\":\"SC\",\"name\":\"SouthCarolina\",\"country_code\":\"US\"},\"49\":{\"short\":\"SD\",\"name\":\"SouthDakota\",\"country_code\":\"US\"},\"50\":{\"short\":\"TN\",\"name\":\"Tennessee\",\"country_code\":\"US\"},\"51\":{\"short\":\"TX\",\"name\":\"Texas\",\"country_code\":\"US\"},\"52\":{\"short\":\"UT\",\"name\":\"Utah\",\"country_code\":\"US\"},\"53\":{\"short\":\"VT\",\"name\":\"Vermont\",\"country_code\":\"US\"},\"54\":{\"short\":\"VI\",\"name\":\"VirginIslands\",\"country_code\":\"US\"},\"55\":{\"short\":\"VA\",\"name\":\"Virginia\",\"country_code\":\"US\"},\"56\":{\"short\":\"WA\",\"name\":\"Washington\",\"country_code\":\"US\"},\"57\":{\"short\":\"WV\",\"name\":\"WestVirginia\",\"country_code\":\"US\"},\"58\":{\"short\":\"WI\",\"name\":\"Wisconsin\",\"country_code\":\"US\"},\"59\":{\"short\":\"WY\",\"name\":\"Wyoming\",\"country_code\":\"US\"},\"60\":{\"short\":\"AE\",\"name\":\"ArmedForcesEurope,theMiddleEast,andCanada\",\"country_code\":\"US\"},\"61\":{\"short\":\"AP\",\"name\":\"ArmedForcesPacific\",\"country_code\":\"US\"},\"62\":{\"short\":\"AA\",\"name\":\"ArmedForcesAmericas(exceptCanada)\",\"country_code\":\"US\"}}";
        try {
            states = new JSONObject(state_str);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (states != null) {
            List<String> stateArray = new ArrayList<String>();
            for (int i = 0; i < 62; i++) {
                String key = String.format("%d", i + 1);
                try {
                    JSONObject ind_state = states.getJSONObject(key);
                    String state_name = ind_state.getString("name");
                    stateArray.add(state_name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(AnalogBridgeActivity.currentActivity, android.R.layout.simple_spinner_dropdown_item, stateArray);
            state.setAdapter(adapter);
        }

        showProgressDialog();
        APIService.sharedService().getCustomer(new CompletionHandler() {
            @Override
            public void completion(boolean bSuccess, String message) {
                dismissProgressDialog();

                if (bSuccess == true) {
                    JSONObject customer = APIService.sharedService().customer;
                    try {

                        if (customer.has("instructions") == true) {
                            String customInstruction = customer.getString("instructions");
                            if (customInstruction != null && customInstruction.length() > 0 && customInstruction.compareTo("null") != 0) {
                                custom_instruction.setText(customInstruction);
                            }
                        }

                        JSONObject ship = customer.getJSONObject("ship");

                        first_name.setText(getValidateString(ship, "first_name"));
                        last_name.setText(getValidateString(ship, "last_name"));
                        email.setText(getValidateString(ship, "email"));
                        phone_number.setText(getValidateString(ship, "phone"));
                        address1.setText(getValidateString(ship, "address1"));
                        address2.setText(getValidateString(ship, "address2"));
                        company.setText(getValidateString(ship, "company"));
                        city.setText(getValidateString(ship, "city"));
                        zipcode.setText(getValidateString(ship, "zip"));

                        String state_short = ship.getString("state");

                        for (int i = 0; i < 62; i++) {
                            String key = String.format("%d", i + 1);
                            try {
                                JSONObject ind_state = states.getJSONObject(key);
                                String short_name = ind_state.getString("short");

                                if (short_name.compareTo(state_short) == 0) {
                                    state.setSelection(i);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(AnalogBridgeActivity.currentActivity, "Get Customer Information Failed " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toast.makeText(AnalogBridgeActivity.currentActivity, message, Toast.LENGTH_LONG).show();
                }
            }
        });

        Button submitOrder = (Button) rootView.findViewById(R.id.submit_order);
        submitOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = AnalogBridgeActivity.currentActivity.getCurrentFocus();
                InputMethodManager imm = (InputMethodManager)AnalogBridgeActivity.currentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                if (isValidated(first_name) == false) {
                    showErrorAlert("First Name should be not empty.");
                    return;
                }

                if (isValidated(last_name) == false) {
                    showErrorAlert("Last Name should be not empty.");
                    return;
                }

                if (isValidated(email) == false) {
                    showErrorAlert("Email Address should be not empty.");
                    return;
                }

                if (isValidated(phone_number) == false) {
                    showErrorAlert("Phone Number should be not empty.");
                    return;
                }

                if (isValidated(address1) == false) {
                    showErrorAlert("Address1 should be not empty.");
                    return;
                }

                if (isValidated(city) == false) {
                    showErrorAlert("City should be not empty.");
                    return;
                }

                if (isValidated(zipcode) == false) {
                    showErrorAlert("ZIP code should be not empty.");
                    return;
                }

                if (isValidated(card_number) == false) {
                    showErrorAlert("Card Number is required.");
                    return;
                }

                if (isValidateCardNumber(card_number.getText().toString()) == false) {
                    showErrorAlert("Please correct Card Number");
                    return;
                }

                if (isValidated(billing_zip) == false) {
                    showErrorAlert("Billing Zip is required.");
                    return;
                }

                if (isValidated(expiration_date) == false) {
                    showErrorAlert("Expiration Date is required.");
                    return;
                }

                if (isValidateExpirationDate(expiration_date.getText().toString()) == false) {
                    showErrorAlert("Please correct Expiration Date.");
                    return;
                }

                if (isValidated(cvc) == false) {
                    showErrorAlert("CVC is required.");
                    return;
                }

                if (isValidateCVC(cvc.getText().toString()) == false) {
                    showErrorAlert("Please correct CVC.");
                    return;
                }

                JSONObject customer = APIService.sharedService().customer;
                try {
                    JSONObject ship = customer.getJSONObject("ship");

                    ship.put("first_name", first_name.getText().toString());
                    ship.put("last_name", last_name.getText().toString());
                    ship.put("email", email.getText().toString());
                    ship.put("phone", phone_number.getText().toString());
                    ship.put("address1", address1.getText().toString());
                    ship.put("city", city.getText().toString());
                    ship.put("zip", zipcode.getText().toString());

                    if (address2.getText().toString() == null || address2.getText().toString().length() == 0) {
                        ship.put("address2", "null");
                    }
                    else {
                        ship.put("address2", address2.getText().toString());
                    }

                    if (company.getText().toString() == null || company.getText().toString().length() == 0) {
                        ship.put("company", "null");
                    }
                    else {
                        ship.put("company", company.getText().toString());
                    }

                    int selectedState = state.getSelectedItemPosition();
                    JSONObject sel_state = states.getJSONObject(String.format("%d", selectedState + 1));
                    String short_state = sel_state.getString("short");
                    ship.put("state", short_state);

                    if (custom_instruction.getText().toString() == null || custom_instruction.getText().toString().length() == 0) {
                        APIService.sharedService().customer.put("instructions", "null");
                    }
                    else {
                        APIService.sharedService().customer.put("instructions", custom_instruction.getText().toString());
                    }

                    APIService.sharedService().customer.put("ship", ship);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String card_number_str = card_number.getText().toString();
                String[] parts = expiration_date.getText().toString().split("/");
                String expMonth = parts[0];
                String expYear = parts[1];
                String cvc_str = cvc.getText().toString();
                String name = first_name.getText().toString() + " " + last_name.getText().toString();

                Card card = new Card(card_number_str,
                        Integer.parseInt(expMonth),
                        Integer.parseInt(expYear),
                        cvc_str,
                        name,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

                showProgressDialog();
                APIService.sharedService().submitOrder(card, new CompletionHandler() {
                    @Override
                    public void completion(boolean bSuccess, String message) {

                        if (bSuccess == true) {
                            APIService.sharedService().getCustomer(new CompletionHandler() {
                                @Override
                                public void completion(boolean bSuccess, String message) {
                                    dismissProgressDialog();
                                    if (bSuccess == true) {
                                        Log.d("customer", APIService.sharedService().customer.toString());
                                        AnalogBridgeActivity.currentActivity.showScreen(AnalogBridgeActivity.SCREEN.ORDER_SUCCESS);
                                    }
                                    else {
                                        Toast.makeText(AnalogBridgeActivity.currentActivity, message, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                        else {
                            dismissProgressDialog();
                            Toast.makeText(AnalogBridgeActivity.currentActivity, message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        partial_payment.setText(String.format("$%,.2f", getPartitialPayment()));

        return rootView;
    }

    void pushShipInformation() {

    }

    boolean isValidated(EditText control) {
        String value = control.getText().toString();
        if (value == null || value.length() == 0) {
            return false;
        }
        return true;
    }

    boolean isValidateCardNumber(String card_number) {

        if (card_number == null)  {
            return false;
        }

        if (card_number.length() != 16) {
            return false;
        }

        return true;
    }

    boolean isValidateExpirationDate(String date) {
        if (date == null) {
            return false;
        }

        if (date.length() > 5 || date.contains("/") == false) {
            return false;
        }

        return true;
    }

    boolean isValidateCVC(String cvc) {
        if (cvc == null || cvc.length() != 3) {
            return false;
        }
        return true;
    }

    String getValidateString(JSONObject json, String key) {
        try {
            String result = json.getString(key);
            if (result.compareTo("null") == 0) {
                return null;
            }
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showErrorAlert(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(AnalogBridgeActivity.currentActivity).create();
        alertDialog.setTitle("Submit Error");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
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

    private double getPartitialPayment() {
        double value = 0;
        double price = 0;

        try {
            price = APIService.sharedService().estimateBox.getDouble("price");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (APIService.sharedService().estimateBox != null) {
            try {
                int qty = APIService.sharedService().estimateBox.getInt("qty");
                if (qty > 0) {
                    value = qty * price;
                }
                else {
                    value = price;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                value = price;
            }
        }

        return value;
    }
}
