package com.gjh.shopdemo.listener.handler;

public abstract class AbstractStockUpdateHandler {

    protected AbstractStockUpdateHandler next;

    public AbstractStockUpdateHandler setNext(AbstractStockUpdateHandler next) {
        this.next = next;
        return next;
    }

    public abstract void handle(StockUpdateContext ctx);

    protected void passToNext(StockUpdateContext ctx) {
        if (next != null) {
            next.handle(ctx);
        }
    }
}