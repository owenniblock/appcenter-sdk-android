package com.microsoft.appcenter.storage;

import com.microsoft.appcenter.utils.storage.SharedPreferencesManager;
import com.microsoft.appcenter.storage.models.TokenResult;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

/**
 * Token cache service
 */
public class TokenManager {
    private static TokenManager tInstance;

    private TokenManager() {
    }

    public static TokenManager getInstance() {
        if (tInstance == null) {
            tInstance = new TokenManager();
        }
        return tInstance;
    }

    /**
     * List all cached tokens' partition names.
     *
     * @return set of cached tokens' partition name.
     */
    public Set<String> getPartitionNames() {
        Set<String> partitionNames = SharedPreferencesManager.getStringSet(Constants.PARTITION_NAMES);
        return partitionNames == null ? new HashSet<String>() : partitionNames;
    }

    /**
     * Get the cached token access to given partition.
     *
     * @param partitionName
     * @return Cached token.
     */
    public TokenResult getCachedToken(String partitionName) {
        TokenResult token = Utils.sGson.fromJson(SharedPreferencesManager.getString(partitionName), TokenResult.class);
        if (token != null) {
            Calendar aGMTCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

            /* The token is considered expired. */
            if (aGMTCalendar.getTime().compareTo(token.expiresOn()) > 0) {
                removeCachedToken(partitionName);
                return null;
            }
        }
        return token;
    }

    /**
     * Set the token to cache.
     *
     * @param tokenResult
     */
    public synchronized void setCachedToken(TokenResult tokenResult) {
        Set<String> partitionNamesSet = getPartitionNames();
        if (!partitionNamesSet.contains(tokenResult.partition())) {
            partitionNamesSet.add(tokenResult.partition());
            SharedPreferencesManager.putStringSet(Constants.PARTITION_NAMES, partitionNamesSet);
        }
        SharedPreferencesManager.putString(tokenResult.partition(), Utils.sGson.toJson(tokenResult));
    }

    /**
     * Remove the cached token access to specific partition.
     *
     * @param partitionName
     */
    public synchronized void removeCachedToken(String partitionName) {
        Set<String> partitionNamesSet = getPartitionNames();
        partitionNamesSet.remove(partitionName);
        SharedPreferencesManager.putStringSet(Constants.PARTITION_NAMES, partitionNamesSet);
        SharedPreferencesManager.remove(partitionName);
    }
}
