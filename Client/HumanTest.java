import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;


public class HumanTest {
    public static void main(String[] args) throws IOException {
        String masterURL = Utils.getMasterURL();
        ArrayList<String> wordList = Utils.readWordList(masterURL);
        Random generator = new Random();
        ArrayList<Integer> usedWords = new ArrayList<Integer>();
        int r = generator.nextInt(wordList.size());
        usedWords.add(r);
        String hangManWord = wordList.get(r);
        boolean keepPlaying = true;
        while (keepPlaying) {
            //System.out.println("the word is " + hangManWord);
            keepPlaying = runGame(hangManWord);
            while (usedWords.contains(r)) {
                r = generator.nextInt(wordList.size());
            }
            hangManWord = wordList.get(r);
        }
    }

    private static boolean runGame(String hangManWord) {
        int incorrectGuesses = 0;
        ArrayList<String> prevGuessed = new ArrayList<String>();
        char[] word = hangManWord.toCharArray();
        char[] partial = new char[hangManWord.length()];
        String guess;
        for (int i = 0; i < hangManWord.length(); i++) {
            partial[i] = '_';
        }
        for (int guesses = 0; guesses < 50; guesses++) {
            printStatus(guesses, partial, prevGuessed, incorrectGuesses);
            guess = userPrompt(prevGuessed);
            if (!hangManWord.contains(guess)) {
                incorrectGuesses++;
            }
            prevGuessed.add(guess);
            partial = handleGuess(guess, word, partial);
            if (wordGuessed(partial)) {
                System.out.println("CONGRATULATIONS!");
                printStatus(guesses, partial, prevGuessed, incorrectGuesses);
                System.out.println("Another game? (y/n)");
                if (readLine().matches("y")) {
                    return true;
                } else
                    return false;
            }
            if (incorrectGuesses == 10) {
                System.out.println("Game over, you lose. Another game? (y/n)");
                if (readLine().matches("y")) {
                    return true;
                } else
                    return false;
            }
        }
        return false;
    }



    private static boolean wordGuessed(char[] partial) {
        for (char c : partial) {
            if (c == '_') {
                return false;
            }
        }
        return true;
    }

    private static char[] handleGuess(String s, char[] word, char[] partial) {
        char c = s.toCharArray()[0];
        for (int x = 0; x < word.length; x++) {
            if (word[x] == c) {
                partial[x] = c;
            }
        }
        return partial;
    }

    private static String userPrompt(ArrayList<String> prevGuessed) {
        System.out.println("Guess a letter");
        String letter = readLine();
        if (prevGuessed.contains(letter)) {
            System.out.println("you already guessed \"" + letter + "\" you jackass, pick another letter!");
            letter = userPrompt(prevGuessed);
        }
        return letter;
    }

    private static void printStatus(int guesses, char[] word, ArrayList<String> prevGuessed, int incorrectGuesses) {
        System.out.println("Total guesses:" + guesses + " - incorrect guesses: " + incorrectGuesses + "/10.");
        for (char c : word) {
            System.out.print(c + " ");
        }
        System.out.println("\nyou have previously guessed: ");
        for (String c : prevGuessed) {
            System.out.print(c + ", ");
        }
        System.out.println();
    }

    public static String readLine() {
        String s = "";
        try {
            InputStreamReader converter = new InputStreamReader(System.in);
            BufferedReader in = new BufferedReader(converter);
            s = in.readLine();
        } catch (Exception e) {
            System.out.println("Error! Exception: " + e);
        }
        return s;
    }
}
