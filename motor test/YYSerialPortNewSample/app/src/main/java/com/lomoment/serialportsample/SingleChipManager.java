package com.lomoment.serialportsample;

import android.content.Context;
import android.util.Log;

import com.lomoment.serialportsdk.VendingMachineKey;
import com.lomoment.serialportsdk.VendingMachineMananger;
import com.lomoment.serialportsdk.entity.LCMachineInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author libin
 * @date 2018/5/22
 * @Description single chip manager
 */

public class SingleChipManager implements VendingMachineMananger.VendingmachineResponseListener, VendingMachineMananger.MachineQueryResponseListener {
    public static final String TAG = SingleChipManager.class.getSimpleName();
    //serial port pa
    public static final String SERICAL_PORT_PATH = "/dev/ttyS4";

    public static final int BAUDRATE = 115200;

    private static SingleChipManager mInstance;
    private VendingMachineMananger serialPort;

    private Map<String, DeliverInfo> sendReq;

    public DeliverDiSposeListener deliverDiSposeListener;

    private SingleChipManager() {
        sendReq = new HashMap<>();
    }

    public static SingleChipManager getInstance() {
        if (mInstance == null) {
            synchronized (SingleChipManager.class) {
                if (mInstance == null) {
                    mInstance = new SingleChipManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * init serial port
     *
     * @param context
     */
    public void init(Context context, String path, int baudrate) {
        if (serialPort != null) {
            serialPort.release();
        }
        serialPort = new VendingMachineMananger(context);
        //open log
        serialPort.isShowLog = true;
        //set serial port  path
        serialPort.init(path, baudrate);
        //set deliver listener
        serialPort.setVendingmachineResponseListener(this);
        //set query machine listener
        serialPort.setMachineQueryResponseListener(this);
    }

    /**
     * deliver invoke
     *
     * @param mcuid    machine number   mcuid
     * @param opration operation code
     * @param result   status code
     * @param seq
     * @param vals
     */
    @Override
    public void response(String mcuid, int opration, int result, String seq, int... vals) {
        if (sendReq.containsKey(seq)) {
            if (opration == VendingMachineKey.OP_DELIVER) {
                DeliverInfo deliverInfo = sendReq.remove(seq);
                if (deliverDiSposeListener != null) {
                    deliverDiSposeListener.deliverListener(deliverInfo, seq, result, vals[1], vals[2], vals[3]);
                }
            }
        } else {
            Log.d(TAG, "unknown : " + seq);
        }

    }


    /**
     * query machine operation
     */
    public void sendQueryMachineMsg() {
        if (serialPort != null) {
            serialPort.sendQueryMachineMsg();
        }
    }

    /**
     * clear execute task
     */
    public void cleatTask() {
        if (serialPort != null) {
            serialPort.clearTask();
        }
    }


    /**
     * @param t business object
     */
    public <T extends DeliverInfo> void sendDeliverMsg(T t) {
        if (serialPort != null) {
            sendReq.put(serialPort.sendDeliveryPacketMsg(t.machine, t.x, t.y, 1), t);
        }
    }

    /**
     * clear single clip  seq
     */
    public void sendClearAllSeqMsg() {
        if (serialPort != null) {
            serialPort.sendClearAllSeqMsg();
        }
    }

    public void recycle() {
        if (serialPort != null) {
            serialPort.release();
        }
    }


    public byte getMachinePortByNumber(String s) {
        return serialPort.getMachinePortByNumber(s);
    }

    public boolean isExistMachineNumber(String s) {
        return serialPort.isExistMachineNumber(s);
    }

    public int getMachineVersionByNumber(String s) {
        return serialPort.getMachineVersionByNumber(s);
    }

    @Override
    public void queryListener(List<LCMachineInfo> list) {
        if (deliverDiSposeListener != null) {
            deliverDiSposeListener.queryMachine(list);
        }
    }

    /**
     * custom deliver listener
     */
    public interface DeliverDiSposeListener {
        public void deliverListener(DeliverInfo deliverInfo, String seq, int result, int appendState, int x, int y);

        public void queryMachine(List<LCMachineInfo> list);
    }

    public void setDeliverDiSposeListener(DeliverDiSposeListener deliverDiSposeListener) {
        this.deliverDiSposeListener = deliverDiSposeListener;
    }


}
