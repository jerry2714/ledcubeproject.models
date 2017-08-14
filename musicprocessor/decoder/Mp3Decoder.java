package ledcubeproject.models.musicprocessor.decoder;

import java.io.FileInputStream;
import java.io.IOException;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.AudioDevice;

/**
 * Created by Jerry on 2017/1/25.
 * create github repo on 2017/1/27.
 */
public class Mp3Decoder implements MusicDecoder {
    private Bitstream bitstream;
    private Decoder decoder;
    private boolean ready = false;
    private String fileName = "";
    private FileInputStream fin;

    private int currentPos; //下一次會被解碼的frame的位置
    private int msPerFrame = 0;    //每個frame佔多少millisecond
    private int duration = 0;      //全長共多少millisecond

    public Mp3Decoder() {
        fileName = "";
        currentPos = 0;
    }

    public Mp3Decoder(String fileName) {
        if (fileName != null)
            init(fileName);
    }

    /**
     * 初始化，包括將檔案開啟
     *
     * @param fileName 欲打開的音樂檔名(路徑)
     */
    public void init(String fileName) {
        ready = false;
        msPerFrame = 0;
        currentPos = 0;
        try {
            if (fileName != null) {
                this.fileName = fileName;
                fin = new FileInputStream(fileName);
                //bin = new BufferedInputStream(fin);
                //bin.reset();
                bitstream = new Bitstream(fin);
                decoder = new Decoder();
            }
            ready = true;
            calculateTimeInfo();
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解碼一個frame。
     * <p>若要指定解碼特定位置的的frame，請先呼叫 {@link #changePosition(int pos)}。若已經沒有frame可以被解碼，則會跳回檔案的開頭位置
     *
     * @return 解碼出的 pcm data，若為null則表示最後一個frame已經被解碼完了
     */
    public short[] decodeFrame() {
        if (!ready) return null;
        short pcm[] = null;
        try {
            Header h = bitstream.readFrame();
            if (h == null) {
                System.out.println("null");
                return null;
            }
            pcm = ((SampleBuffer) decoder.decodeFrame(h, bitstream)).getBuffer();
            short[] p = new short[pcm.length];
            for (int i = 0; i < pcm.length; i++)
                p[i] = pcm[i];
            pcm = p;
            currentPos++;
            bitstream.closeFrame();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pcm;
    }

    /**
     * 變更下一個可以被解碼的frame的位置
     *
     * @param pos 欲換到的位置
     */
    public int changePosition(int pos) {
        try {
            if (pos < currentPos)
                refresh();
            while (currentPos < pos)
                if (!skipFrame()) break;
        } catch (BitstreamException e) {
            e.printStackTrace();
        }
        return currentPos;
    }

    public int getCurrentPosition() {
        return currentPos;
    }

    private void refresh() {
        if (fileName != null && !fileName.equals("")) {
            try {
                fin.getChannel().position(0);
                bitstream = new Bitstream(fin);
                decoder = new Decoder();
                currentPos = 0;
                System.out.println("refresh");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean skipFrame() throws BitstreamException {
        Header h = bitstream.readFrame();
        if (h == null) return false;
        bitstream.closeFrame();
        currentPos++;
        return true;
    }

//    public void turnBackFrame(int num)
//    {
//        for(int i = 0; i < num; i++)
//            try {
//                bitstream.unreadFrame();
//            } catch (BitstreamException e) {
//                e.printStackTrace();
//                return;
//            }
//    }

    /**
     * 將音訊裝置和decoder連結在一起
     *
     * @param audev 欲連結的音訊裝置
     * @throws JavaLayerException
     */
    public void bindAudioDevice(AudioDevice audev) throws JavaLayerException {
        if (audev != null)
            audev.open(decoder);
    }

    /**
     * 取得採樣頻率
     *
     * @return 採樣頻率
     */
    public int getSampleRate() {
        return decoder.getOutputFrequency();
    }

    public Decoder getDecoder() {
        return decoder;
    }

    private void calculateTimeInfo() {
        Header h = null;
        int count = 0;
        try {
            h = bitstream.readFrame();
        } catch (BitstreamException ex) {
            ex.printStackTrace();
        }
        msPerFrame = (int) h.ms_per_frame();
//        long tn = 0;
//        try {
//            tn = fin.getChannel().size();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        duration = (int) h.total_ms((int) tn);
        // *** 某些歌曲出現計算出的長度超出實際長度的情況，故改採整個檔案跑完計數的方式
        do {
            try {
                bitstream.closeFrame();
                h = bitstream.readFrame();
                count++;
            } catch (BitstreamException e) {
                e.printStackTrace();
            }
        }while (h != null);
        duration = count * msPerFrame;
    }

    /**
     * 取得一個frame佔幾毫秒
     *
     * @return 單位為毫秒
     */
    public int getMsPerFrame() {
        return msPerFrame;
    }

    /**
     * 取得目前音樂檔的全長
     *
     * @return 單位為毫秒
     */
    public int getDuration() {
        return duration;
    }
}
