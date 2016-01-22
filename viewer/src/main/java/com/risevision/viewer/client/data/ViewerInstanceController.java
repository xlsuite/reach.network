// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.data;

import com.risevision.viewer.client.ViewerEntryPoint;

public class ViewerInstanceController {
	public static void init() {
		initNative(ViewerEntryPoint.getDisplayId());
	}
	
	protected static void handleStorageEvent() {
		ViewerDataController.setDuplicateInstance();
	}
    
	protected static native void initNative(String displayId) /*-{
//		debugger;
		var duplicateInstance = false;
		var timestamp = Date.now();
		var instanceObject = { 
					timestamp: timestamp, 
					displayId: displayId 
				};
		
		var handlerFunction = function ( event ) {
//			debugger;
	        var type = event.key,
	        	newInstanceObject = 0;
	        if ( type === 'instance' ) {
	            try {
	                newInstanceObject = +localStorage.getItem( 'instance' ) || 0;
	                
	                newInstanceObject = JSON.parse( newInstanceObject );
	            } catch ( error ) {}
	            
	            // check if instance is using the same Display Id
	            if ( newInstanceObject && newInstanceObject.displayId === instanceObject.displayId) {
		            // block current instance
		            //if ( !duplicateInstance ) {
		            //    @com.risevision.viewer.client.data.ViewerInstanceController::handleStorageEvent()();
		            //
		            //    duplicateInstance = true;
		            //
						//$wnd.writeToLog("Instance Controller - duplicate found, shutting down");
		            //}
		            
		            if ( newInstanceObject.timestamp > timestamp) {
		                $wnd.localStorage.setItem( 'instance', JSON.stringify(instanceObject) );
		                
		                $wnd.writeToLog("Instance Controller - sent timestamp");
		            }
		            else {
						removeEvent();
		            }
	            }
	        }
		};
		
		var removeEvent = function () {
			$wnd.window.removeEventListener( 'storage', handlerFunction, false );
	               
			$wnd.writeToLog("Instance Controller - removed event");
		};
				
		$wnd.window.addEventListener( 'storage', handlerFunction, false );
		
		try {
			$wnd.localStorage.setItem( 'instance', JSON.stringify(instanceObject) );

			$wnd.writeToLog("Instance Controller - initial timestamp");
			
		} catch ( error ) {}
		
	}-*/;
}
