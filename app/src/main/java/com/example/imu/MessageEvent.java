package com.example.imu;

public class MessageEvent {

    private float[] uuid;
    private float[] value;

    public MessageEvent(float[] uuid, float[] value) {
        this.uuid = uuid;
        this.value = value;

    }

    public float[] getValue() {
        return value;
    }

    public float[] getUuid() {
        return uuid;
    }
}
