package ledcubeproject.models.musicprocessor.processor;

/**
 * Created by Jerry on 2017/7/10.
 */
class SpectrumStrategy {
    //static final int ranges[] = {0, 200, 400, 800, 1600, 3200, 6400};
    static final int amount = 250;

    public int[] excute(double[] spectrum, int sampleRate) {
        int count = 0;
        int max = 0;
        int freq;
        int band[] = new int[2];
        int[] result = new int[amount];
//            for(int i = 0; i < ranges.length-1; i++)
//            {
//                max = 0;
//                band[0] = ranges[i] * spectrum.length / (sampleRate / 2);
//                band[1] = ranges[i+1] * spectrum.length / (sampleRate / 2);
//                result[i] = (int)spectrum[band[0]];
//            }
        for (int i = 0; i < result.length; i++) {
            int start = i * spectrum.length / amount;
            int end = (i + 1) * spectrum.length / amount;
            for (int j = start; j < end; j++) {
                if (spectrum[j] > max)
                    max = (int) spectrum[j];
            }
            result[i] = max;
        }
        return result;
    }

   /* static double [] getBigest(double[] array, int index1, int index2)
    {
        if(index1 - index2 <= 0) return null;
        double arr[] = new double[index1-index2+1];
        for()
    }*/
}
