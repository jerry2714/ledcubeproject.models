package ledcubeproject.models.musicprocessor.processor;

/**
 * 配合SimpleSpectrumAnalyzer，目的在於提供SimpleSpectrumAnalyzer可替換的輸出圖形產生方法
 * Created by Jerry on 2017/7/10.
 */
interface SpectrumStrategy {
    //static final int ranges[] = {0, 200, 400, 800, 1600, 3200, 6400};
    static final int amount = 250;

    /**
     * 處理頻譜，產生想要的圖形
     * @param spectrum  頻譜
     * @param sampleRate 取樣頻率
     * @return  目標圖形
     */
    int[] execute(double[] spectrum, int sampleRate);


}

class EmptySpectrumStrategy implements SpectrumStrategy{

    @Override
    public int[] execute(double[] spectrum, int sampleRate) {
        return new int[0];
    }
}
