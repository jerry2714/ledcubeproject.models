package ledcubeproject.models.ledcubecontroller;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;

/**
 * 用以輸出資料至LED立方體的控制裝置，並擁有包含製作命令碼的公能，可以依照不同的通訊方式來繼承擴展，例如藍芽、wifi等等
 * Created by Jerry on 2017/3/20.
 */
public class LedCubeController {

    public static void main(String args[])
    {
        LedCubeController deviceController = new LedCubeController();

        /*byte[] command = deviceController.command( ASSIGN, DISPLAY+SAME_COLOR_STRIP, 0,  216);
        for(int i = 0; i < command.length; i++)
            System.out.println(command[i]);*/
        deviceController.sameColorStrip(true, 0, 216, 0x00FFFF00);
    }

    /*command number*/
    public static final int ASSIGN = -1;
    public static final int INTERRUPT = 0;
    public static final int DELAY = (1 << 1);
    public static final int OUTPUT = (2 << 1);

    /*assign oFlag*/
    public static final int NOT_DISPLAY = 0;
    public static final int DISPLAY = (1 << 1);

    /*assign fCode*/
    public static final int SET = 0;
    public static final int CLEAR_AND_SET = (1 << 2);
    public static final int SET_BACKGROUND = (2 << 2);
    public static final int SAME_COLOR = (3 << 2);
    public static final int SAME_COLOR_STRIP = (4 << 2);
    public static final int STRIP = (5 << 2);

    public static final int DEFAULT_COMMAND_LENGTH = 3;

    private byte[] command = null;

    private OutputStream outputStream = null;
    private InputStream inputStream = null;

    private ArrayDeque<byte[]> transferQueue = new ArrayDeque<>();
    private int totalSizeInQueue = 0;
    private int availableTransferSize = 63;

//    /**
//     * 標記目前有沒有CommunicationThread正在執行中，用以避免同時有兩個CommunicatoinThread在執行的狀況
//     */
//    private boolean communicating = false;

//    class CommunicationThread extends Thread
//    {
//        byte[] buf = null;
//        byte[] s = new byte[1];
//        int transferSize = 0;
//        CommunicationThread(byte[] buf)
//        {
//            this.buf = buf;
//        }
//        @Override
//        public void run() {
//            long time = 0;
//            communicating = true;
//            int offset = 0;
////            try {
////                while (offset < buf.length) {
////                    if(availableTransferSize <= 0)
////                        while (inputStream.available() <= 0);
////                    time = System.nanoTime();
////                    while (inputStream.available() > 0) {
////                        availableTransferSize += inputStream.read();
////                    }
////                    time = System.nanoTime() - time;
////                    // System.out.println("wait: "+time);
////                    if(availableTransferSize < 0)
////                        availableTransferSize = 0;
////                    if(offset + availableTransferSize > buf.length)     //可傳量超過剩餘量
////                        transferSize = buf.length - offset;
////                    else transferSize = availableTransferSize;
////                    System.out.println("send: "+transferSize + "bytes");
////                    outputStream.write(buf, offset, transferSize);
////                    /*for(int j = 0; j < availableTransferSize; j++)
////                        System.out.println(buf[i+j]);*/
////                    offset = offset + transferSize;
////                    availableTransferSize -= transferSize;
////                }
////            }catch (IOException e)
////            {
////                e.printStackTrace();
////                return;
////            }
////            communicating = false;
//            try {
//                while(offset < buf.length)
//                {
//                    //time = System.nanoTime();
//                    if(offset + availableTransferSize > buf.length)
//                        transferSize = buf.length - offset;
//                    else transferSize = availableTransferSize;
//                    outputStream.write(buf, offset, transferSize);
//                    //while(System.nanoTime() - time < (1000000000 / 50000));
//                    //time = System.nanoTime();
//                    offset += availableTransferSize;
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                return;
//            }
//            communicating = false;
//        }
//    }


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
        this.command[0] = (byte) (dataFlag + funcCode);
        this.command[1] = (byte) ( (number >> 8)& 0x00ff);
        this.command[2] = (byte) (number & 0x00ff);

        return this.command;
    }

    public byte[] command(int command, int funcCode, int high, int low)
    {
        int number = ((high & 0x00FF) << 8) + (low & 0x00FF);
        return command(command, funcCode, number);
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


    public void sameColorStrip(boolean output, int offset, int number, int color)
    {
        byte[] buf = command(ASSIGN, (output?DISPLAY:NOT_DISPLAY)+SAME_COLOR_STRIP, offset, number);
        for(byte b : buf)
            System.out.println(b);
        addToQueue(buf);
        buf = new byte[3];
        for(int i = 0; i < 3; i++) {
            buf[i] = (byte) (color >> (8 * (3 - (i + 1))));
            //System.out.println(buf[i]);
        }
        addToQueue(buf);
    }


    /**
     * 加入一組資料到傳輸佇列裡
     * @param buf  資料
     */
    public void addToQueue(byte[] buf)
    {
        if(buf != null)
        {
            transferQueue.addLast(buf);
            totalSizeInQueue += buf.length;
        }
    }

    /**
     * 將一個 4 bytes 的資料加入到傳輸佇列裡
     * @param data 資料
     */
    public void addToQueue(int data)
    {
        byte[] buf = new byte[4];
        for (int i = 0; i < buf.length; i++) {
            buf[buf.length - i - 1] = (byte) data;
            data = data >> 8;
        }
        addToQueue(buf);
    }

    /**
     * 將傳輸佇列裡的資料輸出並清空
     * @throws IOException
     */
    public void sendQueue() throws IOException
    {
        byte[] buf = new byte[totalSizeInQueue];
        int i = 0;
        for(byte[] bs : transferQueue)
        {
            for(byte b : bs)
            {
                buf[i] = b;
                i++;
            }
        }
        send(buf);
        transferQueue.clear();
        totalSizeInQueue = 0;
    }

//    public boolean startTransmission()
//    {
//        if(communicating)
//            return false;
//        byte[] buf = new byte[totalSizeInQueue];
//        int i = 0;
//        for(byte[] bs : transferQueue)
//        {
//            for(byte b : bs)
//            {
//                buf[i] = b;
//                i++;
//            }
//        }
//        transferQueue.clear();
//        totalSizeInQueue = 0;
//        CommunicationThread ct = new CommunicationThread(buf);
//        communicating = true;
//        ct.setPriority(10);
//        ct.start();
//        return true;
//    }

//    public boolean isCommunicating()
//    {
//        return communicating;
//    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public int read() throws IOException
    {
        return inputStream.read();
    }

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
