package ledcubeproject.models.musicprocessor;

import java.util.ArrayList;
import java.util.Collections;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.FactoryRegistry;
import ledcubeproject.models.musicprocessor.decoder.Mp3Decoder;
import ledcubeproject.models.musicprocessor.processor.SimpleSpectrumAnalyzer;
import javazoom.jl.player.AudioDevice;


/**
 * Created by Jerry on 2017/2/1.
 */
public class Player{

    private Mp3Decoder mp3Decoder = null;
    private AudioDevice audev = null;
    private short pcm[];
    private double spectrum[];

    private int sampleRate;
    private boolean pause = true;
    private int currentPos = 0; //下一個要播放的frame的位置

    private MusicSegment<short[]> playback;     //正要使用的PCM data segment
    private ArrayList<MusicSegment<short[]>> segmentList= new ArrayList<>(); //所有目前的檔案已解碼出的PCM data segments

    private SimpleSpectrumAnalyzer simpleSpectrumAnalyzer = new SimpleSpectrumAnalyzer();

    public static void main(String args[]) throws JavaLayerException {
        Player player = new Player(FactoryRegistry.systemRegistry().createAudioDevice(), args[0]);
        //player.setPosition(10000);
        //player.play();
        int count = 0;
        while(player.playFrame(count))
        {
            count++;
            if(count == 100) count = 0;
        }
       //player.test();
    }

    public Player(AudioDevice ad)
    {
        audev = ad;
    }

    public Player(AudioDevice ad, String fileName)
    {
        this(ad);
        init(fileName);
    }

    /**
     * 呼叫以開始處理一個新的音樂檔案
     * @param fileName
     */
    public void init(String fileName)
    {
        try {
            mp3Decoder = new Mp3Decoder(fileName);
            mp3Decoder.bindAudioDevice(audev);
            sampleRate = 0;
            currentPos = 0;
            playback = new MusicSegment<>(0);
            segmentList.clear();

        }catch (Exception e){}
    }

    /**
     * 一個簡單的播放功能，呼叫後會把一首音樂播完，播完後才會return，會占用執行緒
     * @return  0代表檔案播放完畢，-1代表錯誤，1代表暫停
     */
    public int play()
    {
        int ret = -1;
        if(audev == null || mp3Decoder == null)
            return ret;
        try {
            audev.open(mp3Decoder.getDecoder());
            pause = false;
        } catch (JavaLayerException e) {
            e.printStackTrace();
            pause = true;
            return ret;
        }
        try {
            //找尋要播放的開頭位置是否已經解碼過
            /*playback = null;
            for(MusicSegment s : segmentList)
            {
                if(s.checkInside(currentPos))
                {
                    playback = s;
                }
            }*/
           /* ArrayList<short[]> record;
            if(playback == null)
            {
                record = new ArrayList<>();
                playback = new MusicSegment<>(record, currentPos, 0);
                segmentList.add(playback);
            }
            else
                record = playback.getList();*/
            while (!pause) {
               /* for(int i = 0; i < record.size() && !pause; i++)
                {
                    pcm = record.get(i);
                    audev.write(pcm, 0, pcm.length);
                    currentPos++;
                }*/
                pcm = mp3Decoder.decodeFrame();
                if(pcm == null)
                {
                    pause = true;
                    ret = 0;
                    break;
                }
                audev.write(pcm, 0, pcm.length);
               // record.add(pcm);
                currentPos++;
                ret = 1;
            }
            //playback.updateLength();
            return ret;
        }catch (JavaLayerException e){
            e.printStackTrace();
            pause = true;
            return  -1;
        }
    }

    public void setPosition(int pos)
    {
        pause();
        mp3Decoder.changePosition(pos);
        currentPos = pos;
    }

