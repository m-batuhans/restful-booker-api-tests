"""Regenerate api-docs/restful-booker.md from the live apidoc JSON data.

The apidoc page (https://restful-booker.herokuapp.com/apidoc/index.html) ships an HTML shell
that only says "Loading..."; the actual documentation is fetched by its JavaScript at runtime
from two JSON endpoints:

  https://restful-booker.herokuapp.com/apidoc/api_data.json     - every endpoint's docs
  https://restful-booker.herokuapp.com/apidoc/api_project.json  - project title/description

Both are plain JSON (no wrapper), so no scraping or rendering is needed. This script downloads
them and writes their content to Markdown, copying every title, description, field and example
verbatim - no summarizing, no rewording, no LLM.
"""

import json
import re
import sys
import urllib.request
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
OUTPUT_FILE = PROJECT_ROOT / "api-docs" / "restful-booker.md"

API_DATA_URL = "https://restful-booker.herokuapp.com/apidoc/api_data.json"
API_PROJECT_URL = "https://restful-booker.herokuapp.com/apidoc/api_project.json"

# The apidoc page groups endpoints as Auth, then Booking, then Ping, and within Booking
# orders them per api_project.json's "order" list. This reproduces that same reading order.
GROUP_DISPLAY_ORDER = ["Auth", "Booking", "Ping"]


def fetch_json(url):
    with urllib.request.urlopen(url) as response:
        return json.loads(response.read().decode("utf-8"))


def strip_p_tags(text):
    if not text:
        return ""
    text = re.sub(r"</p>\s*<p>", "\n", text)
    text = re.sub(r"</?p>", "", text)
    return text.strip()


def render_field_table(fields_by_group):
    lines = []
    for group_name, fields in fields_by_group.items():
        lines.append(f"**{group_name}**")
        lines.append("")
        lines.append("| Field | Type | Optional | Default value | Description |")
        lines.append("|---|---|---|---|---|")
        for field in fields:
            name = field.get("field", "")
            ftype = field.get("type", "")
            optional = "yes" if field.get("optional") else "no"
            default = field.get("defaultValue", "")
            description = strip_p_tags(field.get("description")).replace("\n", "<br>")
            lines.append(f"| {name} | {ftype} | {optional} | {default} | {description} |")
        lines.append("")
    return "\n".join(lines)


def render_examples(examples):
    lines = []
    for example in examples:
        title = example.get("title", "")
        content = example.get("content", "")
        ex_type = example.get("type") or "text"
        lines.append(f"#### {title}")
        lines.append("")
        lines.append(f"```{ex_type}")
        lines.append(content)
        lines.append("```")
        lines.append("")
    return "\n".join(lines)


def render_endpoint(endpoint):
    lines = [f"### {endpoint['group']} - {endpoint['title']}", ""]

    version = endpoint.get("version")
    if version:
        lines.append(f"Version: {version}")
        lines.append("")

    description = strip_p_tags(endpoint.get("description"))
    if description:
        lines.append(description)
        lines.append("")

    lines.append(f"**{endpoint['type'].upper()}** `{endpoint['url']}`")
    lines.append("")

    header = endpoint.get("header") or {}
    header_fields = header.get("fields")
    if header_fields:
        lines.append("#### Header")
        lines.append("")
        lines.append(render_field_table(header_fields))

    parameter = endpoint.get("parameter") or {}
    parameter_fields = parameter.get("fields")
    if parameter_fields:
        lines.append("#### Parameters")
        lines.append("")
        lines.append(render_field_table(parameter_fields))

    examples = endpoint.get("examples")
    if examples:
        lines.append("#### Request examples")
        lines.append("")
        lines.append(render_examples(examples))

    success = endpoint.get("success") or {}
    success_fields = success.get("fields")
    if success_fields:
        lines.append("#### Success fields")
        lines.append("")
        lines.append(render_field_table(success_fields))

    success_examples = success.get("examples")
    if success_examples:
        lines.append("#### Success examples")
        lines.append("")
        lines.append(render_examples(success_examples))

    error = endpoint.get("error") or {}
    error_fields = error.get("fields")
    if error_fields:
        lines.append("#### Error fields")
        lines.append("")
        lines.append(render_field_table(error_fields))

    error_examples = error.get("examples")
    if error_examples:
        lines.append("#### Error examples")
        lines.append("")
        lines.append(render_examples(error_examples))

    lines.append("---")
    lines.append("")
    return "\n".join(lines)


def ordered_endpoints(api_data, project):
    by_group = {}
    for endpoint in api_data:
        by_group.setdefault(endpoint["group"], []).append(endpoint)

    booking_order = project.get("order") or []

    def sort_key(endpoint, order_list):
        name = endpoint["name"]
        return order_list.index(name) if name in order_list else len(order_list)

    ordered = []
    groups_seen = set()
    for group in GROUP_DISPLAY_ORDER:
        if group not in by_group:
            continue
        groups_seen.add(group)
        endpoints = by_group[group]
        if group == "Booking" and booking_order:
            endpoints = sorted(endpoints, key=lambda e: sort_key(e, booking_order))
        ordered.append((group, endpoints))

    for group, endpoints in by_group.items():
        if group not in groups_seen:
            ordered.append((group, endpoints))

    return ordered


def main():
    try:
        api_data = fetch_json(API_DATA_URL)
        project = fetch_json(API_PROJECT_URL)
    except Exception as exc:
        print(f"Error: failed to download API documentation data: {exc}", file=sys.stderr)
        sys.exit(1)

    lines = [f"# {project.get('title', '')}", ""]
    description = strip_p_tags(project.get("description"))
    if description:
        lines.append(description)
        lines.append("")

    generator = project.get("generator") or {}
    lines.append(
        f"Source: {API_DATA_URL} and {API_PROJECT_URL} "
        f"(generated by {generator.get('name', 'apidoc')} {generator.get('version', '')} "
        f"on {generator.get('time', '')})"
    )
    lines.append("")

    for group, endpoints in ordered_endpoints(api_data, project):
        lines.append(f"## {group}")
        lines.append("")
        for endpoint in endpoints:
            lines.append(render_endpoint(endpoint))

    OUTPUT_FILE.write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote {OUTPUT_FILE}")


if __name__ == "__main__":
    main()
