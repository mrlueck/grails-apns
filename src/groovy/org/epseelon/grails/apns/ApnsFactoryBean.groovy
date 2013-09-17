package org.epseelon.grails.apns

import org.springframework.beans.factory.FactoryBean
import com.notnoop.apns.APNS
import com.notnoop.apns.ApnsService

/**
 * @author sarbogast
 * @version 1 janv. 2010, 00:37:38
 */
class ApnsFactoryBean implements FactoryBean {
    public enum Environment{SANDBOX, PRODUCTION}

    String pathToCertificate
    String certificateResourcePath
    String password
    boolean queued = false
    Environment environment = Environment.SANDBOX
    
	boolean nonBlocking = false
	
	Map certificates
	
    Object getObject() {
		if ( certificates && certificates.size() > 0){
			def apnsServices = [:]
			certificates.each { key,value->
				def apnsService = APNS.newService()
				
				if(value.certificateResourcePath){
					
					apnsService = apnsService.withCert(this.getClass().getResourceAsStream(value.certificateResourcePath), value.password)
					
				}else if(value.pathToCertificate){
					
					apnsService = apnsService.withCert(value.pathToCertificate, value.password)
					
				} 
				
				switch(environment){
					case Environment.PRODUCTION:
						apnsService = apnsService.withProductionDestination(); break;
					case Environment.SANDBOX:
					default:
						apnsService = apnsService.withSandboxDestination(); break;
				}
				  
				apnsService = apnsService.asQueued()
				apnsServices[key] = apnsService.build()
			}
			
			return apnsServices
			
		}else{
	        def apnsService = APNS.newService()
	        if(certificateResourcePath){
	            apnsService = apnsService.withCert(this.getClass().getResourceAsStream(certificateResourcePath), password)
	        } else if(pathToCertificate){
	            apnsService = apnsService.withCert(pathToCertificate, password)
	        } else {
	            switch (environment){
	                case Environment.PRODUCTION: certificateResourcePath = "/apns-prod.p12"
	                    break
	                case Environment.SANDBOX: certificateResourcePath = "/apns-dev.p12"
	            }
	            apnsService = apnsService.withCert(this.getClass().getResourceAsStream(certificateResourcePath), password)
	        }
			
	        switch(environment){
	            case Environment.PRODUCTION: 
					apnsService = apnsService.withProductionDestination(); break;
	            case Environment.SANDBOX:
	            default: 
					apnsService = apnsService.withSandboxDestination(); break;
	        }
			
			/*
			 * Apns, nowadays, is just supporting Queued service. 
			 * Was talking to the developer of the API, and he told me that furtermore
			 * even the method asNonBlocking might be removed!
			 */
			  
	        apnsService = apnsService.asQueued()
	
	        return apnsService.build()
		}
    }

    Class getObjectType() { 
		if ( certificates && certificates.size() > 0){
			return ApnsService
		}else{
			return Map
		} 
    }

    boolean isSingleton() { true }
}
