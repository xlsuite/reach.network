// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.widgets.oem;

public class RegisterFrameWidget extends HelpFrameWidget {

    private static RegisterFrameWidget instance;

    public RegisterFrameWidget() {
        super("http://reach.network/register");
    }

    public static RegisterFrameWidget getInstance() {
        try {
            if (instance == null)
                instance = new RegisterFrameWidget();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

}
