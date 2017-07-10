package ledcubeproject.models.musicprocessor;

import java.util.ArrayList;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.JavaSoundAudioDevice;
import ledcubeproject.models.musicprocessor.decoder.Mp3Decoder;
import javazoom.jl.player.AudioDevice;
import ledcubeproject.util.Callback;

import static org.apache.commons.math3.util.FastMath.log;


/**
 * 簡易播放mp3播放器
 * Created by Jerry on 2017/2/1.
 */
public class Player{

    private Mp3Decoder mp3Decoder = new Mp3Decoder();
    private AudioDevice audev = null;
    private short pcm[];
    //private double spectrum[];

    private int sampleRate;
    private boolean pause = true;
    private int currentPos = 0; //下一個要播放的frame的位置
    private int offset = 0;     //

    private Callback playingAction = null;


    //private MusicSegment<short[]> playback;     //正要使用的PCM data segment
    private short[][] playback;
    //private ArrayList<MusicSegment<short[]>> segmentList= new ArrayList<>(); //所有目前的檔案已解碼出的PCM data segments


    public static void main(String args[]) throws JavaLayerException {
        final Player player = new Player(new JavaSoundAudioDevice(), args[0]);
        //player.setPosition(10000);
        Thread n = new Thread(){
            public void run()
            {
                player.play();
            }
        }; n.start();
        long t = System.nanoTime();
        while((System.nanoTime() - t) < (1000000000L * 10L));
        System.out.print((System.nanoTime() - t) / 1000000000.0);
        player.pause();
        //player.setPosition(300);
        //player.play();
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
            pause = true;
            audev.close();
            mp3Decoder.init(fileName);
            mp3Decoder.bindAudioDevice(audev);
            sampleRate = 0;
            currentPos = 0;
            playback = new short[(mp3Decoder.getDuration()/mp3Decoder.getMsPerFrame() +1)][];
            //segmentList.clear();
        }catch (Exception e){}
    }

    boolean pauseFlag;
    /**
     * 一個簡單的播放功能，呼叫後會把一首音樂播完，播完後才會return，會占用執行緒
     * @return  0代表檔案播放完畢，-1代表錯誤，1代表暫停
     */
    public int play()
    {
        int ret = -1;
        boolean decodeable = true;
        pause = false;
        if(audev == null || mp3Decoder == null)
            return ret;
        try {
            audev.open(mp3Decoder.getDecoder());
        } catch (JavaLayerException e) {
            e.printStackTrace();
            pause = true;
            return ret;
        }
        try {
            pauseFlag = false;
            pause = false;
            int playbackIndex = 0;
            while (!pauseFlag) {
                playbackIndex = mp3Decoder.getCurrentPosition();
                if(decodeable)
                    pcm = mp3Decoder.decodeFrame();
                if(pcm != null)
                {

                    playback[playbackIndex] = pcm;
                    if (playingAction != null)
                    {
                        playingAction.run();
                    }
                }
                else decodeable = false;
                if(playbackIndex > currentPos + offset || pcm == null)
                {
//                    System.out.println("c: " + currentPos);
                    if(playback[currentPos] == null)
                    {
                        ret = 0;
                        break;
                    }
                    ret = currentPos;
                    audev.write(playback[currentPos], 0, playback[currentPos].length);
                    currentPos++;
                }
            }
        }catch (JavaLayerException e){
            e.printStackTrace();
            pause = true;
            return  -1;
        }
        pause = true;
        return ret;
    }

    public void setPosition(int pos)
    {
        pause();
        mp3Decoder.changePosition(pos);
        currentPos = pos;
    }

