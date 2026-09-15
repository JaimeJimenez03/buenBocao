-- =============================================================================
-- BUEN BOCAO — SQL DE PRODUCCIÓN
-- Base de datos: MySQL 8+
-- Ejecutar después de que Hibernate cree las tablas (ddl-auto: validate en prod)
-- O usar este script como init para crear todo desde cero (ddl-auto: none)
-- =============================================================================

-- ===================== CATEGORÍAS =====================
INSERT IGNORE INTO categories (id, name, slug) VALUES
(1, 'Entrantes', 'entrantes'),
(2, 'Sopas y Cremas', 'sopas-y-cremas'),
(3, 'Ensaladas', 'ensaladas'),
(4, 'Arroces', 'arroces'),
(5, 'Pastas', 'pastas'),
(6, 'Carnes', 'carnes'),
(7, 'Pescados y Mariscos', 'pescados-y-mariscos'),
(8, 'Guisos y Estofados', 'guisos-y-estofados'),
(9, 'Verduras y Hortalizas', 'verduras-y-hortalizas'),
(10, 'Legumbres', 'legumbres'),
(11, 'Huevos', 'huevos'),
(12, 'Pizzas y Empanadas', 'pizzas-y-empanadas'),
(13, 'Sándwiches y Bocadillos', 'sandwiches-y-bocadillos'),
(14, 'Salsas y Condimentos', 'salsas-y-condimentos'),
(15, 'Pan y Bollería', 'pan-y-bolleria'),
(16, 'Postres', 'postres'),
(17, 'Tartas y Bizcochos', 'tartas-y-bizcochos'),
(18, 'Galletas', 'galletas'),
(19, 'Bebidas', 'bebidas'),
(20, 'Smoothies y Batidos', 'smoothies-y-batidos'),
(21, 'Desayunos y Brunch', 'desayunos-y-brunch'),
(22, 'Tapas', 'tapas'),
(23, 'Comida Internacional', 'comida-internacional'),
(24, 'Comida Rápida Casera', 'comida-rapida-casera'),
(25, 'Conservas y Fermentados', 'conservas-y-fermentados');

-- ===================== TAGS =====================
INSERT IGNORE INTO tags (id, name, slug) VALUES
(1, 'Rápido', 'rapido'),
(2, 'Fácil', 'facil'),
(3, 'Económico', 'economico'),
(4, 'Sin horno', 'sin-horno'),
(5, 'Al horno', 'al-horno'),
(6, 'A la plancha', 'a-la-plancha'),
(7, 'Fritura', 'fritura'),
(8, 'Slow cooking', 'slow-cooking'),
(9, 'Batch cooking', 'batch-cooking'),
(10, 'Meal prep', 'meal-prep'),
(11, 'Para niños', 'para-ninos'),
(12, 'Comfort food', 'comfort-food'),
(13, 'Saludable', 'saludable'),
(14, 'Proteico', 'proteico'),
(15, 'Bajo en calorías', 'bajo-en-calorias'),
(16, 'Alto en fibra', 'alto-en-fibra'),
(17, 'Navidad', 'navidad'),
(18, 'Verano', 'verano'),
(19, 'Otoño', 'otono'),
(20, 'San Valentín', 'san-valentin'),
(21, 'Fiesta', 'fiesta'),
(22, 'Picnic', 'picnic'),
(23, 'Barbacoa', 'barbacoa'),
(24, 'Thermomix', 'thermomix'),
(25, 'Air Fryer', 'air-fryer'),
(26, 'Un solo plato', 'un-solo-plato'),
(27, 'Menos de 30 min', 'menos-de-30-min'),
(28, 'Sin azúcar añadido', 'sin-azucar-anadido'),
(29, 'Receta de la abuela', 'receta-de-la-abuela'),
(30, 'Gourmet', 'gourmet');

-- ===================== DIETAS =====================
INSERT IGNORE INTO diets (id, name, slug) VALUES
(1, 'Vegano', 'vegano'),
(2, 'Vegetariano', 'vegetariano'),
(3, 'Sin gluten', 'sin-gluten'),
(4, 'Sin lactosa', 'sin-lactosa'),
(5, 'Keto', 'keto'),
(6, 'Paleo', 'paleo'),
(7, 'Bajo en carbohidratos', 'bajo-en-carbohidratos'),
(8, 'Sin frutos secos', 'sin-frutos-secos'),
(9, 'Sin huevo', 'sin-huevo'),
(10, 'Sin soja', 'sin-soja'),
(11, 'Kosher', 'kosher'),
(12, 'Halal', 'halal'),
(13, 'FODMAP', 'fodmap'),
(14, 'Mediterránea', 'mediterranea'),
(15, 'Whole30', 'whole30');

-- ===================== INGREDIENTES BASE =====================
INSERT IGNORE INTO ingredients (id, name, slug) VALUES
-- Verduras y hortalizas
(1, 'Tomate', 'tomate'),
(2, 'Cebolla', 'cebolla'),
(3, 'Ajo', 'ajo'),
(4, 'Pimiento rojo', 'pimiento-rojo'),
(5, 'Pimiento verde', 'pimiento-verde'),
(6, 'Zanahoria', 'zanahoria'),
(7, 'Patata', 'patata'),
(8, 'Calabacín', 'calabacin'),
(9, 'Berenjena', 'berenjena'),
(10, 'Espinacas', 'espinacas'),
(11, 'Lechuga', 'lechuga'),
(12, 'Brócoli', 'brocoli'),
(13, 'Champiñones', 'champinones'),
(14, 'Pepino', 'pepino'),
(15, 'Aguacate', 'aguacate'),
-- Frutas
(16, 'Limón', 'limon'),
(17, 'Naranja', 'naranja'),
(18, 'Plátano', 'platano'),
(19, 'Manzana', 'manzana'),
(20, 'Fresa', 'fresa'),
-- Proteínas
(21, 'Pechuga de pollo', 'pechuga-de-pollo'),
(22, 'Carne picada de ternera', 'carne-picada-de-ternera'),
(23, 'Lomo de cerdo', 'lomo-de-cerdo'),
(24, 'Salmón', 'salmon'),
(25, 'Bacalao', 'bacalao'),
(26, 'Gambas', 'gambas'),
(27, 'Atún en lata', 'atun-en-lata'),
(28, 'Huevo', 'huevo'),
(29, 'Tofu', 'tofu'),
-- Lácteos
(30, 'Leche', 'leche'),
(31, 'Nata para cocinar', 'nata-para-cocinar'),
(32, 'Queso rallado', 'queso-rallado'),
(33, 'Queso cremoso', 'queso-cremoso'),
(34, 'Yogur natural', 'yogur-natural'),
(35, 'Mantequilla', 'mantequilla'),
-- Cereales y legumbres
(36, 'Arroz', 'arroz'),
(37, 'Pasta (espaguetis)', 'pasta-espaguetis'),
(38, 'Pasta (macarrones)', 'pasta-macarrones'),
(39, 'Pan rallado', 'pan-rallado'),
(40, 'Harina de trigo', 'harina-de-trigo'),
(41, 'Garbanzos', 'garbanzos'),
(42, 'Lentejas', 'lentejas'),
(43, 'Alubias', 'alubias'),
-- Aceites y condimentos
(44, 'Aceite de oliva virgen extra', 'aceite-de-oliva-virgen-extra'),
(45, 'Sal', 'sal'),
(46, 'Pimienta negra', 'pimienta-negra'),
(47, 'Pimentón', 'pimenton'),
(48, 'Comino', 'comino'),
(49, 'Orégano', 'oregano'),
(50, 'Perejil', 'perejil'),
(51, 'Albahaca', 'albahaca'),
(52, 'Azúcar', 'azucar'),
(53, 'Vinagre', 'vinagre'),
(54, 'Salsa de soja', 'salsa-de-soja'),
(55, 'Caldo de pollo', 'caldo-de-pollo'),
(56, 'Tomate frito', 'tomate-frito'),
(57, 'Levadura', 'levadura'),
(58, 'Chocolate negro', 'chocolate-negro'),
(59, 'Canela', 'canela'),
(60, 'Vainilla', 'vainilla');

