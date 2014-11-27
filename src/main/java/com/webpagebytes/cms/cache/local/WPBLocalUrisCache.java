package com.webpagebytes.cms.cache.local;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.webpagebytes.cms.cache.WPBUrisCache;
import com.webpagebytes.cms.cmsdata.WPBFile;
import com.webpagebytes.cms.cmsdata.WPBUri;
import com.webpagebytes.cms.datautility.WPBAdminDataStorage;
import com.webpagebytes.cms.datautility.WPBAdminDataStorageFactory;
import com.webpagebytes.cms.exception.WPBIOException;

public class WPBLocalUrisCache implements WPBUrisCache {

	private static final Object lock = new Object();
	private WPBAdminDataStorage dataStorage;
	Map<Integer, Map<String, WPBUri>> localCache;
	long cacheFingerPrint;
	
	public WPBLocalUrisCache()
	{
		dataStorage = WPBAdminDataStorageFactory.getInstance();
		try
		{
			if (dataStorage != null)
			{
				Refresh();
			}
		} catch (WPBIOException e)
		{
			
		}
	}
	public WPBUri getByExternalKey(String key) throws WPBIOException
	{
		return null;
	}
	
	public WPBUri get(String uri, int httpIndex) throws WPBIOException
	{
		if (localCache != null)
		{
			Map<String, WPBUri> uris = localCache.get(httpIndex);
			if (uris != null)
			{
				return uris.get(uri);
			}
		}
		return null;
	}

	public Set<String> getAllUris(int httpIndex) throws WPBIOException
	{
		if (localCache != null)
		{
			Map<String, WPBUri> uris = localCache.get(httpIndex);
			if (uris != null)
			{
				return uris.keySet();
			}
		}
		return new HashSet<String>();
	}
	
	public Long getCacheFingerPrint()
	{
		return cacheFingerPrint;
	}
	
	public int httpToOperationIndex(String httpOperation)
	{
		if (httpOperation.toUpperCase().equals("GET"))
		{
			return HTTP_GET_INDEX;
		} else if (httpOperation.toUpperCase().equals("POST"))
		{
			return HTTP_POST_INDEX;
		} else if (httpOperation.toUpperCase().equals("PUT"))
		{
			return HTTP_PUT_INDEX;
		} else if (httpOperation.toUpperCase().equals("DELETE"))
		{
			return HTTP_DELETE_INDEX;
		}
		return -1;	
	}
	public String indexOperationToHttpVerb(int httpIndex)
	{
		if (httpIndex == WPBUrisCache.HTTP_GET_INDEX)
		{
			return "GET";
		} else if (httpIndex == WPBUrisCache.HTTP_POST_INDEX)
		{
			return "POST";
		} else if (httpIndex == WPBUrisCache.HTTP_PUT_INDEX)
		{
			return "PUT";
		} else if (httpIndex == WPBUrisCache.HTTP_DELETE_INDEX)
		{
			return "DELETE";
		}
		return null;
	}

	public void Refresh() throws WPBIOException {
		synchronized (lock)
		{
			Map<Integer, Map<String, WPBUri>> tempMapUris = new HashMap<Integer, Map<String, WPBUri>>();
			
			List<WPBUri> recList = dataStorage.getAllRecords(WPBUri.class);
			for(WPBUri item: recList)
			{
				int httpIndex = httpToOperationIndex(item.getHttpOperation().toUpperCase());
				if (httpIndex >=0)
				{
					Map<String, WPBUri> aMap = tempMapUris.get(httpIndex);
					if (aMap == null)
					{
						aMap = new HashMap<String, WPBUri>();
						tempMapUris.put(httpIndex, aMap);
					}
					aMap.put(item.getUri(), item);
				}			
			}
			localCache = tempMapUris;
			Random r = new Random();
			cacheFingerPrint = r.nextLong();
		}

		
	}

}