package framework.main;

import framework.features.ConstantQ;
import framework.features.MFCC;
import framework.io.DSP;
import framework.io.FileImport;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
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

        MFCC mfcc = new MFCC(1, 22050);
        System.out.println("\nMFCC");
        mfcc.applyMFCC(file.getOutFloatArray());

        ConstantQ cq = new ConstantQ(22050, 256, 1024, 12);
        cq.calculate(fft.getFloatFft(fft.getComplexFft(file.getOutByteArray())));
        System.out.println("\nConstantQ Coefficients:");
        cq.getCoefficients();
        cq.calculateMagintudes(fft.getFloatFft(fft.getComplexFft(file.getOutByteArray())));
        System.out.println("\nConstantQ Magnitudes:");
        cq.getMagnitudes();

    }
}
