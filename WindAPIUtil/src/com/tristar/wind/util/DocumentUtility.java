package com.tristar.wind.util;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.doc.WTDocument;
import wt.doc.WTDocumentDependencyLink;
import wt.doc.WTDocumentMaster;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.ObjectSetVector;
import wt.fc.ObjectToObjectLink;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTReference;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.log4j.LogR;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.pds.StatementSpec;
import wt.pom.PersistenceException;
import wt.pom.Transaction;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;
import wt.vc.VersionControlHelper;
import wt.vc.wip.WorkInProgressHelper;

public class DocumentUtility {
	
	private static String CLASSNAME = DocumentUtility.class.getName();
	private final static Logger logger = LogR.getLogger(DocumentUtility.class.getName());
	
	

	public static WTDocument getLatestDocumentByNumber(String number) {
		WTDocument result = null;
		try {
			if (StringUtils.isNotBlank(number)) {

				WTDocumentMaster master = DocumentUtility.getDocumentMaster(number);
				QueryResult qr = VersionControlHelper.service.allIterationsOf(master);
				if (qr.hasMoreElements()) {
					qr = VersionControlHelper.service.allIterationsOf(master);
					if (qr.hasMoreElements()) {
						result = (WTDocument) qr.nextElement();
					}
				}
			}
		} catch (WTException e) {
			logger.error(CLASSNAME + ".getDocumentByNumber:" + e);
		}
		return result;
	}
	
	public static WTDocumentMaster getDocumentMaster(String number)
	{
		WTDocumentMaster result = null;
		try {
			if (StringUtils.isNotBlank(number))
			{
				QuerySpec qs = new QuerySpec(WTDocumentMaster.class);
				SearchCondition scnumber = new SearchCondition(WTDocumentMaster.class, WTDocumentMaster.NUMBER, SearchCondition.EQUAL, number.toUpperCase());
				qs.appendWhere(scnumber, new int[] { 0 });
				QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
				
				//TODO need to handle in the unlikely event that more than one master is present.  Cuz it shouldn't
				if (qr.hasMoreElements()) {
					result = (WTDocumentMaster) qr.nextElement();
				}
			}
		} catch (WTException e) {
			logger.error(CLASSNAME+".getDocumentByNumber:" + e);
		} 
		return result;
	}
	
	private static WTDocument getDocumentFromCollection(Object obj)
	{
		if (obj instanceof ObjectReference) {
			ObjectReference itsaRef = (ObjectReference)obj;
			WTDocument thisDocument = (WTDocument)itsaRef.getObject();
			return thisDocument;
		}
		else
		{
			return (WTDocument)obj;
		}
	}
	
	public static void removeDependencyLink(WTDocument doc) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				RemoteMethodServer.getDefault().invoke("removeDependencyLink", DocumentUtility.class.getName(), null,
						new Class[] { String.class }, new Object[] {});
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (doc == null) {
						return;
					}
					QuerySpec queryspec = new QuerySpec(WTDocumentDependencyLink.class);
					queryspec.appendWhere(new SearchCondition(WTDocumentDependencyLink.class, "roleAObjectRef.key", "=",
							PersistenceHelper.getObjectIdentifier(doc)), new int[] { 0 });
					QueryResult qr = PersistenceServerHelper.manager.query(queryspec);
					while (qr.hasMoreElements()) {
						WTDocumentDependencyLink link = (WTDocumentDependencyLink) qr.nextElement();
						PersistenceServerHelper.manager.remove(link);
					}
				} catch (Exception e) {
					logger.error(CLASSNAME + "." + "removeDependencyLink" + ":" + e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
			}
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(), e);
		}
	} 
	
	public static boolean isCheckOut(String oid) throws RemoteException, InvocationTargetException, WTRuntimeException, WTException{
		try {
			if (!RemoteMethodServer.ServerFlag){
				return (Boolean) RemoteMethodServer.getDefault().invoke("isCheckOut", CLASSNAME, null,
						new Class[] { String.class }, new Object[] { oid });
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				ReferenceFactory referenceFactory = new ReferenceFactory();
				WTDocument doc = (WTDocument) referenceFactory.getReference(oid).getObject();
				try {
					if (doc.isLatestIteration()) {
						doc = (WTDocument) VersionControlHelper.service.getLatestIteration(doc, false);
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"isCheckOut"+":"+e);
				} finally {
				    SessionServerHelper.manager.setAccessEnforced(enforce);
			    }
				if (WorkInProgressHelper.isCheckedOut(doc)) {
					return true;
				}
			}
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		}
		return false;
	
	}
	
	public static QueryResult intGetDescribeAssociations(WTPart wtpart, WTDocumentMaster wtdocumentmaster) throws WTException {
	    try {
			if (!RemoteMethodServer.ServerFlag) {
						return (QueryResult) RemoteMethodServer.getDefault().invoke("intGetDescribeAssociations", 
								DocumentUtility.class.getName(), null, new Class[] { WTPart.class,WTDocumentMaster.class},
								new Object[] { wtpart,wtdocumentmaster });
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				QueryResult queryresult = new QueryResult();
				QuerySpec queryspec = new QuerySpec(WTPartDescribeLink.class);
				if(wtpart != null && wtdocumentmaster != null){
					queryspec.appendClassList(WTDocument.class, true);
					queryspec.appendWhere(new SearchCondition(WTPartDescribeLink.class, "roleAObjectRef.key", "=",
							PersistenceHelper.getObjectIdentifier(wtpart)), new int[] { 0 });
					queryspec.appendAnd();
					queryspec.appendWhere(new SearchCondition(WTPartDescribeLink.class, "roleBObjectRef.key.id",
							WTDocument.class, "thePersistInfo.theObjectIdentifier.id"), new int[] { 0, 1 });
					queryspec.appendAnd();
					queryspec.appendWhere(new SearchCondition(WTDocument.class, "masterReference.key",
									"=", PersistenceHelper.getObjectIdentifier(wtdocumentmaster)), new int[] { 1 });
					QueryResult queryresult1 = PersistenceHelper.manager.find(queryspec);
					Vector<ObjectToObjectLink> vector = new Vector<ObjectToObjectLink>();
					WTPartDescribeLink wtpartdescribelink;
					try {
						for (; queryresult1.hasMoreElements(); vector.add(wtpartdescribelink)) {
							Object aobj[] = (Object[]) (Object[]) queryresult1.nextElement();
							wtpartdescribelink = (WTPartDescribeLink) aobj[0];
								wtpartdescribelink.setDescribes(wtpart);
								wtpartdescribelink.setDescribedBy((WTDocument) aobj[1]);
						}
						queryresult.append(new ObjectSetVector(vector));
					} catch (Exception e) {
						logger.error(CLASSNAME+"."+"intGetDescribeAssociations"+":"+e);
					} finally {
					    SessionServerHelper.manager.setAccessEnforced(enforce);
					}
					return queryresult;
				}
			}
		} catch (java.rmi.RemoteException e ) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e){
			logger.error(e.getMessage(),e);
		}
		return null;	
	}
	
	
}
