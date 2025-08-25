-- Initialize Fatline database with required tables and indexes
-- This file is automatically run when the PostgreSQL container starts

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_stock_prices_symbol_timestamp ON stock_prices(symbol, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_portfolio_holdings_user_symbol ON portfolio_holdings(user_id, symbol);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Insert some sample data (optional)
-- You can remove this section if you don't want sample data

-- Sample stocks
INSERT INTO stocks (symbol, name, exchange, currency) 
VALUES 
    ('AAPL', 'Apple Inc.', 'NASDAQ', 'USD'),
    ('GOOGL', 'Alphabet Inc.', 'NASDAQ', 'USD'),
    ('MSFT', 'Microsoft Corporation', 'NASDAQ', 'USD'),
    ('TSLA', 'Tesla, Inc.', 'NASDAQ', 'USD')
ON CONFLICT (symbol) DO NOTHING;
