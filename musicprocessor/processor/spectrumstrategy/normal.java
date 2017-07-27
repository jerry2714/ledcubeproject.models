package ledcubeproject.models.musicprocessor.processor.spectrumstrategy;

import static org.apache.commons.math3.util.FastMath.log;

public class normal implements SpectrumStrategy{
        @Override
        public int[] execute(double[] spectrum, int sampleRate) {
            int num = 4900 * spectrum.length / sampleRate;
            int[] s = new int[num];
            int max = 0;
            for(int i = 0; i < num; i++)
            {
                s[i] = (int) (log(10, spectrum[i])*200 - 500);
                if(max < spectrum[i])
                    max = (int) spectrum[i];
            }
            System.out.println(max);
            return s;

        }
}
