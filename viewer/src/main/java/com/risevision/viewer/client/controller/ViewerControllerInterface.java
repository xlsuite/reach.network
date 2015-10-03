// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.controller;

import com.risevision.common.client.info.PlaylistItemInfo;
import com.risevision.common.client.info.PresentationInfo;

public interface ViewerControllerInterface {	
	public void updateHtml(PresentationInfo presentation);

	public void setReady(String presFrame, boolean canPlay, boolean canStop, boolean canPause, boolean canReportReady, boolean canReportDone);	

	public void setError(String presFrame, String reason);
	
	public void setDone();
	
	public void play(boolean show);
	
	public void stop(boolean hide);
	
	public void pause(boolean hide);
	
	public void setReady(boolean isReady);
	
	public boolean isReady();
	
	public PlaylistItemInfo getItem();
}
