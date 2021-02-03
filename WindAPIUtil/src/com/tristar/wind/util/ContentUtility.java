package com.tristar.wind.util;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentRoleType;
import wt.doc.WTDocument;
import wt.fc.QueryResult;
import wt.log4j.LogR;
import wt.util.WTException;

public class ContentUtility {
	
	private static String CLASSNAME = DocumentUtility.class.getName();
	private final static Logger logger = LogR.getLogger(DocumentUtility.class.getName());
	
	public static ApplicationData getPrimaryContent(WTDocument doc) throws WTException {
		QueryResult qr;
		try {
			if (doc != null ) {
				qr = ContentHelper.service.getContentsByRole(doc, ContentRoleType.PRIMARY);
				while (qr.hasMoreElements()) {
					ApplicationData appData = (ApplicationData) qr.nextElement();
					if (appData != null) {
						return appData;
					}
				}
			}
		} catch (WTException e) {
			logger.error(CLASSNAME + ".getPrimaryContent:" + e);
		} 
		return null;
	}

}
