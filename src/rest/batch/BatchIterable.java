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
package rest.batch;

import java.util.Iterator;


import rest.RequestResult;
import rest.RestAPI;
import rest.UpdatableRestResult;

/**
 * @author mh
 * @since 21.09.11
 */
public class BatchIterable<T> implements Iterable<T>, UpdatableRestResult<Iterable<T>> {
    private final long batchId;
    private Iterable<T> data;

    public BatchIterable(RequestResult requestResult) {
        batchId = requestResult.getBatchId();
    }

    @Override
    public void updateFrom(Iterable<T> newValue, RestAPI restApi) {
        this.data = newValue;
    }

    @Override
    public Iterator<T> iterator() {
        if (data==null) throw new IllegalStateException("Rest Batch Request has not been executed, results only available after successful execution.");
        return data.iterator();
    }
}
