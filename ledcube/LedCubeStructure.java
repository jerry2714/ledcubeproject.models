package ledcubeproject.models.ledcube;

/**
 * Created by Jerry on 2017/2/11.
 */
public class LedCubeStructure {
    private int[][][] structure = null;
    private int sideLength = 1;

    /**
     * 建立一個LED立方體的資料模型
     *
     * @param sideLength 用以建立n*n*n的方塊，若小於等於0則預設建立一個'1*1*1的方塊
     */
    public LedCubeStructure(int sideLength) {
        if (sideLength > 0)
            this.sideLength = sideLength;
        structure = new int[sideLength][sideLength][sideLength];
    }

    /**
     * 設定一顆LED燈的顏色
     *
     * @param x     x座標
     * @param y     y座標
     * @param z     z座標
     * @param color 以上座標的LED的顏色
     */
    public void setColor(int x, int y, int z, int color) {
        if (checkBound(x) && checkBound(y) && checkBound(z))
            structure[x][y][z] = color;
    }

    public void setColor(int x, int y, int z, int r, int g, int b) {
        int mask = 0x000000FF;
        int rgb = parseRGB(mask & r, mask & g, mask & b);
        setColor(x, y, z, rgb);
    }

    private int parseRGB(int r, int g, int b) {
        int rgb = 128;  //alpha value
        rgb = rgb << 8;
        rgb = rgb | r;
        rgb = rgb << 8;
        rgb = rgb | g;
        rgb = rgb << 8;
        rgb = rgb | b;
        return rgb;
    }

    /**
     * 清空LED立方體
     */
    public void clear() {
        for (int x = 0; x < sideLength; x++)
            for (int y = 0; y < sideLength; y++)
                for (int z = 0; z < sideLength; z++)
                    structure[x][y][z] = 0;
    }

    private boolean checkBound(int x) {
        if (x >= 0 && x < sideLength) return true;
        else return false;
    }

    public int getColor(int x, int y, int z) {
        return structure[x][y][z];
    }

    public int getSideLength() {
        return sideLength;
    }

}
