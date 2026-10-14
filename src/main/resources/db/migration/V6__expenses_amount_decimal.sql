-- Les montants étaient stockés en INT : 11,99 € devenait 11 €.
ALTER TABLE expenses MODIFY amount DECIMAL(10,2);
