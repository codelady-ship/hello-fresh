# HelloFresh Landing Page Clone (practice project)

[![Live demo](https://img.shields.io/badge/live-demo-067a46?style=for-the-badge)](https://codelady-ship.github.io/hello-fresh/hellofresh.html)

A responsive clone of the HelloFresh meal-kit landing page, built with plain HTML, CSS and a little JavaScript to practise layout and responsive design.

> **Practice clone.** This is a learning project. It is not affiliated with, endorsed by or connected to HelloFresh. Brand names and images belong to their owners.
>
> 🇦🇿 Təlim layihəsi: HelloFresh açılış səhifəsinin HTML, CSS və JavaScript ilə hazırlanmış responsiv klonu.

![Screenshot of the landing page](docs/screenshot.png)

## Features

- Hero, "Why HelloFresh?", "What's inside each box?", recipe plans, community photos, flexible plans, FAQ and footer sections
- Sticky navigation with in-page links and a working hamburger menu on small screens
- Recipe carousel with previous/next arrow buttons (scroll-snap, swipe on touch)
- FAQ built with native `<details>` / `<summary>` (no JavaScript needed)
- Responsive layout with CSS Grid and Flexbox, tested at 390, 768, 1440 and 1920 px
- Self-hosted Open Sans font via `@font-face`
- Large images converted to WebP (hero image went from 4.9 MB to 140 KB)
- SEO basics: meta description, canonical URL, Open Graph/Twitter tags, favicon, `robots.txt`, `sitemap.xml`

## Tech stack

HTML5, CSS3 (Grid, Flexbox, media queries, `clamp()`), vanilla JavaScript.

## Run locally

No build step is needed.

```bash
git clone https://github.com/codelady-ship/hello-fresh.git
cd hello-fresh
# open hellofresh.html in a browser, or serve the folder:
npx serve .
```

## Project structure

```
hellofresh.html   main page
index.html        redirects the root URL to hellofresh.html
hellofresh.css    base styles
responsive.css    media queries
hellofresh.js     hamburger menu and recipe carousel
images/           page images (WebP for photos)
font-family/      Open Sans font files
```

## Deployment

GitHub Pages serves the `main` branch from the repository root, so every push to `main` updates the live site:
https://codelady-ship.github.io/hello-fresh/

## What I learned

- Why fixed `margin-left` values break a layout on other screen sizes, and how Grid/Flexbox with `max-width` fixes that
- `calc()` needs spaces around `+` and `-`; `clamp()` keeps fluid font sizes readable
- How `@font-face` needs a `font-family` name and a correct path before it can be used
- Building an accessible hamburger menu (`aria-expanded`, closing on link click and Escape)
- Compressing images to WebP and why image size matters for page speed
