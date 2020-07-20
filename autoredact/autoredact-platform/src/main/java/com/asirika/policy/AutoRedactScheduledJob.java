package com.asirika.policy;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

public class AutoRedactScheduledJob extends AbstractScheduledLockedJob implements StatefulJob {
	 public void executeJob(JobExecutionContext context) throws JobExecutionException {
	        JobDataMap jobData = context.getJobDetail().getJobDataMap();

	        // Extract the Job executer to use
	        Object executerObj = jobData.get("jobExecuter");
	        if (executerObj == null || !(executerObj instanceof AutoRedactScheduledJobExecuter)) {
	            throw new AlfrescoRuntimeException(
	                    "ScheduledJob data must contain valid 'Executer' reference");
	        }

	        final AutoRedactScheduledJobExecuter jobExecuter = (AutoRedactScheduledJobExecuter) executerObj;

	        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
	            public Object doWork() throws Exception {
	                jobExecuter.execute();
	                return null;
	            }
	        }, AuthenticationUtil.getSystemUserName());
	    }
	}