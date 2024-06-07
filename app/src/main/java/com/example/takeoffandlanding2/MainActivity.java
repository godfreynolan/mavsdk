package com.example.takeoffandlanding2;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import io.mavsdk.System;
import io.mavsdk.mavsdkserver.MavsdkServer;
import io.mavsdk.telemetry.Telemetry;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private MavsdkServer mMavsdkServer;
    private static final String MAVSDK_SERVER_IP = "127.0.0.1";
    private System mDrone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnServer = findViewById(R.id.btn_server);
        Button btnTakeOff = findViewById(R.id.btn_takeoff);
        Button btnLand = findViewById(R.id.btn_land);
        Button btnAltitude = findViewById(R.id.btn_altitude);

        btnServer.setOnClickListener(v -> initializeServerAndDrone());

        btnTakeOff.setOnClickListener(v -> takeOff());
        btnLand.setOnClickListener(v -> land());

        btnAltitude.setOnClickListener(v -> getAltitude());

    }

    private void initializeServerAndDrone() {
        mMavsdkServer = new MavsdkServer();
        int mavsdkServerPort = mMavsdkServer.run("udp://:14550");
        mDrone = new System(MAVSDK_SERVER_IP, mavsdkServerPort);
    }

    private void takeOff() {
        mDrone.getAction().arm().andThen(mDrone.getAction().takeoff()).subscribe();
    }

    private void land() {
        mDrone.getAction().arm().andThen(mDrone.getAction().land()).subscribe();
    }

    private void getAltitude() {
        // get the altitude of the drone
        Flowable<Float> altitude = mDrone.getTelemetry().getPosition()
                .map(new Function<Telemetry.Position, Float>() {
                    @Override
                    public Float apply(Telemetry.Position position) throws Exception {
                        return position.getRelativeAltitudeM();
                    }
                });
        // subscribe to the altitude flowable
        altitude.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single())
                .subscribe(new io.reactivex.functions.Consumer<Float>() {
                    @Override
                    public void accept(Float aFloat) throws Exception {
                        Log.d("Altitude", "Altitude: " + aFloat);
                    }
                });
    }
}