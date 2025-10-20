package com.johnnyb.graphql;

import com.johnnyb.model.Booking;
import com.johnnyb.model.Room;
import com.johnnyb.model.Customer;
import com.johnnyb.service.IRoomService;
import com.johnnyb.service.ICustomerService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;
import org.jboss.logging.Logger;

@GraphQLApi
@ApplicationScoped
public class BookingFieldResolver {

    private static final Logger LOG = Logger.getLogger(BookingFieldResolver.class);

    @Inject
    IRoomService roomService;

    @Inject
    ICustomerService customerService;

    public Room room(@Source Booking booking) {
        LOG.debugf("Resolving room for booking %s with roomId %s", booking.getId(), booking.getRoomId());
        return roomService.findById(booking.getRoomId()).orElse(null);
    }

    public Customer customer(@Source Booking booking) {
        LOG.debugf("Resolving customer for booking %s with customerId %s", booking.getId(), booking.getCustomerId());
        return customerService.findById(booking.getCustomerId()).orElse(null);
    }
}
