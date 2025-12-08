import io
import json
import os
import re
import time
from typing import List

from dotenv import load_dotenv
from google import genai
from google.genai import types
from PIL import Image

load_dotenv()
API_KEY = os.getenv("API_KEY")
client = genai.Client(api_key=API_KEY)
SYSTEM_CONFIG = types.GenerateContentConfig(
    system_instruction=("""
        You are a receipt-processing assistant.
        Your task is to analyze a photo of a receipt and extract only the purchased items, then categorize each item into one of the categories provided in the user prompt.
        You must output a single valid JSON object with the following structure:
        {
          "items": [
            {
              "name": string,
              "quantity": number,
              "price": number,
              "category": string
            }
          ],
          "total": number
        }

        Rules:
        - Output ONLY JSON. No explanations. No commentary. No text outside the JSON.
        - Output STRICT valid JSON (double quotes, no trailing commas, correct types).
        - Prices, totals, and quantities must be numeric values only (no currency symbols).
        - If the receipt does not specify quantity, use 1.
        - If the receipt breaks an item into multiple lines, merge them into one coherent item.
        - If there are multiple totals (e.g., subtotal, total with tax), always choose the total that includes taxes.
        - If an item clearly does not match any provided category, you may create one new category, but:
            - Name it concisely (1–2 words).
            - Make it a general category that could reasonably include similar items, avoiding overly specific or niche categories.
            - Only create a new category if absolutely necessary; prefer mapping items to broader existing categories whenever possible.
        - If a field is missing or ambiguous, deduce it cautiously from surrounding information.
        - If there is no receipt in the provided image, return an empty JSON.
        """
    )
)


def extract_json_from_response(text: str) -> str:
    fenced = re.search(r"```(?:json)?\s*(.*?)```", text, re.DOTALL | re.IGNORECASE)
    if fenced:
        return fenced.group(1).strip()
    return text.strip()


def _validate_image(image_bytes: bytes):
    try:
        Image.open(io.BytesIO(image_bytes))
    except Exception:
        raise ValueError("The provided image is not valid.")


def _validate_receipt_response(response_json: str):
    try:
        data = json.loads(response_json)
    except json.JSONDecodeError:
        raise ValueError("Response JSON is not valid.")
    if "items" not in data or "total" not in data:
        raise ValueError("Response JSON is missing 'items' or 'total' fields.")
    for item in data["items"]:
        if "name" not in item:
            raise ValueError("Response JSON is missing 'name' field.")
        if "quantity" not in item:
            raise ValueError("Response JSON is missing 'quantity' field.")
        if "price" not in item:
            raise ValueError("Response JSON is missing 'price' field.")
        if "category" not in item:
            raise ValueError("Response JSON is missing 'category' field.")


def generate_prompt(categories: List[str]) -> str:
    if categories:
        category_titles = ", ".join([c for c in categories])
        prompt = (
            f"Analyze the receipt image and categorize each purchased item into one of these categories: "
            f"{category_titles}."
        )
    else:
        prompt = "Analyze the receipt image and categorize each purchased item into one category"
    return prompt


def process_receipt_photo(image_bytes: bytes, categories: List[str], max_retries=3, delay=2):
    _validate_image(image_bytes)
    prompt = generate_prompt(categories)

    for attempt in range(1, max_retries + 1):
        print(f"Attempt {attempt}...")
        response = client.models.generate_content(
            model="gemini-2.5-flash",
            contents=[
                types.Part.from_bytes(
                    data=image_bytes,
                    mime_type="image/jpeg",
                ),
                prompt,
            ],
            config=SYSTEM_CONFIG,
        )
        response_json = extract_json_from_response(response.text)
        if response_json == "{}":
            raise ValueError("The provided image is not a valid receipt image.")
        try:
            _validate_receipt_response(response_json)
            return response_json
        except ValueError as e:
            print(f"Validation failed: {e}")
            if attempt < max_retries:
                print(f"Retrying in {delay} seconds...")
                time.sleep(delay)
            else:
                raise RuntimeError(f"Failed after {max_retries} attempts: {e}")

