-- FieldSync DB
-- Created for MVP with extensions for future features

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";



-- Users table for auth. and basic user management

CREATE TABLE users (
	id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
	email VARCHAR(255) UNIQUE NOT NULL,
	password_hash VARCHAR(255) NOT NULL,
	full_name VARCHAR(255),
	phone VARCHAR(20),
	is_active BOOLEAN DEFAULT true,
	created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Stores table with GPS coords and contact info

CREATE TABLE stores (
	id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
	name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(255),
    latitude DECIMAL(10, 8) NOT NULL, -- GPS coordinates
    longitude DECIMAL(11, 8) NOT NULL,
    store_type VARCHAR(100), -- For future survey targeting
    assigned_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Visits table for check-in/check-out tracking

CREATE TABLE visits (
	id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
	store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    check_in_time TIMESTAMP WITH TIME ZONE NOT NULL,
    check_out_time TIMESTAMP WITH TIME ZONE,
    duration_minutes INTEGER, -- calculated when checking out
    check_in_latitude DECIMAL(10, 8), -- GPS verification
    check_in_longitude DECIMAL(11, 8),
    check_out_latitude DECIMAL(10, 8),
    check_out_longitude DECIMAL(11, 8),
    notes TEXT, -- Flexible field for visit notes
    visit_purpose VARCHAR(100),
    compliance_status VARCHAR(50), -- "compliant", "non_compliant", "needs_attention"
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Photos table for multiple photos per visit

CREATE TABLE visit_photos (
	id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    visit_id UUID NOT NULL REFERENCES visits(id) ON DELETE CASCADE,
    photo_url VARCHAR(500) NOT NULL, -- Cloud storage URL or local path
    photo_type VARCHAR(100), -- "compliance", "merchandising", "general", "before", "after"
    caption TEXT,
    latitude DECIMAL(10, 8), -- GPS where photo was taken
    longitude DECIMAL(11, 8),
    file_size_kb INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Surveys table for dynamic questionnaires

CREATE TABLE surveys (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    survey_type VARCHAR(50) DEFAULT 'general', -- "store_type", "specific_deployment", "general"
    target_store_type VARCHAR(100), -- For store-type-based surveys
    is_active BOOLEAN DEFAULT true,
    start_date TIMESTAMP WITH TIME ZONE,
    end_date TIMESTAMP WITH TIME ZONE,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Survey questions for flexible question types

CREATE TABLE survey_questions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    survey_id UUID NOT NULL REFERENCES surveys(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL, -- "text", "multiple_choice", "yes_no", "rating", "photo"
    question_order INTEGER NOT NULL,
    is_required BOOLEAN DEFAULT false,
    options JSONB, -- For multiple choice options: ["Option 1", "Option 2", "Option 3"]
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Survey deployments for targeted distribution

CREATE TABLE survey_deployments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    survey_id UUID NOT NULL REFERENCES surveys(id) ON DELETE CASCADE,
    target_user_id UUID REFERENCES users(id), -- Specific user targeting
    target_store_id UUID REFERENCES stores(id), -- Specific store targeting
    deployment_type VARCHAR(50) NOT NULL, -- "user_specific", "store_specific", "store_type"
    status VARCHAR(50) DEFAULT 'pending', -- "pending", "completed", "expired"
    deployed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE
);

-- Survey responses for storing completed questionnaires

CREATE TABLE survey_responses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    survey_id UUID NOT NULL REFERENCES surveys(id) ON DELETE CASCADE,
    visit_id UUID REFERENCES visits(id) ON DELETE CASCADE, -- Link to visit if completed during visit
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    store_id UUID REFERENCES stores(id) ON DELETE CASCADE,
    responses JSONB NOT NULL, -- Store all Q&A pairs
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);


-- Sales data table for analytics integration

CREATE TABLE sales_data (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    sale_date DATE NOT NULL,
    sales_amount DECIMAL(12, 2) NOT NULL,
    units_sold INTEGER,
    product_category VARCHAR(100),
    sku VARCHAR(100), -- Stock Keeping Unit
    data_source VARCHAR(100), -- "pos_system", "manual_entry", "api_import"
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);




-- Primary lookup indexes

CREATE INDEX idx_stores_assigned_user ON stores(assigned_user_id);
CREATE INDEX idx_stores_location ON stores(latitude, longitude);
CREATE INDEX idx_visits_store_user ON visits(store_id, user_id);
CREATE INDEX idx_visits_check_in_time ON visits(check_in_time);
CREATE INDEX idx_visit_photos_visit ON visit_photos(visit_id);

-- Survey system indexes

CREATE INDEX idx_survey_deployments_user ON survey_deployments(target_user_id);
CREATE INDEX idx_survey_deployments_store ON survey_deployments(target_store_id);
CREATE INDEX idx_survey_responses_survey ON survey_responses(survey_id);
CREATE INDEX idx_survey_responses_user_store ON survey_responses(user_id, store_id);

-- Sales data indexes

CREATE INDEX idx_sales_data_store_date ON sales_data(store_id, sale_date);
CREATE INDEX idx_sales_data_date_range ON sales_data(sale_date);




-- Function to update the updated_at timestamp

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';



-- Apply triggers to relevant tables

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_stores_updated_at BEFORE UPDATE ON stores 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_visits_updated_at BEFORE UPDATE ON visits 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_surveys_updated_at BEFORE UPDATE ON surveys 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sales_data_updated_at BEFORE UPDATE ON sales_data 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();




-- Check constraints for data validation

ALTER TABLE stores ADD CONSTRAINT check_latitude_range 
    CHECK (latitude >= -90 AND latitude <= 90);

ALTER TABLE stores ADD CONSTRAINT check_longitude_range 
    CHECK (longitude >= -180 AND longitude <= 180);

ALTER TABLE visits ADD CONSTRAINT check_check_out_after_check_in 
    CHECK (check_out_time IS NULL OR check_out_time >= check_in_time);

ALTER TABLE visit_photos ADD CONSTRAINT check_photo_file_size 
    CHECK (file_size_kb > 0);

ALTER TABLE sales_data ADD CONSTRAINT check_sales_amount_positive 
    CHECK (sales_amount >= 0);

ALTER TABLE sales_data ADD CONSTRAINT check_units_sold_positive 
    CHECK (units_sold IS NULL OR units_sold >= 0);

-- Allow null for password for MVP

ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;

-- Insert sample user

INSERT INTO users (email, password_hash, full_name, phone) VALUES 
('demo@demo.com', 'hash_placeholder', 'Demo Johnson', '+5-555-5555');


--Sample data

DO $$
DECLARE
    demo_user_id UUID;
BEGIN
    SELECT id INTO demo_user_id FROM users WHERE email = 'demo@demo.com';
    
    -- Insert sample stores
    INSERT INTO stores (name, address, city, state, zip_code, phone, latitude, longitude, store_type, assigned_user_id) VALUES 
    ('SuperMart Downtown', '123 Main St', 'Demotown', 'CA', '90210', '+5-555-5556', 0.00, 0.00, 'supermarket', demo_user_id),
    ('QuickStop Plaza', '456 Oak Ave', 'Placeholder City', 'CA', '90211', '+5-555-5557', 0.00, 0.00, 'convenience', demo_user_id),
    ('MegaStore West', '789 Pine Blvd', 'Mockville', 'CA', '90212', '+5-555-5558', 0.00, 0.00, 'big_box', demo_user_id);
END $$;
