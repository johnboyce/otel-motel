import React from 'react';
import { gql } from '@apollo/client';
import { useQuery, useMutation } from '@apollo/client/react';
import './BookingList.css';

const GET_UPCOMING_BOOKINGS = gql`
  query GetUpcomingBookings {
    upcomingBookings {
      id
      checkInDate
      checkOutDate
      numberOfGuests
      totalPrice
      status
      specialRequests
      room {
        id
        roomNumber
        roomType
        hotel {
          id
          name
          city
          state
        }
      }
      customer {
        firstName
        lastName
        email
      }
    }
  }
`;

const CANCEL_BOOKING = gql`
  mutation CancelBooking($bookingId: String!) {
    cancelBooking(bookingId: $bookingId) {
      id
      status
    }
  }
`;

function BookingList() {
  const { loading, error, data, refetch } = useQuery(GET_UPCOMING_BOOKINGS);
  const [cancelBooking] = useMutation(CANCEL_BOOKING, {
    onCompleted: () => refetch(),
  });

  if (loading) return <div className="loading">Loading bookings...</div>;
  if (error) return <div className="error">Error loading bookings: {error.message}</div>;

  const handleCancel = async (bookingId) => {
    if (window.confirm('Are you sure you want to cancel this booking?')) {
      try {
        await cancelBooking({ variables: { bookingId } });
      } catch (err) {
        console.error('Cancel error:', err);
      }
    }
  };

  return (
    <div className="bookings-container">
      <h2 className="bookings-title">My Upcoming Bookings</h2>
      
      {data.upcomingBookings.length === 0 ? (
        <div className="empty-state">
          <p>You don't have any upcoming bookings.</p>
          <a href="/" className="browse-hotels-link">Browse Hotels</a>
        </div>
      ) : (
        <div className="bookings-list">
          {data.upcomingBookings.map((booking) => (
            <div key={booking.id} className={`booking-card ${booking.status.toLowerCase()}`}>
              <div className="booking-header">
                <div>
                  <h3>{booking.room.hotel.name}</h3>
                  <p className="booking-location">
                    📍 {booking.room.hotel.city}, {booking.room.hotel.state}
                  </p>
                </div>
                <div className={`status-badge ${booking.status.toLowerCase()}`}>
                  {booking.status}
                </div>
              </div>
              
              <div className="booking-details">
                <div className="detail-row">
                  <span className="detail-label">Room:</span>
                  <span className="detail-value">
                    {booking.room.roomType} - Room #{booking.room.roomNumber}
                  </span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">Check-in:</span>
                  <span className="detail-value">{booking.checkInDate}</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">Check-out:</span>
                  <span className="detail-value">{booking.checkOutDate}</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">Guests:</span>
                  <span className="detail-value">{booking.numberOfGuests}</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">Total Price:</span>
                  <span className="detail-value price">${booking.totalPrice}</span>
                </div>
                {booking.specialRequests && (
                  <div className="special-requests">
                    <span className="detail-label">Special Requests:</span>
                    <p>{booking.specialRequests}</p>
                  </div>
                )}
              </div>
              
              {booking.status === 'CONFIRMED' && (
                <div className="booking-actions">
                  <button 
                    className="cancel-button"
                    onClick={() => handleCancel(booking.id)}
                  >
                    Cancel Booking
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default BookingList;
