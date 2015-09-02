package com.movile.test.model;

/**
 * Created by jgutierrez on 9/1/15.
 */
public class Episode {

    public int season;
    public int number;

    public String title;

    public Episode() {
    }

    public Episode(int number, String title, int season) {
        this.number = number;
        this.title = title;
        this.season = season;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public String getNumberAsString() {
        return String.valueOf(number);
    }
}
