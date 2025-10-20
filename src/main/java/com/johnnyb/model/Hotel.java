package com.johnnyb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class Hotel {
    private String id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String phone;
    private String description;
    private Integer starRating;
    private List<String> roomIds;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStarRating() { return starRating; }
    public void setStarRating(Integer starRating) { this.starRating = starRating; }
    public List<String> getRoomIds() { return roomIds; }
    public void setRoomIds(List<String> roomIds) { this.roomIds = roomIds; }

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

    public static final TableSchema<Hotel> HOTEL_TABLE_SCHEMA = TableSchema.builder(Hotel.class)
        .newItemSupplier(Hotel::new)
        .addAttribute(String.class, a -> a.name("id").getter(Hotel::getId).setter(Hotel::setId).tags(StaticAttributeTags.primaryPartitionKey()))
        .addAttribute(String.class, a -> a.name("name").getter(Hotel::getName).setter(Hotel::setName))
        .addAttribute(String.class, a -> a.name("address").getter(Hotel::getAddress).setter(Hotel::setAddress))
        .addAttribute(String.class, a -> a.name("city").getter(Hotel::getCity).setter(Hotel::setCity))
        .addAttribute(String.class, a -> a.name("state").getter(Hotel::getState).setter(Hotel::setState))
        .addAttribute(String.class, a -> a.name("zipCode").getter(Hotel::getZipCode).setter(Hotel::setZipCode))
        .addAttribute(String.class, a -> a.name("country").getter(Hotel::getCountry).setter(Hotel::setCountry))
        .addAttribute(String.class, a -> a.name("phone").getter(Hotel::getPhone).setter(Hotel::setPhone))
        .addAttribute(String.class, a -> a.name("description").getter(Hotel::getDescription).setter(Hotel::setDescription))
        .addAttribute(Integer.class, a -> a.name("starRating").getter(Hotel::getStarRating).setter(Hotel::setStarRating))
        .addAttribute(EnhancedType.listOf(String.class), a -> a.name("roomIds").getter(Hotel::getRoomIds).setter(Hotel::setRoomIds).attributeConverter(new StringListConverter()))
        .build();
}
