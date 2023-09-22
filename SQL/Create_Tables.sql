DROP DATABASE IF EXISTS grocery_store;

CREATE DATABASE grocery_store;

USE grocery_store;


CREATE TABLE customer(
	customer_id INT PRIMARY KEY AUTO_INCREMENT,
    email_address VARCHAR(200) NOT NULL,
    customer_name VARCHAR(200) NOT NULL,
    date_of_birth DATE NOT NULL,
    grocery_dollar DOUBLE DEFAULT 0 CHECK (grocery_dollar >= 0),
    points INT DEFAULT 0 CHECK (points >= 0)
);


CREATE TABLE customer_order(
	order_id INT PRIMARY KEY AUTO_INCREMENT,
    order_date DATE NOT NULL,
    total_amount DOUBLE CHECK(total_amount >= 0) NOT NULL,
    customer_id INT NOT NULL,
    CONSTRAINT customer_places_order
		FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id)
        ON UPDATE CASCADE ON DELETE CASCADE
);


CREATE TABLE store_area(
	area_id INT PRIMARY KEY AUTO_INCREMENT,
    area_name VARCHAR(200) NOT NULL
);


CREATE TABLE checkout_counter(
	counter_id INT PRIMARY KEY AUTO_INCREMENT
);


CREATE TABLE employee(
	employee_id INT PRIMARY KEY AUTO_INCREMENT,
    employee_name VARCHAR(200) NOT NULL,
    employee_type ENUM('store_manager', 'cashier', 'cleaner','warehouse_manager') NOT NULL,
    date_of_birth DATE NOT NULL,
    hourly_wage DOUBLE DEFAULT 0 CHECK (hourly_wage >= 0)
);


