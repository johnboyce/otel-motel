package com.johnnyb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    private String id;
    private String roomId;
    private String customerId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfGuests;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private String specialRequests;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    public Integer getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(Integer numberOfGuests) { this.numberOfGuests = numberOfGuests; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public enum BookingStatus {
        PENDING,
        CONFIRMED,
        CANCELLED,
        COMPLETED
    }

    public static final class LocalDateConverter implements AttributeConverter<LocalDate> {
        @Override
        public AttributeValue transformFrom(LocalDate input) {
            return AttributeValue.builder().s(input != null ? input.toString() : null).build();
        }
        @Override
        public LocalDate transformTo(AttributeValue input) {
            return input.s() != null ? LocalDate.parse(input.s()) : null;
        }
        @Override
        public EnhancedType<LocalDate> type() {
            return EnhancedType.of(LocalDate.class);
        }
        @Override
        public AttributeValueType attributeValueType() {
            return AttributeValueType.S;
        }
    }
    public static final class BigDecimalConverter implements AttributeConverter<BigDecimal> {
        @Override
        public AttributeValue transformFrom(BigDecimal input) {
            return AttributeValue.builder().n(input != null ? input.toPlainString() : null).build();
        }
        @Override
        public BigDecimal transformTo(AttributeValue input) {
            return input.n() != null ? new BigDecimal(input.n()) : null;
        }
        @Override
        public EnhancedType<BigDecimal> type() {
            return EnhancedType.of(BigDecimal.class);
        }
        @Override
        public AttributeValueType attributeValueType() {
            return AttributeValueType.N;
        }
    }
    public static final class BookingStatusConverter implements AttributeConverter<BookingStatus> {
        @Override
        public AttributeValue transformFrom(BookingStatus input) {
            return AttributeValue.builder().s(input != null ? input.name() : null).build();
        }
        @Override
        public BookingStatus transformTo(AttributeValue input) {
            return input.s() != null ? BookingStatus.valueOf(input.s()) : null;
        }
        @Override
        public EnhancedType<BookingStatus> type() {
            return EnhancedType.of(BookingStatus.class);
        }
        @Override
        public AttributeValueType attributeValueType() {
            return AttributeValueType.S;
        }
    }

    public static final TableSchema<Booking> BOOKING_TABLE_SCHEMA = TableSchema.builder(Booking.class)
        .newItemSupplier(Booking::new)
        .addAttribute(String.class, a -> a.name("id").getter(Booking::getId).setter(Booking::setId).tags(StaticAttributeTags.primaryPartitionKey()))
        .addAttribute(String.class, a -> a.name("roomId").getter(Booking::getRoomId).setter(Booking::setRoomId))
        .addAttribute(String.class, a -> a.name("customerId").getter(Booking::getCustomerId).setter(Booking::setCustomerId))
        .addAttribute(LocalDate.class, a -> a.name("checkInDate").getter(Booking::getCheckInDate).setter(Booking::setCheckInDate).attributeConverter(new LocalDateConverter()))
        .addAttribute(LocalDate.class, a -> a.name("checkOutDate").getter(Booking::getCheckOutDate).setter(Booking::setCheckOutDate).attributeConverter(new LocalDateConverter()))
        .addAttribute(BigDecimal.class, a -> a.name("totalPrice").getter(Booking::getTotalPrice).setter(Booking::setTotalPrice).attributeConverter(new BigDecimalConverter()))
        .addAttribute(BookingStatus.class, a -> a.name("status").getter(Booking::getStatus).setter(Booking::setStatus).attributeConverter(new BookingStatusConverter()))
        .addAttribute(String.class, a -> a.name("specialRequests").getter(Booking::getSpecialRequests).setter(Booking::setSpecialRequests))
        .build();
}
