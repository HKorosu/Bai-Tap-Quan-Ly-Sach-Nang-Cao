CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE books (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(180) NOT NULL,
    author VARCHAR(120) NOT NULL,
    isbn VARCHAR(40) NOT NULL UNIQUE,
    category_id BIGINT NOT NULL,
    available_quantity INT NOT NULL CHECK (available_quantity >= 0),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_books_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE members (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    phone VARCHAR(30)
);

CREATE TABLE fine_policies (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    daily_fine_amount DECIMAL(12, 2) NOT NULL,
    max_fine_amount DECIMAL(12, 2),
    grace_days INT NOT NULL,
    active BOOLEAN NOT NULL
);

CREATE TABLE borrowings (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    borrowed_date DATE NOT NULL,
    due_date DATE NOT NULL,
    returned_date DATE,
    status VARCHAR(30) NOT NULL,
    total_fine_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    paid_fine_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    waived_fine_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_borrowings_member FOREIGN KEY (member_id) REFERENCES members(id),
    INDEX idx_borrowings_member (member_id),
    INDEX idx_borrowings_due_status (due_date, status)
);

CREATE TABLE borrowing_details (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    borrowing_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    returned_quantity INT NOT NULL DEFAULT 0 CHECK (returned_quantity >= 0 AND returned_quantity <= quantity),
    last_returned_date DATE,
    late_days INT NOT NULL DEFAULT 0,
    fine_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_details_borrowing FOREIGN KEY (borrowing_id) REFERENCES borrowings(id),
    CONSTRAINT fk_details_book FOREIGN KEY (book_id) REFERENCES books(id),
    CONSTRAINT uk_borrowing_book UNIQUE (borrowing_id, book_id),
    INDEX idx_details_book (book_id)
);

CREATE TABLE fine_payments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    borrowing_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    payment_date DATE NOT NULL,
    method VARCHAR(40) NOT NULL,
    note VARCHAR(500),
    CONSTRAINT fk_payments_borrowing FOREIGN KEY (borrowing_id) REFERENCES borrowings(id),
    INDEX idx_payments_borrowing (borrowing_id)
);

CREATE TABLE fine_waivers (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    borrowing_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    approved_by VARCHAR(150) NOT NULL,
    approved_date DATE NOT NULL,
    CONSTRAINT fk_waivers_borrowing FOREIGN KEY (borrowing_id) REFERENCES borrowings(id),
    INDEX idx_waivers_borrowing (borrowing_id)
);

INSERT INTO categories (name) VALUES ('Văn học'), ('Công nghệ');

INSERT INTO books (title, author, isbn, category_id, available_quantity)
SELECT 'Dế Mèn phiêu lưu ký', 'Tô Hoài', 'DE-MEN-001', id, 5 FROM categories WHERE name = 'Văn học';
INSERT INTO books (title, author, isbn, category_id, available_quantity)
SELECT 'Cho tôi xin một vé đi tuổi thơ', 'Nguyễn Nhật Ánh', 'NGUYEN-NHAT-ANH-001', id, 3 FROM categories WHERE name = 'Văn học';
INSERT INTO books (title, author, isbn, category_id, available_quantity)
SELECT 'Clean Code', 'Robert C. Martin', 'CLEAN-CODE-001', id, 2 FROM categories WHERE name = 'Công nghệ';

INSERT INTO members (full_name, email, phone) VALUES
    ('Nguyễn Văn An', 'an@example.com', '0900000001'),
    ('Trần Thị Bình', 'binh@example.com', '0900000002');

INSERT INTO fine_policies (daily_fine_amount, max_fine_amount, grace_days, active)
VALUES (1000.00, 50000.00, 0, TRUE);
