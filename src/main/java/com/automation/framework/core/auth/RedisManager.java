package com.automation.framework.core.auth;

import com.automation.framework.core.config.ApiConfig;
import com.automation.framework.core.interfaces.LoggingInterface;
import com.automation.framework.core.logging.ApiLogger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * RedisManager handles Redis connections and OTP retrieval for authentication
 * Follows the singleton pattern and integrates with the framework's configuration system
 */
public class RedisManager implements Closeable {
    private static final LoggingInterface logger = new ApiLogger(RedisManager.class);
    private static RedisManager instance;
    private static final Object lock = new Object();
    
    private JedisPool jedisPool;
    private final ApiConfig apiConfig;
    
    /**
     * Check if OTP mock is enabled in configuration
     */
    private boolean isMockOtpEnabled() {
        return Boolean.parseBoolean(apiConfig.getProperty("auth.otp.mock", "false"));
    }
    
    /**
     * Private constructor - use getInstance() to get the singleton instance
     */
    private RedisManager() {
        this.apiConfig = new ApiConfig();
        // Only initialize Redis if OTP mock is disabled
        if (!isMockOtpEnabled()) {
            initializeConnectionPool();
        } else {
            logger.logInfo("Redis initialization skipped - OTP mock is enabled (auth.otp.mock=true)");
        }
    }
    
