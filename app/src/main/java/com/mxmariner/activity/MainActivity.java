package com.mxmariner.activity;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.mxmariner.drawer.DrawerAboutFragment;
import com.mxmariner.drawer.DrawerFragment;
import com.mxmariner.drawer.DrawerHarmonicsFragment;
import com.mxmariner.drawer.DrawerSettingsFragment;
import com.mxmariner.fragment.MXMainFragment;
import com.mxmariner.fragment.MXMainFragmentId;
import com.mxmariner.fragment.MXTideMapFragment;
import com.mxmariner.fragment.StationCardRecyclerFragment;
import com.mxmariner.signal.DrawerMenuSignal;
import com.mxmariner.signal.SignalDispatch;
import com.mxmariner.tides.R;
import com.mxmariner.util.MXPreferences;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class MainActivity extends Activity {

    //region CLASS VARIABLES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public static final String TAG = MainActivity.class.getSimpleName();

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //region CLASS METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //region FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private MXMainFragmentId pendingId = null;
    private MXPreferences mxPreferences;
    private MenuEventSubscriber menuEventSubscriber;
    private StationIdEvenSubscriber stationIdEvenSubscriber;

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //region CONSTRUCTOR ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //region ACCESSORS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //region PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private void navigateToMenuFragment(Fragment menuFragment, String backStackName) {
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right,
                        R.animator.slide_in_right, R.animator.slide_out_left)
                .replace(R.id.left_drawer, menuFragment)
                .addToBackStack(backStackName)
                .commit();
    }

    private void navigateToMainFragmentWithId(MXMainFragmentId fragmentId, boolean enforce) {
        if (enforce || mxPreferences.getMainFragmentId() != fragmentId) {

            MXMainFragment mxMainFragment = null;

            switch (fragmentId) {
                case STATION_CARD_RECYCLER_FRAGMENT_TIDES: {
                    mxMainFragment = StationCardRecyclerFragment.createFragment(fragmentId);
                    break;
                }

                case STATION_CARD_RECYCLER_FRAGMENT_CURRENTS: {
                    mxMainFragment = StationCardRecyclerFragment.createFragment(fragmentId);
                    break;
                }

                case MAP_FRAGMENT_TIDES: {
                    mxMainFragment = MXTideMapFragment.createFragment(fragmentId);
                    break;
                }

                case MAP_FRAGMENT_CURRENTS: {
                    mxMainFragment = MXTideMapFragment.createFragment(fragmentId);
                    break;
                }
            }

            if (mxMainFragment != null) {
                getFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.animator.fade_in, R.animator.fade_out,
                                R.animator.fade_in, R.animator.fade_out)
                        .replace(R.id.activity_main_fragment_container, mxMainFragment, MXMainFragment.TAG)
                        .commit();
                mxPreferences.setMainFragmentId(fragmentId);
            } else {
                Log.e(TAG, "navigateToMainFragmentWithId() fragment was null!");
            }
        } else {
            Log.d(TAG, "navigateToMainFragmentWithId() already at target fragment");
        }
    }

    private void subscribeToStationIdEvent() {
        if (stationIdEvenSubscriber != null) {
            stationIdEvenSubscriber.unsubscribe();
        }
        stationIdEvenSubscriber = new StationIdEvenSubscriber();
        SignalDispatch.getInstance().getStationIdEventObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stationIdEvenSubscriber);
    }

    private void subscribeToDrawerMenuEvent() {
        if (menuEventSubscriber != null) {
            menuEventSubscriber.unsubscribe();
        }
        menuEventSubscriber = new MenuEventSubscriber();
        SignalDispatch.getInstance().getDrawerEventObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(menuEventSubscriber);
    }

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //region PUBLIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //region INNER CLASSES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private class StationIdEvenSubscriber extends Subscriber<Long> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, "onError()", e);
            subscribeToStationIdEvent();
        }

        @Override
        public void onNext(Long aLong) {
            onStationIdEvent(aLong);
        }
    }

    private class MenuEventSubscriber extends Subscriber<DrawerMenuSignal> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, "onError()", e);
            subscribeToDrawerMenuEvent();
        }

        @Override
        public void onNext(DrawerMenuSignal drawerMenuSignal) {
            onDrawerMenuEvent(drawerMenuSignal);
        }
    }

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //region EVENTS  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public void onStationIdEvent(Long stationId) {
        StationActivity.startWithStationId(this, stationId);
    }

    public void onDrawerMenuEvent(DrawerMenuSignal event) {
        switch (event) {
            case CLOSE_TIDE_STATIONS: {
                pendingId = MXMainFragmentId.STATION_CARD_RECYCLER_FRAGMENT_TIDES;
                drawerLayout.closeDrawer(Gravity.START);
                break;
            }
            case CLOSE_CURRENT_STATIONS: {
                pendingId = MXMainFragmentId.STATION_CARD_RECYCLER_FRAGMENT_CURRENTS;
                drawerLayout.closeDrawer(Gravity.START);
                break;
            }
            case MAP_TIDE: {
                pendingId = MXMainFragmentId.MAP_FRAGMENT_TIDES;
                drawerLayout.closeDrawer(Gravity.START);
                break;
            }
            case MAP_CURRENT: {
                pendingId = MXMainFragmentId.MAP_FRAGMENT_CURRENTS;
                drawerLayout.closeDrawer(Gravity.START);
                break;
            }
            case HARMONICS: {
                navigateToMenuFragment(new DrawerHarmonicsFragment(), null);
                break;
            }
            case SETTINGS: {
                navigateToMenuFragment(new DrawerSettingsFragment(), DrawerSettingsFragment.TAG);
                break;
            }
            case SETTINGS_DONE: {
                getFragmentManager()
                        .popBackStack();
                MXMainFragment mxMainFragment = (MXMainFragment) getFragmentManager().findFragmentByTag(MXMainFragment.TAG);
                if (mxMainFragment != null) {
                    mxMainFragment.invalidate();
                }
                DrawerFragment drawerFragment = (DrawerFragment) getFragmentManager().findFragmentByTag("DrawerFragment");
                if (drawerFragment != null) {
                    drawerFragment.invalidate();
                }
                break;
            }
            case ABOUT: {
                navigateToMenuFragment(new DrawerAboutFragment(), null);
                break;
            }
        }
    }

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    /*~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^
                                               ANDROID
    ~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^*/


    //region LIFE CYCLE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
        setContentView(R.layout.activity_main);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        actionBarDrawerToggle = new ActionBarDrawerListener();

        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }

        subscribeToDrawerMenuEvent();
        subscribeToStationIdEvent();

        mxPreferences = new MXPreferences(getApplicationContext());
        navigateToMainFragmentWithId(mxPreferences.getMainFragmentId(), true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        menuEventSubscriber.unsubscribe();
        stationIdEvenSubscriber.unsubscribe();
        super.onDestroy();
    }

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //region IMPLEMENTATION  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //region LISTENERS  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private class ActionBarDrawerListener extends ActionBarDrawerToggle {
        private ActionBarDrawerListener() {
            super(MainActivity.this, drawerLayout, R.string.drawer_open,
                    R.string.drawer_close);
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);

            if (pendingId != null) {
                navigateToMainFragmentWithId(pendingId, false);
                pendingId = null;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                if (drawerLayout.isDrawerOpen(Gravity.START)) {
                    drawerLayout.closeDrawer(Gravity.START);
                } else {
                    drawerLayout.openDrawer(Gravity.START);
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


}
