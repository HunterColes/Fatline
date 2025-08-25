"""
Enhanced FastAPI server for Fatline FOSS stock tracker with PostgreSQL and user accounts
Run with: uvicorn server:app --host 0.0.0.0 --port 8686
"""
from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security import HTTPBearer
from sqlalchemy.orm import Session
from pydantic import BaseModel
from datetime import datetime, timedelta
import yfinance as yf
from typing import Optional, List
import logging

from database import get_db, create_tables, User, Stock, StockPrice, PortfolioHolding
from auth import (
    authenticate_user, create_access_token, get_current_user, 
    get_password_hash, ACCESS_TOKEN_EXPIRE_MINUTES
)

app = FastAPI(title="Fatline Stock Tracker API", version="1.0.0")

# Allow CORS for local development
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Pydantic models for requests/responses
class UserCreate(BaseModel):
    username: str
    password: str

class UserLogin(BaseModel):
    username: str
    password: str

class Token(BaseModel):
    access_token: str
    token_type: str

class StockSearchResponse(BaseModel):
    symbol: str
    name: str
    price: float
    change: float
    changePercent: float
    marketCap: Optional[int]
    volume: Optional[int]
    exchange: str
    currency: str = "USD"

class PortfolioHoldingRequest(BaseModel):
    symbol: str
    shares: float
    price: float

class PortfolioHoldingResponse(BaseModel):
    symbol: str
    name: str
    shares: float
    average_cost: float
    current_price: float
    total_value: float
    total_return: float
    return_percent: float

@app.on_event("startup")
async def startup_event():
    logging.basicConfig(level=logging.INFO)
    logging.info("Creating database tables...")
    create_tables()
    logging.info("Fatline server started.")

# Authentication endpoints
@app.post("/auth/register", response_model=Token)
def register_user(user: UserCreate, db: Session = Depends(get_db)):
    # Check if user already exists
    db_user = db.query(User).filter(User.username == user.username).first()
    if db_user:
        raise HTTPException(
            status_code=400,
            detail="Username already registered"
        )
    
    # Create new user
    hashed_password = get_password_hash(user.password)
    db_user = User(username=user.username, hashed_password=hashed_password)
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    
    # Create access token
    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        data={"sub": user.username}, expires_delta=access_token_expires
    )
    return {"access_token": access_token, "token_type": "bearer"}

@app.post("/auth/login", response_model=Token)
def login_user(user: UserLogin, db: Session = Depends(get_db)):
    authenticated_user = authenticate_user(db, user.username, user.password)
    if not authenticated_user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        data={"sub": authenticated_user.username}, expires_delta=access_token_expires
    )
    return {"access_token": access_token, "token_type": "bearer"}

# Stock data endpoints
@app.get("/stocks/{symbol}/history")
def get_stock_history(
    symbol: str, 
    range: str = "1mo", 
    interval: str = "1d",
    db: Session = Depends(get_db)
):
    """Get historical stock data with caching"""
    # TODO: Check cache first, then fetch from yfinance if needed
    ticker = yf.Ticker(symbol)
    hist = ticker.history(period=range, interval=interval)
    if hist.empty:
        return []
    
    hist = hist.reset_index()
    hist['Date'] = hist['Date'].astype(str)
    
    # Cache the data in database
    # TODO: Implement caching logic
    
    return hist.to_dict(orient="records")

@app.get("/stocks/{symbol}/quote")
def get_stock_quote(symbol: str, db: Session = Depends(get_db)):
    """Get current stock quote"""
    ticker = yf.Ticker(symbol)
    info = ticker.info
    
    # Update stock info in database
    stock = db.query(Stock).filter(Stock.symbol == symbol).first()
    if not stock:
        stock = Stock(
            symbol=symbol,
            name=info.get("shortName", info.get("longName", symbol)),
            exchange=info.get("fullExchangeName", ""),
            currency=info.get("currency", "USD")
        )
        db.add(stock)
        db.commit()
    
    return {
        "symbol": symbol,
        "price": info.get("regularMarketPrice"),
        "currency": info.get("currency"),
        "name": info.get("shortName"),
    }

@app.get("/stocks/search")
def search_stocks(q: str, db: Session = Depends(get_db)):
    """Search for stocks by symbol or company name"""
    try:
        ticker = yf.Ticker(q.upper())
        info = ticker.info
        
        if info.get("regularMarketPrice") is not None:
            # Update/create stock in database
            stock = db.query(Stock).filter(Stock.symbol == q.upper()).first()
            if not stock:
                stock = Stock(
                    symbol=q.upper(),
                    name=info.get("shortName", info.get("longName", q.upper())),
                    exchange=info.get("fullExchangeName", ""),
                    currency=info.get("currency", "USD")
                )
                db.add(stock)
                db.commit()
            
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

# Portfolio endpoints (protected)
@app.post("/portfolio/holdings")
def add_portfolio_holding(
    holding: PortfolioHoldingRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Add a stock to user's portfolio"""
    # Check if holding already exists
    existing_holding = db.query(PortfolioHolding).filter(
        PortfolioHolding.user_id == current_user.id,
        PortfolioHolding.symbol == holding.symbol
    ).first()
    
    if existing_holding:
        # Update existing holding (average cost calculation)
        total_shares = existing_holding.shares + holding.shares
        total_cost = (existing_holding.shares * existing_holding.average_cost) + (holding.shares * holding.price)
        existing_holding.shares = total_shares
        existing_holding.average_cost = total_cost / total_shares
    else:
        # Create new holding
        new_holding = PortfolioHolding(
            user_id=current_user.id,
            symbol=holding.symbol,
            shares=holding.shares,
            average_cost=holding.price
        )
        db.add(new_holding)
    
    db.commit()
    return {"message": f"Added {holding.shares} shares of {holding.symbol} to portfolio"}

@app.get("/portfolio/holdings", response_model=List[PortfolioHoldingResponse])
def get_portfolio_holdings(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Get user's portfolio holdings with current values"""
    holdings = db.query(PortfolioHolding).filter(
        PortfolioHolding.user_id == current_user.id
    ).all()
    
    result = []
    for holding in holdings:
        # Get current stock price
        ticker = yf.Ticker(holding.symbol)
        current_price = ticker.info.get("regularMarketPrice", 0)
        
        total_value = holding.shares * current_price
        total_cost = holding.shares * holding.average_cost
        total_return = total_value - total_cost
        return_percent = (total_return / total_cost * 100) if total_cost > 0 else 0
        
        result.append(PortfolioHoldingResponse(
            symbol=holding.symbol,
            name=holding.stock.name if holding.stock else holding.symbol,
            shares=holding.shares,
            average_cost=holding.average_cost,
            current_price=current_price,
            total_value=total_value,
            total_return=total_return,
            return_percent=return_percent
        ))
    
    return result

@app.get("/health")
def health_check():
    return {"status": "healthy", "timestamp": datetime.utcnow()}
