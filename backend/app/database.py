import os
from sqlalchemy import create_engine, text
from sqlalchemy.orm import declarative_base
from sqlalchemy.orm import sessionmaker

# Database connection and session management

# For MVP local PostgresSQL, but can be changed via environment variable
DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "postgresql://postgres:monkey57@localhost:5432/fieldsync"
    # IMPORTANT: This is set to a local path for PostgreSQL, you must load your own password and create a local version of DB for the MVP
)

# Create SQLAlchemy engine
engine = create_engine(
    DATABASE_URL,
    # Connection pool settings for local development
    pool_pre_ping=True,
    echo=False
)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# Test database connection
def test_connection():
    try:
        # Test connection with an actual query
        with engine.connect() as connection:
            result = connection.execute(text("SELECT 1"))
            result.fetchone()
        return True
    except Exception as e:
        print(f"Database connection failed: {e}")
        return False

if __name__ == "__main__":
    print("Testing database connection...")
    result = test_connection()
    print(f"Connection successful: {result}")

