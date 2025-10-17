package com.johnnyb.service;

import com.johnnyb.model.Hotel;

import java.util.List;
import java.util.Optional;

public interface IHotelService {
    Hotel save(Hotel hotel);
    Optional<Hotel> findById(String id);
    List<Hotel> findAll();
    List<Hotel> findByCity(String city);
    List<Hotel> findByCountry(String country);
    void delete(String id);
    long count();
}
