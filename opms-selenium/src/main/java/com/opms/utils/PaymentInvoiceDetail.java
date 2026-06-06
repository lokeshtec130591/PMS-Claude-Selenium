package com.opms.utils;

public class PaymentInvoiceDetail {

    private final String paymentMethod;
    private final String invoiceAmount;

    public PaymentInvoiceDetail(String paymentMethod, String invoiceAmount) {
        this.paymentMethod = paymentMethod;
        this.invoiceAmount = invoiceAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getInvoiceAmount() {
        return invoiceAmount;
    }
}
