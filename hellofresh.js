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
