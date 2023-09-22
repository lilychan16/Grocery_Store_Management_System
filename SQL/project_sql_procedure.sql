USE grocery_store;

Delimiter $$
Create Procedure queryCustomerById(In customer_id_p int)
Begin
	Select *
	From customer
	Where customer_id = customer_id_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure queryCustomerByName(In customer_name_p varchar(100))
Begin
	Select *
	From customer
	Where customer_name = customer_name_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure queryProductByIdCustomerVersion(In product_id_p int)
Begin
	Select product_id, price, stock, product_name, category_name, area_id
	From product
	Where product_id = product_id_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure queryProductByIdEmployeeVersion(In product_id_p int)
Begin
	Select *
	From product
	Where product_id = product_id_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure updateProductStockByIdAfterAddToCart(In product_id_p int,
													  In quantity_p int)
Begin
	Update product 
	Set stock = stock - quantity_p
	Where product_id = product_id_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure putUnwantedProductQuantityBackInStock(In product_id_p int,
														In unwanted_quantity_p int)
Begin
	Update product
    Set stock = stock + unwanted_quantity_p
    Where product_id = product_id_p;
End $$
Delimiter ;    


Delimiter $$
Create Procedure queryProductByNameCustomerVersion(In product_name_p varchar(100))
Begin
	Select product_id, price, stock, product_name, category_name, area_id
	From product
	Where product_name = product_name_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure queryProductByNameEmployeeVersion(In product_name_p varchar(100))
Begin
	Select *
	From product
	Where product_name = product_name_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure updateCustomerDollarById(In customer_id_p int)
Begin
	Update customer
	Set grocery_dollar = grocery_dollar + (points Div 100), points = points % 100
	Where customer_id = customer_id_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure insertCustomerOrder(In total_amount_p double, In customer_id_p int)
Begin
	Insert into customer_order(order_date,total_amount,customer_id)  
	values(cast(sysdate() as date), total_amount_p, customer_id_p);
End $$
Delimiter ;


Delimiter $$
Create Procedure insertOrderContainsProduct(In order_id_p int, In product_id_p int, 
											In product_quantity_p int)
Begin
	Insert into order_contains_product 
	Values(order_id_p, product_id_p, product_quantity_p);
End $$
Delimiter ;


Delimiter $$
Create Procedure updateCustomerPointsById(In new_get_points_p int, 
											In customer_id_p int)
Begin
	Update customer
	Set points = points + new_get_points_p
	Where customer_id = customer_id_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure updateCustomerDollarsById(In new_applied_dollars_p double, 
											In customer_id_p int)
Begin
	Update customer
    Set grocery_dollar = grocery_dollar - new_applied_dollars_p
    Where customer_id = customer_id_p;
End $$
Delimiter ;    
	

Delimiter $$
Create Procedure getOrderIdByDateAmountCustomerId(In order_date_p date, In total_amount_p double, 
													In customer_id_p int)
Begin
	Select order_id
    From customer_order
    Where order_date = order_date_p AND total_amount = total_amount_p AND customer_id = customer_id_p;
End $$  
Delimiter ;  


Delimiter $$
Create Procedure getOrderAll()
Begin
	Select *
    From customer_order;
End $$
Delimiter ;  


Delimiter $$
Create Procedure getOrderByCustomerId(In customer_id_p int)
Begin
	Select *
    From customer_order
    Where customer_id = customer_id_p;
End $$
Delimiter ; 


Delimiter $$
Create Procedure getOrderByOrderId(In order_id_p int)
Begin
	Select *
    From customer_order
    Where order_id = order_id_p;
End $$
Delimiter ;    


Delimiter $$
Create Procedure getOrderProductsByOrderId(In order_id_p int)
Begin
	Select * 
    From order_contains_product
    Where order_id = order_id_p;
End $$
Delimiter ;    
    

Delimiter $$
Create Procedure queryEmployeeById(In employee_id_p int)
Begin
	Select *
	From employee
	Where employee_id = employee_id_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure queryEmployeeByName(In employee_name_p varchar(100))
Begin
	Select *
	From employee
	Where employee_name = employee_name_p;
End $$


Delimiter $$
Create Procedure queryEmployeeTypeById(In employee_id_p int)
Begin
	Select employee_type
	From employee
	Where employee_id = employee_id_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure queryEmployeeAll()
Begin
	Select *
	From employee;
End $$
Delimiter ;


Delimiter $$
Create Procedure queryStoreManagerAll()
Begin
	Select *
	From store_manager;
End $$
Delimiter ;


Delimiter $$
Create Procedure queryWarehouseManagerAll()
Begin
	Select *
	From warehouse_manager;
End $$
Delimiter ;


Delimiter $$
Create Procedure queryCashierAll()
Begin
	Select *
	From cashier;
End $$
Delimiter ;


Delimiter $$
Create Procedure queryCleanerAll()
Begin
	Select *
	From cleaner;
End $$
Delimiter ;


Delimiter $$
Create Procedure insertEmployee(In employee_name_p varchar(200), 
								In employee_type_p varchar(100), 
                                In date_of_birth_p date,
                                In hourly_wage_p double)
