import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class Worker {
    public static void main(String[] args) throws InterruptedException, IOException {
        String masterURL = Utils.getMasterURL();
        int cpuCount =  Runtime.getRuntime().availableProcessors();
        int threadCount = cpuCount + 1;
        System.out.println("Available processors: " + cpuCount + ", will use " + threadCount + " threads.");
        ArrayList<String> wordList = Utils.readWordList(masterURL);
        new Worker(wordList, threadCount, masterURL); // start worker
    }

    private ArrayList<String> wordList = new ArrayList<String>();
    private int threadCount;
    private ArrayList<String> jobList = new ArrayList<String>();
    private List<Thread> threads = new ArrayList<Thread>(threadCount);
    private boolean jobsAvailable = true;
    private String masterURL;

    Worker(ArrayList<String> wordList, int threadCount, String masterURL) throws InterruptedException, IOException {
        this.wordList = wordList;
        this.threadCount = threadCount;
        this.masterURL = masterURL;
        this.jobList = getJobList(threadCount);
        int i = 0;
        int repetitions = getTestRepetitions(masterURL);
        while (true) {
            while (jobsAvailable) {
                i++;
                if (i>100){
                    repetitions = getTestRepetitions(masterURL);
                    i=0;
                }
                //System.out.println("--------------------------------------------------------------------------------------------- We have " + aliveThreads() + " running threads. ");
                fillThreadPool(repetitions);
                pause(100);
            }
            pause(5000);
            jobsAvailable = true;
        }
    }

    private ArrayList<String> getJobList(int size) {
        // http://172.31.25.155:3000/jobs/
        ArrayList jobList = new ArrayList<String>();
        try{
        URL url = new URL(this.masterURL + "jobs/" + size);
        URLConnection con = url.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            jobList.add(inputLine);
        in.close();
        if (jobList.size() == 0) {
            jobsAvailable = false;
        }
        }catch (IOException e){
            System.out.println("Error fetching jobs from: " + this.masterURL + "jobs/" + size);
            e.printStackTrace();
        }
        return jobList;
    }

    private int aliveThreads() {
        int running = 0;
        for (Thread thread : this.threads) {
            if (thread.isAlive()) {
                running++;
            }
        }
        return running;
    }

    private void fillThreadPool(int testRepetitions) {
        try {
            int neededJobs = threadCount - aliveThreads();
            if (neededJobs > 0) {
                jobList = getJobList(neededJobs);
                for (int i = 0; i < jobList.size(); i++) {
                    System.out.println("******************************************************************************************** Creating new thread");
                    WorkerThread w = new WorkerThread(jobList.get(i), wordList, masterURL, testRepetitions);
                    threads.add(w);
                    w.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void pause(int time) {
        try {
            sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int getTestRepetitions(String masterURL) {
        int repetitions;
        try {
            URL url = new URL(masterURL + "repetitions");
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            repetitions = Integer.parseInt(in.readLine());
            in.close();
        } catch (IOException e) {
            System.out.println("Problem connecting to: " + masterURL + "repetitions");
            e.printStackTrace();
            repetitions = 200;
        }
        return repetitions;
    }
}

