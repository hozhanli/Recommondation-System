/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozhanli.calculations;

/**
 *
 * @author ozhanli
 */
public class PredictedItem {
    private String movieId;
    private String userId;
    private Double predictionRating;

    public PredictedItem() {
    }
    
    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getPredictionRating() {
        return predictionRating;
    }

    public void setPredictionRating(Double predictionRating) {
        this.predictionRating = predictionRating;
    }
    
}
