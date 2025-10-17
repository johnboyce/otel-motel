package com.johnnyb.service;

import com.johnnyb.model.Room;

import java.util.List;
import java.util.Optional;

public interface IRoomService {
    Room save(Room room);
    Optional<Room> findById(String id);
    List<Room> findAll();
    List<Room> findByHotelId(String hotelId);
    void delete(String id);
    long count();
}
