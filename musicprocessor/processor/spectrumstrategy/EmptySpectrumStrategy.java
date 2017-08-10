package ledcubeproject.models.musicprocessor.processor.spectrumstrategy;

public class EmptySpectrumStrategy implements SpectrumStrategy {

    @Override
    public int[] execute(double[] spectrum, int sampleRate) {
        return new int[0];
    }
}
