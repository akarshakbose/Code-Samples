package com.something.jrgun.elluckphant.model;

import android.util.Log;

import com.something.jrgun.elluckphant.HoldStats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Collections;

/**
 * Creates arrays of numbers to display
 *
 * Stats should already be calculated by the time this class is called
 *      - if not, random numbers generated
 */

public class GenerateNumbers
{
    static Random rand = new Random();

    // class variables
    private double numOfLottos;
    private ArrayList<Integer> nums; // array for pick 5 and mega ball to be set for default constuctor
    private HashMap<Integer, Integer> pick5stats; // keeps track of how many times
    private HashMap<Integer, Integer> megaBstats; // each ball has been picked
    private static int MaxPick5 = 70;
    private static int MaxMega = 25;


    // default constructor - generates numbers randomly
    public GenerateNumbers()
    {
        System.out.println("default constructor");
        this.nums = generateRandom();
    }

    // constructor for statistical play
    // any int argument is fine, an int just ensures statistical
    public GenerateNumbers( int n )
    {
        System.out.println("statistical constructor");
        HoldStats hs = HoldStats.getInstance();
        pick5stats = hs.getPick5stats();
        megaBstats = hs.getMegaBstats();
        numOfLottos = hs.getNumOfLottos();

    }


    // getter for ArrayList of lotto balls
    public ArrayList<Integer> getNums()
    {
        return this.nums;
    }

    // getter for pick 5 stats
    public HashMap<Integer, Integer> getPick5stats()
    {
        return pick5stats;
    }

    // getter for mega ball stats
    public HashMap<Integer, Integer> getMegaBstats()
    {
        return megaBstats;
    }



    // generate lotto numbers randomly
    // does not mod since that has been proven to be bias
    public static ArrayList<Integer> generateRandom()
    {
        ArrayList<Integer> randNums = new ArrayList<>();

        // randomly generate pick5 numbers
        for (int i = 0; i < 5; ++i)
        {
            int num = rand.nextInt(100); // no number is favored when modding by multiple of 10
            ++num; // increment randomInt by one since random number chosen was 0 indexed

            // continue randomly generating number if:
            // a. out of range, b. already chosen
            while (num > MaxPick5 || randNums.contains(num)) {
                num = rand.nextInt(100);
                ++num;
            }

            randNums.add(num); // add number

        }
        Collections.sort(randNums);

        // randomly generate megaball
        int num = rand.nextInt(100);
        ++num;
        while ( num > MaxMega ) {
            num = rand.nextInt(100);
            ++num;
        }

        randNums.add(num); // add number

        return randNums;
    }


    // Generates 1 set of numbers based on frequency count
    // I don't 'mod' to get range of desired numbers, since modding has been been proven to be bias
    private ArrayList<Integer> statisticalPicks()
    {

        if(pick5stats == null || megaBstats == null || numOfLottos == 0)
        {
            Log.e("WARNING", "THERE WAS A PROBLEM GENERATING STATISTICS");
            return generateRandom(); // returns random numbers so app doesn't break
        }
        else{
            ArrayList<Integer> randNums = new ArrayList<>();

            // Generate Pick 5
            double ratio = 5.0 / MaxPick5;

            for (int i = 0; i < 5; ++i)
            {
                int num = rand.nextInt(100); // no number is favored when modding by multiple of 10
                ++num; // increment randomInt by one since random number chosen was 0 indexed

                // continue randomly generating number if:
                // a. out of range, b. number has been picked above expected ratio, c. already chosen
                while (num > MaxPick5 || (pick5stats.get(num) / numOfLottos) > ratio || randNums.contains(num)) {
                    num = rand.nextInt(100);
                    ++num;
                }

                randNums.add(num); // add number

            }
            Collections.sort(randNums); // sort for sequential output

            // generate Mega Ball
            ratio = 1.0 / MaxMega;

            int num = rand.nextInt(100);
            ++num;
            while (num > MaxMega || (megaBstats.get(num) / numOfLottos) > ratio) {
                num = rand.nextInt(100);
                ++num;
            }

            randNums.add(num); // add number

            return randNums;
        }

    }


    // Takes all numbers not pulled under its ratio
    // Randomizes them and evenly distributes them over 5 plays
    // ( because it's not good to over-play a single (randomly chosen) number multiple times )
    public ArrayList<ArrayList<Integer>> statisticalPlay5()
    {

        if(pick5stats == null || megaBstats == null || numOfLottos == 0)
        {
            Log.e("WARNING", "THERE WAS A PROBLEM GENERATING STATISTICS");
            return null;
        }

        ArrayList<ArrayList<Integer>> play5 = new ArrayList<ArrayList<Integer>>(); // only stores 5 lotto picks
        ArrayList<Integer> play5pick5 = new ArrayList<>(); // stores all statistically avail pick 5 nums
        ArrayList<Integer> play5mega = new ArrayList<>(); // stores all statistically avail mega nums

        // generate Pick 5 numbers
        double ratio = 5.0 / MaxPick5;
        for(Map.Entry<Integer, Integer> e : pick5stats.entrySet())
        {
            if( e.getValue()/numOfLottos < ratio )
            {
                play5pick5.add(e.getKey());
            }
        }
        Collections.shuffle(play5pick5); // shuffle the order of poolable numbers
        print("Pick 5 numbers (" + play5pick5.size() + ")");
        print(play5pick5);

        // generate Mega Ball numbers
        ratio = 1.0 / MaxMega;
        for(Map.Entry<Integer, Integer> e : megaBstats.entrySet())
        {
            if( e.getValue()/numOfLottos < ratio )
            {
                play5mega.add(e.getKey());
            }
        }
        Collections.shuffle(play5mega); // randomize
        print("Megaball numbers (" + play5mega.size() + ")");
        print(play5mega);

        // fill 2D array with 5 sets of lotto numbers
        for( int i=0; i<25; ++i )
        {
            if( i%5 == 0 )
            {
                play5.add(new ArrayList<Integer>());
            }

            play5.get(i/5).add( play5pick5.get(i) );

        }

        // sort arrays
        int play = 0;
        for( ArrayList<Integer> array : play5 )
        {
            Collections.sort(array);
            play5.get(play).add( play5mega.get(play) ); // then add a megaball to end
            ++play;
        }

        return play5;
    }

    // to print without typing out "system.out.println"
    public static <T> void print( T t)
    {
        System.out.println(t);
    }


    // nicely formatted lotto numbers
    public String toString()
    {
        String s = "[ ";
        for( int i=0; i<6; ++i )
        {
            int current = this.nums.get(i);

            if( i >= 0 && i < this.nums.size()-2 )
            {
                if( current < 10)
                {
                    s += " ";
                }
                s += current + " - ";
            }
            else if( i == this.nums.size()-2 )
            {
                if( current < 10)
                {
                    s = " " + s;
                }

                s += current;
            }
            else{
                s += " ] [ ";
                if( current < 10)
                {
                    s += " ";
                }
                s += current + " ]";
            }
        }
        return s;
    }


} // end class



