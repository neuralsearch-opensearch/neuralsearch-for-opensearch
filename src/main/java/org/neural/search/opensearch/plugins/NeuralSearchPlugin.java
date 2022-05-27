/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */
/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */
package org.neural.search.opensearch.plugins;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.neural.search.opensearch.plugins.ingestion.NeuralSearchIngestionProcessor;
import org.neural.search.opensearch.plugins.highlight.NeuralHighlighter;
import org.opensearch.client.Client;
import org.opensearch.cluster.metadata.IndexNameExpressionResolver;
import org.opensearch.cluster.service.ClusterService;
import org.opensearch.common.io.stream.NamedWriteableRegistry;
import org.opensearch.common.settings.Setting;
import org.opensearch.common.xcontent.NamedXContentRegistry;
import org.opensearch.env.Environment;
import org.opensearch.env.NodeEnvironment;
import org.opensearch.ingest.Processor.Factory;
import org.opensearch.ingest.Processor.Parameters;
import org.opensearch.knn.plugin.KNNPlugin;
import org.opensearch.plugins.ExtensiblePlugin;
import org.opensearch.plugins.IngestPlugin;
import org.opensearch.plugins.Plugin;
import org.opensearch.plugins.SearchPlugin;
import org.opensearch.repositories.RepositoriesService;
import org.opensearch.script.ScriptService;
import org.opensearch.search.fetch.FetchSubPhase;
import org.opensearch.search.fetch.subphase.highlight.Highlighter;
import org.opensearch.threadpool.ThreadPool;
import org.opensearch.watcher.ResourceWatcherService;

import static java.util.Collections.singletonList;

import java.util.ArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class NeuralSearchPlugin extends Plugin implements SearchPlugin, IngestPlugin, ExtensiblePlugin {

    @Override
    public List<Setting<?>> getSettings() {

        return NeuralSearchSettings.state().getSettings();
    }

    @Override
    public Collection<Object> createComponents(Client client, ClusterService clusterService, ThreadPool threadPool,
            ResourceWatcherService resourceWatcherService, ScriptService scriptService,
            NamedXContentRegistry xContentRegistry, Environment environment, NodeEnvironment nodeEnvironment,
            NamedWriteableRegistry namedWriteableRegistry, IndexNameExpressionResolver indexNameExpressionResolver,
            Supplier<RepositoriesService> repositoriesServiceSupplier) {
        NeuralSearchSettings.state().init(client);
        return super.createComponents(client, clusterService, threadPool, resourceWatcherService, scriptService,
                xContentRegistry, environment, nodeEnvironment, namedWriteableRegistry, indexNameExpressionResolver,
                repositoriesServiceSupplier);
    }

    @Override
    public List<QuerySpec<?>> getQueries() {
        return singletonList(new QuerySpec<>(NeuralSearchQueryBuilder.NAME, NeuralSearchQueryBuilder::new, 
        NeuralSearchQueryBuilder::fromXContent));
    }

    @Override
    public Map<String, Factory> getProcessors(Parameters parameters) {
        // TODO Auto-generated method stub
        return Collections.singletonMap(NeuralSearchIngestionProcessor.TYPE, new NeuralSearchIngestionProcessor.Factory());
    }

    @Override
    public Map<String, Highlighter> getHighlighters() {
        // TODO Auto-generated method stub
        Map<String, Highlighter> highlighters = new HashMap<String,Highlighter>();
        highlighters.put(NeuralHighlighter.NAME, new NeuralHighlighter());
        return highlighters;
    }

    

    

    
    
}