-- =============================================================================
-- BUEN BOCAO — BASE DE DATOS DE PRUEBA (TEST DATA)
-- Incluye: usuarios, recetas completas con pasos e ingredientes, carritos
-- Password de todos los usuarios de prueba: Test1234!
-- bcrypt hash de "Test1234!": $2a$10$TkIhkZKPCsfoWFHKXHMGJeNNGdj0r5G2VKNjQqSGIxC9x5MrSgCmy
-- =============================================================================

-- ===================== USUARIOS DE PRUEBA =====================
-- Password: Test1234!
INSERT IGNORE INTO users (id, username, email, password, first_name, last_name, bio, role, enabled, email_verified, created_at, updated_at) VALUES
(2, 'maria_cocina', 'maria@test.com', '$2a$10$K1n0U73Re8xrTOdqhBvAJeoNMPegvHoELmEU6Rzjvl0Iouo6GAQ/.', 'María', 'García', 'Apasionada de la cocina mediterránea y la repostería. ¡El olor a pan recién horneado es mi favorito!', 'USER', true, true, NOW(), NOW()),
(3, 'chef_pedro', 'pedro@test.com', '$2a$10$K1n0U73Re8xrTOdqhBvAJeoNMPegvHoELmEU6Rzjvl0Iouo6GAQ/.', 'Pedro', 'Martínez', 'Chef profesional con 15 años de experiencia. Me encanta compartir recetas tradicionales con un toque moderno.', 'USER', true, true, NOW(), NOW()),
(4, 'veggie_ana', 'ana@test.com', '$2a$10$K1n0U73Re8xrTOdqhBvAJeoNMPegvHoELmEU6Rzjvl0Iouo6GAQ/.', 'Ana', 'López', 'Vegana desde 2020. Demuestro que comer sin carne puede ser delicioso y variado.', 'USER', true, true, NOW(), NOW()),
(5, 'carlos_bbq', 'carlos@test.com', '$2a$10$K1n0U73Re8xrTOdqhBvAJeoNMPegvHoELmEU6Rzjvl0Iouo6GAQ/.', 'Carlos', 'Ruiz', 'Fan número uno de las barbacoas y las recetas con mucha proteína. Gym & Grill!', 'USER', true, true, NOW(), NOW()),
(6, 'dulce_laura', 'laura@test.com', '$2a$10$K1n0U73Re8xrTOdqhBvAJeoNMPegvHoELmEU6Rzjvl0Iouo6GAQ/.', 'Laura', 'Sánchez', 'Repostera amateur. Los domingos son para hornear. ¡Sígueme y te comparto mis secretos!', 'USER', true, true, NOW(), NOW());

-- ===================== SETTINGS USUARIOS DE PRUEBA =====================
INSERT IGNORE INTO user_settings (user_id, theme, language, notify_new_follower, notify_new_review, notify_new_favorite, notify_events, notify_email, measurement_system, public_profile, show_activity) VALUES
(2, 'light', 'es', true, true, true, true, true, 'metric', true, true),
(3, 'dark', 'es', true, true, false, true, true, 'metric', true, true),
(4, 'system', 'es', true, false, true, true, false, 'metric', true, true),
(5, 'light', 'es', false, true, true, false, true, 'metric', true, false),
(6, 'system', 'es', true, true, true, true, true, 'metric', true, true);

-- ===================== PREFERENCIAS DIETÉTICAS USUARIOS =====================
INSERT IGNORE INTO user_dietary_preferences (user_id, diet_id) VALUES
(4, 1), -- Ana: Vegano
(4, 2), -- Ana: Vegetariano
(4, 3), -- Ana: Sin gluten
(6, 4); -- Laura: Sin lactosa

-- ===================== FOLLOWS =====================
INSERT IGNORE INTO follows (follower_id, followed_id, created_at) VALUES
(2, 3, NOW()),
(2, 4, NOW()),
(3, 2, NOW()),
(4, 2, NOW()),
(4, 3, NOW()),
(5, 3, NOW()),
(6, 2, NOW()),
(6, 4, NOW());

-- =============================================================================
-- RECETAS
-- =============================================================================

-- ===================== RECETA 1: GAZPACHO ANDALUZ =====================
INSERT IGNORE INTO recipes (id, title, description, slug, difficulty, servings, prep_time_minutes, status, author_id, category_id, created_at, updated_at) VALUES
(1, 'Gazpacho andaluz tradicional',
 'El gazpacho más fresco y auténtico que hayas probado. Receta de mi abuela sevillana, sin trucos ni atajos. Perfecto para el verano.',
 'gazpacho-andaluz-tradicional', 'EASY', 4, 20, 'PUBLISHED', 2, 2, NOW(), NOW());

INSERT IGNORE INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit, notes, sort_order) VALUES
(1, 1,  1000, 'GRAMS', 'tomates maduros, tipo pera', 0),
(1, 14,  1,   'PIECE', 'pelado', 1),
(1, 4,   1,   'PIECE', 'sin semillas', 2),
(1, 5,   0.5, 'PIECE', 'sin semillas', 3),
(1, 3,   2,   'PIECE', 'dientes', 4),
(1, 44,  80,  'MILLILITERS', 'virgen extra, de calidad', 5),
(1, 53,  30,  'MILLILITERS', 'de Jerez', 6),
(1, 45,  1,   'TEASPOON', null, 7);