CREATE TABLE cashier(
	employee_id INT NOT NULL,
    counter_number INT,
    CONSTRAINT cashier_employee
		FOREIGN KEY (employee_id)
        REFERENCES employee(employee_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT cashier_counter
		FOREIGN KEY (counter_number)
        REFERENCES checkout_counter(counter_id)
        ON UPDATE CASCADE ON DELETE SET NULL    
);


CREATE TABLE store_manager(
	employee_id INT NOT NULL,
    CONSTRAINT store_manager_employee
		FOREIGN KEY (employee_id)
        REFERENCES employee(employee_id)
        ON UPDATE CASCADE ON DELETE CASCADE
);


CREATE TABLE warehouse_manager(
	employee_id INT NOT NULL,
    CONSTRAINT warehouse_manager_employee
		FOREIGN KEY (employee_id)
        REFERENCES employee(employee_id)
        ON UPDATE CASCADE ON DELETE CASCADE
);


CREATE TABLE cleaner(
	employee_id INT NOT NULL,
    area_id INT,
    CONSTRAINT cleaner_employee
		FOREIGN KEY (employee_id)
        REFERENCES employee(employee_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT cleaner_area
		FOREIGN KEY (area_id)
        REFERENCES store_area(area_id)
        ON UPDATE CASCADE ON DELETE SET NULL
);


CREATE TABLE category(
	category_name ENUM('Fruit & Vegetable', 'Meat & Seafood', 'Dairy & Egg') PRIMARY KEY
);


CREATE TABLE product(
	product_id INT PRIMARY KEY AUTO_INCREMENT,
    price DOUBLE NOT NULL CHECK (price >= 0),
    stock INT NOT NULL CHECK (stock >= 0),
    product_name VARCHAR(100) NOT NULL,
    category_name ENUM('Fruit & Vegetable', 'Meat & Seafood', 'Dairy & Egg'),
    area_id INT,
    warehouse_manager_id INT,
    CONSTRAINT product_category
		FOREIGN KEY (category_name)
        REFERENCES category(category_name)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT product_area
		FOREIGN KEY (area_id)
        REFERENCES store_area(area_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT product_warehouse_manager
		FOREIGN KEY (warehouse_manager_id)
        REFERENCES warehouse_manager(employee_id)
        ON UPDATE CASCADE ON DELETE SET NULL
);


CREATE TABLE order_contains_product(
	order_id INT NOT NULL,
    product_id INT NOT NULL,
    PRIMARY KEY (order_id, product_id),
    product_quantity INT NOT NULL CHECK (product_quantity > 0),
    CONSTRAINT contains_order_id
		FOREIGN KEY (order_id)
        REFERENCES customer_order(order_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT contains_product_id
		FOREIGN KEY (product_id)
        REFERENCES product(product_id)
        ON UPDATE CASCADE ON DELETE CASCADE
);


Insert into customer(email_address, customer_name, date_of_birth, grocery_dollar, points)
	Values('test_customer_1@gmail.com', 'John Doe', '1997-08-15', 100, 10);
Insert into customer(email_address, customer_name, date_of_birth, grocery_dollar, points)
	Values('test_customer_2@gmail.com', 'Jane Doe', '1963-01-21', 0, 200);
Insert into customer(email_address, customer_name, date_of_birth, grocery_dollar, points)
	Values('test_customer_3@gmail.com', 'Kevin Wang', '2001-09-14', 50, 50);
Insert into customer(email_address, customer_name, date_of_birth, grocery_dollar, points)
	Values('test_customer_4@gmail.com', 'Stephen King', '1973-6-5', 60, 357);    
    
select * from customer;


Insert into store_area(area_name)
	Values('Fruit & Vegetable');
Insert into store_area(area_name)
	Values('Meat & Seafood');
Insert into store_area(area_name)
	Values('Dairy & Egg');

select * from store_area;


Insert into checkout_counter
	Values();
Insert into checkout_counter
	Values();
Insert into checkout_counter
	Values();
    
select * from checkout_counter;    


Insert into category 
	Values('Fruit & Vegetable');
Insert into category
	Values('Meat & Seafood');
Insert into category
	Values('Dairy & Egg');

select * from category;


Insert into employee(employee_name, employee_type, date_of_birth, hourly_wage)
	Values('Sophie Li', 'store_manager', '1980-11-01', 60);
Insert into employee(employee_name, employee_type, date_of_birth, hourly_wage)
	Values('Jonathan Stewart', 'cashier', '1995-12-28', 25);
Insert into employee(employee_name, employee_type, date_of_birth, hourly_wage)
	Values('Nancy White', 'warehouse_manager', '1983-04-12', 55);
Insert into employee(employee_name, employee_type, date_of_birth, hourly_wage)
	Values('Sam Ellis', 'cleaner', '1970-02-26', 25);
Insert into employee(employee_name, employee_type, date_of_birth, hourly_wage)
	Values('Matt Collins', 'warehouse_manager', '1990-07-05', 55);
Insert into employee(employee_name, employee_type, date_of_birth, hourly_wage)
	Values('Emma Yu', 'store_manager', '1990-2-2', 60);

select * from employee;


Insert into store_manager
	Values(1);
Insert into store_manager
	Values(6);    
    
select * from store_manager;

Insert into cashier
	Values(2, 1);

select * from cashier;

Insert into cleaner
	Values(4, 1);
    
select * from cleaner;

Insert into warehouse_manager
	Values(3);
Insert into warehouse_manager
	Values(5);

select * from warehouse_manager;


Insert into product(price, stock, product_name, category_name, area_id, warehouse_manager_id)
	Values(10, 150, 'coconut milk', 'Dairy & Egg', 3, 3);
Insert into product(price, stock, product_name, category_name, area_id, warehouse_manager_id)
	Values(6.99, 230, 'strawberry', 'Fruit & Vegetable', 1, 3);
Insert into product(price, stock, product_name, category_name, area_id, warehouse_manager_id)
	Values(18.99, 358, 'beef short rib', 'Meat & Seafood', 2, 5);
Insert into product(price, stock, product_name, category_name, area_id, warehouse_manager_id)
	Values(7, 122, 'pear', 'fruit & vegetable', 1, 3);    
    
select * from product;


Insert into customer_order(order_date, total_amount, customer_id)
	Values('2022-12-08', 6.99, 1);
Insert into customer_order(order_date, total_amount, customer_id)
	Values('2022-12-01', 16.99, 2);  
Insert into customer_order(order_date, total_amount, customer_id)
	Values('2022-10-19', 18.99, 3);    
Insert into customer_order(order_date, total_amount, customer_id)
	Values('2022-11-22', 20.99, 4);     
    
select * from customer_order;


Insert into order_contains_product(order_id, product_id, product_quantity)
	Values(1, 2, 2);
Insert into order_contains_product(order_id, product_id, product_quantity)
	Values(2, 1, 1);
Insert into order_contains_product(order_id, product_id, product_quantity)
	Values(2, 2, 1);   
Insert into order_contains_product(order_id, product_id, product_quantity)
	Values(3, 3, 1);   
Insert into order_contains_product(order_id, product_id, product_quantity)
	Values(4, 4, 2);    
Insert into order_contains_product(order_id, product_id, product_quantity)
	Values(4, 2, 1);     
    
select * from order_contains_product;
