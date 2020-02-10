package com.ieltstutorials.mylibrary;

import android.app.Activity;
import android.content.Intent;

import com.ieltstutorials.mylibrary.Gallery.ActivityGallery;

public class PubFun {

    static PubFun pubFun;
    public static ButtonClick click;

    public static PubFun getPubFun() {
        if (pubFun == null) {
            pubFun = new PubFun();
        }
        return pubFun;

    }

    public String getData() {
        return "Krunal";
    }

    public void startActivity(ButtonClick click, Activity activity) {
        this.click = click;
        activity.startActivity(new Intent(activity, ActivityGallery.class));
    }
}
