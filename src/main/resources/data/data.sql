INSERT INTO users (id, user_id, first_name, last_name, email, phone, bio,
                   reference_id, image_url, created_by, updated_by, created_at, updated_at, account_non_expired,
                   account_non_locked, enabled, mfa)
VALUES (0, '023a7479-e7a7-079f-3ae5-a766fe25eca9', 'System', 'System', 'system@gmail.com', '1234567890',
        'This is not a user but the system itself',
        '897f3898-31ed-654a-eb93-531648a0db88', 'https://cdn-icons-png.flaticon.com/128/2911/2911833.png', 0, 0,
        '2024-06-02 22:10:47.725642', '2024-06-02 22:10:47.725642', TRUE, TRUE, FALSE, FALSE);