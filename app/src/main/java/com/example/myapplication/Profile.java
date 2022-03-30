package com.example.myapplication;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Date;

public class Profile {
    int age;
    //double distance;
    //ArrayList<String> log = new ArrayList<String>(); //
    //String emotion;
    //String faqTopic; //Zu welchem Thema hat der user eine Frage?
    //String funTopic;
    //String intention; //unterhaltung oder Frage, was kannst du Roboter
    //Date date = new Date();
    //String dates = date.toString();

    public Profile(){

    }

    public String formality(){
        return (this.age < 10) ? "child" : (this.age < 20) ? "teen" : "adult";
    }
}