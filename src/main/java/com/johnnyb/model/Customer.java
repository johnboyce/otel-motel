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
public class Customer {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String creditCardNumber;
    private String creditCardExpiry;
    private String creditCardCvv;
    private List<String> bookingIds;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCreditCardNumber() { return creditCardNumber; }
    public void setCreditCardNumber(String creditCardNumber) { this.creditCardNumber = creditCardNumber; }
    public String getCreditCardExpiry() { return creditCardExpiry; }
    public void setCreditCardExpiry(String creditCardExpiry) { this.creditCardExpiry = creditCardExpiry; }
    public String getCreditCardCvv() { return creditCardCvv; }
    public void setCreditCardCvv(String creditCardCvv) { this.creditCardCvv = creditCardCvv; }
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

    public static final TableSchema<Customer> CUSTOMER_TABLE_SCHEMA = TableSchema.builder(Customer.class)
        .newItemSupplier(Customer::new)
        .addAttribute(String.class, a -> a.name("id").getter(Customer::getId).setter(Customer::setId).tags(StaticAttributeTags.primaryPartitionKey()))
        .addAttribute(String.class, a -> a.name("firstName").getter(Customer::getFirstName).setter(Customer::setFirstName))
        .addAttribute(String.class, a -> a.name("lastName").getter(Customer::getLastName).setter(Customer::setLastName))
        .addAttribute(String.class, a -> a.name("email").getter(Customer::getEmail).setter(Customer::setEmail))
        .addAttribute(String.class, a -> a.name("phone").getter(Customer::getPhone).setter(Customer::setPhone))
        .addAttribute(String.class, a -> a.name("address").getter(Customer::getAddress).setter(Customer::setAddress))
        .addAttribute(String.class, a -> a.name("creditCardNumber").getter(Customer::getCreditCardNumber).setter(Customer::setCreditCardNumber))
        .addAttribute(String.class, a -> a.name("creditCardExpiry").getter(Customer::getCreditCardExpiry).setter(Customer::setCreditCardExpiry))
        .addAttribute(String.class, a -> a.name("creditCardCvv").getter(Customer::getCreditCardCvv).setter(Customer::setCreditCardCvv))
        .addAttribute(EnhancedType.listOf(String.class), a -> a.name("bookingIds").getter(Customer::getBookingIds).setter(Customer::setBookingIds).attributeConverter(new StringListConverter()))
        .build();
}