    public boolean playFrame(int pos)
    {
        pause = false;
        pcm = null;

        for(int i = 0; true; i++)
        {
            if(playback != null && playback.checkInside(pos))
            {
                int p = pos - playback.getStartPosition();
                pcm = playback.get(p);
                System.out.println("found " + p);
                break;
            }
            else if( !(i < segmentList.size()) )
                break;
            else
            {
                playback = segmentList.get(i);
                System.out.println("next");
            }

        }
        if(pcm == null)
        {
            System.out.println("null");
            mp3Decoder.changePosition(pos);
            pcm = mp3Decoder.decodeFrame();
            if(pcm == null)
                return false;
            if(!playback.add(pcm, pos))
            {
                System.out.println("add fault" + pos);
                if(playback.size() > 0)
                {
                    segmentList.add(playback);
                    System.out.println("add into list");
                }
                playback = new MusicSegment<>(pos);
                System.out.println(playback.add(pcm, pos));

            }
            else
            {
                System.out.println("add " + pos);
            }

        }
        try {
            audev.write(pcm, 0, pcm.length);
            currentPos = pos + 1;
        } catch (JavaLayerException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void test()
    {
        ArrayList<short[]> list = new ArrayList<>();
        for(int i = 0; i < 100; i++)
        {
            pcm = mp3Decoder.decodeFrame();
            System.out.println(pcm.length);
            try {
                audev.write(pcm, 0, pcm.length);
            } catch (JavaLayerException e) {
                e.printStackTrace();
            }
            currentPos++;
            short[] p = new short[pcm.length];
            for(int j = 0; j < pcm.length; j++)
                p[j] = pcm[j];
            list.add(p);
        }
        for(short[] p : list)
            try {
                audev.write(p, 0, p.length);
            } catch (JavaLayerException e) {
                e.printStackTrace();
            }
    }

    /**
     * 暫停播放
     */
    public void pause()
    {
        pause = true;
        audev.close();
    }

    /**
     * 回傳是否正在播放
     * @return  true代表正在播放，反之false
     */
    public boolean isPlaying()
    {
        return !pause;
    }

    public int[] playOneFrame()
    {
        if(audev == null || mp3Decoder == null)
            return null;
        pcm = mp3Decoder.decodeFrame();
        if(pcm == null)
            return  null;
        try{
            audev.write(pcm, 0, pcm.length);
            int sum = 0;
            for(int a : pcm)
                sum += a;
            sum /= pcm.length;
            for(int i = 0; i < pcm.length; i++)
                pcm[i] -= sum;
            spectrum = simpleSpectrumAnalyzer.getSpectrum(pcm);
            if(sampleRate == 0)
            {
                sampleRate = mp3Decoder.getSampleRate();
                System.out.println("sample rate: "+ sampleRate);
            }
        }catch (Exception e){}
        return getCurrentSpectrum();
    }

//    public void updatePlayback()
//    {
//        int num = segmentList.size();
//        Collections.sort(segmentList);
//        for(int i = 0; i < num; i++)
//        {
//            MusicSegment seg1 = segmentList.get(i);
//            MusicSegment seg2 = segmentList.get(i+1);
//            if(MusicSegment.merge(seg1, seg2))
//            {
//                segmentList.remove(i+1);
//                i--;
//                num--;
//            }
//        }
//
//    }

    public int[] getCurrentSpectrum()
    {
        if(spectrum == null) return null;

        final int amount = 100;

        int[] s = new int[amount];
        int n = spectrum.length / amount;
        //int s[] = SpectrumStrategy.excute(spectrum, mp3Decoder.getSampleRate());
        for(int i = 0; i < s.length; i++)
        {
            s[i] = (int) spectrum[i] /1000;
        }
        return s;
    }

    static class SpectrumStrategy
    {
        static final int ranges[][] = {{20, 60}, {60, 250}, {250, 500}, {500, 2000}, {2000, 4000}, {4000, 6000}};
        static final int amount = ranges.length;
        static int[] excute(double[] spectrum, int sampleRate)
        {
            int count = 0;
            int max = 0;
            int freq;
            int band[] = new int[2];
            int[] result = new int[amount];
            for(int i = 0; i < ranges.length; i++)
            {
                max = 0;
                band[0] = ranges[i][0] * spectrum.length / sampleRate;
                band[1] = ranges[i][1] * spectrum.length / sampleRate;
                result[i] = (int)spectrum[band[0]];
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


}