    /**
     * Get singleton instance of RedisManager
     */
    public static RedisManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new RedisManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Initialize Redis connection pool using configuration
     */
    private void initializeConnectionPool() {
        String redisHost = apiConfig.getProperty("redis.host", "localhost");
        int redisPort = Integer.parseInt(apiConfig.getProperty("redis.port", "6379"));
        int timeout = Integer.parseInt(apiConfig.getProperty("redis.timeout", "5000"));
        
        try {
            // Configure connection pool
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(Integer.parseInt(apiConfig.getProperty("redis.connection.pool.max.total", "8")));
            poolConfig.setMaxIdle(Integer.parseInt(apiConfig.getProperty("redis.connection.pool.max.idle", "8")));
            poolConfig.setMinIdle(Integer.parseInt(apiConfig.getProperty("redis.connection.pool.min.idle", "0")));
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);
            
            // Create connection pool
            jedisPool = new JedisPool(poolConfig, redisHost, redisPort, timeout);
            
            // Test connection
            try (Jedis jedis = jedisPool.getResource()) {
                String pong = jedis.ping();
                logger.logInfo("Redis connection established successfully. Response: " + pong);
                logger.logInfo("Redis server info - Host: " + redisHost + ", Port: " + redisPort + ", Timeout: " + timeout + "ms");
            }
            
        } catch (Exception e) {
            logger.logError("Failed to initialize Redis connection to " + redisHost + ":" + redisPort + " - Redis operations will fall back to mock values",e);
            
            // Clean up failed connection pool
            if (jedisPool != null && !jedisPool.isClosed()) {
                jedisPool.close();
            }
            jedisPool = null;
            
            // Don't throw exception - allow graceful fallback to mock OTP
            logger.logInfo("Redis initialization failed but framework will continue with fallback mock OTP support");
        }
    }
    
    /**
     * Get Redis connection from pool
     */
    public Jedis getConnection() {
        if (jedisPool == null) {
            if (isMockOtpEnabled()) {
                logger.logDebug("Redis connection requested but mock OTP is enabled - Redis operations will be bypassed");
            } else {
                logger.logDebug("Redis connection requested but pool is not available - falling back to mock OTP");
            }
            return null;
        }
        return jedisPool.getResource();
    }
    
    /**
     * Connect to specific Redis database
     */
    public void selectDatabase(Jedis connection, int database) {
        if (connection != null) {
            connection.select(database);
            logger.logDebug("Selected Redis database: " + database);
        }
    }
    
    /**
     * Get OTP from Redis using the configured key pattern
     * Follows the same pattern as the original cucumber-api framework
     */
    public String getOtp(String loginId) {
        String keyPrefix = apiConfig.getProperty("redis.otp.key.prefix", "onetimepasswordsixdigit:v2:");
        String redisKey = keyPrefix + loginId;
        
        return getOtpByKey(redisKey);
    }
    
    /**
     * Get OTP from Redis by exact key
     */
    public String getOtpByKey(String redisKey) {
        // Check if mock OTP is enabled
        if (isMockOtpEnabled()) {
            String mockOtp = apiConfig.getProperty("auth.otp.mock.value", "123456");
            logger.logDebug("Using mock OTP instead of Redis - Key: " + redisKey + ", Mock OTP: " + mockOtp);
            return mockOtp;
        }
        
        int database = Integer.parseInt(apiConfig.getProperty("redis.database", "0"));
        int extractStart = Integer.parseInt(apiConfig.getProperty("redis.otp.extract.start", "6"));
        int extractEnd = Integer.parseInt(apiConfig.getProperty("redis.otp.extract.end", "13"));
        
        try (Jedis connection = getConnection()) {
            if (connection == null) {
                logger.logWarning("Redis connection is null, falling back to mock OTP");
                String fallbackOtp = apiConfig.getProperty("auth.otp.mock.value", "123456");
                logger.logDebug("Using fallback mock OTP: " + fallbackOtp);
                return fallbackOtp;
            }
            
            selectDatabase(connection, database);
            
            String value = connection.get(redisKey);
            if (value != null && value.length() >= extractEnd) {
                String otp = value.substring(extractStart, extractEnd);
                logger.logDebug("Retrieved OTP from Redis - Key: " + redisKey + ", OTP: " + otp);
                return otp;
            } else {
                logger.logWarning("OTP not found or invalid format in Redis for key: " + redisKey);
                return null;
            }
            
        } catch (JedisException e) {
            logger.logError("Redis error while retrieving OTP for key: " + redisKey, e);
            return null;
        } catch (Exception e) {
            logger.logError("Unexpected error while retrieving OTP for key: " + redisKey, e);
            return null;
        }
    }
    
    /**
     * Get raw value from Redis
     */
    public String getValue(String key) {
        return getValue(key, 0);
    }
    
    /**
     * Get raw value from Redis with database selection
     */
    public String getValue(String key, int database) {
        try (Jedis connection = getConnection()) {
            selectDatabase(connection, database);
            String value = connection.get(key);
            logger.logDebug("Retrieved value from Redis - Key: " + key + ", Database: " + database);
            return value;
        } catch (JedisException e) {
            logger.logError("Redis error while retrieving value for key: " + key, e);
            return null;
        } catch (Exception e) {
            logger.logError("Unexpected error while retrieving value for key: " + key, e);
            return null;
        }
    }
    
    /**
     * Set value in Redis
     */
    public boolean setValue(String key, String value) {
        return setValue(key, value, 0);
    }
    
    /**
     * Set value in Redis with database selection
     */
    public boolean setValue(String key, String value, int database) {
        try (Jedis connection = getConnection()) {
            selectDatabase(connection, database);
            String result = connection.set(key, value);
            boolean success = "OK".equals(result);
            logger.logDebug("Set value in Redis - Key: " + key + ", Database: " + database + ", Success: " + success);
            return success;
        } catch (JedisException e) {
            logger.logError("Redis error while setting value for key: " + key, e);
            return false;
        } catch (Exception e) {
            logger.logError("Unexpected error while setting value for key: " + key, e);
            return false;
        }
    }
    
    /**
     * Delete key from Redis
     */
    public boolean deleteKey(String key) {
        return deleteKey(key, 0);
    }
    
    /**
     * Delete key from Redis with database selection
     */
    public boolean deleteKey(String key, int database) {
        try (Jedis connection = getConnection()) {
            selectDatabase(connection, database);
            Long result = connection.del(key);
            boolean success = result > 0;
            logger.logDebug("Deleted key from Redis - Key: " + key + ", Database: " + database + ", Success: " + success);
            return success;
        } catch (JedisException e) {
            logger.logError("Redis error while deleting key: " + key, e);
            return false;
        } catch (Exception e) {
            logger.logError("Unexpected error while deleting key: " + key, e);
            return false;
        }
    }
    
    /**
     * Delete OTP limit keys for a login ID to avoid rate limiting
     * Follows the same cleanup pattern as the original framework
     */
    public void deleteOtpLimit(String loginId) {
        deleteOtpLimit(loginId, 0);
    }
    
    /**
     * Delete OTP limit keys for a login ID with database selection
     */
    public void deleteOtpLimit(String loginId, int database) {
        try (Jedis connection = getConnection()) {
            selectDatabase(connection, database);
            
            // Find all keys containing the loginId
            Set<String> keys = connection.keys("*" + loginId + "*");
            
            if (!keys.isEmpty()) {
                String[] keyArray = keys.toArray(new String[0]);
                Long deletedCount = connection.del(keyArray);
                logger.logInfo("Deleted " + deletedCount + " OTP limit keys for loginId: " + loginId);
            } else {
                logger.logDebug("No OTP limit keys found for loginId: " + loginId);
            }
            
        } catch (JedisException e) {
            logger.logError("Redis error while deleting OTP limit keys for loginId: " + loginId, e);
        } catch (Exception e) {
            logger.logError("Unexpected error while deleting OTP limit keys for loginId: " + loginId, e);
        }
    }
    
    /**
     * Update Redis hash value (for complex data structures)
     */
    public boolean updateHashValue(String key, Map<String, String> hashValues) {
        return updateHashValue(key, hashValues, 0);
    }
    
    /**
     * Update Redis hash value with database selection
     */
    public boolean updateHashValue(String key, Map<String, String> hashValues, int database) {
        try (Jedis connection = getConnection()) {
            selectDatabase(connection, database);
            
            String result = connection.hmset(key, hashValues);
            boolean success = "OK".equals(result);
            logger.logDebug("Updated hash in Redis - Key: " + key + ", Database: " + database + ", Fields: " + hashValues.size() + ", Success: " + success);
            return success;
            
        } catch (JedisException e) {
            logger.logError("Redis error while updating hash for key: " + key, e);
            return false;
        } catch (Exception e) {
            logger.logError("Unexpected error while updating hash for key: " + key, e);
            return false;
        }
    }
    
    /**
     * Check if Redis connection is healthy
     */
    public boolean isHealthy() {
        try (Jedis connection = getConnection()) {
            String pong = connection.ping();
            return "PONG".equals(pong);
        } catch (Exception e) {
            logger.logError("Redis health check failed",e);
            return false;
        }
    }
    
    /**
     * Get Redis connection info
     */
    public String getConnectionInfo() {
        if (jedisPool != null) {
            return String.format("Redis Pool - Active: %d, Idle: %d, Total: %d", 
                               jedisPool.getNumActive(), 
                               jedisPool.getNumIdle(), 
                               jedisPool.getNumActive() + jedisPool.getNumIdle());
        }
        return "Redis connection pool not initialized";
    }
    
    /**
     * Close the Redis connection pool
     */
    @Override
    public void close() throws IOException {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            logger.logInfo("Redis connection pool closed");
        }
    }
    
    /**
     * Cleanup method for testing - resets singleton instance
     * Should only be used in test scenarios
     */
    public static void resetInstance() {
        synchronized (lock) {
            if (instance != null) {
                try {
                    instance.close();
                } catch (IOException e) {
                    logger.logError("Error closing Redis manager during reset",e);
                }
                instance = null;
            }
        }
    }
}