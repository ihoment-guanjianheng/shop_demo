package com.gjh.shopdemo.listener.handler;

public abstract class AbstractOrderDelayHandler {

    protected AbstractOrderDelayHandler next;

    public AbstractOrderDelayHandler setNext(AbstractOrderDelayHandler next) {
        this.next = next;
        return next;
    }

    public abstract void handle(OrderDelayContext ctx);

    protected void passToNext(OrderDelayContext ctx) {
        if (next != null) {
            next.handle(ctx);
        }
    }
}