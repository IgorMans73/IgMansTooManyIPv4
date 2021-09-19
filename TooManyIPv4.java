//package com.company;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashSet;

public class TooManyIPv4 {

    public static void main(String[] args) throws IOException {
        String fileS = "";
        if (args.length==0){
            fileS = "ip_adr_test";
        } else{
            fileS = args[0];
        }

        MyMappedByteBuffer myMappedByteBuffer = new MyMappedByteBuffer(fileS);

        PlanA planA = new PlanA();
        char a[];
        while ((a = myMappedByteBuffer.readLine()) != null) {
            planA.set(a);
        }
        planA.report();
        myMappedByteBuffer.closeAll();
    }
}

class PlanA{
    private long[] arrAdr;
    private int el;
    private int bt;
    private int ipUnic;
    PlanA(){
        arrAdr = new long[67108864]; // 0xffff_ffff / 64 + 1
        ipUnic = 0;
    }
    private void indxArrAdr(long ipAdr){
        Long r = (ipAdr/64L);
        el = r.intValue();
        r = ipAdr - (el*64);
        bt = r.intValue();
    }
    public void set(char[] strIPv4){
        long r = strIPv4toInt(strIPv4);
        indxArrAdr(r);
        long d = arrAdr[el];
        int k = (int) (d >> bt)&1;
        if (k==1){
            ipUnic--;
        } else {
            r = (1<<bt);
            d = d|r;
            arrAdr[el] = d;
            ipUnic++;
        }
    }
    public void report(){
        System.out.println("Plan A, The number of unique ip addresses is "
                + ipUnic
        );
    }
    private long strIPv4toInt(char[] strIPv4){
        long ret = 0;
        char[] ch = strIPv4;
        int i = ch.length - 1;
        int r = 0;
        int k = 0;
        int dec = 0;
        while (i>=0) {
            k = 0;
            while (ch[i] != '.') {
                switch (k) {
                    case 0:
                        r = (ch[i] - 48);
                        break;
                    case 1:
                        r = ((ch[i] - 48) * 10) + r;
                        break;
                    case 2:
                        r = ((ch[i] - 48) * 100) + r;
                        break;
                    }
                k++;
                i--;
                if (i<0) break;
            }
            i--;
            ret = ((long)r<<((long)dec*8)) | ret;
            dec++;
        }
        return ret;
    }

}
class MyMappedByteBuffer{
    private RandomAccessFile randomAccessFile;
    private FileChannel fileChannel;
    private MappedByteBuffer mappedByteBuffer;
    private long size;
    private long stepSize;
    private long positOne;
    private long positTo;
    private long pos;
    private long readSize = 0L;
    private final long STEPBUFF = 0xFFFFFF;

    MyMappedByteBuffer(String sourceFile) throws IOException {
        randomAccessFile = new RandomAccessFile(sourceFile,"r");
        fileChannel = randomAccessFile.getChannel();
        size = fileChannel.size();
        stepSize = STEPBUFF;
        if (size<stepSize) stepSize = size;
        positOne = 0L;
        positTo = positOne + stepSize;
        pos = 0L;
    }
    public void reStart(){
        stepSize = STEPBUFF;
        if (size<stepSize) stepSize = size;
        positOne = 0L;
        positTo = positOne + stepSize;
        indxBuffArr = null;
    }
    public long getSize(){
        return size;
    }
    public void closeAll() throws IOException {
        randomAccessFile.close();
        fileChannel.close();
    }
    private char[] read() throws IOException {
        if (stepSize<=0) return null;
        mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, positOne, stepSize);
        char[] arr = new char[(int)stepSize];
        readSize = readSize + stepSize;
        for (int i=0;i<(int)stepSize;i++) arr[i] = (char)mappedByteBuffer.get(i);
        //System.out.println(Arrays.toString(arr));
        return arr;
    }
    public long getReadSize(){return readSize;}

    private char[] buffArr;
    private int[] indxBuffArr;
    private int indx;

    public char[] readLine() throws IOException {
        char[] arr = null;
        if (indxBuffArr != null){
            int r = indx + 1;
            if (indxBuffArr[r]==0){
                indxBuffArr = null;
            } else {
                arr = Arrays.copyOfRange(buffArr,indxBuffArr[indx]+1,indxBuffArr[r]);
                indx = r;
                return arr;
            }
        }
        if (indxBuffArr == null){
            buffArr = read();
            if (buffArr==null) return null;
            indxBuffArr = new int[(int)stepSize];
            int k=1;
            if (buffArr[0] == '\n'){
                indxBuffArr[0] = 0;
            } else {
                indxBuffArr[0] = -1;
            }
            for (int i = 1; i < (int)stepSize;i++){
                if (buffArr[i] != '\n') continue;
                indxBuffArr[k] = i;
                k++;
            }

            if (k>1) arr = Arrays.copyOfRange(buffArr,indxBuffArr[0]+1,indxBuffArr[1]);
            if (k==1){
                if (indxBuffArr[0] == -1) {
                    arr = buffArr;
                } else {
                    arr = Arrays.copyOfRange(buffArr,1,buffArr.length-1);
                }
            }
            pos = 0;
            for (int i = (int)stepSize-1; i >= 0; i--){
                if (buffArr[i] == '\n') break;
                pos--;
            }
            positOne = positTo + pos;
            positTo = positTo + pos + stepSize;
            if (positTo>size){
                stepSize = size - positOne;
            }
            indx=1;
            return arr;
        }
        return arr;
    }
}

