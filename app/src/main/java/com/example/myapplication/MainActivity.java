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
import java.util.HashMap;
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
                            say(qiContext,"es scheinnt so als wäre ein Problem aufgetreten");
                            break;
                    }
                case "eventwait":
                    event = (String) actionObj.get("event");
                    eventwait();
                    break;
                default:
                    say(qiContext,"es scheinnt so als wäre ein Problem aufgetreten");
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
    public int question(QiContext qiContext,String text,JSONArray answers ){
        int listlength = answers.length();
        if(listlength>0){
            PhraseSet set1 = PhraseSetBuilder.with(qiContext)
                    .withTexts()
                    .build();
            if(listlength>1){
                PhraseSet set2 = PhraseSetBuilder.with(qiContext)
                        .withTexts("Hello", "Hi")
                        .build();
                if(listlength>2){
                    PhraseSet set3 = PhraseSetBuilder.with(qiContext)
                            .withTexts("Hello", "Hi")
                            .build();
                    if(listlength>3){
                        PhraseSet set4 = PhraseSetBuilder.with(qiContext)
                                .withTexts("Hello", "Hi")
                                .build();
                    }
                }
            }
        }



        Listen listen = ListenBuilder.with()
                .withPhraseSets(forwards, backwards, stop)
                .build();

        ListenResult listenResult = listen.run();

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

    public static ArrayList<Map<String, Object>> faqHandler(JSONObject questionsJSON, int[] foundKeys){
        ArrayList<Map<String, Object>> rated_questions = new ArrayList<Map<String, Object>>();
        ArrayList<Double> weighted_keysums = new ArrayList<Double>();
        ArrayList<Double> frequencies = new ArrayList<Double>();
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
        try {
            JSONArray questions = (JSONArray) questionsJSON.get("questions");
            JSONArray keywords = (JSONArray) questionsJSON.get("keywords");
            for (int i = 0; i < questions.length(); i++) {
                double sum = 0;
                JSONObject question = (JSONObject) questions.get(i);
                for (int j = 0; j < keywords.length(); j++) {
                    JSONArray keyweights = (JSONArray) question.get("keyweights");
                    sum += keyweights.getInt(j) * foundKeys[j];
                }
                weighted_keysums.add(sum);
            }

            // calculate frequencies
            int total_count = 0;
            for (int i = 0; i < questions.length(); i++) {
                JSONObject question = (JSONObject) questions.get(i);
                total_count += question.getInt("count");
            }
            for (int i = 0; i < questions.length(); i++) {
                double freq;
                JSONObject question = (JSONObject) questions.get(i);
                freq = question.getInt("count") / total_count;
                frequencies.add(freq * questions.length()); // correction with number of questions
            }

            rated_questions = rating(questions, weighted_keysums, frequencies);

        } catch (JSONException e) {
            Log.e("MYAPP", "unexpected JSON exception", e);
        }

        return rated_questions;
    }

    public static ArrayList<Map<String, Object>> rating(JSONArray questions, ArrayList<Double> weighted_keysums, ArrayList<Double> frequencies) {
        // double freqParameter = 0.5;
        ArrayList<Map<String, Object>> rated_questions = null;

        try {
            for (int i = 0; i < questions.length(); i++) {
                double rating = weighted_keysums.get(i) + frequencies.get(i);
                HashMap<String, Object> rated_question = new HashMap();
                JSONObject question = (JSONObject) questions.get(i);
                rated_question.put("question", question.getString("question"));
                rated_question.put("answer", question.getString("answer"));
                rated_question.put("rating", rating);
                rated_questions.add(rated_question);
            }

        } catch (JSONException e) {
            Log.e("MYAPP", "unexpected JSON exception", e);
        }

        // sort rated_questions
        for (int i = 0; i < rated_questions.size(); i++) {
            // Inner nested loop pointing 1 index ahead
            for (int j = i + 1; j < rated_questions.size(); j++) {
                // Checking elements
                HashMap<String, Object> temp = new HashMap<String, Object>();
                HashMap<String, Object> first_q = (HashMap<String, Object>) rated_questions.get(j);
                int first_r = (Integer) first_q.get("rating");
                HashMap<String, Object> second_q = (HashMap<String, Object>) rated_questions.get(i);
                int second_r = (Integer) second_q.get("rating");
                if (first_r < second_r) {
                    // Swapping
                    temp = (HashMap<String, Object>) rated_questions.get(i);
                    rated_questions.set(i, rated_questions.get(j));
                    rated_questions.set(j, temp);
                }
            }
        }
        return rated_questions;
    }
}

