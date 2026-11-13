#!/usr/bin/env python3
"""Verify the behaviour specs still describe tests that exist.

    python3 scripts/check_specs.py

`openspec/` states what the apps must do; the Kotlin tests prove it holds. A
scenario names its test in a `*Verifies:*` line, by the test's name:

    - *Verifies:* `a short season ends after half the matchdays`,
      `GameTest.the first round deals every hand`

A name resolves against the test functions of the tracked `*Test.kt` files, in
backticks or camelCase. `ClassName.name` pins the class (a JVM method name cannot
contain a dot, so the first dot is always the separator); a bare `ClassName`
means the whole class verifies the scenario.

Two checks with different severities:

- A named test that does not exist fails: there is no judgement in it, and a spec
  claiming coverage that is not there is worse than no spec.
- A scenario with no `*Verifies:*` warns, unless it carries a GAP: whether a given
  scenario deserves a test is a judgement.
"""

import re
import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
SPECS = REPO_ROOT / "openspec" / "specs"

# A test function is the `fun` that follows an annotation containing "Test"
# (`@Test`, `@ParameterizedTest`, `@RepeatedTest`).
_FUN = re.compile(r"^\s*(?:private\s+|internal\s+)?fun\s+(?:`([^`]+)`|([A-Za-z_]\w*))\s*\(")
# Leading annotations on the line, as in `@Test fun x()` or `@Test @Config(...) fun x()`.
_ANNOTATION = re.compile(r"\s*@([\w.:]+)(?:\([^()]*\))?")


def split_annotations(line: str) -> tuple[list[str], str]:
    """The names of the annotations that open the line, and what follows them."""
    names = []
    while (match := _ANNOTATION.match(line)) is not None:
        names.append(match.group(1))
        line = line[match.end():]
    return names, line


def known_tests() -> dict[str, set[str]]:
    """Test function names per class, for every tracked `*Test.kt`."""
    files = subprocess.run(
        ["git", "ls-files", "*Test.kt"], cwd=REPO_ROOT, capture_output=True, text=True
    ).stdout.split()
    classes: dict[str, set[str]] = {}
    for path in files:
        lines = (REPO_ROOT / path).read_text().splitlines()
        names = classes.setdefault(Path(path).stem, set())
        annotated = False
        for line in lines:
            annotations, rest = split_annotations(line)
            if annotations:
                annotated = annotated or any("Test" in name for name in annotations)
                if not rest.strip():
                    continue
                line = rest
            stripped = line.strip()
            match = _FUN.match(line)
            if match and annotated:
                names.add(match.group(1) or match.group(2))
            if stripped:
                annotated = False
    return classes


def verifier_blocks(text: str) -> list[str]:
    """The text of each `*Verifies:*` block: from the marker to the next blank line."""
    blocks = []
    lines = text.splitlines()
    for i, line in enumerate(lines):
        if "*Verifies:*" not in line:
            continue
        block = [line.split("*Verifies:*", 1)[1]]
        for follow in lines[i + 1 :]:
            if not follow.strip() or follow.lstrip().startswith(("#", "- **", "> **")):
                break
            block.append(follow)
        blocks.append("\n".join(block))
    return blocks


def resolves(reference: str, classes: dict[str, set[str]]) -> bool:
    if reference in classes:
        return True
    head, dot, tail = reference.partition(".")
    if dot and head in classes:
        return tail in classes[head]
    return any(reference in names for names in classes.values())


def scenarios(text: str) -> list[tuple[str, bool]]:
    """`[(title, accounted_for)]`: a scenario is accounted for by a verifier or a GAP."""
    blocks = re.split(r"^#+\s*Scenario:\s*", text, flags=re.M)[1:]
    return [
        (b.splitlines()[0].strip(), "*Verifies:*" in b or "GAP" in b) for b in blocks
    ]


def main() -> int:
    if not SPECS.is_dir():
        print(f"No specs directory at {SPECS}", file=sys.stderr)
        return 1

    classes = known_tests()
    specs = sorted(SPECS.rglob("spec.md"))
    broken: list[str] = []
    unverified: list[str] = []
    total_scenarios = 0
    cited = 0

    for spec in specs:
        rel = spec.relative_to(SPECS).as_posix()
        text = spec.read_text()
        for block in verifier_blocks(text):
            for reference in re.findall(r"`([^`]+)`", block):
                cited += 1
                if not resolves(reference.strip(), classes):
                    broken.append(f"{rel} → {reference}")
        for title, accounted in scenarios(text):
            total_scenarios += 1
            if not accounted:
                unverified.append(f"{rel} → {title}")

    missing = [
        d.relative_to(SPECS).as_posix()
        for d in sorted(SPECS.glob("*/*"))
        if d.is_dir() and not (d / "spec.md").exists()
    ]

    if unverified:
        print(f"==> {len(unverified)} scenario(s) with no test named:")
        for line in unverified:
            print(f"      {line}")
    if missing:
        print(f"==> {len(missing)} capability directory(ies) with no spec.md:")
        for line in missing:
            print(f"      {line}")

    if broken or missing:
        if broken:
            print("\nSpecs name tests that do not exist:\n", file=sys.stderr)
            for line in broken:
                print(f"  - {line}", file=sys.stderr)
            print(
                "\nRename the reference, or point it at what replaced the test.",
                file=sys.stderr,
            )
        return 1

    tests = sum(len(names) for names in classes.values())
    print(
        f"==> specs OK ({len(specs)} specs, {total_scenarios} scenarios, {cited} test "
        f"references, every one resolves; {tests} tests in the repo)"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
