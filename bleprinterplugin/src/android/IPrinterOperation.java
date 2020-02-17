package com.zijin.plugin.bleprinterPlugin;

import com.android.print.sdk.PrinterInstance;

public interface IPrinterOperation {

    /**
     * 建立连接
     *
     * @param address 打印设备蓝牙的mac地址
     */
    public PrinterInstance connect(String address);

    /**
     * 获取打印对象
     *
     * @return
     */
    public PrinterInstance getPrinter();

    /**
     * 关闭连接
     */
    public void close();


}
