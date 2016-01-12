/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozhanli.calculations;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.ozhanli.utils.Constant;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 *
 * @author ozhanli
 */
public class Recomender {
    /* Store movie datas */
    private Map<String, Movie> movieList = new HashMap<String, Movie>();
    /* Store movie ratings of a userId and mean value  for train data */
    private Map<String, UserMovieRatings> trainMovieRatings = new HashMap<String, UserMovieRatings>();
    /* Store movie ratings of a userId and mean value  for train data */
    private Map<String, UserMovieRatings> testMovieRatings = new HashMap<String, UserMovieRatings>();
    /* Store predicted error and prediction results to sort */
    private Map<Double, Set<PredictedItem>> predictedMovieRatingsAndError = new HashMap<Double, Set<PredictedItem>>();
    /* Prediction count made by recommender system */
    private long predictionCount = 0;
    private double sumOfErrors = 0;
    private double pow2SumOfErrors = 0;
    private double meanAbsoluteError = 0;
    private double rootMeanSquareError = 0;
    /* Prediction results ==> Predictions.txt */
    private StringBuilder predictions = new StringBuilder();
    /* Recommendation results ==> Recommendations.txt */
    private StringBuilder recommendations = new StringBuilder();

    public Recomender() {
        /* Read movies ids, year and name, store in movieList */
        readMovieList();
        /* Read train dataset including movieId, userId and userRate, store in UserMovieRating */
        readTrainMovieRatings();
        /* Calculates each users mean value */
        calculateTrainUserMean();
        /* Read Test movies and ratings */
        readTestMovieRatings();
        /* Print prediction results to file */
        printToFile(predictions.toString(), Constant.PREDICTION_OUTPUT_PATH);
        printToFile(recommendations.toString(), Constant.RECOMMENDATION_OUTPUT_PATH);
        
    }
    
