package com.zijin.plugin.bleprinterPlugin;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.print.sdk.PrinterConstants;
import com.android.print.sdk.PrinterInstance;
import com.android.print.sdk.util.Utils;
import com.google.gson.Gson;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Description: 蓝牙打印
 * Date: 2020-02-03
 *
 * @author wangke
 */
public class BlePrinterPlugin extends CordovaPlugin {
    private Context context;
    private BluetoothAdapter bleAdapter;
    private PrinterInstance printer;
    private boolean hasRegReceiver;
    private BlePrinterOperation blePrinterOperation;
    private String currDeviceAddress = "";
    private CallbackContext callbackContext;
    private static final int REQUEST_ENABLE_BT = 0x001;
    

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.context = cordova.getContext();
        bleAdapter = BluetoothAdapter.getDefaultAdapter();
        blePrinterOperation = new BlePrinterOperation(context, new MsgHandler());
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            this.callbackContext = callbackContext;
            if ("isSupportBluetooth".equals(action)) {
                isSupportBluetooth();
            } else if ("bluetoothEnable".equals(action)) {
                bluetoothEnable();
            } else if ("getPairedDevices".equals(action)) {
                getPairedDevices();
            } else if ("connectPrinter".equals(action)) {
                connectPrinter(args);
            } else if ("printerEnable".equals(action)) {
                printerEnable();
            } else if ("openBluetooth".equals(action)) {
                openBluetooth();
            } else if ("printImage".equals(action)) {
                printImage(args);
            } else if ("initPrinter".equals(action)) {
                initPrinter();
            } else if ("printText".equals(action)) {
                printText(args);
            } else if ("close".equals(action)) {
                close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error("出现异常了：" + e.getMessage());
        }
        return true;
    }

    //region 蓝牙相关

    /**
     * 获取设备是否支持蓝牙
     */
    private void isSupportBluetooth() {
        if (bleAdapter == null) {
            callbackContext.error("设备不支持蓝牙");
        } else {
            callbackContext.success("设备支持蓝牙");
        }
    }

    /**
     * 获取当前蓝牙是否启用
     */
    private void bluetoothEnable() {
        if (bleAdapter.isEnabled()) {
            callbackContext.success("蓝牙可用");
        } else {
            callbackContext.error("蓝牙不可用");
        }
    }

    /**
     * 开启蓝牙
     */
    private void openBluetooth() {
        if (!bleAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            cordova.startActivityForResult(this, enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            this.callbackContext.success("蓝牙已经开启");
        }
    }

    /**
     * 获取已配对的设备
     *
     * @throws JSONException
     */
    private void getPairedDevices() throws JSONException {
        if (bleAdapter == null || !bleAdapter.isEnabled()) {
            callbackContext.error("获取已配对设备失败，当前蓝牙不可用");
        } else {
            Set<BluetoothDevice> bondedDevices = bleAdapter.getBondedDevices();
            String json = getPairedDevicesJson(bondedDevices);
            callbackContext.success(json);
        }
    }
    //endregion

    //region 打印机有关操作

    /**
     * 获取蓝牙打印设备是否可用
     */
    private void printerEnable() {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                if (printer == null) {
                    callbackContext.error("打印设备未连接");
                } else {
                    if (printer.isConnected()) {
                        PrinterStatus status = checkPrinter(printer);
                        if (status == PrinterStatus.PRINTER_ONLINE) {
                            callbackContext.success("打印机处于联机状态");
                        } else if (status.equals(PrinterStatus.DID_NOT_PRESS_PAPER_KEY)) {
                            callbackContext.error("打印机设备处于脱机状态，原因：未按走纸键");
                        } else if (status.equals(PrinterStatus.PRINTER_PAPER_SHORTAGE)) {
                            callbackContext.error("打印机设备处于脱机状态，原因：缺纸");
                        } else if (status.equals(PrinterStatus.PRINTER_ERROR)) {
                            callbackContext.error("打印机设备处于脱机状态，原因：内部出现故障");
                        } else {
                            callbackContext.error("打印机自检失败");
                        }
                    } else {
                        callbackContext.error("打印设备未连接");
                    }
                }
            }
        });
    }

    /**
     * 检查打印机状态
     *
     * @param printer
     * @return PrinterStatus 状态枚举
     */
    private PrinterStatus checkPrinter(PrinterInstance printer) {
        try {
            long waitReturnTime = 200;
            byte[] retData;
            byte[] arrayOfByte = new byte[3];
            // 实时状态传输
            arrayOfByte[0] = 16;
            arrayOfByte[1] = 4;
            // 获取传输打印机的状态
            arrayOfByte[2] = 1;
            printer.sendByteData(arrayOfByte);
            // 等待数据返回
            Thread.sleep(waitReturnTime);
            retData = printer.read();
            if (retData.length == 8) {
                // 打印机处于联机状态
                if (retData[3] == 0) {
                    return PrinterStatus.PRINTER_ONLINE;
                } else {
                    // 获取造成脱机状态的原因
                    // 获取脱机状态的原因
                    arrayOfByte[2] = 1;
                    printer.sendByteData(arrayOfByte);
                    Thread.sleep(waitReturnTime);
                    retData = printer.read();
                    if (retData.length == 8) {
                        if (retData[3] == 0) {
                            // 未按走纸键造成的脱机
                            return PrinterStatus.DID_NOT_PRESS_PAPER_KEY;
                        }
                        if (retData[5] == 1) {
                            // 打印机缺纸造成的脱机
                            return PrinterStatus.PRINTER_PAPER_SHORTAGE;
                        }
                        if (retData[6] == 1) {
                            // 打印机内部出现错误引起的脱机
                            return PrinterStatus.PRINTER_ERROR;
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return PrinterStatus.PRINTER_STATUS_CHECKE_FAILED;
    }


    /**
     * 连接打印机
     *
     * @param args [{"macAddress": macAddress, "name": name}]
     */
    private void connectPrinter(JSONArray args) throws JSONException {
        String macAddress = getMacAddressFromArgs(args);
        this.currDeviceAddress = macAddress;
        printer = blePrinterOperation.connect(macAddress);
        // 异步操作，连接的状态由MsgHandler通知
    }
    //endregion


    //region 打印相关
    private void printText(JSONArray args) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    printer.init();
                    // 设置打印格式样式
                    List<PrintBean> printBeanList = getPrintBeanFromArgs(args);
                    for (PrintBean printBean : printBeanList) {
                        setPrintAlignStyle(printer, printBean.getAlign());
                        printer.printText(printBean.getContent());
                        printAndWakePaperByLine(1);
                    }
                    printAndWakePaperByLine(3);
                    callbackContext.success("打印成功");
                } catch (JSONException e) {
                    e.printStackTrace();
                    callbackContext.error("文本打印出现异常：" + e.getMessage());
                }
            }
        });
    }

    /**
     * 设置打印时的文字对其方式
     *
     * @param printer
     * @param align   0: 居左对其 1: 居中对其 2: 居右对齐
     */
    private void setPrintAlignStyle(PrinterInstance printer, int align) {
        switch (align) {
            case 0:
                printer.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT);
                break;
            case 1:
                printer.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER);
                break;
            case 2:
                printer.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_RIGHT);
                break;
            default:
                break;
        }
    }


    /**
     * 初始化打印机
     */
    private void initPrinter() {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                printer.init();
                try {
                    // 等待打印机初始化成功
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                callbackContext.success("初始化打印机成功");
            }
        });
    }


    /**
     * 打印图片
     *
     * @param args [{"imagePath": "/sdcard/img.png"}]
     * @throws FileNotFoundException
     */
    private void printImage(JSONArray args) throws FileNotFoundException, JSONException {
        String imgPath = getImagePath(args);
        File file = new File(imgPath);
        if (file.exists()) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    // 打印图像
                    Bitmap grayBmp = Utils.color2GrayBmp(BitmapFactory.decodeFile(file.getPath()));
                    printer.printImage(grayBmp);
                    callbackContext.success("打印完成");
                }
            });
        } else {
            throw new FileNotFoundException("找不到要打印的图片");
        }
    }

    /**
     * 打印文字并按行向前走纸
     *
     * @param line
     */
    private void printAndWakePaperByLine(int line) {
        printer.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, line);
    }

    /**
     * 切纸
     */
    private void cutePaper() {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                printer.cutPaper();
            }
        });
    }
    //endregion

    //region 工具方法


    private List<PrintBean> getPrintBeanFromArgs(JSONArray args) throws JSONException {
        List<PrintBean> printBeanList = new ArrayList<>();
        Gson gson = new Gson();
        for (int i = 0; i < args.length(); i++) {
            String jsonStr = args.getJSONObject(i).toString();
            Log.i("wk", "jsonStr: " + jsonStr);
            PrintBean obj = gson.fromJson(jsonStr, PrintBean.class);
            printBeanList.add(obj);
        }
        return printBeanList;
    }


    /**
     * 从参数中获取要打印图片的路径
     *
     * @param args
     * @return
     */
    private String getImagePath(JSONArray args) throws JSONException {
        JSONArray jsonArray = new JSONArray(args.getString(0));
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        return jsonObject.getString("imagePath");
    }
    
    /**
     * 从参数中获取蓝牙设备的mac地址
     *
     * @param args
     * @return
     * @throws JSONException
     */
    private String getMacAddressFromArgs(JSONArray args) throws JSONException {
        JSONArray jsonArray = new JSONArray(args.getString(0));
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        return jsonObject.getString("macAddress");
    }

    /**
     * 将已匹配的设备转换成json格式
     *
     * @param bondedDevices
     * @return
     * @throws JSONException
     */
    private String getPairedDevicesJson(Set<BluetoothDevice> bondedDevices) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (BluetoothDevice bondedDevice : bondedDevices) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", bondedDevice.getName());
            jsonObject.put("address", bondedDevice.getAddress());
            jsonArray.put(jsonObject);
        }
        return jsonArray.toString();
    }

    /**
     * 关闭连接释放资源
     */
    private void close() {
        if (printer.isConnected()) {
            blePrinterOperation.close();
        }
        currDeviceAddress = "";
    }
    //endregion

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // 获取蓝牙的开启状态
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                callbackContext.success("蓝牙开启成功");
            } else {
                callbackContext.error("蓝牙开启失败");
            }
        }
    }
    
    class MsgHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PrinterConstants.Connect.SUCCESS:
                    callbackContext.success("蓝牙设备连接成功");
                    Log.i("wk", "Connect.SUCCESS");
                    break;
                case PrinterConstants.Connect.FAILED:
                    callbackContext.error("蓝牙打印设备连接失败");
                    Log.i("wk", "Connect.FAILED");
                    break;
                case PrinterConstants.Connect.CLOSED:
                    callbackContext.error("蓝牙打印设备连接关闭");
                    Log.i("wk", "Connect.CLOSED");
                    break;
                case PrinterConstants.Connect.NODEVICE:
                    callbackContext.error("没有找到设备");
                    Log.i("wk", "Connect.NODEVICE");
                    break;
                default:
                    break;
            }

        }
    }

    private enum PrinterStatus {
        //打印机连接状态
        PRINTER_ONLINE,
        //没有按走纸键
        DID_NOT_PRESS_PAPER_KEY,
        //打印机缺纸
        PRINTER_PAPER_SHORTAGE,
        //打印机出现错误
        PRINTER_ERROR,
        //打印机状态检查失败
        PRINTER_STATUS_CHECKE_FAILED
    }

    @Override
    public void onDestroy() {
        close();
        super.onDestroy();
    }
}