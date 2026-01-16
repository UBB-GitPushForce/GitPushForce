# Receipt Processing AI Service 🧾

A production-grade receipt image analysis service powered by Google Gemini's multimodal capabilities. Extracts purchased items from receipt photos, categorizes them, and produces deterministic, schema-safe JSON output.

---

## Overview

The service processes JPEG receipt images using `gemini-2.5-flash` to extract and categorize line items. It emphasizes **deterministic behavior**, **strict schema validation**, and **graceful error handling** for production use.

The system leverages multimodal AI reasoning to understand receipt context, infer missing fields, merge line items, and apply semantic categorization. All outputs are validated against a strict schema before returning.

---

## Architecture

The service implements a **linear validation pipeline**:

Image Input → Image Validation → Prompt Construction → Gemini API Call
→ JSON Extraction → Schema Validation → Retry Logic → JSON Output

**Pipeline Stages:**

- **Image Validation:** Uses Pillow to verify valid JPEG format
- **Prompt Construction:** Dynamically generates prompts from category definitions
- **Multimodal Inference:** Sends image + prompt to Gemini with strict system instructions
- **JSON Extraction:** Parses response using regex (handles markdown code blocks)
- **Schema Validation:** Enforces strict structure (items array + total number)
- **Retry Logic:** Auto-retries failed validations with exponential backoff

---

## Core Components

### `process_receipt_photo(image_bytes, categories, max_retries=3, delay=2)`

Main entry point. Orchestrates the entire pipeline.

**Parameters:**
- `image_bytes` (bytes): Raw JPEG image data
- `categories` (dict[str, List[str]]): Category names mapped to keyword lists
- `max_retries` (int): Max retry attempts (default: 3)
- `delay` (int): Seconds between retries (default: 2)

**Returns:**
```json
{
  "items": [
    {
      "name": "string",
      "quantity": number,
      "price": number,
      "category": "string",
      "keywords": ["list", "of", "keywords"]
    }
  ],
  "total": number
}