import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Utils {

    public static String getMasterURL() {
        return "http://127.0.0.1:5000/";
    }

    public static ArrayList<String> copyWordList(int length, ArrayList<String> oldList) {
        ArrayList<String> newList = new ArrayList<String>();
        for (String s : oldList) {
            if (s.length() == length)
                newList.add(s);
        }
        return newList;
    }

    public static ArrayList<String> copyWordList(ArrayList<String> oldList) {
        ArrayList<String> newList = new ArrayList<String>();
        for (String s : oldList) {
            newList.add(s);
        }
        return newList;
    }

    public static ArrayList<String> removeFromList(ArrayList<String> list, String c, ArrayList<Integer> positionList) {
        ArrayList<String> newList = new ArrayList<String>();
        boolean keepWord = true;
        if (c != "") {
            //char guess = c.toCharArray()[0];
            for (String word : list) {
                for (int y : positionList) {
                    //System.out.println("******************************************************************************  " +  positionList.get(i));
                    if (!c.matches(word.substring(y, y + 1))) {
                        keepWord = false;
                        //System.out.println("character " + c + " not found in position " + positionList.get(i) + " of word " + word);
                    }
                }
                if (keepWord) {
                    newList.add(word);
                }
                keepWord = true;
            }
        } else {
            System.out.println("--------------------------------------------- guess was blank");
        }
        return newList;
    }

    public static ArrayList<String> removeFromList(ArrayList<String> list, String c) {
        //remove any words from list that contain c
        ArrayList<String> newList = new ArrayList<String>();
        for (String s : list) {
            if (!s.contains(c)) {
                newList.add(s);
            }
        }
        return newList;
    }


    public static ArrayList<String> readWordList(String masterURL) {
        ArrayList<String> wordList = new ArrayList<String>();
        try {

            URL url = new URL(masterURL + "dictionary");
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                wordList.add(inputLine);
            in.close();
        } catch (IOException e) {
            System.out.println("Problems connecting to: " + masterURL + "dictionary");
            e.printStackTrace();
        }
        return wordList;
    }

    //returns false if the word contains any non-* characters
    public static boolean checkForBlanks(String word) {
        boolean noBlanks = true;
        for (char d : word.toCharArray()) {
            if (d != '*') {
                noBlanks = false;
            }
        }
        return noBlanks;
    }

    public static boolean skippable(String c, ArrayList<String> tempList) {
        for (String s : tempList) {
            if (s.contains(c)) {
                return false;
            }
        }
        return true;
    }
}
