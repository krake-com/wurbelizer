# Wurbelizer documentation website

This directory builds the public documentation site for the Wurbelizer with
[MkDocs](https://www.mkdocs.org/) and the
[Material](https://squidfunk.github.io/mkdocs-material/) theme. It is styled to
mimic [wurbelizer.org](https://wurbelizer.org) and is built to be GDPR-compliant
(no third-party requests, no tracking).

## How it works

The page content is **not** stored here. It is generated from the repository's
real documentation by [`prepare.py`](prepare.py):

- the project [`README.md`](../../README.md) becomes the site home page (`index.md`),
- the pages under [`../docs`](../docs) become the documentation pages,
- the brand stylesheet, favicon and the bilingual legal pages under
  [`theme/`](theme) are copied in.

The generated `docs/` and the built `site/` directories are **git-ignored** — they
are produced on demand. The sources under `../docs` and the repo `README.md` are
never modified.

```
prepare.py  ──>  docs/   ──(mkdocs)──>  site/
   ^                ^                      ^
 staging        generated             static HTML
```

## Prerequisites

- Python 3.9+
- The build dependencies:

```bash
make install        # = pip install -r requirements.txt
```

> `mkdocs-material`'s privacy plugin downloads the Roboto web font once at build
> time and serves it locally, so the published site makes no requests to Google.
> The **first** build therefore needs network access. On a fully offline host,
> set `font: false` in [`mkdocs.yml`](mkdocs.yml) to fall back to system fonts.

## Build & preview

```bash
make serve          # live preview at http://127.0.0.1:8000
make build          # production build into ./site (strict: fails on broken links)
make clean          # remove generated docs/, site/ and caches
```

Both `serve` and `build` run `prepare.py` first, so edits to `../docs`, the repo
`README.md` or anything under `theme/` are always picked up.

## Editing

- **Page content:** edit the Markdown under `../docs` (or the repo `README.md`).
  Do not edit `docs/` here — it is overwritten on every build.
- **Colors / fonts / look:** edit [`theme/stylesheets/extra.css`](theme/stylesheets/extra.css).
- **Navigation, theme options, consent banner:** edit [`mkdocs.yml`](mkdocs.yml).
- **Legal pages:** edit [`theme/content/impressum.md`](theme/content/impressum.md)
  and [`theme/content/datenschutz.md`](theme/content/datenschutz.md). These are
  **placeholders** with `TODO` markers — fill in the operator details and have the
  text reviewed before publishing.

## GDPR notes

- Roboto is localized at build time by the Material **privacy** plugin — no Google
  Fonts requests at runtime.
- No analytics, no tracking cookies. Only `localStorage` is used (color-scheme
  choice + consent), disclosed in the bilingual consent banner and the privacy page.
- The **ResizeObserver polyfill is self-hosted** (see below) — no `unpkg.com`
  request is ever made.
- An Impressum and a Datenschutzerklärung (both German/English placeholders) are
  linked from the navigation and the footer.

### Self-hosted ResizeObserver polyfill

Material's JavaScript bundle would, on browsers that lack a native
`ResizeObserver` (pre-2020 browsers), inject `<script src="https://unpkg.com/
resize-observer-polyfill">`. To avoid that third-party request, a copy of the
polyfill is vendored in [`theme/vendor/`](theme/vendor) and served from this
site's own origin. The theme override [`overrides/main.html`](overrides/main.html)
loads it in `<head>`, **only when `ResizeObserver` is missing**, synchronously, so
the global is defined before Material's bundle runs — Material therefore never
reaches its `unpkg.com` fallback. Native implementations are never overwritten and
modern browsers download nothing extra.

> The literal string `https://unpkg.com/...` still exists as an **unreachable
> dead-code branch** inside Material's prebuilt `bundle.*.min.js` (and its source
> map). It is never executed and never requested. Removing the string itself would
> require post-processing Material's compiled bundle, which this setup deliberately
> avoids so that theme upgrades stay clean.
