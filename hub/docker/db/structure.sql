CREATE TABLE processed_agent_data (
    id SERIAL PRIMARY KEY,
    road_state VARCHAR(255) NOT NULL,
    weather_state VARCHAR(255),
    light_state VARCHAR(255),
    user_id INTEGER NOT NULL,
    x FLOAT,
    y FLOAT,
    z FLOAT,
    latitude FLOAT,
    longitude FLOAT,
    temperature FLOAT,
    humidity FLOAT,
    precipitation FLOAT,
    lux FLOAT,
    light_on BOOLEAN,
    timestamp TIMESTAMP
);
