package com.risevision.viewer.client.widgets.oem;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.risevision.viewer.client.player.RisePlayerController;

public class ReachNetworkDisplayWidget extends DisplayRegisterBaseWidget {

    public static final String DISPLAY_ID_PARAM = "%display-id%";

    private static ReachNetworkDisplayWidget instance;

    private HorizontalPanel hpButtons1 = new HorizontalPanel();
    private HorizontalPanel hpButtons2 = new HorizontalPanel();

    private Button btEnterDisplayId = new DisplayRegisterButtonWidget("Enter Display ID");
    private Button btEnterClaimId = new DisplayRegisterButtonWidget("Enter Claim ID");
    private Button btQuit = new DisplayRegisterButtonWidget("Quit");
    private Button btRegister = new DisplayRegisterButtonWidget("Register");


    public ReachNetworkDisplayWidget() {
        super(true);

        styleControls();

        hpButtons1.add(btEnterDisplayId);
        hpButtons1.add(btEnterClaimId);

        hpButtons2.add(btQuit);
        hpButtons2.add(btRegister);

        buttonPanel.add(hpButtons1);
        buttonPanel.add(hpButtons2);

        initActions();
    }

    private void styleControls() {
        hpButtons1.setWidth("280px");
        hpButtons1.setSpacing(5);

        hpButtons2.setWidth("280px");
        hpButtons2.setSpacing(5);

        outerPanel.setSize("450px", "250px");
        topContainer.getElement().getStyle().setHeight(25, Style.Unit.PX);
    }

    private void initActions() {
        btQuit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                stopCountdownTimer();
                RisePlayerController.shutdown();
            }
        });
        btEnterClaimId.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                stopCountdownTimer();
                EnterClaimIdWidget.getInstance(false).show();
            }
        });
        btEnterDisplayId.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                stopCountdownTimer();
                EnterDisplayIdWidget.getInstance().show();
            }
        });
        ClickHandler registerHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                stopCountdownTimer();
                RegisterFrameWidget.getInstance().show();
            }
        };
        btRegister.addClickHandler(registerHandler);
    }

    public static ReachNetworkDisplayWidget getInstance() {
        try {
            if (instance == null)
                instance = new ReachNetworkDisplayWidget();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

}
