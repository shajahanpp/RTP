package com.experian.tablevalidation;

public class StaticSqlQueryClass {
	
	public static String GetEventsTableDetails="SELECT \r\n" + 
			"[APPLCTN_ID] AS 'AppID'\r\n" + 
			",[EDA_TENANT1].[EVENTS].[EVNT_INDX] AS 'EventID'\r\n" + 
			",CASE \r\n" + 
			"WHEN [EDA_TENANT1].[EVENTS].[EVNT_TYP] = '2' THEN 'Error' \r\n" + 
			"WHEN [EDA_TENANT1].[EVENTS].[EVNT_TYP] = '1' THEN 'Warning'\r\n" + 
			"ELSE 'Information' END AS 'Type'\r\n" + 
			",[EDA_TENANT1].[EVENTS].[APPLCTN_STG] AS 'Stage'\r\n" + 
			",[EDA_TENANT1].[EVENTS].APPLCTN_WRKLST AS 'Worklist'\r\n" + 
			",[EDA_TENANT1].[EVENTS].[APPLCTN_STTS] AS 'Status'\r\n" + 
			",[EDA_TENANT1].[EVENTS].[EVNT_CD] AS 'Code'\r\n" + 
			",CONVERT(VARCHAR, [EDA_TENANT1].[EVENTS].[EVNT_DT] + \r\n" + 
			"CASE\r\n" + 
			"WHEN LEFT([EDA_TENANT1].[EVENTS].[EVNT_TM],2) = '24' THEN '00'+RIGHT([EDA_TENANT1].[EVENTS].[EVNT_TM],6) --when 24...... then 00......\r\n" + 
			"ELSE [EDA_TENANT1].[EVENTS].[EVNT_TM]\r\n" + 
			"END\r\n" + 
			",20) AS 'DateTime'\r\n" + 
			"\r\n" + 
			",[EDA_TENANT1].[EVENTS].[EVNT_MSSG] AS 'Message'\r\n" + 
			",[EDA_TENANT1].[EVENTS].[EVNT_SRC] AS 'Source'\r\n" + 
			"\r\n" + 
			"FROM EDA_TENANT1.PROPOSAL\r\n" + 
			"\r\n" + 
			"INNER JOIN [EDA_TENANT1].[EVENTS] ON [EDA_TENANT1].[EVENTS].[IDS_PROPOSAL] = [EDA_TENANT1].[PROPOSAL].[IDS_PROPOSAL]";


public static String GetKeyPersonApplicationMappingQuery="INNER JOIN EDA_TENANT1.KEY_PERSON ON EDA_TENANT1.APPLICATION.IDS_APPLICATION = EDA_TENANT1.KEY_PERSON.IDS_APPLICATION";

public static String GetKeyPersonAddressMappingQuery="INNER JOIN EDA_TENANT1.ADDRESS ON EDA_TENANT1.KEY_PERSON.IDS_KEY_PERSON = EDA_TENANT1.ADDRESS.IDS_KEY_PERSON";
public static String GetApplicantAddressMappingQuery="INNER JOIN EDA_TENANT1.ADDRESS ON EDA_TENANT1.APPLICANT.IDS_APPLICANT = EDA_TENANT1.ADDRESS.IDS_APPLICANT";

}
