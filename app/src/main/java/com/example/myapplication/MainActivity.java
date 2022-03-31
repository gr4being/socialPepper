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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import androidx.core.app.ActivityCompat;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.ApproachHumanBuilder;
import com.aldebaran.qi.sdk.builder.EngageHumanBuilder;
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
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.humanawareness.ApproachHuman;
import com.aldebaran.qi.sdk.object.humanawareness.EngageHuman;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.aldebaran.qi.sdk.object.locale.Language;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.aldebaran.qi.sdk.object.locale.Region;
import com.aldebaran.qi.sdk.object.touch.Touch;
import com.aldebaran.qi.sdk.object.touch.TouchSensor;
import com.aldebaran.qi.sdk.util.PhraseSetUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private Activity mainActivity;
    public String[] a_ar = new String[]{};
    private boolean wait = false;

    JSONObject questionsObj = null;
    private TouchSensor headTouchSensor;
    JSONObject dialogsObj = null;
    String jsonString = null;
    Boolean humanEnganged = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);
        mainActivity=this;

        String[] ar = new String[]{"a", "b", "c", "d", "e"};
        a_ar = new String[]{"no", "yes", "etc", "idgaf", "sure m8"};

        setContentView(R.layout.activity_main); //setzt die Anzeigefläche: res->layout->activity_main.xml

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);
    }

    private void eventwait () {
        wait = true;
        while (wait){
            Log.i("evenwati", "waiting");
        }
    }


    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    public void onRobotFocusGained(QiContext qiContext) {
        Touch touch = qiContext.getTouch();
        headTouchSensor = touch.getSensor("Head/Touch");
        headTouchSensor.addOnStateChangedListener(touchState -> {
            Log.i("ttt", "Sensor " + (touchState.getTouched() ? "touched" : "released") + " at " + touchState.getTime());
            wait = false;
        });

        try {
            InputStream is = getApplicationContext().getAssets().open("robot/questions.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, "UTF-8");
            questionsObj = new JSONObject(jsonString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            InputStream is = getApplicationContext().getAssets().open("robot/dialogs.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, "UTF-8");
            dialogsObj = new JSONObject(jsonString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HumanAwareness humanAwareness = qiContext.getHumanAwareness();
        humanAwareness.addOnEngagedHumanChangedListener(human -> {
            if (human != null) {
                humanEnganged = true;
                // Human recommendedHuman = humanAwareness.getRecommendedHumanToApproach();
                ApproachHuman approachHuman = ApproachHumanBuilder.with(qiContext)
                        .withHuman(human)
                        .build();





                approachHuman.async().run();
                EngageHuman engageHuman = EngageHumanBuilder.with(qiContext)
                        .withHuman(human)
                        .build();
                engageHuman.async().run();
                engageHuman.addOnHumanIsDisengagingListener(() -> {
                    say_sync(qiContext, "Tschüss!");
                    //engagement.requestCancellation();
                });
                Profile profile = new Profile();
                profile.age = human.getEstimatedAge().getYears();
                conversation(qiContext, profile);
            } else {
                humanEnganged = false;
                standby(qiContext);
            }
        });

        Button question_button_1 = (Button) findViewById(R.id.btn_question_1);
        question_button_1.setOnClickListener(new View.OnClickListener() {
            public void onClick (View view) {
                Log.i("aa", "button question 1 clicked");
                say_async(qiContext, a_ar[0]);
                changeText(a_ar[0]);
            }
        });
        Button question_button_2 = (Button) findViewById(R.id.btn_question_2);
        question_button_2.setOnClickListener(new View.OnClickListener() {
            public void onClick (View view) {
                Log.i("aa", "button question 2 clicked");
                say_async(qiContext, a_ar[1]);
                changeText(a_ar[1]);
            }
        });
        Button question_button_3 = (Button) findViewById(R.id.btn_question_3);
        question_button_3.setOnClickListener(new View.OnClickListener() {
            public void onClick (View view) {
                Log.i("aa", "button question 3 clicked");
                say_async(qiContext, a_ar[2]);
                changeText(a_ar[2]);
            }
        });
        Button question_button_4 = (Button) findViewById(R.id.btn_question_4);
        question_button_4.setOnClickListener(new View.OnClickListener() {
            public void onClick (View view) {
                Log.i("aa", "button question 4 clicked");
                say_async(qiContext, a_ar[3]);
                changeText(a_ar[3]);
            }
        });
        Button question_button_5 = (Button) findViewById(R.id.btn_question_5);
        question_button_5.setOnClickListener(new View.OnClickListener() {
            public void onClick (View view) {
                Log.i("aa", "button question 5 clicked");
                say_async(qiContext, a_ar[4]);
                changeText(a_ar[4]);
            }
        });
        Button back_button = (Button) findViewById(R.id.btn_back);
        back_button.setOnClickListener(new View.OnClickListener() {
            public void onClick (View view) {
                Log.i("aa", "back clicked");
                JSONObject action = null;
                try { action = dialogsObj.getJSONObject(next); } catch (JSONException e) { e.printStackTrace(); }
                try { next = action.getString("before"); } catch (JSONException e) { e.printStackTrace(); }
            }
        });
    }
    String next;

    public void conversation (QiContext qiContext, Profile profile) {

        // get dialogs from file
        next = "start";
        String type;
        JSONArray then;
        JSONArray textSet;
        String event;
        JSONArray answers;
        JSONObject texts;
        JSONObject displaytexts;
        String displaytext;
        String action;
        int rnd;
        Boolean conversation_finished = false;
        while (!conversation_finished) {
            try {
                JSONObject actionObj = (JSONObject) dialogsObj.get(next);

                then = actionObj.getJSONArray("then");
                next = then.getString(0);
                type = (String) actionObj.get("type");
                switch (type) {
                    case "talk":
                        texts = (JSONObject) actionObj.get("texts");
                        textSet = (JSONArray) texts.get(profile.formality());
                        rnd = new Random().nextInt(textSet.length());
                        say_sync(qiContext, textSet.getString(rnd));
                        break;
                    case "question":
                        texts = (JSONObject) actionObj.get("texts");
                        textSet = (JSONArray) texts.get(profile.formality());
                        rnd = new Random().nextInt(textSet.length());
                        answers = (JSONArray) actionObj.get("answers");
                        int index = question(qiContext, textSet.getString(rnd), answers);
                        next = then.getString(index);
                        break;
                    case "animation":
                        JSONObject animationfiles = (JSONObject) actionObj.get("filename");
                        animation(qiContext, animationfiles.getString(profile.formality()));
                        break;
                    case "display":
                        displaytext = (String) actionObj.getString("texts");
                        //displaytext = (String) displaytexts.get(profile.formality());
                        display(qiContext, displaytext);
                        break;
                    case "action":
                        action = (String) actionObj.get("action");
                        switch (action) {
                            case "faq":
                                faq(qiContext);
                                break;
                            case "tictactoe":
                                //tictacttoe(); never gonna happen
                                break;
                            default:
                                say_sync(qiContext, "es scheint so als wäre ein Problem aufgetreten");
                                break;
                        }
                    case "eventwait":
                        event = (String) actionObj.get("event");
                        eventwait();
                        break;
                    default:
                        say_sync(qiContext, "es scheint so als wäre ein Problem aufgetreten");
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void standby(QiContext qiContext) {
        String[] standbyAnimations = {"stand1", "stand2", "stand3"};
        int rnd = new Random().nextInt(standbyAnimations.length);
        animation(qiContext, standbyAnimations[rnd]);

    }

    public void onRobotFocusLost() {
        // The robot focus is lost.
    }

    public void onRobotFocusRefused(String reason){
            // The robot focus is refused.
    }

    public int question(QiContext qiContext,String text,JSONArray answers ) {
        say_async(qiContext,text);
        int index = 0;
        ArrayList<String[]> words = new ArrayList<String[]>();
        for(int i=0;i<answers.length();i++){
            try {
                words.add((String[]) answers.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        List<PhraseSet> keywordsAsSets = new ArrayList<PhraseSet>();
        for (int k=0; k<answers.length(); k++) {
            keywordsAsSets.add(PhraseSetBuilder.with(qiContext)
                    .withTexts(words.get(k))
                    .build());
        }
        Listen listen = ListenBuilder.with(qiContext)
                .withPhraseSets(keywordsAsSets)
                .build();
        ListenResult listenResult = listen.run();

        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();
        //int[] heard_keys = new int[answers.length()];
        for (int i = 0; i < answers.length(); i++) {
            if (matchedPhraseSet.equals(keywordsAsSets.get(i))) {
                index = i; //momentan ist es nur möglich, dass ein einzelnes Keyword erkannt wird.
                //mit einem anderen Sprachinterpreten könnten mehrere Keywörter erkannt werden und
            } else {
                index = 0;
            }
        }
        return index;
    }

    private void say_async(QiContext qiContext, String text){
        Locale locale = new Locale(Language.GERMAN, Region.GERMANY);
        Phrase phrase = new Phrase(text);
        Future<Say> sayBuilding = SayBuilder.with(qiContext)
                .withPhrase(phrase)
                .withLocale(locale)
                .buildAsync();
        Future<Void> sayActionFuture = sayBuilding.andThenCompose(say_async -> say_async.async().run());
    }

    public void say_sync(QiContext qiContext, String text){
        Locale locale = new Locale(Language.GERMAN, Region.GERMANY);
        Phrase phrase = new Phrase(text);
        Say sayActionFuture = SayBuilder.with(qiContext)
                .withPhrase(phrase)
                .withLocale(locale)
                .build();
        sayActionFuture.run();
    }

    private void changeText(String text){
        TextView textView  = findViewById(R.id.mytextview_id);
        textView.setText(text);
    }

    public void display(QiContext qiContext,String displayText ){
        View a = findViewById(R.id.btn_question_1);
        a.setVisibility(View.INVISIBLE);
        View b = findViewById(R.id.btn_question_2);
        b.setVisibility(View.INVISIBLE);
        View c = findViewById(R.id.btn_question_3);
        c.setVisibility(View.INVISIBLE);
        View d = findViewById(R.id.btn_question_4);
        d.setVisibility(View.INVISIBLE);
        View e = findViewById(R.id.btn_question_5);
        e.setVisibility(View.INVISIBLE);

        changeText(displayText);
    }

    public void update_keyweights(QiContext qiContext, String question_text, double[] heard_keys, Boolean correct) {
        double learning_rate = 0.4;
        int sign = 0;
        if (correct) {
            sign = 1;
        } else {
            sign = -1;
        }
        try {
            JSONArray questionsArr = (JSONArray) questionsObj.get("questions");

            JSONObject newquestionsObj = new JSONObject();
            JSONArray newquestionsArr = new JSONArray();

            newquestionsObj.put("keywords", questionsObj.get("keywords"));


            for (int i = 0; i < questionsArr.length(); i++) {
                JSONObject question = (JSONObject) questionsArr.get(i);
                if (question.getString("question") == question_text) {

                    JSONArray keyweights = question.getJSONArray("keyweights");
                    JSONArray newkeyweights = new JSONArray();

                    int sum_keyprobs_over05 = 0;
                    int sum_keyprobs_under05 = 0;

                    for (int j = 0; j < keyweights.length(); j++) {
                        if (heard_keys[j] > 0.5) {
                            sum_keyprobs_over05 += heard_keys[j];
                        } else {
                            sum_keyprobs_under05 -= heard_keys[j] - 1;
                        }
                    }

                    for (int j = 0; j < keyweights.length(); j++) {

                        if (heard_keys[j] > 0.5) {
                            newkeyweights.put(j, keyweights.getDouble(j) + sign * (heard_keys[j] / sum_keyprobs_over05) * learning_rate);
                        } else {
                            newkeyweights.put(j, keyweights.getDouble(j) + sign * -1 * (heard_keys[j] / sum_keyprobs_under05) * learning_rate);
                        }
                    }

                    JSONObject newquestion = new JSONObject();

                    newquestion.put("question", question.getString("question"));
                    newquestion.put("answer", question.getString("answer"));
                    newquestion.put("count", question.getInt("count"));
                    newquestion.put("keyweights", newkeyweights);

                    newquestionsArr.put(newquestion);
                } else {
                    newquestionsArr.put(question);
                }
            }

            newquestionsObj.put("questions", newquestionsArr);

            FileWriter file; // public static
            file = new FileWriter("./questions.json");
            file.write(newquestionsObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Map<String, Object>> faqHandler(double[] heard_keys){
        ArrayList<Map<String, Object>> rated_questions = new ArrayList<Map<String, Object>>();
        ArrayList<Double> weighted_keysums = new ArrayList<Double>();
        ArrayList<Double> frequencies = new ArrayList<Double>();

        // calculate weighted_keysums
        try {
            JSONArray questions = (JSONArray) questionsObj.getJSONArray("questions");
            JSONArray keywords = (JSONArray) questionsObj.getJSONArray("keywords");
            for (int i = 0; i < questions.length(); i++) {
                double sum = 0;
                JSONObject question = (JSONObject) questions.get(i);
                for (int j = 0; j < keywords.length(); j++) {
                    JSONArray keyweights = (JSONArray) question.get("keyweights");
                    sum += keyweights.getInt(j) * heard_keys[j];
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

            rated_questions = rating(weighted_keysums, frequencies);

        } catch (JSONException e) {
            Log.e("MYAPP", "unexpected JSON exception", e);
        }

        return rated_questions;
    }

    public ArrayList<Map<String, Object>> rating(ArrayList<Double> weighted_keysums, ArrayList<Double> frequencies) {
        // double freqParameter = 0.5;
        ArrayList<Map<String, Object>> rated_questions = null;

        try {
            JSONArray questions = questionsObj.getJSONArray("questions");
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

    public void animation(QiContext qiContext, String animationName){
        Animation fist = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.fist) // Set the animation resource.
                .build(); // Build the animation.
        Animation handshake = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.handshake) // Set the animation resource.
                .build(); // Build the animation.
        Animation nod_no = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.nod_no) // Set the animation resource.
                .build(); // Build the animation.
        Animation nod_yes = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.nod_yes) // Set the animation resource.
                .build(); // Build the animation.
        Animation show_hand = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.show_hand) // Set the animation resource.
                .build(); // Build the animation.
        Animation show_screen = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.show_screen) // Set the animation resource.
                .build(); // Build the animation.
        Animation show_self = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.show_self) // Set the animation resource.
                .build(); // Build the animation.
        Animation show_user = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.show_user) // Set the animation resource.
                .build(); // Build the animation.
        Animation skispringer = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.skispringer) // Set the animation resource.
                .build(); // Build the animation.
        Animation stand1 = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.stand1) // Set the animation resource.
                .build(); // Build the animation.
        Animation stand2 = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.stand2) // Set the animation resource.
                .build(); // Build the animation.
        Animation stand3 = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.stand3) // Set the animation resource.
                .build(); // Build the animation.
        Animation thinking = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.thinking) // Set the animation resource.
                .build(); // Build the animation.
        Animation verbeugen = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.verbeugen) // Set the animation resource.
                .build(); // Build the animation.
        Animation wave = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.wave) // Set the animation resource.
                .build(); // Build the animation.

        switch(animationName){
            case "fist":
                AnimateBuilder.with(qiContext) // Create the builder with the context.
                        .withAnimation(fist) // Set the animation.
                        .build().async().run(); // Build the animate action and run it
                break;
            case "handshake":
                AnimateBuilder.with(qiContext) // Create the builder with the context.
                        .withAnimation(handshake) // Set the animation.
                        .build().async().run(); // Build the animate action and run it
                break;
            case "nod_no":
                AnimateBuilder.with(qiContext) // Create the builder with the context.
                        .withAnimation(nod_no) // Set the animation.
                        .build().async().run(); // Build the animate action and run it
                break;
            case "nod_yes":
                AnimateBuilder.with(qiContext) // Create the builder with the context.
                        .withAnimation(nod_yes) // Set the animation.
                        .build().async().run(); // Build the animate action and run it
                break;
            case "show_hand":
                AnimateBuilder.with(qiContext) // Create the builder with the context.
                        .withAnimation(show_hand) // Set the animation.
                        .build().async().run(); // Build the animate action and run it
                break;
            case "show_screen":
                AnimateBuilder.with(qiContext) // Create the builder with the context.
                        .withAnimation(show_screen) // Set the animation.
                        .build().async().run(); // Build the animate action and run it
            case "show_self":
                AnimateBuilder.with(qiContext) // Create the builder with the context.
                        .withAnimation(show_self) // Set the animation.
                        .build().async().run(); // Build the animate action and run it
            case "show_user":
                AnimateBuilder.with(qiContext) // Create the builder with the context.
                        .withAnimation(show_user) // Set the animation.
                        .build().async().run(); // Build the animate action and run it

            case "skispringer":
                AnimateBuilder.with(qiContext) // Create the builder with the context.
                        .withAnimation(skispringer) // Set the animation.
                        .build().async().run(); // Build the animate action and run it
            case "stand1":
                AnimateBuilder.with(qiContext) // Create the builder with the context.
                        .withAnimation(stand1) // Set the animation.
                        .build().async().run(); // Build the animate action and run it
                break;
            case "stand2":
                AnimateBuilder.with(qiContext) // Create the builder with the context.
                        .withAnimation(stand2) // Set the animation.
                        .build().async().run(); // Build the animate action and run it
                break;
            case "stand3":
                AnimateBuilder.with(qiContext) // Create the builder with the context.
                        .withAnimation(stand3) // Set the animation.
                        .build().async().run(); // Build the animate action and run it
                break;
            case "thinking":
                AnimateBuilder.with(qiContext) // Create the builder with the context.
                        .withAnimation(thinking) // Set the animation.
                        .build().async().run(); // Build the animate action and run it
                break;
            case "verbeugen":
                AnimateBuilder.with(qiContext) // Create the builder with the context.
                        .withAnimation(verbeugen) // Set the animation.
                        .build().async().run(); // Build the animate action and run it
                break;
            case "wave":
                AnimateBuilder.with(qiContext) // Create the builder with the context.
                        .withAnimation(wave) // Set the animation.
                        .build().async().run(); // Build the animate action and run it
                break;
            default:
                say_sync(qiContext, "Ich kenne die auszuführende Aktion nicht.");
                break;
        }
    }

    public void faq(QiContext qiContext) {
        JSONArray keywords = null;
        try {
            keywords = questionsObj.getJSONArray("keywords");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        List<PhraseSet> keywordsAsSets = new ArrayList<PhraseSet>();
        for (int i=0; i<keywords.length(); i++) {
            try {
                keywordsAsSets.add(PhraseSetBuilder.with(qiContext)
                        .withTexts(keywords.getString(i))
                        .build());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Listen listen = ListenBuilder.with(qiContext)
                .withPhraseSets(keywordsAsSets)
                .build();

        ListenResult listenResult = listen.run();

        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();
        double[] heard_keys = new double[keywords.length()];
        for (int i = 0; i < keywords.length(); i++) {
            if (matchedPhraseSet.equals(keywordsAsSets.get(i))) {
                heard_keys[i] = 1; //momentan ist es nur möglich, dass ein einzelnes Keyword erkannt wird.
                //mit einem anderen Sprachinterpreten könnten mehrere Keywörter erkannt werden und
            } else {
                heard_keys[i] = 0;
            }
        }

        ArrayList<Map<String, Object>> rated_questions = faqHandler(heard_keys);

        Button btn_a = findViewById(R.id.btn_question_1);
        btn_a.setText("1: "+(String) rated_questions.get(-1).get("question"));
        Button btn_b = findViewById(R.id.btn_question_2);
        btn_b.setText("2: "+(String) rated_questions.get(-2).get("question"));
        Button btn_c = findViewById(R.id.btn_question_3);
        btn_c.setText("3: "+(String) rated_questions.get(-3).get("question"));
        Button btn_d = findViewById(R.id.btn_question_4);
        btn_d.setText("4: "+(String) rated_questions.get(-4).get("question"));

        btn_a.setVisibility(View.VISIBLE);
        //View view_b = findViewById(R.id.btn_question_2);
        btn_b.setVisibility(View.VISIBLE);
        //View view_c = findViewById(R.id.btn_question_3);
        btn_c.setVisibility(View.VISIBLE);
        //View view_d = findViewById(R.id.btn_question_4);
        btn_d.setVisibility(View.VISIBLE);

        JSONArray answers = new JSONArray();
        try {
            JSONArray eins = new JSONArray();
            eins.put(0, "eins");
            answers.put(0, eins);
            JSONArray zwei = new JSONArray();
            zwei.put(0, "zwei");
            answers.put(0, zwei);
            JSONArray drei = new JSONArray();
            drei.put(0, "drei");
            answers.put(0, drei);
            JSONArray vier = new JSONArray();
            vier.put(0, "vier");
            answers.put(0, vier);
        }catch (JSONException e) {

        }

        Boolean another = false;
        do {
            int q_idx = question(qiContext, "Passt Frage eins, zwei, drei oder vier am besten?", answers);


            btn_a.setVisibility(View.INVISIBLE);
            //View view_b = findViewById(R.id.btn_question_2);
            btn_b.setVisibility(View.INVISIBLE);
            //View view_c = findViewById(R.id.btn_question_3);
            btn_c.setVisibility(View.INVISIBLE);
            //View view_d = findViewById(R.id.btn_question_4);
            btn_d.setVisibility(View.INVISIBLE);

            changeText((String) rated_questions.get(-(q_idx + 1)).get("answer"));

            say_sync(qiContext, (String) rated_questions.get(-(q_idx + 1)).get("answer"));

            JSONArray correct_answers = new JSONArray();
            try {
                JSONArray ja = new JSONArray();
                ja.put(0, "ja");
                answers.put(0, ja);
                JSONArray nein = new JSONArray();
                nein.put(0, "nein");
                answers.put(0, nein);
            } catch (JSONException e) {

            }
            int correct = question(qiContext, "War diese Antwort Hilfreich?", correct_answers);

            update_keyweights(qiContext, (String) rated_questions.get(-(q_idx + 1)).get("question"), heard_keys, correct == 0);

            btn_a.setVisibility(View.VISIBLE);
            //View view_b = findViewById(R.id.btn_question_2);
            btn_b.setVisibility(View.VISIBLE);
            //View view_c = findViewById(R.id.btn_question_3);
            btn_c.setVisibility(View.VISIBLE);
            //View view_d = findViewById(R.id.btn_question_4);
            btn_d.setVisibility(View.VISIBLE);

            changeText("");

            JSONArray another_answers = new JSONArray();
            try {
                JSONArray ja = new JSONArray();
                ja.put(0, "ja");
                answers.put(0, ja);
                JSONArray nein = new JSONArray();
                nein.put(0, "nein");
                answers.put(0, nein);
            } catch (JSONException e) {}
            int another_int = question(qiContext, "Möchtest du eine ANDERE FRAGE auswählen?", another_answers);

            another = another_int == 0;

        } while (another);
    }
}

