import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { ApolloClient, InMemoryCache, HttpLink } from '@apollo/client';
import { ApolloProvider } from '@apollo/client/react';
import HotelList from './components/HotelList';
import HotelDetails from './components/HotelDetails';
import BookingList from './components/BookingList';
import './App.css';

const client = new ApolloClient({
  link: new HttpLink({
    uri: '/graphql',
  }),
  cache: new InMemoryCache(),
});

function App() {
  return (
    <ApolloProvider client={client}>
      <Router>
        <div className="App">
          <header className="App-header">
            <div className="header-content">
              <h1>🏨 OTEL MOTEL</h1>
              <p className="tagline">Your Premium Hotel Booking Experience</p>
            </div>
            <nav className="main-nav">
              <Link to="/" className="nav-link">Hotels</Link>
              <Link to="/bookings" className="nav-link">My Bookings</Link>
            </nav>
          </header>
          <main className="App-main">
            <Routes>
              <Route path="/" element={<HotelList />} />
              <Route path="/hotel/:id" element={<HotelDetails />} />
              <Route path="/bookings" element={<BookingList />} />
            </Routes>
          </main>
          <footer className="App-footer">
            <p>© 2025 OTEL MOTEL - Powered by OpenTelemetry & Observability</p>
          </footer>
        </div>
      </Router>
    </ApolloProvider>
  );
}

export default App;
