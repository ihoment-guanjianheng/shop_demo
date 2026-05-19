package com.gjh.shopdemo.notify;

import java.util.List;

public interface NotificationSender {

    void send(String message, List<String> receivers);
}