    private void readMovieList() {
        try (BufferedReader br = new BufferedReader(new FileReader(Constant.MOVIE_TITLES_PATH)))
            {
                String sCurrentLine;
                while ((sCurrentLine = br.readLine()) != null) {
                    setMovieList(sCurrentLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    private void setMovieList(String currentLine) {
        String[] out = currentLine.split(",", 3);
        if (out.length == 3) {
            Movie movie = new Movie();
            movie.setId(out[0]);
            movie.setYear(out[1]);
            movie.setName(out[2]);
            movieList.put(out[0].trim(), movie);
        } else {
            System.out.println("Missing data");
        }
    }
    
    private void readTrainMovieRatings() {
        try (BufferedReader br = new BufferedReader(new FileReader(Constant.TRAINING_RATINGS_PATH)))
            {
                String sCurrentLine;
                while ((sCurrentLine = br.readLine()) != null) {
                    setTrainMovieRatings(sCurrentLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    private void setTrainMovieRatings(String currentLine) {
        String[] out = currentLine.split(",", 3);
        if (out.length == 3) {
            String movieId = out[0].trim();
            String userId = out[1].trim();
            String movieRating = out[2].trim();
            
            UserMovieRatings userMovieRatings = trainMovieRatings.get(userId);
            if (userMovieRatings != null) {
                userMovieRatings.getMovieRatings().put(movieId, Double.parseDouble(movieRating));
                trainMovieRatings.put(userId, userMovieRatings);
            } else {
                userMovieRatings = new UserMovieRatings();
                userMovieRatings.getMovieRatings().put(movieId, Double.parseDouble(movieRating));
                trainMovieRatings.put(userId, userMovieRatings);
            }
        } else {
            System.out.println("Missing data");
        }
    }
    private void calculateTrainUserMean() {
        for (String userId : trainMovieRatings.keySet()) {
            /* Get movies and ratings by user id */
            UserMovieRatings userMovieRatings = trainMovieRatings.get(userId);
            /* Get movie ratings voted by user have this userId*/
            Map<String, Double> movieRatings = userMovieRatings.getMovieRatings();
            double userTotalRatings = 0;
            double userMean = 0;
            for (String movieId : movieRatings.keySet()) {
                userTotalRatings += movieRatings.get(movieId);
            }
            userMean = userTotalRatings / movieRatings.keySet().size();
            userMovieRatings.setUserMean(userMean);
        }
    }
    
    private void readTestMovieRatings() {
        try (BufferedReader br = new BufferedReader(new FileReader(Constant.TESTING_RATINGS_PATH)))
            {
                String sCurrentLine;
                while ((sCurrentLine = br.readLine()) != null) {
                    currentTestUser(sCurrentLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    private void currentTestUser(String currentTestData) {
        String[] out = currentTestData.split(",", 3);
        if (out.length == 3) {
            String movieId = out[0].trim();
            String userId = out[1].trim();
            String movieRating = out[2].trim();
            predictionCount += 1;
            double predictedRateForMovie = predictMovieRatingOfUser(userId, movieId);
            double error = Math.abs(predictedRateForMovie - Double.parseDouble(movieRating));
            sumOfErrors += error;
            pow2SumOfErrors += Math.pow(error, 2);
            PredictedItem predictedItem = new PredictedItem();
            predictedItem.setUserId(userId);
            predictedItem.setMovieId(movieId);
            predictedItem.setPredictionRating(predictedRateForMovie);
            
            Set<PredictedItem> predictedItems = predictedMovieRatingsAndError.get(error);
            if (predictedItems == null) {
                predictedItems = new HashSet<PredictedItem>();
                predictedItems.add(predictedItem);
                predictedMovieRatingsAndError.put(error, predictedItems);
            } else {
                predictedMovieRatingsAndError.put(error, predictedItems);
            }
            predictions.append(movieId + "," + userId + "," + predictedRateForMovie + "\n");
            if (predictedRateForMovie > 4) {
                recommendations.append(movieId + "," + userId + "," + predictedRateForMovie + "\n");
            }
        } else {
            System.out.println("Missing data");
        }
    }
    private double predictMovieRatingOfUser(String targetUser, String targetMovie) {
        double normalizingFactor = 0;
        double predictionRate = 0;
        double ratingsTotalWeight = 0;
        double targetUserMean = trainMovieRatings.get(targetUser).getUserMean();
        
        for(String user : trainMovieRatings.keySet()) {
            if (user.equalsIgnoreCase(targetUser) == false) {
                if (trainMovieRatings.get(user).getMovieRatings().get(targetMovie) != null) {
                    double pearsonCorrelation = calculatePearsonCoefficent(targetUser, user);
                    if (pearsonCorrelation == 0) {
                        continue;
                    }
                    double userMean = trainMovieRatings.get(user).getUserMean();
                    ratingsTotalWeight += ((trainMovieRatings.get(user).getMovieRatings().get(targetMovie) - userMean) * pearsonCorrelation);
                    normalizingFactor += Math.abs(pearsonCorrelation);
                }
            }
        }
        if (normalizingFactor == 0) {
            return 0;
        }
        normalizingFactor = 1 / normalizingFactor;
        predictionRate = targetUserMean + (normalizingFactor * ratingsTotalWeight);
        return predictionRate;
    }
    
    private double calculatePearsonCoefficent(String user1, String user2) {
        double user1AndUser2Covariance = 0;
        double user1Variance = 0;
        double user2Variance = 0;
        double user1Mean = trainMovieRatings.get(user1).getUserMean();
        double user2Mean = trainMovieRatings.get(user2).getUserMean();
        Set<String> movieRatedByBoth = getMoviesRatedByBoth(user1, user2);
        if (movieRatedByBoth.size() > 0) {
            for (String movie : movieRatedByBoth) {
                double user1Rate = trainMovieRatings.get(user1).getMovieRatings().get(movie);
                double user2Rate = trainMovieRatings.get(user2).getMovieRatings().get(movie);
                user1AndUser2Covariance += ((user1Rate - user1Mean)*(user2Rate - user2Mean));
                user1Variance += Math.pow((user1Rate - user1Mean), 2);
                user2Variance += Math.pow((user2Rate - user2Mean), 2);
            }
            if (user1Variance == 0 || user2Variance == 0) {
                return 0;
            }
            double pearsonCoefficent = (user1AndUser2Covariance / Math.sqrt(user1Variance * user2Variance));
            return pearsonCoefficent;
        }
        return 0;
    }
    
    private Set<String> getMoviesRatedByBoth(String user1, String user2) {
        Set<String> user1RatedMovies = trainMovieRatings.get(user1).getMovieRatings().keySet();
        Set<String> user2RatedMovies = trainMovieRatings.get(user2).getMovieRatings().keySet();
        Set<String> moviesRatedByBoth = new HashSet<>();
        for (String item : user1RatedMovies) {
            if (user2RatedMovies.contains(item)) {
                moviesRatedByBoth.add(item);
            }
        }
        return moviesRatedByBoth;
    }
    
    private void printToFile(String content, String filePath) {
        File outputFile = new File(filePath);
        try {
            if(outputFile.exists() == false) {
                outputFile.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(outputFile.getAbsoluteFile());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(content); bufferedWriter.close();
        } catch (IOException ex) {
            System.out.println("Message: << exception in creating file  >>:" + filePath + " ex: " + ex );
        }
    }
}