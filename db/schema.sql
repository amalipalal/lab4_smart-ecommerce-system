CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE role (
    role_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	role_name VARCHAR(50) NOT NULL,
	description VARCHAR(255) NOT NULL
);

CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	username VARCHAR(100) NOT NULL,
	password_hash VARCHAR(255) NOT NULL,
	role_id UUID NOT NULL,
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

	CONSTRAINT fk_role_in_users
		FOREIGN KEY (role_id) 
		REFERENCES role(role_id)
		ON DELETE RESTRICT
);

CREATE INDEX index_user_role_id ON users(role_id);

CREATE TABLE category (
    category_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	name VARCHAR(100) NOT NULL,
	description VARCHAR(255) NOT NULL,
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE product (
    product_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	name VARCHAR(100) NOT NULL,
	description TEXT NOT NULL,
	price DECIMAL(10,2) NOT NULL CHECK (price > 0),
	stock_quantity INT,
	category_id UUID NOT NULL,
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

	CONSTRAINT fk_category_in_product
		FOREIGN KEY (category_id) 
		REFERENCES category(category_id)
		ON DELETE RESTRICT
);

CREATE INDEX index_product_category_id ON product(category_id);

CREATE TABLE customer (
    customer_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	first_name VARCHAR(150) NOT NULL,
	last_name VARCHAR(150),
	email VARCHAR(150) NOT NULL,
	phone VARCHAR(30) NOT NULL,
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE review (
    review_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	product_id UUID NOT NULL,
	customer_id UUID NOT NULL,
	rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
	comment TEXT,
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

	CONSTRAINT fk_product_in_review
		FOREIGN KEY (product_id) 
		REFERENCES product(product_id)
		ON DELETE CASCADE,

	CONSTRAINT fk_customer_in_review
		FOREIGN KEY (customer_id) 
		REFERENCES customer(customer_id)
		ON DELETE CASCADE,

	CONSTRAINT uq_review_customer_product
		UNIQUE (product_id, customer_id)
);

CREATE INDEX index_review_product_id ON review(product_id);

CREATE TABLE orders(
    order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	customer_id UUID NOT NULL,
	order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount > 0),
	shipping_country VARCHAR(100),
	shipping_city VARCHAR(100),
	shipping_postal_code VARCHAR(100) NOT NULL,

	CONSTRAINT fk_customer_in_orders
		FOREIGN KEY (customer_id)
		REFERENCES customer(customer_id)
		ON DELETE RESTRICT
);

CREATE INDEX index_order_customer_id ON orders(customer_id);

CREATE TABLE order_item (
	order_item_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	order_id UUID NOT NULL,
	product_id UUID NOT NULL,
	quantity INT NOT NULL CHECK (quantity > 0),
	price_at_purchase DECIMAL(10, 2) NOT NULL CHECK (price_at_purchase > 0),

	CONSTRAINT fk_order_in_order_item
		FOREIGN KEY (order_id)
		REFERENCES orders(order_id)
		ON DELETE CASCADE,

	CONSTRAINT fk_product_in_order_item
		FOREIGN KEY (product_id)
		REFERENCES product(product_id)
		ON DELETE RESTRICT
);

CREATE INDEX index_order_item_order_id ON order_item(order_id);
CREATE INDEX index_order_item_product_id ON order_item(product_id);