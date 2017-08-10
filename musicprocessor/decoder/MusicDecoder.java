package ledcubeproject.models.musicprocessor.decoder;

/**
 * Created by Jerry on 2017/1/25.
 */
public interface MusicDecoder {

    /**
     * 取得下一個解碼後的frame
     *
     * @return PCM data
     */
    public short[] decodeFrame();
}
