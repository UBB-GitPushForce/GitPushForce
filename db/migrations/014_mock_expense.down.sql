DELETE FROM expenses
WHERE (user_id, title, amount) IN (
  (1, 'Cumpărături supermarket - Mega', 120.50),
  (1, 'Cafea & mic dejun - Bistró', 8.90),
  (1, 'Plin rezervor - OMV', 43.25),
  (1, 'Geacă toamnă', 230.00),
  (1, 'Abonament transport (lunar)', 19.75),
  (1, 'Cina aniversară la restaurant', 310.10),

  (2, 'Factura energie - septembrie', 45.00),
  (2, 'Cadou 30 ani prietenă', 78.90),
  (2, 'Comandă haine online', 155.20),
  (2, 'Mâncare pentru pisică (sac 10kg)', 99.99),
  (2, 'Abonament sală (lunar)', 205.75),
  (2, 'Taxi aeroport', 62.30),

  (3, 'Revizie & schimb ulei', 310.40),
  (3, 'Cafea zilnică - abonament', 28.99),
  (3, 'Benzină drum lung', 150.00),
  (3, 'Bilet avion CLUJ - LONDRA', 490.75),
  (3, 'Snack-uri birou', 12.10),
  (3, 'Abonament streaming (anual)', 29.99),

  (4, 'Produse curățenie - supermarket', 25.00),
  (4, 'Rochie eveniment', 132.50),
  (4, 'Prânz la birou (5 zile)', 78.40),
  (4, 'Uber seară', 49.99),
  (4, 'Set cosmetice îngrijire', 312.60),
  (4, 'Reîncărcare credit mobil', 95.10),

  (5, 'Abonament internet (6 luni)', 210.00),
  (5, 'Cartuș imprimantă + hârtie', 67.45),
  (5, 'Escapadă weekend (hotel)', 189.90),
  (5, 'Cina business la restaurant', 430.20),
  (5, 'Cadouri Crăciun familie', 120.75),
  (5, 'Cafea specialitate (zi)', 5.55
);