INSERT IGNORE INTO recipe_steps (recipe_id, step_number, description, image_url) VALUES
(1, 1, 'Lava bien todos los vegetales. Trocea los tomates en cuartos y el pepino en rodajas. Quita las semillas al pimiento rojo y verde y córtalos en tiras.', null),
(1, 2, 'Pela los dientes de ajo y añádelos al vaso de la batidora junto con todos los vegetales troceados.', null),
(1, 3, 'Añade el aceite de oliva, el vinagre de Jerez y la sal. Tritura a máxima potencia durante 2 minutos hasta obtener una crema muy fina.', null),
(1, 4, 'Cuela el gazpacho por un colador fino presionando con una cuchara para extraer todo el líquido. Desecha los sólidos.', null),
(1, 5, 'Prueba y ajusta de sal y vinagre. Si queda muy espeso, añade un poco de agua fría. Refrigera al menos 2 horas antes de servir.', null),
(1, 6, 'Sirve bien frío con una guarnición de daditos de tomate, pepino y pimiento, y un hilo de aceite de oliva.', null);

INSERT IGNORE INTO recipe_tags (recipe_id, tag_id) VALUES
(1, 1),  -- Rápido
(1, 2),  -- Fácil
(1, 3),  -- Económico
(1, 4),  -- Sin horno
(1, 18), -- Verano
(1, 15), -- Bajo en calorías
(1, 29); -- Receta de la abuela

INSERT IGNORE INTO recipe_diets (recipe_id, diet_id) VALUES
(1, 1),  -- Vegano
(1, 2),  -- Vegetariano
(1, 3),  -- Sin gluten
(1, 4),  -- Sin lactosa
(1, 14); -- Mediterránea

-- ===================== RECETA 2: PAELLA VALENCIANA =====================
INSERT IGNORE INTO recipes (id, title, description, slug, difficulty, servings, prep_time_minutes, status, author_id, category_id, created_at, updated_at) VALUES
(2, 'Paella valenciana auténtica',
 'La auténtica paella valenciana con pollo, conejo y judías verdes. Sin trucos, sin colorante, solo azafrán de verdad y mucho amor.',
 'paella-valenciana-autentica', 'HARD', 6, 60, 'PUBLISHED', 3, 4, NOW(), NOW());

INSERT IGNORE INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit, notes, sort_order) VALUES
(2, 36,  600, 'GRAMS', 'arroz bomba', 0),
(2, 21,  500, 'GRAMS', 'troceada', 1),
(2, 4,   2,   'PIECE', 'en tiras', 2),
(2, 1,   3,   'PIECE', 'rallados', 3),
(2, 3,   4,   'PIECE', 'dientes', 4),
(2, 55,  1.5, 'LITERS', 'caliente', 5),
(2, 44,  100, 'MILLILITERS', null, 6),
(2, 47,  1,   'TEASPOON', 'dulce', 7),
(2, 45,  1,   'TABLESPOON', null, 8);

INSERT IGNORE INTO recipe_steps (recipe_id, step_number, description, image_url) VALUES
(2, 1, 'Calienta el aceite en la paellera a fuego medio-alto. Sazona el pollo y dóralo bien por todos los lados durante 10 minutos. Reserva.', null),
(2, 2, 'En el mismo aceite, sofríe el pimiento rojo cortado en tiras durante 5 minutos. Añade el ajo picado y sofríe 1 minuto más.', null),
(2, 3, 'Añade el tomate rallado y el pimentón. Sofríe a fuego bajo durante 5 minutos hasta que el tomate pierda el agua.', null),
(2, 4, 'Reincorpora el pollo y añade el caldo caliente con el azafrán disuelto. Ajusta de sal. Lleva a ebullición.', null),
(2, 5, 'Añade el arroz distribuyéndolo uniformemente. Cocina a fuego alto 5 minutos, luego baja a fuego medio-bajo otros 13 minutos. NO remuevas.', null),
(2, 6, 'Sube el fuego al máximo durante 1-2 minutos para conseguir el socarrat (capa tostada del fondo). Retira del fuego y deja reposar 5 minutos tapado con papel de periódico.', null);

INSERT IGNORE INTO recipe_tags (recipe_id, tag_id) VALUES
(2, 5),  -- Al horno (plancha)
(2, 12), -- Comfort food
(2, 21), -- Fiesta
(2, 30), -- Gourmet
(2, 29); -- Receta de la abuela

INSERT IGNORE INTO recipe_diets (recipe_id, diet_id) VALUES
(2, 3),  -- Sin gluten
(2, 14); -- Mediterránea

-- ===================== RECETA 3: TORTILLA DE PATATAS =====================
INSERT IGNORE INTO recipes (id, title, description, slug, difficulty, servings, prep_time_minutes, status, author_id, category_id, created_at, updated_at) VALUES
(3, 'Tortilla de patatas jugosa',
 'La tortilla de patatas perfecta: jugosa por dentro, dorada por fuera. El secreto está en el tiempo de reposo y no escatimar en aceite.',
 'tortilla-de-patatas-jugosa', 'MEDIUM', 4, 40, 'PUBLISHED', 2, 11, NOW(), NOW());

INSERT IGNORE INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit, notes, sort_order) VALUES
(3, 7,   800, 'GRAMS', 'patatas medianas', 0),
(3, 28,  6,   'PIECE', 'huevos L', 1),
(3, 2,   1,   'PIECE', 'grande, opcional', 2),
(3, 44,  300, 'MILLILITERS', 'para freír', 3),
(3, 45,  1,   'TEASPOON', null, 4);

INSERT IGNORE INTO recipe_steps (recipe_id, step_number, description, image_url) VALUES
(3, 1, 'Pela y lava las patatas. Córtalas en láminas finas de unos 3mm. Pela y corta la cebolla en juliana fina.', null),
(3, 2, 'Calienta abundante aceite en una sartén honda. Fríe las patatas y la cebolla a fuego medio-bajo durante 20 minutos, removiendo ocasionalmente. Deben quedarse tiernas, no crujientes.', null),
(3, 3, 'Escurre bien las patatas del aceite. Bate los huevos con sal en un bol grande y añade las patatas escurridas. Mezcla bien y deja reposar 5 minutos.', null),
(3, 4, 'Calienta 2 cucharadas de aceite en una sartén antiadherente de 22cm. Vierte la mezcla y cuaja a fuego medio-bajo 4-5 minutos hasta que los bordes estén firmes.', null),
(3, 5, 'Coloca un plato grande boca abajo sobre la sartén y voltea con un movimiento rápido y seguro. Desliza de nuevo la tortilla a la sartén por el lado crudo.', null),
(3, 6, 'Cuaja 2-3 minutos más para una tortilla jugosa. Deja reposar 5 minutos antes de cortar.', null);

