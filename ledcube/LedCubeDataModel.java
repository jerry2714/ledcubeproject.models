package ledcubeproject.models.ledcube;

/**
 * Created by Jerry on 2017/3/22.
 * 主體LED立方體的燈號資料模型
 * 此類別可設定、取得個別位置LED燈的顏色
 * 可產生4bytes可輸出燈號資料(1 byte 位置，3byte顏色，暫時以一個int表示(類ARGB模型，A的部分以1 byte的位置資訊取代))
 * 內部有buffer簡單管理這些可輸出燈號資料，也可在外部進行管理
 */
public class LedCubeDataModel extends LedCubeStructure {

    private LedCubeStructure previousFrame;

    private int[] insideBuffer;
    private int[] outputBuffer;
    private int currentUsedSize = 0;

    /**
     * @param sideLength LED立方體的邊長
     */
    public LedCubeDataModel(int sideLength) {
        super(sideLength);
        insideBuffer = new int[sideLength * sideLength * sideLength];
        //outputBuffer = new int[sideLength*sideLength*sideLength];
        previousFrame = new LedCubeStructure(sideLength);
        previousFrame.clear();
    }

    public static void main(String args[]) {
        LedCubeDataModel ledCubeDataModel = new LedCubeDataModel(6);
    }

    /**
     * 自動將設定的位置顏色加入buffer
     *
     * @param x   x座標
     * @param y   y座標
     * @param z   z座標
     * @param rgb
     */
    @Override
    public void setColor(int x, int y, int z, int rgb) {
        super.setColor(x, y, z, rgb);
        if (currentUsedSize >= insideBuffer.length) {
            regenerateCubeData();
            return;
        }
        insideBuffer[currentUsedSize] = generateOneData(x, y, z);
        currentUsedSize++;
    }

    /**
     * 產生指定座標的輸出燈號資料
     *
     * @param x
     * @param y
     * @param z
     * @return 4bytes可輸出燈號資料(1 byte 位置，3byte顏色，暫時以一個int表示(類ARGB模型，A的部分以1 byte位置取代))
     */
    public int generateOneData(int x, int y, int z) {
        return combinePosAndColor(coordinateConvert(x, y, z), getColor(x, y, z));
    }

    /**
     * 產生整個LED立方體的資料並放入buffer
     */
    public void regenerateCubeData() {
        int count = 0;
        for (int i = 0; i < getSideLength(); i++)
            for (int j = 0; j < getSideLength(); j++)
                for (int k = 0; k < getSideLength(); k++) {
                    insideBuffer[count] = generateOneData(k, j, i);
                    count++;
                }
        currentUsedSize = count;
    }

    /**
     * 清空buffer
     */
    public void clearBuffer() {
        currentUsedSize = 0;
    }

    public void clear() {
        super.clear();
        clearBuffer();
        setColor(0, 0, 0, 0);
    }

    /**
     * 取得儲存目前的尚未取得過的資料(呼叫一次後會清空buffer，再次呼叫不會得到相同的buffer)
     *
     * @return 輸出buffer
     */
    public int[] getOutputBuffer() {
        if (currentUsedSize == 0) return null;
        outputBuffer = new int[currentUsedSize];
        for (int i = 0; i < currentUsedSize; i++)
            outputBuffer[i] = insideBuffer[i];
        /*for(int i = currentUsedSize; i < outputBuffer.length; i++)
            outputBuffer[i] = 0;*/
        currentUsedSize = 0;
        return outputBuffer;
    }

    public int coordinateConvert(int x, int y, int z) {
        int l = getSideLength();
        if (y % 2 == 0)
            return z * l * l + y * l + x;
        else
            return z * l * l + y * l + (l - x - 1);
    }

    private int combinePosAndColor(int pos, int color) {
        int result = color & 0x00FFFFFF;
        result = result | (pos << 24);
        return result;
    }


}
