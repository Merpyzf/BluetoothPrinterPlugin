import { Injectable } from '@angular/core';
import { Plugin, Cordova, IonicNativePlugin } from '@ionic-native/core';

/**
 * @name Ble Printer Plugin
 * @description
 * This plugin does something
 *
 * @usage
 * ```typescript
 * import { BlePrinterPlugin } from '@ionic-native/ble-printer-plugin';
 *
 *
 * constructor(private blePrinterPlugin: BlePrinterPlugin) { }
 *
 * ...
 *
 *
 * this.blePrinterPlugin.functionName('Hello', 123)
 *   .then((res: any) => console.log(res))
 *   .catch((error: any) => console.error(error));
 *
 * ```
 */
@Plugin({
  pluginName: 'BlePrinterPlugin',
  plugin: 'bleprinterplugin', // npm package name, example: cordova-plugin-camera
  pluginRef: 'cordova.plugins.BlePrinterPlugin', // the variable reference to call the plugin, example: navigator.geolocation
  platforms: ['Android'] // Array of platforms supported, example: ['Android', 'iOS']
})
@Injectable()
export class BlePrinterPlugin extends IonicNativePlugin {

  /**
   * 获取当前设备对蓝牙的支持状态
   * @return {Promise<any>} resolve：支持, reject：不支持
   */
  @Cordova()
  isSupportBluetooth(): Promise<any> {
    return; // We add return; here to avoid any IDE / Compiler errors
  }

  /**
   * 获取当前蓝牙的开启状态
   * @return {Promise<any>} resolve：开启状态, reject：关闭状态
   */
  @Cordova()
  bluetoothEnable(): Promise<any> {
    return;
  }

  /**
   * 开启蓝牙
   * @return {Promise<any>}  resolve：开启成功, reject：开启失败
   */
  @Cordova()
  openBluetooth(): Promise<any> {
    return;
  }

  /**
   * 获取已经配对的蓝牙设备
   * @return {Promise<any>} resolve：已配对的设备列表, reject：获取失败
   */
  @Cordova()
  getPairedDevices(): Promise<any> {
    return;
  }
  /**
   * 连接到指定的已配对的打印设备
   * @param arg0 [{"macAddress":"ble mac address", "name": "ble name"}]
   * @return {Promise<any>} resolve：连接成功, reject：连接失败
   */
  @Cordova()
  connectPrinter(arg0): Promise<any> {
    return;
  }

  /**
   * 打印文字，支持通过参数传递的方式调用PrinterInstance对象提供的方法进行样式设置 
   * @param arg0 
   */
  @Cordova()
  printText(arg0): Promise<any>{
    return;
  }

  /**
   * 打印图像
   * @param arg0 图像本地存储路径 {"imagePath": "/sdcard/image.png"}
   * @return {Promise<any>} resolve：打印成功, reject：打印失败
   */
  @Cordova()
  printImage(arg0): Promise<any> {
    return;
  }

  /**
   * 关闭与蓝牙打印设备的连接
   * @return {Promise<any>} resolve：关闭成功, reject：关闭失败
   */
  @Cordova()
  close():Promise<any> {
    return;
  }
}
