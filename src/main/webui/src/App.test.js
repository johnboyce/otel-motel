import { render, screen } from '@testing-library/react';
import App from './App';

test('renders OTEL MOTEL header', () => {
  render(<App />);
  const headerElement = screen.getByText(/OTEL MOTEL/i);
  expect(headerElement).toBeInTheDocument();
});

test('renders navigation links', () => {
  render(<App />);
  const hotelsLink = screen.getByText(/Hotels/i);
  const bookingsLink = screen.getByText(/My Bookings/i);
  expect(hotelsLink).toBeInTheDocument();
  expect(bookingsLink).toBeInTheDocument();
});
