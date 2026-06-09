USE officesync;

UPDATE users
SET role = 'Employee'
WHERE role = 'Department Head';
