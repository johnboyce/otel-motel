import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { gql } from '@apollo/client';
import { useQuery, useMutation } from '@apollo/client/react';
import './HotelDetails.css';

const GET_HOTEL_WITH_ROOMS = gql`
  query GetHotelWithRooms($id: String!) {
    hotel(id: $id) {
      id
      name
      address
      city
      state
      zipCode
      country
      phone
      starRating
      description
    }
  }
`;

const GET_ROOMS_BY_HOTEL = gql`
  query GetRoomsByHotel($hotelId: String!) {
    roomsByHotel(hotelId: $hotelId) {
      id
      roomNumber
      roomType
      pricePerNight
      capacity
      description
    }
  }
`;

const CREATE_BOOKING = gql`
  mutation CreateBooking(
    $roomId: String!
    $customerId: String!
    $checkInDate: String!
    $checkOutDate: String!
    $numberOfGuests: Int!
    $specialRequests: String
  ) {
    createBooking(
      roomId: $roomId
      customerId: $customerId
      checkInDate: $checkInDate
      checkOutDate: $checkOutDate
      numberOfGuests: $numberOfGuests
      specialRequests: $specialRequests
    ) {
      id
      totalPrice
      status
    }
  }
`;

function HotelDetails() {
  const { id } = useParams();
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [checkIn, setCheckIn] = useState('');
  const [checkOut, setCheckOut] = useState('');
  const [guests, setGuests] = useState(1);
  const [specialRequests, setSpecialRequests] = useState('');
  const [bookingSuccess, setBookingSuccess] = useState(false);

  const { loading, error, data } = useQuery(GET_HOTEL_WITH_ROOMS, {
    variables: { id },
  });

  const { loading: roomsLoading, error: roomsError, data: roomsData } = useQuery(GET_ROOMS_BY_HOTEL, {
    variables: { hotelId: id },
  });

  const [createBooking, { loading: bookingLoading }] = useMutation(CREATE_BOOKING, {
    onCompleted: () => {
      setBookingSuccess(true);
      setTimeout(() => {
        setBookingSuccess(false);
        setSelectedRoom(null);
      }, 3000);
    },
  });

  if (loading || roomsLoading) return <div className="loading">Loading hotel details...</div>;
  if (error) return <div className="error">Error loading hotel: {error.message}</div>;
  if (roomsError) return <div className="error">Error loading rooms: {roomsError.message}</div>;

  const hotel = data.hotel;
  const rooms = roomsData.roomsByHotel || [];

  if (!hotel) {
    return <div className="error">Hotel not found</div>;
  }

  const handleBooking = async () => {
    try {
      await createBooking({
        variables: {
          roomId: selectedRoom.id,
          customerId: "1", // Default customer for demo - should be a String UUID in production
          checkInDate: checkIn,
          checkOutDate: checkOut,
          numberOfGuests: guests,
          specialRequests: specialRequests || null,
        },
      });
    } catch (err) {
      console.error('Booking error:', err);
    }
  };

  return (
    <div className="hotel-details-container">
      <Link to="/" className="back-link">← Back to Hotels</Link>
      
      <div className="hotel-header">
        <div>
          <h1>{hotel.name}</h1>
          <p className="hotel-location">
            📍 {hotel.address && `${hotel.address}, `}{hotel.city}, {hotel.state} {hotel.zipCode}
            {hotel.country && hotel.country !== 'USA' && `, ${hotel.country}`}
          </p>
          {hotel.phone && (
            <p className="hotel-phone">📞 {hotel.phone}</p>
          )}
          <div className="hotel-rating">
            {'★'.repeat(hotel.starRating)}
            {'☆'.repeat(5 - hotel.starRating)}
          </div>
        </div>
      </div>

      <div className="hotel-description">
        <p>{hotel.description || 'Welcome to our luxurious hotel with world-class amenities and service.'}</p>
      </div>

      <h2 className="rooms-title">Available Rooms</h2>
      
      <div className="rooms-grid">
        {rooms.map((room) => (
          <div key={room.id} className="room-card">
            <div className="room-header">
              <h3>Room {room.roomNumber}</h3>
              <span className="room-type">{room.roomType}</span>
            </div>
            <div className="room-body">
              <div className="room-detail">
                <span className="label">Capacity:</span>
                <span className="value">{room.capacity} guests</span>
              </div>
              <div className="room-detail">
                <span className="label">Price:</span>
                <span className="value price">${room.pricePerNight}/night</span>
              </div>
              {room.description && (
                <div className="room-description">
                  <span className="label">Description:</span>
                  <span className="value">{room.description}</span>
                </div>
              )}
            </div>
            <button 
              className="book-button"
              onClick={() => setSelectedRoom(room)}
            >
              Book Now
            </button>
          </div>
        ))}
      </div>

      {selectedRoom && (
        <div className="booking-modal-overlay" onClick={() => setSelectedRoom(null)}>
          <div className="booking-modal" onClick={(e) => e.stopPropagation()}>
            <button className="close-modal" onClick={() => setSelectedRoom(null)}>×</button>
            <h2>Book Room {selectedRoom.roomNumber}</h2>
            <form className="booking-form" onSubmit={(e) => { e.preventDefault(); handleBooking(); }}>
              <div className="form-group">
                <label>Check-in Date:</label>
                <input 
                  type="date" 
                  value={checkIn} 
                  onChange={(e) => setCheckIn(e.target.value)}
                  min={new Date().toISOString().split('T')[0]}
                  required 
                />
              </div>
              <div className="form-group">
                <label>Check-out Date:</label>
                <input 
                  type="date" 
                  value={checkOut} 
                  onChange={(e) => setCheckOut(e.target.value)}
                  min={checkIn || new Date().toISOString().split('T')[0]}
                  required 
                />
              </div>
              <div className="form-group">
                <label>Number of Guests:</label>
                <input 
                  type="number" 
                  value={guests} 
                  onChange={(e) => setGuests(parseInt(e.target.value))}
                  min="1"
                  max={selectedRoom.capacity}
                  required 
                />
              </div>
              <div className="form-group">
                <label>Special Requests:</label>
                <textarea 
                  value={specialRequests}
                  onChange={(e) => setSpecialRequests(e.target.value)}
                  rows="3"
                  placeholder="Any special requests or requirements..."
                />
              </div>
              <button 
                type="submit" 
                className="submit-booking"
                disabled={bookingLoading}
              >
                {bookingLoading ? 'Booking...' : 'Confirm Booking'}
              </button>
            </form>
            
            {bookingSuccess && (
              <div className="success-message">
                ✓ Booking confirmed! Redirecting...
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default HotelDetails;
