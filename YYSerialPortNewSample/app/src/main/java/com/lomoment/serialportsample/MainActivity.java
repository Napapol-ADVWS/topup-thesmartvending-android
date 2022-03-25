package com.lomoment.serialportsample;

import android.content.Intent;
import android.os.Bundle;
import android.serialport.YySerialPort;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lomoment.serialportsample.annotations.AnnotationsParse;
import com.lomoment.serialportsample.annotations.BindRid;
import com.lomoment.serialportsample.utils.DateUtils;
import com.lomoment.serialportsample.utils.LanguageUtils;
import com.lomoment.serialportsample.utils.ResutCountUtils;
import com.lomoment.serialportsample.utils.VendingMachineDevicesUtils;
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
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    @BindRid(R.id.et_x)
    private EditText etx;
    @BindRid(R.id.et_y)
    private EditText ety;
    @BindRid(R.id.tv_content)
    private TextView tvContent;
    @BindRid(R.id.spinner)
    private Spinner spinner;
    @BindRid(R.id.tv_count)
    private TextView tvCount;
    @BindRid(R.id.checkbox)
    private CheckBox checkbox;
    @BindRid(R.id.et_countx)
    private EditText etCountX;
    @BindRid(R.id.et_county)
    private EditText etCountY;
    @BindRid(R.id.checkbox_all_test)
    private CheckBox cbAllTest;
    @BindRid(R.id.tv_query_count)
    private TextView tvQueryCount;
    @BindRid(R.id.edt_storey0)
    private EditText edtStorey0;
    @BindRid(R.id.edt_storey1)
    private EditText edtStorey1;
    @BindRid(R.id.edt_storey2)
    private EditText edtStorey2;
    @BindRid(R.id.edt_storey3)
    private EditText edtStorey3;
    @BindRid(R.id.edt_storey4)
    private EditText edtStorey4;
    @BindRid(R.id.edt_storey5)
    private EditText edtStorey5;
    @BindRid(R.id.edt_storey6)
    private EditText edtStorey6;
    @BindRid(R.id.edt_storey7)
    private EditText edtStorey7;
    @BindRid(R.id.edt_storey8)
    private EditText edtStorey8;
    @BindRid(R.id.edt_storey9)
    private EditText edtStorey9;
    @BindRid(R.id.edt_single_storey)
    private EditText edtSingleStorey;
    @BindRid(R.id.edt_auto_query_storey)
    private EditText edtAutoStoreyNumber;
    @BindRid(R.id.edt_number_of_plies)
    private EditText edtNumberOfPlies;
    @BindRid(R.id.edt_light_brightness)
    private EditText edtLightBrightness;
    @BindRid(R.id.sp_aisle)
    private Spinner spAisle;
    @BindRid(R.id.sp_path)
    private Spinner spPath;
    @BindRid(R.id.sp_baudrate)
    private Spinner spBaudrate;
    @BindRid(R.id.sp_language)
    private Spinner spLanguage;

    private Map<String, Object> requestMap = new HashMap<>();
    /**
     * Only one object of the same path exists
     */
    private VendingMachineMananger serialPort;
    private List<LCMachineInfo> shelfMap = new ArrayList<>();
    private List<String> mcuidList = new ArrayList<>();
    private ResutCountUtils resutCountUtils = new ResutCountUtils();
    private DateUtils timeFormat = new DateUtils();

    private boolean isMostDeliverTest = false;
    //Compatible with different versions
    private static final String XBIN_SU = "/system/xbin/su";
    private static final String BIN_SU = "/system/bin/su";
    private int[] aisleVal;
    private ArrayAdapter arrayAdapter;
    private String[] pathVal;
    private int[] baudrateVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //注解绑定id
        AnnotationsParse.parseBindRid(getWindow().getDecorView(), this);
        baudrateVal = getResources().getIntArray(R.array.serialport_baudrate_val);
        pathVal = getResources().getStringArray(R.array.serialport_path_value);
        aisleVal = getResources().getIntArray(R.array.aisle_val);
        //检测语言
        if (LanguageUtils.comparison(this, Locale.SIMPLIFIED_CHINESE)) {
            spLanguage.setSelection(0);
        } else {
            spLanguage.setSelection(1);
        }
    }


    /**
     * create serial object
     */
    private void createSerialPort() {
        serialPort = new VendingMachineMananger(this);
        //Initialize according to the selected path
        String path = pathVal[spPath.getSelectedItemPosition()];
        //set su path
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
                    } else if (opration == VendingMachineKey.OP_LIFT_AUTO_QUEYR_ALL_STORY_HEIGHT) {
                        if (state == VendingMachineKey.STATE_SUCCESS) {
                            //操作成功，解析数据
                            Toast.makeText(MainActivity.this, R.string.operation_success, Toast.LENGTH_SHORT).show();
                            onClickQueryListAllStorey(null);
                        } else {
                            Toast.makeText(MainActivity.this, R.string.operation_fail, Toast.LENGTH_SHORT).show();
                        }
                    } else if (opration == VendingMachineKey.OP_QUERY_MACHINE_STATE) {
                        sb.append(getString(R.string.query_machine_state) + " ：");
                        if (state == VendingMachineKey.STATE_SUCCESS) {
                            //操作成功，解析数据
                            if (args[0] == VendingMachineCode.QUERY_MACHINE_STATE_NORMAL) {
                                Toast.makeText(MainActivity.this, R.string.operation_success, Toast.LENGTH_SHORT).show();
                                sb.append(getString(R.string.query_machine_state_normal));
                            } else {
                                if (args[0] == VendingMachineCode.QUERY_MACHINE_STATE_DOOR_OPEN) {
                                    sb.append(getString(R.string.query_machine_state_door_open));
                                } else if (args[0] == VendingMachineCode.QUERY_MACHINE_STATE_PICK_UP_DOOR_OPEN) {
                                    sb.append(getString(R.string.query_machine_state_pick_up_door_open));
                                } else if (args[0] == VendingMachineCode.QUERY_MACHINE_STATE_LIFT_PLATFORM_NOT_IN_STARTING_POSITION) {
                                    sb.append(getString(R.string.query_machine_state_lift_platform_not_in_starting_position));
                                } else {
                                    sb.append(getString(R.string.unknown_command));
                                }
                                Toast.makeText(MainActivity.this, R.string.operation_fail, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            sb.append(getString(R.string.timeout));
                            Toast.makeText(MainActivity.this, R.string.operation_fail, Toast.LENGTH_SHORT).show();
                        }
                    } else if (opration == VendingMachineKey.OP_LIFT_PLATFORM_RESET) {
                        sb.append(getString(R.string.lift_platform_reset) + " ");
                        if (state == VendingMachineKey.STATE_SUCCESS) {
                            //操作成功，解析数据
                            if (args[0] == VendingMachineCode.LIFT_PLATFORM_RESET_SUCCESS) {
                                Toast.makeText(MainActivity.this, R.string.operation_success, Toast.LENGTH_SHORT).show();
                                sb.append(getString(R.string.operation_success));
                            } else {
                                if (args[0] == VendingMachineCode.LIFT_PLATFORM_RESET_FAIL) {
                                    sb.append(getString(R.string.operation_fail));
                                } else {
                                    sb.append(getString(R.string.unknown_command));
                                }
                                Toast.makeText(MainActivity.this, R.string.operation_fail, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            sb.append(getString(R.string.timeout));
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

    /**
     * 根据机器编号初始化控件
     */
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

    /**
     * 发送出货
     *
     * @param view
     */
    public void onClickSendMsg(View view) {
        sendDelivery();
    }

    /**
     * 发送出货
     */
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
        String seq = serialPort.sendDeliveryPacketMsg(machine, Integer.parseInt(etx.getText().toString()), Integer.parseInt(ety.getText().toString()), aisleVal[point]);
        //Save the SEQ that executes the instruction
        requestMap.put(seq, new Object());
    }

    /**
     * 查询机器
     *
     * @param view
     */
    public void onClickQueryMachine(View view) {
        if (serialPort == null) {
            return;
        }
        serialPort.sendQueryMachineMsg();

    }

    /**
     * 清除下位机缓存的seq
     *
     * @param view
     */
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
        spLanguage.setOnItemSelectedListener(this);
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

    /**
     * 回收串口对象
     */
    private void recycleSerialPort() {
        if (serialPort != null) {
            serialPort.release();
        }
    }

    /**
     * 跳转到封装类
     *
     * @param view
     */
    public void onClickEncapsulationClass(View view) {
        startActivity(new Intent(this, MainTwoActivity.class));
    }

    /**
     * 清空任务
     *
     * @param view
     */
    public void onClickClearTask(View view) {
        if (serialPort == null) {
            return;
        }
        serialPort.clearTask();
        requestMap.clear();
        isMostDeliverTest = false;
    }

    /**
     * 根据填入xy测试所有货道
     *
     * @param view
     */
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

    /**
     * 创建串口对象
     *
     * @param view
     */
    public void onClickCreateSerialPort(View view) {
        recycleSerialPort();
        createSerialPort();
        if (arrayAdapter != null) {
            arrayAdapter.clear();
            arrayAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 改变灯跳亮度
     *
     * @param view
     */
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


    /**
     * 设置升降机所有层高
     *
     * @param view
     */
    public void onClickSetAllStorey(View view) {
        String machine = checkMachine();
        if (TextUtils.isEmpty(machine)) {
            Toast.makeText(this, R.string.create_serial_port_operation_obj_please, Toast.LENGTH_LONG).show();
            return;
        }
        int[] storeys = new int[]{getStoreyEditValue(edtStorey0), getStoreyEditValue(edtStorey1), getStoreyEditValue(edtStorey2), getStoreyEditValue(edtStorey3), getStoreyEditValue(edtStorey4), getStoreyEditValue(edtStorey5), getStoreyEditValue(edtStorey6), getStoreyEditValue(edtStorey7), getStoreyEditValue(edtStorey8), getStoreyEditValue(edtStorey9)};
        requestMap.put(serialPort.setAllStoreyOfLiftMachine(machine, storeys), null);
    }


    /**
     * 查询升降机所有层高
     *
     * @param view
     */
    public void onClickQueryListAllStorey(View view) {
        String machine = checkMachine();
        if (TextUtils.isEmpty(machine)) {
            Toast.makeText(this, R.string.create_serial_port_operation_obj_please, Toast.LENGTH_LONG).show();
            return;
        }
        requestMap.put(serialPort.queryAllStoreyOfLiftMachine(machine), null);
    }

    /**
     * 重置所有层高
     *
     * @param view
     */
    public void onClickResetStorey(View view) {
        String machine = checkMachine();
        if (TextUtils.isEmpty(machine)) {
            Toast.makeText(this, R.string.create_serial_port_operation_obj_please, Toast.LENGTH_LONG).show();
            return;
        }
        requestMap.put(serialPort.resetStoreyOfLiftMachine(machine), null);
    }

    /**
     * 设置单层层高
     *
     * @param view
     */
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
        int storey = getStoreyEditValue(edtSingleStorey);
        byte[] storeys = LiftUtils.splitIntBits(getStoreyEditValue(edtSingleStorey));
        byte[] command = new byte[2];
        command[0] = storeys[0];
        command[1] = storeys[1];
        requestMap.put(serialPort.setSingleStoreyOfLiftMachine(machine, numberOfPlies, storey), null);
    }

    /**
     * 获取层高控件数值
     *
     * @param edt
     * @return
     */
    public int getStoreyEditValue(EditText edt) {
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

    /**
     * 自动查询层高
     *
     * @param view
     */
    public void onClickAutoQueryStorey(View view) {
        String machine = checkMachine();
        if (TextUtils.isEmpty(machine)) {
            Toast.makeText(this, R.string.create_serial_port_operation_obj_please, Toast.LENGTH_LONG).show();
            return;
        }
        String number = edtAutoStoreyNumber.getText().toString();
        if (TextUtils.isEmpty(number)) {
            Toast.makeText(this, R.string.auto_query_hint, Toast.LENGTH_LONG).show();
            return;
        }
        requestMap.put(serialPort.sendAutoQueryAllStoreyOfLiftMachine(machine, Byte.valueOf(number)), null);
    }

    /**
     * 查询设备状态
     *
     * @param view
     */
    public void onClickQueryMachineState(View view) {
        String machine = checkMachine();
        if (TextUtils.isEmpty(machine)) {
            Toast.makeText(this, R.string.create_serial_port_operation_obj_please, Toast.LENGTH_LONG).show();
            return;
        }
        requestMap.put(serialPort.sendQueryMachineState(machine), null);
    }

    /**
     * 还原升降平台指令
     *
     * @param view
     */
    public void onClickLiftPlatformReset(View view) {
        String machine = checkMachine();
        if (TextUtils.isEmpty(machine)) {
            Toast.makeText(this, R.string.create_serial_port_operation_obj_please, Toast.LENGTH_LONG).show();
            return;
        }
        requestMap.put(serialPort.sendLiftPlatformReset(machine), null);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d("tag", "position " + position);
        if (position == 0) {
            if (!LanguageUtils.comparison(this, Locale.SIMPLIFIED_CHINESE)) {
                LanguageUtils.changeAppLanguage(this, Locale.SIMPLIFIED_CHINESE);
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        } else {
            if (!LanguageUtils.comparison(this, Locale.ENGLISH)) {
                LanguageUtils.changeAppLanguage(this, Locale.ENGLISH);
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
