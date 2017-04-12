package ledcubeproject.models.ledcubecontroller;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 用以輸出資料至LED立方體的控制裝置，並擁有包含製作命令碼的公能，可以依照不同的通訊方式來繼承擴展，例如藍芽、wifi等等
 * Created by Jerry on 2017/3/20.
 */
public class LedCubeController {

    public static void main(String args[])
    {
        /*LedCubeController deviceController = new LedCubeController();

        byte[] command = deviceController.command( OUTPUT, DISPLAY+CLEAR_AND_SET,  216);
        for(int i = 0; i < command.length; i++)
            System.out.println(command[i]);*/
    }

    /*command number*/
    public static final int ASSIGN = -1;
    public static final int INTERRUPT = 0;
    public static final int DELAY = 1;
    public static final int OUTPUT = 2;

    /*assign oFlag*/
    public static final int NOT_DISPLAY = 0;
    public static final int DISPLAY = 1;

    /*assign fCode*/
    public static final int SET = 0;
    public static final int CLEAR_AND_SET = (1 << 1);

    public static final int DEFAULT_COMMAND_LENGTH = 3;

    private byte[] command = null;

    private OutputStream outputStream = null;
    private InputStream inputStream = null;

    public LedCubeController()
    {
        command = new byte[DEFAULT_COMMAND_LENGTH];
    }

    /**
     * 選擇將要執行的命令，產生對應的指令碼，(主要資料見指令規格)
     * @param command  命令
     * @param funcCode  一些指令會有比較特殊的funcCode可在此指定，沒有的話請填 0，會直接以command為準自動填入
     * @param number
     * @return  指令碼
     */
    public byte[] command(int command, int funcCode, int number)
    {
        byte dataFlag = 0;
        switch(command)
        {
            case ASSIGN:
                dataFlag = 1;
                break;
            case INTERRUPT: case DELAY: case OUTPUT:
                dataFlag = 0;
                funcCode = command;
                break;
        }
        this.command[0] = (byte) (dataFlag + (funcCode << 1));
        this.command[1] = (byte) ( (number >> 8)& 0x00ff);
        this.command[2] = (byte) (number & 0x00ff);

        return this.command;
    }

    public void sendCommand(int command, int funcCode, int number) throws IOException
    {
        send(command(command, funcCode, number));
    }

    /**
     * 簡單包裝"中斷"命令
     * @throws IOException
     */
    public void interrupt() throws IOException
    {
        sendCommand(INTERRUPT, 0, 0);
    }

    /**
     * 簡單包裝"延時"命令
     * @param microsec  延多少微秒
     * @throws IOException
     */
    public void delay(int microsec) throws IOException
    {
        sendCommand(DELAY, 0, microsec);
    }

    /**
     * 簡單包裝"送出"命令
     * @throws IOException
     */
    public void show() throws IOException
    {
        sendCommand(OUTPUT, 0, 0);
    }


    /**
     * 對已連線的裝置送出一組資料
     * @param buf
     * @throws IOException
     */
    public void send(byte[] buf) throws IOException {
        outputStream.write(buf);
        //Log.d("mytest", "send");
    }

    public void send(byte b) throws  IOException{
        outputStream.write(b);
    }

    /**
     *將一個4bytes的整數拆成 <code> byte[] </code> 來傳送，高位元組最先傳，低位元組最後傳
     * @param data  欲傳送的整數
     * @throws IOException
     */
    public void send(int data) throws IOException {
        byte[] buf = new byte[4];
        for (int i = 0; i < buf.length; i++) {
            buf[buf.length - i - 1] = (byte) data;
            //Log.d("mytest", "" + buf[buf.length - i - 1]);
            data = data >> 8;
        }
        send(buf);
    }

    //public abstract boolean connect();

    /**
     * 替換輸出串流，供不同的裝置實作方式(ex. 藍牙)來呼叫以順利使用相同的方法來傳送命令與資料
     * @param o 欲使用的裝置所提供的OutputStream
     */
    protected void setOutputStream(OutputStream o){outputStream = o;}

    /**
     * 替換輸入串流，供不同的裝置實作方式(ex. 藍牙)來呼叫以順利順利使用相同的方法來接收資料
     * @param i 欲使用的裝置所提供的InputStream
     */
    protected void setInputStream(InputStream i){inputStream = i;}
}
