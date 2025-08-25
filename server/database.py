"""
Database models and configuration for Fatline server
Handles PostgreSQL connection with robust retry logic and proper relationships
"""
from sqlalchemy import create_engine, Column, Integer, String, Float, DateTime, Boolean, ForeignKey, text
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, relationship
from sqlalchemy.sql import func
import os
import time
import logging

DATABASE_URL = os.getenv("DATABASE_URL", "postgresql://fatline:fatline@localhost:5432/fatline")

# Create engine with connection pooling and retry logic
engine = create_engine(
    DATABASE_URL,
    pool_pre_ping=True,
    pool_recycle=300,
    echo=False  # Set to True for SQL debugging
)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()


class User(Base):
    __tablename__ = "users"
    
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    is_active = Column(Boolean, default=True)
    
    # Relationships
    portfolio_holdings = relationship("PortfolioHolding", back_populates="user")


class Stock(Base):
    __tablename__ = "stocks"
    
    symbol = Column(String, primary_key=True, index=True)
    name = Column(String, nullable=False)
    exchange = Column(String)
    currency = Column(String, default="USD")
    last_updated = Column(DateTime(timezone=True), server_default=func.now())


class StockPrice(Base):
    __tablename__ = "stock_prices"
    
    id = Column(Integer, primary_key=True, index=True)
    symbol = Column(String, ForeignKey("stocks.symbol"), nullable=False)
    timestamp = Column(DateTime(timezone=True), nullable=False)
    open_price = Column(Float)
    high_price = Column(Float)
    low_price = Column(Float)
    close_price = Column(Float)
    volume = Column(Integer)
    
    # Relationships
    stock = relationship("Stock")


class PortfolioHolding(Base):
    __tablename__ = "portfolio_holdings"
    
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    symbol = Column(String, ForeignKey("stocks.symbol"), nullable=False)
    shares = Column(Float, nullable=False)
    average_cost = Column(Float, nullable=False)
    added_at = Column(DateTime(timezone=True), server_default=func.now())
    
    # Relationships
    user = relationship("User", back_populates="portfolio_holdings")
    stock = relationship("Stock")


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def create_tables():
    """Create database tables with retry logic"""
    max_retries = 30
    retry_interval = 2
    
    for attempt in range(max_retries):
        try:
            # Test connection first
            with engine.connect() as connection:
                connection.execute(text("SELECT 1"))
            
            # Create all tables
            Base.metadata.create_all(bind=engine)
            logging.info("Database tables created successfully!")
            return
            
        except Exception as e:
            if attempt < max_retries - 1:
                logging.warning(f"Database connection attempt {attempt + 1} failed: {e}")
                logging.info(f"Retrying in {retry_interval} seconds...")
                time.sleep(retry_interval)
            else:
                logging.error(f"Failed to connect to database after {max_retries} attempts: {e}")
                raise
