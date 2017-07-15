package ledcubeproject.models.musicprocessor.processor;


import org.jtransforms.fft.DoubleFFT_1D;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;


/**
 * Created by Jerry on 2017/1/30.
 */
public class SimpleSpectrumAnalyzer {

    private DoubleFFT_1D fft_1D;
    private double hannWindow[];
    private int inputArray[];
    private SpectrumStrategy spectrumStrategy = new EmptySpectrumStrategy();


    public static void main(String args[])
    {
        short pcm[] = new short[]{980,988,1160,1080,928,1068,1156,1152,1176,1264};

        SimpleSpectrumAnalyzer simpleSpectrumAnalyzer = new SimpleSpectrumAnalyzer();

        double spectrum[] = simpleSpectrumAnalyzer.getSpectrum(pcm);
        for(double d : spectrum)
            System.out.print(d + " ");
        System.out.println();
    }

    public SimpleSpectrumAnalyzer()
    {
        hannWindow = new double[0];
    }



    public double[] getFFTFromPCM(int[] pcm)
    {
        if(fft_1D == null)
            fft_1D = new DoubleFFT_1D(pcm.length);
        double[] fft = new double[pcm.length];
        for(int i = 0; i < pcm.length; i++)
            fft[i] = pcm[i];
        fft_1D.realForward(fft);
        return fft;
    }

    public double[] getMagnitude(double fft[])
    {
        if(fft == null) return null;
        double spectrum[] = new double[fft.length/2];
        //double n1, n2;
        //spectrum[0] = abs(fft[0]);
        for (int i = 0; i < fft.length / 2; i++)
            spectrum[i] = sqrt(fft[2*i]*fft[2*i] + fft[2*i + 1]*fft[2*i + 1]);
            //spectrum[i] = 10 * log(fft[2*i]*fft[2*i] + fft[2*i + 1]*fft[2*i + 1])/log(10);
        return spectrum;
    }

    public double[] getSpectrum(short[] pcm)
    {
        if(hannWindow.length != pcm.length){
            hannWindow = new double[pcm.length];
            // Hann window define
            for (int i=0; i<hannWindow.length; i++)
                hannWindow[i] = 0.5 * (1.0 - cos(2.0f*Math.PI*i / (float)(hannWindow.length-1)));
        }
        if(inputArray == null || inputArray.length != pcm.length)
            inputArray = new int[pcm.length];
        // use Hann window
        for(int i = 0; i < pcm.length; i++)
            inputArray[i] = (int)(pcm[i] * hannWindow[i]);
        return getMagnitude(getFFTFromPCM(inputArray));
    }

    /**
     * 取得想要的頻譜圖或者其他特殊圖形，圖形樣式取決於使用的策略
     * @param pcm
     * @param sampleRate
     * @return 圖形陣列
     */
    public int[] getOutput(short[] pcm, int sampleRate)
    {
        if(pcm == null) return null;
        return spectrumStrategy.execute(getSpectrum(pcm), sampleRate);
    }

    /**
     * 設定輸出的圖形所使用的策略
     * @param s 欲使用的策略
     */
    public void setSpectrumStrategy(SpectrumStrategy s)
    {
        spectrumStrategy = s;
    }

}
