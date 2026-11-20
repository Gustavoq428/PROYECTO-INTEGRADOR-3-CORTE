-- Consultas para la evidencia en phpMyAdmin (base: gimnasio)

-- 1. Las tres tablas con sus datos
SELECT * FROM plans;
SELECT * FROM enrollments;
SELECT * FROM payments;

-- 2. Inscripciones con su plan (usa la llave foránea plan_id)
SELECT e.id, e.member_name, p.name AS plan, e.start_date, e.end_date, e.status
FROM enrollments e
JOIN plans p ON p.id = e.plan_id
ORDER BY e.id;

-- 3. Estado de cuenta: precio del plan, total pagado y saldo (usa plan_id y enrollment_id)
SELECT e.id, e.member_name, p.name AS plan, p.price,
       COALESCE(SUM(pay.amount), 0)           AS pagado,
       p.price - COALESCE(SUM(pay.amount), 0) AS saldo,
       e.status
FROM enrollments e
JOIN plans p ON p.id = e.plan_id
LEFT JOIN payments pay ON pay.enrollment_id = e.id
GROUP BY e.id, e.member_name, p.name, p.price, e.status
ORDER BY e.id;

-- 4. Ingresos por medio de pago
SELECT method, COUNT(*) AS pagos, SUM(amount) AS total
FROM payments
GROUP BY method;

-- 5. Llaves foráneas definidas
SELECT TABLE_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'gimnasio' AND REFERENCED_TABLE_NAME IS NOT NULL;
