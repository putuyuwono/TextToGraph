/**
 * Copyright (c) 2002-2013 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package rest.query;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.neo4j.helpers.collection.IterableWrapper;
import rest.*;
import rest.converter.RestEntityExtractor;
import rest.converter.RestTableResultExtractor;
import rest.util.ConvertedResult;
import rest.util.DefaultConverter;
import rest.util.Handler;
import rest.util.QueryResult;
import rest.util.QueryResultBuilder;
import rest.util.ResultConverter;

/**
 * @author mh
 * @since 22.06.11
 */
public class RestGremlinQueryEngine implements QueryEngine<Object> {
    private final RestAPI restApi;
    private final ResultConverter resultConverter;


    public RestGremlinQueryEngine(RestAPI restApi) {
        this(restApi,null);
    }
    public RestGremlinQueryEngine(RestAPI restApi, ResultConverter resultConverter) {
        this.restApi = restApi;
        this.resultConverter = resultConverter!=null ? resultConverter : new DefaultConverter();
    }

    @Override
    public QueryResult<Object> query(String statement, Map<String, Object> params) {
        return restApi.run(statement, params, resultConverter);
    }
}