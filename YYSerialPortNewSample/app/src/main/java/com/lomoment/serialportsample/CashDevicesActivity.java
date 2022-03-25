package com.lomoment.serialportsample;

import android.os.Bundle;
import android.serialport.YySerialPort;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lomoment.pmod.DevicesInitListener;
import com.lomoment.pmod.IDevices;
import com.lomoment.pmod.cash.TransactionListener;
import com.lomoment.serialportsample.annotations.AnnotationsParse;
import com.lomoment.serialportsample.annotations.BindRid;
import com.lomoment.serialportsample.utils.VendingMachineDevicesUtils;

/**
 * @author libin
 * @date 2021/6/3
 * @Description
 */

public class CashDevicesActivity extends AppCompatActivity implements TransactionListener, DevicesInitListener {


    @BindRid(R.id.sp_path)
    private Spinner spPath;
    @BindRid(R.id.sp_baudrate)
    private Spinner spBaudrate;
    @BindRid(R.id.edt_money)
    private EditText edtMoney;
    @BindRid(R.id.tv_log)
    private TextView tvLog;
    @BindRid(R.id.tv_customer_money)
    private TextView tvCustomerMoney;
    private int[] baudrateVal;
    private String[] pathVal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashdevices);
        AnnotationsParse.parseBindRid(getWindow().getDecorView(), this);
        CashAmountManager.getInstance().addTransactionListener(this);
        CashAmountManager.getInstance().setDeviceInitListenerList(this);
        baudrateVal = getResources().getIntArray(R.array.serialport_baudrate_val);
        pathVal = getResources().getStringArray(R.array.serialport_path_value);
    }

    public void onClickCreate(View view) {
        //Initialize according to the selected path
        String path = pathVal[spPath.getSelectedItemPosition()];
        //set su path
        YySerialPort.setSuPath(VendingMachineDevicesUtils.getSuPathFromDevices());
        int baudrate = baudrateVal[spBaudrate.getSelectedItemPosition()];
        YySerialPort.setSuPath(VendingMachineDevicesUtils.getSuPathFromDevices());
        Log.d("TAG", "path  " + path + "  baudrate " + baudrate);
        CashAmountManager.getInstance().init(path, baudrate);
        changeCustomerMoney();
    }


    @Override
    public void initSuccess(IDevices iDevices) {
        tvLog.append(iDevices.getDevicesName() + "  initSuccess\n");
    }

    @Override
    public void initFinish(IDevices iDevices) {
        tvLog.append("initFinish\n");
    }

    @Override
    public void initFail(IDevices iDevices) {
        tvLog.append(iDevices.getDevicesName() + "  initFail\n");
    }


    public void onClickRefundMoney(View view) {
        CashAmountManager.getInstance().executeRefund();
    }

    public void onClickReceiveMoney(View view) {
        String val = edtMoney.getText().toString();
        if (TextUtils.isEmpty(val)) {
            Toast.makeText(this, "Please enter the amont.", Toast.LENGTH_LONG).show();
            return;
        }
        CashAmountManager.getInstance().tryOpenReceived();
        CashAmountManager.getInstance().setPrice(Double.parseDouble(val));
    }

    public void onClickCompleteTransaction(View view) {
        CashAmountManager.getInstance().transactionFinish();
    }


    public void changeCustomerMoney() {
        tvCustomerMoney.setText("Customer money ：" + CashAmountManager.getInstance().getCustomerMoney());
    }

    @Override
    public void receivedMoneyFinish() {
        tvLog.append("receivedMoneyFinish\n");
        //To deduct
        String val = edtMoney.getText().toString();
        CashAmountManager.getInstance().executeAmountOfDeducted(Double.parseDouble(val));
    }

    @Override
    public void refuseToAcceptTheMoney(int i, double v) {
        tvLog.append("refuse money , cause " + i + "   money   " + v + "\n");
    }

    @Override
    public void acceptTheMoneySuccess(double v) {
        tvLog.append("accept The Money Success  " + v + "\n");
    }

    @Override
    public void userMoneyChange(double v) {
        tvLog.append("user Money Change  " + v + "\n");
        changeCustomerMoney();
    }

    @Override
    public void executeRefundFail() {
        tvLog.append("execute Refund Fail  \n");
    }

    @Override
    public void executeRefundSuccess() {
        tvLog.append("execute Refund Success  \n");
    }

    @Override
    public void clearMoneyEven(double v) {
        tvLog.append("Customer amount cleared" + v + "\n");
    }


}
