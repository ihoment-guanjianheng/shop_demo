package com.gjh.shopdemo.message;

import java.util.List;

public interface MessageSender {

    void send(String message, List<String> receivers);
}
