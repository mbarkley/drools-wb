/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.workbench.screens.guided.dtable.model;

import java.util.Set;

import org.drools.workbench.models.datamodel.workitems.PortableWorkDefinition;
import org.drools.workbench.models.guided.dtable.shared.model.GuidedDecisionTable52;
import org.guvnor.common.services.shared.metadata.model.Overview;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.kie.soup.commons.validation.PortablePreconditions;
import org.kie.soup.project.datamodel.model.PackageDataModelOracleBaselinePayload;

@Portable
public class GuidedDecisionTableEditorContent {

    private GuidedDecisionTable52 model;
    private Set<PortableWorkDefinition> workItemDefinitions;
    private PackageDataModelOracleBaselinePayload dataModel;
    private Overview overview;

    public GuidedDecisionTableEditorContent() {
    }

    public GuidedDecisionTableEditorContent(final GuidedDecisionTable52 model,
                                            final Set<PortableWorkDefinition> workItemDefinitions,
                                            final Overview overview,
                                            final PackageDataModelOracleBaselinePayload dataModel) {
        this.overview = PortablePreconditions.checkNotNull("overview",
                                                           overview);
        this.model = PortablePreconditions.checkNotNull("model",
                                                        model);
        this.workItemDefinitions = PortablePreconditions.checkNotNull("workItemDefinitions",
                                                                      workItemDefinitions);
        this.dataModel = PortablePreconditions.checkNotNull("dataModel",
                                                            dataModel);
    }

    public GuidedDecisionTable52 getModel() {
        return model;
    }

    public Set<PortableWorkDefinition> getWorkItemDefinitions() {
        return workItemDefinitions;
    }

    public PackageDataModelOracleBaselinePayload getDataModel() {
        return dataModel;
    }

    public Overview getOverview() {
        return overview;
    }

    public void setOverview(Overview overview) {
        this.overview = overview;
    }
}
