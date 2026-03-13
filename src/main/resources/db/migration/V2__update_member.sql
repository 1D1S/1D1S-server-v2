ALTER TABLE member DROP INDEX UKmbmcqelty0fbrvxp1q58dn57t;

ALTER TABLE member ADD CONSTRAINT uk_email_signup_route UNIQUE (email, signup_route);
