package CacheSystem; 

import java.util.ArrayList;
import java.util.Date;
import java.util.SortedMap;

import CacheSystem.util.ValueObj;
import CacheSystem.util.CacheCleaner;
import java.util.Hashtable;
import java.util.TreeMap;

public class CacheSystem {
	Hashtable<String, ValueObj> dataStore;
	TreeMap<Long, ArrayList<String>> expiryIndex;
	
	CacheCleaner cleaner;
	public CacheSystem (int initialCapacity, float loadFactor) {
		dataStore = new Hashtable<String, ValueObj>(initialCapacity, loadFactor);
		expiryIndex = new TreeMap<Long, ArrayList<String>>  ();
		cleaner = new CacheCleaner ();
		cleaner.setDataStore(dataStore);
		cleaner.setExpiryIndex(expiryIndex);
		cleaner.setDaemon(true);
		cleaner.start();
	}
	
	public CacheSystem () {
		dataStore   = new Hashtable<String, ValueObj> ();
		expiryIndex = new TreeMap<Long, ArrayList<String>>  ();
		cleaner = new CacheCleaner ();
		cleaner.setDataStore(dataStore);
		cleaner.setExpiryIndex(expiryIndex);
		cleaner.start();
	}
	
	public Object get (String key) throws Exception {
		if (dataStore.isEmpty()) {
			throw (new Exception ("No data stored at this time."));
		}
		else {
			ValueObj vo = dataStore.get(key); 
			if (vo!=null && !vo.isExpired()) {
				return vo;
			}
			else
				throw (new Exception ("Object does not exist/expired."));
		}
	}
	
	public synchronized void delete (String key) throws Exception {
		if (!dataStore.isEmpty()) {
			throw (new Exception ("No data stored at this time."));
		}
		else {
			dataStore.remove(key);
		}
	}
	
	public synchronized void add (String key, Object obj, long inputTTL) throws Exception {
		long timeToLive = System.currentTimeMillis() + (inputTTL * 1000);
		if (dataStore.containsKey(key)) {
			delete (key);
		}
		// Convert input TTL (in seconds) to milliseconds.
		ValueObj vo = new ValueObj(obj, timeToLive); 
		dataStore.put (key, vo);
		ArrayList<String> keys = expiryIndex.get (timeToLive);
		
		if (keys == null)
			keys = new ArrayList<String> (); 

		keys.add(key);
		System.out.println ("Adding: ("+key+", "+inputTTL+"s ["+timeToLive+" ms])");
		expiryIndex.put (timeToLive, keys);
	}
}
