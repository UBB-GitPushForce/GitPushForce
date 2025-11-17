import os

from dotenv import load_dotenv
from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

# Load environment variables from .env file
load_dotenv()

# Database URL loaded from environment configuration
DATABASE_URL = os.getenv("DATABASE_URL")

# SQLAlchemy engine that manages the DB connection pool
engine = create_engine(DATABASE_URL)

# Session factory used for creating DB sessions per request
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Base class for all ORM models
Base = declarative_base()


def get_db():
    """
    Provides a database session to FastAPI dependencies.

    Yields:
        SessionLocal active database session

    Ensures:
        Session is rolled back and closed after use
    """
    db = SessionLocal()
    try:
        yield db
        db.rollback()
        db.close()
    
    except:
        db.rollback()
        db.close()
        raise