INSERT IGNORE INTO recipe_tags (recipe_id, tag_id) VALUES
(3, 2),  -- Fácil
(3, 3),  -- Económico
(3, 12), -- Comfort food
(3, 29), -- Receta de la abuela
(3, 26); -- Un solo plato

INSERT IGNORE INTO recipe_diets (recipe_id, diet_id) VALUES
(3, 2),  -- Vegetariano
(3, 3),  -- Sin gluten
(3, 4),  -- Sin lactosa
(3, 14); -- Mediterránea

-- ===================== RECETA 4: BOWL VEGANO DE QUINOA =====================
INSERT IGNORE INTO recipes (id, title, description, slug, difficulty, servings, prep_time_minutes, status, author_id, category_id, created_at, updated_at) VALUES
(4, 'Bowl vegano de quinoa con aguacate y tomate',
 'Un bowl lleno de color, nutrientes y sabor. 100% vegano, sin gluten, listo en 25 minutos. Ideal para meal prep de la semana.',
 'bowl-vegano-quinoa-aguacate', 'EASY', 2, 25, 'PUBLISHED', 4, 3, NOW(), NOW());

INSERT IGNORE INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit, notes, sort_order) VALUES
(4, 15,  1,   'PIECE', 'maduro', 0),
(4, 1,   2,   'PIECE', 'cherry, partidos por la mitad', 1),
(4, 11,  80,  'GRAMS', 'mezcla de lechugas', 2),
(4, 10,  60,  'GRAMS', 'baby', 3),
(4, 16,  0.5, 'PIECE', 'zumo', 4),
(4, 44,  2,   'TABLESPOON', null, 5),
(4, 45,  1,   'PINCH', null, 6),
(4, 46,  1,   'PINCH', null, 7);

INSERT IGNORE INTO recipe_steps (recipe_id, step_number, description, image_url) VALUES
(4, 1, 'Añade la quinoa (200g, lavada) en un cazo con 400ml de agua y una pizca de sal. Lleva a ebullición, baja el fuego, tapa y cocina 15 minutos hasta que absorba toda el agua. Deja reposar 5 minutos con el fuego apagado.', null),
(4, 2, 'Mientras se cocina la quinoa, lava y seca las hojas de lechuga y espinacas. Corta los tomates cherry por la mitad.', null),
(4, 3, 'Corta el aguacate por la mitad, retira el hueso y extrae la pulpa con una cuchara. Córtalo en láminas y rocía con zumo de limón para que no oxide.', null),
(4, 4, 'Prepara el aliño mezclando el aceite de oliva, el zumo de limón restante, sal y pimienta.', null),
(4, 5, 'Monta el bowl: coloca una base de quinoa, luego las hojas verdes, los tomates cherry y las láminas de aguacate. Riega con el aliño y sirve inmediatamente.', null);

INSERT IGNORE INTO recipe_tags (recipe_id, tag_id) VALUES
(4, 1),  -- Rápido
(4, 2),  -- Fácil
(4, 10), -- Meal prep
(4, 13), -- Saludable
(4, 15), -- Bajo en calorías
(4, 27); -- Menos de 30 min

INSERT IGNORE INTO recipe_diets (recipe_id, diet_id) VALUES
(4, 1),  -- Vegano
(4, 2),  -- Vegetariano
(4, 3),  -- Sin gluten
(4, 4),  -- Sin lactosa
(4, 14); -- Mediterránea

-- ===================== RECETA 5: PASTA CARBONARA =====================
INSERT IGNORE INTO recipes (id, title, description, slug, difficulty, servings, prep_time_minutes, status, author_id, category_id, created_at, updated_at) VALUES
(5, 'Pasta carbonara a la italiana',
 'La carbonara de verdad: sin nata, sin chorizo. Solo huevo, guanciale, pecorino y pimienta. La receta que lleva siglos haciéndose en Roma.',
 'pasta-carbonara-italiana', 'MEDIUM', 2, 20, 'PUBLISHED', 3, 5, NOW(), NOW());

INSERT IGNORE INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit, notes, sort_order) VALUES
(5, 37,  200, 'GRAMS', 'espaguetis o rigatoni', 0),
(5, 28,  3,   'PIECE', '2 yemas + 1 entero', 1),
(5, 32,  60,  'GRAMS', 'pecorino romano o parmesano', 2),
(5, 46,  1,   'TEASPOON', 'recién molida, generosa', 3),
(5, 45,  1,   'TABLESPOON', 'para el agua de cocción', 4);

INSERT IGNORE INTO recipe_steps (recipe_id, step_number, description, image_url) VALUES
(5, 1, 'Cuece la pasta en abundante agua con sal siguiendo las instrucciones del paquete. Guarda una taza del agua de cocción antes de escurrir.', null),
(5, 2, 'Mezcla en un bol las yemas, el huevo entero y el queso rallado. Añade pimienta negra generosa y bate bien hasta obtener una crema espesa.', null),
(5, 3, 'En una sartén grande sin aceite, dora el guanciale (o panceta) cortado en dados a fuego medio-alto hasta que esté crujiente. Reserva la grasa que suelta.', null),
(5, 4, 'Añade la pasta escurrida a la sartén con el guanciale (fuego apagado). Remueve para que se mezcle con la grasa.', null),
(5, 5, 'Vierte la mezcla de huevo y queso sobre la pasta. Añade un poco del agua de cocción caliente y remueve enérgicamente creando una salsa cremosa. El calor residual cocinará el huevo sin que cuaje.', null),
(5, 6, 'Sirve inmediatamente con más queso rallado y pimienta negra por encima.', null);

INSERT IGNORE INTO recipe_tags (recipe_id, tag_id) VALUES
(5, 1),  -- Rápido
(5, 12), -- Comfort food
(5, 27), -- Menos de 30 min
(5, 14), -- Proteico
(5, 30); -- Gourmet

INSERT IGNORE INTO recipe_diets (recipe_id, diet_id) VALUES
(5, 4); -- Sin lactosa (no, pero lo dejamos fuera — correcto)

-- ===================== RECETA 6: BROWNIE DE CHOCOLATE =====================
INSERT IGNORE INTO recipes (id, title, description, slug, difficulty, servings, prep_time_minutes, status, author_id, category_id, created_at, updated_at) VALUES
(6, 'Brownie de chocolate intenso',
 'El brownie más fácil y adictivo del mundo. Crujiente por fuera, fundente por dentro. Con solo una sartén y 30 minutos lo tienes.',
 'brownie-chocolate-intenso', 'EASY', 9, 35, 'PUBLISHED', 6, 16, NOW(), NOW());

