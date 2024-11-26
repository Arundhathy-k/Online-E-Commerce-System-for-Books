package com.kovan.service;

import com.kovan.entities.Order;
import com.kovan.entities.Payment;
import com.kovan.repository.OrderRepository;
import com.kovan.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    public Payment processPayment(Long orderId, Payment paymentDetails) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found!"));

        if (!order.getOrderStatus().equals("PENDING")) {
            throw new RuntimeException("Payment can only be processed for pending orders!");
        }

        paymentDetails.setOrder(order);
        paymentDetails.setPaymentDate(LocalDate.now());

        Payment savedPayment = paymentRepository.save(paymentDetails);

        if ("COMPLETED".equals(paymentDetails.getPaymentStatus())) {
            order.setOrderStatus("SHIPPED");
            orderRepository.save(order);
        }

        return savedPayment;
    }

    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found!"));
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment updatePayment(Long paymentId, Payment updatedPaymentDetails) {
        Payment existingPayment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found!"));

        existingPayment.setPaymentMethod(updatedPaymentDetails.getPaymentMethod());
        existingPayment.setPaymentStatus(updatedPaymentDetails.getPaymentStatus());
        existingPayment.setAmount(updatedPaymentDetails.getAmount());
        existingPayment.setPaymentReferenceNumber(updatedPaymentDetails.getPaymentReferenceNumber());
        existingPayment.setPaymentDate(LocalDate.now());

        Payment savedPayment = paymentRepository.save(existingPayment);

        if (existingPayment.getOrder() != null && "COMPLETED".equals(savedPayment.getPaymentStatus())) {
            Order order = existingPayment.getOrder();
            order.setOrderStatus("SHIPPED");
            orderRepository.save(order);
        }

        return savedPayment;
    }

    public void deletePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found!"));

        if (payment.getOrder() != null && "COMPLETED".equals(payment.getPaymentStatus())) {
            throw new RuntimeException("Cannot delete a completed payment associated with an order!");
        }

        paymentRepository.deleteById(paymentId);
    }
}

