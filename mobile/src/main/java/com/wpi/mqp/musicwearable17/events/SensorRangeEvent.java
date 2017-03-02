package com.wpi.mqp.musicwearable17.events;

import com.wpi.mqp.musicwearable17.data.Sensor;

public class SensorRangeEvent {
    private Sensor sensor;

    public SensorRangeEvent(Sensor sensor) {
        this.sensor = sensor;
    }

    public Sensor getSensor() {
        return sensor;
    }
}
