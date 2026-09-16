"""Generate AI test cases for Restful-Booker from the prompt and API docs.

Reads prompts/generate-cases.md and api-docs/restful-booker.md, sends them to
the Gemini API as one message, and writes the response to
docs/01-ai-generated-cases.md.
"""

import os
import sys
from datetime import datetime
from pathlib import Path

from google import genai

PROJECT_ROOT = Path(__file__).resolve().parent.parent
PROMPT_FILE = PROJECT_ROOT / "prompts" / "generate-cases.md"
DOCS_FILE = PROJECT_ROOT / "api-docs" / "restful-booker.md"
OUTPUT_FILE = PROJECT_ROOT / "docs" / "01-ai-generated-cases.md"

DEFAULT_MODEL = "gemini-3.6-flash"


def fail(message: str) -> None:
    print(f"Error: {message}", file=sys.stderr)
    sys.exit(1)


def main() -> None:
    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key:
        fail("GEMINI_API_KEY environment variable is not set.")

    model = os.environ.get("GEMINI_MODEL", DEFAULT_MODEL)

    if OUTPUT_FILE.exists():
        fail(f"{OUTPUT_FILE} already exists. Refusing to overwrite it.")

    if not PROMPT_FILE.exists():
        fail(f"Prompt file not found: {PROMPT_FILE}")

    if not DOCS_FILE.exists():
        fail(f"API docs file not found: {DOCS_FILE}")

    prompt_text = PROMPT_FILE.read_text(encoding="utf-8")
    docs_text = DOCS_FILE.read_text(encoding="utf-8")

    if not docs_text.strip():
        fail(f"{DOCS_FILE} is empty. Paste the API documentation into it first.")

    message = f"{prompt_text}\n\n{docs_text}"

    client = genai.Client(api_key=api_key)
    response = client.models.generate_content(model=model, contents=message)

    generated_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    header = (
        f"<!--\n"
        f"Model: {model}\n"
        f"Generated: {generated_at}\n"
        f"Prompt file: {PROMPT_FILE.relative_to(PROJECT_ROOT)}\n"
        f"-->\n\n"
    )

    OUTPUT_FILE.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT_FILE.write_text(header + response.text, encoding="utf-8")
    print(f"Wrote {OUTPUT_FILE}")


if __name__ == "__main__":
    main()
