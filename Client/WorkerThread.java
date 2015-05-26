import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;

public class WorkerThread extends Thread {
    private String DNA;
    private ArrayList<String> wordList = new ArrayList<String>();
    private Random generator = new Random();
    private int testRepetitions;
    private String masterURL;
    private ArrayList<Integer> usedWords = new ArrayList<Integer>();

    WorkerThread(String DNA, ArrayList<String> wordList, String masterURL, int testRepetitions) throws IOException {
        //this.DNA = "eisarntolcdugpmhbyfvwkxqjz";
        this.DNA = DNA;
        this.wordList = wordList;
        this.masterURL = masterURL;
        this.testRepetitions = testRepetitions;
    }

    public void run() {
        int[] results;
        int total = 0;
        int incorrect = 0;
        int wins =0;
        for (int i = 0; i < this.testRepetitions; i++) {
            results = test();
            total += results[0];
            incorrect += results[1];
            if(results[1] < 10){
                wins++;
            }
        }
        Double avg = total / (double) this.testRepetitions;
        Double incorrectAvg = incorrect / (double) this.testRepetitions;
        Double winPercentage =  100*(wins / (double) this.testRepetitions);
        System.out.println("Job complete, average: " + avg + " - Incorrect: " + incorrectAvg + " - Win%: " + winPercentage + " - Repetitions: " + testRepetitions + " - DNA: " + DNA);
        sendResult(avg, incorrectAvg, winPercentage);
    }

    private int[] test() {
        //int incorrectGuesses = 0;
        //int guesses = 0;
        int[] guesses = new int[2];// [0] is number of guesses, [1] number of incorrect guesses
        int wordNumber = generator.nextInt(wordList.size());
        while (usedWords.contains(wordNumber)) {
            wordNumber = generator.nextInt(wordList.size());
        }
        String hangManWord = wordList.get(wordNumber);
        String originalWord = hangManWord; //only used for debugging
        ArrayList<Integer> positionList = new ArrayList<Integer>();
        //copy wordlist to templist, excluding words with the wrong length
        ArrayList<String> tempList = Utils.copyWordList(hangManWord.length(), wordList);
        ArrayList<String> newList = new ArrayList<String>();
        //System.out.println("templist length: " + tempList.size());

        //looping through guesses
        for (int i = 0; i < DNA.length(); i++) {
            guesses[0]++;// the number of guesses indicates the score for each test run
            String c = Character.toString(DNA.charAt(i));
            //System.out.println("char at " + i + " is: " + c);

            //if none of the remaining words contain the character, do not count toward guesses
          //  if (Utils.skippable(c, tempList)) {
          //     guesses[0]--;
          //     guesses[1]--;
          //  }
            if (hangManWord.contains(c)) {
                //exclude all words that don't contain the guessed character at the positions it was found
                int x = 0;
                positionList.clear();
                for (Character letter : hangManWord.toCharArray()) {
                    if (letter == '*')
                        break;
                    //System.out.println("checking character " + letter.toString() + " against " + c);
                    if (c.matches(letter.toString())) {
                        positionList.add(x);
                        //System.out.println("added to positionlist: " + c.toString() + ", " + x);
                    }
                    x++;
                }
                tempList = Utils.removeFromList(tempList, c, positionList);

                //replace guessed characters with *
                hangManWord = hangManWord.replaceAll(c, "*");
                if (Utils.checkForBlanks(hangManWord)) {  //if there are no un-guessed (non *) characters left, return guesses
                    //System.out.println(originalWord + " guessed in " + guesses + ", words remaining in list: " + tempList.size() + ", last character guessed was " + c);
                    return guesses;
                }
            } else {
                guesses[1]++;
                //if the guess is not in hangmanword, exclude all words that do contain the guessed character
                tempList = Utils.removeFromList(tempList, c);
                //If only 1 possible word remaining on list, no further guesses.
                if (tempList.size() < 2) {//if only one word remaining, return guesses+1
                    //System.out.println(originalWord + " guessed in " + (guesses + 1));
                    return guesses;
                }
            }

        }

        System.out.println("Error running test on DNA: " + this.DNA + " - with word: " + originalWord + " " + hangManWord);
        return guesses;
    }


    // Sends the DNA string and the average score to
    private void sendResult(Double avg, Double incorrectAvg, Double winPercentage) {
        try {
            // masterurl - http://172.31.25.155:3000/
            URL url = new URL(masterURL + "dna/" + DNA + "/" + avg + "/" + incorrectAvg + "/" + winPercentage);
            URLConnection conn = url.openConnection();
            InputStream stream = conn.getInputStream();
            stream.close();
        } catch (MalformedURLException e) {
            System.out.println("Problems connecting to: " + masterURL + "dna/" + DNA + "/" + avg + "/" + incorrectAvg + "/" + winPercentage);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Problems connecting to: " + masterURL + "dna/" + DNA + "/" + avg + "/" + incorrectAvg + "/" + winPercentage);
            e.printStackTrace();
        }
    }


}
