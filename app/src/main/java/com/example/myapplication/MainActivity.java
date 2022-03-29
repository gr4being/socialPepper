package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import androidx.core.app.ActivityCompat;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.locale.Language;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.aldebaran.qi.sdk.object.locale.Region;

import java.util.ArrayList;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private Activity mainActivity;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);
        mainActivity=this;

        setContentView(R.layout.activity_main); //setzt die Anzeigefläche: res->layout->activity_main.xml

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);

    }


    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    //Das macht der Pepper sobald das Programm startet
    public void onRobotFocusGained(QiContext qiContext) {

        Locale locale = new Locale(Language.GERMAN, Region.GERMANY);
        // Pepper Sprachausgabe (nur Sprechblase im Simulator)
        Say sayActionFuture = SayBuilder.with(qiContext)
                .withText("Hallo und Herzlich Willkommen bei BeIng 2022! Wie kann ich Ihnen helfen?")
                .withLocale(locale)
                .build();
        sayActionFuture.run();

        //Animationen aus Ressourcen abspielen
        Animation animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.elephant_a001) // Diese kann durch beliebige andere Animationen ersetzt werden
                .build();
        AnimateBuilder.with(qiContext)
                .withAnimation(animation)
                .build().run();
    }

    public void onRobotFocusLost() {
        // The robot focus is lost.
    }

    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }



    public static double[] faqHandler(String[] keyList, double[] foundKeys, double[][] keyweights){
        double[] understand = new double[keyweights.length];
        for(int questionNr = 0; questionNr < keyweights.length; questionNr++){
            double sum = 0;
            for(int keyWord = 0; keyWord < keyList.length; keyWord++){
                sum += keyweights[questionNr][keyWord] * foundKeys[keyWord];
            }
            understand[questionNr] = sum;
        }
        return understand;
    }

    public static double[] rating(double[] understand, double[] frequency) {
        double frequParameter = 0.5;
        double[] rating = new double[understand.length];
        for (int i = 0; i < understand.length; i++) {
            rating[i] = understand[i] + frequParameter * frequency[i];
        }
        return rating;
    }

    public static int[] sortedIndices(double[] originalArray)
    {
        int len = originalArray.length;
        double[] sortedCopy = originalArray.clone();
        int[] indices = new int[len];
        Arrays.sort(sortedCopy);
        for (int index = 0; index < len; index++)
            indices[index] = Arrays.binarySearch(sortedCopy, originalArray[index]);
        return indices;
    }
}

