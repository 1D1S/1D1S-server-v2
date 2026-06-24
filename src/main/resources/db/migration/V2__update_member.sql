ALTER TABLE member DROP CONSTRAINT IF EXISTS ukmbmcqelty0fbrvxp1q58dn57t;

ALTER TABLE member ADD CONSTRAINT uk_email_signup_route UNIQUE (email, signup_route);