INSERT IGNORE INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit, notes, sort_order) VALUES
(6, 58,  200, 'GRAMS', 'mínimo 70% cacao', 0),
(6, 35,  150, 'GRAMS', 'sin sal', 1),
(6, 52,  200, 'GRAMS', null, 2),
(6, 28,  3,   'PIECE', 'L, temperatura ambiente', 3),
(6, 40,  80,  'GRAMS', null, 4),
(6, 60,  1,   'TEASPOON', 'extracto', 5),
(6, 45,  1,   'PINCH', null, 6);

INSERT IGNORE INTO recipe_steps (recipe_id, step_number, description, image_url) VALUES
(6, 1, 'Precalienta el horno a 180°C con calor arriba y abajo. Engrasa un molde cuadrado de 20x20cm y fórralo con papel de horno.', null),
(6, 2, 'Funde el chocolate negro troceado con la mantequilla al baño maría o en el microondas a intervalos de 30 segundos. Remueve hasta obtener una mezcla lisa. Deja templar 5 minutos.', null),
(6, 3, 'Añade el azúcar a la mezcla de chocolate y bate bien. Incorpora los huevos uno a uno, batiendo después de cada adición. Añade la vainilla.', null),
(6, 4, 'Tamiza la harina y la sal sobre la mezcla y mezcla con movimientos envolventes hasta integrar. No sobremezcles.', null),
(6, 5, 'Vierte la masa en el molde y hornea 20-22 minutos. Estará listo cuando al insertar un palillo salga con migas húmedas (no líquido). Si sale limpio, está sobrehorneado.', null),
(6, 6, 'Deja enfriar completamente en el molde antes de desmoldar y cortar en 9 cuadrados. Aguanta 3 días en recipiente hermético.', null);

INSERT IGNORE INTO recipe_tags (recipe_id, tag_id) VALUES
(6, 2),  -- Fácil
(6, 5),  -- Al horno
(6, 12), -- Comfort food
(6, 17), -- Navidad
(6, 20); -- San Valentín

-- ===================== RECETA 7: LENTEJAS ESTOFADAS =====================
INSERT IGNORE INTO recipes (id, title, description, slug, difficulty, servings, prep_time_minutes, status, author_id, category_id, created_at, updated_at) VALUES
(7, 'Lentejas estofadas con verduras',
 'Lentejas de los de toda la vida: bien cargadas de verduras, sabrosas y reconfortantes. Perfectas para batch cooking.',
 'lentejas-estofadas-verduras', 'EASY', 6, 50, 'PUBLISHED', 4, 10, NOW(), NOW());

INSERT IGNORE INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit, notes, sort_order) VALUES
(7, 42,  400, 'GRAMS', 'lentejas pardinas, sin remojar', 0),
(7, 2,   1,   'PIECE', 'grande', 1),
(7, 6,   2,   'PIECE', null, 2),
(7, 4,   1,   'PIECE', null, 3),
(7, 3,   3,   'PIECE', 'dientes', 4),
(7, 1,   2,   'PIECE', 'troceados', 5),
(7, 44,  4,   'TABLESPOON', null, 6),
(7, 47,  1,   'TEASPOON', 'dulce', 7),
(7, 48,  0.5, 'TEASPOON', null, 8),
(7, 45,  1,   'TO_TASTE', null, 9),
(7, 55,  1.5, 'LITERS', null, 10);

INSERT IGNORE INTO recipe_steps (recipe_id, step_number, description, image_url) VALUES
(7, 1, 'Calienta el aceite en una olla grande. Sofríe la cebolla picada a fuego medio durante 8 minutos hasta que esté transparente. Añade el ajo picado y sofríe 2 minutos más.', null),
(7, 2, 'Añade la zanahoria y el pimiento troceados. Sofríe 5 minutos. Incorpora el pimentón y el comino, remueve 30 segundos.', null),
(7, 3, 'Añade el tomate troceado y sofríe 5 minutos hasta que pierda el agua.', null),
(7, 4, 'Añade las lentejas (lavadas) y cubre con el caldo caliente. Lleva a ebullición.', null),
(7, 5, 'Reduce el fuego a medio-bajo y cocina tapado durante 30-35 minutos, removiendo de vez en cuando, hasta que las lentejas estén tiernas. Ajusta de sal.', null),
(7, 6, 'Si las lentejas han quedado muy espesas, añade un poco de agua caliente. Sirve calientes con un chorrito de aceite de oliva.', null);

INSERT IGNORE INTO recipe_tags (recipe_id, tag_id) VALUES
(7, 2),  -- Fácil
(7, 3),  -- Económico
(7, 8),  -- Slow cooking
(7, 9),  -- Batch cooking
(7, 10), -- Meal prep
(7, 12), -- Comfort food
(7, 16), -- Alto en fibra
(7, 19); -- Otoño

INSERT IGNORE INTO recipe_diets (recipe_id, diet_id) VALUES
(7, 1),  -- Vegano
(7, 2),  -- Vegetariano
(7, 3),  -- Sin gluten
(7, 4),  -- Sin lactosa
(7, 14); -- Mediterránea

-- ===================== RECETA 8: POLLO AL LIMÓN =====================
INSERT IGNORE INTO recipes (id, title, description, slug, difficulty, servings, prep_time_minutes, status, author_id, category_id, created_at, updated_at) VALUES
(8, 'Pollo al limón con ajo',
 'Pechugas de pollo jugosas, marinadas en limón y ajo, cocinadas a la plancha en 15 minutos. Simple, sano y con mucho sabor.',
 'pollo-limon-ajo', 'EASY', 2, 30, 'PUBLISHED', 5, 6, NOW(), NOW());

INSERT IGNORE INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit, notes, sort_order) VALUES
(8, 21,  2,   'PIECE', 'pechugas grandes', 0),
(8, 16,  2,   'PIECE', 'zumo y ralladura', 1),
(8, 3,   4,   'PIECE', 'dientes, picados', 2),
(8, 44,  3,   'TABLESPOON', null, 3),
(8, 50,  1,   'TABLESPOON', 'fresco picado', 4),
(8, 45,  1,   'TEASPOON', null, 5),
(8, 46,  1,   'PINCH', null, 6);

INSERT IGNORE INTO recipe_steps (recipe_id, step_number, description, image_url) VALUES
(8, 1, 'Prepara la marinada: mezcla el zumo de limón, la ralladura, el ajo picado, el aceite, el perejil, la sal y la pimienta.', null),
(8, 2, 'Abre las pechugas en mariposa y cúbrelas con film. Aplana con un mazo hasta que tengan un grosor uniforme de 1,5cm.', null),
(8, 3, 'Introduce las pechugas en la marinada y deja reposar al menos 20 minutos (o hasta 24h en la nevera).', null),
(8, 4, 'Calienta una plancha o sartén a fuego alto. Cuando esté muy caliente, añade las pechugas escurridas y cocina 3-4 minutos por cada lado.', null),
(8, 5, 'Deja reposar 3 minutos antes de servir. Acompaña con la marinada reducida como salsa.', null);

