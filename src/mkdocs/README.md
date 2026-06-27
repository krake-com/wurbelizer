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
- **Colours / fonts / look:** edit [`theme/stylesheets/extra.css`](theme/stylesheets/extra.css).
- **Navigation, theme options, consent banner:** edit [`mkdocs.yml`](mkdocs.yml).
- **Legal pages:** edit [`theme/content/impressum.md`](theme/content/impressum.md)
  and [`theme/content/datenschutz.md`](theme/content/datenschutz.md). These are
  **placeholders** with `TODO` markers — fill in the operator details and have the
  text reviewed before publishing.

## GDPR notes

- Roboto is localized at build time by the Material **privacy** plugin — no Google
  Fonts requests at runtime.
- No analytics, no tracking cookies. Only `localStorage` is used (colour-scheme
  choice + consent), disclosed in the bilingual consent banner and the privacy page.
- An Impressum and a Datenschutzerklärung (both German/English placeholders) are
  linked from the navigation and the footer.

### Known residual

Material's JavaScript bundle contains a conditional dynamic import of a
`resize-observer-polyfill` from `unpkg.com`, guarded by
`typeof ResizeObserver == "undefined"`. It is only fetched by browsers that lack
`ResizeObserver` (pre-2020 browsers); every current browser ships `ResizeObserver`,
so the request is never made in practice. The privacy plugin cannot rewrite URLs
embedded inside minified JS, so this string remains in `bundle.*.min.js`. If you
must guarantee *zero* third-party URLs even in dead code paths, self-host that
polyfill via a theme override or post-process the bundle.
