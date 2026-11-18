#!/usr/bin/env python3
"""Capture complete responses from arbitrary Moodle web-service functions."""

import argparse
import json
from pathlib import Path
import sys
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import Request, urlopen


def read_env(env_file):
    try:
        lines = env_file.read_text(encoding="utf-8").splitlines()
    except OSError as error:
        raise RuntimeError(f"Could not read .env file {env_file}: {error}") from error

    values = {}
    for line in lines:
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        if line.startswith("export "):
            line = line[len("export ") :]
        key, separator, value = line.partition("=")
        if not separator:
            continue
        value = value.strip()
        if len(value) >= 2 and value[0] == value[-1] and value[0] in {"'", '"'}:
            value = value[1:-1]
        values[key.strip()] = value
    return values


def fetch_token(base_url, username, password, service):
    data = urlencode(
        {"username": username, "password": password, "service": service}
    ).encode()
    request = Request(
        f"{base_url.rstrip('/')}/login/token.php",
        data=data,
        headers={"Content-Type": "application/x-www-form-urlencoded"},
        method="POST",
    )
    try:
        with urlopen(request, timeout=60) as response:
            result = json.loads(response.read().decode("utf-8"))
    except (HTTPError, URLError, TimeoutError, json.JSONDecodeError) as error:
        raise RuntimeError(f"Could not obtain Moodle API token: {error}") from error

    token = result.get("token") if isinstance(result, dict) else None
    if not token:
        message = result.get("error", "Moodle did not return a token")
        raise RuntimeError(f"Could not obtain Moodle API token: {message}")
    return token


def flatten_arguments(arguments):
    flattened = []
    for key, value in arguments.items():
        flattened.extend(flatten_argument(key, value))
    return flattened


def flatten_argument(name, value):
    if value is None:
        return []
    if isinstance(value, dict):
        flattened = []
        for key, nested_value in value.items():
            flattened.extend(flatten_argument(f"{name}[{key}]", nested_value))
        return flattened
    if isinstance(value, list):
        flattened = []
        for index, nested_value in enumerate(value):
            flattened.extend(flatten_argument(f"{name}[{index}]", nested_value))
        return flattened
    if isinstance(value, bool):
        value = "1" if value else "0"
    elif not isinstance(value, (str, int, float)):
        raise ValueError(f"Unsupported Moodle argument value for {name}")
    return [(name, str(value))]


def call_moodle(base_url, token, function, arguments):
    form = [
        ("wsfunction", function),
        ("moodlewsrestformat", "json"),
        ("wstoken", token),
        *flatten_arguments(arguments),
    ]
    request = Request(
        f"{base_url.rstrip('/')}/webservice/rest/server.php",
        data=urlencode(form).encode(),
        headers={"Content-Type": "application/x-www-form-urlencoded"},
        method="POST",
    )
    try:
        with urlopen(request, timeout=60) as response:
            result = json.loads(response.read().decode("utf-8"))
    except (HTTPError, URLError, TimeoutError, json.JSONDecodeError) as error:
        raise RuntimeError(f"Call to {function} failed: {error}") from error

    if isinstance(result, dict) and result.get("exception"):
        raise RuntimeError(
            f"Moodle error in {function}: {result.get('message', 'no details')}"
        )
    return result


def read_calls(calls_file):
    try:
        data = json.loads(calls_file.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        raise RuntimeError(f"Could not read calls JSON {calls_file}: {error}") from error

    calls = data.get("calls") if isinstance(data, dict) and "calls" in data else data
    if isinstance(calls, dict):
        calls = [calls]
    if not isinstance(calls, list) or not calls:
        raise RuntimeError("Calls JSON must contain a non-empty list of calls")

    for index, call in enumerate(calls):
        if (
            not isinstance(call, dict)
            or not isinstance(call.get("function"), str)
            or not call["function"]
            or not isinstance(call.get("arguments", {}), dict)
        ):
            raise RuntimeError(
                f"Call at index {index} must have a function and an arguments object"
            )
    return calls


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    source = parser.add_mutually_exclusive_group(required=True)
    source.add_argument("--calls", type=Path, help="JSON file describing one or more calls")
    source.add_argument("--function", help="Moodle web-service function for a single call")
    parser.add_argument(
        "--arguments",
        default="{}",
        help="JSON object of arguments for --function (default: {})",
    )
    parser.add_argument("--base-url", help="Override MOODLE_HOST from .env")
    parser.add_argument(
        "--env-file",
        type=Path,
        default=Path(__file__).resolve().parents[1] / ".env",
        help="Path to .env (default: repository root .env)",
    )
    parser.add_argument(
        "--output", required=True, type=Path, help="Path for the full JSON output"
    )
    args = parser.parse_args()

    try:
        env = read_env(args.env_file)
        username = env.get("MOODLE_USERNAME") or env.get("MOODLE_USER")
        password = env.get("MOODLE_PASSWORD")
        base_url = args.base_url or env.get("MOODLE_HOST")
        service = env.get("MOODLE_SERVICE", "moodle_mobile_app")
        if not username or not password or not base_url:
            raise RuntimeError(
                f"MOODLE_HOST, MOODLE_USERNAME, and MOODLE_PASSWORD are required in {args.env_file}"
            )
        if args.calls:
            calls = read_calls(args.calls)
        else:
            arguments = json.loads(args.arguments)
            if not isinstance(arguments, dict):
                raise RuntimeError("--arguments must be a JSON object")
            calls = [{"function": args.function, "arguments": arguments}]
        token = fetch_token(base_url, username, password, service)

        results = []
        for call in calls:
            call_arguments = call.get("arguments", {})
            response = call_moodle(
                base_url, token, call["function"], call_arguments
            )
            results.append(
                {
                    "function": call["function"],
                    "arguments": call_arguments,
                    "response": response,
                }
            )
    except (RuntimeError, ValueError, json.JSONDecodeError) as error:
        sys.exit(str(error))

    output_path = args.output.resolve()
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(
        json.dumps({"calls": results}, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    print(f"Full capture written to: {output_path}")


if __name__ == "__main__":
    main()
