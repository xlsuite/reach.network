// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.linker;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.AbstractLinker;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.ext.linker.EmittedArtifact.Visibility;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;

@LinkerOrder(Order.POST)
public class OfflineLinker extends AbstractLinker {

	@Override
	public String getDescription() {
		return "HTML 5 Offline Linker";
	}

	@Override
	public ArtifactSet link(TreeLogger logger, LinkerContext context, ArtifactSet artifacts) throws UnableToCompleteException {
		ArtifactSet artifactSet = new ArtifactSet(artifacts);
		StringBuilder buf = new StringBuilder();
		buf.append("CACHE MANIFEST\n");
		buf.append("#").append(System.currentTimeMillis()).append("\n");
		buf.append("\n");
		buf.append("CACHE:\n");
		buf.append("Viewer.html\n");
//		buf.append("scripts/jquery-1.7.1.min.js\n");
		buf.append("scripts/jquery-1.10.2.min.js\n");
		buf.append("scripts/jquery.timers-1.2.js\n");
		buf.append("scripts/viewerScripts.js\n");
		buf.append("scripts/presentationScripts.js\n");
		buf.append("scripts/videoScripts.js\n");
		buf.append("scripts/imageScripts.js\n");
		buf.append("scripts/ext-logger.js\n");
		buf.append("images/ajax-loader-circle-notext.gif\n");
		buf.append("images/eye-icon.png\n");
		buf.append("images/icons-error.png\n");
		buf.append("style/viewer.css\n");
		buf.append("slicebox/js/jquery.easing.1.3.js\n");
		buf.append("slicebox/js/jquery.slicebox.js\n");
//		buf.append("mediaplayer/BrowserDetect.js\n");
		buf.append("jwplayer/jwplayer.js\n");
		buf.append("jwplayer/jwplayer.html5.js\n");
		buf.append("jwplayer/jwplayer.flash.swf\n");
		buf.append("jwplayer/skins/six.xml\n");
//		buf.append("videojs/video.min.js\n");
//		buf.append("videojs/video-js.css\n");
//		buf.append("videojs/video-js.png\n");
//		buf.append("videojs/video-js.swf\n");
		
		buf.append("viewer/viewer.nocache.js\n");
		for(EmittedArtifact artifact: artifacts.find(EmittedArtifact.class)){
			if(artifact.getVisibility().compareTo(Visibility.Private) != 0 && !artifact.getPartialPath().contains(".symbolMap")){
				buf.append("viewer/").append(artifact.getPartialPath()).append("\n");
			}
		}
		buf.append("\n");
		buf.append("NETWORK:\n");
		buf.append("*\n");

		EmittedArtifact artifact = emitString(logger, buf.toString(), "offline.manifest"); //my manifest file name
		artifactSet.add(artifact);
		return artifactSet;
	}
}