INSERT IGNORE INTO recipe_tags (recipe_id, tag_id) VALUES
(8, 1),  -- Rápido
(8, 2),  -- Fácil
(8, 6),  -- A la plancha
(8, 13), -- Saludable
(8, 14), -- Proteico
(8, 15), -- Bajo en calorías
(8, 27); -- Menos de 30 min

INSERT IGNORE INTO recipe_diets (recipe_id, diet_id) VALUES
(8, 3),  -- Sin gluten
(8, 4),  -- Sin lactosa
(8, 14); -- Mediterránea

-- ===================== RECETA 9: PAN CASERO RÚSTICO =====================
INSERT IGNORE INTO recipes (id, title, description, slug, difficulty, servings, prep_time_minutes, status, author_id, category_id, created_at, updated_at) VALUES
(9, 'Pan casero rústico sin amasar',
 'Pan artesanal crujiente con miga esponjosa. La técnica sin amasar hace el trabajo por ti: solo mezcla, espera y hornea.',
 'pan-casero-rustico-sin-amasar', 'MEDIUM', 8, 720, 'PUBLISHED', 6, 15, NOW(), NOW());

INSERT IGNORE INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit, notes, sort_order) VALUES
(9, 40,  500, 'GRAMS', 'harina de fuerza', 0),
(9, 57,  7,   'GRAMS', 'seca de panadero', 1),
(9, 45,  10,  'GRAMS', null, 2);

INSERT IGNORE INTO recipe_steps (recipe_id, step_number, description, image_url) VALUES
(9, 1, 'Mezcla en un bol grande la harina, la levadura seca y la sal. Añade 375ml de agua tibia (no caliente) y mezcla con una cuchara hasta que no quede harina seca.', null),
(9, 2, 'La masa quedará pegajosa y sin forma. Tapa el bol con film y deja fermentar 12 horas a temperatura ambiente (o toda la noche).', null),
(9, 3, 'Al día siguiente, vuelca la masa sobre una superficie enharinada. Dóblala sobre sí misma varias veces. Dale forma de bola y colócala sobre papel de horno.', null),
(9, 4, 'Tapa con un trapo y deja levar 1 hora más. Mientras tanto, precalienta el horno a 230°C con una olla de hierro fundido dentro (con tapa).', null),
(9, 5, 'Con mucho cuidado (la olla estará muy caliente), coloca el pan con el papel dentro de la olla. Tapa y hornea 30 minutos.', null),
(9, 6, 'Quita la tapa y hornea 15 minutos más hasta que la corteza esté dorada y oscura. Deja enfriar en una rejilla al menos 1 hora antes de cortar.', null);

INSERT IGNORE INTO recipe_tags (recipe_id, tag_id) VALUES
(9, 5),  -- Al horno
(9, 12), -- Comfort food
(9, 3),  -- Económico
(9, 8);  -- Slow cooking

INSERT IGNORE INTO recipe_diets (recipe_id, diet_id) VALUES
(9, 1),  -- Vegano
(9, 2),  -- Vegetariano
(9, 4);  -- Sin lactosa

-- ===================== RECETA 10: SALMÓN TERIYAKI =====================
INSERT IGNORE INTO recipes (id, title, description, slug, difficulty, servings, prep_time_minutes, status, author_id, category_id, created_at, updated_at) VALUES
(10, 'Salmón teriyaki con arroz',
 'El salmón teriyaki perfecto: glaseado brillante, jugoso por dentro. Con arroz blanco esponjoso, una cena de restaurante japonés en casa.',
 'salmon-teriyaki-arroz', 'EASY', 2, 25, 'PUBLISHED', 3, 7, NOW(), NOW());

INSERT IGNORE INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit, notes, sort_order) VALUES
(10, 24,  2,   'PIECE', 'lomos de 150g', 0),
(10, 54,  3,   'TABLESPOON', null, 1),
(10, 52,  2,   'TABLESPOON', null, 2),
(10, 36,  200, 'GRAMS', 'arroz japonés', 3),
(10, 44,  1,   'TABLESPOON', null, 4),
(10, 45,  1,   'PINCH', null, 5);

INSERT IGNORE INTO recipe_steps (recipe_id, step_number, description, image_url) VALUES
(10, 1, 'Prepara el arroz: lávalo bajo agua fría hasta que salga clara. Cuécelo con agua a razón 1:1.2 con una pizca de sal. Tapa y cocina 12 minutos a fuego bajo.', null),
(10, 2, 'Prepara la salsa teriyaki: mezcla la salsa de soja con el azúcar en un cazo pequeño. Calienta a fuego medio 3-4 minutos removiendo hasta que espese ligeramente.', null),
(10, 3, 'Seca los lomos de salmón con papel de cocina. Sazónales con sal.', null),
(10, 4, 'Calienta el aceite en una sartén antiadherente a fuego medio-alto. Coloca el salmón con la piel hacia abajo y cocina 4 minutos. Da la vuelta y cocina 2 minutos más.', null),
(10, 5, 'Vierte la salsa teriyaki sobre el salmón y glasea durante 1 minuto, dando la vuelta para que quede bien cubierto.', null),
(10, 6, 'Sirve el salmón sobre el arroz blanco y riega con el resto de la salsa del pan.', null);

INSERT IGNORE INTO recipe_tags (recipe_id, tag_id) VALUES
(10, 1),  -- Rápido
(10, 2),  -- Fácil
(10, 13), -- Saludable
(10, 14), -- Proteico
(10, 27); -- Menos de 30 min

INSERT IGNORE INTO recipe_diets (recipe_id, diet_id) VALUES
(10, 3),  -- Sin gluten
(10, 4);  -- Sin lactosa

-- ===================== RECETA 11: HUMMUS CASERO =====================
INSERT IGNORE INTO recipes (id, title, description, slug, difficulty, servings, prep_time_minutes, status, author_id, category_id, created_at, updated_at) VALUES
(11, 'Hummus casero cremoso',
 'Hummus esponjoso y cremoso como el del restaurante árabe. El secreto: quitar la piel a los garbanzos y usar mucho tahini.',
 'hummus-casero-cremoso', 'EASY', 4, 15, 'PUBLISHED', 4, 1, NOW(), NOW());

INSERT IGNORE INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit, notes, sort_order) VALUES
(11, 41,  400, 'GRAMS', 'cocidos (1 bote escurrido)', 0),
(11, 16,  1,   'PIECE', 'zumo', 1),
(11, 3,   1,   'PIECE', 'diente', 2),
(11, 44,  2,   'TABLESPOON', null, 3),
(11, 45,  1,   'TEASPOON', null, 4),
(11, 48,  0.5, 'TEASPOON', null, 5),
(11, 47,  0.5, 'TEASPOON', 'para decorar', 6);

