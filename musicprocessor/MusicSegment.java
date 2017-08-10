package ledcubeproject.models.musicprocessor;

/**
 * 此類別用以紀錄一段連續的音樂pcm data
 * Created by Jerry on 2017/5/30.
 */


import java.util.ArrayList;

/**
 * 用來儲存一段已解碼成PCM data的音樂資料
 *
 * @param <E>
 */
class MusicSegment<E> extends ArrayList<E> implements Comparable<MusicSegment> {

    private int start;

    public MusicSegment(int start) {
        super();
        this.start = start;
    }

    static private boolean mergeable(MusicSegment destination, MusicSegment source) {
        int sourceHead = source.getStartPosition();
        if (destination.checkInside(sourceHead) || destination.checkInside(sourceHead - 1)) {
            //System.out.println("true");
            return true;
        } else {
            //System.out.println("false");
            return false;
        }
    }

    /**
     * 檢查某個位置的frame是否在這個音樂片段之中
     *
     * @param pos 代表這個frame是整首音樂中的位置
     * @return
     */
    public boolean checkInside(int pos) {
        if (pos <= start + this.size() - 1 && pos >= start) {
            // System.out.println(pos + " " + start + " " + (start+length-1) + " true");
            return true;
        } else {
            // System.out.println(pos + " " + start + " " + (start+length-1) + " false");
            return false;
        }
    }

    public boolean add(E e, int pos) {
        if (start + this.size() == pos)
            return add(e);
        else
            return false;
    }

    /**
     * 把兩個音樂片段合併
     *
     * @param destination
     * @param source
     * @return
     */
//    static public boolean merge(MusicSegment destination, MusicSegment source)
//    {
//        if(mergeable(destination, source))
//        {
//            int startPoint = destination.getLength() + destination.getStartPosition();
//            System.out.println(startPoint);
//            if(source.checkInside(startPoint))
//            {
//                ArrayList des = destination.getList();
//                ArrayList src = source.getList();
//                int pos = startPoint-source.getStartPosition();
//                for(int i = 0; i < pos; i++)
//                {
//                    src.remove(0);
//                }
//                des.addAll(src);
//                destination.updateLength();
//            }
//            return true;
//        }
//        return false;
//    }
    public int getStartPosition() {
        return start;
    }

    @Override
    public int compareTo(MusicSegment o) {
        return this.getStartPosition() - o.getStartPosition();
    }
}