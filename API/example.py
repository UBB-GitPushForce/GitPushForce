from database import SessionLocal
from repositories.user_repository import UserRepository
from models.user import User
from passlib.hash import sha256_crypt

# Create a database session
db = SessionLocal()
repo = UserRepository(db)

# Example users to add
example_users = [
    {
        "first_name": "Alice",
        "last_name": "Johnson",
        "email": "alice@example.com",
        "hashed_password": sha256_crypt.hash("password123"),
        "phone_number": "1234567890"
    },
    {
        "first_name": "Bob",
        "last_name": "Smith",
        "email": "bob@example.com",
        "hashed_password": sha256_crypt.hash("securepass"),
        "phone_number": "2345678901"
    },
    {
        "first_name": "Charlie",
        "last_name": "Brown",
        "email": "charlie@example.com",
        "hashed_password": sha256_crypt.hash("mypassword"),
        "phone_number": "3456789012"
    }
]

# Add users to the database
for user_data in example_users:
    user = User(**user_data)
    repo.add(user)

# Retrieve and print all users
users = repo.get_all()
for user in users:
    print(f"ID: {user.id}, Name: {user.first_name} {user.last_name}, Email: {user.email}")

# Working with the other repositories is the same