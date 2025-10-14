from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
import os

# Change <username>, <password>, <database> with your info from .env
DATABASE_URL = os.getenv("DATABASE_URL", "postgresql://username:password@localhost:5050/database")

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()
