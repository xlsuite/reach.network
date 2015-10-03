// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.utils;

import com.risevision.common.client.utils.RiseUtils;

public class ViewerUtils {
	// AD - Scale and Position functions are deprecated; keep in case they need to return
    @SuppressWarnings("unused")
	private static double getItemScale(String itemScale)
    {
        double scale;

        if (itemScale.equals("fit")){
        	scale = 100;
        }
        else {
        	scale = RiseUtils.strToInt(itemScale, 100);
        }
        
        return scale / 100;
    }
	
    @SuppressWarnings("unused")
	private static int getPositionTop(String position, int top, int height, int realHeight)
    {
        if (position.equals("Middle Left") || position.equals("Centered") || position.equals("Middle Right"))
        {
            return (int)(top + (height- realHeight) / 2);
        }
        else if (position.equals("Bottom Left") || position.equals("Bottom") || position.equals("Bottom Right"))
        {
            return (int)(top + height - realHeight);
        }
        else
            return top;
    }

    @SuppressWarnings("unused")
	private static int getPositionLeft(String position, int left, int width, int realWidth)
    {
        if (position.equals("Top") || position.equals("Centered") || position.equals("Bottom"))
        {
            return (int)(left + (width - realWidth) / 2);
        }
        else if (position.equals("Top Right") || position.equals("Bottom Right") || position.equals("Middle Right"))
        {
            return (int)(left + width - realWidth);
        }
        else
            return left;
    }
}
