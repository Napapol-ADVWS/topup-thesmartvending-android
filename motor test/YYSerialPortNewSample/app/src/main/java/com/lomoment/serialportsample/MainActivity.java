package com.lomoment.serialportsample;

import android.content.Intent;
import android.os.Bundle;
import android.serialport.YySerialPort;
import android.support.annotation.Nullable;
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
import com.lomoment.serialportsdk.VendingMachineMananger;
import com.lomoment.serialportsdk.VendingMachineUtils;
import com.lomoment.serialportsdk.entity.LCMachineInfo;
import com.lomoment.serialportsdk.utils.LiftUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private EditText etx;
    private EditText ety;
    private TextView tvContent;

    private Map<String, Object> requestMap = new HashMap<>();
    /**
     * Only one object of the same path exists
     */
    private VendingMachineMananger serialPort;
    private List<LCMachineInfo> shelfMap = new ArrayList<>();
    private Spinner spinner;
    private List<String> mcuidList = new ArrayList<>();
    private CheckBox checkbox;

    private ResutCountUtils resutCountUtils = new ResutCountUtils();
    private TextView tvCount;

    private DateUtils timeFormat = new DateUtils();
    private EditText etCountX;
    private EditText etCountY;
    private CheckBox cbAllTest;

    private boolean isMostDeliverTest = false;

    //Compatible with different versions
    private static final String XBIN_SU = "/system/xbin/su";
    private static final String BIN_SU = "/system/bin/su";
    private TextView tvQueryCount;
    private EditText edtStorey0;
    private EditText edtStorey1;
    private EditText edtStorey2;
    private EditText edtStorey3;
    private EditText edtStorey4;
    private EditText edtStorey5;
    private EditText edtStorey6;
    private EditText edtStorey7;
    private EditText edtStorey8;
    private EditText edtStorey9;
    private EditText edtSingleStorey;
    private EditText edtNumberOfPlies;
    private EditText edtLightBrightness;
    private Spinner spAisle;
    private int[] aisleVal;
    private ArrayAdapter arrayAdapter;
    private Spinner spPath;
    private Spinner spBaudrate;
    private int[] baudrateVal;
    private String[] pathVal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etx = (EditText) findViewById(R.id.et_x);
        ety = (EditText) findViewById(R.id.et_y);
        tvContent = (TextView) findViewById(R.id.tv_content);
        spinner = (Spinner) findViewById(R.id.spinner);
        spAisle = (Spinner) findViewById(R.id.sp_aisle);
        checkbox = (CheckBox) findViewById(R.id.checkbox);
        tvCount = (TextView) findViewById(R.id.tv_count);
        tvQueryCount = (TextView) findViewById(R.id.tv_query_count);
        etCountX = (EditText) findViewById(R.id.et_countx);
        etCountY = (EditText) findViewById(R.id.et_county);
        cbAllTest = (CheckBox) findViewById(R.id.checkbox_all_test);
        spPath = (Spinner) findViewById(R.id.sp_path);
        spBaudrate = (Spinner) findViewById(R.id.sp_baudrate);
        baudrateVal = getResources().getIntArray(R.array.serialport_baudrate_val);
        pathVal = getResources().getStringArray(R.array.serialport_path_value);

        edtStorey0 = (EditText) findViewById(R.id.edt_storey0);
        edtStorey1 = (EditText) findViewById(R.id.edt_storey1);
        edtStorey2 = (EditText) findViewById(R.id.edt_storey2);
        edtStorey3 = (EditText) findViewById(R.id.edt_storey3);
        edtStorey4 = (EditText) findViewById(R.id.edt_storey4);
        edtStorey5 = (EditText) findViewById(R.id.edt_storey5);
        edtStorey6 = (EditText) findViewById(R.id.edt_storey6);
        edtStorey7 = (EditText) findViewById(R.id.edt_storey7);
        edtStorey8 = (EditText) findViewById(R.id.edt_storey8);
        edtStorey9 = (EditText) findViewById(R.id.edt_storey9);
        edtSingleStorey = (EditText) findViewById(R.id.edt_single_storey);
        edtNumberOfPlies = (EditText) findViewById(R.id.edt_number_of_plies);
        edtLightBrightness = (EditText) findViewById(R.id.edt_light_brightness);
        edtLightBrightness = (EditText) findViewById(R.id.edt_light_brightness);
        aisleVal = getResources().getIntArray(R.array.aisle_val);

    }


    /**
     * create serial object
     */
    private void createSerialPort() {
        serialPort = new VendingMachineMananger(this);
        //Initialize according to the selected path
        String path = pathVal[spPath.getSelectedItemPosition()];
        //设置su路径
        YySerialPort.setSuPath(VendingMachineDevicesUtils.getSuPathFromDevices());
        int baudrate = baudrateVal[spBaudrate.getSelectedItemPosition()];
        serialPort.init(path, baudrate);
        //show log
        VendingMachineMananger.isShowLog = true;
        //Implement the callback
        serialPort.setVendingmachineResponseListener(new VendingMachineMananger.VendingmachineResponseListener() {
            @Override
            public void response(String machine, int opration, int state, String seq, int[] args) {
                if (requestMap.containsKey(seq)) {
                    //splicing result
                    StringBuilder sb = new StringBuilder();
                    sb.append(timeFormat.getTime());
                    sb.append("\t");
                    sb.append(getString(R.string.machine));
                    sb.append(machine + "\t" + "seq：" + seq);
                    sb.append("\t");
                    //deliver operation
                    if (opration == VendingMachineKey.OP_DELIVER) {
                        if (state == VendingMachineCode.STATE_SUCCESS) {
                            sb.append(getString(R.string.deliver_success));
                            if (args[0] == VendingMachineCode.DELIVER_EMBODY_SUCCESS) {
                                sb.append(getString(R.string.deliver_success));
                            } else {
                                sb.append(getString(R.string.deliver_fail) + "\t");
                                sb.append(getString(R.string.explain_code));
                                sb.append(args[1]);
                            }
                        } else if (state == VendingMachineCode.STATE_FAIL) {
                            sb.append(getString(R.string.deliver_fail) + "\t");
                            sb.append(getString(R.string.explain_code));
                            sb.append(args[1]);
                        }

                        sb.append(getString(R.string.aisle));
                        sb.append("  x " + args[2] + "   y " + args[3]);
                        resutCountUtils.countResult(args[1] + "");
                        //是否需要重复发送
                        if (checkbox.isChecked()) {
                            sendDelivery();
                        }
                    }
                    //Lift  storey
                    else if (opration == VendingMachineKey.OP_LIFT_STORY_HEIGHT_SETTING_ALL || opration == VendingMachineKey.OP_LIFT_STORY_HEIGHT_QUERY || opration == VendingMachineKey.OP_LIFT_STORY_HEIGHT_RESET) {
                        if (state == VendingMachineKey.STATE_SUCCESS) {
                            Toast.makeText(MainActivity.this, R.string.operation_success, Toast.LENGTH_SHORT).show();
                            if (opration == VendingMachineKey.OP_LIFT_STORY_HEIGHT_SETTING_ALL || opration == VendingMachineKey.OP_LIFT_STORY_HEIGHT_QUERY) {
                                edtStorey0.setText(args[0] + "");
                                edtStorey1.setText(args[1] + "");
                                edtStorey2.setText(args[2] + "");
                                edtStorey3.setText(args[3] + "");
                                edtStorey4.setText(args[4] + "");
                                edtStorey5.setText(args[5] + "");
                                edtStorey6.setText(args[6] + "");
                                edtStorey7.setText(args[7] + "");
                                edtStorey8.setText(args[8] + "");
                                edtStorey9.setText(args[9] + "");
                            }
                        } else {
                            Toast.makeText(MainActivity.this, R.string.operation_fail, Toast.LENGTH_SHORT).show();
                        }

                    } else if (opration == VendingMachineKey.OP_LIFT_STORY_HEIGHT_SETTING_SINGLE) {
                        if (state == VendingMachineCode.STATE_SUCCESS) {
                            Toast.makeText(MainActivity.this, R.string.operation_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.operation_fail, Toast.LENGTH_SHORT).show();
                        }
                    } else if (opration == VendingMachineKey.OP_LIGHT_BRIGHTNESS) {
                        if (state == VendingMachineCode.STATE_SUCCESS) {
                            Toast.makeText(MainActivity.this, R.string.operation_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.operation_fail, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (state == VendingMachineCode.STATE_SUCCESS) {
                            sb.append(getString(R.string.operation_success));
                        } else {
                            sb.append(getString(R.string.operation_fail));
                        }
                    }
                    sb.append("\n");
                    final String msg = sb.toString();
                    String str = tvContent.getText().toString();
                    tvContent.setText(msg);
                    tvContent.append(str);
                    requestMap.remove(seq);
                    //Statistics
                    tvCount.setText(getString(R.string.statistics) + "\n" + resutCountUtils.toString().replaceAll(",", "\n").replaceAll("\\{|\\}", ""));
                }
            }
        });


        serialPort.setMachineQueryResponseListener(new VendingMachineMananger.MachineQueryResponseListener() {
            @Override
            public void queryListener(List<LCMachineInfo> list) {
                shelfMap.clear();
                if (list != null && list.size() > 0) {
                    shelfMap.addAll(list);
                    initMachineNumberSpinnerAdapter();
                } else {

                    Toast.makeText(MainActivity.this, R.string.not_found_machine, Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void initMachineNumberSpinnerAdapter() {
        mcuidList.clear();
        for (LCMachineInfo info : shelfMap) {
            String name = "";
            if (serialPort != null && serialPort.isExistMachineNumber(info.getMcuid())) {
                byte port = serialPort.getMachinePortByNumber(info.getMcuid());
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
            //Join device details (Routing,Machine number,Version)
            name = name + VendingMachineUtils.SEPARATOR + info.getMcuid() + VendingMachineUtils.SEPARATOR + serialPort.getMachineVersionByNumber(info.getMcuid());
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
        arrayAdapter = new ArrayAdapter(this, R.layout.item_shelf_spinner, mcuidList);
        spinner.setAdapter(arrayAdapter);
    }

    public void onClickSendMsg(View view) {
        sendDelivery();
    }

    private void sendDelivery() {
        if (serialPort == null) {
            return;
        }
        String machine = checkMachine();
        if (TextUtils.isEmpty(machine)) {
            Toast.makeText(this, R.string.create_serial_port_operation_obj_please, Toast.LENGTH_LONG).show();
            return;
        }

        int point = 0;
        if (spAisle.getSelectedItem() != null) {
            point = spAisle.getSelectedItemPosition();
        }

        //截取机器编号——机器显示做了额外的数据添加（主副柜、单片机版本）
        String seq = serialPort.sendDeliveryPacketMsg(machine,
                Integer.parseInt(etx.getText().toString()),
                Integer.parseInt(ety.getText().toString()),
                aisleVal[point]);
        //保存seq值,Object为业务对象
        requestMap.put(seq, new Object());
    }

    public void onClickQueryMachine(View view) {
        if (serialPort == null) {
            return;
        }
        serialPort.sendQueryMachineMsg();

    }

    public void onClickClearSeq(View view) {
        if (serialPort == null) {
            return;
        }
        serialPort.sendClearAllSeqMsg();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Recycing serial object,Avoid having two objects with the same path after the jump
        recycleSerialPort();
        if (spinner != null && spinner.getAdapter() != null) {
            mcuidList.clear();
            ((ArrayAdapter) spinner.getAdapter()).notifyDataSetChanged();
        }
    }

    private void recycleSerialPort() {
        if (serialPort != null) {
            serialPort.release();
        }
    }

    public void onClickEncapsulationClass(View view) {
        startActivity(new Intent(this, MainTwoActivity.class));
    }

    public void onClickClearTask(View view) {
        if (serialPort == null) {
            return;
        }
        serialPort.clearTask();
        requestMap.clear();
        isMostDeliverTest = false;
    }

    public void onClickTestAll(View view) {
        if (serialPort == null) {
            return;
        }
        String machine = checkMachine();
        if (TextUtils.isEmpty(machine)) {
            Toast.makeText(this, R.string.create_serial_port_operation_obj_please, Toast.LENGTH_LONG).show();
            return;
        }
        int x = Integer.parseInt(etCountX.getText().toString());
        int y = Integer.parseInt(etCountY.getText().toString());
        if (x > 9 || y > 9) {
            Toast.makeText(this, getString(R.string.input_value_cannot_be_greater_than, 9), Toast.LENGTH_LONG).show();
            return;
        }
        isMostDeliverTest = true;
        for (int i = 1; i <= x; i++) {
            for (int j = 1; j <= y; j++) {
                String seq = serialPort.sendDeliveryPacketMsg(machine, i, j);
                //Mock business objects
                requestMap.put(seq, new Object());
            }
        }
    }

    public void onClickCreateSerialPort(View view) {
        recycleSerialPort();
        createSerialPort();
        if (arrayAdapter != null) {
            arrayAdapter.clear();
            arrayAdapter.notifyDataSetChanged();
        }
    }

    public void onClickChangeLightBrightness(View view) {
        String machine = checkMachine();
        if (TextUtils.isEmpty(machine)) {
            Toast.makeText(this, R.string.create_serial_port_operation_obj_please, Toast.LENGTH_LONG).show();
            return;
        }
        String vals = edtLightBrightness.getText().toString();
        if (TextUtils.isEmpty(vals)) {
            Toast.makeText(this, getString(R.string.please_enter_values_from_some_to_some, 0, 255), Toast.LENGTH_LONG).show();
            return;
        }
        requestMap.put(serialPort.sendLightBrightness(machine, (byte) Integer.parseInt(vals)), null);
    }


    public void onClickSetAllStorey(View view) {
        String machine = checkMachine();
        if (TextUtils.isEmpty(machine)) {
            Toast.makeText(this, R.string.create_serial_port_operation_obj_please, Toast.LENGTH_LONG).show();
            return;
        }
        int[] storeys = new int[]{getLiftStorey(edtStorey0), getLiftStorey(edtStorey1), getLiftStorey(edtStorey2), getLiftStorey(edtStorey3), getLiftStorey(edtStorey4), getLiftStorey(edtStorey5), getLiftStorey(edtStorey6), getLiftStorey(edtStorey7), getLiftStorey(edtStorey8), getLiftStorey(edtStorey9)};
        requestMap.put(serialPort.setAllStoreyOfLiftMachine(machine, storeys), null);
    }


    public void onClickQueryListAllStorey(View view) {
        String machine = checkMachine();
        if (TextUtils.isEmpty(machine)) {
            Toast.makeText(this, R.string.create_serial_port_operation_obj_please, Toast.LENGTH_LONG).show();
            return;
        }
        requestMap.put(serialPort.queryAllStoreyOfLiftMachine(machine), null);
    }

    public void onClickResetStorey(View view) {
        String machine = checkMachine();
        if (TextUtils.isEmpty(machine)) {
            Toast.makeText(this, R.string.create_serial_port_operation_obj_please, Toast.LENGTH_LONG).show();
            return;
        }
        requestMap.put(serialPort.resetStoreyOfLiftMachine(machine), null);
    }

    public void onClickSetSingleStorey(View view) {
        String machine = checkMachine();
        if (TextUtils.isEmpty(machine)) {
            Toast.makeText(this, R.string.create_serial_port_operation_obj_please, Toast.LENGTH_LONG).show();
            return;
        }
        String numberOfPliesStr = edtNumberOfPlies.getText().toString();
        if (TextUtils.isEmpty(numberOfPliesStr)) {
            Toast.makeText(this, R.string.number_of_layers_cannot_be_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        int numberOfPlies = Integer.parseInt(numberOfPliesStr);
        int storey = getLiftStorey(edtSingleStorey);
        byte[] storeys = LiftUtils.splitIntBits(getLiftStorey(edtSingleStorey));
        byte[] command = new byte[2];
        command[0] = storeys[0];
        command[1] = storeys[1];
        requestMap.put(serialPort.setSingleStoreyOfLiftMachine(machine, numberOfPlies, storey), null);
    }

    public int getLiftStorey(EditText edt) {
        String result = edt.getText().toString();
        int number = 0;
        if (!TextUtils.isEmpty(result)) {
            number = Integer.parseInt(result);
        }
        return number;
    }

    @Nullable
    private String checkMachine() {
        String machine = null;
        if (spinner.getSelectedItem() != null) {
            machine = spinner.getSelectedItem().toString();
            machine = machine.split(VendingMachineUtils.SEPARATOR)[1];
        }
        if (TextUtils.isEmpty(machine)) {
            return null;
        }
        return machine;
    }

}
