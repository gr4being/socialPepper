package com.example.myapplication;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Date;

public class Profile {

    int age;
    double distance;
    ArrayList<String> log = new ArrayList<String>(); //
    String emotion;
    String typ; //Einordnung in verschiedene Menschentypen
    String faqTopic; //Zu welchem Thema hat der user eine Frage?
    String funTopic;
    String intention; //unterhaltung oder Frage, was kannst du Roboter
    Date date = new Date();
    String dates = date.toString();

    public Profile(){

    }

    public void updateTyp(){
    }

    public void updateTopic(){
    }



}