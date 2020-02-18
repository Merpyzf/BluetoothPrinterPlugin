# BluetoothPrinterPlugin
基于Cordova开发的蓝牙打印凭条插件
## bleprinterplugin
cordova蓝牙凭条打印插件
## ble-printer-plugin
兼容Ionic Native 类型声明文件
## 开始使用
### 1. 添加 bleprinterplugin 蓝牙凭条打印插件到Ionic
ionic cordova plugin add <插件路径>
### 2. 克隆ionic-native项目到本地，之后在其根路径编译 ble-printer-plugin
npm run build ble-printer-plugin
### 3. 将编译生成在/dist/@ionic-native/plugins/ble-printer-plugin的文件夹手动拷贝到项目的node_module/@ionic-native
