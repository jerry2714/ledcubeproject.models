package ledcubeproject.models.ledcube;

/**
 * Created by Jerry on 2017/3/2.
 * 以序列形式儲存LED立方體每顆燈的顏色
 */
public class LedSerialStructure {
    private int[] structure = null;
    private int sideLength = 1;

    /**
     * 以一個LedCubeStructure物件的燈數來建構
     *
     * @param l
     */
    public LedSerialStructure(LedCubeStructure l) {
        sideLength = l.getSideLength();
        structure = new int[sideLength * sideLength * sideLength];
    }

    /**
     * 以燈數來建構
     *
     * @param num 代表序列中共有幾顆燈
     */
    public LedSerialStructure(int num) {
        structure = new int[num];
    }

    /**
     * 直接把一整個LedCubeStructure儲存的燈號複製過來
     *
     * @param l 使用三維座標定位的模型
     * @return 如果兩個模型的燈數不符合則為false
     */
    public boolean setStructure(LedCubeStructure l) {
        int color;
        if (l.getSideLength() != sideLength)
            return false;
        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                for (int k = 0; k < sideLength; k++) {
                    color = l.getColor(k, j, i);
                    if (j % 2 == 0)
                        structure[i * sideLength * sideLength + j * sideLength + k] = color;
                    else
                        structure[i * sideLength * sideLength + j * sideLength + (sideLength - k - 1)] = color;
                }
            }
        }
        return true;
    }

    /**
     * 設定單顆燈的顏色
     *
     * @param num   代表第幾顆燈
     * @param color 欲設定的顏色
     */
    public void setColor(int num, int color) {
        if (structure.length > num)
            structure[num] = color;
    }

    /**
     * 取得指定的一個燈的顏色
     *
     * @param num 代表序列中第num個燈
     * @return 顏色
     */
    public int getColor(int num) {
        return num < structure.length ? structure[num] : 0;
    }

}
