package com.tristar.wind.util;

import org.apache.log4j.Logger;

import com.ptc.core.lwc.server.PersistableAdapter;
import com.ptc.core.meta.common.DisplayOperationIdentifier;

import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.session.SessionHelper;
import wt.util.WTException;

public class IBAHolderUtility {
	
	private static String CLASSNAME = IBAHolderUtility.class.getName();
	private final static Logger logger = LogR.getLogger(IBAHolderUtility.class.getName());
	
	public static Object getObjectAttribute(WTObject pbo, String attribute) throws WTException
	{
		PersistableAdapter object = new PersistableAdapter(null, SessionHelper.getLocale(),new DisplayOperationIdentifier());
		object.load(attribute);
		Object value = object.get(attribute);
		return value;
		
	}
	
	public static String getSingleValueAttribute(WTObject pbo, String attribute) throws WTException
	{
		Object prod = null;
		logger.info("Begin getSingleValueAttribute");
		try{
		prod = IBAHolderUtility.getObjectAttribute(pbo, attribute);
		}
		catch(Exception e)
		{
			logger.error("Exiting getSingleValueAttribute with Errors", e);
			e.printStackTrace();
			return null;
		}
		if (prod instanceof Object[]){
			System.out.println("== Multi-Value");
			throw new WTException("Error, single value attribute requested");
		}
		logger.info("Single Value");
		logger.info("Exiting with value equals: " + prod);
		return (String)prod;
	}
	
	public static boolean isAttributeEqual(WTObject pbo, String att, String condition) throws WTException
	{
		logger.info("Is attribute " + att + " equal to" + condition);
		String result = null;
		condition = condition.trim();
		condition = condition.toUpperCase();
		try {
			result = IBAHolderUtility.getSingleValueAttribute(pbo, att);
			if(result == null)
			{
				WTException e = new WTException("Unable to determine value of:" + att);
				throw e;
			}
			result = result.trim();
			result = result.toUpperCase();
		} catch (WTException e) {
			logger.error("Unable to get value of " + att, e);
			e.printStackTrace();
			
		}
		if(result.equals(condition))
		{
			logger.info("Returning " + result);
			return true;
		}
		else
		{
			logger.info("Proceeding");
			return false;
		}
	}

}
