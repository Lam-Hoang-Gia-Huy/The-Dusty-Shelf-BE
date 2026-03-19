-- Sample Products (Books) with explicit IDs and corrected Category IDs
-- Using ON CONFLICT (id) DO NOTHING to allow rerunning the script safely.

-- Fiction (ID: 12)
INSERT INTO product (id, name, author, category_id, description, price, stock_quantity, status, average_score, created_date) VALUES 
(101, 'The Great Gatsby', 'F. Scott Fitzgerald', 12, 'A classic novel set in the Roaring Twenties.', 15, 50, true, 4.8, CURRENT_TIMESTAMP),
(102, 'To Kill a Mockingbird', 'Harper Lee', 12, 'A story of racial injustice and the loss of innocence.', 12, 30, true, 4.9, CURRENT_TIMESTAMP),
(103, '1984', 'George Orwell', 12, 'A dystopian social science fiction novel.', 14, 40, true, 4.7, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Science (ID: 13)
INSERT INTO product (id, name, author, category_id, description, price, stock_quantity, status, average_score, created_date) VALUES 
(104, 'A Brief History of Time', 'Stephen Hawking', 13, 'A landmark volume in science writing.', 18, 25, true, 4.8, CURRENT_TIMESTAMP),
(105, 'The Selfish Gene', 'Richard Dawkins', 13, 'Interpretations of evolution from a gene-centered perspective.', 20, 15, true, 4.6, CURRENT_TIMESTAMP),
(106, 'Cosmos', 'Carl Sagan', 13, 'Exploration of the universe and human history of science.', 22, 10, true, 4.9, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- History (ID: 14)
INSERT INTO product (id, name, author, category_id, description, price, stock_quantity, status, average_score, created_date) VALUES 
(107, 'Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', 14, 'Exploration of the history of our species.', 25, 20, true, 4.8, CURRENT_TIMESTAMP),
(108, 'Guns, Germs, and Steel', 'Jared Diamond', 14, 'The fates of human societies.', 24, 12, true, 4.7, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Technology (ID: 15)
INSERT INTO product (id, name, author, category_id, description, price, stock_quantity, status, average_score, created_date) VALUES 
(109, 'Clean Code', 'Robert C. Martin', 15, 'A handbook of agile software craftsmanship.', 45, 100, true, 4.9, CURRENT_TIMESTAMP),
(110, 'The Pragmatic Programmer', 'Andrew Hunt & David Thomas', 15, 'Your journey to mastery.', 40, 60, true, 4.9, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Sample Image URLs (using placeholders)
INSERT INTO image_url (id, image_url, product_id) VALUES 
(101, 'https://res.cloudinary.com/demo/image/upload/v1/book_cover_1.jpg', 101),
(102, 'https://res.cloudinary.com/demo/image/upload/v1/book_cover_2.jpg', 102),
(103, 'https://res.cloudinary.com/demo/image/upload/v1/book_cover_3.jpg', 103),
(104, 'https://res.cloudinary.com/demo/image/upload/v1/book_cover_4.jpg', 104),
(105, 'https://res.cloudinary.com/demo/image/upload/v1/book_cover_5.jpg', 105),
(106, 'https://res.cloudinary.com/demo/image/upload/v1/book_cover_6.jpg', 106),
(107, 'https://res.cloudinary.com/demo/image/upload/v1/book_cover_7.jpg', 107),
(108, 'https://res.cloudinary.com/demo/image/upload/v1/book_cover_8.jpg', 108),
(109, 'https://res.cloudinary.com/demo/image/upload/v1/book_cover_9.jpg', 109),
(110, 'https://res.cloudinary.com/demo/image/upload/v1/book_cover_10.jpg', 110)
ON CONFLICT (id) DO NOTHING;