INSERT IGNORE INTO recipe_steps (recipe_id, step_number, description, image_url) VALUES
(11, 1, 'Escurre y enjuaga los garbanzos. Para un hummus más cremoso, frota los garbanzos entre las manos para quitarles la piel fina exterior.', null),
(11, 2, 'En el vaso de la batidora o procesador, añade los garbanzos, el tahini (3 cucharadas), el zumo de limón, el ajo, la sal y el comino.', null),
(11, 3, 'Tritura a máxima potencia. Con el motor en marcha, añade 3-4 cucharadas de agua fría hasta conseguir la textura deseada.', null),
(11, 4, 'Prueba y ajusta de sal y limón. Si queda demasiado espeso, añade más agua.', null),
(11, 5, 'Sirve en un bol con un poco de aceite de oliva, pimentón y semillas de sésamo opcionales. Acompaña con pan de pita o crudités.', null);

INSERT IGNORE INTO recipe_tags (recipe_id, tag_id) VALUES
(11, 1),  -- Rápido
(11, 2),  -- Fácil
(11, 3),  -- Económico
(11, 4),  -- Sin horno
(11, 13), -- Saludable
(11, 22), -- Picnic
(11, 27); -- Menos de 30 min

INSERT IGNORE INTO recipe_diets (recipe_id, diet_id) VALUES
(11, 1),  -- Vegano
(11, 2),  -- Vegetariano
(11, 3),  -- Sin gluten
(11, 4),  -- Sin lactosa
(11, 14); -- Mediterránea

-- ===================== RECETA 12: BORRADOR (DRAFT) =====================
INSERT IGNORE INTO recipes (id, title, description, slug, difficulty, servings, prep_time_minutes, status, author_id, category_id, created_at, updated_at) VALUES
(12, 'Tarta de queso vasca (en elaboración)',
 'La famosa tarta de queso de San Sebastián, quemada por fuera y cremosa por dentro. Próximamente...',
 'tarta-queso-vasca-draft', 'MEDIUM', 8, 60, 'DRAFT', 6, 17, NOW(), NOW());

-- =============================================================================
-- REVIEWS (VALORACIONES)
-- =============================================================================

INSERT IGNORE INTO reviews (user_id, recipe_id, rating, comment, created_at, updated_at) VALUES
(3, 1, 5, '¡El mejor gazpacho que he probado fuera de Sevilla! El punto de vinagre de Jerez marca la diferencia.', NOW(), NOW()),
(4, 1, 5, 'Hecho con tomates de la huerta y queda increíble. La técnica de colar es clave.', NOW(), NOW()),
(5, 3, 4, 'Muy buena receta. Le puse chorizo también y quedó de muerte. Eso sí, hay que ser valiente al voltear.', NOW(), NOW()),
(2, 5, 5, 'Por fin una carbonara sin nata. La textura cremosa del huevo es impresionante cuando se hace bien.', NOW(), NOW()),
(6, 5, 4, 'Perfecta. Solo le doy 4 estrellas porque la primera vez se me cuajó el huevo, pero en la segunda salió genial.', NOW(), NOW()),
(4, 7, 5, 'Mis lentejas favoritas ya. Hago el doble y congelo. Gracias por la receta!', NOW(), NOW()),
(2, 6, 5, 'El brownie más bueno que he hecho en mi vida. El punto de horneado es crucial, sacarlo con migas húmedas.', NOW(), NOW()),
(3, 10, 5, 'Salmón espectacular. La salsa teriyaki casera es infinitamente mejor que la de bote.', NOW(), NOW()),
(5, 8, 5, 'Receta de gimnasio ideal. Preparo 4 pechugas los domingos y tengo proteína para toda la semana.', NOW(), NOW()),
(2, 11, 4, 'Muy rico. Lo hago con garbanzos secos cocidos en casa y la diferencia es notable.', NOW(), NOW());

-- =============================================================================
-- FAVORITOS
-- =============================================================================

INSERT IGNORE INTO favorites (user_id, recipe_id, created_at) VALUES
(2, 5, NOW()),
(2, 6, NOW()),
(2, 10, NOW()),
(3, 1, NOW()),
(3, 7, NOW()),
(4, 1, NOW()),
(4, 4, NOW()),
(4, 7, NOW()),
(4, 11, NOW()),
(5, 2, NOW()),
(5, 8, NOW()),
(6, 5, NOW()),
(6, 6, NOW()),
(6, 9, NOW());

-- =============================================================================
-- LISTAS DE LA COMPRA (SHOPPING LISTS / CARRITOS)
-- =============================================================================

-- Lista 1: Compra semanal de María (mezcla de ingredientes de varias recetas)
INSERT IGNORE INTO shopping_lists (id, title, personal, user_id, created_at, updated_at) VALUES
(1, 'Compra semanal', true, 2, NOW(), NOW());

INSERT IGNORE INTO shopping_items (shopping_list_id, ingredient_id, custom_name, quantity, unit, notes, checked, sort_order) VALUES
-- Para gazpacho y ensaladas
(1, 1,  null, 2,    'KILOGRAMS', 'tomates maduros para gazpacho', false, 0),
(1, 14, null, 2,    'PIECE',     null, false, 1),
(1, 4,  null, 3,    'PIECE',     null, false, 2),
(1, 15, null, 4,    'PIECE',     'aguacates para el bowl', false, 3),
(1, 11, null, 200,  'GRAMS',     null, true, 4),
-- Para tortilla
(1, 7,  null, 1,    'KILOGRAMS', null, false, 5),
(1, 28, null, 12,   'PIECE',     'carton de huevos', false, 6),
-- Para pasta carbonara
(1, 37, null, 400,  'GRAMS',     null, false, 7),
(1, 32, null, 100,  'GRAMS',     'pecorino romano', false, 8),
-- Básicos
(1, 44, null, 500,  'MILLILITERS', 'aceite AOVE', true, 9),
(1, 3,  null, 1,    'PIECE',     'cabeza de ajos', false, 10),
-- Item personalizado (no en catálogo)
(1, null, 'Papel de cocina', 3, 'PIECE', null, false, 11),
(1, null, 'Bolsas de basura', 1, 'PIECE', null, true, 12);

-- Lista 2: Ingredientes para cena del sábado (paella) - Pedro
INSERT IGNORE INTO shopping_lists (id, title, personal, user_id, created_at, updated_at) VALUES
(2, 'Paella del sábado', true, 3, NOW(), NOW());

