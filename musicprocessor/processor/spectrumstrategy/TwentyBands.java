package ledcubeproject.models.musicprocessor.processor.spectrumstrategy;

public class TwentyBands implements SpectrumStrategy{
    @Override
    public int[] execute(double[] spectrum, int sampleRate) {
        final int lowLimit[] = {89, 112, 141, 178, 224, 282, 355, 447, 562, 708, 891, 1122, 1413, 1778, 2239, 2818, 3548, 4467, 5623, 7079};
        final int upperLimit[] = {112, 141, 178, 224, 282, 355, 447, 562, 708, 891, 1122, 1413, 1778, 2239, 2818, 3548, 4467, 5623, 7079, 8913};
        //final int center[] =     {63, 100, 160, 250, 400, 630, 1000, 1600, 2500, 4000};
        final int limitMax = 6;
        final int limitMin = 0;
        final int amount = 20;
        final int seg = 6;
        double sum = 0;
        double[] s = new double[amount];
        for (int i = 0; i < s.length; i++) {
            s[i] = max(spectrum, sampleRate, lowLimit[i], upperLimit[i]);
        }
        double max = 0, min = (double) Integer.MAX_VALUE;
        for (int i = 0; i < s.length; i++) {
            //s[i] = log(2, s[i]);
            if (max < s[i])
                max = s[i];
        }
        System.out.println(max);
        final int idealMax = 200000;
        if (max < idealMax) max = idealMax;
        for (int i = 0; i < s.length; i++) {
            double d = scale(s[i], 0, max, limitMin, limitMax);
            s[i] = (int) d;
            if (d - (int) d > 0.2)
                s[i]++;
            s[i] *= 100;
        }
        int[] result = new int[s.length];
        for (int i = 0; i < result.length; i++)
            result[i] = (int) s[i];
        return result;

    }

    private double scale(double num, double min, double max, double limitMin, double limitMax) {
        return (limitMax - limitMin) * (num - min) / (max - min) + limitMin;
    }

    private double max(double[] spectrum, int sampleRate, int r1, int r2) {
        r1 = r1 * spectrum.length / (sampleRate);
        r2 = r2 * spectrum.length / (sampleRate);
        double max = 0;
        r1 = r1 < spectrum.length ? r1 : spectrum.length - 1;
        r2 = r2 < spectrum.length ? r2 : spectrum.length - 1;
        for (int i = r1; i <= r2; i++) {
            if (max < spectrum[i])
                max = spectrum[i];
        }
        return max;
    }

    private double average(double[] spectrum, int sampleRate, int r1, int r2) {
        r1 = r1 * spectrum.length / (sampleRate);
        r2 = r2 * spectrum.length / (sampleRate);
        double avg = 0;
        r1 = r1 < spectrum.length ? r1 : spectrum.length - 1;
        r2 = r2 < spectrum.length ? r2 : spectrum.length - 1;
        for (int i = r1; i <= r2; i++)
            avg += spectrum[i];
        avg /= (r2 - r1 + 1);
        return avg;
    }
}
