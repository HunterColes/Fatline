"""
Main FastAPI server for Fatline FOSS stock tracker
Run with: uvicorn server:app --host 0.0.0.0 --port 8686
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import yfinance as yf
from typing import Optional
import logging

app = FastAPI()

# Allow CORS for local development
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.on_event("startup")
async def startup_event():
    logging.basicConfig(level=logging.INFO)
    logging.info("Fatline server started.")

@app.get("/stocks/{symbol}/history")
def get_stock_history(symbol: str, range: str = "1mo", interval: str = "1d"):
    ticker = yf.Ticker(symbol)
    hist = ticker.history(period=range, interval=interval)
    if hist.empty:
        return []
    hist = hist.reset_index()
    # Convert Timestamp to string for JSON serialization
    hist['Date'] = hist['Date'].astype(str)
    return hist.to_dict(orient="records")

@app.get("/stocks/{symbol}/quote")
def get_stock_quote(symbol: str):
    ticker = yf.Ticker(symbol)
    info = ticker.info
    return {
        "symbol": symbol,
        "price": info.get("regularMarketPrice"),
        "currency": info.get("currency"),
        "name": info.get("shortName"),
    }

@app.get("/stocks/search")
def search_stocks(q: str):
    """Search for stocks by symbol or company name"""
    try:
        # Use yfinance to search for tickers
        import yfinance as yf
        
        # Simple search - you might want to implement a more sophisticated search
        # For now, try to get info for the query as a symbol
        ticker = yf.Ticker(q.upper())
        info = ticker.info
        
        if info.get("regularMarketPrice") is not None:
            return [{
                "symbol": q.upper(),
                "name": info.get("shortName", info.get("longName", q.upper())),
                "price": info.get("regularMarketPrice"),
                "change": info.get("regularMarketChange", 0),
                "changePercent": info.get("regularMarketChangePercent", 0),
                "marketCap": info.get("marketCap"),
                "volume": info.get("regularMarketVolume"),
                "exchange": info.get("fullExchangeName", ""),
                "currency": info.get("currency", "USD")
            }]
        else:
            return []
    except Exception as e:
        logging.error(f"Search error for '{q}': {e}")
        return []

# Future: Add caching and stats endpoints here
