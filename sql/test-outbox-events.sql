-- 99-test-events.sql
DELETE FROM public.outbox_events;

INSERT INTO public.outbox_events (aggregatetype, aggregateid, type, payload) VALUES
('Order',   'ORD-1001', 'OrderCreated',
 '{
   "orderId": "ORD-1001",
   "customer": "alice@example.com",
   "total": 149.99,
   "items": [
     {"product": "Laptop", "qty": 1, "price": 1299.99},
     {"product": "Mouse",  "qty": 1, "price": 29.99}
   ]
 }'::jsonb),

('Order',   'ORD-1001', 'OrderShipped',
 '{
   "orderId": "ORD-1001",
   "trackingNumber": "1Z999AA10123456784",
   "carrier": "UPS"
 }'::jsonb),

('Payment', 'PAY-2001', 'PaymentProcessed',
 '{
   "paymentId": "PAY-2001",
   "orderId": "ORD-1001",
   "amount": 149.99,
   "method": "credit_card"
 }'::jsonb),

('Customer','CUST-3001','CustomerRegistered',
 '{
   "customerId": "CUST-3001",
   "email": "bob@example.com",
   "name": "Bob Johnson"
 }'::jsonb);