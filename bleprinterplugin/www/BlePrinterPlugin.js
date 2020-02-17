var exec = require('cordova/exec');

exports.isSupportBluetooth = function (success, error) {
    exec(success, error, 'BlePrinterPlugin', 'isSupportBluetooth', []);
};

exports.openBluetooth = function (success, error) {
    exec(success, error, 'BlePrinterPlugin', 'openBluetooth', []);
};

exports.bluetoothEnable = function (success, error) {
    exec(success, error, 'BlePrinterPlugin', 'bluetoothEnable', []);
};

exports.getPairedDevices = function (success, error) {
    exec(success, error, 'BlePrinterPlugin', 'getPairedDevices', []);
};

exports.connectPrinter = function (arg0, success, error) {
    exec(success, error, 'BlePrinterPlugin', 'connectPrinter', [arg0]);
};

exports.printText = function(arg0, success, error){
    exec(success, error, 'BlePrinterPlugin', 'printText', arg0);
}

exports.printImage = function(arg0, success, error){
    exec(success, error, 'BlePrinterPlugin', 'printImage', [arg0]);
}

exports.close = function (success, error) {
    exec(success, error, 'BlePrinterPlugin', 'close', []);
};

