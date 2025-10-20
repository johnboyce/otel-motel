import React from 'react';
import { gql } from '@apollo/client';
import { useQuery } from '@apollo/client/react';
import { Link } from 'react-router-dom';
import './HotelList.css';

const GET_HOTELS = gql`
  query GetHotels {
    hotels {
      id
      name
      city
      state
      starRating
      description
    }
  }
`;

function HotelList() {
  const { loading, error, data } = useQuery(GET_HOTELS);

  if (loading) return <div className="loading">Loading hotels...</div>;
  if (error) return <div className="error">Error loading hotels: {error.message}</div>;

  return (
    <div className="hotel-list-container">
      <h2 className="section-title">Discover Our Premium Hotels</h2>
      <div className="hotel-grid">
        {data.hotels.map((hotel) => (
          <Link to={`/hotel/${hotel.id}`} key={hotel.id} className="hotel-card-link">
            <div className="hotel-card">
              <div className="hotel-card-header">
                <h3>{hotel.name}</h3>
                <div className="rating">
                  {'★'.repeat(hotel.starRating)}
                  {'☆'.repeat(5 - hotel.starRating)}
                </div>
              </div>
              <div className="hotel-card-body">
                <p className="location">📍 {hotel.city}, {hotel.state}</p>
                <p className="description">{hotel.description || 'Experience luxury and comfort at our premium location.'}</p>
              </div>
              <div className="hotel-card-footer">
                <button className="view-button">View Details →</button>
              </div>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}

export default HotelList;
