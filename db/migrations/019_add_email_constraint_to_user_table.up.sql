ALTER TABLE users 
ADD CONSTRAINT ck_email CHECK (email ~'^[^\s@]+@[^\s@]+\.[^\s@]+$')