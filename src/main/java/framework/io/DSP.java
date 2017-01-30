package framework.io;

import framework.fft.Complex;
import framework.fft.FFT;
import framework.fft.DataPoint;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by User on 28.1.2017.
 */
public class DSP {

    double magnitudes[][];  //dimenzije window.count * FREQ_RANGE.count. Za svaki window i svaki freq range belezimo magnitudu.
    long frequencies[][];   //dimenzije window.count * FREQ_RANGE.count. Za svaki window i svaki freq range belezimo frekvenciju.

    double recordPoints[][];    //dimenzije window.count * UPPER_LIMIT.


    //odnosi se na frekvenciju
    public final int UPPER_LIMIT = 300;
    public final int LOWER_LIMIT = 40;
    public final int[] FREQ_RANGE = new int[]{40, 80, 120, 180, UPPER_LIMIT + 1};

    public DSP(){
    }

    public void ApplyFFT(byte[] byteArray){
        byte audio[] = byteArray;
        final int totalSize = audio.length;
        int numberOfWindows = totalSize / 4096;

        // When turning into frequency domain we'll need complex numbers:
        Complex[][] windows = new Complex[numberOfWindows][];

        // For all the chunks:
        for (int windowNumber = 0; windowNumber < numberOfWindows; windowNumber++) {
            Complex[] complex = new Complex[4096];
            for (int i = 0; i < 4096; i++) {
                // Put the time domain infrastructure into a complex number with imaginary
                // part as 0:
                complex[i] = new Complex(audio[(windowNumber * 4096) + i], 0);
            }
            // Perform fft analysis on the window:
            windows[windowNumber] = FFT.fft(complex);
        }

        //determineKeyPoints(windows, 0, false);
    }

    private void determineKeyPoints(Complex[][] windows, long songId, boolean isMatching) {
        FileWriter fstream = null;
        try {
            fstream = new FileWriter("result.txt");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        BufferedWriter outFile = new BufferedWriter(fstream);

        //napravi novi niz dimenzija windows.count * broj raspona frekvencija koji merimo. Na taj nacin za svaki window imamo niz svih raspona.
        magnitudes = new double[windows.length][5];
        for (int i = 0; i < windows.length; i++) {
            for (int j = 0; j < 5; j++) {
                magnitudes[i][j] = 0;
            }
        }

        //napravi novi niz dimenzija windows.count * najveca frekvencija koju merimo. Na taj nacin za svaki window imamo niz svih frekvencija.
        recordPoints = new double[windows.length][UPPER_LIMIT];
        for (int i = 0; i < windows.length; i++) {
            for (int j = 0; j < UPPER_LIMIT; j++) {
                recordPoints[i][j] = 0;
            }
        }

        //napravi novi niz dimenzija windows.count * broj raspona frekvencija koji merimo. Na taj nacin za svaki window imamo niz svih raspona.
        frequencies = new long[windows.length][5];
        for (int i = 0; i < windows.length; i++) {
            for (int j = 0; j < 5; j++) {
                frequencies[i][j] = 0;
            }
        }

        //prodji kroz svaki window i ubelezi kljucne tacke
        for (int window = 0; window < windows.length; window++) {

            for (int freq = LOWER_LIMIT; freq < UPPER_LIMIT - 1; freq++) {

                // Get the magnitude:
                double mag = Math.log(windows[window][freq].abs() + 1);

                // Find out which frequency range we are in:
                int range = getFrequencyRange(freq);

                // Save the highest magnitude and corresponding frequency:
                if (mag > magnitudes[window][range]) {
                    magnitudes[window][range] = mag;
                    frequencies[window][range] = freq;
                    recordPoints[window][freq] = 1;
                }
            }

            try {
                outFile.write("----------------- [window] " + window + " ------------------\n");
                for (int k = 0; k < 5; k++) {
                    outFile.write("[Magnitude] " + magnitudes[window][k]
                                    /*+ "; [?] " + recordPoints[window][k] */
                                    + "; [Frequency]: " + frequencies[window][k] + "\n"
                    );
//                    for (int i = 0; i < UPPER_LIMIT-1; i++) {
//                        outFile.write("\n[" + recordPoints[window][i] + "]");
//                    }
                }
                outFile.write("\n");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        try {
            outFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //broj od 0-range.count koji pokazuje u kom intervalu se nalazi frekvencija
    private int getFrequencyRange(int freq) {
        int i = 0;
        while (FREQ_RANGE[i] < freq) {
            i++;
        }
        return i;
    }

}