INSERT IGNORE INTO shopping_items (shopping_list_id, ingredient_id, custom_name, quantity, unit, notes, checked, sort_order) VALUES
(2, 36,  null, 600,  'GRAMS',   'arroz bomba, imprescindible', false, 0),
(2, 21,  null, 500,  'GRAMS',   'pollo troceado', false, 1),
(2, 4,   null, 2,    'PIECE',   null, false, 2),
(2, 1,   null, 3,    'PIECE',   'para el sofrito', false, 3),
(2, 55,  null, 2,    'LITERS',  'caldo de pollo casero o brick', false, 4),
(2, 47,  null, 1,    'PIECE',   'pimentón dulce de la Vera', true, 5),
(2, null, 'Azafrán en hebras', 1, 'PIECE', 'imprescindible, no usar colorante', false, 6),
(2, null, 'Judías verdes', 200, 'GRAMS', 'o bajoqueta valenciana', false, 7),
(2, null, 'Garrofó (alubia valenciana)', 100, 'GRAMS', 'puede ser congelado', false, 8),
(2, 44,  null, 150,  'MILLILITERS', null, true, 9);

-- Lista 3: Batch cooking vegano del domingo - Ana
INSERT IGNORE INTO shopping_lists (id, title, personal, user_id, created_at, updated_at) VALUES
(3, 'Batch cooking domingo', true, 4, NOW(), NOW());

INSERT IGNORE INTO shopping_items (shopping_list_id, ingredient_id, custom_name, quantity, unit, notes, checked, sort_order) VALUES
-- Para lentejas x2
(3, 42,  null, 800,  'GRAMS',   'lentejas pardinas', false, 0),
(3, 2,   null, 2,    'PIECE',   null, false, 1),
(3, 6,   null, 4,    'PIECE',   null, false, 2),
(3, 4,   null, 2,    'PIECE',   null, true, 3),
-- Para bowl de quinoa x4
(3, null, 'Quinoa', 400, 'GRAMS', null, false, 4),
(3, 15,  null, 6,    'PIECE',   'aguacates maduros', false, 5),
(3, 10,  null, 300,  'GRAMS',   'espinacas baby', false, 6),
-- Para hummus
(3, 41,  null, 800,  'GRAMS',   'garbanzos cocidos (2 botes)', false, 7),
(3, null, 'Tahini', 200, 'GRAMS', 'pasta de sésamo', false, 8),
(3, 16,  null, 4,    'PIECE',   null, true, 9),
-- Básicos
(3, 44,  null, 1,    'LITERS',  null, false, 10),
(3, 3,   null, 1,    'PIECE',   'cabeza', true, 11),
(3, 45,  null, 1,    'PIECE',   'sal gruesa', false, 12),
(3, null, 'Contenedores herméticos x6', 1, 'PIECE', 'para conservar en nevera', false, 13);

-- Lista 4: Repostería del fin de semana - Laura
INSERT IGNORE INTO shopping_lists (id, title, personal, user_id, created_at, updated_at) VALUES
(4, 'Repostería fin de semana', true, 6, NOW(), NOW());

INSERT IGNORE INTO shopping_items (shopping_list_id, ingredient_id, custom_name, quantity, unit, notes, checked, sort_order) VALUES
-- Para brownies x2 bandejas
(4, 58,  null, 400,  'GRAMS',   'chocolate 70% mínimo', false, 0),
(4, 35,  null, 300,  'GRAMS',   'mantequilla sin sal', false, 1),
(4, 52,  null, 400,  'GRAMS',   'azúcar blanquilla', true, 2),
(4, 28,  null, 6,    'PIECE',   null, true, 3),
(4, 40,  null, 200,  'GRAMS',   'harina todo uso', false, 4),
(4, 60,  null, 1,    'PIECE',   'extracto de vainilla', false, 5),
-- Para pan
(4, 40,  null, 1,    'KILOGRAMS', 'harina de fuerza', false, 6),
(4, 57,  null, 14,   'GRAMS',   'levadura seca de panadero (2 sobres)', false, 7),
-- Extras repostería
(4, null, 'Papel de horno', 1, 'PIECE', null, true, 8),
(4, null, 'Molde cuadrado 20x20', 1, 'PIECE', 'si no tienes', false, 9),
(4, null, 'Termómetro horno', 1, 'PIECE', 'opcional pero recomendable', false, 10);

-- Lista 5: Cena romántica San Valentín - Carlos
INSERT IGNORE INTO shopping_lists (id, title, personal, user_id, created_at, updated_at) VALUES
(5, 'Cena San Valentín ❤️', true, 5, NOW(), NOW());

INSERT IGNORE INTO shopping_items (shopping_list_id, ingredient_id, custom_name, quantity, unit, notes, checked, sort_order) VALUES
(5, 24,  null, 2,    'PIECE',   'lomos de salmón fresco, 200g cada uno', false, 0),
(5, 54,  null, 100,  'MILLILITERS', 'salsa de soja japonesa', false, 1),
(5, 52,  null, 50,   'GRAMS',   null, false, 2),
(5, 36,  null, 300,  'GRAMS',   'arroz japonés (sushi rice)', false, 3),
(5, 58,  null, 200,  'GRAMS',   'para el brownie de postre', false, 4),
(5, 35,  null, 150,  'GRAMS',   null, false, 5),
(5, null, 'Vino tinto Rioja', 1, 'PIECE', 'botella', false, 6),
(5, null, 'Velas', 2, 'PIECE', null, false, 7),
(5, 20,  null, 200,  'GRAMS',   'fresas para decorar el brownie', false, 8);

-- =============================================================================
-- NOTIFICACIONES DE EJEMPLO
-- =============================================================================

INSERT IGNORE INTO notifications (user_id, type, title, message, reference_id, reference_type, is_read, created_at) VALUES
(2, 'NEW_FOLLOWER',  'Nuevo seguidor',         'chef_pedro ahora te sigue',                        3,  'USER',   false, NOW()),
(2, 'NEW_FOLLOWER',  'Nuevo seguidor',         'veggie_ana ahora te sigue',                        4,  'USER',   false, NOW()),
(2, 'NEW_REVIEW',    'Nueva valoración',       'chef_pedro valoró tu gazpacho con 5 estrellas',   1,  'RECIPE', false, NOW()),
(2, 'NEW_FAVORITE',  'Receta guardada',        'veggie_ana guardó tu Gazpacho como favorito',     1,  'RECIPE', true,  NOW()),
(3, 'NEW_REVIEW',    'Nueva valoración',       'maria_cocina valoró tu carbonara con 5 estrellas', 5, 'RECIPE', false, NOW()),
(4, 'NEW_REVIEW',    'Nueva valoración',       'veggie_ana valoró tu bowl con 5 estrellas',       7,  'RECIPE', true,  NOW()),
(6, 'NEW_REVIEW',    'Nueva valoración',       'maria_cocina valoró tu brownie con 5 estrellas',  6,  'RECIPE', false, NOW()),
(6, 'NEW_FOLLOWER',  'Nuevo seguidor',         'maria_cocina ahora te sigue',                     2,  'USER',   false, NOW());