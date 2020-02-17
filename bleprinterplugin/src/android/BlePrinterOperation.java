package com.zijin.plugin.bleprinterPlugin;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;
import com.android.print.sdk.PrinterInstance;
import com.android.print.sdk.bluetooth.BluetoothPort;


/**
 * Description:
 * Date: 2020-02-03
 *
 * @author wangke
 */
public class BlePrinterOperation implements IPrinterOperation {
    private Context context;
    private Handler handler;
    private final BluetoothAdapter bleAdapter;
    private PrinterInstance printer;

    public BlePrinterOperation(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        this.bleAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    @Override
    public PrinterInstance connect(String macAddress) {
        printer = new BluetoothPort().btConnnect(context, macAddress, bleAdapter, handler);
        return printer;
    }

    @Override
    public PrinterInstance getPrinter() {
        return printer;
    }

    @Override
    public void close() {
        if (printer != null) {
            printer.closeConnection();
            printer = null;
        }
    }
}
