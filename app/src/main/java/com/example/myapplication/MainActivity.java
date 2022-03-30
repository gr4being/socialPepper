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
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.simple.parser.JSONParser;


import java.io.FileReader;
import java.io.IOException;
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
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.locale.Language;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.aldebaran.qi.sdk.object.locale.Region;
import com.aldebaran.qi.sdk.util.PhraseSetUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        // get dialogs from file
        JSONParser parser = new JSONParser();
        String next = "start";
        String text;
        String event;
        JSONArray answers;
        JSONArray animationfiles;
        JSONObject texts;
        JSONObject displaytexts;
        String displaytext;
        String action;
        try {
            JSONObject dialogsObj = (JSONObject) parser.parse(new FileReader("./dialogs.json"));
            JSONObject actionObj = (JSONObject) dialogsObj.get(next);
            String type = (String) actionObj.get("type");
            switch(type){
                case "talk":
                    texts = (JSONObject) actionObj.get("texts");
                    text = (String) texts.get(profile.formality());
                    say(qiContext,text);
                    break;
                case "question":
                    texts = (JSONObject) actionObj.get("texts");
                    text = (String) texts.get(profile.formality());
                    answers = (JSONArray) actionObj.get("answers");
                    question(qiContext,text,answers);
                    break;
                case "animation":
                    animationfiles = (JSONArray) actionObj.get("filename");
                    animation(qiContext,animationfiles);
                    break;
                case "display":
                    displaytexts = (JSONObject) actionObj.get("texts");
                    displaytext = (String) displaytexts.get(profile.formality());
                    display(qiContext,displaytext);
                    break;
                case "action":
                    action = (String) actionObj.get("action");
                    switch(action){
                        case "faq":
                            faq();
                            break;
                        case "tictactoe":
                            tictacttoe();
                            break;
                        default:
                            say(qiContext,"es scheint so als wäre ein Problem aufgetreten");
                            break;
                    }
                case "eventwait":
                    event = (String) actionObj.get("event");
                    eventwait();
                    break;
                default:
                    say(qiContext,"es scheint so als wäre ein Problem aufgetreten");
                    break;

            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        // Build the action.
        Listen listen = ListenBuilder.with(qiContext)
                .withPhraseSet(phraseSet)
                .build();

        // Run the action synchronously.
        ListenResult listenResult =  listen.run();

        Log.i("TAG", "Heard phrase: " + listenResult.getHeardPhrase().getText());

        Locale locale = new Locale(Language.GERMAN, Region.GERMANY);
        // Pepper Sprachausgabe (nur Sprechblase im Simulator)
        Say sayActionFuture = SayBuilder.with(qiContext)
                .withText(listenResult.getHeardPhrase().getText())
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

    public void say(QiContext qiContext, String string){
        Locale locale = new Locale(Language.GERMAN, Region.GERMANY);

        Say sayActionFuture = SayBuilder.with(qiContext)
                .withText(string)
                .withLocale(locale)
                .build();
        sayActionFuture.run();
    }
    public int question(QiContext qiContext,String text,JSONArray answers ) {
        JSONArray answer;
        Listen listen;
        ListenResult listenResult;
        int answersetnum = 0;
        PhraseSet set1;
        PhraseSet set2;
        PhraseSet set3;
        PhraseSet set4;
        int listlength = answers.length();
        String[] words = {};
        if (listlength > 0) {
            try {
                answer = (JSONArray) answers.get(0);
                for (int i = 0; i < answer.length(); i++) {
                    words[i] = (String) answer.get(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            set1 = PhraseSetBuilder.with(qiContext)
                    .withTexts(words)
                    .build();
            Arrays.fill(words, null);
            if (listlength > 1) {
                try {
                    answer = (JSONArray) answers.get(1);
                    for (int i = 0; i < answer.length(); i++) {
                        words[i] = (String) answer.get(i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                set2 = PhraseSetBuilder.with(qiContext)
                        .withTexts(words)
                        .build();
                Arrays.fill(words, null);
                if (listlength > 2) {
                    try {
                        answer = (JSONArray) answers.get(2);
                        for (int i = 0; i < answer.length(); i++) {
                            words[i] = (String) answer.get(i);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    set3 = PhraseSetBuilder.with(qiContext)
                            .withTexts(words)
                            .build();
                    Arrays.fill(words, null);
                    if (listlength == 4) {
                        try {
                            answer = (JSONArray) answers.get(3);
                            for (int i = 0; i < answer.length(); i++) {
                                words[i] = (String) answer.get(i);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        set4 = PhraseSetBuilder.with(qiContext)
                                .withTexts(words)
                                .build();
                        Arrays.fill(words, null);
                    } else {
                        say(qiContext, "es scheint so als wäre ein Problem aufgetreten");
                    }
                }
            }
        }

        switch (listlength){
            case 1:
                listen = ListenBuilder.with(qiContext)
                        .withPhraseSets(set1)
                        .build();

                listenResult = listen.run();
                break;
            case 2:
                listen = ListenBuilder.with(qiContext)
                        .withPhraseSets(set1,set2)
                        .build();

                listenResult = listen.run();
                break;
            case 3:
                listen = ListenBuilder.with(qiContext)
                        .withPhraseSets(set1,set2,set3)
                        .build();

                listenResult = listen.run();
                break;
            case 4:
                listen = ListenBuilder.with(qiContext)
                        .withPhraseSets(set1,set2,set3,set4)
                        .build();

                listenResult = listen.run();
                break;
            default:
                say(qiContext,"hmmmmmmm");
        }
        Phrase heardPhrase = listenResult.getHeardPhrase();
        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();
        if(matchedPhraseSet==set1){
            answersetnum = 0;
        }
        if(matchedPhraseSet==set2){
            answersetnum = 1;
        }
        if(matchedPhraseSet==set3){
            answersetnum = 2;
        }
        if(matchedPhraseSet==set4){
            answersetnum = 3;
        }
        return answersetnum;

    }

    public void display(QiContext qiContext,String displaytext ){

    }

    public void greeting(QiContext qiContext, Profile profile,  JSONObject dialogs){

        String greeting;
        String RobotName = "Alpha";

        PhraseSet phraseSetPositiv = PhraseSetBuilder.with(qiContext)
                .withTexts("gut", "schön")
                .build();

        Listen listen = ListenBuilder.with(qiContext)
                .withPhraseSet(phraseSetPositiv)
                .build();

        ListenResult listenResult = listen.run();

        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();



        if(PhraseSetUtil.equals(matchedPhraseSet, phraseSetPositiv)){
            say(qiContext, (String) dialogs.get("greetingPositiv")[profile.respect]);
        }else{
            say(qiContext, dialogs["greetingNormal"][profile.respect]);
        }







    }

    public static ArrayList<Map<String, Object>> faqHandler(JSONObject parsed, int[] foundKeys){
        ArrayList<Map<String, Object>> rated_questions = new ArrayList<Map<String, Object>>();
        ArrayList<Double> weighted_keysums = new ArrayList<Double>;
        ArrayList<Double> frequencies = new ArrayList<Double>;
        /*
        for(int questionNr = 0; questionNr < keyweights.length; questionNr++){
            double sum = 0;
            for(int keyWord = 0; keyWord < keyList.length; keyWord++){
                sum += keyweights[questionNr][keyWord] * foundKeys[keyWord];
            }
            understand[questionNr] = sum;
        }
*/
        // calculate weighted_keysums
        for (Map<String, Object> question : parsed["questions"]) {
            double sum = 0;
            for(int keyword : parsed["keywords"]){
                Int kw_idx = parsed["keywords"].index(keyword);
                sum += question["keyweights"][kw_idx] * foundKeys[kw_idx];
            }
            weighted_keysums.add(sum);
        }

        // calculate frequencies
        int total_count = 0;
        for (Map<String, Object> question : parsed["questions"]) {
            total_count += question["count"];
        }
        for (Map<String, Object> question : parsed["questions"]) {
            double freq;
            freq = question["count"] / total_count;
            frequencies.add(freq * parsed["questions"].length); // correction with number of questions
        }

        rated_questions = rating(parsed["questions"], weighted_keysums, frequencies);

        return rated_questions;
    }

    public static ArrayList<Map<String, Object>> rating(JSONObject questions, ArrayList<Double> weighted_keysums, ArrayList<Double> frequencies) {
        // double freqParameter = 0.5;
        ArrayList<Map<String, Object>> rated_questions;
        /*double[] rating = new double[understand.length];
        for (int i = 0; i < understand.length; i++) {
            rating[i] = understand[i] + freqParameter * frequency[i];
        }*/

        for (int i = 0; i < questions.length(); i++) {
            double rating = weighted_keysums[i] + frequencies[i];
            rated_questions.add(Map.of("question", questions[i]["question"], "answer", questions[i]["answer"], "rating", rating);
        }
        return rated_questions;
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

