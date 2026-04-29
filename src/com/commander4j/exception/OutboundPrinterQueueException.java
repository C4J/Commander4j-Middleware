package com.commander4j.exception;

public class OutboundPrinterQueueException extends Exception { 
    private static final long serialVersionUID = 1L;

	public OutboundPrinterQueueException(String errorMessage) {
        super(errorMessage);
    }
}
