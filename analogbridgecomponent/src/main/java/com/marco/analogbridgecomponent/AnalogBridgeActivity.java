package com.marco.analogbridgecomponent;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikepenz.actionitembadge.library.ActionItemBadge;

import org.json.JSONObject;

public class AnalogBridgeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    public enum SCREEN {
        FORMAT_PHOTO,
        FORMAT_IMAGE,
        FORMAT_FILM,
        HOW_IT_WORKS,
        FEATURES,
        FAQS,
        CART,
        CHECK_OUT,
        ORDER_SUCCESS,
        ORDER_HISTORY,
        EXIT
    }

    public static AnalogBridgeActivity currentActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentActivity = this;

        setContentView(R.layout.main_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getKeyAndToken();

        showScreen(SCREEN.FORMAT_PHOTO);
    }

    void getKeyAndToken() {
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            String key = extra.getString("PUBLIC_KEY");
            String token = extra.getString("TOKEN");

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        if (APIService.sharedService().getApprovalCount() > 0) {
            ActionItemBadge.update(this, menu.findItem(R.id.order_history), getResources().getDrawable(R.drawable.ic_person_black_24dp), ActionItemBadge.BadgeStyles.GREEN, APIService.sharedService().getApprovalCount());
        } else {
            ActionItemBadge.update(this, menu.findItem(R.id.order_history), getResources().getDrawable(R.drawable.ic_person_black_24dp), ActionItemBadge.BadgeStyles.GREEN, null);
        }

        if (APIService.sharedService().getEstimateCount() > 0) {
            ActionItemBadge.update(this, menu.findItem(R.id.shopping_cart), getResources().getDrawable(R.drawable.ic_shopping_basket_black_24dp), ActionItemBadge.BadgeStyles.GREEN, APIService.sharedService().getEstimateCount());
        } else {
            ActionItemBadge.update(this, menu.findItem(R.id.shopping_cart), getResources().getDrawable(R.drawable.ic_shopping_basket_black_24dp), ActionItemBadge.BadgeStyles.GREEN, null);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.order_history) {
            showScreen(SCREEN.ORDER_HISTORY);
        }
        else if (id == R.id.shopping_cart) {
            showScreen(SCREEN.CART);
        }
        else if (id == R.id.exit_app) {
            showScreen(SCREEN.EXIT);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.photos) {
            showScreen(SCREEN.FORMAT_PHOTO);
        }
        else if (id == R.id.images) {
            showScreen(SCREEN.FORMAT_IMAGE);
        }
        else if (id == R.id.films) {
            showScreen(SCREEN.FORMAT_FILM);
        }
        else if (id == R.id.how_it_works) {
            showScreen(SCREEN.HOW_IT_WORKS);
        }
        else if (id == R.id.features) {
            showScreen(SCREEN.FEATURES);
        }
        else if (id == R.id.faqs) {
            showScreen(SCREEN.FAQS);
        }
        else if (id == R.id.sendbox) {
            APIService.sharedService().increaseEstimateBox();
            showScreen(SCREEN.CART);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showScreen(SCREEN screen) {
        Fragment fragment = null;

        switch (screen) {
            case FORMAT_PHOTO:
                fragment = new FormatFragment();
                ((FormatFragment) fragment).category = SCREEN.FORMAT_PHOTO;
                break;
            case FORMAT_IMAGE:
                fragment = new FormatFragment();
                ((FormatFragment) fragment).category = SCREEN.FORMAT_IMAGE;
                break;
            case FORMAT_FILM:
                fragment = new FormatFragment();
                ((FormatFragment) fragment).category = SCREEN.FORMAT_FILM;
                break;
            case HOW_IT_WORKS:
                fragment = new HowItWorksFragment();
                break;
            case FEATURES:
                fragment = new FeaturesFragment();
                break;
            case FAQS:
                fragment = new FaqFragment();
                break;
            case CART:
                fragment = new CartFragment();
                break;
            case CHECK_OUT:
                fragment = new CheckoutFragment();
                break;
            case ORDER_SUCCESS:
                fragment = new OrderSuccessFragment();
                break;
            case ORDER_HISTORY:
                fragment = new OrderHistoryFragment();
                break;
            case EXIT:
                Intent i = new Intent();
                setResult(RESULT_OK);
                finish();
                return;
        }

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content_main, fragment).commitAllowingStateLoss();
    }

    public void showOrderDetailScreen(JSONObject order, int index) {
        OrderHistoryDetailFragment fragment = new OrderHistoryDetailFragment();
        fragment.order = order;
        fragment.index = index;
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content_main, fragment).commitAllowingStateLoss();
    }

}
