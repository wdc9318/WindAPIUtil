package com.tristar.wind.util;

import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentRoleType;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.method.RemoteMethodServer;
import wt.session.SessionServerHelper;
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
	
	public static ApplicationData getAttachmentByName(WTObject obj, String fileName) throws WTException, PropertyVetoException {
		ContentHolder contentHolder = null;
		try {
			if (StringUtils.isEmpty(fileName) && obj != null) {
				if (obj instanceof WTDocument) {
					WTDocument wtdocument = (WTDocument) obj;
					contentHolder = ContentHelper.service.getContents((ContentHolder) wtdocument);
				} else if (obj instanceof EPMDocument) {
					EPMDocument epm = (EPMDocument) obj;
					contentHolder = ContentHelper.service.getContents((ContentHolder) epm);
				}
				QueryResult qr = ContentHelper.service.getContentsByRole(contentHolder, ContentRoleType.SECONDARY);
				while (qr.hasMoreElements()) {
					ApplicationData appData = (ApplicationData) qr.nextElement();
					String appDataName = appData.getFileName();
					if (appDataName.indexOf(fileName) >= 0) {
						return appData;
					}
				}
			}
		} catch (WTException e1) {
			logger.error(CLASSNAME + ".getAttachmentByName:" + e1);
		}
		return null;
	}

}
