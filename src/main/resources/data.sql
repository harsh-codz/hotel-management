
INSERT INTO role (name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO role (name) VALUES ('ROLE_STAFF') ON CONFLICT (name) DO NOTHING;
INSERT INTO role (name) VALUES ('ROLE_CUSTOMER') ON CONFLICT (name) DO NOTHING;


INSERT INTO room_type (name, description) VALUES ('Single', 'A room assigned to one person.') ON CONFLICT (name) DO NOTHING;
INSERT INTO room_type (name, description) VALUES ('Double', 'A room assigned to two people.') ON CONFLICT (name) DO NOTHING;
INSERT INTO room_type (name, description) VALUES ('Suite', 'A larger room or connected rooms.') ON CONFLICT (name) DO NOTHING;
INSERT INTO room_type (name, description) VALUES ('Deluxe', 'A room with additional amenities.') ON CONFLICT (name) DO NOTHING;
INSERT INTO room_type (name, description) VALUES ('Supreme', 'Highest category of rooms.') ON CONFLICT (name) DO NOTHING;


INSERT INTO amenity (name) VALUES ('Wi-Fi') ON CONFLICT (name) DO NOTHING;
INSERT INTO amenity (name) VALUES ('TV') ON CONFLICT (name) DO NOTHING;
INSERT INTO amenity (name) VALUES ('Mini-Bar') ON CONFLICT (name) DO NOTHING;
INSERT INTO amenity (name) VALUES ('Air Conditioning') ON CONFLICT (name) DO NOTHING;
INSERT INTO amenity (name) VALUES ('En Suite Bathroom') ON CONFLICT (name) DO NOTHING;


INSERT INTO service (name, default_price) VALUES ('Room Service', 25.00) ON CONFLICT (name) DO NOTHING;
INSERT INTO service (name, default_price) VALUES ('Spa Treatment', 150.00) ON CONFLICT (name) DO NOTHING;
INSERT INTO service (name, default_price) VALUES ('Laundry Service', 10.00) ON CONFLICT (name) DO NOTHING;
INSERT INTO service (name, default_price) VALUES ('Late Checkout Fee', 50.00) ON CONFLICT (name) DO NOTHING;
INSERT INTO service (name, default_price) VALUES ('Restaurant Dining', 0.00) ON CONFLICT (name) DO NOTHING; 