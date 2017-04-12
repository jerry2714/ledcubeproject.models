package ledcubeproject.models.musicprocessor.decoder;

import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;

/**
 * Created by Jerry on 2017/1/26.
 */
public class decodeTest {

    public static void main(String args[])
    {
        Mp3Decoder mp3Decoder;

        if(args.length != 1){
            System.out.println("no file input");
            return;
        }

        try {
            mp3Decoder = new Mp3Decoder(args[0]);

            AudioDevice audev = FactoryRegistry.systemRegistry().createAudioDevice();
            mp3Decoder.bindAudioDevice(audev);
            int max = Integer.MAX_VALUE;
            boolean ret = true;
            short pcm[];
            while (max-- > 0 && ret) {
                pcm = mp3Decoder.decodeFrame();
                if(pcm == null) ret = false;
                audev.write(pcm, 0, pcm.length);
            }
        }catch (Exception e){}
    }
}
