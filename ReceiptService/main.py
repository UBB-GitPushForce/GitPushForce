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
        The categories will be provided in this format: category (a list of relevant keywords for this category), category ...
        A keyword may be one word or a sentence describing the category.
        You must output a single valid JSON object with the following structure:
        {
          "items": [
            {
              "name": string,
              "quantity": number,
              "price": number,
              "category": string,
              "keywords": List(string)
            }
          ],
          "total": number
        }

        Rules:
        - Output ONLY JSON. No explanations. No commentary. No text outside the JSON.
        - Output STRICT valid JSON (double quotes, no trailing commas, correct types).
        - Do not include any fields other than those defined in the JSON schema.
        - Prices, totals, and quantities must be numeric values only (no currency symbols).
        - If the receipt does not specify quantity, use 1.
        - If the receipt breaks an item into multiple lines, merge them into one coherent item.
        - If there are multiple totals (e.g., subtotal, total with tax), always choose the total that includes taxes.
        - The semantic meaning of the category always takes precedence over keyword matches. Keywords help, but only when the category meaning aligns with the actual type of the item.
        - If an item matches one of the provided categories, add it in the response, along with its keywords; Do NOT generate any additional keywords for this category and make sure to include all of the provided keywords.
        - If an item clearly does not match any provided category, you may create one new category, but:
            - Name it concisely (1–2 words).
            - Make it a general category that could reasonably include similar items, avoiding overly specific or niche categories.
            - Generate 5 relevant keywords for the category to include in the response.
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
    if not data:
        raise ValueError("The provided image is not a valid receipt image.")

    required_top = {"items", "total"}
    if not required_top.issubset(data.keys()):
        raise ValueError("Response JSON is missing 'items' or 'total' fields.")
    extra_top = set(data.keys()) - required_top
    if extra_top:
        raise ValueError(f"Unexpected top-level fields: {extra_top}")

    required_item_fields = {"name", "quantity", "price", "category", "keywords"}
    for item in data["items"]:
        missing = required_item_fields - set(item.keys())
        if missing:
            raise ValueError(f"Item is missing required fields: {missing}")
        extra = set(item.keys()) - required_item_fields
        if extra:
            raise ValueError(f"Item contains unexpected fields: {extra}")


def generate_prompt(categories: dict[str, List[str]]) -> str:
    if categories:
        parts = []
        for category, keywords in categories.items():
            keyword_str = ", ".join(keywords)
            parts.append(f"{category} ({keyword_str})")
        joined = ", ".join(parts)
        return f"Analyze the receipt image and categorize each purchased item into one of these categories: {joined}"
    else:
        return "Analyze the receipt image and categorize each purchased item into one category"


def process_receipt_photo(image_bytes: bytes, categories: dict[str, List[str]], max_retries=3, delay=2):
    _validate_image(image_bytes)
    prompt = generate_prompt(categories)

    for attempt in range(1, max_retries + 1):
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
        try:
            _validate_receipt_response(response_json)
            return response_json
        except ValueError as e:
            error_msg = str(e)
            if "not a valid receipt image" in error_msg:
                raise ValueError(error_msg)
            if attempt >= max_retries:
                raise RuntimeError(f"Failed after {max_retries} attempts: {error_msg}")
            time.sleep(delay)
