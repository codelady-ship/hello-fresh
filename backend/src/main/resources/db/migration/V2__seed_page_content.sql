-- Seed data taken from the landing page (hellofresh.html).
-- The four menu cards in the "Over 30+ fresh recipes every week" carousel become meal kits; their titles,
-- subtitles and images are the ones on the page. The page states prices start "from $7.49 per meal";
-- cooking times, calories and the other prices are sample values for this practice project.

INSERT INTO meal_kit (slug, title, subtitle, description, image_url, cooking_time_minutes, calories, price_per_serving, sort_order) VALUES
('meat-and-veggies', 'Meat & Veggies', 'Our most popular plan',
 'Chef-created recipes with chicken, steak, fish or plant protein and plenty of fresh vegetables.',
 'images/page4-1.jpg', 30, 650, 9.99, 1),
('veggie', 'Veggie', '& plant-based meals',
 'Meat-free recipes built around seasonal vegetables, grains and plant protein.',
 'images/page4-2.jpg', 25, 580, 8.99, 2),
('family-friendly', 'Family Friendly', 'Kid-tested recipes',
 'Quick, crowd-pleasing dinners the whole family can cook and enjoy together.',
 'images/page4-3.jpg', 20, 620, 7.49, 3),
('fit-and-wholesome', 'Fit & Wholesome', 'For a balanced lifestyle',
 'Calorie-smart recipes with lean protein and whole foods for a balanced week.',
 'images/page4-4.jpg', 25, 550, 10.49, 4);

INSERT INTO meal_kit_tag (meal_kit_id, tag)
SELECT id, 'popular' FROM meal_kit WHERE slug = 'meat-and-veggies'
UNION ALL SELECT id, 'meat' FROM meal_kit WHERE slug = 'meat-and-veggies'
UNION ALL SELECT id, 'fish' FROM meal_kit WHERE slug = 'meat-and-veggies'
UNION ALL SELECT id, 'vegetarian' FROM meal_kit WHERE slug = 'veggie'
UNION ALL SELECT id, 'plant-based' FROM meal_kit WHERE slug = 'veggie'
UNION ALL SELECT id, 'family' FROM meal_kit WHERE slug = 'family-friendly'
UNION ALL SELECT id, 'quick' FROM meal_kit WHERE slug = 'family-friendly'
UNION ALL SELECT id, 'healthy' FROM meal_kit WHERE slug = 'fit-and-wholesome'
UNION ALL SELECT id, 'low-calorie' FROM meal_kit WHERE slug = 'fit-and-wholesome';

-- "Flexible plans to meet your household's needs": people x meals per week.
-- The cheapest combination matches the page's "from $7.49 per meal".
INSERT INTO plan (people, meals_per_week, price_per_serving, shipping_fee) VALUES
(2, 3, 9.99, 10.99),
(2, 4, 9.49, 10.99),
(2, 5, 8.99, 10.99),
(4, 3, 8.49, 10.99),
(4, 4, 7.99, 10.99),
(4, 5, 7.49, 10.99);
