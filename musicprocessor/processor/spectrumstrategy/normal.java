package ledcubeproject.models.musicprocessor.processor.spectrumstrategy;

public class normal implements SpectrumStrategy{
        @Override
        public int[] execute(double[] spectrum, int sampleRate) {
            int num = 4900 * spectrum.length / sampleRate;
            int[] s = new int[num];
            int max = 0;
            for(int i = 0; i < num; i++)
            {
                s[i] = (int) (spectrum[i] / 1000);
                if(max < spectrum[i])
                    max = (int) spectrum[i];
            }
            System.out.println(max);
            return s;

        }
}
