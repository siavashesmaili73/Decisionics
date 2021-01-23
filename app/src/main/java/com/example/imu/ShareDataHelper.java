package com.example.imu;

import android.content.Context;
import android.content.Intent;

public class ShareDataHelper {

    public static void shareData(Context context, String body){

        try{
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject here");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
            context.startActivity(Intent.createChooser(sharingIntent, "Sharing Option"));

        }catch (Exception e){
            e.printStackTrace();

        }
    }
}
