# Fatline Server (Self-Hosted)

This is a simple FastAPI server for the Fatline FOSS stock tracker app. It fetches stock data using yfinance and serves it to your mobile app.

## Features
- Get current stock quote
- Get historical stock prices (for graphing)

## Requirements
- Python 3.8+

## Setup

1. Open a terminal in this `server` directory.
2. Install dependencies:
   ```
   pip install -r requirements.txt
   ```
3. Start the server:
   ```
   uvicorn main:app --reload
   ```
4. By default, the server runs at `http://127.0.0.1:8000/`.

## Usage
- Set your Android app's server URL to your server's address (e.g., `http://127.0.0.1:8000/` for local, or your LAN IP for network access).
- Endpoints:
  - `/stocks/{symbol}/quote` — Get current quote
  - `/stocks/{symbol}/history?range=1mo&interval=1d` — Get price history

## Notes
- For use on a real device, ensure your phone and server are on the same network, and use your computer's LAN IP as the server URL.
- For remote access, you can deploy this server to a VPS or cloud provider.

## Security
- This server is for personal/self-hosted use. No authentication is included by default.

---

FOSS and privacy-friendly. You control your data!
