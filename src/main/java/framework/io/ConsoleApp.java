package framework.io;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by User on 27.1.2017.
 */
public class ConsoleApp {

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {

        System.out.println("Enter song path:");

        Scanner in = new Scanner(System.in);
        String path = in.nextLine();

        System.out.println(path);

        FileImport file = new FileImport(path);
        file.listenSound();

        System.out.println(file.getOutByteArray().length);

        DSP fft = new DSP();

        fft.ApplyFFT(file.getOutByteArray());
    }
}
