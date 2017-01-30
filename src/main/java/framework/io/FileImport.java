package framework.io;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;

/**
 * Created by User on 27.1.2017.
 */
public class FileImport {

    private boolean running = false;
    private String filePath = "";
    private byte[] outByteArray;

    public FileImport(String path) {
        this.filePath = path;
    }

    public void listenSound() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        AudioFormat formatTmp = null;
        TargetDataLine lineTmp = null;
        AudioInputStream decodedInputStream = null;
        boolean isMicrophone = false;

        AudioInputStream in;

        if (filePath.contains("http")) {
            URL url = new URL(filePath);
            in = AudioSystem.getAudioInputStream(url);
        } else {
            System.out.println("");
            File file = new File(filePath);
            in = AudioSystem.getAudioInputStream(file);
        }

        AudioFormat baseFormat = in.getFormat();

        System.out.println(baseFormat.toString());

        AudioFormat.Encoding[] targetEncodings = AudioSystem.getTargetEncodings(AudioFormat.Encoding.PCM_FLOAT);
        for (AudioFormat.Encoding targetEncoding : targetEncodings) {
            System.out.println(targetEncoding);
        }

        AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
                false);

        System.out.println("SAMPLE RATE: " + decodedFormat.getSampleRate());
        System.out.println("FPS RATE: " + decodedFormat.getFrameRate());
        System.out.println("SAMPLES PER FRAME: " + decodedFormat.getSampleRate()/decodedFormat.getFrameRate());

        decodedInputStream = AudioSystem.getAudioInputStream(decodedFormat, in);

        System.out.println(decodedFormat.toString());

        formatTmp = decodedFormat;

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, formatTmp);
        lineTmp = (TargetDataLine) AudioSystem.getLine(info);

        final AudioFormat format = formatTmp;
        final TargetDataLine line = lineTmp;
        final boolean isMicro = isMicrophone;
        final AudioInputStream outDinSound = decodedInputStream;

        if (isMicro) {
            try {
                line.open(format);
                line.start();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        running = true;
        int n = 0;
        byte[] buffer = new byte[(int) 1024];

        try {
            while (running) {
                n++;
                if (n > 1000) {
                    break;
                }

                int count = 0;
                if (isMicro) {
                    count = line.read(buffer, 0, 1024);
                } else {
                    count = outDinSound.read(buffer, 0, 1024);
                }
                if (count > 0) {
                    out.write(buffer, 0, count);
                }
            }

            byte[] b = out.toByteArray();
            outByteArray = b;

            System.out.println("Thread started.");

            try {
                FileWriter fstream = new FileWriter("out.txt");
                BufferedWriter outFile = new BufferedWriter(fstream);

                for (int i = 0; i < b.length; i++) {
                    outFile.write("" + b[i] + "[" + i + "]");
                }
                outFile.close();

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }

            out.close();
            line.close();
        } catch (IOException e) {
            System.err.println("I/O problems: " + e);
            System.exit(-1);
        }

    }

    public byte[] getOutByteArray() {
        return outByteArray;
    }

    public float[] getOutFloatArray() throws IOException {
        ByteArrayInputStream bas = new ByteArrayInputStream(outByteArray);
        DataInputStream ds = new DataInputStream(bas);

        float[] fArr = new float[outByteArray.length / 4];  // 4 bytes per float
        for (int i = 0; i < fArr.length; i++)
        {
            fArr[i] = ds.readFloat();
        }

        return fArr;
    }
}
