package dev.emreozcan.pazarsync.orderservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "questions", uniqueConstraints = @UniqueConstraint(name = "uq_questions_external", columnNames = {"marketplace", "external_question_id"}))
public class CustomerQuestion {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String marketplace;

    @Column(name = "external_question_id", nullable = false)
    private String externalQuestionId;

    @Column(name = "product_barcode", nullable = false)
    private String productBarcode;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Column(name = "asked_at", nullable = false)
    private Instant askedAt;

    @Column(nullable = false)
    private String status;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(String marketplace) {
        this.marketplace = marketplace;
    }

    public String getExternalQuestionId() {
        return externalQuestionId;
    }

    public void setExternalQuestionId(String externalQuestionId) {
        this.externalQuestionId = externalQuestionId;
    }

    public String getProductBarcode() {
        return productBarcode;
    }

    public void setProductBarcode(String productBarcode) {
        this.productBarcode = productBarcode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public Instant getAskedAt() {
        return askedAt;
    }

    public void setAskedAt(Instant askedAt) {
        this.askedAt = askedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
