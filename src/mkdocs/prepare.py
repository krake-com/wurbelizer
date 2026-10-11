#!/usr/bin/env python3
"""Stage the Wurbelizer documentation into ./docs for the MkDocs build.

The page content lives in the repository's real docs (../docs) and README; this
script assembles a clean MkDocs `docs_dir` from them without ever modifying the
sources, so Bitbucket rendering keeps working. Run it via `make prepare`
(automatically invoked by `make serve` / `make build`).

Standard library only — no third-party dependencies.
"""

from __future__ import annotations

import re
import shutil
from pathlib import Path

HERE = Path(__file__).resolve().parent          # src/mkdocs
SRC_DOCS = (HERE / ".." / "docs").resolve()      # src/docs
REPO_README = (HERE / ".." / ".." / "README.md").resolve()
THEME = HERE / "theme"
OUT = HERE / "docs"                              # generated docs_dir (git-ignored)

# Content pages copied verbatim (minus their Bitbucket nav footer), in any order.
# toc.md and index.md are intentionally excluded — Material provides nav + search.
CONTENT_PAGES = [
    "quickstart.md",
    "code-levels.md",
    "integration.md",
    "maven.md",
    "ant.md",
    "console.md",
    "wurblet-syntax.md",
    "source-syntax.md",
    "guard-types.md",
    "writing-wurblets.md",
]

# Matches the trailing "---\n\n**Navigation:** ..." footer to the end of the file.
NAV_FOOTER = re.compile(r"\n+---\s*\n+\*\*Navigation:\*\*.*$", re.S)


def strip_nav_footer(text: str) -> str:
    """Remove the Bitbucket prev/next navigation footer (Material adds its own)."""
    return NAV_FOOTER.sub("\n", text)


def readme_to_index(text: str) -> str:
    """Turn the repo README into the site home page (index.md).

    - the README's leading logo image is promoted to the page's H1, so it becomes
      the title/hero (and MkDocs does not inject the nav title "Home" as a heading);
    - drop helper paragraphs that point at the excluded toc.md / index.md pages;
    - drop the Maven Central version badge (a third-party image that would be
      frozen at build time);
    - rewrite ``./src/docs/`` links so they resolve inside the site.
    """
    # Drop whole paragraphs that reference the excluded helper pages, and the
    # version badge: it is a third-party image that the privacy plugin would
    # freeze at build time, so it would show a stale version.
    paragraphs = re.split(r"\n\s*\n", text)
    paragraphs = [p for p in paragraphs
                  if "toc.md" not in p and "index.md" not in p
                  and "img.shields.io" not in p]

    # Drop now-empty sections: a heading immediately followed by another heading
    # or the end of the document (e.g. "## Contents" once its helper text is gone).
    kept: list[str] = []
    for i, para in enumerate(paragraphs):
        if para.lstrip().startswith("#"):
            nxt = paragraphs[i + 1] if i + 1 < len(paragraphs) else None
            if nxt is None or nxt.lstrip().startswith("#"):
                continue
        kept.append(para)
    text = "\n\n".join(kept)

    # Rewrite repo-relative doc links to site-relative ones.
    #   ./src/docs/assets/x.png -> assets/x.png
    #   ./src/docs/quickstart.md -> quickstart.md
    text = text.replace("./src/docs/", "")

    # Promote the leading logo image to the page H1 (the title), so MkDocs uses it
    # as the heading instead of injecting the nav title "Home" above the content.
    text = re.sub(r"^(!\[[^\]]*\]\([^)]*\))", r"# \1", text.lstrip(), count=1)

    return text.rstrip() + "\n"


def main() -> None:
    if OUT.exists():
        shutil.rmtree(OUT)
    OUT.mkdir(parents=True)

    # 1. Content pages (strip the Bitbucket nav footer).
    for name in CONTENT_PAGES:
        src = SRC_DOCS / name
        if not src.exists():
            raise SystemExit(f"missing source page: {src}")
        (OUT / name).write_text(strip_nav_footer(src.read_text(encoding="utf-8")),
                                encoding="utf-8")

    # 2. Home page from the repo README.
    (OUT / "index.md").write_text(
        readme_to_index(REPO_README.read_text(encoding="utf-8")), encoding="utf-8")

    # 3. Images (and the white header logo) from ../docs/assets.
    shutil.copytree(SRC_DOCS / "assets", OUT / "assets")
    # ... plus the favicon (an MkDocs-only brand asset).
    shutil.copy2(THEME / "assets" / "favicon.ico", OUT / "assets" / "favicon.ico")
    # ... plus the self-hosted ResizeObserver polyfill loaded by overrides/main.html,
    # so the site never requests it from a third party (unpkg).
    js_dir = OUT / "assets" / "javascripts"
    js_dir.mkdir(parents=True, exist_ok=True)
    shutil.copy2(THEME / "vendor" / "resize-observer-polyfill.js",
                 js_dir / "resize-observer-polyfill.js")

    # 4. Brand stylesheet.
    (OUT / "stylesheets").mkdir()
    shutil.copy2(THEME / "stylesheets" / "extra.css", OUT / "stylesheets" / "extra.css")

    # 5. Bilingual legal placeholder pages.
    for name in ("impressum.md", "datenschutz.md"):
        shutil.copy2(THEME / "content" / name, OUT / name)

    pages = len(CONTENT_PAGES) + 3  # content + index + 2 legal
    print(f"staged {pages} pages + assets into {OUT.relative_to(HERE)}/")


if __name__ == "__main__":
    main()