//    public boolean playFrame(int pos)
//    {
//        pause = false;
//        pcm = null;
//        MusicSegment<short[]> temp = playback;
//        mp3Decoder.changePosition(pos);
//        if(playback != null && playback.checkInside(pos))
//        {
//            int p = pos - playback.getStartPosition();
//            pcm = playback.get(p);
//            if(playback.size() - p < 3) //在本段已解碼過的資料播放完之前提早開始解碼，若不提前則會有一個小小的不連貫，原因不明
//                mp3Decoder.decodeFrame();
//            //System.out.println("found " + pos);
//        }
////        for(int i = 0; true; i++)
////        {
////            if(playback != null && playback.checkInside(pos))
////            {
////                int p = pos - playback.getStartPosition();
////                pcm = playback.get(p);
////                //System.out.println("found " + p);
////                break;
////            }
////            else if( !(i < segmentList.size()) )
////                break;
////            else
////            {
////                playback = segmentList.get(i);
////                //System.out.println("next");
////            }
////
////        }
//        if(pcm == null)
//        {
//            playback = temp;
//            pcm = mp3Decoder.decodeFrame();
//            if(pcm == null)
//                return false;
//            if(!playback.add(pcm, pos))
//            {
//                if(playback.size() > 0 && !segmentList.contains(playback))
//                {
//                    segmentList.add(playback);
//                    playback = null;
//                }
//                for(MusicSegment<short[]> s : segmentList)
//                {
//                    if(s.checkInside(pos) || s.add(pcm, pos))
//                        playback = s;
//                }
//                if(playback == null)
//                {
//                    playback = new MusicSegment<>(pos);
//                    playback.add(pcm, pos);
//                }
//                //System.out.println(playback.add(pcm, pos));
//            }
//        }
//
//        try {
//            audev.write(pcm, 0, pcm.length);
//            currentPos = pos + 1;
//        } catch (JavaLayerException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }

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
        pauseFlag = true;
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

//    public int[] playOneFrame()
//    {
//        if(audev == null || mp3Decoder == null)
//            return null;
//        pcm = mp3Decoder.decodeFrame();
//        if(pcm == null)
//            return  null;
//        try{
//            audev.write(pcm, 0, pcm.length);
//            int sum = 0;
//            for(int a : pcm)
//                sum += a;
//            sum /= pcm.length;
//            for(int i = 0; i < pcm.length; i++)
//                pcm[i] -= sum;
//            spectrum = simpleSpectrumAnalyzer.getSpectrum(pcm);
//            if(sampleRate == 0)
//            {
//                sampleRate = mp3Decoder.getSampleRate();
//                System.out.println("sample rate: "+ sampleRate);
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return getCurrentSpectrum();
//    }

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

    public short[] getCurrentPCM()
    {
        return pcm;
    }



    public void setPlayingAction(Callback c)
    {
        playingAction = c;
    }

    /**
     * 取得一個frame佔幾毫秒
     * @return  單位為毫秒
     */
    public int getMsPerFrame(){return mp3Decoder.getMsPerFrame();}

    /**
     * 取得目前音樂檔的全長
     * @return  單位為毫秒
     */
    public int getDuration(){return mp3Decoder.getDuration();}

    /**
     * 取得目前音樂檔的全長
     * @return 格式為 mm : ss
     */
    public String getDurationFormatted()
    {
        String str = "";
        int min = (getDuration() / 1000) / 60;
        str += min;
        if(min < 10)
            str = 0 + str;
        str += " : ";
        String sec = "" + (getDuration() / 1000) % 60;
        if(sec.length() == 1)
            sec = 0 + sec;
        str += sec;
        return str;
    }

    /**
     * 取得目前播放到的位置
     * @return 單位為毫秒
     */
    public int getCurrentPosition()
    {
        return currentPos * getMsPerFrame();
    }

    /**
     * 取得目前播放到的位置
     * @return 格式為 mm : ss
     */
    public String getCurrentPositionFormatted()
    {
        String str;
        String min = "" + (currentPos*getMsPerFrame() / 1000) / 60;
        if(min.length() == 1)
            min = 0 + min;
        String sec = "" + (currentPos*getMsPerFrame() / 1000) % 60;
        if(sec.length() == 1)
            sec = 0 + sec;
        str = min + " : " + sec;
        return str;
    }
}

