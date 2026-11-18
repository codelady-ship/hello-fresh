// ---------------------------------------------------------------------------
// API config: the Spring Boot backend in backend/ (deployed on Render).
// Set API_BASE_URL to the Render service URL after deploying. On localhost the
// page talks to `./mvnw spring-boot:run` on port 8080.
// ---------------------------------------------------------------------------
const API_BASE_URL = ['localhost', '127.0.0.1'].includes(location.hostname)
    ? 'http://localhost:8080'
    : 'https://hellofresh-api.onrender.com';
const LOAD_TIMEOUT_MS = 8000;    // after 8 s keep the static cards/prices (Render free cold starts)
const SUBMIT_TIMEOUT_MS = 45000; // a sleeping free instance can take a while to answer the first POST

// Hamburger menu toggle
const bars = document.querySelector('.bars');
const nav = document.getElementById('main-nav');

function setMenu(open) {
    nav.classList.toggle('open', open);
    bars.setAttribute('aria-expanded', String(open));
    bars.setAttribute('aria-label', open ? 'Close menu' : 'Open menu');
}

bars.addEventListener('click', () => setMenu(!nav.classList.contains('open')));
// Close the menu after a link is chosen or Escape is pressed
nav.querySelectorAll('a').forEach((link) => link.addEventListener('click', () => setMenu(false)));
document.addEventListener('keydown', (e) => { if (e.key === 'Escape') setMenu(false); });

// Recipe carousel arrows
const track = document.getElementById('recipe-track');
document.querySelectorAll('.carousel .arrow').forEach((btn) => {
    btn.addEventListener('click', () => {
        const card = track.querySelector('.card');
        const step = card ? card.getBoundingClientRect().width + 16 : track.clientWidth;
        const dir = btn.classList.contains('next') ? 1 : -1;
        const atEnd = track.scrollLeft + track.clientWidth >= track.scrollWidth - 2;
        const atStart = track.scrollLeft <= 2;
        // wrap around at both ends so the arrows always do something
        if (dir === 1 && atEnd) track.scrollTo({ left: 0 });
        else if (dir === -1 && atStart) track.scrollTo({ left: track.scrollWidth });
        else track.scrollBy({ left: dir * step });
    });
});

// ---------------------------------------------------------------------------
// Live data from the API. The static content in hellofresh.html is the
// fallback: if the API is down or slow, the page keeps the built-in cards and
// prices, so GitHub Pages never looks broken.
// ---------------------------------------------------------------------------

// Same values as the seed migration (backend/src/main/resources/db/migration), used until the API answers.
let plans = [
    { people: 2, mealsPerWeek: 3, pricePerServing: 9.99, weeklyTotal: 70.93 },
    { people: 2, mealsPerWeek: 4, pricePerServing: 9.49, weeklyTotal: 86.91 },
    { people: 2, mealsPerWeek: 5, pricePerServing: 8.99, weeklyTotal: 100.89 },
    { people: 4, mealsPerWeek: 3, pricePerServing: 8.49, weeklyTotal: 112.87 },
    { people: 4, mealsPerWeek: 4, pricePerServing: 7.99, weeklyTotal: 138.83 },
    { people: 4, mealsPerWeek: 5, pricePerServing: 7.49, weeklyTotal: 160.79 },
];

async function apiFetch(path, options = {}, timeoutMs = LOAD_TIMEOUT_MS) {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);
    try {
        return await fetch(API_BASE_URL + path, {
            ...options,
            signal: controller.signal,
            headers: { Accept: 'application/json', ...(options.headers || {}) },
        });
    } finally {
        clearTimeout(timer);
    }
}

async function getJson(path) {
    const res = await apiFetch(path);
    if (!res.ok) throw new Error(`${path} answered ${res.status}`);
    return res.json();
}

// Only allow http(s) image URLs; relative paths such as images/page4-1.jpg resolve against this page.
function safeImageUrl(value) {
    try {
        const url = new URL(value, location.href);
        return ['http:', 'https:'].includes(url.protocol) ? url.href : null;
    } catch {
        return null;
    }
}

const money = (n) => `$${Number(n).toFixed(2)}`;

