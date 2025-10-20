package com.johnnyb.graphql;

import com.johnnyb.model.Room;
import com.johnnyb.model.Hotel;
import com.johnnyb.service.IHotelService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;
import org.jboss.logging.Logger;

@GraphQLApi
@ApplicationScoped
public class RoomFieldResolver {

    private static final Logger LOG = Logger.getLogger(RoomFieldResolver.class);

    @Inject
    IHotelService hotelService;

    public Hotel hotel(@Source Room room) {
        LOG.debugf("Resolving hotel for room %s with hotelId %s", room.getId(), room.getHotelId());
        return hotelService.findById(room.getHotelId()).orElse(null);
    }
}
