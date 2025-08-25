# Fatline Server

FastAPI-based backend server for the Fatline FOSS stock tracking application. Provides real-time stock data, user authentication, and portfolio synchronization.

## Features

- **Real-time Stock Data**: Yahoo Finance integration for live market data
- **User Authentication**: JWT-based auth with bcrypt password hashing
- **Portfolio Management**: Multi-user portfolio tracking and synchronization
- **Data Caching**: PostgreSQL-based caching for improved performance
- **Docker Support**: Containerized deployment with PostgreSQL

## Quick Start

### Prerequisites

- Docker and Docker Compose
- Python 3.11+ (for local development)

### Running with Docker (Recommended)

```bash
# Start the complete stack (server + database)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the stack
docker-compose down
```

The server will be available at `http://localhost:8686`

### Local Development

```bash
# Install dependencies
pip install -r requirements.txt

# Set environment variables
export DATABASE_URL="postgresql://fatline:fatline@localhost:5432/fatline"

# Start PostgreSQL (if not using Docker)
# Then run the server
uvicorn server:app --host 0.0.0.0 --port 8686 --reload
```

## API Endpoints

### Authentication

- `POST /auth/register` - Register new user
- `POST /auth/login` - User login

### Stock Data

- `GET /stocks/{symbol}/quote` - Get current stock quote
- `GET /stocks/{symbol}/history` - Get historical data
- `GET /stocks/search?q={query}` - Search for stocks

### Portfolio (Authenticated)

- `GET /portfolio/holdings` - Get user's portfolio
- `POST /portfolio/holdings` - Add stock to portfolio

### Health Check

- `GET /health` - Server health status

## Configuration

### Environment Variables

- `DATABASE_URL`: PostgreSQL connection string
- `SECRET_KEY`: JWT signing key (change in production!)

### Database

The server uses PostgreSQL with automatic table creation and sample data initialization. Database schema is automatically migrated on startup.

## Security Notes

⚠️ **Important**: Change the `SECRET_KEY` in `auth.py` before production deployment!

## Development

### Project Structure

```
server/
├── server.py          # Main FastAPI application
├── auth.py           # Authentication utilities
├── database.py       # Database models and configuration
├── requirements.txt  # Python dependencies
├── docker-compose.yml # Docker deployment configuration
├── Dockerfile        # Container build instructions
└── init.sql         # Database initialization script
```

### Adding New Features

1. Define database models in `database.py`
2. Add API endpoints in `server.py`
3. Update authentication logic in `auth.py` if needed
4. Test with the Android client

## Troubleshooting

### Connection Issues

- Ensure PostgreSQL is running and accessible
- Check DATABASE_URL environment variable
- Verify firewall settings for port 8686

### Database Issues

- Check Docker logs: `docker-compose logs postgres`
- Verify database credentials
- Ensure database initialization completed

### Performance

- Monitor database connection pool
- Consider Redis for caching if needed
- Scale horizontally behind a load balancer

## License

This project is part of the Fatline FOSS stock tracker. See the main project LICENSE for details.
