package com.asirika.policy;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class AutoRedactScheduledJobExecuter {
    private static final Logger logger = LoggerFactory.getLogger(AutoRedactScheduledJobExecuter.class);
    
    private static String REDACT_URL= "http://content:8080/alfresco/OpenContent/annotation/redactTermsFromDocuments?";

	/* URI */
    static final String NAMESPACE_URI_REDACT = "http://www.alfresco.com/model/customredact/1.0";
    
    /* TYPES */
    static final QName ASPECT__AUTOREDACT = QName.createQName(NAMESPACE_URI_REDACT, "autoredact");
    
    /**
     * Public API access
     */
    private ServiceRegistry serviceRegistry;
    
    private boolean enabled;
    
    private String parentNodeId;
    private String readactPattern;
    private boolean folderCheck;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
    
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void setParentNodeId(String parentNodeId) {
		this.parentNodeId = parentNodeId;
	}


	public void setReadactPattern(String readactPattern) {
		this.readactPattern = readactPattern;
	}

	public void setFolderCheck(boolean folderCheck) {
		this.folderCheck = folderCheck;
	}

	/**
     * Executer implementation
     */
	public void execute() {
		logger.info("Running the scheduled job");

		if (enabled) {


			List<NodeRef> document = getRedactDocumentList();
			
			if (document.size() > 0) {
				for (NodeRef docNodeRef : document) 
				{
					NodeRef parentFolderRef = serviceRegistry.getNodeService().getPrimaryParent(docNodeRef)
							.getParentRef();
					NodeRef parentNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, this.parentNodeId);

					logger.info(" Redact Pattern: " + this.readactPattern);
					logger.info("parentNodeFromProp : " + parentNode + " ParentNode: " + parentFolderRef);

					if (parentNode.equals(parentFolderRef) || !folderCheck) {

						logger.info("Ready to Redact docref ({})  in folder ({})", docNodeRef, parentFolderRef);

						String ticket = serviceRegistry.getAuthenticationService().getCurrentTicket();


						MultiValueMap<String, String> allRequestParams = new LinkedMultiValueMap<String, String>();

						allRequestParams.add("textToRedact", this.readactPattern);
						allRequestParams.add("ticket", ticket);
						allRequestParams.add("objectIds", docNodeRef.toString());
						allRequestParams.add("newVersion", "false");

						UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(REDACT_URL)
								.queryParams(allRequestParams);
						UriComponents uriComponents = builder.build().encode();

						logger.info(" REDACT_URL :" + uriComponents.toUri());

						try {
							RestTemplate restTemplate = new RestTemplate();
							ResponseEntity<Object> responseEntity = restTemplate.getForEntity(uriComponents.toUri(),
									Object.class);

							logger.info("Successfully redacted the document node  :" + docNodeRef);

							serviceRegistry.getNodeService().removeAspect(docNodeRef, ASPECT__AUTOREDACT);

							logger.info("Successfully Removed the  Custom Redact Aspect");
							logger.info("===========================================================================");
						
						} catch (Exception e) {
							logger.warn("failed to redact document node: " + docNodeRef + " fail:" + e);
						}
					}
				}
			}
		}
	}
    

	private List<NodeRef> getRedactDocumentList() 
	{

		SearchParameters sp = new SearchParameters();
		sp.setQuery("select * from cusredact:autoredact");
		sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
		sp.setLanguage(SearchService.LANGUAGE_CMIS_ALFRESCO);
		
		List<NodeRef> docNodeRefs = new ArrayList<>();

		ResultSet results = null;
		try {
			results = serviceRegistry.getSearchService().query(sp);
			docNodeRefs = results.getNodeRefs();
		} finally {
			if (results != null) {
				results.close();
			}
		}

		return docNodeRefs;
	}
		
    	
 
}