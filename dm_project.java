import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

public class dm_project {

    static int users = 943, items = 1682;
    static double[][] similarity = new double[1683][1683];
    static int[][] finalRating = new int[944][1683];
    static int[][] rateMatrix = new int[944][1683];
    static double[] avgRate = new double[1683];
    static int[] uniqueItemCount = new int[1683];

    public static void main(String args[]) {
        try {
	    if (args.length != 2) {
		throw new IllegalArgumentException("Please pass 2 arguments i.e. input file and output file");	
	    }
	    String fileName = args[0];
            Scanner scan = new Scanner(new File(fileName));
            String outputFile = args[1];
            PrintWriter output = new PrintWriter(new FileWriter(outputFile));
            int[] data = new int[3];
            while (scan.hasNextLine()) {
                String token[] = scan.nextLine().split("\\s+");
                for (int i = 0; i < 3; i++) {
                    data[i] = Integer.parseInt(token[i]);
                }
                rateMatrix[data[0]][data[1]] = data[2];
                uniqueItemCount[data[1]]++;
            }
            
            // Calculating average rating of items
            for (int i = 1; i <= items; i++) {
                avgRate[i] = averageItemRating(i);
            }
            
            //Calling method to calculate similarities between items by using Pearson correlation algorithm 
            pearsonCorrelation(rateMatrix, avgRate);
            
            //Calling method to calculate final predicted values
            finalPredictions(rateMatrix);

            for (int u = 1; u <= users; u++) {
                for (int i = 1; i <= items; i++) {
                    output.println(u + " " + i + " " + rateMatrix[u][i]);
                }
            }
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Method for calculating average rating
    public static double averageItemRating(int items) {
        double avg = 0;
        for (int j = 1; j <= users; j++) {
            avg += rateMatrix[j][items];

        }
        avg = avg / uniqueItemCount[items];
        return avg;
    }

    // Method for calculating final predictions
    public static void finalPredictions(int[][] rateMatrix) {
        double prediction = 0;
        double num = 0, den = 0;
        for (int user = 1; user <= users; user++) {
            for (int item1 = 1; item1 <= items; item1++) {
                if (rateMatrix[user][item1] == 0) {
                    num = 0;
                    den = 0;
                    TreeMap<Double, Integer> sortedMap = new TreeMap<Double, Integer>(Collections.reverseOrder());
                    for (int item2 = 1; item2 <= items; item2++) {
                        if (item1 != item2 && rateMatrix[user][item2] != 0) {
                            sortedMap.put(similarity[item1][item2], item2);

                        }
                    }
                    int neighbors = 0;
                    int minSize = Math.min(10, sortedMap.size());
                    for (Entry<Double, Integer> entry : sortedMap.entrySet()) {
                        if (neighbors < minSize) {
                            num += similarity[item1][entry.getValue()] * rateMatrix[user][entry.getValue()];
                            den += Math.abs((similarity[item1][entry.getValue()]));
                            neighbors++;
                        }
                    }
                    prediction = Math.round(num / den);
                    if (prediction > 4.0) {
                        prediction = 5.0;
                    } else if (prediction <= 1.0) {
                        prediction = 1.0;
                    }
                    rateMatrix[user][item1] = (int) prediction;
                }
            }
        }
    }

    // Method for calculating similarity between items using Pearson correlation
    public static void pearsonCorrelation(int rateMatrix[][], double[] avgItemRating) {
        double num = 0, den1 = 0, den2 = 0, den = 0;
        for (int item1 = 1; item1 <= items; item1++) {
            num = 0;
            den1 = 0;
            den2 = 0;
            den = 0;
            for (int item2 = 1; item2 <= items; item2++) {
                if (item1 != item2) {
                    for (int user = 1; user <= users; user++) {
                        if (rateMatrix[user][item1] != 0 && rateMatrix[user][item2] != 0) {
                            num += (rateMatrix[user][item1] - avgItemRating[item1]) * (rateMatrix[user][item2] - avgItemRating[item2]);
                            den1 += Math.pow(rateMatrix[user][item1] - avgItemRating[item1], 2);
                            den2 += Math.pow(rateMatrix[user][item2] - avgItemRating[item2], 2);
                        }
                    }
                }
                den = Math.sqrt(den1 * den2);

                if (den != 0) {
                    similarity[item1][item2] = num / den;
                }
            }
        }
    }

}
