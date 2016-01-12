/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozhanli.calculations;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ozhanli
 */
public class UserMovieRatings {
    private HashMap<String, Double>  movieRatings = new HashMap<String, Double>();
    private double userMean;

    public UserMovieRatings() {
    }

    public HashMap<String, Double> getMovieRatings() {
        return movieRatings;
    }

    public void setMovieRatings(HashMap<String, Double> movieRatings) {
        this.movieRatings = movieRatings;
    }

    public double getUserMean() {
        return userMean;
    }

    public void setUserMean(double userMean) {
        this.userMean = userMean;
    }
    
}
