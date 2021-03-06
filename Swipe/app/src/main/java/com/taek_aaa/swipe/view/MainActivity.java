package com.taek_aaa.swipe.view;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.RemoteViews;

import com.taek_aaa.swipe.R;
import com.taek_aaa.swipe.SwipeSensorService;
import com.taek_aaa.swipe.controller.DataController;
import com.taek_aaa.swipe.controller.NotificationController;
import com.taek_aaa.swipe.controller.ShutdownAdminReceiver;

import static com.taek_aaa.swipe.R.color.headColor;

public class MainActivity extends AppCompatActivity {

    public static Sensor sensor;
    public static SensorManager sensorManager;
    public static PowerManager.WakeLock wakeLock;
    public static DevicePolicyManager devicePolicyManager;
    public static SwitchCompat sensorSwitch;
    public static int headColorThem;
    DataController dataController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getAuthority();
        init();
        drawWindowTheme();
        startSensor();
        registSensorListener();

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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            case R.id.howToUse:

                break;
            case R.id.contactUs:
                ContactUsDialog contactUsDialog = new ContactUsDialog(this);
                contactUsDialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
        switch (dataController.getPreferencesIsStart(getBaseContext())) {
            case 0:
                sensorSwitch.setChecked(false);
                break;
            case 1:
                sensorSwitch.setChecked(true);
                break;
        }
    }

    private void drawWindowTheme() {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(headColorThem);
        }
    }

    private void registSensorListener() {

        sensorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent timerIntent = new Intent(MainActivity.this, SwipeSensorService.class);

                if (isChecked) {
                    Snackbar.make(buttonView, "Swipe를 실행합니다.", Snackbar.LENGTH_LONG).setAction("ACTION", null).show();
                    dataController.setPreferencesIsStart(getBaseContext(), 1);
                    NotificationController notificationController = new NotificationController();
                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    Notification.Builder builder = new Notification.Builder(getApplicationContext());

                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent pi = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.view_notification);


                    notificationController.startNotification(nm, builder, pi, contentView, getBaseContext());
                    startService(timerIntent);
                } else {
                    Snackbar.make(buttonView, "Swipe를 종료합니다.", Snackbar.LENGTH_LONG).setAction("ACTION", null).show();
                    dataController.setPreferencesIsStart(getBaseContext(), 0);
                    stopService(timerIntent);
                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    nm.cancel(1);

                }
            }
        });
    }

    private void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dataController = new DataController();
        sensorSwitch = (SwitchCompat) findViewById(R.id.switchButton);
        headColorThem = getResources().getColor(headColor);
        drawSensorChecked();
    }

    private void drawSensorChecked() {
        switch (dataController.getPreferencesIsStart(getBaseContext())) {
            case 0:
                sensorSwitch.setChecked(false);
                break;
            case 1:
                sensorSwitch.setChecked(true);
                break;
        }
    }

    private void getAuthority() {
        ComponentName comp = new ComponentName(this, ShutdownAdminReceiver.class);
        devicePolicyManager = (DevicePolicyManager) getApplicationContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (!devicePolicyManager.isAdminActive(comp)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, comp);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "message string");
            startActivityForResult(intent, 101);
        }
    }

    private void startSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

}
