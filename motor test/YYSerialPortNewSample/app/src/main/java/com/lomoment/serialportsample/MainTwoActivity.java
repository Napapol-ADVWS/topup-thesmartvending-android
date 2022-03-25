package com.lomoment.serialportsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lomoment.serialportsdk.VendingMachineCode;
import com.lomoment.serialportsdk.VendingMachineKey;
import com.lomoment.serialportsdk.VendingMachineUtils;
import com.lomoment.serialportsdk.entity.LCMachineInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainTwoActivity extends AppCompatActivity implements SingleChipManager.DeliverDiSposeListener {

    private EditText etx;
    private EditText ety;
    private TextView tvContent;

    private SingleChipManager singleChipManager;
    private Spinner spinner;
    private List<LCMachineInfo> shelfMap = new ArrayList<>();
    private List<String> mcuidList = new ArrayList<>();
    private CheckBox checkbox;

    private ResutCountUtils resutCountUtils = new ResutCountUtils();
    private TextView tvCount;

    private DateUtils timeFormat = new DateUtils();
    private Spinner spPath;
    private Spinner spBaudrate;
    private int[] baudrateVal;
    private String[] pathVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_two);
        initView();
        tvCount = (TextView) findViewById(R.id.tv_count);
    }

    private void initView() {
        spinner = (Spinner) findViewById(R.id.spinner);
        etx = (EditText) findViewById(R.id.et_x);
        ety = (EditText) findViewById(R.id.et_y);
        tvContent = (TextView) findViewById(R.id.tv_content);
        checkbox = (CheckBox) findViewById(R.id.checkbox);
        spPath = (Spinner) findViewById(R.id.sp_path);
        spBaudrate = (Spinner) findViewById(R.id.sp_baudrate);
        baudrateVal = getResources().getIntArray(R.array.serialport_baudrate_val);
        pathVal = getResources().getStringArray(R.array.serialport_path_value);
    }

    public void onClickSendMsg(View view) {
        sendDelivery();
    }

    private void sendDelivery() {
        if (singleChipManager == null) {
            Toast.makeText(this, R.string.not_found_machine, Toast.LENGTH_LONG).show();
            return;
        }
        String machine = null;
        if (spinner.getSelectedItem() != null) {
            machine = spinner.getSelectedItem().toString();
        }
        if (TextUtils.isEmpty(machine)) {
            Toast.makeText(this, R.string.not_found_machine, Toast.LENGTH_LONG).show();
            return;
        }
        DeliverInfo deliverInfo = new DeliverInfo(machine.split(VendingMachineUtils.SEPARATOR)[1], Integer.parseInt(etx.getText().toString()), Integer.parseInt(ety.getText().toString()));
        singleChipManager.sendDeliverMsg(deliverInfo);
    }

    public void onClickQueryMachine(View view) {
        if (singleChipManager == null) {
            Toast.makeText(this, R.string.not_found_machine, Toast.LENGTH_LONG).show();
            return;
        }
        singleChipManager.sendQueryMachineMsg();
    }


    public void onClickClearSeq(View view) {
        if (singleChipManager == null) {
            Toast.makeText(this, R.string.not_found_machine, Toast.LENGTH_LONG).show();
            return;
        }
        singleChipManager.sendClearAllSeqMsg();
    }

    public void onClickBack(View view) {
        onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        recycleSerialPort();
        if (spinner != null && spinner.getAdapter() != null) {
            mcuidList.clear();
            ((ArrayAdapter) spinner.getAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    public void deliverListener(DeliverInfo deliverInfo, String seq, int result, int appendState, int x, int y) {
        StringBuilder sb = new StringBuilder();
        sb.append(timeFormat.getTime());
        sb.append("\t");
        sb.append(getString(R.string.machine));
        sb.append(deliverInfo.machine + "\t" + "seq：" + seq);
        sb.append("\t");
        if (result == VendingMachineCode.STATE_SUCCESS) {
            if (appendState == VendingMachineCode.DELIVER_EMBODY_SUCCESS) {
                sb.append(getString(R.string.deliver_success));
            } else {
                sb.append(getString(R.string.deliver_fail) + "\t");
                sb.append(getString(R.string.explain_code));
                sb.append(appendState);
            }
        } else if (result == VendingMachineCode.STATE_FAIL) {
            sb.append(getString(R.string.deliver_fail) + "\t");
            sb.append(getString(R.string.explain_code));
            sb.append(appendState);
        }
        sb.append(getString(R.string.aisle));
        sb.append("  x " + x + "   y " + y);
        sb.append("\n");
        final String msg = sb.toString();
        String str = tvContent.getText().toString();
        tvContent.setText(msg);
        tvContent.append(str);
        //是否需要重复发送
        if (checkbox.isChecked()) {
            sendDelivery();
        }
        resutCountUtils.countResult(appendState + "");
        tvCount.setText("统计：\n" + resutCountUtils.toString().replaceAll(",", "\n").replaceAll("\\{|\\}", ""));

    }

    @Override
    public void queryMachine(List<LCMachineInfo> list) {
        shelfMap.clear();
        if (list.size() > 0) {
            shelfMap.addAll(list);
            initSpinnerAdapter();
        } else {
            Toast.makeText(this, R.string.not_found_machine, Toast.LENGTH_LONG).show();
        }
    }


    private void initSpinnerAdapter() {
        mcuidList.clear();
        for (LCMachineInfo lcMachineInfo : shelfMap) {
            String name = "";
            String mcuid = lcMachineInfo.getMcuid();
            //查询
            if (singleChipManager != null && singleChipManager.isExistMachineNumber(mcuid)) {
                byte port = singleChipManager.getMachinePortByNumber(mcuid);
                if (port == VendingMachineKey.MACHINE_ROUTE_MIDDLE) {
                    name = getString(R.string.machine_main);
                } else if (port == VendingMachineKey.MACHINE_ROUTE_LEFT) {
                    name = getString(R.string.affiliate_machine_C);
                } else if (port == VendingMachineKey.MACHINE_ROUTE_RIGHT) {
                    name = getString(R.string.affiliate_machine_A);
                }
            } else {
                name = getString(R.string.unknown);
            }
            name = name + VendingMachineUtils.SEPARATOR + mcuid + VendingMachineUtils.SEPARATOR + singleChipManager.getMachineVersionByNumber(mcuid);
            mcuidList.add(name);
        }
        if (mcuidList.size() > 0) {
            Collections.sort(mcuidList, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.item_shelf_spinner, mcuidList);
        spinner.setAdapter(arrayAdapter);
    }

    public void onClickClearTask(View view) {
        if (singleChipManager == null) {
            Toast.makeText(this, R.string.create_serial_port_operation_obj_please, Toast.LENGTH_LONG).show();
            return;
        }
        singleChipManager.cleatTask();
    }

    public void onClickCreateSerialPort(View view) {
        recycleSerialPort();
        createSerialPortObj();
    }

    private void createSerialPortObj() {
        String path = pathVal[spPath.getSelectedItemPosition()];
        int baudrate = baudrateVal[spBaudrate.getSelectedItemPosition()];
        singleChipManager = SingleChipManager.getInstance();
        singleChipManager.init(this, path, baudrate);
        singleChipManager.setDeliverDiSposeListener(this);
    }

    private void recycleSerialPort() {
        if (singleChipManager != null) {
            singleChipManager.recycle();
        }
    }
}
