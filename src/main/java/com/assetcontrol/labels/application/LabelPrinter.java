package com.assetcontrol.labels.application;

public interface LabelPrinter {

    void print(String printerIp, LabelContent content);
}
