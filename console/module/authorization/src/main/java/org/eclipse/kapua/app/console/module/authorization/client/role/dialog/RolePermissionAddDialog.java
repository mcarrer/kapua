/*******************************************************************************
 * Copyright (c) 2011, 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.app.console.module.authorization.client.role.dialog;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.eclipse.kapua.app.console.module.api.client.GwtKapuaErrorCode;
import org.eclipse.kapua.app.console.module.api.client.GwtKapuaException;
import org.eclipse.kapua.app.console.module.api.client.ui.dialog.entity.EntityAddEditDialog;
import org.eclipse.kapua.app.console.module.api.client.ui.panel.FormPanel;
import org.eclipse.kapua.app.console.module.api.client.util.DialogUtils;
import org.eclipse.kapua.app.console.module.api.shared.model.session.GwtSession;
import org.eclipse.kapua.app.console.module.authorization.client.messages.ConsolePermissionMessages;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtDomain;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtGroup;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtPermission;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtPermission.GwtAction;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtRole;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtRolePermission;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtRolePermissionCreator;
import org.eclipse.kapua.app.console.module.authorization.shared.model.permission.GroupSessionPermission;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtDomainRegistryService;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtDomainRegistryServiceAsync;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtGroupService;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtGroupServiceAsync;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtRoleService;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtRoleServiceAsync;

import java.util.List;

public class RolePermissionAddDialog extends EntityAddEditDialog {

    private static final ConsolePermissionMessages MSGS = GWT.create(ConsolePermissionMessages.class);

    private static final GwtDomainRegistryServiceAsync DOMAIN_SERVICE = GWT.create(GwtDomainRegistryService.class);
    private static final GwtGroupServiceAsync GWT_GROUP_SERVICE = GWT.create(GwtGroupService.class);
    private static final GwtDomainRegistryServiceAsync GWT_DOMAIN_SERVICE = GWT.create(GwtDomainRegistryService.class);

    private ComboBox<GwtDomain> domainsCombo;
    private SimpleComboBox<GwtAction> actionsCombo;
    private ComboBox<GwtGroup> groupsCombo;
    private CheckBoxGroup forwardableChecboxGroup;
    private CheckBox forwardableChecbox;

    private GwtRole selectedRole;

    private final GwtGroup allGroup;
    private final GwtDomain allDomain = new GwtDomain(null, "ALL");
    private final GwtAction allAction = GwtAction.ALL;

    public RolePermissionAddDialog(GwtSession currentSession) {
        super(currentSession);
        allGroup = new GwtGroup();
        allGroup.setId(null);
        allGroup.setGroupName("ALL");

        DialogUtils.resizeDialog(this, 500, 250);
    }

    public void setSelectedRole(GwtRole selectedRole) {
        this.selectedRole = selectedRole;
    }

    @Override
    public void createBody() {
        FormPanel permissionFormPanel = new FormPanel(FORM_LABEL_WIDTH);

        //
        // Domain
        domainsCombo = new ComboBox<GwtDomain>();
        domainsCombo.setStore(new ListStore<GwtDomain>());
        domainsCombo.setEditable(false);
        domainsCombo.setTypeAhead(false);
        domainsCombo.setAllowBlank(false);
        domainsCombo.disable();
        domainsCombo.setFieldLabel(MSGS.permissionAddDialogDomain());
        domainsCombo.setTriggerAction(TriggerAction.ALL);
        domainsCombo.setEmptyText(MSGS.permissionAddDialogLoading());
        domainsCombo.setDisplayField("domainName");
        GWT_DOMAIN_SERVICE.findAll(new AsyncCallback<List<GwtDomain>>() {

            @Override
            public void onFailure(Throwable caught) {
                exitMessage = MSGS.dialogAddError(caught.getLocalizedMessage());
                exitStatus = false;
                hide();
            }

            @Override
            public void onSuccess(List<GwtDomain> result) {
                domainsCombo.getStore().add(allDomain);
                domainsCombo.getStore().add(result);
                domainsCombo.setValue(allDomain);
                domainsCombo.enable();
            }
        });

        domainsCombo.addSelectionChangedListener(new SelectionChangedListener<GwtDomain>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<GwtDomain> se) {
                final GwtDomain selectedDomain = se.getSelectedItem();

                GWT_DOMAIN_SERVICE.findActionsByDomainName(selectedDomain.getDomainName(), new AsyncCallback<List<GwtAction>>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        exitMessage = MSGS.dialogAddError(caught.getLocalizedMessage());
                        exitStatus = false;
                        hide();
                    }

                    @Override
                    public void onSuccess(List<GwtAction> result) {
                        actionsCombo.removeAll();
                        actionsCombo.add(allAction);
                        actionsCombo.add(result);
                        actionsCombo.setSimpleValue(allAction);
                        actionsCombo.enable();

                        if (selectedDomain.getGroupable()) {
                            groupsCombo.setEnabled(selectedDomain.getGroupable());
                            groupsCombo.setValue(allGroup);

                        } else {
                            groupsCombo.setEnabled(selectedDomain.getGroupable());
                            groupsCombo.setRawValue(MSGS.permissionAddDialogGroupNotGroupable());
                        }

                    }
                });

            }
        });
        permissionFormPanel.add(domainsCombo);

        //
        // Action
        actionsCombo = new SimpleComboBox<GwtAction>();
        actionsCombo.disable();
        actionsCombo.setEditable(false);
        actionsCombo.setTypeAhead(false);
        actionsCombo.setAllowBlank(false);
        actionsCombo.setFieldLabel(MSGS.permissionAddDialogAction());
        actionsCombo.setTriggerAction(TriggerAction.ALL);
        actionsCombo.setEmptyText(MSGS.permissionAddDialogLoading());

        permissionFormPanel.add(actionsCombo);

        // Groups
        groupsCombo = new ComboBox<GwtGroup>();
        groupsCombo.setStore(new ListStore<GwtGroup>());
        groupsCombo.setEditable(false);
        groupsCombo.setTypeAhead(false);
        groupsCombo.setAllowBlank(false);
        groupsCombo.setDisplayField("groupName");
        groupsCombo.setValueField("id");
        groupsCombo.setFieldLabel(MSGS.permissionAddDialogGroup());
        groupsCombo.setTriggerAction(TriggerAction.ALL);
        groupsCombo.setEmptyText(MSGS.permissionAddDialogLoading());
        groupsCombo.disable();
        if (currentSession.hasPermission(GroupSessionPermission.read())) {
            GWT_GROUP_SERVICE.findAll(currentSession.getSelectedAccountId(), new AsyncCallback<List<GwtGroup>>() {

                @Override
                public void onFailure(Throwable caught) {
                    exitMessage = MSGS.dialogAddError(caught.getLocalizedMessage());
                    exitStatus = false;
                    hide();
                }

                @Override
                public void onSuccess(List<GwtGroup> result) {
                    groupsCombo.getStore().removeAll();
                    groupsCombo.getStore().add(allGroup);
                    groupsCombo.getStore().add(result);
                    groupsCombo.setValue(allGroup);
                    groupsCombo.enable();
                }
            });
            permissionFormPanel.add(groupsCombo);
        } else {
            groupsCombo.getStore().add(allGroup);
            groupsCombo.setValue(allGroup);
        }

        //
        // Forwardable
        forwardableChecbox = new CheckBox();
        forwardableChecbox.setBoxLabel("");

        forwardableChecboxGroup = new CheckBoxGroup();
        forwardableChecboxGroup.setFieldLabel(MSGS.permissionAddDialogForwardable());
        forwardableChecboxGroup.add(forwardableChecbox);
        permissionFormPanel.add(forwardableChecboxGroup);

        //
        // Add form panel to body
        bodyPanel.add(permissionFormPanel);

    }

    @Override
    public void submit() {
        GwtPermission permission = new GwtPermission();
        permission.setDomain(domainsCombo.getValue().getDomainId());
        permission.setAction(actionsCombo.getValue().getValue().toString());
        permission.setTargetScopeId(currentSession.getSelectedAccountId());
        permission.setGroupId(groupsCombo.getValue().getId());
        permission.setForwardable(forwardableChecboxGroup.getValue() != null);

        GwtRolePermissionCreator rolePermission = new GwtRolePermissionCreator();
        rolePermission.setScopeId(currentSession.getSelectedAccountId());
        rolePermission.setRoleId(selectedRole.getId());

        GwtRoleServiceAsync roleService = GWT.create(GwtRoleService.class);
        roleService.addRolePermission(xsrfToken, rolePermission, permission, new AsyncCallback<GwtRolePermission>() {

            @Override
            public void onSuccess(GwtRolePermission rolePermission) {
                exitStatus = true;
                exitMessage = MSGS.dialogAddConfirmation();
                hide();
            }

            @Override
            public void onFailure(Throwable cause) {
                unmask();

                submitButton.enable();
                cancelButton.enable();
                status.hide();

                exitStatus = false;
                if ((cause instanceof GwtKapuaException) && (GwtKapuaErrorCode.ENTITY_UNIQUENESS.equals(((GwtKapuaException) cause).getCode()))) {
                    exitMessage = MSGS.dialogAddPermissionAlreadyExists();
                } else {
                    exitMessage = MSGS.dialogAddError(MSGS.dialogAddPermissionError(cause.getLocalizedMessage()));
                }

                hide();
            }
        });

    }

    @Override
    public String getHeaderMessage() {
        return MSGS.permissionAddDialogHeader();
    }

    @Override
    public String getInfoMessage() {
        return MSGS.permissionAddDialogMessage();
    }

}
