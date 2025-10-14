package com.johnnyb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    private String id;
    private String hotelId;
    private String roomNumber;
    private String roomType;
    private BigDecimal pricePerNight;
    private Integer capacity;
    private String description;
    private List<String> bookingIds;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHotelId() { return hotelId; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getBookingIds() { return bookingIds; }
    public void setBookingIds(List<String> bookingIds) { this.bookingIds = bookingIds; }

    public static final class StringListConverter implements AttributeConverter<List<String>> {
        @Override
        public AttributeValue transformFrom(List<String> input) {
            return AttributeValue.builder().ss(input).build();
        }
        @Override
        public List<String> transformTo(AttributeValue input) {
            return input.ss();
        }
        @Override
        public EnhancedType<List<String>> type() {
            return EnhancedType.listOf(String.class);
        }
        @Override
        public AttributeValueType attributeValueType() {
            return AttributeValueType.SS;
        }
    }

    public static final TableSchema<Room> ROOM_TABLE_SCHEMA = TableSchema.builder(Room.class)
        .newItemSupplier(Room::new)
        .addAttribute(String.class, a -> a.name("id").getter(Room::getId).setter(Room::setId).tags(StaticAttributeTags.primaryPartitionKey()))
        .addAttribute(String.class, a -> a.name("hotelId").getter(Room::getHotelId).setter(Room::setHotelId))
        .addAttribute(String.class, a -> a.name("roomNumber").getter(Room::getRoomNumber).setter(Room::setRoomNumber))
        .addAttribute(String.class, a -> a.name("roomType").getter(Room::getRoomType).setter(Room::setRoomType))
        .addAttribute(BigDecimal.class, a -> a.name("pricePerNight").getter(Room::getPricePerNight).setter(Room::setPricePerNight))
        .addAttribute(Integer.class, a -> a.name("capacity").getter(Room::getCapacity).setter(Room::setCapacity))
        .addAttribute(String.class, a -> a.name("description").getter(Room::getDescription).setter(Room::setDescription))
        .addAttribute(EnhancedType.listOf(String.class), a -> a.name("bookingIds").getter(Room::getBookingIds).setter(Room::setBookingIds).attributeConverter(new StringListConverter()))
        .build();
}
