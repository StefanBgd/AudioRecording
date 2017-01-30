package framework.features;

/**
 * Created by User on 29.1.2017.
 */
public class MFCC {

    private int amountOfCepstrumCoef; //Number of MFCCs per frame
    protected int amountOfMelFilters; //Number of mel filters (SPHINX-III uses 40)
    protected float lowerFilterFreq; //lower limit of filter (or 64 Hz?)
    protected float upperFilterFreq; //upper limit of filter (or half of sampling freq.?)

    private int samplesPerFrame;
    private float sampleRate;
    int centerFrequencies[];

    public MFCC(int samplesPerFrame, int sampleRate){
        this(samplesPerFrame, sampleRate, 30, 30, 133.3334f, ((float)sampleRate)/2f);
    }

    public MFCC(int samplesPerFrame, float sampleRate, int amountOfCepstrumCoef, int amountOfMelFilters, float lowerFilterFreq, float upperFilterFreq) {
        this.samplesPerFrame = samplesPerFrame;
        this.sampleRate = sampleRate;
        this.amountOfCepstrumCoef = amountOfCepstrumCoef;
        this.amountOfMelFilters = amountOfMelFilters;
        //this.fft = new FFT(samplesPerFrame, new HammingWindow());

        this.lowerFilterFreq = Math.max(lowerFilterFreq, 25);
        this.upperFilterFreq = Math.min(upperFilterFreq, sampleRate / 2);
        calculateFilterBanks();
    }

    public void applyMFCC(float bin[]){
        float[] melFilterData = melFilter(bin, centerFrequencies);

        float[] nonLinearTransformationData = nonLinearTransformation(melFilterData);

        float[] cepCoefficientsData = cepCoefficients(nonLinearTransformationData);
    }

    /**
     * calculates the FFT bin indices<br> calls: none<br> called by:
     * featureExtraction
     *
     */

    public final void calculateFilterBanks() {
        centerFrequencies = new int[amountOfMelFilters + 2];

        centerFrequencies[0] = Math.round(lowerFilterFreq / sampleRate * samplesPerFrame);
        centerFrequencies[centerFrequencies.length - 1] = (int) (samplesPerFrame / 2);

        double mel[] = new double[2];
        mel[0] = freqToMel(lowerFilterFreq);
        mel[1] = freqToMel(upperFilterFreq);

        float factor = (float)((mel[1] - mel[0]) / (amountOfMelFilters + 1));
        //Calculates te centerfrequencies.
        for (int i = 1; i <= amountOfMelFilters; i++) {
            float fc = (inverseMel(mel[0] + factor * i) / sampleRate) * samplesPerFrame;
            centerFrequencies[i - 1] = Math.round(fc);
        }

    }

    /**
     * Calculate the output of the mel filter
     * @param bin The bins.
     * @param centerFrequencies  The frequency centers.
     * @return Output of mel filter.
     */
    public float[] melFilter(float bin[], int centerFrequencies[]) {    //first
        float temp[] = new float[amountOfMelFilters + 2];

        for (int k = 1; k <= amountOfMelFilters; k++) {
            float num1 = 0, num2 = 0;

            float den = (centerFrequencies[k] - centerFrequencies[k - 1] + 1);

            for (int i = centerFrequencies[k - 1]; i <= centerFrequencies[k]; i++) {
                num1 += bin[i] * (i - centerFrequencies[k - 1] + 1);
            }
            num1 /= den;

            den = (centerFrequencies[k + 1] - centerFrequencies[k] + 1);

            for (int i = centerFrequencies[k] + 1; i <= centerFrequencies[k + 1]; i++) {
                num2 += bin[i] * (1 - ((i - centerFrequencies[k]) / den));
            }

            temp[k] = num1 + num2;
        }

        float fbank[] = new float[amountOfMelFilters];

        for (int i = 0; i < amountOfMelFilters; i++) {
            fbank[i] = temp[i + 1];
        }

        return fbank;
    }

    /**
     * the output of mel filtering is subjected to a logarithm function (natural logarithm)<br>
     * @param fbank Output of mel filtering
     * @return Natural log of the output of mel filtering
     */
    public float[] nonLinearTransformation(float fbank[]){      //second
        float f[] = new float[fbank.length];
        final float FLOOR = -50;

        for (int i = 0; i < fbank.length; i++){
            f[i] = (float) Math.log(fbank[i]);

            // check if ln() returns a value less than the floor
            if (f[i] < FLOOR) f[i] = FLOOR;
        }

        return f;
    }

    /**
     * Cepstral coefficients are calculated from the output of the Non-linear Transformation method<br>
     * @param f Output of the Non-linear Transformation method
     * @return Cepstral Coefficients
     */
    public float[] cepCoefficients(float f[]){          //third
        float cepc[] = new float[amountOfCepstrumCoef];

        for (int i = 0; i < cepc.length; i++){
            for (int j = 0; j < f.length; j++){
                cepc[i] += f[j] * Math.cos(Math.PI * i / f.length * (j + 0.5));
            }
        }

        for (int i = 0; i < cepc.length ; i++) {
            System.out.println(cepc[i]);
        }

        return cepc;
    }

    //helper methods

    /**
     * convert frequency to mel-frequency<br>
     * @param freq Frequency
     * @return Mel-Frequency
     */
    protected static float freqToMel(float freq){
        return (float) (2595 * log10(1 + freq / 700));
    }

    /**
     * calculates logarithm with base 10<br>
     * @param value Number to take the log of
     * @return base 10 logarithm of the input values
     */
    protected static float log10(float value){
        return (float) (Math.log(value) / Math.log(10));
    }

    /**
     * calculates the inverse of Mel Frequency<br>
     */
    private static float inverseMel(double x) {
        return (float) (700 * (Math.pow(10, x / 2595) - 1));
    }
}
