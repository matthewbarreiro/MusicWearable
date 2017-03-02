package com.wpi.mqp.musicwearable17.events;

import com.wpi.mqp.musicwearable17.data.Sensor;

public class NewSensorEvent {
    private Sensor sensor;

    public NewSensorEvent(Sensor sensor) {
        this.sensor = sensor;
    }

    public Sensor getSensor() {
        return sensor;
    }
}
