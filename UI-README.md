# OTEL MOTEL UI Documentation

## Overview

OTEL MOTEL now features a modern, professional web UI built with React and integrated seamlessly with Quarkus using the Quinoa extension. The UI provides an intuitive interface for browsing hotels, viewing room details, and managing bookings.

## Technologies Used

- **React 18** - Modern React with hooks for component development
- **React Router** - Client-side routing for navigation
- **Apollo Client 4** - GraphQL client for backend integration
- **Quinoa** - Quarkus extension for frontend integration
- **CSS3** - Modern styling with gradients and animations

## Features

### 1. Hotel Browsing
- Grid layout displaying all available hotels
- Star ratings and location information
- Responsive design that adapts to screen size
- Hover effects for enhanced interactivity

### 2. Hotel Details
- Detailed view of individual hotels
- Complete room listing with amenities
- Real-time availability information
- Booking functionality with modal dialog

### 3. Booking Management
- View all upcoming bookings
- Booking status indicators (Confirmed, Cancelled, Completed)
- Cancel bookings with confirmation
- Detailed booking information display

### 4. Professional Design
- Modern gradient backgrounds (purple/blue theme)
- Smooth animations and transitions
- Card-based layouts for content organization
- Fully responsive design for mobile and desktop
- Intuitive navigation with clear visual hierarchy

## Project Structure

```
src/main/webui/
├── public/                  # Static assets
│   ├── index.html          # HTML entry point
│   ├── favicon.ico         # App icon
│   └── manifest.json       # PWA manifest
├── src/
│   ├── components/         # React components
│   │   ├── HotelList.js/css       # Hotel grid view
│   │   ├── HotelDetails.js/css    # Hotel details and booking
│   │   └── BookingList.js/css     # Booking management
│   ├── App.js              # Main application component
│   ├── App.css             # Main application styles
│   ├── index.js            # React entry point
│   └── index.css           # Global styles
├── package.json            # NPM dependencies
└── build/                  # Production build (generated)
```

## Running the Application

### Development Mode

When running Quarkus in dev mode, Quinoa automatically:
1. Detects changes to the React application
2. Rebuilds the frontend
3. Serves it alongside the backend

```bash
./mvnw quarkus:dev
```

Access the application at: http://localhost:8080/

### Production Build

Build the complete application (backend + frontend):

```bash
./mvnw clean package
```

The frontend will be automatically built and bundled into the Quarkus application JAR.

### Frontend Development

To work on the frontend independently:

```bash
cd src/main/webui
npm start
```

This starts the React development server at http://localhost:3000/ with hot reload.

## GraphQL Integration

The UI integrates with the backend GraphQL API using Apollo Client:

- **Endpoint**: `/graphql`
- **Queries Used**:
  - `hotels` - Fetch all hotels
  - `hotel(id)` - Fetch hotel with rooms
  - `upcomingBookings` - Fetch user bookings
- **Mutations Used**:
  - `createBooking` - Create new booking
  - `cancelBooking` - Cancel existing booking

## Configuration

### Quinoa Configuration (application.properties)

```properties
# Enable Quinoa for frontend integration
quarkus.quinoa.build-dir=build
quarkus.quinoa.enable-spa-routing=true
quarkus.quinoa.package-manager-install=true
quarkus.quinoa.package-manager-install.node-version=20.11.1
quarkus.quinoa.package-manager=npm
```

### Apollo Client Configuration

The GraphQL endpoint is configured in `src/main/webui/src/App.js`:

```javascript
const client = new ApolloClient({
  uri: '/graphql',
  cache: new InMemoryCache(),
});
```

## Key Design Decisions

1. **Single Page Application (SPA)** - React Router provides client-side routing
2. **GraphQL Integration** - Apollo Client for efficient data fetching
3. **Component-Based Architecture** - Reusable React components
4. **CSS Modules** - Component-specific styling prevents conflicts
5. **Responsive Design** - Mobile-first approach with media queries
6. **Modern Aesthetics** - Gradient backgrounds, smooth animations, card layouts

## Styling Theme

### Colors
- **Primary**: Purple gradient (#667eea to #764ba2)
- **Accent**: Gold (#ffd700) for ratings
- **Background**: White cards on gradient background
- **Text**: Dark gray (#333) for readability

### Typography
- **Font**: System fonts (San Francisco, Segoe UI, Roboto)
- **Headings**: Bold, large sizes for hierarchy
- **Body**: Clean, readable line-height (1.6)

## Browser Support

- Chrome/Edge (latest)
- Firefox (latest)
- Safari (latest)
- Mobile browsers (iOS Safari, Chrome Mobile)

## Future Enhancements

Potential improvements for the UI:

1. **User Authentication** - Login/logout functionality
2. **Search & Filters** - Search hotels by location, price, rating
3. **Date Picker** - Enhanced date selection for bookings
4. **Payment Integration** - Credit card processing
5. **Booking Confirmation** - Email notifications
6. **Reviews & Ratings** - Customer feedback system
7. **Image Gallery** - Hotel and room photos
8. **Advanced Animations** - Page transitions, loading states
9. **Dark Mode** - Theme switcher
10. **Accessibility** - WCAG 2.1 AA compliance

## Troubleshooting

### Frontend not loading
- Check if Quinoa built the frontend: `ls target/quinoa/build`
- Verify application.properties has correct Quinoa config
- Try rebuilding: `./mvnw clean package`

### GraphQL errors
- Ensure backend is running
- Check browser console for Apollo Client errors
- Verify GraphQL endpoint is accessible at `/graphql`

### Node/NPM issues
- Delete `node_modules`: `rm -rf src/main/webui/node_modules`
- Clear npm cache: `npm cache clean --force`
- Reinstall: `cd src/main/webui && npm install`

## Development Tips

1. **Use React DevTools** - Browser extension for debugging React components
2. **Apollo DevTools** - Browser extension for inspecting GraphQL queries
3. **Hot Reload** - Changes to React code reload automatically in dev mode
4. **Console Logging** - Check browser console for errors
5. **Network Tab** - Monitor GraphQL requests in browser DevTools

## Contributing

When adding new features to the UI:

1. Create new components in `src/main/webui/src/components/`
2. Add corresponding CSS files for styling
3. Update routing in `App.js` if needed
4. Test responsive design at different screen sizes
5. Ensure GraphQL queries are efficient
6. Follow the existing code style and patterns

## Resources

- [React Documentation](https://react.dev)
- [Apollo Client Documentation](https://www.apollographql.com/docs/react/)
- [Quinoa Documentation](https://docs.quarkiverse.io/quarkus-quinoa/dev/)
- [Quarkus Documentation](https://quarkus.io/guides/)
- [GraphQL Documentation](https://graphql.org/learn/)

---

**Built with** ❤️ **using React, Apollo Client, and Quinoa**
