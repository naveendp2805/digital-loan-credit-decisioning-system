package com.naveen.payment_service.service;

import com.razorpay.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RazorpayService {

    private final RazorpayClient razorpayClient;

    private final String keySecret;

    private final String webhookSecret;

    public RazorpayService(RazorpayClient razorpayClient,
                           @Value("${razorpay.secret}") String keySecret,
                           @Value("${razorpay.webhook-secret:}") String webhookSecret
    ) {

        this.razorpayClient = razorpayClient;
        this.keySecret = keySecret;
        this.webhookSecret = webhookSecret;
    }

    public Order createOrder(BigDecimal amount, String currency, String receipt) throws RazorpayException {

        long amountInSubunits = amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();

        JSONObject orderRequest = new JSONObject();

        orderRequest.put("amount", amountInSubunits);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", receipt);

        return razorpayClient.orders.create(orderRequest);
    }

    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        try {
            JSONObject options = new JSONObject();

            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            return Utils.verifyPaymentSignature(options, keySecret);
        } catch(Exception e) {
            return false;
        }
    }

    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            if(webhookSecret == null || webhookSecret.isBlank())
                return false;

            return Utils.verifyWebhookSignature(payload, signature, webhookSecret);
        } catch(Exception e) {
            return false;
        }
    }

    public Payment fetchPayment(String paymentId) throws RazorpayException {
        return razorpayClient.payments.fetch(paymentId);
    }

    public Order fetchOrder(String orderId) throws RazorpayException{
        return razorpayClient.orders.fetch(orderId);
    }
}
