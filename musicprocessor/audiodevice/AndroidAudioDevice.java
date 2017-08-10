package ledcubeproject.models.musicprocessor.audiodevice;

/**
 * Created by Jerry on 2017/1/26.
 */

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDeviceBase;

public class AndroidAudioDevice extends AudioDeviceBase {

    private AudioTrack audioTrack = null;
    private boolean ready = false;
    private byte byteBuffer[] = new byte[4096];


    @Override
    protected void writeImpl(short[] samples, int offs, int len)
            throws JavaLayerException {
        if (audioTrack == null)
            createAudioTrack();
        if (ready == false) return;
        byte b[] = toByteArray(samples, offs, len);
        audioTrack.write(b, 0, len * 2);
    }

    public synchronized void open(Decoder decoder) throws JavaLayerException {
        if (getDecoder() != decoder) {
            if (audioTrack != null) {
                audioTrack.pause();
                audioTrack.flush();
                audioTrack.release();
            }
            audioTrack = null;
        }
        super.open(decoder);
    }

    protected void openImpl() {
        if (audioTrack != null)
            audioTrack.play();
    }

    protected void closeImpl() {
        if (audioTrack != null)
            audioTrack.pause(); //根據AudioDeviceBase定義必須及時停止並且不能flush，所以不能用stop()並且不能在pause後接flush()
    }

    protected void flushImpl() {
        int state;
        if (audioTrack == null) return;
        while (true) {
            state = audioTrack.getState();
            if (state == AudioTrack.PLAYSTATE_STOPPED) {
                //System.out.println("STOP");
                return;
            } else if (state == AudioTrack.PLAYSTATE_PAUSED)
                audioTrack.play();
        }
    }

    private byte[] toByteArray(short[] samples, int offs, int len) {
        byte b[];
        if (len * 2 > byteBuffer.length) b = new byte[len * 2];
        else b = byteBuffer;
        int idx = 0;
        short s;
        while (len-- > 0) {
            s = samples[offs++];
            b[idx++] = (byte) s;
            b[idx++] = (byte) (s >>> 8);
        }
        return b;
    }

    private void createAudioTrack() {
        Decoder decoder = getDecoder();
        int rate, channel, format, minBufSize;
        format = AudioFormat.ENCODING_PCM_16BIT;
        switch (decoder.getOutputChannels()) {
            case 1:
                channel = AudioFormat.CHANNEL_OUT_MONO;
                break;
            case 2:
                channel = AudioFormat.CHANNEL_OUT_STEREO;
                break;
            default:
                channel = AudioFormat.CHANNEL_OUT_MONO;
                break;
        }
        rate = decoder.getOutputFrequency();
        minBufSize = AudioTrack.getMinBufferSize(rate, channel, format);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, rate, channel, format, minBufSize, AudioTrack.MODE_STREAM);
        audioTrack.play();
        ready = true;
    }


    /**
     * 還不想做0.0
     *
     * @return
     */
    @Override
    public int getPosition() {
        return 0;
    }
}