// Recipe cards: same markup and classes as the static cards, built with textContent (no innerHTML).
function renderMealKits(kits) {
    const valid = kits.filter((kit) => kit && kit.title && kit.slug && safeImageUrl(kit.imageUrl));
    if (valid.length === 0) return;
    const cards = valid.map((kit) => {
        const card = document.createElement('article');
        card.className = 'card';
        card.style.backgroundImage = `url("${safeImageUrl(kit.imageUrl).replace(/["\\]/g, encodeURIComponent)}")`;
        card.title = `${kit.title}: ${kit.cookingTimeMinutes} min · ${kit.calories} kcal · `
            + `from ${money(kit.pricePerServing)} per serving`;
        const h3 = document.createElement('h3');
        h3.textContent = kit.title.toUpperCase();
        const p = document.createElement('p');
        p.textContent = (kit.subtitle || '').toUpperCase();
        card.append(h3, p);
        return card;
    });
    track.replaceChildren(...cards);
    track.dataset.source = 'api';

    const kitSelect = document.getElementById('plan-kit');
    const current = kitSelect.value;
    kitSelect.replaceChildren(...valid.map((kit) => new Option(kit.title, kit.slug)));
    if (valid.some((kit) => kit.slug === current)) kitSelect.value = current;
}

// Plan request form ---------------------------------------------------------
const form = document.getElementById('plan-form');
const peopleSelect = document.getElementById('plan-people');
const mealsSelect = document.getElementById('plan-meals');
const priceLine = document.getElementById('plan-price');
const statusLine = document.getElementById('plan-status');
const submitBtn = document.getElementById('plan-submit');

function updatePrice() {
    const plan = plans.find((p) => p.people === Number(peopleSelect.value)
        && p.mealsPerWeek === Number(mealsSelect.value));
    priceLine.textContent = plan
        ? `${money(plan.pricePerServing)} per serving · ${money(plan.weeklyTotal)} per week incl. shipping`
        : 'This combination is not available yet';
}

function renderPlans(apiPlans) {
    if (!Array.isArray(apiPlans) || apiPlans.length === 0) return;
    plans = apiPlans;
    const fill = (select, values, label) => {
        const current = Number(select.value);
        select.replaceChildren(...values.map((v) => new Option(label(v), String(v))));
        if (values.includes(current)) select.value = String(current);
    };
    const uniq = (key) => [...new Set(plans.map((p) => p[key]))].sort((a, b) => a - b);
    fill(peopleSelect, uniq('people'), (v) => `${v} people`);
    fill(mealsSelect, uniq('mealsPerWeek'), (v) => `${v} meals`);
    form.dataset.source = 'api';
    updatePrice();
}

function setStatus(message, kind) {
    statusLine.textContent = message;
    statusLine.className = kind ? `form-status ${kind}` : 'form-status';
}

function markInvalid(fields) {
    form.querySelectorAll('[aria-invalid]').forEach((el) => el.removeAttribute('aria-invalid'));
    fields.forEach((name) => form.elements[name]?.setAttribute('aria-invalid', 'true'));
}

peopleSelect.addEventListener('change', updatePrice);
mealsSelect.addEventListener('change', updatePrice);

form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(form));
    const body = {
        name: data.name.trim(),
        email: data.email.trim(),
        people: Number(data.people),
        mealsPerWeek: Number(data.mealsPerWeek),
        mealKitSlug: data.mealKitSlug || null,
    };

    // Quick check in the browser; the API validates again and is the source of truth.
    const invalid = [];
    if (!body.name) invalid.push('name');
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(body.email)) invalid.push('email');
    markInvalid(invalid);
    if (invalid.length) {
        setStatus('Please enter your name and a valid email address.', 'error');
        return;
    }

    submitBtn.disabled = true;
    setStatus('Sending your request… (the free server may need a few seconds to wake up)');
    try {
        const res = await apiFetch('/api/plan-requests', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body),
        }, SUBMIT_TIMEOUT_MS);
        const result = await res.json().catch(() => ({}));
        if (res.ok) {
            form.elements.name.value = '';
            form.elements.email.value = '';
            markInvalid([]);
            setStatus(`Thanks, ${result.name}! Request #${result.id} received: ${result.people} people, `
                + `${result.mealsPerWeek} meals a week, ${money(result.weeklyTotal)} per week.`, 'success');
        } else {
            const errors = Array.isArray(result.errors) ? result.errors : [];
            markInvalid(errors.map((e) => e.field));
            const details = errors.map((e) => e.message).join(' ');
            setStatus(details || result.detail || 'The request could not be sent. Please check the form.', 'error');
        }
    } catch {
        setStatus('We could not reach the server right now. Please try again in a minute.', 'error');
    } finally {
        submitBtn.disabled = false;
    }
});

updatePrice();

// Load live data; on any error or timeout the static content simply stays.
getJson('/api/meal-kits?size=12')
    .then((page) => renderMealKits(page.content || []))
    .catch((err) => console.info('Meal kits: using built-in content.', err.message));
getJson('/api/plans?size=50')
    .then((page) => renderPlans(page.content || []))
    .catch((err) => console.info('Plans: using built-in prices.', err.message));
