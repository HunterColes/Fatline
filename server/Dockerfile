# Use a stable, minimal Python image
FROM python:3.11-slim-bullseye

# Set work directory
WORKDIR /app

# Copy requirements and install dependencies
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

# Copy server code
COPY . .

# Expose the internal port (chosen: 8686)
EXPOSE 8686

# Run the FastAPI server on port 8686
CMD ["uvicorn", "server:app", "--host", "0.0.0.0", "--port", "8686"]
