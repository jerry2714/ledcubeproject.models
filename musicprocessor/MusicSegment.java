package ledcubeproject.models.musicprocessor;

/**
 * Created by Jerry on 2017/5/30.
 */

import java.util.ArrayList;

/**
 * 用來儲存一段已解碼成PCM data的音樂資料
 * @param <E>
 */
class MusicSegment<E>
{
    private ArrayList<E> segment;
    private int start, length;

    public MusicSegment(ArrayList<E> list, int start, int length)
    {
        segment = list;
        this.start = start;
        this.length = length;
    }

    /**
     * 檢查某個位置的frame是否在這個音樂片段之中
     * @param pos   代表這個frame是整首音樂中的位置
     * @return
     */
    public boolean checkInside(int pos)
    {
        if(pos < start+length-1 && pos >= start)
            return true;
        else
            return false;
    }

    /**
     * 把兩個音樂片段合併
     * @param destination
     * @param source
     * @return
     */
    static public boolean merge(MusicSegment destination, MusicSegment source)
    {
        if(destination.checkInside(source.getStartPosition()))
        {
            int startPoint = destination.getLength() + destination.getStartPosition();
            System.out.println(startPoint);
            if(source.checkInside(startPoint))
            {
                ArrayList des = destination.getList();
                ArrayList src = source.getList();
                int pos = startPoint-source.getStartPosition();
                for(int i = 0; i < pos; i++)
                {
                    src.remove(0);
                }
                des.addAll(src);
            }
            return true;
        }
        return false;
    }

    public int getStartPosition(){return start;}

    public int getLength(){return length;}

    public ArrayList getList(){return segment;}
}
