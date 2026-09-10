package com.naveen.payment_service.service;

import com.naveen.payment_service.entity.Payment;
import com.naveen.payment_service.entity.PaymentStatus;
import com.naveen.payment_service.exception.InvalidPaymentException;
import com.naveen.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentWebhookService {

    private final RazorpayService razorpayService;
    private final PaymentRepository paymentRepository;
    private final WebhookEventService webhookEventService;

    @Transactional
    public void processWebhook(
            String payload,
            String signature,
            String eventId
    ) {


        if (eventId == null || eventId.isBlank()) {

            throw new InvalidPaymentException(
                    "Missing Razorpay webhook event ID"
            );
        }

        /*
         * 2. Verify signature BEFORE processing.
         */
        boolean valid =
                razorpayService.verifyWebhookSignature(
                        payload,
                        signature
                );

        if (!valid) {

            throw new InvalidPaymentException(
                    "Invalid Razorpay webhook signature"
            );
        }

        /*
         * 3. Ignore duplicate events.
         */
        if (webhookEventService
                .alreadyProcessed(eventId)) {

            return;
        }

        JSONObject root =
                new JSONObject(payload);

        String eventType =
                root.optString("event");

        /*
         * 4. Register event.
         */
        webhookEventService.registerEvent(
                eventId,
                eventType
        );

        /*
         * 5. Process event.
         */
        switch (eventType) {

            case "payment.captured" ->
                    handlePaymentCaptured(root);

            case "payment.failed" ->
                    handlePaymentFailed(root);

            case "order.paid" ->
                    handleOrderPaid(root);

            default ->
                    handleUnknownEvent(eventType);
        }

        /*
         * 6. Mark event processed.
         */
        webhookEventService.markProcessed(
                eventId
        );
    }

    private void handlePaymentCaptured(
            JSONObject root
    ) {

        JSONObject paymentEntity =
                root.getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

        String paymentId =
                paymentEntity.getString("id");

        String orderId =
                paymentEntity.optString("order_id");

        Payment payment =
                paymentRepository
                        .findByRazorpayOrderId(orderId)
                        .orElse(null);

        if (payment == null) {
            return;
        }

        if (payment.getStatus()
                == PaymentStatus.SUCCESS) {

            return;
        }

        long actualAmount =
                paymentEntity
                        .getLong("amount");

        long expectedAmount =
                payment.getAmount()
                        .movePointRight(2)
                        .longValueExact();

        if (actualAmount != expectedAmount) {

            payment.setStatus(
                    PaymentStatus.FAILED
            );

            payment.setFailureReason(
                    "Webhook payment amount mismatch"
            );

            paymentRepository.save(payment);

            return;
        }

        payment.setRazorpayPaymentId(
                paymentId
        );

        payment.setStatus(
                PaymentStatus.SUCCESS
        );

        paymentRepository.save(payment);
    }

    private void handlePaymentFailed(
            JSONObject root
    ) {

        JSONObject paymentEntity =
                root.getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

        String paymentId =
                paymentEntity.getString("id");

        String orderId =
                paymentEntity.optString("order_id");

        Payment payment =
                paymentRepository
                        .findByRazorpayOrderId(orderId)
                        .orElse(null);

        if (payment == null) {
            return;
        }

        /*
         * Never move SUCCESS backward to FAILED.
         */
        if (payment.getStatus()
                == PaymentStatus.SUCCESS) {

            return;
        }

        payment.setRazorpayPaymentId(
                paymentId
        );

        String reason =
                paymentEntity
                        .optString(
                                "error_description",
                                "Razorpay payment failed"
                        );

        payment.setFailureReason(reason);

        payment.setStatus(
                PaymentStatus.FAILED
        );

        paymentRepository.save(payment);
    }

    private void handleOrderPaid(
            JSONObject root
    ) {

        JSONObject orderEntity =
                root.getJSONObject("payload")
                        .getJSONObject("order")
                        .getJSONObject("entity");

        String orderId =
                orderEntity.getString("id");

        Payment payment =
                paymentRepository
                        .findByRazorpayOrderId(orderId)
                        .orElse(null);

        if (payment == null) {
            return;
        }

        /*
         * order.paid indicates the order has been paid.
         *
         * Keep SUCCESS idempotent.
         */
        if (payment.getStatus()
                == PaymentStatus.SUCCESS) {

            return;
        }

        payment.setStatus(
                PaymentStatus.SUCCESS
        );

        paymentRepository.save(payment);
    }

    private void handleUnknownEvent(
            String eventType
    ) {

        /*
         * Unknown events should not break the webhook
         * processing pipeline.
         *
         * You can add logging here later.
         */
    }
}