Begin
	If hourly_wage_p < 0 then
		Set hourly_wage_p = 0;
    End If;    
	Insert into employee(employee_name, employee_type, date_of_birth, hourly_wage)
	Values(employee_name_p, employee_type_p, date_of_birth_p, hourly_wage_p);
End $$
Delimiter ;


Delimiter $$
Create Procedure getEmployeeIdByNameAndBirthDate(In employee_name_p varchar(200),
												In date_of_birth_p DATE)
Begin
	Select employee_id
    From employee
    Where employee_name = employee_name_p AND date_of_birth = date_of_birth_p;
End $$
Delimiter ;    


Delimiter $$
Create Procedure deleteEmployeeById(In employee_id_p int)
Begin
	Delete
    From employee
    Where employee_id = employee_id_p;
End $$
Delimiter ;    


Delimiter $$
Create Procedure insertCashier(In employee_id_p int, In counter_number_p int)
Begin
	Insert into cashier
	Values(employee_id_p, counter_number_p);
End $$
Delimiter ;


Delimiter $$
Create Procedure insertStoreManager(In employee_id_p int)
Begin
	Insert into store_manager
	Values(employee_id_p);
End $$
Delimiter ;


Delimiter $$
Create Procedure insertWarehouseManager(In employee_id_p int)
Begin
	Insert into warehouse_manager
	Values(employee_id_p);
End $$
Delimiter ;


Delimiter $$
Create Procedure insertCleaner(In employee_id_p int, In area_id_p int)
Begin
	Insert into cleaner
	Values(employee_id_p, area_id_p);
End $$
Delimiter ;


Delimiter $$
Create Procedure deleteCashierById(In employee_id_p int)
Begin
	Delete 
	From cashier
	Where employee_id = employee_id_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure deleteCleanerById(In employee_id_p int)
Begin
	Delete 
	From cleaner
	Where employee_id = employee_id_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure deleteWarehouseManagerById(In employee_id_p int)
Begin
	Delete 
	From warehouse_manager
	Where employee_id = employee_id_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure queryCustomerAll()
Begin
	Select *
	From customer;
End $$
Delimiter ;


Delimiter $$
Create Procedure insertCustomer(In email_address_p varchar(200), 
								In customer_name_p varchar(200), 
                                In date_of_birth_p date,
                                In points_p int)
Begin
	If points_p < 0 then
		Set points_p = 0;
    End If;
	Insert into customer(email_address, customer_name, date_of_birth, points)
	Values(email_address_p, customer_name_p, date_of_birth_p, points_p);
End $$
Delimiter ;


Delimiter $$
Create Procedure deleteCustomerById(In customer_id_p int)
Begin
	Delete 
	From customer
	Where customer_id = customer_id_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure queryProductAll()
Begin
	Select * 
	From product;
End $$
Delimiter ;

call queryProductAll();


Delimiter $$
Create Procedure insertProduct(In price_p double, In stock_p int, In product_name_p varchar(100),
	In category_name_p varchar(100), In area_id_p int, In warehouse_manager_id_p int)
Begin
	If price_p < 0 then
		Set price_p = 0;
    End If;
    If stock_p < 0 then
		Set stock_p = 0;
    End if;    
	Insert into product(price, stock, product_name, 
		category_name, area_id, warehouse_manager_id)
	Values(price_p, stock_p, product_name_p, 
		category_name_p, area_id_p, warehouse_manager_id_p);
End $$
Delimiter ;


Delimiter $$
Create Procedure updateProductPriceById(In product_id_p int, In price_p double)
Begin
	Set @price = price_p;
    If @price < 0 Then
		Set @price = 0;
    End If;
	Update product 
	Set price = @price 
	Where product_id = product_id_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure updateProductStockById(In product_id_p int, In stock_p int)
Begin
	Set @stock = stock_p;
    If @stock < 0 Then
		Set @stock = 0;
    End If;
	Update product 
	set stock = @stock 
	Where product_id = product_id_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure deleteProductById(In product_id_p int)
Begin
	Delete 
	From product
	Where product_id = product_id_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure insertStoreAreaByName(In area_name_p varchar(200))
Begin
	Insert store_area(area_name)
    Values (area_name_p);
End $$
Delimiter ;


Delimiter $$
Create Procedure updateStoreAreaById(In area_id_p int, In area_name_p varchar(200))
Begin
	Update store_area
	Set area_name = area_name_p
	Where area_id = area_id_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure queryAreaAll()
Begin
	Select * 
	From store_area;
End $$
Delimiter ;


Delimiter $$
Create Procedure queryCategoryAll()
Begin
	Select * 
	From category;
End $$
Delimiter ;


Delimiter $$
Create Procedure queryEmployeeSpecialById(In employee_id_p int)
Begin
	Select e.employee_name, e.employee_type, ca.counter_number, cl.area_id, sa.area_name
	From employee e
	Left Join cashier ca On e.employee_id = ca.employee_id
	Left Join cleaner cl On e.employee_id = cl.employee_id
    Left Join store_area sa On cl.area_id = sa.area_id
	Where e.employee_id = employee_id_p;
End $$
Delimiter ;


Delimiter $$
Create Procedure getAllCounterId()
Begin
	Select counter_id
    From checkout_counter;
End $$
Delimiter ;    


